package com.coditria.footpos.core.common

import io.github.aakira.napier.Napier

interface Logger {
    fun debug(message: String, throwable: Throwable? = null)
    fun info(message: String, throwable: Throwable? = null)
    fun warn(message: String, throwable: Throwable? = null)
    fun error(message: String, throwable: Throwable? = null)
}

class NapierLogger(private val tag: String = "SahmPOS") : Logger {
    override fun debug(message: String, throwable: Throwable?) = Napier.d(message, throwable, tag)
    override fun info(message: String, throwable: Throwable?) = Napier.i(message, throwable, tag)
    override fun warn(message: String, throwable: Throwable?) = Napier.w(message, throwable, tag)
    override fun error(message: String, throwable: Throwable?) = Napier.e(message, throwable, tag)
}
