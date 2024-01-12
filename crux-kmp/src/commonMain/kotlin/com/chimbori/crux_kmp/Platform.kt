package com.chimbori.crux_kmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform