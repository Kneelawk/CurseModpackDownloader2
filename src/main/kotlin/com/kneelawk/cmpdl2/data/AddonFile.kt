package com.kneelawk.cmpdl2.data

import com.kneelawk.cmpdl2.data.curseapi.AddonFileData

/**
 * Describes an addon file along with its project id.
 */
data class AddonFile(override val projectID: Long, val fileData: AddonFileData) : AddonId {
    override val fileID = fileData.id
}