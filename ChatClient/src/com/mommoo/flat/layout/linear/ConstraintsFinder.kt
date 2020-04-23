package com.mommoo.flat.layout.linear

import com.mommoo.flat.layout.linear.constraints.LinearConstraints
import java.awt.Component
import java.util.HashMap

class ConstraintsFinder {
    private val FINDER: MutableMap<Component, LinearConstraints> = HashMap()
    fun put(component: Component, constraints: LinearConstraints) {
        FINDER[component] = constraints
    }

    fun remove(component: Component) {
        FINDER.remove(component)
    }

    fun find(component: Component): LinearConstraints {
        return FINDER.getOrDefault(component, LinearConstraints())
    }

    val weightSum: Int
        get() = FINDER.keys
                .stream()
                .mapToInt { component: Component -> FINDER[component]!!.weight }
                .sum()
}