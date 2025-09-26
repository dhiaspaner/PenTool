package com.example.pentool

import androidx.compose.ui.geometry.Offset
import kotlin.math.sqrt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

sealed class CurveSegment {
    abstract var startPoint: AnchorPoint
    abstract var endPoint: AnchorPoint

    class Line(
        override var startPoint: AnchorPoint,
        override var endPoint: AnchorPoint
    ) : CurveSegment()

    class QuadraticBezier(
        override var startPoint: AnchorPoint,
        var controlPoint: ControlHandle,
        override var endPoint: AnchorPoint
    ) : CurveSegment()

    class CubicBezier(
        override var startPoint: AnchorPoint,
        var controlPoint1: ControlHandle,
        var controlPoint2: ControlHandle,
        override var endPoint: AnchorPoint
    ) : CurveSegment()
}


class ControlHandle(
    position: Offset,
    isVisible: Boolean = true,
    isSelected: Boolean = false,
) {


    var position: Offset by mutableStateOf(position)
    var isVisible: Boolean by mutableStateOf(isVisible)
    var isSelected: Boolean by mutableStateOf(isSelected)

    fun distanceTo(point: Offset): Float {
        val dx = position.x - point.x
        val dy = position.y - point.y
        return sqrt(dx * dx + dy * dy)
    }

    fun moveTo(newPosition: Offset) {
        position = newPosition
    }
}

class AnchorPoint(
    position: Offset,
    inHandle: ControlHandle? = null,
    outHandle: ControlHandle? = null,
    isSelected: Boolean = false
) {
    var position: Offset by mutableStateOf(position)
    var inHandle: ControlHandle? by mutableStateOf(inHandle)
    var outHandle: ControlHandle? by mutableStateOf(outHandle)
    var isSelected: Boolean by mutableStateOf(isSelected)

    fun distanceTo(point: Offset): Float {
        val dx = position.x - point.x
        val dy = position.y - point.y
        return sqrt(dx * dx + dy * dy)
    }

    // Move the anchor point and optionally move handles with it
    fun moveTo(newPosition: Offset, moveHandles: Boolean = true) {
        if (moveHandles) {
            val delta = newPosition - position
            inHandle?.let { it.position += delta }
            outHandle?.let { it.position += delta }
        }
        position = newPosition
    }

    // Create symmetric handles
    fun addSymmetricHandles(handleOffset: Offset) {
        inHandle = ControlHandle( handleOffset)

        val midian = position
        val symmetry = midian * 2f - handleOffset
        outHandle = ControlHandle(  handleOffset)
        inHandle = ControlHandle( symmetry)


    }

    // Create asymmetric handles
    fun addHandles(inOffset: Offset?, outOffset: Offset?) {
        inHandle = inOffset?.let { ControlHandle(  it) }
        outHandle = outOffset?.let { ControlHandle(  it) }
    }


    // Remove handles (convert to corner point)
    fun removeHandles() {
        inHandle = null
        outHandle = null
    }

    // Make handles symmetric based on the active handle
    fun makeHandlesSymmetric(activeHandle: ControlHandle) {
        val isInHandle = activeHandle == inHandle
        val isOutHandle = activeHandle == outHandle

        when {
            isInHandle && outHandle != null -> {
                val offset = position - activeHandle.position
                outHandle!!.position = position + offset
            }
            isOutHandle && inHandle != null -> {
                val offset = position - activeHandle.position
                inHandle!!.position = position + offset
            }
        }
    }
}
