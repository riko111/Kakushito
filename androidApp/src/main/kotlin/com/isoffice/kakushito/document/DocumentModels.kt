package com.isoffice.kakushito.document

import android.net.Uri

data class PdfPoint(
    val x: Float,
    val y: Float
)

data class RecentFile(
    val uri: Uri,
    val fileName: String
)
