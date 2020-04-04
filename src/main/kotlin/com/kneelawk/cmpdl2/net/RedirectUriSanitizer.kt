package com.kneelawk.cmpdl2.net

import org.apache.commons.codec.DecoderException
import org.apache.http.ProtocolException
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.DefaultRedirectStrategy
import org.apache.http.util.TextUtils
import java.net.URI
import java.net.URISyntaxException

/**
 * A sanitizer for redirect URIs encountered by the customized http client.
 */
object RedirectUriSanitizer : DefaultRedirectStrategy() {
    override fun createLocationURI(location: String?): URI {
        try {
            val b = URIBuilder(CurseURIUtils.sanitizeUri(location!!, true).normalize())

            b.host?.let {
                b.host = it.toLowerCase()
            }

            if (TextUtils.isEmpty(b.path)) {
                b.path = "/"
            }

            return b.build()
        } catch (e: URISyntaxException) {
            throw ProtocolException("Invalid redirect URI: $location", e)
        } catch (e: DecoderException) {
            throw ProtocolException("Invalid redirect URI: $location", e)
        }
    }
}