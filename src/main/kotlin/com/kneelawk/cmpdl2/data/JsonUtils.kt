package com.kneelawk.cmpdl2.data

import tornadofx.jsonArray
import tornadofx.jsonObject
import tornadofx.long
import tornadofx.string
import java.io.IOException
import javax.json.JsonArray
import javax.json.JsonObject

/**
 * Gets a the [Long] associated with [key] and throws a [JsonParseException] if it is missing or `null`.
 */
fun JsonObject.reqLong(vararg key: String): Long {
    return long(*key) ?: jsonNull(*key)
}

/**
 * Gets a [String] associated with [key] and throws a [JsonParseException] if it is missing or `null`.
 */
fun JsonObject.reqString(vararg key: String): String {
    return string(*key) ?: jsonNull(*key)
}

/**
 * Gets a [JsonArray] associated with [key] and throws a [JsonParseException] if it is missing or `null`.
 */
fun JsonObject.reqJsonArray(vararg key: String): JsonArray {
    return jsonArray(*key) ?: jsonNull(*key)
}

/**
 * Gets a [JsonObject] associated with [key] and throws a [JsonParseException] if it is missing or `null`.
 */
fun JsonObject.reqJsonObject(vararg key: String): JsonObject {
    return jsonObject(*key) ?: jsonNull(*key)
}

/**
 * Throws a [JsonParseException] indicating that the value for the given [key] was missing or `null`.
 */
fun jsonNull(vararg key: String): Nothing {
    throw JsonParseException("Json missing or null value for key(s): ${listOf(*key)}")
}

/**
 * Used to indicate an error while parsing Json.
 */
class JsonParseException(message: String) : IOException(message)
