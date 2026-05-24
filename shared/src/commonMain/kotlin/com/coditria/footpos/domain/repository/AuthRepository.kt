package com.coditria.footpos.domain.repository

import com.coditria.footpos.core.common.Result
import com.coditria.footpos.domain.auth.SocialCredential
import com.coditria.footpos.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Source of truth for the signed-in user.
 *
 * Reads (the hot [observeCurrentUser] stream and [currentUser]) are split from
 * writes (the sign-in / sign-up / sign-out commands) so view-models can depend on
 * the narrower port they actually need. Mirrors the SettingsReader / SettingsWriter
 * split already used in this codebase.
 */
interface AuthReader {
    fun observeCurrentUser(): Flow<User?>
    suspend fun currentUser(): User?
}

interface AuthWriter {
    suspend fun signInWithEmail(email: String, password: String): Result<User>
    suspend fun signUpWithEmail(email: String, password: String, displayName: String?): Result<User>
    suspend fun signInWithSocial(credential: SocialCredential): Result<User>
    suspend fun signOut()
}

interface AuthRepository : AuthReader, AuthWriter
