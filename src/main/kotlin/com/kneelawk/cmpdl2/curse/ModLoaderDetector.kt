package com.kneelawk.cmpdl2.curse

import com.kneelawk.cmpdl2.curse.modpack.ModpackFile
import com.kneelawk.cmpdl2.data.jumploader.JumpLoaderJson
import tornadofx.Controller
import tornadofx.toModel
import java.nio.file.Files
import java.util.stream.Collectors
import javax.json.Json

/**
 * Controller that detects a modpack's mod loader given its manifest and configs.
 */
class ModLoaderDetector : Controller() {
    companion object {
        const val JUMP_LOADER_PROJECT_ID = 361988L
        const val FABRIC_GROUP = "net.fabricmc"
        const val FABRIC_ID = "fabric-loader"
    }

    fun detectModLoader(modpackFile: ModpackFile): String {
        val overrides = modpackFile.readOverrides()
        val jumpLoaderConfig = overrides.resolve("config/jumploader.json")
        if (Files.exists(jumpLoaderConfig)) {
            val jumpLoaderJson: JumpLoaderJson =
                    Json.createReader(Files.newBufferedReader(jumpLoaderConfig)).use { it.readObject() }.toModel()

            val fabricLoader = jumpLoaderJson.jars.maven.find {
                val split = it.mavenPath.split(":")
                split.size == 3 && split[0] == FABRIC_GROUP && split[1] == FABRIC_ID
            }
            fabricLoader?.let {
                val split = it.mavenPath.split(":")
                return "fabric-loader: ${split[2]}"
            }

            return "unknown main-class: ${jumpLoaderJson.launch.mainClass}"
        }

        val manifest = modpackFile.readManifest()
        if (manifest.files.find { it.projectID == JUMP_LOADER_PROJECT_ID } != null) {
            return "fabric-loader: latest for ${manifest.minecraft.version}"
        }

        if (manifest.minecraft.modLoaders.isNotEmpty()) {
            return manifest.minecraft.modLoaders.stream().map { it.id }.collect(Collectors.joining(", "))
        }

        return "none"
    }
}