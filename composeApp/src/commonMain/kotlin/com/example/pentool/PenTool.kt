package com.example.pentool

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.runtime.mutableStateListOf


class PenTool {
    private val _segments = mutableStateListOf<CurveSegment>()
    private val _anchorPoints = mutableStateListOf<AnchorPoint>()
    val anchorPoints: List<AnchorPoint> get()  = _anchorPoints

    var strokeWidth: Float by mutableStateOf(2f)
    var strokeColor: Color by mutableStateOf(Color.Blue)
    var isClosed: Boolean by mutableStateOf(false)

    private val path = Path()

    fun DrawScope.drawCurve() {
        if (_segments.isEmpty()) return

        path.apply {
            reset()
            _segments.forEachIndexed { index, segment ->
                if (index == 0) initialMove(segment)
                drawSegment(segment)
            }

            if (isClosed) close()

            drawPath(
                path = this,
                style = Stroke(width = strokeWidth),
                color = strokeColor
            )
        }
    }


    fun Path.initialMove(segment: CurveSegment) {
        when (segment) {
            is CurveSegment.Line -> {
                moveTo(segment.startPoint.position.x, segment.startPoint.position.y)
            }

            is CurveSegment.QuadraticBezier -> {
                moveTo(segment.startPoint.position.x, segment.startPoint.position.y)
            }

            is CurveSegment.CubicBezier -> {
                moveTo(segment.startPoint.position.x, segment.startPoint.position.y)
            }
        }
    }


    fun Path.drawSegment(segment: CurveSegment) {
        when (segment) {
            is CurveSegment.Line -> {
                lineTo(
                    x = segment.endPoint.position.x,
                    y = segment.endPoint.position.y
                )
            }

            is CurveSegment.QuadraticBezier -> {
                quadraticTo(
                    x1 = segment.controlPoint.position.x,
                    y1  = segment.controlPoint.position.y,
                    x2 = segment.endPoint.position.x,
                    y2 = segment.endPoint.position.y
                )
            }

            is CurveSegment.CubicBezier -> {
                cubicTo(
                    segment.controlPoint1.position.x, segment.controlPoint1.position.y,
                    segment.controlPoint2.position.x, segment.controlPoint2.position.y,
                    segment.endPoint.position.x, segment.endPoint.position.y
                )
            }
        }
    }

    fun DrawScope.drawAnchorsAndHandles(showHandles: Boolean = true) {
        _anchorPoints.forEach { anchor ->
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
                        color = if (handle.isSelected) Color.Blue else Color.Gray,
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
                        color = if (handle.isSelected) Color.Blue else Color.Gray,
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

    fun DrawScope.drawNextSegment(end: AnchorPoint) {
        val start = _anchorPoints.lastOrNull() ?: return
        val segment = createSegmentBetween(start, end)
        val path = Path()
        path.apply {
            initialMove(segment)
            drawSegment(segment)
            drawPath(
                path = this,
                style = Stroke(width = strokeWidth),
                color = strokeColor.copy(alpha = 0.6f),
            )
        }




    }

    // Hit testing
    fun hitTestAnchor(point: Offset, tolerance: Float = 10f): AnchorPoint? {
        return _anchorPoints.find { it.distanceTo(point) <= tolerance }
    }

    fun hitTestHandle(point: Offset, tolerance: Float = 8f): Pair<AnchorPoint, ControlHandle>? {
        _anchorPoints.forEach { anchor ->
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

    // Mutable operations for pen tool
    fun addAnchor(point: Offset): AnchorPoint {
        val newAnchor = AnchorPoint(point)
        _anchorPoints.add(newAnchor)

        // Create segment connecting to previous point
        if (_anchorPoints.size > 1) {
            val prevAnchor = _anchorPoints[_anchorPoints.size - 2]
            val segment = createSegmentBetween(prevAnchor, newAnchor)
            _segments.add(segment)
        }

        return newAnchor
    }

    fun addBezierPoint(point: Offset, controlOffset: Offset): AnchorPoint {
        val newAnchor = AnchorPoint(point)
        newAnchor.addSymmetricHandles(controlOffset)
        _anchorPoints.add(newAnchor)

        // Create segment connecting to previous point
        if (_anchorPoints.size > 1) {
            val prevAnchor = _anchorPoints[_anchorPoints.size - 2]
            val segment = createSegmentBetween(prevAnchor, newAnchor)
            _segments.add(segment)
        }

        return newAnchor
    }

    fun removePoint(anchor: AnchorPoint) {
        val index = _anchorPoints.indexOf(anchor)
        if (index == -1) return

        _anchorPoints.removeAt(index)

        // Remove and rebuild affected segments
        rebuildSegments()
    }

    fun insertPoint(point: Offset, afterIndex: Int): AnchorPoint? {
        if (afterIndex !in 0 until _anchorPoints.size) return null

        val newAnchor = AnchorPoint(point)
        _anchorPoints.add(afterIndex + 1, newAnchor)

        rebuildSegments()
        return newAnchor
    }

    fun closePath() {
        if (_anchorPoints.size < 3 || isClosed) return

        isClosed = true
        val firstPoint = _anchorPoints.first()
        val lastPoint = _anchorPoints.last()

        val closingSegment = createSegmentBetween(lastPoint, firstPoint)
        _segments.add(closingSegment)
    }

    fun openPath() {
        if (!isClosed) return

        isClosed = false
        // Remove the closing segment (last one)
        if (_segments.isNotEmpty()) {
            _segments.removeAt(_segments.size - 1)
        }
    }

    private fun createSegmentBetween(start: AnchorPoint, end: AnchorPoint): CurveSegment {
        return when {
            start.outHandle != null && end.inHandle != null -> {
                CurveSegment.CubicBezier(
                    startPoint = start,
                    controlPoint1 = start.outHandle!!,
                    controlPoint2 = end.inHandle!!,
                    endPoint = end
                )
            }

            start.outHandle != null -> {
                CurveSegment.QuadraticBezier(
                    startPoint = start,
                    controlPoint = start.outHandle!!,
                    endPoint = end
                )
            }

            end.inHandle != null -> {
                CurveSegment.QuadraticBezier(
                    startPoint = start,
                    controlPoint = end.inHandle!!,
                    endPoint = end
                )
            }

            else -> CurveSegment.Line(start, end)
        }
    }

    fun rebuildSegments() {
        _segments.clear()

        for (i in 0 until _anchorPoints.size - 1) {
            val segment = createSegmentBetween(_anchorPoints[i], _anchorPoints[i + 1])
            _segments.add(segment)
        }

        // Add closing segment if closed
        if (isClosed && _anchorPoints.size >= 3) {
            val closingSegment = createSegmentBetween(_anchorPoints.last(), _anchorPoints.first())
            _segments.add(closingSegment)
        }
    }

    // Selection methods
    fun selectAll() {
        _anchorPoints.forEach { it.isSelected = true }
    }

    fun deselectAll() {
        _anchorPoints.forEach { it.isSelected = false }
    }

    fun selectAnchor(offset: Offset,) {
        val threshold = 10f
        var isInHandleSelected = false
        var isOutHandleSelected = false
        var isAnchorSelected = false
        val selectedAnchor = _anchorPoints.firstOrNull { anchor ->
            val anchorDistance = (anchor.position - offset).getDistance()
            val inHandleDistance = anchor.inHandle?.let { inHandle  ->
                (inHandle.position - offset).getDistance()
            } ?: Float.MAX_VALUE
            val outHandleDistance = anchor.outHandle?.let { outHandle  ->
                (outHandle.position - offset).getDistance()
            } ?: Float.MAX_VALUE

            isInHandleSelected = inHandleDistance <= threshold
            isOutHandleSelected = outHandleDistance <= threshold
            isAnchorSelected = anchorDistance <= threshold

            (isAnchorSelected || isOutHandleSelected || isInHandleSelected)
        }

        _anchorPoints.forEach {
            it.inHandle?.isSelected = false
            it.outHandle?.isSelected = false
            it.isSelected = false

        }
        selectedAnchor?.apply {
            isSelected = isAnchorSelected
            inHandle?.isSelected = isInHandleSelected
            outHandle?.isSelected = isOutHandleSelected
        }
    }

    fun clear() {
        _segments.clear()
        _anchorPoints.clear()
        isClosed = false
    }

}