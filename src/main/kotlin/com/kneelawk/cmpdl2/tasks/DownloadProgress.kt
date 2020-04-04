package com.kneelawk.cmpdl2.tasks

/**
 * Describes the current progress of a download.
 */
data class DownloadProgress(val currentProgress: Long, val contentLength: Long)