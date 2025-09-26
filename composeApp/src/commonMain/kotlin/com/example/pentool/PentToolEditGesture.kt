package com.example.pentool

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.pentToolEditGesture(state: CurveState): Modifier {
    val threshold = 50f
    state.run {
        return this@pentToolEditGesture.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { dragStartOffset ->
                    penTool.selectAnchor(dragStartOffset)
                },
                onDrag = { _, dragAmount ->
                    val selectedAnchor = penTool.anchorPoints.firstOrNull {
                        it.isSelected || it.inHandle?.isSelected == true || it.outHandle?.isSelected == true
                    }

                    selectedAnchor?.apply {
                        if (isSelected) {
                            moveTo(newPosition = selectedAnchor.position + dragAmount)
                        }
                        inHandle?.let { inHandle ->
                            if (inHandle.isSelected) {
                                inHandle.moveTo(inHandle.position + dragAmount)
                            }
                        }
                        outHandle?.let { outHandle ->
                            if (outHandle.isSelected) {
                                outHandle.moveTo(outHandle.position + dragAmount)
                            }
                        }
                    }

                    penTool.rebuildSegments()


                },
                onDragEnd = {
                },
                onDragCancel = {
                }
            )
        }
    }

}