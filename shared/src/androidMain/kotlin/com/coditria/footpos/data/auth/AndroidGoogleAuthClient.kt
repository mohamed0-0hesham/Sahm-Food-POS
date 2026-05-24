package com.coditria.footpos.data.auth

import com.coditria.footpos.core.common.Result
import com.coditria.footpos.domain.auth.GoogleAuthClient
import com.coditria.footpos.domain.auth.SocialCredential
import com.coditria.footpos.domain.model.AuthProvider
import kotlinx.coroutines.delay

/**
 * Stub Google Sign-In for Android.
 *
 * Returns a deterministic dummy credential so the upstream flow (UI → use case →
 * repository → DummyAuthDataSource) is wired end-to-end. When the real Google SDK
 * lands (Credential Manager + Identity Services), replace the body of [signIn] with
 * the SDK call and forward the resulting ID token via [SocialCredential.idToken].
 */
class AndroidGoogleAuthClient : GoogleAuthClient {
    override val isAvailable: Boolean = true

    override suspend fun signIn(): Result<SocialCredential> {
        delay(SIMULATED_LATENCY_MS)
        return Result.Success(
            SocialCredential(
                provider = AuthProvider.Google,
                idToken = "android-google-dummy-token",
                email = "google.user@example.com",
                displayName = "Google User",
            ),
        )
    }

    private companion object {
        const val SIMULATED_LATENCY_MS = 600L
    }
}
