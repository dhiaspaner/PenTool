package com.example.pentool

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.pentToolEditGesture(state: CurveState) : Modifier {
    val threshold = 50f
    state.run {
        return this@pentToolEditGesture.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { dragStartOffset ->

                    val selectedCurve = state.curveList.firstOrNull { curve ->
                        val p0Distance = (curve.p0 - dragStartOffset).getDistance()
                        val p1Distance = (curve.p1 - dragStartOffset).getDistance()
                        val p2Distance = (curve.p2!! - dragStartOffset).getDistance()
                        val p3Distance = (curve.p3!! - dragStartOffset).getDistance()


                        (p0Distance <= threshold || p1Distance <= threshold) ||
                                (p2Distance <= threshold || p3Distance <= threshold)
                    }


                    val selectedType = with(selectedCurve) {
                        if (this == null) return@with null
                        val p0Distance = EditPointType.P0 to (p0 - dragStartOffset).getDistanceSquared()

                        val p1Distance = EditPointType.P1 to  (p1 - dragStartOffset).getDistanceSquared()
                        val p2Distance = EditPointType.P2 to  (p2!! - dragStartOffset).getDistanceSquared()
                        val p3Distance = EditPointType.P3 to  (p3!! - dragStartOffset).getDistanceSquared()

                        listOf(
                            p0Distance,
                            p1Distance,
                            p2Distance,
                            p3Distance
                        ).minByOrNull {
                            it.second
                        }?.first

                    }

                    selectedType?.let { type ->
                        setEditPoint(type, selectedCurve!!)
                    }

                },
                onDrag = { _, dragAmount ->
                    translateEditCurve(dragAmount)
                },
                onDragEnd = {
                    clearEditCurve()
                },
                onDragCancel = {
                    clearEditCurve()
                }
            )
        }
    }
}