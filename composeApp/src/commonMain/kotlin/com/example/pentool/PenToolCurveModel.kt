package com.example.pentool

data class PenToolCurveModel(
    val segment: List<CurveSegment>,
    val anchorPointList: List<AnchorPoint>,
    val isClosed: Boolean,
)
