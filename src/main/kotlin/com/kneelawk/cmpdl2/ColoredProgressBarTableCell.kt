package com.kneelawk.cmpdl2

import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.util.Callback
import tornadofx.addPseudoClass
import tornadofx.progressbar
import tornadofx.removePseudoClass

/**
 * Specialized TableCell that holds a ProgressBar indicating a number value.
 */
class ColoredProgressBarTableCell<S> : TableCell<S, Number>() {
    companion object {

        /**
         * Creates a callback for creating ColoredProgressBarTableCells.
         */
        fun <S> forTableColumn(): Callback<TableColumn<S, Number>, TableCell<S, Number>> {
            return Callback<TableColumn<S, Number>, TableCell<S, Number>> { ColoredProgressBarTableCell() }
        }
    }

    /**
     * The ProgressBar graphic node.
     */
    private val progressBar = progressbar(0.0) {
        maxWidth = Double.MAX_VALUE
    }

    init {
        styleClass += "progress-bar-table-cell"
    }

    /**
     * Updates this cell's value.
     */
    override fun updateItem(item: Number?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty) {
            graphic = null
        } else {
            progressBar.progressProperty().unbind()

            val progress = tableColumn?.getCellObservableValue(index)

            if (progress != null) {
                progressBar.progressProperty().bind(progress)
                if (progress.value.toDouble() >= 1) {
                    progressBar.addPseudoClass("done")
                } else {
                    progressBar.removePseudoClass("done")
                }
            } else {
                progressBar.progress = item!!.toDouble()
                if (item.toDouble() >= 1) {
                    progressBar.addPseudoClass("done")
                } else {
                    progressBar.removePseudoClass("done")
                }
            }

            graphic = progressBar
        }
    }
}
