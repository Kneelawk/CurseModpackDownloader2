package com.kneelawk.cmpdl2.curse

import com.kneelawk.cmpdl2.curse.data.AddonId
import com.kneelawk.cmpdl2.curse.data.curseapi.AddonFileJson
import tornadofx.Controller
import tornadofx.Rest
import tornadofx.toModel
import java.time.LocalDateTime

/**
 * Used to get information about addons.
 */
class AddonUtils : Controller() {
    companion object {
        const val GET_ADDON_FILE_INFO_FORMAT = "https://addons-ecs.forgesvc.net/api/v2/addon/%d/file/%d"
        const val GET_ADDON_FILES_FORMAT = "https://addons-ecs.forgesvc.net/api/v2/addon/%d/files"
    }

    private val rest: Rest by inject()

    fun getAddonFile(addonId: AddonId): AddonFileJson? {
        val response = rest.get(String.format(
            GET_ADDON_FILE_INFO_FORMAT, addonId.projectID, addonId.fileID))
        return if (response.status == Rest.Response.Status.OK) {
            response.one().toModel()
        } else {
            null
        }
    }

    fun getAddonFiles(projectId: Long): List<AddonFileJson> {
        return rest.get(String.format(
            GET_ADDON_FILES_FORMAT, projectId)).list().toModel()
    }

    fun getLatestAddonFile(projectId: Long, minecraftVersion: String): AddonFileJson? {
        var newest: AddonFileJson? = null
        var newestDate: LocalDateTime? = null

        getAddonFiles(projectId).forEach { file ->
            if (file.gameVersion.contains(minecraftVersion) && (newestDate == null || file.fileDate.isAfter(
                        newestDate))) {
                newest = file
                newestDate = file.fileDate
            }
        }

        return newest
    }

    fun getAddonFileOrLatest(addonId: AddonId, minecraftVersion: String): AddonFileJson? {
        return getAddonFile(addonId) ?: getLatestAddonFile(addonId.projectID, minecraftVersion)
    }
}