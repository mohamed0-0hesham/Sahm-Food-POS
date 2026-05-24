package com.coditria.footpos.data.auth

import com.coditria.footpos.core.common.Result
import com.coditria.footpos.domain.auth.SocialCredential
import com.coditria.footpos.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Backend port for identity. Implementations talk to Firestore / Supabase / etc.
 *
 * The repository depends on this port (not a concrete client), so swapping the
 * backend is a single Koin binding change and no domain or presentation code moves.
 */
interface AuthDataSource {
    fun observeCurrentUser(): Flow<User?>
    suspend fun currentUser(): User?

    suspend fun signInWithEmail(email: String, password: String): Result<User>
    suspend fun signUpWithEmail(email: String, password: String, displayName: String?): Result<User>
    suspend fun signInWithSocial(credential: SocialCredential): Result<User>
    suspend fun signOut()
}
