package com.example.pentool

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform