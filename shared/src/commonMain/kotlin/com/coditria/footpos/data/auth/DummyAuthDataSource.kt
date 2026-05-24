package com.coditria.footpos.data.auth

import com.coditria.footpos.core.common.AppError
import com.coditria.footpos.core.common.Logger
import com.coditria.footpos.core.common.Result
import com.coditria.footpos.core.common.Uuid
import com.coditria.footpos.domain.auth.SocialCredential
import com.coditria.footpos.domain.model.AuthProvider
import com.coditria.footpos.domain.model.User
import com.coditria.footpos.domain.model.UserId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * In-memory stand-in for a real identity backend (Firestore / Supabase).
 *
 * Stores a {email → password, user} map per process so sign-up + sign-in round-trip works
 * during development. Social sign-in always succeeds (the platform layer already produced a
 * provider credential, so there's no second check to perform here). Adds a small artificial
 * delay so the UI's loading states are exercised.
 *
 * Replacing this with a Firestore/Supabase implementation is a single Koin binding change.
 */
class DummyAuthDataSource(
    private val logger: Logger,
) : AuthDataSource {

    private data class Account(val user: User, val password: String)

    private val mutex = Mutex()
    private val accounts = mutableMapOf<String, Account>()
    private val current = MutableStateFlow<User?>(null)

    override fun observeCurrentUser(): Flow<User?> = current.asStateFlow()

    override suspend fun currentUser(): User? = current.value

    override suspend fun signInWithEmail(email: String, password: String): Result<User> {
        delay(FAKE_LATENCY_MS)
        val key = email.lowercase()
        return mutex.withLock {
            val account = accounts[key]
            when {
                account == null -> Result.Failure(AppError.NotFound("No account found for $email"))
                account.password != password -> Result.Failure(AppError.ValidationError("Incorrect password"))
                else -> {
                    current.value = account.user
                    logger.info("Email sign-in: ${account.user.email}")
                    Result.Success(account.user)
                }
            }
        }
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String?,
    ): Result<User> {
        delay(FAKE_LATENCY_MS)
        val key = email.lowercase()
        return mutex.withLock {
            if (accounts.containsKey(key)) {
                return@withLock Result.Failure(AppError.ValidationError("An account already exists for $email"))
            }
            val user = User(
                id = UserId(Uuid.random()),
                email = email,
                displayName = displayName,
                provider = AuthProvider.Email,
            )
            accounts[key] = Account(user = user, password = password)
            current.value = user
            logger.info("Email sign-up: ${user.email}")
            Result.Success(user)
        }
    }

    override suspend fun signInWithSocial(credential: SocialCredential): Result<User> {
        delay(FAKE_LATENCY_MS)
        // No backend yet — trust the platform-issued credential and synthesise a stable user.
        // A real backend would verify [credential.idToken] and either create or look up the user.
        val email = credential.email ?: "${credential.provider.name.lowercase()}-user@example.com"
        val key = email.lowercase()
        return mutex.withLock {
            val existing = accounts[key]?.user
            val user = existing ?: User(
                id = UserId(Uuid.random()),
                email = email,
                displayName = credential.displayName,
                provider = credential.provider,
            )
            if (existing == null) {
                // Social accounts have no password; store a sentinel so the slot is occupied.
                accounts[key] = Account(user = user, password = SOCIAL_SENTINEL)
            }
            current.value = user
            logger.info("Social sign-in (${credential.provider}): ${user.email}")
            Result.Success(user)
        }
    }

    override suspend fun signOut() {
        mutex.withLock {
            logger.info("Sign-out: ${current.value?.email}")
            current.value = null
        }
    }

    private companion object {
        const val FAKE_LATENCY_MS = 400L
        const val SOCIAL_SENTINEL = "__social__"
    }
}
