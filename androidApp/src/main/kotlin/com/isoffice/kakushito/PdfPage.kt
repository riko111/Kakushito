package com.isoffice.kakushito

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.min

@Composable
fun PdfPage(
    page: RenderedPage,
    pageIndex: Int,
    markers: List<Marker>,
    drawingEnabled: Boolean,
    onMarkerDrawn: (Marker) -> Unit,
    modifier: Modifier = Modifier
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var activeStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }
    val scale = if (canvasSize.width == 0 || canvasSize.height == 0) 1f
    else min(canvasSize.width.toFloat() / page.width, canvasSize.height.toFloat() / page.height)
    val displayWidth = page.width * scale
    val displayHeight = page.height * scale
    val offset = Offset((canvasSize.width - displayWidth) / 2f, (canvasSize.height - displayHeight) / 2f)

    Canvas(
        modifier
            .background(Color(0xFF555555))
            .onSizeChanged { canvasSize = it }
            .pointerInput(drawingEnabled, scale, offset, pageIndex) {
                if (drawingEnabled) detectDragGestures(
                    onDragStart = { start -> activeStroke = listOf(start) },
                    onDrag = { change, dragAmount ->
                        activeStroke = activeStroke + (activeStroke.last() + dragAmount)
                        change.consume()
                    },
                    onDragEnd = {
                        val pdfPoints = activeStroke.map { screen ->
                            PdfPoint(
                                ((screen.x - offset.x) / scale).coerceIn(0f, page.width.toFloat()),
                                (page.height - (screen.y - offset.y) / scale).coerceIn(0f, page.height.toFloat())
                            )
                        }
                        if (pdfPoints.size > 1) onMarkerDrawn(Marker(pageIndex, pdfPoints))
                        activeStroke = emptyList()
                    },
                    onDragCancel = { activeStroke = emptyList() },
                )
            }
    ) {
        drawImage(
            image = page.bitmap.asImageBitmap(),
            srcSize = IntSize(page.bitmap.width, page.bitmap.height),
            dstOffset = IntOffset(offset.x.toInt(), offset.y.toInt()),
            dstSize = IntSize(displayWidth.toInt(), displayHeight.toInt()),
        )
        fun pdfToScreen(point: PdfPoint) = Offset(offset.x + point.x * scale, offset.y + (page.height - point.y) * scale)
        markers.forEach { marker ->
            marker.points.zipWithNext().forEach { (from, to) ->
                drawLine(Color(0x88FFE600), pdfToScreen(from), pdfToScreen(to), 18f * scale)
            }
        }
        activeStroke.zipWithNext().forEach { (from, to) ->
            drawLine(Color(0xAAFFE600), from, to, 18f * scale)
        }
    }
}
