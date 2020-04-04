package com.kneelawk.cmpdl2.tasks

import com.kneelawk.cmpdl2.curse.data.AddonFile
import com.kneelawk.cmpdl2.curse.data.MaybeAddonFile
import com.kneelawk.cmpdl2.curse.data.manifest.FileData
import com.kneelawk.cmpdl2.curse.data.manifest.ManifestData
import javafx.application.Platform
import javafx.beans.property.ReadOnlyIntegerProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.concurrent.Task
import tornadofx.getValue
import tornadofx.intProperty
import tornadofx.listProperty
import tornadofx.runLater
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

/**
 * A task for downloading and extracting entire modpacks.
 */
class ModpackDownloadTask(private val manifest: ManifestData, private val modsDir: Path, private val numThreads: Int) :
        Task<ModpackDownloadResult>() {

    val tasksProperty = listProperty<ModDownloadTask>(FXCollections.observableArrayList())
    val tasks: ObservableList<ModDownloadTask> by tasksProperty

    val totalDownloadsProperty: ReadOnlyIntegerProperty = intProperty(manifest.files.size)
    val totalDownloads: Int by totalDownloadsProperty

    val successfulDownloadsProperty = listProperty<AddonFile>(FXCollections.observableArrayList())
    val successfulDownloads: ObservableList<AddonFile> by successfulDownloadsProperty

    val failedDownloadsProperty = listProperty<MaybeAddonFile>(FXCollections.observableArrayList())
    val failedDownloads: ObservableList<MaybeAddonFile> by failedDownloadsProperty

    private fun addTask(t: ModDownloadTask) {
        if (Platform.isFxApplicationThread()) {
            tasks.add(t)
        } else runLater {
            tasks.add(t)
        }
    }

    private fun removeTask(t: ModDownloadTask) {
        if (Platform.isFxApplicationThread()) {
            tasks.remove(t)
        } else runLater {
            tasks.remove(t)
        }
    }

    private fun addSuccessfulDownload(addonFile: AddonFile) {
        if (Platform.isFxApplicationThread()) {
            successfulDownloads.add(addonFile)
        } else runLater {
            successfulDownloads.add(addonFile)
        }
    }

    private fun addFailedDownload(addonFile: MaybeAddonFile) {
        if (Platform.isFxApplicationThread()) {
            failedDownloads.add(addonFile)
        } else runLater {
            failedDownloads.add(addonFile)
        }
    }

    override fun call(): ModpackDownloadResult {
        val executor = Executors.newFixedThreadPool(numThreads) { Thread(it).apply { isDaemon = true } }

        if (!Files.exists(modsDir)) {
            Files.createDirectories(modsDir)
        }

        val latch = CountDownLatch(totalDownloads)
        val successCount = AtomicLong(0)

        updateMessage("Downloading mods... 0 / $totalDownloads")
        manifest.files.forEach {
            if (it.required) {
                startModDownload(executor, latch, successCount, it, manifest.minecraft.version, modsDir)
            }
        }

        latch.await()

        executor.shutdown()

        return ModpackDownloadResult(manifest, modsDir, successfulDownloads, failedDownloads)
    }

    private fun startModDownload(executor: ExecutorService, latch: CountDownLatch, successCount: AtomicLong,
                                 file: FileData, minecraftVersion: String, modsDir: Path) {
        val modDownloadTask = ModDownloadTask(file, minecraftVersion, modsDir)
        addTask(modDownloadTask)
        modDownloadTask.setOnSucceeded {
            // Add this download to the list of successful downloads and update this tasks's progress.
            addSuccessfulDownload(modDownloadTask.get().addon)
            latch.countDown()

            val count = successCount.incrementAndGet()
            updateProgress(count, totalDownloads.toLong())
            updateMessage("Downloading mods... $count / $totalDownloads")
        }
        modDownloadTask.setOnFailed {
            printModDownloadErrorInfo(modDownloadTask)
            // Is this an error we can recover from?
            if (modDownloadTask.exception is ModDownloadException) {
                // A ModDownloadException means that there was some fundamental problem with the mod
                // specified, like it being missing.
                addFailedDownload(modDownloadTask.file)
                latch.countDown()
            } else {
                // Otherwise, retry the download.
                removeTask(modDownloadTask)
                startModDownload(executor, latch, successCount, file, minecraftVersion, modsDir)
            }
        }
        modDownloadTask.setOnCancelled {
            latch.countDown()
        }
        executor.execute(modDownloadTask)
    }

    private fun printModDownloadErrorInfo(modDownloadTask: ModDownloadTask) {
        val file = modDownloadTask.file
        val fileInfo = file.fileData?.downloadURL ?: "${file.projectID}/${file.fileID}"
        println("Mod download error: ${modDownloadTask.exception}\n" +
                "\tFile: $fileInfo")
        // Gives error in the IDE
//        modDownloadTask.exception.printStackTrace()
    }
}

/**
 * The result of a modpack download task.
 */
data class ModpackDownloadResult(val manifest: ManifestData, val modsDir: Path,
                                 val successfulDownloads: List<AddonFile>, val failedDownloads: List<MaybeAddonFile>)
