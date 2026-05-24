package com.coditria.footpos.data.repository

import com.coditria.footpos.core.common.Result
import com.coditria.footpos.data.auth.AuthDataSource
import com.coditria.footpos.domain.auth.SocialCredential
import com.coditria.footpos.domain.model.User
import com.coditria.footpos.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

/**
 * Thin pass-through to [AuthDataSource]. The repository exists so the domain layer
 * depends on its own interface (AuthRepository) rather than on the data-source contract,
 * leaving room for future cross-cutting concerns (caching, analytics, retries) without
 * touching call sites.
 */
class AuthRepositoryImpl(
    private val dataSource: AuthDataSource,
) : AuthRepository {

    override fun observeCurrentUser(): Flow<User?> = dataSource.observeCurrentUser()

    override suspend fun currentUser(): User? = dataSource.currentUser()

    override suspend fun signInWithEmail(email: String, password: String): Result<User> =
        dataSource.signInWithEmail(email, password)

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String?,
    ): Result<User> = dataSource.signUpWithEmail(email, password, displayName)

    override suspend fun signInWithSocial(credential: SocialCredential): Result<User> =
        dataSource.signInWithSocial(credential)

    override suspend fun signOut() = dataSource.signOut()
}
