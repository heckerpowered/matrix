/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.core

import java.io.InputStream

fun resource(path: String): InputStream {
    class Empty
    return Empty::class.java.getResourceAsStream(path) ?: throw IllegalArgumentException("Resource $path not found")
}

/**
 * Converts resource to string
 *
 * @param path The *absolute* resource path
 * @throws IllegalArgumentException If the path is invalid
 */
fun resourceToString(path: String) = resource(path).use { it.reader().readText() }