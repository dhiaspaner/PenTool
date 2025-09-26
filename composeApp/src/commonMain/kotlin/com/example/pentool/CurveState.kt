package com.example.pentool

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset


enum class EditPointType {
    P0,
    P1,
    P2,
    P3;

    fun isControlPoint() = this == P1 || this == P2

}

class CurveState {


    var incrementUpdate by mutableIntStateOf(0)
    var currentCurve by mutableStateOf<Curve?>(null)

    var mode by mutableStateOf(PenToolMode.Draw)

    val curveList = mutableStateListOf<Curve>()

    var offset1 by mutableStateOf(Offset.Unspecified) // first point
    var offset2 by mutableStateOf(Offset.Unspecified) // second point

    var cursorPosition by mutableStateOf(Offset.Unspecified) // follows mouse/touch


    var editCurve: Curve? = null
        private set
    var selectedPointType by mutableStateOf(EditPointType.P0)
        private set

    fun setEditPoint(type: EditPointType, curve: Curve) {
        editCurve = curve
        selectedPointType = type
        incrementUpdate ++
    }

    fun clearEditCurve() {
        editCurve = null
    }

    fun translateEditCurve(change: Offset) {
        println("edit curve ${editCurve}")
        when (selectedPointType) {
            EditPointType.P0 -> editCurve?.p0 += change
            EditPointType.P1 ->  editCurve?.p1 += change
            EditPointType.P2 -> editCurve?.p2 =  editCurve?.p2?.plus(change)
            EditPointType.P3 ->  editCurve?.p3 = editCurve?.p3?.plus(change)
        }

        incrementUpdate++

    }

}
