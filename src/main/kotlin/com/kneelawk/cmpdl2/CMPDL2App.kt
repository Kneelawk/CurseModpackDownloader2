package com.kneelawk.cmpdl2

import tornadofx.App
import tornadofx.View
import tornadofx.launch
import tornadofx.vbox

/**
 * Entry point.
 */
fun main(args: Array<String>) {
    launch<CurseModpackDownloader2App>(args)
}

/**
 * App class.
 */
class CurseModpackDownloader2App : App(CurseModpackDownloader2MainView::class)

/**
 * Curse Modpack Downloader 2 Main View.
 */
class CurseModpackDownloader2MainView : View("Curse Modpack Downloader 2") {
    override val root = vbox { }
}
