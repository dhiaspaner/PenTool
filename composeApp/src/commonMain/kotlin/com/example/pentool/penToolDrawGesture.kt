package com.example.pentool

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput


fun Modifier.penToolDrawGesture(state: CurveState): Modifier {
    state.run {
        return this@penToolDrawGesture
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        penTool.addAnchor(it)
                    },
                    onDrag = { _, _ ->
                        penTool.anchorPoints.lastOrNull()?.addSymmetricHandles(handleOffset = cursorPosition)
                        penTool.rebuildSegments()
                    },
                    onDragEnd = {
                        penTool.anchorPoints.lastOrNull()?.addSymmetricHandles(handleOffset = cursorPosition)
                    }
                )
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val position = event.changes.first().position
                        cursorPosition = position

                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures {
                    if (penTool.anchorPoints.size == 1 && penTool.anchorPoints.lastOrNull()?.inHandle != null) {
                    penTool.addBezierPoint(it, it)
                    } else {
                        penTool.addAnchor(it)
                    }
//                    currentCurve?.p3 = it
//                    currentCurve?.let { curve ->
//                        curveList.add(curve)
//                        currentCurve = null
//
//                        offset1 = Offset.Unspecified
//                        offset2 = Offset.Unspecified
//                    }
                }
            }
    }
}