package com.isoffice.kakushito

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

private val MarkerColors = listOf(
    0xFF29B6F6.toInt(), // 水色
    0xFFFFE600.toInt(), // 黄色
    0xFF66BB6A.toInt()  // 緑
)

private val MarkerWidths = listOf(8f, 11f, 14f, 18f, 22f)

private fun getFileName(
    context: Context,
    uri: android.net.Uri
): String {
    val cursor = context.contentResolver.query(
        uri,
        arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
        null,
        null,
        null
    )

    cursor?.use {
        if (it.moveToFirst()) {
            val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (index >= 0) return it.getString(index)
        }
    }

    return "PDF"
}

@Composable
private fun MarkerWidthDialog(
    selectedWidth: Float,
    onSelected: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("マーカーの太さ") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MarkerWidths.forEach { width ->
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .then(
                                if (width == selectedWidth) {
                                    Modifier.border(2.dp, Color(0xFFFFC107), CircleShape)
                                } else {
                                    Modifier
                                }
                            )
                            .pointerInput(width) {
                                detectTapGestures {
                                    onSelected(width)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width.dp)
                                .background(Color(0xFFFFC107), CircleShape)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("閉じる")
            }
        }
    )
}

@Composable
private fun MarkerColorDialog(
    selectedColor: Int,
    onSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("マーカーの色") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MarkerColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .then(
                                if (color == selectedColor) {
                                    Modifier.border(2.dp, Color.DarkGray, CircleShape)
                                } else {
                                    Modifier
                                }
                            )
                            .pointerInput(color) {
                                detectTapGestures {
                                    onSelected(color)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(color), CircleShape)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("閉じる")
            }
        }
    )
}

@Composable
fun KakushitoApp(context: Context) {
    val store = remember { DocumentStore(context) }

    var uri by remember { mutableStateOf(store.documentUri()) }
    var pageIndex by remember(uri) {
        mutableIntStateOf(uri?.let { getLastPage(context, it.toString()) } ?: 0)
    }
    var renderedPageIndex by remember { mutableIntStateOf(-1) }
    var rendered by remember { mutableStateOf<RenderedPage?>(null) }

    var markers by remember(uri) {
        mutableStateOf(uri?.let(store::loadMarkers).orEmpty())
    }

    var drawMode by remember { mutableStateOf(DrawMode.None) }
    var markerColor by remember { mutableIntStateOf(0xFFFFE600.toInt()) }
    var markerWidth by remember { mutableFloatStateOf(14f) }
    var showWidthDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }

    var message by remember { mutableStateOf<String?>(null) }
    var isRendering by remember { mutableStateOf(false) }
    var pageCount by remember { mutableIntStateOf(0) }
    var pageSliderValue by remember { mutableFloatStateOf(0f) }
    var fileName by remember { mutableStateOf("") }
    var showRecentFiles by remember { mutableStateOf(false) }

    fun goToPage(page: Int) {
        if (pageCount <= 0) return
        val newPage = page.coerceIn(0, pageCount - 1)
        pageIndex = newPage
        uri?.let { saveLastPage(context, it.toString(), newPage) }
    }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { selected ->
        if (selected != null) {
            context.contentResolver.takePersistableUriPermission(
                selected,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val selectedFileName = getFileName(context, selected)
            store.setDocumentUri(selected)
            store.addRecentFile(selected, selectedFileName)
            uri = selected
            pageCount = getPageCount(context, selected)
            markers = emptyList()
            message = null
        }
    }

    LaunchedEffect(uri) {
        val document = uri ?: return@LaunchedEffect
        fileName = getFileName(context, document)
        pageCount = runCatching { getPageCount(context, document) }
            .getOrElse {
                message = "PDFを開けませんでした: ${it.message}"
                0
            }
    }

    LaunchedEffect(uri, pageIndex) {
        val document = uri ?: return@LaunchedEffect
        isRendering = true
        val result = runCatching { renderPage(context, document, pageIndex) }

        result
            .onSuccess {
                rendered = it
                renderedPageIndex = pageIndex
                pageCount = it.pageCount
                message = null
            }
            .onFailure {
                message = "PDFを開けませんでした: ${it.message}"
            }

        isRendering = false
    }

    LaunchedEffect(pageIndex) {
        pageSliderValue = pageIndex.toFloat()
    }

    MaterialTheme {
        Column(
            Modifier
                .fillMaxSize()
                .safeContentPadding()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(10f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    picker.launch(arrayOf("application/pdf"))
                }) {
                    Icon(
                        painter = painterResource(R.drawable.folder_open),
                        contentDescription = "PDFを開く"
                    )
                }

                IconButton(onClick = { showRecentFiles = true }) {
                    Icon(
                        painter = painterResource(R.drawable.history),
                        contentDescription = "最近使ったファイル"
                    )
                }

                Text(
                    text = fileName,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // マーカー：短押しでON/OFF、長押しで太さ選択
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .pointerInput(rendered, drawMode) {
                            detectTapGestures(
                                onTap = {
                                    if (rendered != null) {
                                        drawMode = if (drawMode == DrawMode.Marker) {
                                            DrawMode.None
                                        } else {
                                            DrawMode.Marker
                                        }
                                    }
                                },
                                onLongPress = {
                                    if (rendered != null) showWidthDialog = true
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ink_marker),
                        contentDescription = "マーカー",
                        tint = Color(markerColor),
                        modifier = Modifier.background(
                            if (drawMode == DrawMode.Marker) {
                                Color(0x22FFC107)
                            } else {
                                Color.Transparent
                            }
                        )
                    )
                }

                // マーカー色
                IconButton(
                    onClick = { showColorDialog = true },
                    enabled = rendered != null
                ) {
                    Icon(
                        painter = painterResource(R.drawable.palette),
                        contentDescription = "マーカーの色"
                    )
                }

                IconButton(
                    onClick = {
                        drawMode = if (drawMode == DrawMode.Eraser) {
                            DrawMode.None
                        } else {
                            DrawMode.Eraser
                        }
                    },
                    enabled = rendered != null,
                    modifier = Modifier.background(
                        if (drawMode == DrawMode.Eraser) {
                            Color(0x229E9E9E)
                        } else {
                            Color.Transparent
                        }
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ink_eraser),
                        contentDescription = "消しゴム",
                        tint = Color.Gray
                    )
                }

                IconButton(
                    onClick = {
                        drawMode = if (drawMode == DrawMode.Hide) {
                            DrawMode.None
                        } else {
                            DrawMode.Hide
                        }
                    },
                    enabled = rendered != null,
                    modifier = Modifier.background(
                        if (drawMode == DrawMode.Hide) {
                            Color(0x229E9E9E)
                        } else {
                            Color.Transparent
                        }
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.visibility_off),
                        contentDescription = "隠すモード",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (rendered == null) {
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clipToBounds()
                        .zIndex(0f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(message ?: "PDFを選択してください")
                }
            } else {
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    PdfPage(
                        page = rendered!!,
                        pageIndex = pageIndex,
                        markers = if (renderedPageIndex == pageIndex) {
                            markers.filter { it.page == pageIndex }
                        } else {
                            emptyList()
                        },
                        drawMode = drawMode,
                        markerColor = markerColor,
                        markerWidth = markerWidth,
                        onMarkerDrawn = { marker ->
                            markers = markers + marker
                            uri?.let { store.saveMarkers(it, markers) }
                        },
                        onMarkerErased = { marker ->
                            markers = markers - marker
                            uri?.let { store.saveMarkers(it, markers) }
                        },
                        onPreviousPage = { goToPage(pageIndex - 1) },
                        onNextPage = { goToPage(pageIndex + 1) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${pageIndex + 1}")
                    Slider(
                        value = pageSliderValue,
                        onValueChange = { value -> pageSliderValue = value },
                        onValueChangeFinished = {
                            goToPage(pageSliderValue.roundToInt())
                        },
                        valueRange = 0f..(pageCount - 1)
                            .coerceAtLeast(0).toFloat(),
                        steps = (pageCount - 2).coerceAtLeast(0),
                        enabled = pageCount > 1,
                        modifier = Modifier.weight(1f)
                    )
                    Text("$pageCount")
                }
            }
        }

        if (showWidthDialog) {
            MarkerWidthDialog(
                selectedWidth = markerWidth,
                onSelected = {
                    markerWidth = it
                    showWidthDialog = false
                },
                onDismiss = { showWidthDialog = false }
            )
        }

        if (showColorDialog) {
            MarkerColorDialog(
                selectedColor = markerColor,
                onSelected = {
                    markerColor = it
                    showColorDialog = false
                },
                onDismiss = { showColorDialog = false }
            )
        }

        if (showRecentFiles) {
            val recentFiles = remember { store.loadRecentFiles() }

            AlertDialog(
                onDismissRequest = { showRecentFiles = false },
                title = { Text("最近使ったファイル") },
                text = {
                    if (recentFiles.isEmpty()) {
                        Text("最近使ったファイルはありません")
                    } else {
                        Column {
                            recentFiles.forEach { file ->
                                TextButton(
                                    onClick = {
                                        showRecentFiles = false
                                        rendered = null
                                        store.setDocumentUri(file.uri)
                                        uri = file.uri
                                        pageIndex = getLastPage(context, file.uri.toString())
                                        markers = store.loadMarkers(file.uri)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = file.fileName, maxLines = 1)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showRecentFiles = false }) {
                        Text("閉じる")
                    }
                }
            )
        }
    }
}
