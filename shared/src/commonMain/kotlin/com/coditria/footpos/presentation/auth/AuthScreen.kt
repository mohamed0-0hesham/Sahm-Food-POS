package com.coditria.footpos.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.components.PrimaryButton
import com.coditria.footpos.core.designsystem.spacing
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import org.jetbrains.compose.resources.painterResource
import sahmfood.shared.generated.resources.Res
import sahmfood.shared.generated.resources.google_icon
import com.coditria.footpos.core.designsystem.typography
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    vm: AuthViewModel = koinViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = PosTheme.colors
    val spacing = PosTheme.spacing

    LaunchedEffect(vm) {
        vm.effects.collectLatest { effect ->
            when (effect) {
                AuthEffect.Authenticated -> onAuthenticated()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundPrimary)
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(horizontal = spacing.xl, vertical = spacing.xxl)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        Spacer(Modifier.height(spacing.xxxl))
        Text("Sahm Food POS", style = PosTheme.typography.largeTitle, color = colors.labelPrimary)
        Text(
            if (state.mode == AuthMode.SignIn) "Sign in to continue" else "Create your account",
            style = PosTheme.typography.body,
            color = colors.labelSecondary,
        )

        Spacer(Modifier.height(spacing.lg))

        ModeToggle(mode = state.mode, onModeChange = vm::onModeChanged)

        if (state.mode == AuthMode.SignUp) {
            AuthTextField(
                value = state.displayName,
                onValueChange = vm::onDisplayNameChanged,
                placeholder = "Display name (optional)",
                keyboard = KeyboardType.Text,
            )
        }

        AuthTextField(
            value = state.email,
            onValueChange = vm::onEmailChanged,
            placeholder = "Email",
            keyboard = KeyboardType.Email,
        )

        AuthTextField(
            value = state.password,
            onValueChange = vm::onPasswordChanged,
            placeholder = "Password",
            keyboard = KeyboardType.Password,
            visualTransformation = PasswordVisualTransformation(),
        )

        state.errorMessage?.let { msg ->
            Text(
                msg,
                style = PosTheme.typography.footnote,
                color = colors.destructive,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        PrimaryButton(
            text = if (state.mode == AuthMode.SignIn) "Sign In" else "Sign Up",
            onClick = vm::onSubmit,
            enabled = state.canSubmit,
            loading = state.submitting,
            modifier = Modifier.fillMaxWidth(),
        )

        if (state.googleAvailable || state.appleAvailable) {
            DividerWithLabel("or continue with")

            if (state.googleAvailable) {
                SocialButton(
                    label = "Continue with Google",
                    background = Color.White,
                    contentColor = Color(0xFF1F1F1F),
                    borderColor = colors.separator,
                    loading = state.socialInFlight,
                    onClick = vm::onGoogleClicked,
                    icon = {
                        Image(
                            painter = painterResource(Res.drawable.google_icon),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                )
            }

            if (state.appleAvailable) {
                SocialButton(
                    label = "Continue with Apple",
                    background = Color.Black,
                    contentColor = Color.White,
                    borderColor = Color.Black,
                    loading = state.socialInFlight,
                    onClick = vm::onAppleClicked,
                )
            }
        }
    }
}

@Composable
private fun ModeToggle(mode: AuthMode, onModeChange: (AuthMode) -> Unit) {
    val colors = PosTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.backgroundSecondary)
            .padding(4.dp),
    ) {
        ModeTab("Sign In", selected = mode == AuthMode.SignIn) { onModeChange(AuthMode.SignIn) }
        ModeTab("Sign Up", selected = mode == AuthMode.SignUp) { onModeChange(AuthMode.SignUp) }
    }
}

@Composable
private fun RowScope.ModeTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = PosTheme.colors
    val bg = if (selected) colors.backgroundPrimary else Color.Transparent
    val fg = if (selected) colors.labelPrimary else colors.labelSecondary
    Box(
        Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = PosTheme.typography.headline, color = fg)
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboard: KeyboardType,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val colors = PosTheme.colors
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.backgroundSecondary)
            .padding(horizontal = 14.dp, vertical = 14.dp),
    ) {
        if (value.isEmpty()) {
            Text(placeholder, style = PosTheme.typography.body, color = colors.labelTertiary)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = keyboard),
            visualTransformation = visualTransformation,
            cursorBrush = SolidColor(colors.accent),
            singleLine = true,
            textStyle = LocalTextStyle.current.merge(
                PosTheme.typography.body.copy(color = colors.labelPrimary)
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DividerWithLabel(text: String) {
    val colors = PosTheme.colors
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f).height(1.dp).background(colors.separator))
        Text(
            text,
            style = PosTheme.typography.footnote,
            color = colors.labelTertiary,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        Box(Modifier.weight(1f).height(1.dp).background(colors.separator))
    }
}

/**
 * [icon] is a composable slot rather than a Painter / glyph string so each provider
 * can supply whatever fits — Google ships a multi-colour webp, Apple may swap in an
 * SF Symbol or vector later — without growing this signature again.
 */
@Composable
private fun SocialButton(
    label: String,
    background: Color,
    contentColor: Color,
    borderColor: Color,
    loading: Boolean,
    onClick: () -> Unit,
    icon: (@Composable () -> Unit)? = null,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .clickable(enabled = !loading) { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                icon()
                Spacer(Modifier.width(10.dp))
            }
            Text(label, style = PosTheme.typography.headline, color = contentColor)
        }
    }
}
