package com.kneelawk.cmpdl2.tasks

import javafx.concurrent.Task
import java.util.concurrent.Executors

/**
 * A private executor for executing this application's tasks.
 */
private val executor = Executors.newCachedThreadPool { Thread(it).apply { isDaemon = true } }

/**
 * Shuts down the application task executor.
 */
internal fun shutdownThreadPool() {
    executor.shutdown()
}

/**
 * Runs a task in the executor.
 */
fun executeTask(task: Task<*>) {
    executor.execute(task)
}
