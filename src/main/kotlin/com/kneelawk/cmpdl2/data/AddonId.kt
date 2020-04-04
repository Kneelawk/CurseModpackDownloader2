package com.kneelawk.cmpdl2.data

/**
 * Interface describing something that can be used as a file id.
 */
interface AddonId {
    val projectID: Long
    val fileID: Long
}

/**
 * Simple file id implementation.
 */
data class SimpleAddonId(override val projectID: Long, override val fileID: Long) : AddonId
