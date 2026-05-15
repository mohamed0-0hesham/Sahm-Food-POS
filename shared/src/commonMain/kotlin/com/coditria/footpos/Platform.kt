package com.coditria.footpos

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform