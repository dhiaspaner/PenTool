package com.example.pentool

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

data class Curve(var p0: Offset, var p1: Offset) {

    var p2: Offset? by mutableStateOf(null)
    var p3: Offset? by mutableStateOf(null)





    val path = Path()

    fun DrawScope.drawCurve() {
        path.apply {
            reset()
            moveTo(p0.x, p0.y)
            if (p3 != null && p2 != null)
                cubicTo(
                    p1.x, p1.y,  // control point
                    p2!!.x, p2!!.y,   // end point
                    p3!!.x, p3!!.y   // end point
                )
            else
                p2?.let { p2 ->
                    quadraticTo(
                        p1.x, p1.y,  // control point
                        p2.x, p2.y   // end point
                    )
                }

            drawPath(
                path = path,
                style = Stroke(width = 2f),
                color = Color.Blue
            )
        }
    }

    fun DrawScope.drawSelectors() {

        drawCircle(
            radius = 5f,
            center = p0,
            color = Color.Magenta
        )

        drawCircle(
            radius = 5f,
            center = p1,
            color = Color.Magenta
        )

        drawLine(
            color = Color.Gray,
            strokeWidth = 2f,
            start = p0,
            end = p1
        )



        p2?.let { p2 ->
            drawCircle(
                radius = 5f,
                center = p2,
                color = Color.Magenta
            )

            val mediator = p3 ?: return@let

            val opposite =  Offset(
                2 * mediator.x - p2.x,
                2 * mediator.y - p2.y
            )

            drawCircle(
                radius = 5f,
                center = opposite,
                color = Color.Magenta
            )

        }

        p3?.let { p3 ->
            drawCircle(
                radius = 5f,
                center = p3,
                color = Color.Magenta
            )



            val mediator = p3

            val opposite =  Offset(
                2 * mediator.x - p2!!.x,
                2 * mediator.y - p2!!.y
            )

            p2?.let { p2 ->
                drawLine(
                    color = Color.Gray,
                    strokeWidth = 2f,
                    start = p2,
                    end = p3
                )
            }
        }

    }
}
