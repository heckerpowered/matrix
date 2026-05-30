/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.rule

object RuleRegistry {
    private val rules = mutableMapOf<Class<*>, MutableList<Any>>()

    fun <T : Any> register(type: Class<T>, rule: T) {
        val rules = rules.getOrPut(type) { mutableListOf() }
        rules += rule
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> all(type: Class<T>): List<T> {
        return rules[type] as? List<T> ?: emptyList()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> forEach(type: Class<T>, action: (T) -> Unit) {
        val rules = rules[type] ?: return
        for (rule in rules) {
            action(rule as T)
        }
    }
}

inline fun <reified T : Any> RuleRegistry.register(rule: T) {
    register(T::class.java, rule)
}

inline fun <reified T : Any> RuleRegistry.all(): List<T> {
    return all(T::class.java)
}

inline fun <reified T : Any> RuleRegistry.registerAll(vararg rules: T) {
    for (rule in rules) {
        register(T::class.java, rule)
    }
}

inline fun <reified T : Any> RuleRegistry.forEach(noinline action: (T) -> Unit) {
    forEach(T::class.java, action)
}