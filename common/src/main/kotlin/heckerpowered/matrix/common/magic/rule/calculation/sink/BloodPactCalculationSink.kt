/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.rule.calculation.sink

import heckerpowered.matrix.common.effect.BloodPactEffect

class BloodPactCalculationSink : CalculationSink {
    var exchangeRate: Double = BloodPactEffect.DEFAULT_EXCHANGE_RATE
}