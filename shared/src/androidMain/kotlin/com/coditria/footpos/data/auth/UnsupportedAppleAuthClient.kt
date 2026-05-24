package com.coditria.footpos.data.auth

import com.coditria.footpos.core.common.AppError
import com.coditria.footpos.core.common.Result
import com.coditria.footpos.domain.auth.AppleAuthClient
import com.coditria.footpos.domain.auth.SocialCredential

/**
 * Apple Sign-In is iOS-only. We still bind an implementation on Android so the
 * use case graph resolves without conditional Koin definitions; the UI hides the
 * button via [isAvailable] so this `signIn` should never actually be called.
 */
class UnsupportedAppleAuthClient : AppleAuthClient {
    override val isAvailable: Boolean = false

    override suspend fun signIn(): Result<SocialCredential> =
        Result.Failure(AppError.UnknownError("Apple Sign-In is not available on this platform"))
}
