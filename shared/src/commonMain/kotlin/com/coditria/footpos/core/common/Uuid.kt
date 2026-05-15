package com.coditria.footpos.core.common

import kotlin.random.Random

object Uuid {
    private val hex = "0123456789abcdef".toCharArray()

    fun random(): String {
        val bytes = ByteArray(16) { Random.nextInt(256).toByte() }
        bytes[6] = (bytes[6].toInt() and 0x0F or 0x40).toByte()
        bytes[8] = (bytes[8].toInt() and 0x3F or 0x80).toByte()
        return buildString(36) {
            for (i in bytes.indices) {
                val v = bytes[i].toInt() and 0xFF
                append(hex[v ushr 4])
                append(hex[v and 0x0F])
                if (i == 3 || i == 5 || i == 7 || i == 9) append('-')
            }
        }
    }
}
