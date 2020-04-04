package com.kneelawk.cmpdl2.curse

import com.kneelawk.cmpdl2.curse.data.AddonFile
import com.kneelawk.cmpdl2.curse.data.AddonId
import com.kneelawk.cmpdl2.curse.data.SimpleAddonId
import org.w3c.dom.Element
import org.xml.sax.SAXException
import tornadofx.Controller
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Utilities used for getting information about an XML modpack descriptor.
 */
class XmlModpackUtils : Controller() {
    val addonUtils: AddonUtils by inject()

    companion object {
        private val documentBuilderFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        private val documentBuilder: DocumentBuilder = documentBuilderFactory.newDocumentBuilder()
    }

    fun parseModpackBin(modpack: Path): AddonId? {
        try {
            val doc = Files.newInputStream(modpack).use { documentBuilder.parse(it) }
            val packageList = doc.getElementsByTagName("package")
            if (packageList.length > 0) {
                val pack = packageList.item(0) as Element
                val projectList = pack.getElementsByTagName("project")
                if (projectList.length > 0) {
                    val project = projectList.item(0) as Element
                    val projectId = project.getAttribute("id").toLong()
                    val fileId = project.getAttribute("file").toLong()
                    return SimpleAddonId(projectId, fileId)
                }
            }
        } catch (e: SAXException) {
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
        return null
    }

    fun getModpackAddon(modpack: Path): AddonFile? {
        return parseModpackBin(modpack)?.let { addonId ->
            addonUtils.getAddonFile(addonId)?.let { addonFileJson ->
                AddonFile(addonId.projectID, addonFileJson)
            }
        }
    }
}