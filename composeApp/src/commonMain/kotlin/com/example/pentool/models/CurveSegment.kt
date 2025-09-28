package com.example.pentool.models

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

sealed class CurveSegment {
    abstract var startPoint: AnchorPoint
    abstract var endPoint: AnchorPoint

    data class Line(
        override var startPoint: AnchorPoint,
        override var endPoint: AnchorPoint
    ) : CurveSegment()

    data class QuadraticBezier(
        override var startPoint: AnchorPoint,
        var controlPoint: ControlHandle,
        override var endPoint: AnchorPoint
    ) : CurveSegment()

    data class CubicBezier(
        override var startPoint: AnchorPoint,
        var controlPoint1: ControlHandle,
        var controlPoint2: ControlHandle,
        override var endPoint: AnchorPoint
    ) : CurveSegment()
}


data class ControlHandle(
    val position: Offset,
    val isVisible: Boolean = true,
    val isSelected: Boolean = false,
) {


//    var position: Offset by mutableStateOf(position)
//    var isVisible: Boolean by mutableStateOf(isVisible)
//    var isSelected: Boolean by mutableStateOf(isSelected)
//
//    fun distanceTo(point: Offset): Float {
//        val dx = position.x - point.x
//        val dy = position.y - point.y
//        return sqrt(dx * dx + dy * dy)
//    }
//
//    fun moveTo(newPosition: Offset) {
//        position = newPosition
//    }
}

data class AnchorPoint(
    val position: Offset,
    val inHandle: ControlHandle? = null,
    val outHandle: ControlHandle? = null,
    val isSelected: Boolean = false
)


