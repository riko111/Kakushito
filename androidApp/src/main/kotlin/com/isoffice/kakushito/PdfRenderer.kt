package com.isoffice.kakushito

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class RenderedPage(val bitmap: Bitmap, val width: Int, val height: Int, val pageCount: Int)

suspend fun renderPage(context: Context, uri: Uri, pageIndex: Int): RenderedPage = withContext(Dispatchers.IO) {
    context.contentResolver.openFileDescriptor(uri, "r")!!.use { descriptor ->
        PdfRenderer(descriptor).use { renderer ->
            val safeIndex = pageIndex.coerceIn(0, renderer.pageCount - 1)
            renderer.openPage(safeIndex).use { page ->
                val bitmap = createBitmap(page.width * 2, page.height * 2)
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                RenderedPage(bitmap, page.width, page.height, renderer.pageCount)
            }
        }
    }
}
