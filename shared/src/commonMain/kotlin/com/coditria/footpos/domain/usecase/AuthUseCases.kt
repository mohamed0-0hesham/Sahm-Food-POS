package com.coditria.footpos.domain.usecase

import com.coditria.footpos.core.common.AppError
import com.coditria.footpos.core.common.Result
import com.coditria.footpos.domain.auth.AppleAuthClient
import com.coditria.footpos.domain.auth.GoogleAuthClient
import com.coditria.footpos.domain.model.User
import com.coditria.footpos.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class ObserveCurrentUserUseCase(private val repo: AuthRepository) {
    operator fun invoke(): Flow<User?> = repo.observeCurrentUser()
}

class GetCurrentUserUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(): User? = repo.currentUser()
}

class SignInWithEmailUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        validateEmail(email)?.let { return Result.Failure(it) }
        validatePassword(password)?.let { return Result.Failure(it) }
        return repo.signInWithEmail(email.trim(), password)
    }
}

class SignUpWithEmailUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String, password: String, displayName: String?): Result<User> {
        validateEmail(email)?.let { return Result.Failure(it) }
        validatePassword(password)?.let { return Result.Failure(it) }
        return repo.signUpWithEmail(email.trim(), password, displayName?.trim()?.takeIf { it.isNotEmpty() })
    }
}

/**
 * Two-step: first obtain a provider credential from the platform SDK, then exchange it
 * via the repository. Splitting these means the platform layer never knows about User and
 * the data source never knows about the Google SDK.
 */
class SignInWithGoogleUseCase(
    private val googleClient: GoogleAuthClient,
    private val repo: AuthRepository,
) {
    suspend operator fun invoke(): Result<User> = when (val r = googleClient.signIn()) {
        is Result.Success -> repo.signInWithSocial(r.value)
        is Result.Failure -> r
    }
}

class SignInWithAppleUseCase(
    private val appleClient: AppleAuthClient,
    private val repo: AuthRepository,
) {
    suspend operator fun invoke(): Result<User> = when (val r = appleClient.signIn()) {
        is Result.Success -> repo.signInWithSocial(r.value)
        is Result.Failure -> r
    }
}

class SignOutUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke() = repo.signOut()
}

private fun validateEmail(email: String): AppError.ValidationError? {
    val trimmed = email.trim()
    if (trimmed.isEmpty()) return AppError.ValidationError("Email is required")
    // Intentionally loose — the backend is authoritative. We just keep obvious typos out.
    if (!trimmed.contains('@') || !trimmed.contains('.')) {
        return AppError.ValidationError("Enter a valid email address")
    }
    return null
}

private fun validatePassword(password: String): AppError.ValidationError? {
    if (password.length < 6) return AppError.ValidationError("Password must be at least 6 characters")
    return null
}
