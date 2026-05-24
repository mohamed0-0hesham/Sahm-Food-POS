package com.coditria.footpos.presentation.auth

import com.coditria.footpos.core.common.Result
import com.coditria.footpos.domain.auth.AppleAuthClient
import com.coditria.footpos.domain.auth.GoogleAuthClient
import com.coditria.footpos.domain.usecase.SignInWithAppleUseCase
import com.coditria.footpos.domain.usecase.SignInWithEmailUseCase
import com.coditria.footpos.domain.usecase.SignInWithGoogleUseCase
import com.coditria.footpos.domain.usecase.SignUpWithEmailUseCase
import com.coditria.footpos.presentation.shared.MviViewModel

enum class AuthMode { SignIn, SignUp }

data class AuthState(
    val mode: AuthMode = AuthMode.SignIn,
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val submitting: Boolean = false,
    val socialInFlight: Boolean = false,
    val errorMessage: String? = null,
    val googleAvailable: Boolean = false,
    val appleAvailable: Boolean = false,
) {
    val canSubmit: Boolean
        get() = !submitting && !socialInFlight && email.isNotBlank() && password.isNotBlank()
}

sealed interface AuthEffect {
    object Authenticated : AuthEffect
}

class AuthViewModel(
    private val signInWithEmail: SignInWithEmailUseCase,
    private val signUpWithEmail: SignUpWithEmailUseCase,
    private val signInWithGoogle: SignInWithGoogleUseCase,
    private val signInWithApple: SignInWithAppleUseCase,
    private val googleClient: GoogleAuthClient,
    private val appleClient: AppleAuthClient,
) : MviViewModel<AuthState, AuthEffect>() {

    // initialState() runs from the MviViewModel base constructor — before this subclass's
    // constructor fields are assigned — so it must NOT touch googleClient / appleClient.
    // The provider-availability flags are filled in from the init block below.
    override fun initialState(): AuthState = AuthState()

    init {
        updateState {
            it.copy(
                googleAvailable = googleClient.isAvailable,
                appleAvailable = appleClient.isAvailable,
            )
        }
    }

    fun onModeChanged(mode: AuthMode) = updateState {
        it.copy(mode = mode, errorMessage = null)
    }

    fun onEmailChanged(value: String) = updateState { it.copy(email = value, errorMessage = null) }
    fun onPasswordChanged(value: String) = updateState { it.copy(password = value, errorMessage = null) }
    fun onDisplayNameChanged(value: String) = updateState { it.copy(displayName = value, errorMessage = null) }

    fun onSubmit() {
        val s = currentState
        if (!s.canSubmit) return
        launch {
            updateState { it.copy(submitting = true, errorMessage = null) }
            val result = when (s.mode) {
                AuthMode.SignIn -> signInWithEmail(s.email, s.password)
                AuthMode.SignUp -> signUpWithEmail(s.email, s.password, s.displayName)
            }
            updateState { it.copy(submitting = false) }
            handle(result)
        }
    }

    fun onGoogleClicked() {
        if (currentState.submitting || currentState.socialInFlight) return
        launch {
            updateState { it.copy(socialInFlight = true, errorMessage = null) }
            val result = signInWithGoogle()
            updateState { it.copy(socialInFlight = false) }
            handle(result)
        }
    }

    fun onAppleClicked() {
        if (currentState.submitting || currentState.socialInFlight) return
        launch {
            updateState { it.copy(socialInFlight = true, errorMessage = null) }
            val result = signInWithApple()
            updateState { it.copy(socialInFlight = false) }
            handle(result)
        }
    }

    private fun handle(result: Result<*>) = when (result) {
        is Result.Success<*> -> emitEffect(AuthEffect.Authenticated)
        is Result.Failure -> updateState { it.copy(errorMessage = result.error.message) }
    }
}
