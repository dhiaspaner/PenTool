package com.example.pentool

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sqrt

// Represents different types of curve segments in a pen tool
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

// Represents a control handle for bezier curves
data class ControlHandle(
    val position: Offset,
    val isVisible: Boolean = true
) {
    fun distanceTo(point: Offset): Float {
        val dx = position.x - point.x
        val dy = position.y - point.y
        return sqrt(dx * dx + dy * dy)
    }
}

// Represents an anchor point in the pen tool
data class AnchorPoint(
    val position: Offset,
    val inHandle: ControlHandle? = null,  // Handle coming into this point
    val outHandle: ControlHandle? = null, // Handle going out of this point
    val isSelected: Boolean = false
) {
    fun distanceTo(point: Offset): Float {
        val dx = position.x - point.x
        val dy = position.y - point.y
        return sqrt(dx * dx + dy * dy)
    }

    // Create symmetric handles
    fun withSymmetricHandles(handleOffset: Offset): AnchorPoint {
        return copy(
            inHandle = ControlHandle(position - handleOffset),
            outHandle = ControlHandle(position + handleOffset)
        )
    }

    // Create asymmetric handles
    fun withHandles(inOffset: Offset?, outOffset: Offset?): AnchorPoint {
        return copy(
            inHandle = inOffset?.let { ControlHandle(position + it) },
            outHandle = outOffset?.let { ControlHandle(position + it) }
        )
    }
}

// Main curve class for pen tool
data class Curve(
    private val segments: List<CurveSegment> = emptyList(),
    private val anchorPoints: List<AnchorPoint> = emptyList()
) {
    var isSelected: Boolean by mutableStateOf(false)
    var strokeWidth: Float by mutableStateOf(2f)
    var strokeColor: Color by mutableStateOf(Color.Blue)

    private val path = Path()

    fun DrawScope.drawCurve() {
        if (segments.isEmpty()) return

        path.apply {
            reset()
            segments.forEachIndexed { index, segment ->
                when (segment) {
                    is CurveSegment.Line -> {
                        if (index == 0) moveTo(segment.startPoint.x, segment.startPoint.y)
                        lineTo(segment.endPoint.x, segment.endPoint.y)
                    }
                    is CurveSegment.QuadraticBezier -> {
                        if (index == 0) moveTo(segment.startPoint.x, segment.startPoint.y)
                        quadraticTo(
                            segment.controlPoint.x, segment.controlPoint.y,
                            segment.endPoint.x, segment.endPoint.y
                        )
                    }
                    is CurveSegment.CubicBezier -> {
                        if (index == 0) moveTo(segment.startPoint.x, segment.startPoint.y)
                        cubicTo(
                            segment.controlPoint1.x, segment.controlPoint1.y,
                            segment.controlPoint2.x, segment.controlPoint2.y,
                            segment.endPoint.x, segment.endPoint.y
                        )
                    }
                }
            }

            drawPath(
                path = this,
                style = Stroke(width = strokeWidth),
                color = strokeColor
            )
        }
    }

    fun DrawScope.drawAnchorsAndHandles(showHandles: Boolean = true) {
        anchorPoints.forEach { anchor ->
            // Draw anchor point
            drawCircle(
                radius = if (anchor.isSelected) 6f else 4f,
                center = anchor.position,
                color = if (anchor.isSelected) Color.Red else Color.Magenta
            )

            if (showHandles) {
                // Draw incoming handle
                anchor.inHandle?.takeIf { it.isVisible }?.let { handle ->
                    drawLine(
                        color = Color.Gray,
                        strokeWidth = 1f,
                        start = anchor.position,
                        end = handle.position
                    )
                    drawCircle(
                        radius = 3f,
                        center = handle.position,
                        color = Color.Cyan
                    )
                }

                // Draw outgoing handle
                anchor.outHandle?.takeIf { it.isVisible }?.let { handle ->
                    drawLine(
                        color = Color.Gray,
                        strokeWidth = 1f,
                        start = anchor.position,
                        end = handle.position
                    )
                    drawCircle(
                        radius = 3f,
                        center = handle.position,
                        color = Color.Cyan
                    )
                }
            }
        }
    }

    // Hit testing
    fun hitTestAnchor(point: Offset, tolerance: Float = 10f): AnchorPoint? {
        return anchorPoints.find { it.distanceTo(point) <= tolerance }
    }

    fun hitTestHandle(point: Offset, tolerance: Float = 8f): Pair<AnchorPoint, ControlHandle>? {
        anchorPoints.forEach { anchor ->
            anchor.inHandle?.let { handle ->
                if (handle.distanceTo(point) <= tolerance) {
                    return anchor to handle
                }
            }
            anchor.outHandle?.let { handle ->
                if (handle.distanceTo(point) <= tolerance) {
                    return anchor to handle
                }
            }
        }
        return null
    }

    // Builder methods for pen tool operations
    fun addPoint(point: Offset): Curve {
        val newAnchor = AnchorPoint(point)
        val newSegment = if (anchorPoints.isNotEmpty()) {
            CurveSegment.Line(anchorPoints.last().position, point)
        } else null

        return copy(
            anchorPoints = anchorPoints + newAnchor,
            segments = if (newSegment != null) segments + newSegment else segments
        )
    }

    fun addBezierPoint(point: Offset, controlOffset: Offset): Curve {
        val newAnchor = AnchorPoint(point).withSymmetricHandles(controlOffset)

        val newSegment = if (anchorPoints.isNotEmpty()) {
            val lastAnchor = anchorPoints.last()
            when {
                lastAnchor.outHandle != null && newAnchor.inHandle != null -> {
                    CurveSegment.CubicBezier(
                        startPoint = lastAnchor.position,
                        controlPoint1 = lastAnchor.outHandle.position,
                        controlPoint2 = newAnchor.inHandle.position,
                        endPoint = point
                    )
                }
                lastAnchor.outHandle != null || newAnchor.inHandle != null -> {
                    val controlPoint = lastAnchor.outHandle?.position ?: newAnchor.inHandle!!.position
                    CurveSegment.QuadraticBezier(
                        startPoint = lastAnchor.position,
                        controlPoint = controlPoint,
                        endPoint = point
                    )
                }
                else -> CurveSegment.Line(lastAnchor.position, point)
            }
        } else null

        return copy(
            anchorPoints = anchorPoints + newAnchor,
            segments = if (newSegment != null) segments + newSegment else segments
        )
    }

    fun updateAnchor(index: Int, newAnchor: AnchorPoint): Curve {
        if (index !in anchorPoints.indices) return this

        val updatedAnchors = anchorPoints.toMutableList().apply {
            this[index] = newAnchor
        }

        // Rebuild segments that connect to this anchor
        val updatedSegments = segments.toMutableList()

        // Update segment ending at this anchor (if exists)
        if (index > 0) {
            val prevAnchor = updatedAnchors[index - 1]
            val segmentIndex = index - 1
            if (segmentIndex < updatedSegments.size) {
                updatedSegments[segmentIndex] = createSegmentBetween(prevAnchor, newAnchor)
            }
        }

        // Update segment starting from this anchor (if exists)
        if (index < updatedAnchors.size - 1) {
            val nextAnchor = updatedAnchors[index + 1]
            val segmentIndex = index
            if (segmentIndex < updatedSegments.size) {
                updatedSegments[segmentIndex] = createSegmentBetween(newAnchor, nextAnchor)
            }
        }

        return copy(
            anchorPoints = updatedAnchors,
            segments = updatedSegments
        )
    }

    private fun createSegmentBetween(start: AnchorPoint, end: AnchorPoint): CurveSegment {
        return when {
            start.outHandle != null && end.inHandle != null -> {
                CurveSegment.CubicBezier(
                    startPoint = start.position,
                    controlPoint1 = start.outHandle.position,
                    controlPoint2 = end.inHandle.position,
                    endPoint = end.position
                )
            }
            start.outHandle != null || end.inHandle != null -> {
                val controlPoint = start.outHandle?.position ?: end.inHandle!!.position
                CurveSegment.QuadraticBezier(
                    startPoint = start.position,
                    controlPoint = controlPoint,
                    endPoint = end.position
                )
            }
            else -> CurveSegment.Line(start.position, end.position)
        }
    }

    fun closePath(): Curve {
        if (anchorPoints.size < 2) return this

        val firstPoint = anchorPoints.first()
        val lastPoint = anchorPoints.last()

        if (firstPoint.position == lastPoint.position) return this

        val closingSegment = createSegmentBetween(lastPoint, firstPoint)
        return copy(segments = segments + closingSegment)
    }

    companion object {
        fun empty() = Curve()

        fun fromPoints(points: List<Offset>): Curve {
            if (points.isEmpty()) return empty()

            var curve = Curve().addPoint(points.first())
            points.drop(1).forEach { point ->
                curve = curve.addPoint(point)
            }
            return curve
        }
    }
}

// DSL for pen tool operations
class PenToolBuilder {
    private var curve = Curve.empty()

    fun startAt(point: Offset): PenToolBuilder {
        curve = curve.addPoint(point)
        return this
    }

    fun lineTo(point: Offset): PenToolBuilder {
        curve = curve.addPoint(point)
        return this
    }

    fun curveTo(point: Offset, controlOffset: Offset): PenToolBuilder {
        curve = curve.addBezierPoint(point, controlOffset)
        return this
    }

    fun close(): PenToolBuilder {
        curve = curve.closePath()
        return this
    }

    fun build(): Curve = curve
}

// DSL function
fun penPath(init: PenToolBuilder.() -> Unit): Curve {
    return PenToolBuilder().apply(init).build()
}

// Usage examples:
/*
// Simple path
val simplePath = penPath {
    startAt(Offset(0f, 0f))
    lineTo(Offset(100f, 0f))
    lineTo(Offset(100f, 100f))
    close()
}

// Bezier path
val bezierPath = penPath {
    startAt(Offset(0f, 100f))
    curveTo(Offset(100f, 0f), Offset(0f, -50f))
    curveTo(Offset(200f, 100f), Offset(0f, 50f))
}

// Interactive usage
val interactiveCurve = Curve.empty()
    .addPoint(clickPoint)
    .addBezierPoint(dragPoint, handleOffset)
*/