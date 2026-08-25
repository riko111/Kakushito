package com.isoffice.kakushito.pdf

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
import com.isoffice.kakushito.document.PdfPoint
import com.isoffice.kakushito.marker.Marker
import kotlin.math.abs
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
    onMarkerDrawn: (Marker) -> Unit,
    onMarkerErased: (Marker) -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    modifier: Modifier = Modifier,
    markerColor: Int = 0xFFFFE600.toInt(),
    markerWidth: Float = 14f,
    hideColors: Set<Int> = emptySet()
) {
    var canvasSize by remember {
        mutableStateOf(IntSize.Zero)
    }

    var activeStroke by remember {
        mutableStateOf<List<Offset>>(emptyList())
    }

    // --------------------------------
    // ユーザーによるズーム
    // --------------------------------
    var userScale by remember {
        mutableFloatStateOf(1f)
    }

    // --------------------------------
    // ユーザーによるパン
    // --------------------------------
    var userOffset by remember {
        mutableStateOf(Offset.Zero)
    }

    val baseScale =
        if (
            canvasSize.width == 0 ||
            canvasSize.height == 0
        ) {
            1f
        } else {
            min(
                canvasSize.width.toFloat() / page.width,
                canvasSize.height.toFloat() / page.height
            )
        }

    val displayWidth =
        page.width * baseScale

    val displayHeight =
        page.height * baseScale

    val baseOffset = Offset(
        (canvasSize.width - displayWidth) / 2f,
        (canvasSize.height - displayHeight) / 2f
    )

    // --------------------------------
    // 実際にPDFを表示する倍率
    // --------------------------------
    val displayScale =
        baseScale * userScale

    // --------------------------------
    // 実際のPDF左上位置
    // --------------------------------
    val displayOffset =
        baseOffset + userOffset

    var lastTapTime by remember {
        mutableLongStateOf(0L)
    }

    var lastTapPosition by remember {
        mutableStateOf(Offset.Zero)
    }

    // --------------------------------
    // ページ変更時はズームをリセット
    // --------------------------------
    LaunchedEffect(pageIndex) {
        userScale = 1f
        userOffset = Offset.Zero
    }

    Canvas(
        modifier
            .fillMaxSize()
            .background(Color(0xFF555555))
            .onSizeChanged {
                canvasSize = it
            }
            .pointerInput(
                drawMode,
                pageIndex,
                markerColor,
                markerWidth
            ) {

                awaitPointerEventScope {

                    while (true) {

                        // --------------------------------
                        // ジェスチャー開始
                        // --------------------------------
                        val downEvent =
                            awaitPointerEvent()

                        val pressedChanges =
                            downEvent.changes
                                .filter { it.pressed }

                        if (pressedChanges.isEmpty()) {
                            continue
                        }

                        // --------------------------------
                        // 1本指操作として開始
                        // --------------------------------
                        val firstPointer =
                            pressedChanges.first()

                        val start =
                            firstPointer.position

                        var endPosition =
                            start

                        var moved =
                            false

                        activeStroke =
                            emptyList()

                        // --------------------------------
                        // 1本指操作を監視
                        // 途中で2本目が来たら
                        // 2本指操作へ切り替える
                        // --------------------------------
                        while (true) {

                            val event =
                                awaitPointerEvent()

                            val changes =
                                event.changes
                                    .filter { it.pressed }

                            // --------------------------------
                            // 2本指になった
                            // --------------------------------
                            if (changes.size >= 2) {

                                // 1本指で描いていた内容はキャンセル
                                activeStroke =
                                    emptyList()

                                var previousDistance =
                                    (
                                            changes[0].position -
                                                    changes[1].position
                                            ).getDistance()

                                var previousCentroid =
                                    (
                                            changes[0].position +
                                                    changes[1].position
                                            ) / 2f

                                // --------------------------------
                                // 2本指操作
                                // --------------------------------
                                while (true) {

                                    val transformEvent =
                                        awaitPointerEvent()

                                    val transformChanges =
                                        transformEvent.changes
                                            .filter { it.pressed }

                                    // 2本指でなくなったら終了
                                    if (
                                        transformChanges.size < 2
                                    ) {
                                        break
                                    }

                                    val first =
                                        transformChanges[0]

                                    val second =
                                        transformChanges[1]

                                    // --------------------------------
                                    // 現在の2本指の距離
                                    // --------------------------------
                                    val currentDistance =
                                        (
                                                first.position -
                                                        second.position
                                                ).getDistance()

                                    // --------------------------------
                                    // 現在の2本指の中心
                                    // --------------------------------
                                    val currentCentroid =
                                        (
                                                first.position +
                                                        second.position
                                                ) / 2f

                                    // --------------------------------
                                    // 現在のズーム倍率
                                    // --------------------------------
                                    val oldScale =
                                        userScale

                                    // --------------------------------
                                    // ピンチによる新しい倍率
                                    // --------------------------------
                                    val newScale =
                                        if (
                                            previousDistance > 0f
                                        ) {
                                            (
                                                    oldScale *
                                                            (
                                                                    currentDistance /
                                                                            previousDistance
                                                                    )
                                                    ).coerceIn(
                                                    1f,
                                                    5f
                                                )
                                        } else {
                                            oldScale
                                        }

                                    // --------------------------------
                                    // ピンチ中心を維持するための
                                    // Offset計算
                                    // --------------------------------
                                    val scaleRatio =
                                        if (
                                            oldScale > 0f
                                        ) {
                                            newScale /
                                                    oldScale
                                        } else {
                                            1f
                                        }

                                    var newOffset =
                                        currentCentroid -
                                                baseOffset -
                                                (
                                                        currentCentroid -
                                                                baseOffset -
                                                                userOffset
                                                        ) *
                                                scaleRatio

                                    // --------------------------------
                                    // 2本指を動かした分だけパン
                                    // --------------------------------
                                    val pan =
                                        currentCentroid -
                                                previousCentroid

                                    newOffset += pan

                                    // --------------------------------
                                    // パン可能範囲を計算
                                    // --------------------------------
                                    val scaledWidth =
                                        page.width *
                                                baseScale *
                                                newScale

                                    val scaledHeight =
                                        page.height *
                                                baseScale *
                                                newScale

                                    val maxX =
                                        maxOf(
                                            0f,
                                            (
                                                    scaledWidth -
                                                            canvasSize.width
                                                    ) / 2f
                                        )

                                    val maxY =
                                        maxOf(
                                            0f,
                                            (
                                                    scaledHeight -
                                                            canvasSize.height
                                                    ) / 2f
                                        )

                                    // --------------------------------
                                    // 画面外に出すぎないよう制限
                                    // --------------------------------
                                    newOffset =
                                        Offset(
                                            newOffset.x.coerceIn(
                                                -maxX,
                                                maxX
                                            ),
                                            newOffset.y.coerceIn(
                                                -maxY,
                                                maxY
                                            )
                                        )

                                    // --------------------------------
                                    // State更新
                                    // --------------------------------
                                    userScale =
                                        newScale

                                    userOffset =
                                        newOffset

                                    // 次回計算用
                                    previousDistance =
                                        currentDistance

                                    previousCentroid =
                                        currentCentroid

                                    first.consume()
                                    second.consume()
                                }

                                // 2本指操作終了
                                break
                            }

                            // --------------------------------
                            // 指が全部離れた
                            // --------------------------------
                            if (changes.isEmpty()) {
                                break
                            }

                            // --------------------------------
                            // 1本指操作継続
                            // --------------------------------
                            val change =
                                changes.first()

                            val position =
                                change.position

                            if (
                                (position - start)
                                    .getDistance() > 10f
                            ) {
                                moved = true
                            }

                            if (moved) {

                                // ==================================================
                                // 拡大中でもマーカー・消しゴムを使用可能
                                // ==================================================
                                if (
                                    userScale > 1f &&
                                    drawMode == DrawMode.None
                                ) {

                                    // --------------------------------
                                    // ズーム中 + 通常モード
                                    // 1本指ドラッグ → パン
                                    // --------------------------------
                                    val pan =
                                        position -
                                                endPosition

                                    var newOffset =
                                        userOffset + pan

                                    val scaledWidth =
                                        page.width *
                                                baseScale *
                                                userScale

                                    val scaledHeight =
                                        page.height *
                                                baseScale *
                                                userScale

                                    val maxX =
                                        maxOf(
                                            0f,
                                            (
                                                    scaledWidth -
                                                            canvasSize.width
                                                    ) / 2f
                                        )

                                    val maxY =
                                        maxOf(
                                            0f,
                                            (
                                                    scaledHeight -
                                                            canvasSize.height
                                                    ) / 2f
                                        )

                                    newOffset =
                                        Offset(
                                            newOffset.x.coerceIn(
                                                -maxX,
                                                maxX
                                            ),
                                            newOffset.y.coerceIn(
                                                -maxY,
                                                maxY
                                            )
                                        )

                                    userOffset =
                                        newOffset

                                } else {

                                    // --------------------------------
                                    // 以下の場合は描画・消去
                                    //
                                    // ・通常倍率
                                    // ・ズーム中 + Marker
                                    // ・ズーム中 + Eraser
                                    // --------------------------------
                                    if (
                                        activeStroke.isEmpty()
                                    ) {
                                        activeStroke =
                                            listOf(start)
                                    }

                                    activeStroke =
                                        activeStroke + position
                                }

                                change.consume()
                            }

                            endPosition =
                                position

                            // --------------------------------
                            // 指を離した
                            // --------------------------------
                            if (!change.pressed) {
                                break
                            }
                        }

                        // --------------------------------
                        // タップ判定
                        // --------------------------------
                        if (!moved) {

                            val now =
                                System.currentTimeMillis()

                            val timeSinceLastTap =
                                now - lastTapTime

                            val distanceFromLastTap =
                                (
                                        start -
                                                lastTapPosition
                                        ).getDistance()

                            if (
                                timeSinceLastTap < 300L &&
                                distanceFromLastTap < 50f
                            ) {

                                // --------------------------------
                                // ダブルタップ
                                // --------------------------------
                                userScale = 1f
                                userOffset = Offset.Zero

                                lastTapTime = 0L

                            } else {

                                // 1回目のタップ
                                lastTapTime = now
                                lastTapPosition = start
                            }
                        }

                        // --------------------------------
                        // 1本指操作の確定
                        // --------------------------------
                        if (moved) {

                            // --------------------------------
                            // 画面座標 → PDF座標
                            // --------------------------------
                            val pdfPoints =
                                activeStroke.map { screen ->

                                    PdfPoint(

                                        (
                                                (
                                                        screen.x -
                                                                displayOffset.x
                                                        ) /
                                                        displayScale
                                                ).coerceIn(
                                                0f,
                                                page.width.toFloat()
                                            ),

                                        (
                                                page.height -
                                                        (
                                                                (
                                                                        screen.y -
                                                                                displayOffset.y
                                                                        ) /
                                                                        displayScale
                                                                )
                                                ).coerceIn(
                                                0f,
                                                page.height.toFloat()
                                            )
                                    )
                                }

                            when (drawMode) {

                                // --------------------------------
                                // マーカーモード
                                // --------------------------------
                                DrawMode.Marker -> {

                                    if (
                                        pdfPoints.size > 1
                                    ) {

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

                                // --------------------------------
                                // 消しゴムモード
                                // --------------------------------
                                DrawMode.Eraser -> {

                                    val eraserRadius =
                                        30f

                                    val pageMarkers =
                                        markers.filter {
                                            it.page ==
                                                    pageIndex
                                        }

                                    pageMarkers.forEach { marker ->
                                        println(
                                            "DISPLAY MARKER: width=${marker.width}, " +
                                                    "markerWidth=$markerWidth, " +
                                                    "displayScale=$displayScale"
                                        )

                                        val hit =
                                            marker.points.any {
                                                    point ->

                                                val screenPoint =
                                                    Offset(

                                                        displayOffset.x +
                                                                point.x *
                                                                displayScale,

                                                        displayOffset.y +
                                                                (
                                                                        page.height -
                                                                                point.y
                                                                        ) *
                                                                displayScale
                                                    )

                                                activeStroke.any {
                                                        eraserPoint ->

                                                    (
                                                            screenPoint -
                                                                    eraserPoint
                                                            ).getDistance() <=
                                                            eraserRadius
                                                }
                                            }

                                        if (hit) {
                                            onMarkerErased(marker)
                                        }
                                    }
                                }

                                // --------------------------------
                                // 隠すモード
                                // --------------------------------
                                DrawMode.Hide -> {
                                    // 何もしない
                                }

                                // --------------------------------
                                // 通常モード
                                // --------------------------------
                                DrawMode.None -> {

                                    // 1倍のときだけ
                                    // 横スワイプでページ送り
                                    if (
                                        userScale <= 1f
                                    ) {

                                        val deltaX =
                                            endPosition.x -
                                                    start.x

                                        val deltaY =
                                            endPosition.y -
                                                    start.y

                                        if (
                                            abs(
                                                deltaX
                                            ) > 50f &&
                                            abs(
                                                deltaX
                                            ) >
                                            abs(
                                                deltaY
                                            )
                                        ) {

                                            if (
                                                deltaX > 0
                                            ) {
                                                onPreviousPage()
                                            } else {
                                                onNextPage()
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        activeStroke =
                            emptyList()
                    }
                }
            }
    ) {

        // --------------------------------
        // PDF画像
        // --------------------------------
        drawImage(
            image =
                page.bitmap.asImageBitmap(),

            srcSize =
                IntSize(
                    page.bitmap.width,
                    page.bitmap.height
                ),

            dstOffset =
                IntOffset(
                    displayOffset.x.toInt(),
                    displayOffset.y.toInt()
                ),

            dstSize =
                IntSize(
                    (
                            page.width *
                                    displayScale
                            ).toInt(),

                    (
                            page.height *
                                    displayScale
                            ).toInt()
                )
        )

        // --------------------------------
        // PDF座標 → 画面座標
        // --------------------------------
        fun pdfToScreen(
            point: PdfPoint
        ): Offset =
            Offset(

                displayOffset.x +
                        point.x *
                        displayScale,

                displayOffset.y +
                        (
                                page.height -
                                        point.y
                                ) *
                        displayScale
            )

        // --------------------------------
        // 保存済みマーカー
        // --------------------------------
        markers.forEach { marker ->

            marker.points
                .zipWithNext()
                .forEach { (from, to) ->

                    // --------------------------------
                    // このマーカーを隠すか
                    // --------------------------------
                    val isHidden =
                        drawMode == DrawMode.Hide &&
                                marker.color in hideColors

                    drawLine(

                        color =
                            if (isHidden) {
                                Color.White
                            } else {
                                Color(marker.color)
                                    .copy(alpha = 0.53f)
                            },

                        start =
                            pdfToScreen(from),

                        end =
                            pdfToScreen(to),

                        // --------------------------------
                        // 隠すときは元の太さ + 2f
                        // --------------------------------
                        strokeWidth =
                            if (isHidden) {
                                (
                                        marker.width +
                                                2f
                                        ) * displayScale
                            } else {
                                marker.width *
                                        displayScale
                            }
                    )
                }
        }

        // --------------------------------
        // 描画中のマーカー
        // --------------------------------
        if (
            drawMode ==
            DrawMode.Marker
        ) {

            activeStroke
                .zipWithNext()
                .forEach { (from, to) ->

                    drawLine(

                        color =
                            Color(markerColor)
                                .copy(alpha = 0.67f),

                        start = from,
                        end = to,

                        strokeWidth =
                            markerWidth *
                                    displayScale
                    )
                }
        }
    }
}