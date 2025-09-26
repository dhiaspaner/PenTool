package com.example.pentool

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class PenToolMode {
    Draw,
    Edit;

    operator fun not(): PenToolMode = when (this) {
        Draw -> Edit
        Edit -> Draw
    }
}
@Composable
@Preview
fun App() {
    MaterialTheme {

//        var offset1 by remember { mutableStateOf(Offset.Unspecified) }
//        var offset2 by remember { mutableStateOf(Offset.Unspecified) }


//        val curveList = remember { mutableStateListOf<Curve>() }

//        var currentCurve by remember { mutableStateOf<Curve?>(null) }


        val curveState = remember { CurveState() }


        Box {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .penToolGesture(curveState)
            ) {

                curveState.run {

                    incrementUpdate

                    if (curveState.offset1.isSpecified)
                        drawCircle(
                            color = Color.Black,
                            radius = 10f,
                            center = curveState.offset1
                        )


                    if (curveState.offset2.isSpecified)
                        drawCircle(
                            color = Color.Black,
                            radius = 10f,
                            center = offset2
                        )

                    if (offset1.isSpecified && offset2.isUnspecified && cursorPosition.isSpecified) {
                        drawLine(
                            color = Color.Black,
                            strokeWidth = 2f,
                            start = offset1,
                            end = cursorPosition
                        )
                    }


                    currentCurve?.run {
                        drawCurve()
                        drawSelectors()
                    }

                    curveList.forEach {
                        it.run {
                            drawCurve()
                            drawSelectors()
                        }
                    }
                }
            }


            Switch(
                checked = curveState.mode == PenToolMode.Draw,
                onCheckedChange = { curveState.mode = !curveState.mode },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20 .dp)
            )

        }

    }
}


