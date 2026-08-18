package com.isoffice.kakushito

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.edit

class MarkerStore(context: Context) {
    private val preferences = context.getSharedPreferences("kakushito_markers", Context.MODE_PRIVATE)

    fun documentUri(): Uri? = preferences.getString("document_uri", null)?.let(Uri::parse)

    fun setDocumentUri(uri: Uri) = preferences.edit { putString("document_uri", uri.toString()) }

    fun loadMarkers(uri: Uri): List<Marker> = runCatching {
        val array = JSONArray(preferences.getString("markers_${uri}", "[]"))
        List(array.length()) { index ->
            array.getJSONObject(index).let { item ->
                val points = item.getJSONArray("points")
                Marker(
                    item.getInt("page"),
                    List(points.length()) { pointIndex ->
                        points.getJSONObject(pointIndex).let {
                            PdfPoint(it.getDouble("x").toFloat(), it.getDouble("y").toFloat())
                        }
                    }
                )
            }
        }
    }.getOrDefault(emptyList())

    fun saveMarkers(uri: Uri, markers: List<Marker>) {
        val array = JSONArray()
        markers.forEach { marker ->
            array.put(JSONObject().put("page", marker.page).put("points", JSONArray().also { points ->
                marker.points.forEach { point ->
                    points.put(JSONObject().put("x", point.x).put("y", point.y))
                }
            }))
        }
        preferences.edit { putString("markers_${uri}", array.toString()) }
    }
}
