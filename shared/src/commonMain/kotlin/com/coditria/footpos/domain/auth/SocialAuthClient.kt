package com.coditria.footpos.domain.auth

import com.coditria.footpos.core.common.Result

/**
 * Port for a single OAuth provider (Google, Apple).
 *
 * Implementations live in platform source sets — Android wires Google, iOS wires
 * both Google and Apple. The split-port design (one interface per provider) means
 * a platform that doesn't support a provider simply doesn't supply a binding,
 * instead of every implementation having to no-op unsupported methods.
 */
interface SocialAuthClient {
    /**
     * Whether the provider is reachable on this device / platform. The UI uses this
     * to hide buttons (e.g. Apple Sign-In on Android) without the data layer having
     * to know which platform it's on.
     */
    val isAvailable: Boolean
    suspend fun signIn(): Result<SocialCredential>
}

/** Marker interfaces for DI binding — keeps Koin lookups type-safe. */
interface GoogleAuthClient : SocialAuthClient
interface AppleAuthClient : SocialAuthClient
