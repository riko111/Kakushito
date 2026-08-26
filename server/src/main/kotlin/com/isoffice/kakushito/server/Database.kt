package com.isoffice.kakushito.server

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

fun createDataSource(): DataSource {
    val config = HikariConfig().apply {
        jdbcUrl = buildJdbcUrl()
        username = env("DB_USER", "kakushito")
        password = env("DB_PASSWORD")
        maximumPoolSize = 5
        minimumIdle = 1
    }

    return HikariDataSource(config)
}

private fun buildJdbcUrl(): String {
    val host = env("DB_HOST", "127.0.0.1")
    val port = env("DB_PORT", "3306")
    val database = env("DB_NAME", "kakushito_dev")

    return "jdbc:mysql://$host:$port/$database" +
            "?useUnicode=true" +
            "&characterEncoding=utf8" +
            "&serverTimezone=Asia/Tokyo"
}

private fun env(name: String, default: String? = null): String {
    return System.getenv(name)
        ?: default
        ?: error("Environment variable $name is not set")
}