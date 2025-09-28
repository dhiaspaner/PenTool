package com.example.pentool

import androidx.compose.ui.geometry.Offset
import kotlin.math.sqrt


fun Offset.distanceTo(other: Offset): Float {
    val dx = x - other.x
    val dy = y - other.y
    return sqrt(dx * dx + dy * dy)
}