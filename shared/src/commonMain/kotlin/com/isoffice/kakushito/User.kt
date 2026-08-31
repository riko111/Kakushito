package com.isoffice.kakushito

import kotlinx.serialization.Serializable

/**
 * `GET /api/me` のレスポンス。
 *
 * サーバ側 `com.isoffice.kakushito.server.User` の JSON 契約と一致させること
 * （フィールド名 = kotlinx.serialization 既定のプロパティ名）。
 */
@Serializable
data class User(
    val id: Long,
    val firebaseUid: String,
    val email: String? = null,
    val displayName: String? = null,
    val plan: String,
)
