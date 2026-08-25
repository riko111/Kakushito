package com.isoffice.kakushito.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import com.isoffice.kakushito.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException


data class RenderedPage(
    val bitmap: Bitmap,
    val width: Int,
    val height: Int,
    val pageCount: Int
)

private const val PDF_RENDER_SCALE = 2
private const val PDF_PROVIDER_RETRY_COUNT = 5
private const val PDF_PROVIDER_RETRY_DELAY_MS = 200L

/**
 * Open a PDF descriptor with a short retry window.
 *
 * Some DocumentsProvider implementations (notably cloud storage providers)
 * can be temporarily unavailable immediately after ACTION_OPEN_DOCUMENT
 * returns. In that case ContentResolver may report "No content provider".
 * Retrying here prevents the first-open race from leaving the app on the
 * "PDFを選択してください" screen even though a file was just selected.
 */
private fun openPdfDescriptor(
    context: Context,
    uri: Uri
): ParcelFileDescriptor {
    var lastError: FileNotFoundException? = null

    repeat(PDF_PROVIDER_RETRY_COUNT) { attempt ->
        try {
            return context.contentResolver
                .openFileDescriptor(uri, "r")
                ?: throw FileNotFoundException(
                    "Could not open PDF: $uri"
                )
        } catch (error: FileNotFoundException) {
            lastError = error

            if (attempt < PDF_PROVIDER_RETRY_COUNT - 1) {
                Thread.sleep(PDF_PROVIDER_RETRY_DELAY_MS)
            }
        }
    }

    throw lastError
        ?: FileNotFoundException("Could not open PDF: $uri")
}


suspend fun renderPage(
    context: Context,
    uri: Uri,
    pageIndex: Int
): RenderedPage = withContext(Dispatchers.IO) {
    openPdfDescriptor(context, uri).use { descriptor ->
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
}

fun getPageCount(
    context: Context,
    uri: Uri
): Int {
    return openPdfDescriptor(context, uri).use { descriptor ->
        PdfRenderer(descriptor).use { renderer ->
            renderer.pageCount
        }
    }
}
