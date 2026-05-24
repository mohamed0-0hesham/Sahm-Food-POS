package com.coditria.footpos.domain.auth

import com.coditria.footpos.domain.model.AuthProvider

/**
 * Provider-issued identity returned by Google / Apple sign-in flows.
 * The [idToken] is opaque to the domain; the data source forwards it to the
 * identity backend (Firestore / Supabase / custom) for verification & exchange.
 */
data class SocialCredential(
    val provider: AuthProvider,
    val idToken: String,
    val email: String?,
    val displayName: String?,
)
