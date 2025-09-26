package com.example.pentool

import androidx.compose.ui.Modifier

fun Modifier. penToolGesture(state: CurveState) : Modifier {
    return when(state.mode) {
        PenToolMode.Draw -> penToolDrawGesture(state)
        PenToolMode.Edit -> pentToolEditGesture(state)
    }
}