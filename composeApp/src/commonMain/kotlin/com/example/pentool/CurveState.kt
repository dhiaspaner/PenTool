package com.example.pentool

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset



class CurveState {


    val penTool = PenTool()
    var mode by mutableStateOf(PenToolMode.Draw)

    var cursorPosition by mutableStateOf(Offset.Zero)

}
