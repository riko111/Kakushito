package com.isoffice.kakushito

data class PdfPoint(val x: Float, val y: Float)
data class Marker(val page: Int, val points: List<PdfPoint>)
