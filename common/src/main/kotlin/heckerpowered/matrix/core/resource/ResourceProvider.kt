/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core.resource

import java.io.InputStream

interface ResourceProvider {
    fun open(resourcePath: String): InputStream?
    fun exists(resourcePath: String): Boolean = open(resourcePath) != null
}