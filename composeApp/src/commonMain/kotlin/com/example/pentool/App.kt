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

        val curveState = remember { CurveState() }

        Box {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .penToolGesture(curveState)
            ) {

                curveState.run {
                    penTool.run {
                        drawCurve()
                        drawAnchorsAndHandles(showHandles = true)
                        drawNextSegment(AnchorPoint(cursorPosition))
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


