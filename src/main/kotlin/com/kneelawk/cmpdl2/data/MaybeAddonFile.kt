package com.kneelawk.cmpdl2.data

import com.kneelawk.cmpdl2.data.curseapi.AddonFileData

/**
 * Object describing something that could describe an addon file or possibly just its addon id.
 */
data class MaybeAddonFile(val projectID: Long, val fileID: Long, val fileData: AddonFileData?) {
    constructor(projectID: Long, addonData: AddonFileData) : this(projectID, addonData.id, addonData)
    constructor(addonId: AddonId) : this(addonId.projectID, addonId.fileID, null)
}
