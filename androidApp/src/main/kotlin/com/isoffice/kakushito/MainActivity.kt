package com.isoffice.kakushito

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.min
import androidx.core.graphics.createBitmap

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { KakushitoApp(this) }
    }
}

private data class PdfPoint(val x: Float, val y: Float)
private data class Marker(val page: Int, val points: List<PdfPoint>)
private data class RenderedPage(val bitmap: Bitmap, val width: Int, val height: Int, val pageCount: Int)

@Composable
private fun KakushitoApp(context: Context) {
    val store = remember { MarkerStore(context) }
    var uri by remember { mutableStateOf(store.documentUri()) }
    var pageIndex by remember { mutableIntStateOf(0) }
    var rendered by remember { mutableStateOf<RenderedPage?>(null) }
    var markers by remember(uri) { mutableStateOf(uri?.let(store::loadMarkers).orEmpty()) }
    var drawMode by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { selected ->
        if (selected != null) {
            context.contentResolver.takePersistableUriPermission(selected, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            store.setDocumentUri(selected)
            uri = selected
            pageIndex = 0
            markers = emptyList()
            message = null
        }
    }
    LaunchedEffect(uri, pageIndex) {
        rendered = uri?.let { document -> runCatching { renderPage(context, document, pageIndex) }
            .onFailure { message = "PDFを開けませんでした: ${it.message}" }.getOrNull() }
    }
    MaterialTheme {
        Column(Modifier.fillMaxSize().safeContentPadding().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { picker.launch(arrayOf("application/pdf")) }) { Text("PDFを開く") }
                Button(onClick = { drawMode = !drawMode }, enabled = rendered != null) { Text(if (drawMode) "描画を終了" else "マーカーを引く") }
            }
            Spacer(Modifier.height(8.dp))
            if (rendered == null) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Text(message ?: "PDFを選択してください") }
            } else {
                PdfPage(rendered!!, pageIndex, markers.filter { it.page == pageIndex }, drawMode, { marker ->
                    markers = markers + marker
                    uri?.let { store.saveMarkers(it, markers) }
                }, Modifier.weight(1f).fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { pageIndex-- }, enabled = pageIndex > 0) { Text("前へ") }
                    Spacer(Modifier.width(16.dp)); Text("${pageIndex + 1} / ${rendered!!.pageCount}"); Spacer(Modifier.width(16.dp))
                    Button(onClick = { pageIndex++ }, enabled = pageIndex + 1 < rendered!!.pageCount) { Text("次へ") }
                }
            }
        }
    }
}

@Composable
private fun PdfPage(page: RenderedPage, pageIndex: Int, markers: List<Marker>, drawingEnabled: Boolean, onMarkerDrawn: (Marker) -> Unit, modifier: Modifier = Modifier) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var activeStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }
    val scale = if (canvasSize.width == 0 || canvasSize.height == 0) 1f else min(canvasSize.width.toFloat() / page.width, canvasSize.height.toFloat() / page.height)
    val displayWidth = page.width * scale; val displayHeight = page.height * scale
    val offset = Offset((canvasSize.width - displayWidth) / 2f, (canvasSize.height - displayHeight) / 2f)
    Canvas(modifier.background(Color(0xFF555555)).onSizeChanged { canvasSize = it }.pointerInput(drawingEnabled, scale, offset, pageIndex) {
        if (drawingEnabled) detectDragGestures(
            onDragStart = { start -> activeStroke = listOf(start) },
            onDrag = { change, dragAmount ->
                activeStroke = activeStroke + (activeStroke.last() + dragAmount)
                change.consume()
            },
            onDragEnd = {
                val pdfPoints = activeStroke.map { screen -> PdfPoint(((screen.x - offset.x) / scale).coerceIn(0f, page.width.toFloat()), (page.height - (screen.y - offset.y) / scale).coerceIn(0f, page.height.toFloat())) }
                if (pdfPoints.size > 1) onMarkerDrawn(Marker(pageIndex, pdfPoints))
                activeStroke = emptyList()
            },
            onDragCancel = { activeStroke = emptyList() },
        )
    }) {
        drawImage(
            image = page.bitmap.asImageBitmap(),
            srcSize = IntSize(page.bitmap.width, page.bitmap.height),
            dstOffset = IntOffset(offset.x.toInt(), offset.y.toInt()),
            dstSize = IntSize(displayWidth.toInt(), displayHeight.toInt()),
        )
        fun pdfToScreen(point: PdfPoint) = Offset(offset.x + point.x * scale, offset.y + (page.height - point.y) * scale)
        markers.forEach { marker -> marker.points.zipWithNext().forEach { (from, to) -> drawLine(Color(0x88FFE600), pdfToScreen(from), pdfToScreen(to), 18f * scale) } }
        activeStroke.zipWithNext().forEach { (from, to) -> drawLine(Color(0xAAFFE600), from, to, 18f * scale) }
    }
}

private suspend fun renderPage(context: Context, uri: Uri, pageIndex: Int): RenderedPage = withContext(Dispatchers.IO) {
    context.contentResolver.openFileDescriptor(uri, "r")!!.use { descriptor -> PdfRenderer(descriptor).use { renderer ->
        val safeIndex = pageIndex.coerceIn(0, renderer.pageCount - 1)
        renderer.openPage(safeIndex).use { page ->
            val bitmap = createBitmap(page.width * 2, page.height * 2)
            bitmap.eraseColor(android.graphics.Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            RenderedPage(bitmap, page.width, page.height, renderer.pageCount)
        }
    } }
}

private class MarkerStore(context: Context) {
    private val preferences = context.getSharedPreferences("kakushito_markers", Context.MODE_PRIVATE)
    fun documentUri(): Uri? = preferences.getString("document_uri", null)?.let(Uri::parse)
    fun setDocumentUri(uri: Uri) = preferences.edit().putString("document_uri", uri.toString()).apply()
    fun loadMarkers(uri: Uri): List<Marker> = runCatching {
        val array = JSONArray(preferences.getString("markers_${uri}", "[]"))
        List(array.length()) { index -> array.getJSONObject(index).let { item ->
            val points = item.getJSONArray("points")
            Marker(item.getInt("page"), List(points.length()) { pointIndex -> points.getJSONObject(pointIndex).let { PdfPoint(it.getDouble("x").toFloat(), it.getDouble("y").toFloat()) } })
        } }
    }.getOrDefault(emptyList())
    fun saveMarkers(uri: Uri, markers: List<Marker>) {
        val array = JSONArray(); markers.forEach { marker -> array.put(JSONObject().put("page", marker.page).put("points", JSONArray().also { points -> marker.points.forEach { point -> points.put(JSONObject().put("x", point.x).put("y", point.y)) } })) }
        preferences.edit().putString("markers_${uri}", array.toString()).apply()
    }
}
