/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.rule.calculation.sink

import heckerpowered.matrix.common.effect.BloodPactEffect

class BloodPactCalculationSink : CalculationSink {
    var conversionRatio: Double = BloodPactEffect.DEFAULT_BLOOD_PACT_CONVERT_RATIO
}