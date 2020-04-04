package com.kneelawk.cmpdl2

import com.google.common.collect.ImmutableList
import com.kneelawk.cmpdl2.curse.AddonUtils
import com.kneelawk.cmpdl2.curse.ModLoaderDetector
import com.kneelawk.cmpdl2.curse.XmlModpackUtils
import com.kneelawk.cmpdl2.curse.modpack.ModpackFile
import com.kneelawk.cmpdl2.tasks.AddonDownloadTask
import com.kneelawk.cmpdl2.tasks.ModDownloadTask
import com.kneelawk.cmpdl2.tasks.ModpackDownloadTask
import com.kneelawk.cmpdl2.tasks.executeTask
import com.kneelawk.cmpdl2.util.ObjectPropertyWrapper
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.stage.FileChooser
import tornadofx.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Controller for the Curse Modpack Downloader main view.
 */
class CMPDL2MainController : Controller() {
    val running = SimpleBooleanProperty(false)
    val modpackLocation = SimpleStringProperty("")
    val modpackOutput = SimpleStringProperty("")
    val downloadThreads = SimpleIntegerProperty(10)
    val overallStatus = SimpleObjectProperty(DownloadStatus("Not Started.", null))
    var overallStatusWrapper: DownloadStatus by ObjectPropertyWrapper(overallStatus)
    val overallProgress = SimpleDoubleProperty(0.0)
    val modLoaders = SimpleStringProperty("")
    val downloadTasks: ObservableList<ModDownloadTask> = FXCollections.observableArrayList()

    val xmlModpackUtils: XmlModpackUtils by inject()
    val modLoaderDetector: ModLoaderDetector by inject()

    var previousDir = File(System.getProperty("user.home"))

    fun chooseModpackLocation() {
        chooseFile("Select a Modpack File", arrayOf(
            FileChooser.ExtensionFilter("Curse Modpack Files", ImmutableList.of("*.zip", "*.bin"))
        ), previousDir, FileChooserMode.Single).firstOrNull()?.let {
            val path = it.absolutePath
            modpackLocation.value = path
            previousDir = it.parentFile

            if (modpackOutput.value.isBlank()) {
                modpackOutput.value = path.substring(0, path.lastIndexOf('.'))
            }
        }
    }

    fun chooseModpackOutput() {
        chooseDirectory("Select an Output Location", previousDir)?.let {
            modpackOutput.value = it.absolutePath
            previousDir = it.parentFile
        }
    }

    fun downloadModpack() {
        val location = modpackLocation.value!!
        val output = modpackOutput.value!!

        if (location.isBlank()) {
            overallStatus.value = DownloadStatus("The modpack location field is empty.", DownloadError.MODPACK_FIELD)
            return
        }

        if (output.isBlank()) {
            overallStatus.value = DownloadStatus("The output location field is empty.", DownloadError.OUTPUT_FIELD)
            return
        }

        val modpackPath = Paths.get(location)
        val toDir = Paths.get(output)

        if (!Files.exists(modpackPath)) {
            overallStatus.value = DownloadStatus("The modpack zip doesn't exits.", DownloadError.MODPACK_FIELD)
            return
        }

        updateOverallStatus("Starting download...")
        running.value = true
        downloadTasks.clear()

        runAsync {
            val addonId = xmlModpackUtils.getModpackAddon(modpackPath)

            addonId?.let {
                updateOverallStatus("Downloading modpack zip...")
                val modpackZip = Files.createTempFile("modpack", ".zip")!!
                modpackZip.toFile().deleteOnExit()

                val addonDownload = AddonDownloadTask(it, modpackZip)
                runLater {
                    addonDownload.messageProperty().addListener { _, _, newValue -> updateOverallStatus(newValue) }
                }

                addonDownload.setOnSucceeded {
                    runModpackDownload(modpackZip, toDir)
                }

                addonDownload.fail {
                    updateOverallStatus("Modpack zip download failed.")
                    println("Download error: ${addonDownload.exception}")
                    running.value = false
                }

                executeTask(addonDownload)
            } ?: runModpackDownload(modpackPath, toDir)
        }
    }

    fun runModpackDownload(modpackZip: Path, toDir: Path) = runAsync {
        updateOverallStatus("Parsing modpack...")

        val modpackFile = ModpackFile(modpackZip)
        val manifest = modpackFile.readManifest()

        val modLoader = modLoaderDetector.detectModLoader(modpackFile)
        runLater {
            modLoaders.value = modLoader
        }

        updateOverallStatus("Extracting overides...")

        if (!Files.exists(toDir)) {
            Files.createDirectories(toDir)
        }

        modpackFile.extractOverrides(toDir)

        val modsDir = toDir.resolve("mods")
        if (!Files.exists(modsDir)) {
            Files.createDirectory(modsDir)
        }

        val modpackDownlad = ModpackDownloadTask(manifest, modsDir, downloadThreads.value)

        runLater {
            modpackDownlad.messageProperty().addListener { _, _, newValue -> updateOverallStatus(newValue) }
            overallProgress.bind(modpackDownlad.progressProperty())
            downloadTasks.bind(modpackDownlad.tasksProperty) { it }
        }

        modpackDownlad.fail {
            overallProgress.unbind()
            println("Modpack download error: ${modpackDownlad.exception}")
            running.value = false
        }

        modpackDownlad.success {
            overallProgress.unbind()
            running.value = false
        }

        executeTask(modpackDownlad)
    }

    fun updateOverallStatus(message: String) {
        overallStatusWrapper = DownloadStatus(message, null)
    }
}

/**
 * A class used to represent a status.
 */
data class DownloadStatus(val status: String, val error: DownloadError?) {
    override fun toString(): String {
        return status
    }
}

/**
 * What ui element was the cause of the error?
 */
enum class DownloadError {
    MODPACK_FIELD,
    OUTPUT_FIELD
}
