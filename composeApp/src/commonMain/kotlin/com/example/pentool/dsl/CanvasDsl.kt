package com.example.pentool.dsl

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.Path
import com.example.pentool.models.AnchorPoint
import com.example.pentool.models.ControlHandle
import com.example.pentool.models.CurveSegment
import com.example.pentool.models.PenToolCurveModel

context(drawScope: DrawScope)
fun ControlHandle.draw(anchor: Offset) {
    if (!isVisible) return

    // Draw line connecting to anchor
    with(drawScope) {
        drawLine(
            color = if (isSelected) Color.Blue else Color.Gray,
            strokeWidth = 1f,
            start = anchor,
            end = position
        )

        // Draw handle circle
        drawCircle(
            radius = 4f,
            center = position,
            color = Color.Cyan
        )
    }
}

context(drawSCope: DrawScope)
fun AnchorPoint.draw(showHandles: Boolean = true) {
    // Draw anchor circle
    drawSCope.drawCircle(
        radius = if (isSelected) 6f else 4f,
        center = position,
        color = if (isSelected) Color.Red else Color.Magenta
    )

    if (showHandles) {
        inHandle?.draw(position)
        outHandle?.draw(position)
    }
}

context(drawScope: DrawScope)
fun CurveSegment.draw(showAnchors: Boolean = true) {
    when (this) {
        is CurveSegment.Line -> {
            drawScope.drawLine(
                color = Color.Black,
                strokeWidth = 2f,
                start = startPoint.position,
                end = endPoint.position
            )
        }

        is CurveSegment.QuadraticBezier -> {
            val path = Path().apply {
                moveTo(x = startPoint.position.x, y = startPoint.position.y)
                quadraticTo(
                    x1 = controlPoint.position.x,
                    y1 = controlPoint.position.y,
                    x2 = endPoint.position.x,
                    y2 = endPoint.position.y
                )
            }
            drawScope.drawPath(path, Color.Black)

            if (showAnchors) {
                startPoint.draw()
                endPoint.draw()
                controlPoint.draw(startPoint.position)
            }
        }

        is CurveSegment.CubicBezier -> {
            val path = Path().apply {
                moveTo(startPoint.position.x, startPoint.position.y)
                cubicTo(
                    x1 = controlPoint1.position.x, y1 = controlPoint1.position.y,
                    x2 = controlPoint2.position.x, y2 = controlPoint2.position.y,
                    x3 = endPoint.position.x, y3 = endPoint.position.y
                )
            }
            drawScope.drawPath(path, Color.Black)

            if (showAnchors) {
                startPoint.draw()
                endPoint.draw()
                controlPoint1.draw(startPoint.position)
                controlPoint2.draw(endPoint.position)
            }
        }
    }
}

context(drawScope: DrawScope)
fun PenToolCurveModel.drawAnchorsAndHandles(showHandles: Boolean = true) {
    anchorPointList.forEach { anchor ->
        with(drawScope) {
            anchor.draw(showHandles)
        }
    }
}
