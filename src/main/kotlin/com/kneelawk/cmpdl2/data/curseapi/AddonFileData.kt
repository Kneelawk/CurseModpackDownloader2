package com.kneelawk.cmpdl2.data.curseapi

import tornadofx.JsonBuilder
import java.time.LocalDateTime

/**
 * Immutable interface for describing a curse addon file.
 */
interface AddonFileData {
    val id: Long
    val displayName: String
    val fileName: String
    val fileDate: LocalDateTime
    val fileLength: Long

    /**
     * Known values are:
     * * 1: Release
     * * 2: Beta
     * * 3: Alpha
     */
    val releaseType: Long

    /**
     * Known values are:
     * * 1: Normal
     * * 2: SemiNormal
     */
    val fileStatus: Long
    val downloadURL: String
    val isAlternate: Boolean
    val alternateFileID: Long?
    val dependencies: List<DependencyJson>
    val isAvailable: Boolean
    val packageFingerprint: Long?
    val gameVersion: List<String>
    val serverPackFileID: Long?
    val hasInstallScript: Boolean
    val gameVersionDateReleased: LocalDateTime?

    fun toJSON(json: JsonBuilder)
}

/**
 * Immutable interface describing a dependency.
 */
interface DependencyData {
    val addonID: Long

    /**
     * Known values are:
     * * 1: Required
     * * 2: Optional
     * * 3: Embedded
     */
    val type: Long

    fun toJSON(json: JsonBuilder)
}
