package com.kneelawk.cmpdl2.curse.data.curseapi

import com.kneelawk.cmpdl2.curse.data.reqJsonArray
import com.kneelawk.cmpdl2.curse.data.reqLong
import com.kneelawk.cmpdl2.curse.data.reqString
import tornadofx.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.json.JsonObject
import javax.json.JsonString

data class AddonFileJson(
        override var id: Long = 0,
        override var displayName: String = "",
        override var fileName: String = "",
        override var fileDate: LocalDateTime = LocalDateTime.now(),
        override var fileLength: Long = 0,

        /**
         * Known values are:
         * * 1: Release
         * * 2: Beta
         * * 3: Alpha
         */
        override var releaseType: Long = 1,

        /**
         * Known values are:
         * * 1: Normal
         * * 2: SemiNormal
         */
        override var fileStatus: Long = 1,
        override var downloadURL: String = "",
        override var isAlternate: Boolean = false,
        override var alternateFileID: Long? = null,
        override var dependencies: List<DependencyJson> = emptyList(),
        override var isAvailable: Boolean = true,
        override var packageFingerprint: Long? = null,
        override var gameVersion: List<String> = emptyList(),
        override var serverPackFileID: Long? = null,
        override var hasInstallScript: Boolean = false,
        override var gameVersionDateReleased: LocalDateTime? = null
) : JsonModel, AddonFileData {
    override fun toJSON(json: JsonBuilder) {
        with(json) {
            add("id", id)
            add("displayName", displayName)
            add("fileName", fileName)
            add("fileDate", fileDate)
            add("fileLength", fileLength)
            add("releaseType", releaseType)
            add("fileStatus", fileStatus)
            add("downloadUrl", downloadURL)
            add("isAlternate", isAlternate)
            alternateFileID?.let { add("alternateFileId", it) }
            add("dependencies", dependencies)
            add("isAvailable", isAvailable)
            packageFingerprint?.let { add("packageFingerprint", it) }
            add("gameVersion", gameVersion)
            serverPackFileID?.let { add("serverPackFileId", it) }
            add("hasInstallScript", hasInstallScript)
            gameVersionDateReleased?.let { add("gameVersionDateReleased", it) }
        }
    }

    override fun updateModel(json: JsonObject) {
        with(json) {
            id = reqLong("id")
            displayName = reqString("displayName")
            fileName = reqString("fileName")
            fileDate = LocalDateTime.parse(reqString("fileDate"), DateTimeFormatter.ISO_DATE_TIME)
            fileLength = reqLong("fileLength")
            releaseType = long("releaseType") ?: 1
            fileStatus = long("fileStatus") ?: 1
            downloadURL = reqString("downloadUrl")
            isAlternate = boolean("isAlternate") ?: false
            alternateFileID = long("alternateFileId")
            dependencies = jsonArray("dependencies")?.toModel() ?: emptyList()
            isAvailable = boolean("isAvailable") ?: true
            packageFingerprint = long("packageFingerprint")
            gameVersion = reqJsonArray("gameVersion").map { (it as JsonString).string }
            serverPackFileID = long("serverPackFileId")
            gameVersionDateReleased =
                    string("gameVersionDateReleased")?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME) }
        }
    }
}

data class DependencyJson(
        override var addonID: Long = 0,

        /**
         * Known values are:
         * * 1: Required
         * * 2: Optional
         * * 3: Embedded
         */
        override var type: Long = 1
) : JsonModel, DependencyData {
    override fun toJSON(json: JsonBuilder) {
        with(json) {
            add("addonId", addonID)
            add("type", type)
        }
    }

    override fun updateModel(json: JsonObject) {
        with(json) {
            addonID = reqLong("addonId")
            type = long("type") ?: 1
        }
    }
}
