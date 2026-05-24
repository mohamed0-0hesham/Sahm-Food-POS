package com.coditria.footpos.data.auth

import com.coditria.footpos.core.common.Result
import com.coditria.footpos.domain.auth.GoogleAuthClient
import com.coditria.footpos.domain.auth.SocialCredential
import com.coditria.footpos.domain.model.AuthProvider
import kotlinx.coroutines.delay

/**
 * Stub Google Sign-In for iOS.
 *
 * Real wiring will live here once the GoogleSignIn pod is added — it should call
 * `GIDSignIn.sharedInstance.signIn(presentingViewController:)` from the main thread
 * and forward `idToken` via [SocialCredential.idToken]. For now we emit a deterministic
 * dummy credential so the full architecture is exercised end-to-end.
 */
class IosGoogleAuthClient : GoogleAuthClient {
    override val isAvailable: Boolean = true

    override suspend fun signIn(): Result<SocialCredential> {
        delay(SIMULATED_LATENCY_MS)
        return Result.Success(
            SocialCredential(
                provider = AuthProvider.Google,
                idToken = "ios-google-dummy-token",
                email = "google.user@example.com",
                displayName = "Google User",
            ),
        )
    }

    private companion object {
        const val SIMULATED_LATENCY_MS = 600L
    }
}
