package com.kneelawk.cmpdl2.data.jumploader

import tornadofx.JsonBuilder

interface JumpLoaderData {
    val downloadRequiredFiles: Boolean
    val forceFallbackStorage: Boolean
    val overrideInferredSide: Boolean
    val disableUI: Boolean
    val launch: LaunchData
    val jars: JarsData
    val autoconfig: AutoconfigData

    fun toJSON(json: JsonBuilder)
}

interface AutoconfigData {
    val enable: Boolean
    val handler: String
    val forceUpdate: Boolean

    fun toJSON(json: JsonBuilder)
}

interface JarsData {
    val minecraft: List<MinecraftData>
    val maven: List<MavenData>

    fun toJSON(json: JsonBuilder)
}

interface MavenData {
    val mavenPath: String
    val repoURL: String

    fun toJSON(json: JsonBuilder)
}

interface MinecraftData {
    val gameVersion: String
    val downloadType: String

    fun toJSON(json: JsonBuilder)
}

interface LaunchData {
    val mainClass: String

    fun toJSON(json: JsonBuilder)
}
