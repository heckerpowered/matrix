/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.core.resource.ClasspathResourceProvider
import heckerpowered.matrix.core.resourceToString

class ShaderStageStore(
    private val discoveryRootDirectory: String = "assets/matrix/shaders/",
    private val resourceProvider: ClasspathResourceProvider = ClasspathResourceProvider.Default,
) {
    companion object {
        val Default = ShaderStageStore()
    }

    private val discoveredStageFiles: MutableMap<String, ShaderStage?> = mutableMapOf()
    val compiledStage: MutableMap<String, Int> = mutableMapOf()

    fun discoverFiles() {
        discoveredStageFiles.clear()
        val all = resourceProvider.listRecursively(discoveryRootDirectory)
        for (resourcePath in all) {
            val stageType = ShaderStage.detectByPath(resourcePath)
            discoveredStageFiles[resourcePath] = stageType
        }
    }

    fun precompileAll() {
        for ((resourcePath, _) in discoveredStageFiles) {
            val source = resourceToString("/$resourcePath")
            ShaderCompiler.addInclude(resourcePath.removePrefix(discoveryRootDirectory), source)
        }
        var failCount = 0
        for ((resourcePath, stageType) in discoveredStageFiles) {
            if (stageType == null) {
                continue
            }
            val source = resourceToString("/$resourcePath")
            try {
                val stage = ShaderCompilerV1.compileShader(source, stageType.shaderType)
                compiledStage["/$resourcePath"] = stage
            } catch (exception: Exception) {
                failCount++
            }
        }

        Matrix.LOGGER.info("${discoveredStageFiles.size - failCount} shaders pre-compiled, $failCount failed.")
    }
}