package com.kneelawk.cmpdl2.tasks

import com.kneelawk.cmpdl2.curse.AddonUtils
import com.kneelawk.cmpdl2.data.AddonFile
import com.kneelawk.cmpdl2.data.AddonId
import com.kneelawk.cmpdl2.data.MaybeAddonFile
import com.kneelawk.cmpdl2.util.ObjectPropertyWrapper
import javafx.concurrent.Task
import tornadofx.Rest
import tornadofx.find
import tornadofx.objectProperty
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/**
 * A task for downloading individual addons based on project and file ids.
 *
 * This task is very similar to the [ModDownloadTask] except that it downloads to a file instead of a directory.
 */
class AddonDownloadTask(private val addonId: AddonId, private val toFile: Path) :
        Task<AddonDownloadResult>() {
    private val addonUtils = find<AddonUtils>()
    private val rest = find<Rest>()

    val fileProperty = objectProperty(MaybeAddonFile(addonId))
    var file by ObjectPropertyWrapper(fileProperty)

    init {
        updateMessage("Waiting ${addonId.projectID}/${addonId.fileID}...")
        updateProgress(-1, -1)
    }

    override fun call(): AddonDownloadResult {
        updateProgress(-1, -1)

        val currentFile = file

        val addonFile = if (currentFile.fileData == null) {
            updateMessage("Getting info ${addonId.projectID}/${addonId.fileID}...")

            val addonFile = addonUtils.getAddonFile(addonId)
                    ?: throw AddonDownloadException(
                        "Failed to find file info for ${addonId.projectID}/${addonId.fileID}")

            file = MaybeAddonFile(addonId.projectID, addonFile)

            addonFile
        } else {
            currentFile.fileData
        }

        updateMessage("Downloading ${addonFile.fileName}... 0%")

        val unescapedUrl = addonFile.downloadURL

        // TODO sanitize download urls
        val response = rest.get(unescapedUrl)

        if (response.status != Rest.Response.Status.OK) {
            throw AddonDownloadException(
                "Mod download status code: ${response.status} ${response.reason}, download url: $unescapedUrl")
        }

        val contentLength = response.header("Content-Length")!!.toLong()
        val input = response.content()
        val output = Files.newOutputStream(toFile)

        updateMessage("Downloading ${addonFile.fileName}... 0%")
        updateProgress(0L, contentLength)

        var currentProgress = 0L
        val buf = ByteArray(8192)
        var len: Int
        while (input.read(buf).also { len = it } >= 0) {
            output.write(buf, 0, len)

            currentProgress += len

            updateMessage("Downloading %s... %.1f%%".format(addonFile.fileName,
                currentProgress.toDouble() * 100.0 / contentLength.toDouble()))
            updateProgress(currentProgress, contentLength)

            if (isCancelled) {
                response.consume()
                return AddonDownloadResult(toFile, AddonFile(addonId.projectID, addonFile),
                    DownloadProgress(currentProgress, contentLength))
            }
        }

        return AddonDownloadResult(toFile, AddonFile(addonId.projectID, addonFile),
            DownloadProgress(currentProgress, contentLength))
    }
}

/**
 * Describes the result of an [AddonDownloadTask].
 */
data class AddonDownloadResult(val downloadTo: Path, val addon: AddonFile, val progress: DownloadProgress)

/**
 * An exception thrown if there is an error while downloading an addon.
 */
class AddonDownloadException(message: String) : IOException(message)
