package com.coditria.footpos.core.common

sealed class AppError(open val message: String) {
    data class DatabaseError(override val message: String) : AppError(message)
    data class NetworkError(override val message: String) : AppError(message)
    data class ValidationError(override val message: String) : AppError(message)
    data class HardwareError(override val message: String) : AppError(message)
    data class NotFound(override val message: String) : AppError(message)
    data class UnknownError(override val message: String) : AppError(message)
}
