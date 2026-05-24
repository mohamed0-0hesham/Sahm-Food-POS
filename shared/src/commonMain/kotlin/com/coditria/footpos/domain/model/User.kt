package com.coditria.footpos.domain.model

import kotlin.jvm.JvmInline

@JvmInline
value class UserId(val value: String)

enum class AuthProvider { Email, Google, Apple }

data class User(
    val id: UserId,
    val email: String,
    val displayName: String?,
    val provider: AuthProvider,
)
