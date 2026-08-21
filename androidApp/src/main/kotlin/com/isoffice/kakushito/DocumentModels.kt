package com.isoffice.kakushito

import android.net.Uri

data class PdfPoint(
    val x: Float,
    val y: Float
)

data class Marker(
    val page: Int,
    val points: List<PdfPoint>,
    val color: Int = 0xFFFFE600.toInt(),
    val width: Float = 14f
)

data class RecentFile(
    val uri: Uri,
    val fileName: String
)
