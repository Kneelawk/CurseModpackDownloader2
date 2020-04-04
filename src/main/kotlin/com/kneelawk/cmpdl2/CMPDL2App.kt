package com.kneelawk.cmpdl2

import com.kneelawk.cmpdl2.net.setupRestEngine
import com.kneelawk.cmpdl2.net.shutdownCustomClient
import com.kneelawk.cmpdl2.tasks.shutdownThreadPool
import tornadofx.App
import tornadofx.importStylesheet
import tornadofx.launch

/**
 * Entry point.
 */
fun main(args: Array<String>) {
    launch<CMPDL2App>(args)
}

/**
 * App class.
 */
class CMPDL2App : App(CMPDL2MainView::class) {
    init {
        importStylesheet(javaClass.getResource("obsidian/obsidian.css").toExternalForm())
        importStylesheet(javaClass.getResource("style.css").toExternalForm())
        setupRestEngine()
    }

    override fun stop() {
        super.stop()
        shutdownThreadPool()
        shutdownCustomClient()
    }
}
