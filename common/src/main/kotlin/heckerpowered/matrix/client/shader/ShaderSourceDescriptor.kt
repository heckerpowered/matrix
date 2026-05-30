/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

/**
 * Describes a single shader stage source.
 *
 * @author heckerpowered
 */
data class ShaderSourceDescriptor(
    val stage: ShaderStage,
    /**
     * Shader source in UTF-8
     */
    val source: String,
)