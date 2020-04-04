package com.kneelawk.cmpdl2

import com.kneelawk.cmpdl2.tasks.ModDownloadTask
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import tornadofx.*

/**
 * Curse Modpack Downloader 2 Main View.
 */
class CMPDL2MainView : View("Curse Modpack Downloader 2") {
    private val cont: CMPDL2MainController by inject()

    init {
        with(primaryStage) {
            width = 1280.0
            height = 800.0
            minWidth = 500.0
            minHeight = 400.0
        }
    }

    override val root = vbox {
        spacing = 10.0
        padding = insets(25.0)

        gridpane {
            alignment = Pos.TOP_CENTER
            hgap = 10.0
            vgap = 10.0
            disableProperty().bind(cont.running)

            row {
                label("Modpack Location:")
                textfield(cont.modpackLocation) {
                    cont.overallStatus.addListener { _, _, newValue ->
                        if (newValue.error == DownloadError.MODPACK_FIELD) {
                            styleClass += "error-field"
                        } else {
                            styleClass -= "error-field"
                        }
                    }
                }
                button("...") {
                    action {
                        cont.chooseModpackLocation()
                    }
                }
            }

            row {
                label("Output Location:")
                textfield(cont.modpackOutput) {
                    cont.overallStatus.addListener { _, _, newValue ->
                        if (newValue.error == DownloadError.OUTPUT_FIELD) {
                            styleClass += "error-field"
                        } else {
                            styleClass -= "error-field"
                        }
                    }
                }
                button("...") {
                    action {
                        cont.chooseModpackOutput()
                    }
                }
            }

            row {
                label("Number of Download Threads:")
                spinner(1, 100, 10, property = cont.downloadThreads) {
                    maxWidth = Double.MAX_VALUE
                    gridpaneConstraints {
                        columnSpan = 2
                    }
                }
            }

            row {
                button("Download Modpack") {
                    action {
                        cont.downloadModpack()
                    }
                    maxWidth = Double.MAX_VALUE
                    gridpaneConstraints {
                        columnSpan = 3
                    }
                }
            }

            constraintsForColumn(0).hgrow = Priority.NEVER
            constraintsForColumn(1).hgrow = Priority.ALWAYS
            constraintsForColumn(2).hgrow = Priority.NEVER
        }

        hbox {
            alignment = Pos.CENTER
            spacing = 10.0
            label("Mod Loaders:") {
                alignment = Pos.CENTER
            }
            textfield(cont.modLoaders) {
                isEditable = false
                hgrow = Priority.ALWAYS
            }
        }

        label(cont.overallStatus) {
            alignment = Pos.CENTER
            cont.overallStatus.addListener { _, _, newValue ->
                if (newValue.error != null) {
                    styleClass += "error-label"
                } else {
                    styleClass -= "error-label"
                }
            }
        }

        progressbar(cont.overallProgress) {
            maxWidth = Double.MAX_VALUE
            minHeight = 20.0
            cont.overallProgress.addListener { _, _, newValue ->
                if (newValue.toDouble() >= 1) {
                    addPseudoClass("done")
                } else {
                    removePseudoClass("done")
                }
            }
        }

        tableview(cont.downloadTasks) {
            column("Status", ModDownloadTask::messageProperty).apply {
                prefWidth = 500.0
            }
            column("Progress", ModDownloadTask::progressProperty).apply {
                prefWidth = 300.0
                cellFactory = ColoredProgressBarTableCell.forTableColumn()
            }
            column("Error", ModDownloadTask::exceptionProperty).apply {
                prefWidth = 500.0
            }
            vgrow = Priority.ALWAYS
        }
    }
}
