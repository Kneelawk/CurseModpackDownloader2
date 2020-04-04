package com.kneelawk.cmpdl2.tasks

import com.kneelawk.cmpdl2.curse.AddonUtils
import com.kneelawk.cmpdl2.curse.data.AddonFile
import com.kneelawk.cmpdl2.curse.data.AddonId
import com.kneelawk.cmpdl2.curse.data.MaybeAddonFile
import com.kneelawk.cmpdl2.net.CurseURIUtils
import com.kneelawk.cmpdl2.net.customClient
import com.kneelawk.cmpdl2.util.ObjectPropertyWrapper
import javafx.concurrent.Task
import org.apache.http.client.methods.HttpGet
import org.apache.http.util.EntityUtils
import tornadofx.Rest
import tornadofx.find
import tornadofx.objectProperty
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/**
 * A task for downloading individual mods based on project and file ids.
 */
class ModDownloadTask(private val addonId: AddonId, private val minecraftVersion: String, private val toDir: Path) :
        Task<ModDownloadResult>() {
    private val addonUtils = find<AddonUtils>()
    private val rest = find<Rest>()

    val fileProperty = objectProperty(MaybeAddonFile(addonId))
    var file by ObjectPropertyWrapper(fileProperty)

    init {
        updateMessage("Waiting ${addonId.projectID}/${addonId.fileID}...")
        updateProgress(-1, -1)
    }

    override fun call(): ModDownloadResult {
        updateProgress(-1, -1)

        val currentFile = file

        val addonFile = if (currentFile.fileData == null) {
            updateMessage("Getting info ${addonId.projectID}/${addonId.fileID}...")

            val addonFile = addonUtils.getAddonFileOrLatest(addonId, minecraftVersion)
                    ?: throw ModDownloadException("Failed to find file info for ${addonId.projectID}/${addonId.fileID}")

            file = MaybeAddonFile(addonId.projectID, addonFile)

            addonFile
        } else {
            currentFile.fileData
        }

        updateMessage("Downloading ${addonFile.fileName}... 0%")

        val unescapedUrl = addonFile.downloadURL

        val toFile = toDir.resolve(unescapedUrl.substring(unescapedUrl.lastIndexOf("/") + 1))

        val saneUri = CurseURIUtils.sanitizeCurseDownloadUri(unescapedUrl, true)

        val request = HttpGet(saneUri)

        customClient.execute(request).use { response ->
            if (response.statusLine.statusCode / 100 != 2) {
                throw ModDownloadException(
                    "Mod download status code: ${response.statusLine}, download url: ($unescapedUrl), sanitized: ($saneUri)")
            }

            val entity = response.entity
            val contentLength = entity.contentLength
            val input = entity.content
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
                    EntityUtils.consume(entity)
                    return ModDownloadResult(toFile, AddonFile(addonId.projectID, addonFile),
                        DownloadProgress(currentProgress, contentLength))
                }
            }

            return ModDownloadResult(toFile, AddonFile(addonId.projectID, addonFile),
                DownloadProgress(currentProgress, contentLength))
        }
    }
}

/**
 * Describes the result of a [ModDownloadTask].
 */
data class ModDownloadResult(val downloadTo: Path, val addon: AddonFile, val progress: DownloadProgress)

/**
 * An exception thrown if there is an error while downloading a mod.
 */
class ModDownloadException(message: String) : IOException(message)
