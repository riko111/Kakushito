package com.isoffice.kakushito.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class RenderedPage(val bitmap: Bitmap, val width: Int, val height: Int, val pageCount: Int)

private const val PDF_RENDER_SCALE = 2

suspend fun renderPage(
    context: Context,
    uri: Uri,
    pageIndex: Int
): RenderedPage = withContext(Dispatchers.IO) {
    context.contentResolver.openFileDescriptor(uri, "r")!!.use { descriptor ->
        PdfRenderer(descriptor).use { renderer ->

            val safeIndex =
                pageIndex.coerceIn(
                    0,
                    renderer.pageCount - 1
                )

            renderer.openPage(safeIndex).use { page ->

                // PDFの論理サイズ
                val pageWidth = page.width
                val pageHeight = page.height

                // 高解像度でレンダリング
                val bitmap = createBitmap(
                    pageWidth * PDF_RENDER_SCALE,
                    pageHeight * PDF_RENDER_SCALE
                )

                bitmap.eraseColor(
                    Color.WHITE
                )

                page.render(
                    bitmap,
                    null,
                    null,
                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                )

                RenderedPage(
                    bitmap = bitmap,
                    width = pageWidth,
                    height = pageHeight,
                    pageCount = renderer.pageCount
                )
            }
        }
    }
}fun getPageCount(context: Context, uri: Uri): Int {
    return context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
        PdfRenderer(descriptor).use { renderer ->
            renderer.pageCount
        }
    } ?: throw IllegalStateException("PDFを開けませんでした")
}