package com.example.sliderbarsample

import kotlin.math.abs
import kotlin.math.roundToInt

class SliderBarNestedValue(private val ranges: IntArray) {

    fun get(min: Float, max: Float) = Pair(findNestedValue(min), findNestedValue(max))

    private fun findNestedValue(value: Float) =
        ranges.minBy { abs(value - it) } ?: value.roundToInt()
}
