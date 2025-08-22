/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.core

object ClientOptions {
    /**
     * Whether aim assist is enabled.
     *
     * If true, the system will automatically target the closest entity within a certain range
     * and angle to the player's crosshair, without requiring perfect aim.
     */
    var aimAssistEnabled: Boolean = true

    /**
     * The maximum distance (in blocks) at which aim assist can detect entities.
     *
     * Entities beyond this distance will not be considered for aim assist targeting.
     */
    var aimAssistMaxDistance: Double = 1024.0

    /**
     * The field of view angle (in degrees) within which aim assist can detect entities.
     *
     * This defines how far off-center an entity can be while still being considered
     * for aim assist.
     */
    var aimAssistFov: Double = 20.0
}