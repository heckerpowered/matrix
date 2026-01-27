/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core.resource

import java.io.File
import java.io.InputStream
import java.net.JarURLConnection
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.jar.JarFile

class ClasspathResourceProvider(
    private val classLoader: ClassLoader = Thread.currentThread().contextClassLoader,
) : ResourceProvider {
    companion object {
        val Default: ClasspathResourceProvider = ClasspathResourceProvider()
    }

    override fun open(resourcePath: String): InputStream? {
        return classLoader.getResourceAsStream(resourcePath.trimStart('/'))
    }

    fun listRecursively(resourceDirectory: String): List<String> {
        val normalized = resourceDirectory.trim('/')

        val discovered: MutableList<String> = mutableListOf()
        val urls: Enumeration<URL> = classLoader.getResources(normalized)
        while (urls.hasMoreElements()) {
            val directoryUrl = urls.nextElement()
            discoverFilesByProtocol(directoryUrl, discovered, normalized)
        }
        return discovered
    }

    private fun discoverFilesByProtocol(directoryUrl: URL, discovered: MutableList<String>, normalized: String) {
        when (directoryUrl.protocol) {
            "file" -> {
                val rootPath = Paths.get(directoryUrl.toURI())
                if (!Files.isDirectory(rootPath)) return
                Files.walk(rootPath).use { stream ->
                    stream.filter { Files.isRegularFile(it) }.forEach { filePath ->
                        val rel = rootPath.relativize(filePath).toString().replace(File.separatorChar, '/')
                        discovered += "$normalized/$rel"
                    }
                }
            }

            "jar" -> {
                val connection = directoryUrl.openConnection() as JarURLConnection
                val jarFile: JarFile = connection.jarFile
                val entryPrefix = ensureTrailingSlash(connection.entryName ?: normalized)
                val iterator = jarFile.entries()
                while (iterator.hasMoreElements()) {
                    val entry = iterator.nextElement()
                    if (entry.isDirectory) continue
                    val name = entry.name
                    if (name.startsWith(entryPrefix)) discovered += name
                }
            }
        }
    }

    private fun ensureTrailingSlash(path: String): String =
        if (path.endsWith('/')) path else "$path/"
}