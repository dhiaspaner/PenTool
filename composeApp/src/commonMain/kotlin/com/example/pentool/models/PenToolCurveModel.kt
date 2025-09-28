package com.example.pentool.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

data class PenToolCurveModel(
    val segmentList: List<CurveSegment>,
    val anchorPointList: List<AnchorPoint>,
    val isClosed: Boolean,
) {


    val path = Path()
    val strokeWidth = 2f
    val strokeColor = Color.Blue


    fun DrawScope.drawCurve() {
        if (segmentList.isEmpty()) return

        path.apply {
            reset()
            segmentList.forEachIndexed { index, segment ->
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
                    x1 = segment.controlPoint1.position.x, segment.controlPoint1.position.y,
                    x2 = segment.controlPoint2.position.x, segment.controlPoint2.position.y,
                    x3 = segment.endPoint.position.x, segment.endPoint.position.y
                )
            }
        }
    }


}