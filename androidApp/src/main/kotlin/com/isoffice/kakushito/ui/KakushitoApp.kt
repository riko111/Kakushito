package com.isoffice.kakushito.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.isoffice.kakushito.R
import com.isoffice.kakushito.document.DocumentStore
import com.isoffice.kakushito.pdf.DrawMode
import com.isoffice.kakushito.pdf.PdfPage
import com.isoffice.kakushito.pdf.RenderedPage
import com.isoffice.kakushito.pdf.getLastPage
import com.isoffice.kakushito.pdf.getPageCount
import com.isoffice.kakushito.pdf.renderPage
import com.isoffice.kakushito.pdf.saveLastPage
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


// ==================================================
// マーカー設定
// ==================================================

val MarkerColors = listOf(
    0xFF29B6F6.toInt(), // 水色
    0xFFFFE600.toInt(), // 黄色
    0xFF66BB6A.toInt()  // 緑
)

val MarkerWidths = listOf(
    8f,
    11f,
    14f,
    18f,
    22f
)


// ==================================================
// ファイル名取得
// ==================================================

private fun getFileName(
    context: Context,
    uri: Uri
): String {

    val cursor = context.contentResolver.query(
        uri,
        arrayOf(
            OpenableColumns.DISPLAY_NAME
        ),
        null,
        null,
        null
    )

    cursor?.use {

        if (it.moveToFirst()) {

            val index =
                it.getColumnIndex(
                    OpenableColumns.DISPLAY_NAME
                )

            if (index >= 0) {
                return it.getString(index)
            }
        }
    }

    return context.getString(R.string.pdf_default_file_name)
}




// ==================================================
// メイン
// ==================================================

@Composable
fun KakushitoApp(context: Context) {

    val store =
        remember {
            DocumentStore(context)
        }


    // ==================================================
    // PDF
    // ==================================================

    var uri by remember {
        mutableStateOf(
            store.documentUri()
        )
    }

    var pageIndex by remember(uri) {

        mutableIntStateOf(
            uri?.let {
                getLastPage(
                    context,
                    it.toString()
                )
            } ?: 0
        )
    }

    var renderedPageIndex by remember {
        mutableIntStateOf(-1)
    }

    var rendered by remember {
        mutableStateOf<RenderedPage?>(null)
    }


    // ==================================================
    // マーカー
    // ==================================================

    var markers by remember(uri) {

        mutableStateOf(
            uri?.let(
                store::loadMarkers
            ).orEmpty()
        )
    }

    var drawMode by remember {

        mutableStateOf(
            DrawMode.None
        )
    }

    var markerColor by remember {

        mutableIntStateOf(
            0xFFFFE600.toInt()
        )
    }

    var markerWidth by remember {

        mutableFloatStateOf(
            14f
        )
    }


    // ==================================================
    // 隠す色
    //
    // デフォルトは黄色だけ
    // ==================================================

    var hideColors by remember {

        mutableStateOf(
            setOf(
                0xFFFFE600.toInt()
            )
        )
    }


    // ==================================================
    // ダイアログ
    // ==================================================

    var showMarkerSettings by remember {
        mutableStateOf(false)
    }

    var showHideColorDialog by remember {
        mutableStateOf(false)
    }

    var showMenu by remember {
        mutableStateOf(false)
    }

    var showRecentFiles by remember {
        mutableStateOf(false)
    }

    var showHowToUse by remember {
        mutableStateOf(false)
    }

    var showAbout by remember {
        mutableStateOf(false)
    }


    // ==================================================
    // その他
    // ==================================================

    var message by remember {
        mutableStateOf<String?>(null)
    }

    var isRendering by remember {
        mutableStateOf(false)
    }

    var pageCount by remember {
        mutableIntStateOf(0)
    }

    var pageSliderValue by remember {
        mutableFloatStateOf(0f)
    }

    var fileName by remember {
        mutableStateOf("")
    }


    // ==================================================
    // ページ移動
    // ==================================================

    fun goToPage(page: Int) {

        if (pageCount <= 0) {
            return
        }

        val newPage =
            page.coerceIn(
                0,
                pageCount - 1
            )

        // ページ切替中に旧ページへ入力されないよう、
        // 新ページの描画が完了するまで現在の表示を破棄する。
        rendered = null
        renderedPageIndex = -1

        pageIndex =
            newPage

        uri?.let {

            saveLastPage(
                context,
                it.toString(),
                newPage
            )
        }
    }


    // ==================================================
    // PDF選択
    // ==================================================

    val picker =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { selected ->

            if (selected != null) {

                context.contentResolver
                    .takePersistableUriPermission(
                        selected,
                        Intent
                            .FLAG_GRANT_READ_URI_PERMISSION
                    )

                val selectedFileName =
                    getFileName(
                        context,
                        selected
                    )

                store.setDocumentUri(
                    selected
                )

                store.addRecentFile(
                    selected,
                    selectedFileName
                )

                uri =
                    selected

                // ページ数は LaunchedEffect(uri) 側で
                // IOディスパッチャを使って取得する。
                pageCount = 0

                markers =
                    emptyList()

                rendered =
                    null

                renderedPageIndex =
                    -1

                message =
                    null
            }
        }


    // ==================================================
    // PDF情報
    // ==================================================

    LaunchedEffect(uri) {

        val document =
            uri ?: return@LaunchedEffect

        fileName =
            getFileName(
                context,
                document
            )

        pageCount =
            runCatching {

                withContext(Dispatchers.IO) {
                    getPageCount(
                        context,
                        document
                    )
                }

            }.getOrElse {

                message =
                    context.getString(R.string.pdf_open_failed, it.message)

                0
            }
    }


    // ==================================================
    // PDF描画
    // ==================================================

    LaunchedEffect(
        uri,
        pageIndex
    ) {

        val document =
            uri ?: return@LaunchedEffect

        isRendering =
            true

        val result =
            runCatching {

                renderPage(
                    context,
                    document,
                    pageIndex
                )
            }

        result
            .onSuccess {

                rendered =
                    it

                renderedPageIndex =
                    pageIndex

                pageCount =
                    it.pageCount

                message =
                    null
            }
            .onFailure {

                message =
                    context.getString(R.string.pdf_open_failed, it.message)
            }

        isRendering =
            false
    }


    LaunchedEffect(pageIndex) {

        pageSliderValue =
            pageIndex.toFloat()
    }


    // ==================================================
    // UI
    // ==================================================

    MaterialTheme {

        Column(

            Modifier
                .fillMaxSize()
                .safeContentPadding()
                .padding(12.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {


            // ==================================================
            // 上部ツールバー
            // ==================================================

            Row(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .zIndex(10f),

                horizontalArrangement =
                    Arrangement.spacedBy(4.dp),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {


                // ------------------------------------------
                // PDFを開く
                // ------------------------------------------

                IconButton(

                    onClick = {

                        picker.launch(
                            arrayOf(
                                "application/pdf"
                            )
                        )
                    }
                ) {

                    Icon(

                        painter =
                            painterResource(
                                R.drawable.folder_open
                            ),

                        contentDescription =
                            stringResource(R.string.open_pdf)
                    )
                }


                // ------------------------------------------
                // ファイル名
                // ------------------------------------------

                Text(

                    text =
                        fileName,

                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(
                                horizontal = 4.dp
                            ),

                    maxLines = 1,

                    overflow =
                        TextOverflow.Ellipsis
                )


                // ==================================================
                // マーカー
                //
                // タップ    → ON/OFF
                // 長押し    → 太さ＋色
                // ==================================================

                Box(

                    modifier =
                        Modifier
                            .size(48.dp)
                            .pointerInput(
                                rendered,
                                drawMode
                            ) {

                                detectTapGestures(

                                    onTap = {

                                        if (
                                            rendered != null
                                        ) {

                                            drawMode =

                                                if (
                                                    drawMode ==
                                                    DrawMode.Marker
                                                ) {

                                                    DrawMode.None

                                                } else {

                                                    DrawMode.Marker
                                                }
                                        }
                                    },

                                    onLongPress = {

                                        if (
                                            rendered != null
                                        ) {
                                            drawMode =
                                                DrawMode.Marker
                                            showMarkerSettings =
                                                true
                                        }
                                    }
                                )
                            },

                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(

                        painter =
                            painterResource(
                                R.drawable.ink_marker
                            ),

                        contentDescription =
                            stringResource(R.string.marker),

                        tint =
                            Color(markerColor),

                        modifier =
                            Modifier.background(

                                if (
                                    drawMode ==
                                    DrawMode.Marker
                                ) {

                                    Color(
                                        0x22FFC107
                                    )

                                } else {

                                    Color.Transparent
                                }
                            )
                    )
                }


                // ==================================================
                // 消しゴム
                // ==================================================

                IconButton(

                    onClick = {

                        drawMode =

                            if (
                                drawMode ==
                                DrawMode.Eraser
                            ) {

                                DrawMode.None

                            } else {

                                DrawMode.Eraser
                            }
                    },

                    enabled =
                        rendered != null,

                    modifier =
                        Modifier.background(

                            if (
                                drawMode ==
                                DrawMode.Eraser
                            ) {

                                Color(
                                    0x229E9E9E
                                )

                            } else {

                                Color.Transparent
                            }
                        )
                ) {

                    Icon(

                        painter =
                            painterResource(
                                R.drawable.ink_eraser
                            ),

                        contentDescription =
                            stringResource(R.string.eraser),

                        tint =
                            Color.Gray
                    )
                }


                // ==================================================
                // 隠す
                //
                // タップ    → ON/OFF
                // 長押し    → 隠す色
                // ==================================================

                Box(

                    modifier =
                        Modifier
                            .size(48.dp)
                            .pointerInput(
                                rendered,
                                drawMode
                            ) {

                                detectTapGestures(

                                    onTap = {

                                        if (
                                            rendered != null
                                        ) {

                                            drawMode =

                                                if (
                                                    drawMode ==
                                                    DrawMode.Hide
                                                ) {

                                                    DrawMode.None

                                                } else {

                                                    DrawMode.Hide
                                                }
                                        }
                                    },

                                    onLongPress = {

                                        if (
                                            rendered != null
                                        ) {

                                            showHideColorDialog =
                                                true
                                        }
                                    }
                                )
                            },

                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(

                        painter =
                            painterResource(
                                R.drawable.visibility_off
                            ),

                        contentDescription =
                            stringResource(R.string.hide_mode),

                        tint =
                            Color.Gray,

                        modifier =
                            Modifier.background(

                                if (
                                    drawMode ==
                                    DrawMode.Hide
                                ) {

                                    Color(
                                        0x229E9E9E
                                    )

                                } else {

                                    Color.Transparent
                                }
                            )
                    )
                }


                // ==================================================
                // メニュー
                // ==================================================

                IconButton(

                    onClick = {
                        showMenu = true
                    }
                ) {

                    Icon(

                        painter =
                            painterResource(
                                R.drawable.menu
                            ),

                        contentDescription =
                            stringResource(R.string.menu)
                    )
                }
            }


            Spacer(
                Modifier.height(8.dp)
            )


            // ==================================================
            // PDF表示
            // ==================================================

            if (rendered == null) {

                Box(

                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clipToBounds()
                        .zIndex(0f),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(
                        message
                            ?: stringResource(R.string.select_pdf)
                    )
                }

            } else {

                Box(

                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),

                    contentAlignment =
                        Alignment.Center
                ) {

                    PdfPage(

                        page =
                            rendered!!,

                        pageIndex =
                            pageIndex,

                        markers =
                            if (
                                renderedPageIndex ==
                                pageIndex
                            ) {

                                markers.filter {
                                    it.page ==
                                            pageIndex
                                }

                            } else {

                                emptyList()
                            },

                        drawMode =
                            drawMode,

                        markerColor =
                            markerColor,

                        markerWidth =
                            markerWidth,

                        // ★ 隠す機能を維持
                        hideColors =
                            hideColors,

                        onMarkerDrawn = { marker ->

                            markers =
                                markers + marker

                            uri?.let {

                                store.saveMarkers(
                                    it,
                                    markers
                                )
                            }
                        },

                        onMarkerErased = { marker ->

                            markers =
                                markers - marker

                            uri?.let {

                                store.saveMarkers(
                                    it,
                                    markers
                                )
                            }
                        },

                        onPreviousPage = {

                            goToPage(
                                pageIndex - 1
                            )
                        },

                        onNextPage = {

                            goToPage(
                                pageIndex + 1
                            )
                        }
                    )
                }


                // ==================================================
                // ページスライダー
                // ==================================================

                Row(

                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        "${pageIndex + 1}"
                    )

                    Slider(

                        value =
                            pageSliderValue,

                        onValueChange = {
                                value ->

                            pageSliderValue =
                                value
                        },

                        onValueChangeFinished = {

                            goToPage(
                                pageSliderValue
                                    .roundToInt()
                            )
                        },

                        valueRange =
                            0f..(
                                    pageCount - 1
                                    )
                                .coerceAtLeast(0)
                                .toFloat(),

                        steps =
                            (
                                    pageCount - 2
                                    )
                                .coerceAtLeast(0),

                        enabled =
                            pageCount > 1,

                        modifier =
                            Modifier.weight(1f)
                    )

                    Text(
                        "$pageCount"
                    )
                }
            }
        }


        // ==================================================
        // マーカー設定
        // ==================================================

        if (showMarkerSettings) {

            MarkerSettingsDialog(

                selectedWidth =
                    markerWidth,

                selectedColor =
                    markerColor,

                onWidthSelected = {
                    markerWidth = it
                },

                onColorSelected = {
                    markerColor = it
                },

                onDismiss = {
                    showMarkerSettings = false
                }
            )
        }


        // ==================================================
        // 隠す色
        // ==================================================

        if (showHideColorDialog) {

            HideColorDialog(

                selectedColors =
                    hideColors,

                onToggle = { color ->

                    val newColors =
                        hideColors.toMutableSet()

                    if (
                        color in newColors
                    ) {

                        newColors.remove(
                            color
                        )

                    } else {

                        newColors.add(
                            color
                        )
                    }

                    hideColors =
                        newColors
                },

                onDismiss = {

                    showHideColorDialog =
                        false
                }
            )
        }


        // ==================================================
        // メニュー
        // ==================================================

        if (showMenu) {

            AlertDialog(

                onDismissRequest = {
                    showMenu = false
                },

                title = {
                    Text(stringResource(R.string.menu))
                },

                text = {

                    Column(
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        TextButton(

                            onClick = {

                                showMenu =
                                    false

                                showRecentFiles =
                                    true
                            },

                            modifier =
                                Modifier.fillMaxWidth()
                        ) {

                            Text(
                                stringResource(R.string.recent_files)
                            )
                        }


                        TextButton(

                            onClick = {

                                showMenu =
                                    false

                                showHowToUse =
                                    true
                            },

                            modifier =
                                Modifier.fillMaxWidth()
                        ) {

                            Text(stringResource(R.string.how_to_use))
                        }


                        TextButton(

                            onClick = {

                                showMenu =
                                    false

                                showAbout =
                                    true
                            },

                            modifier =
                                Modifier.fillMaxWidth()
                        ) {

                            Text(
                                stringResource(R.string.about_app)
                            )
                        }
                    }
                },

                confirmButton = {

                    TextButton(

                        onClick = {
                            showMenu = false
                        }
                    ) {

                        Text(stringResource(R.string.close))
                    }
                }
            )
        }


        // ==================================================
        // 最近使ったファイル
        // ==================================================

        if (showRecentFiles) {

            RecentFilesDialog(

                store =
                    store,

                onFileSelected = { selectedUri ->

                    showRecentFiles =
                        false

                    // ------------------------------------------
                    // URIの読み取り権限を維持
                    // ------------------------------------------

                    runCatching {

                        context.contentResolver
                            .takePersistableUriPermission(
                                selectedUri,
                                Intent
                                    .FLAG_GRANT_READ_URI_PERMISSION
                            )
                    }


                    // ------------------------------------------
                    // ファイル名
                    // ------------------------------------------

                    val selectedFileName =
                        getFileName(
                            context,
                            selectedUri
                        )


                    // ------------------------------------------
                    // 最近使ったファイルを更新
                    //
                    // Aを表示中にBを選んだ場合
                    // B → A → その他
                    // の順になる
                    // ------------------------------------------

                    store.addRecentFile(
                        selectedUri,
                        selectedFileName
                    )


                    // ------------------------------------------
                    // 現在のPDFを切り替え
                    // ------------------------------------------

                    store.setDocumentUri(
                        selectedUri
                    )

                    uri =
                        selectedUri


                    // ------------------------------------------
                    // Bの状態を読み直す
                    // ------------------------------------------

                    pageIndex =
                        getLastPage(
                            context,
                            selectedUri.toString()
                        )

                    pageCount =
                        0

                    markers =
                        store.loadMarkers(
                            selectedUri
                        )


                    // ------------------------------------------
                    // 古いPDFの描画を破棄
                    // ------------------------------------------

                    rendered =
                        null

                    renderedPageIndex =
                        -1

                    message =
                        null
                },

                onDismiss = {

                    showRecentFiles =
                        false
                }
            )
        }


        // ==================================================
        // 使い方
        // ==================================================

        if (showHowToUse) {

            HowToUseDialog(

                onDismiss = {
                    showHowToUse = false
                }
            )
        }


        // ==================================================
        // このアプリについて
        // ==================================================

        if (showAbout) {

            AboutDialog(

                onDismiss = {
                    showAbout = false
                }
            )
        }
    }
}
