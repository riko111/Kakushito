package com.isoffice.kakushito

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.edit

class DocumentStore(context: Context) {
    private val preferences = context.getSharedPreferences("kakushito_markers", Context.MODE_PRIVATE)
    private companion object {
        const val RECENT_FILES_KEY = "recent_files"
        const val MAX_RECENT_FILES = 10
    }

    fun addRecentFile(uri: Uri, fileName: String) {
        val current = loadRecentFiles()
            .filterNot { it.uri == uri }
            .toMutableList()

        current.add(0, RecentFile(uri, fileName))

        val array = JSONArray()
        current.take(MAX_RECENT_FILES).forEach { file ->
            array.put(
                JSONObject()
                    .put("uri", file.uri.toString())
                    .put("fileName", file.fileName)
            )
        }

        preferences.edit { putString(RECENT_FILES_KEY, array.toString()) }
    }

    fun loadRecentFiles(): List<RecentFile> = runCatching {
        val array = JSONArray(preferences.getString(RECENT_FILES_KEY, "[]"))
        List(array.length()) { index ->
            array.getJSONObject(index).let { item ->
                RecentFile(
                    uri = Uri.parse(item.getString("uri")),
                    fileName = item.getString("fileName")
                )
            }
        }
    }.getOrDefault(emptyList())

    fun documentUri(): Uri? = preferences.getString("document_uri", null)?.let(Uri::parse)

    fun setDocumentUri(uri: Uri) = preferences.edit { putString("document_uri", uri.toString()) }

    fun loadMarkers(uri: Uri): List<Marker> = runCatching {
        val array = JSONArray(preferences.getString("markers_${uri}", "[]"))
        List(array.length()) { index ->
            array.getJSONObject(index).let { item ->
                val points = item.getJSONArray("points")
                Marker(
                    page = item.getInt("page"),
                    points = List(points.length()) { pointIndex ->
                        points.getJSONObject(pointIndex).let {
                            PdfPoint(
                                it.getDouble("x").toFloat(),
                                it.getDouble("y").toFloat()
                            )
                        }
                    },
                    color = item.optInt("color", 0xFFFFE600.toInt()),
                    width = item.optDouble("width", 14.0).toFloat()
                )
            }
        }
    }.getOrDefault(emptyList())

    fun saveMarkers(uri: Uri, markers: List<Marker>) {
        val array = JSONArray()
        markers.forEach { marker ->
            array.put(
                JSONObject()
                    .put("page", marker.page)
                    .put("color", marker.color)
                    .put("width", marker.width)
                    .put("points", JSONArray().also { points ->
                        marker.points.forEach { point ->
                            points.put(
                                JSONObject()
                                    .put("x", point.x)
                                    .put("y", point.y)
                            )
                        }
                    })
            )
        }
        preferences.edit { putString("markers_${uri}", array.toString()) }
    }
}
