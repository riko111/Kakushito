package com.isoffice.kakushito

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun KakushitoApp(context: Context) {
    val store = remember { MarkerStore(context) }
    var uri by remember { mutableStateOf(store.documentUri()) }
    var pageIndex by remember { mutableIntStateOf(0) }
    var rendered by remember { mutableStateOf<RenderedPage?>(null) }
    var markers by remember(uri) { mutableStateOf(uri?.let(store::loadMarkers).orEmpty()) }
    var drawMode by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { selected ->
        if (selected != null) {
            context.contentResolver.takePersistableUriPermission(
                selected,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            store.setDocumentUri(selected)
            uri = selected
            pageIndex = 0
            markers = emptyList()
            message = null
        }
    }

    LaunchedEffect(uri, pageIndex) {
        rendered = uri?.let { document ->
            runCatching { renderPage(context, document, pageIndex) }
                .onFailure { message = "PDFを開けませんでした: ${it.message}" }
                .getOrNull()
        }
    }

    MaterialTheme {
        Column(
            Modifier
                .fillMaxSize()
                .safeContentPadding()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { picker.launch(arrayOf("application/pdf")) }) { Text("PDFを開く") }
                Button(onClick = { drawMode = !drawMode }, enabled = rendered != null) {
                    Text(if (drawMode) "描画を終了" else "マーカーを引く")
                }
            }
            Spacer(Modifier.height(8.dp))
            if (rendered == null) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(message ?: "PDFを選択してください")
                }
            } else {
                PdfPage(
                    rendered!!,
                    pageIndex,
                    markers.filter { it.page == pageIndex },
                    drawMode,
                    { marker ->
                        markers = markers + marker
                        uri?.let { store.saveMarkers(it, markers) }
                    },
                    Modifier.weight(1f).fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { pageIndex-- }, enabled = pageIndex > 0) { Text("前へ") }
                    Spacer(Modifier.width(16.dp))
                    Text("${pageIndex + 1} / ${rendered!!.pageCount}")
                    Spacer(Modifier.width(16.dp))
                    Button(onClick = { pageIndex++ }, enabled = pageIndex + 1 < rendered!!.pageCount) { Text("次へ") }
                }
            }
        }
    }
}
