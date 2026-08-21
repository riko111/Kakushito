package com.isoffice.kakushito

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
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

enum class DrawMode {
    None,
    Marker,
    Eraser,
    Hide
}

@Composable
fun PdfPage(
    page: RenderedPage,
    pageIndex: Int,
    markers: List<Marker>,
    drawMode: DrawMode,
    markerColor: Int = 0xFFFFE600.toInt(),
    markerWidth: Float = 14f,
    onMarkerDrawn: (Marker) -> Unit,
    onMarkerErased: (Marker) -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var activeStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }

    var userScale by remember { mutableFloatStateOf(1f) }
    var userOffset by remember { mutableStateOf(Offset.Zero) }

    val baseScale =
        if (canvasSize.width == 0 || canvasSize.height == 0) {
            1f
        } else {
            min(
                canvasSize.width.toFloat() / page.width,
                canvasSize.height.toFloat() / page.height
            )
        }

    val displayWidth = page.width * baseScale
    val displayHeight = page.height * baseScale

    val baseOffset = Offset(
        (canvasSize.width - displayWidth) / 2f,
        (canvasSize.height - displayHeight) / 2f
    )

    val displayScale = baseScale * userScale
    val displayOffset = baseOffset + userOffset

    var lastTapTime by remember { mutableLongStateOf(0L) }
    var lastTapPosition by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(pageIndex) {
        userScale = 1f
        userOffset = Offset.Zero
    }

    Canvas(
        modifier
            .fillMaxSize()
            .background(Color(0xFF555555))
            .onSizeChanged { canvasSize = it }
            .pointerInput(drawMode, pageIndex) {
                awaitPointerEventScope {
                    while (true) {
                        val downEvent = awaitPointerEvent()
                        val pressedChanges = downEvent.changes.filter { it.pressed }

                        if (pressedChanges.isEmpty()) continue

                        val firstPointer = pressedChanges.first()
                        val start = firstPointer.position
                        var endPosition = start
                        var moved = false

                        activeStroke = emptyList()

                        while (true) {
                            val event = awaitPointerEvent()
                            val changes = event.changes.filter { it.pressed }

                            if (changes.size >= 2) {
                                activeStroke = emptyList()

                                var previousDistance =
                                    (changes[0].position - changes[1].position).getDistance()
                                var previousCentroid =
                                    (changes[0].position + changes[1].position) / 2f

                                while (true) {
                                    val transformEvent = awaitPointerEvent()
                                    val transformChanges = transformEvent.changes.filter { it.pressed }
                                    if (transformChanges.size < 2) break

                                    val first = transformChanges[0]
                                    val second = transformChanges[1]
                                    val currentDistance =
                                        (first.position - second.position).getDistance()
                                    val currentCentroid =
                                        (first.position + second.position) / 2f

                                    val oldScale = userScale
                                    val newScale =
                                        if (previousDistance > 0f) {
                                            (oldScale * (currentDistance / previousDistance))
                                                .coerceIn(1f, 5f)
                                        } else {
                                            oldScale
                                        }

                                    val scaleRatio =
                                        if (oldScale > 0f) newScale / oldScale else 1f

                                    var newOffset =
                                        currentCentroid - baseOffset -
                                                (currentCentroid - baseOffset - userOffset) * scaleRatio

                                    val pan = currentCentroid - previousCentroid
                                    newOffset += pan

                                    val scaledWidth = page.width * baseScale * newScale
                                    val scaledHeight = page.height * baseScale * newScale

                                    val maxX = maxOf(0f, (scaledWidth - canvasSize.width) / 2f)
                                    val maxY = maxOf(0f, (scaledHeight - canvasSize.height) / 2f)

                                    newOffset = Offset(
                                        newOffset.x.coerceIn(-maxX, maxX),
                                        newOffset.y.coerceIn(-maxY, maxY)
                                    )

                                    userScale = newScale
                                    userOffset = newOffset
                                    previousDistance = currentDistance
                                    previousCentroid = currentCentroid

                                    first.consume()
                                    second.consume()
                                }
                                break
                            }

                            if (changes.isEmpty()) break

                            val change = changes.first()
                            val position = change.position

                            if ((position - start).getDistance() > 10f) {
                                moved = true
                            }

                            if (moved) {
                                if (userScale > 1f && drawMode == DrawMode.None) {
                                    val pan = position - endPosition
                                    var newOffset = userOffset + pan

                                    val scaledWidth = page.width * baseScale * userScale
                                    val scaledHeight = page.height * baseScale * userScale
                                    val maxX = maxOf(0f, (scaledWidth - canvasSize.width) / 2f)
                                    val maxY = maxOf(0f, (scaledHeight - canvasSize.height) / 2f)

                                    newOffset = Offset(
                                        newOffset.x.coerceIn(-maxX, maxX),
                                        newOffset.y.coerceIn(-maxY, maxY)
                                    )
                                    userOffset = newOffset
                                } else {
                                    if (activeStroke.isEmpty()) {
                                        activeStroke = listOf(start)
                                    }
                                    activeStroke = activeStroke + position
                                }
                                change.consume()
                            }

                            endPosition = position
                            if (!change.pressed) break
                        }

                        if (!moved) {
                            val now = System.currentTimeMillis()
                            val timeSinceLastTap = now - lastTapTime
                            val distanceFromLastTap = (start - lastTapPosition).getDistance()

                            if (timeSinceLastTap < 300L && distanceFromLastTap < 50f) {
                                userScale = 1f
                                userOffset = Offset.Zero
                                lastTapTime = 0L
                            } else {
                                lastTapTime = now
                                lastTapPosition = start
                            }
                        }

                        if (moved) {
                            val pdfPoints = activeStroke.map { screen ->
                                PdfPoint(
                                    ((screen.x - displayOffset.x) / displayScale)
                                        .coerceIn(0f, page.width.toFloat()),
                                    (page.height - ((screen.y - displayOffset.y) / displayScale))
                                        .coerceIn(0f, page.height.toFloat())
                                )
                            }

                            when (drawMode) {
                                DrawMode.Marker -> {
                                    if (pdfPoints.size > 1) {
                                        onMarkerDrawn(
                                            Marker(
                                                page = pageIndex,
                                                points = pdfPoints,
                                                color = markerColor,
                                                width = markerWidth
                                            )
                                        )
                                    }
                                }

                                DrawMode.Eraser -> {
                                    val eraserRadius = 30f
                                    val pageMarkers = markers.filter { it.page == pageIndex }

                                    pageMarkers.forEach { marker ->
                                        val hit = marker.points.any { point ->
                                            val screenPoint = Offset(
                                                displayOffset.x + point.x * displayScale,
                                                displayOffset.y + (page.height - point.y) * displayScale
                                            )

                                            activeStroke.any { eraserPoint ->
                                                (screenPoint - eraserPoint).getDistance() <= eraserRadius
                                            }
                                        }

                                        if (hit) onMarkerErased(marker)
                                    }
                                }

                                DrawMode.Hide -> Unit

                                DrawMode.None -> {
                                    if (userScale <= 1f) {
                                        val deltaX = endPosition.x - start.x
                                        val deltaY = endPosition.y - start.y

                                        if (
                                            kotlin.math.abs(deltaX) > 50f &&
                                            kotlin.math.abs(deltaX) > kotlin.math.abs(deltaY)
                                        ) {
                                            if (deltaX > 0) onPreviousPage() else onNextPage()
                                        }
                                    }
                                }
                            }
                        }

                        activeStroke = emptyList()
                    }
                }
            }
    ) {
        drawImage(
            image = page.bitmap.asImageBitmap(),
            srcSize = IntSize(page.bitmap.width, page.bitmap.height),
            dstOffset = IntOffset(displayOffset.x.toInt(), displayOffset.y.toInt()),
            dstSize = IntSize(
                (page.width * displayScale).toInt(),
                (page.height * displayScale).toInt()
            )
        )

        fun pdfToScreen(point: PdfPoint): Offset = Offset(
            displayOffset.x + point.x * displayScale,
            displayOffset.y + (page.height - point.y) * displayScale
        )

        markers.forEach { marker ->
            marker.points.zipWithNext().forEach { (from, to) ->
                drawLine(
                    color = if (drawMode == DrawMode.Hide) {
                        Color.White
                    } else {
                        Color(marker.color).copy(alpha = 0.53f)
                    },
                    start = pdfToScreen(from),
                    end = pdfToScreen(to),
                    strokeWidth = if (drawMode == DrawMode.Hide) {
                        22f * displayScale
                    } else {
                        marker.width * displayScale
                    }
                )
            }
        }

        if (drawMode == DrawMode.Marker) {
            activeStroke.zipWithNext().forEach { (from, to) ->
                drawLine(
                    color = Color(markerColor).copy(alpha = 0.67f),
                    start = from,
                    end = to,
                    strokeWidth = markerWidth * displayScale
                )
            }
        }
    }
}
