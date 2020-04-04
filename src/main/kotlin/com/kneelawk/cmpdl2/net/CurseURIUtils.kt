package com.kneelawk.cmpdl2.net

import org.apache.commons.codec.DecoderException
import java.net.URI
import java.net.URISyntaxException
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.util.*
import java.util.regex.Pattern

/**
 * Utility for sanitizing curse urls.
 *
 * Curse urls are especially nasty because they tend to mix escaped and unescaped characters.
 * This is a dedicated utility for properly escaping a curse url.
 */
object CurseURIUtils {
    private val FULL_URI_PATTERN: Pattern = Pattern.compile(
        "^(?<scheme>[^/]+)://(?<host>[^/]+)(?<path>/.*)?$")
    private const val FULL_URI_FORMAT = "%s://%s%s"
    private val HOSTLESS_URI_PATTERN: Pattern =
            Pattern.compile("^(?<scheme>[^/]+)://(?<path>/.*)$")
    private const val HOSTLESS_URI_FORMAT = "%s://%s"
    private val HOST_RELATIVE_URI_PATTERN: Pattern =
            Pattern.compile("^//(?<host>[^/]+)(?<path>/.*)?$")
    private val HOSTLESS_RELATIVE_URI_PATTERN: Pattern =
            Pattern.compile("^//(?<path>/.*)$")
    private val PATH_RELATIVE_URI_PATTERN: Pattern = Pattern.compile("^(?<path>/.*)$")
    private val UTF_8: Charset = Charset.forName("UTF-8")

    /*
     * ### COPIED FROM ### Apache Commons Http Client:
     * org.apache.http.client.utils.URLEncodedUtils.java
     */

    /**
     * Unreserved characters, i.e. alphanumeric, plus: `_ - ! . ~ ' ( ) *`
     *
     *
     * This list is the same as the `unreserved` list in
     * [RFC 2396](http://www.ietf.org/rfc/rfc2396.txt)
     */
    private val UNRESERVED = BitSet(256)

    /**
     * Punctuation characters: , ; : $ & + =
     *
     *
     * These are the additional characters allowed by userinfo.
     */
    private val PUNCT = BitSet(256)

    /**
     * Characters which are safe to use in userinfo, i.e. [.UNRESERVED]
     * plus [.PUNCT]uation
     */
    private val USERINFO = BitSet(256)

    /**
     * Characters which are safe to use in a path, i.e. [.UNRESERVED] plus
     * [.PUNCT]uation plus / @
     */
    private val PATHSAFE = BitSet(256)

    /**
     * Characters which are safe to use in an encoded path, i.e.
     * [.PATHSAFE] plus %
     */
    private val ENCPATHSAFE = BitSet(256)

    /**
     * Characters which are safe to use in a query or a fragment, i.e.
     * [.RESERVED] plus [.UNRESERVED]
     */
    private val URIC = BitSet(256)

    /**
     * Reserved characters, i.e. `;/?:@&=+$,[]`
     *
     *
     * This list is the same as the `reserved` list in
     * [RFC 2396](http://www.ietf.org/rfc/rfc2396.txt) as augmented
     * by [RFC 2732](http://www.ietf.org/rfc/rfc2732.txt)
     */
    private val RESERVED = BitSet(256)

    /**
     * Safe characters for x-www-form-urlencoded data, as per
     * java.net.URLEncoder and browser behaviour, i.e. alphanumeric plus
     * `"-", "_", ".", "*"`
     */
    private val URLENCODER = BitSet(256)
    private const val RADIX = 16

    init {
        // unreserved chars
        // alpha characters
        for (i in 'a'.toInt()..'z'.toInt()) {
            UNRESERVED.set(i)
        }
        for (i in 'A'.toInt()..'Z'.toInt()) {
            UNRESERVED.set(i)
        }
        // numeric characters
        for (i in '0'.toInt()..'9'.toInt()) {
            UNRESERVED.set(i)
        }
        UNRESERVED.set('_'.toInt()) // these are the charactes of the "mark" list
        UNRESERVED.set('-'.toInt())
        UNRESERVED.set('.'.toInt())
        UNRESERVED.set('*'.toInt())
        URLENCODER.or(UNRESERVED) // skip remaining unreserved characters
        UNRESERVED.set('!'.toInt())
        UNRESERVED.set('~'.toInt())
        UNRESERVED.set('\''.toInt())
        UNRESERVED.set('('.toInt())
        UNRESERVED.set(')'.toInt())
        // punct chars
        PUNCT.set(','.toInt())
        PUNCT.set(';'.toInt())
        PUNCT.set(':'.toInt())
        PUNCT.set('$'.toInt())
        PUNCT.set('&'.toInt())
        PUNCT.set('+'.toInt())
        PUNCT.set('='.toInt())
        // Safe for userinfo
        USERINFO.or(UNRESERVED)
        USERINFO.or(PUNCT)

        // URL path safe
        PATHSAFE.or(UNRESERVED)
        PATHSAFE.set('/'.toInt()) // segment separator
        PATHSAFE.set(';'.toInt()) // param separator
        PATHSAFE.set(':'.toInt()) // rest as per list in 2396, i.e. : @ & = + $ ,
        PATHSAFE.set('@'.toInt())
        PATHSAFE.set('&'.toInt())
        PATHSAFE.set('='.toInt())
        PATHSAFE.set('+'.toInt())
        PATHSAFE.set('$'.toInt())
        PATHSAFE.set(','.toInt())
        ENCPATHSAFE.or(PATHSAFE)
        ENCPATHSAFE.set('%'.toInt())
        RESERVED.set(';'.toInt())
        RESERVED.set('/'.toInt())
        RESERVED.set('?'.toInt())
        RESERVED.set(':'.toInt())
        RESERVED.set('@'.toInt())
        RESERVED.set('&'.toInt())
        RESERVED.set('='.toInt())
        RESERVED.set('+'.toInt())
        RESERVED.set('$'.toInt())
        RESERVED.set(','.toInt())
        RESERVED.set('['.toInt()) // added by RFC 2732
        RESERVED.set(']'.toInt()) // added by RFC 2732
        URIC.or(RESERVED)
        URIC.or(UNRESERVED)
    }

    /*
     * ### END OF COPY ###
     */
    @Throws(URISyntaxException::class, DecoderException::class)
    fun sanitizeCurseDownloadUri(insaneUri: String,
                                 escapePath: Boolean): URI {
        return sanitizeUri("https", "files.forgecdn.net", "/files/", insaneUri,
            escapePath)
    }

    @Throws(URISyntaxException::class, DecoderException::class)
    fun sanitizeUri(insaneUri: String, escapePath: Boolean): URI {
        return sanitizeUri(null, null, null, insaneUri, escapePath)
    }

    @Throws(URISyntaxException::class, DecoderException::class)
    fun sanitizeUri(baseScheme: String?, baseHost: String?,
                    basePath: String?, insaneUri: String, escapePath: Boolean): URI {
        val fullUri = FULL_URI_PATTERN.matcher(insaneUri)
        val hostlessUri = HOSTLESS_URI_PATTERN.matcher(insaneUri)
        val hostRelativeUri = HOST_RELATIVE_URI_PATTERN.matcher(insaneUri)
        val hostlessRelativeUri =
                HOSTLESS_RELATIVE_URI_PATTERN.matcher(insaneUri)
        val pathRelativeUri = PATH_RELATIVE_URI_PATTERN.matcher(insaneUri)
        return when {
            fullUri.matches()             -> {
                val scheme = fullUri.group("scheme")
                val host = fullUri.group("host")
                var path = fullUri.group("path")
                if (escapePath) {
                    path = urlEncode(path, UTF_8, ENCPATHSAFE, false)
                }
                URI(String.format(FULL_URI_FORMAT, scheme, host, path))
            }
            hostlessUri.matches()         -> {
                val scheme = fullUri.group("scheme")
                var path = hostlessUri.group("path")
                if (escapePath) {
                    path = urlEncode(path, UTF_8, ENCPATHSAFE, false)
                }
                URI(String.format(HOSTLESS_URI_FORMAT, scheme, path))
            }
            hostRelativeUri.matches()     -> {
                val host = fullUri.group("host")
                var path = hostRelativeUri.group("path")
                if (escapePath) {
                    path = urlEncode(path, UTF_8, ENCPATHSAFE, false)
                }
                URI(String.format(FULL_URI_FORMAT, baseScheme, host, path))
            }
            hostlessRelativeUri.matches() -> {
                var path = hostlessRelativeUri.group("path")
                if (escapePath) {
                    path = urlEncode(path, UTF_8, ENCPATHSAFE, false)
                }
                URI(String.format(HOSTLESS_URI_FORMAT, baseScheme, path))
            }
            pathRelativeUri.matches()     -> {
                var path = pathRelativeUri.group("path")
                if (escapePath) {
                    path = urlEncode(path, UTF_8, ENCPATHSAFE, false)
                }
                URI(String.format(FULL_URI_FORMAT, baseScheme, baseHost, path))
            }
            else                          -> {
                URI(baseScheme, baseHost, basePath, null).resolve(
                    if (escapePath) urlEncode(insaneUri, UTF_8, ENCPATHSAFE,
                        false) else insaneUri)
            }
        }
    }

    /*
     * ### COPIED FROM ### Apache Commons Http Client:
     * org.apache.http.client.utils.URLEncodedUtils.java
     */
    private fun urlEncode(content: String, charset: Charset,
                          safechars: BitSet, blankAsPlus: Boolean): String {
        val buf = StringBuilder()
        val bb = charset.encode(content)
        while (bb.hasRemaining()) {
            val b: Int = bb.get().toInt() and 0xff
            if (safechars[b]) {
                buf.append(b.toChar())
            } else if (blankAsPlus && b == ' '.toInt()) {
                buf.append('+')
            } else {
                buf.append("%")
                val hex1 = Character
                        .toUpperCase(Character.forDigit(b shr 4 and 0xF, RADIX))
                val hex2 = Character
                        .toUpperCase(Character.forDigit(b and 0xF, RADIX))
                buf.append(hex1)
                buf.append(hex2)
            }
        }
        return buf.toString()
    }

    /**
     * Decode/unescape a portion of a URL, to use with the query part ensure
     * `plusAsBlank` is true.
     *
     * @param content     the portion to decode
     * @param charset     the charset to use
     * @param plusAsBlank if `true`, then convert '+' to space (e.g. for
     * www-url-form-encoded content), otherwise leave as is.
     * @return encoded string
     */
    fun urlDecode(content: String, charset: Charset,
                  safechars: BitSet, plusAsBlank: Boolean): String {
        val bb = ByteBuffer.allocate(content.length)
        val cb = CharBuffer.wrap(content)
        while (cb.hasRemaining()) {
            val c = cb.get()
            if (c == '%' && cb.remaining() >= 2) {
                val uc = cb.get()
                val lc = cb.get()
                val u = Character.digit(uc, 16)
                val l = Character.digit(lc, 16)
                val nch = ((u shl 4) + l).toByte()
                if (u != -1 && l != -1 && !safechars[nch.toInt()]) {
                    bb.put(nch)
                } else {
                    bb.put('%'.toByte())
                    bb.put(uc.toByte())
                    bb.put(lc.toByte())
                }
            } else if (plusAsBlank && c == '+') {
                bb.put(' '.toByte())
            } else {
                bb.put(c.toByte())
            }
        }
        bb.flip()
        return charset.decode(bb).toString()
    }

    /*
     * ### END OF COPY ###
     */
}