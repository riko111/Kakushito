package com.isoffice.kakushito

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform