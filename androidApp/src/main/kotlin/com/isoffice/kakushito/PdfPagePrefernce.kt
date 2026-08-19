package com.isoffice.kakushito

import android.content.Context
import androidx.core.content.edit

private const val PREFS_NAME = "kakushito_prefs"
private const val KEY_LAST_PAGE_PREFIX = "last_page_"

fun getLastPage(context: Context, pdfKey: String): Int {
    val prefs = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    return prefs.getInt(
        KEY_LAST_PAGE_PREFIX + pdfKey,
        0
    )
}

fun saveLastPage(
    context: Context,
    pdfKey: String,
    page: Int
) {
    val prefs = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    prefs.edit {
        putInt(KEY_LAST_PAGE_PREFIX + pdfKey, page)
    }
}