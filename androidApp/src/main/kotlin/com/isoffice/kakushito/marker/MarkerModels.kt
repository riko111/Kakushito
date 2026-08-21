package com.isoffice.kakushito.marker

import com.isoffice.kakushito.document.PdfPoint

data class Marker(
    val page: Int,
    val points: List<PdfPoint>,
    val color: Int = 0xFFFFE600.toInt(),
    val width: Float = 14f
)