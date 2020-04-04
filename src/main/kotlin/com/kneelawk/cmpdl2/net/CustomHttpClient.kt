package com.kneelawk.cmpdl2.net

import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import tornadofx.Rest

/**
 * Sets up TornatoFX's Rest client to use Apache HttpClient.
 */
fun setupRestEngine() {
    Rest.useApacheHttpClient()
}

/**
 * The connection manager for the custom client.
 */
private val customConnectionManager = PoolingHttpClientConnectionManager().apply {
    maxTotal = 100
    defaultMaxPerRoute = 100
}

/**
 * Custom http client with builtin redirect url sanitization and a max of 100 total connections running at once.
 */
val customClient: CloseableHttpClient =
        HttpClients.custom().setRedirectStrategy(RedirectUriSanitizer).setConnectionManager(customConnectionManager)
                .build()

/**
 * Closes the custom client.
 */
fun shutdownCustomClient() {
    customClient.close()
}
