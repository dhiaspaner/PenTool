package com.example.pentool

import androidx.compose.ui.geometry.Offset

sealed class CurveSegment {
    abstract val startPoint: Offset
    abstract val endPoint: Offset
    
    data class Line(
        override val startPoint: Offset,
        override val endPoint: Offset
    ) : CurveSegment()
    
    data class QuadraticBezier(
        override val startPoint: Offset,
        val controlPoint: Offset,
        override val endPoint: Offset
    ) : CurveSegment()
    
    data class CubicBezier(
        override val startPoint: Offset,
        val controlPoint1: Offset,
        val controlPoint2: Offset,
        override val endPoint: Offset
    ) : CurveSegment()
}