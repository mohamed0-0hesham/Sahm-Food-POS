package com.coditria.footpos.data.auth

import com.coditria.footpos.core.common.Result
import com.coditria.footpos.domain.auth.AppleAuthClient
import com.coditria.footpos.domain.auth.SocialCredential
import com.coditria.footpos.domain.model.AuthProvider
import kotlinx.coroutines.delay

/**
 * Stub Apple Sign-In for iOS.
 *
 * Real wiring will use `ASAuthorizationAppleIDProvider` + `ASAuthorizationController`
 * from AuthenticationServices, forwarding the resulting `identityToken` via
 * [SocialCredential.idToken]. Until then, we emit a deterministic dummy credential.
 */
class IosAppleAuthClient : AppleAuthClient {
    override val isAvailable: Boolean = true

    override suspend fun signIn(): Result<SocialCredential> {
        delay(SIMULATED_LATENCY_MS)
        return Result.Success(
            SocialCredential(
                provider = AuthProvider.Apple,
                idToken = "ios-apple-dummy-token",
                email = "apple.user@example.com",
                displayName = "Apple User",
            ),
        )
    }

    private companion object {
        const val SIMULATED_LATENCY_MS = 600L
    }
}
