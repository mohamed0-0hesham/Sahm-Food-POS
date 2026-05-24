package com.coditria.footpos.presentation.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.coditria.footpos.core.designsystem.PosMotion
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.components.PrimaryButton
import com.coditria.footpos.core.designsystem.shapes
import com.coditria.footpos.core.designsystem.spacing
import com.coditria.footpos.core.designsystem.typography
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import sahmfood.shared.generated.resources.Res
import sahmfood.shared.generated.resources.google_icon

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
            .padding(PaddingValues(horizontal = spacing.xl, vertical = spacing.lg)),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Spacer(Modifier.height(spacing.xxxl))

        // Brand tile — mirrors the splash monogram so the launch → auth flow feels
        // continuous. Static here (no rotation) to keep focus on the form.
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(PosTheme.shapes.md)
                .background(colors.accent),
            contentAlignment = Alignment.Center,
        ) {
            Text("S", style = PosTheme.typography.title2, color = colors.onAccent)
        }

        Spacer(Modifier.height(spacing.lg))

        // Headline tracks the current mode; AnimatedContent gives a quick crossfade
        // so the transition reads as one app, not two screens.
        AnimatedContent(
            targetState = state.mode,
            transitionSpec = {
                (fadeIn(PosMotion.tweenStandard()) togetherWith fadeOut(PosMotion.tweenFast()))
            },
            label = "auth-headline",
        ) { mode ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    if (mode == AuthMode.SignIn) "Welcome back" else "Create your account",
                    style = PosTheme.typography.largeTitle,
                    color = colors.labelPrimary,
                )
                Text(
                    if (mode == AuthMode.SignIn)
                        "Sign in to continue taking orders."
                    else
                        "Set up an account to start running shifts.",
                    style = PosTheme.typography.body,
                    color = colors.labelSecondary,
                )
            }
        }

        Spacer(Modifier.height(spacing.md))

        ModeToggle(mode = state.mode, onModeChange = vm::onModeChanged)

        // Display name slot animates in/out cleanly when toggling Sign Up.
        AnimatedVisibility(
            visible = state.mode == AuthMode.SignUp,
            enter = fadeIn(PosMotion.tweenStandard()) + expandVertically(PosMotion.tweenStandard()),
            exit = fadeOut(PosMotion.tweenFast()) + shrinkVertically(PosMotion.tweenFast()),
        ) {
            Column {
                AuthTextField(
                    value = state.displayName,
                    onValueChange = vm::onDisplayNameChanged,
                    placeholder = "Display name",
                    label = "Name",
                    keyboard = KeyboardType.Text,
                )
                Spacer(Modifier.height(spacing.md))
            }
        }

        AuthTextField(
            value = state.email,
            onValueChange = vm::onEmailChanged,
            placeholder = "you@store.com",
            label = "Email",
            keyboard = KeyboardType.Email,
        )

        AuthTextField(
            value = state.password,
            onValueChange = vm::onPasswordChanged,
            placeholder = "••••••••",
            label = "Password",
            keyboard = KeyboardType.Password,
            visualTransformation = PasswordVisualTransformation(),
        )

        AnimatedVisibility(
            visible = state.errorMessage != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            ErrorPill(message = state.errorMessage.orEmpty())
        }

        Spacer(Modifier.height(spacing.xs))

        PrimaryButton(
            text = if (state.mode == AuthMode.SignIn) "Sign In" else "Create Account",
            onClick = vm::onSubmit,
            enabled = state.canSubmit,
            loading = state.submitting,
            modifier = Modifier.fillMaxWidth(),
        )

        if (state.googleAvailable || state.appleAvailable) {
            Spacer(Modifier.height(spacing.xs))
            DividerWithLabel("or continue with")

            if (state.googleAvailable) {
                SocialButton(
                    label = "Continue with Google",
                    background = colors.backgroundPrimary,
                    contentColor = colors.labelPrimary,
                    borderColor = colors.separatorStrong,
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
                    background = colors.labelPrimary,
                    contentColor = colors.labelInverted,
                    borderColor = colors.labelPrimary,
                    loading = state.socialInFlight,
                    onClick = vm::onAppleClicked,
                    icon = {
                        Text(
                            "",
                            style = PosTheme.typography.title3,
                            color = colors.labelInverted,
                        )
                    },
                )
            }
        }

        Spacer(Modifier.height(spacing.xl))
    }
}

@Composable
private fun ModeToggle(mode: AuthMode, onModeChange: (AuthMode) -> Unit) {
    val colors = PosTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(PosTheme.shapes.md)
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
    val bg by animateColorAsState(
        targetValue = if (selected) colors.backgroundPrimary else Color.Transparent,
        animationSpec = PosMotion.tweenStandard(),
        label = "tab-bg",
    )
    val fg by animateColorAsState(
        targetValue = if (selected) colors.labelPrimary else colors.labelSecondary,
        animationSpec = PosMotion.tweenStandard(),
        label = "tab-fg",
    )
    Box(
        Modifier
            .weight(1f)
            .clip(PosTheme.shapes.sm)
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
    label: String,
    keyboard: KeyboardType,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val colors = PosTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = PosTheme.typography.caption1, color = colors.labelSecondary)
        Box(
            Modifier
                .fillMaxWidth()
                .clip(PosTheme.shapes.md)
                .background(colors.backgroundPrimary)
                .border(width = 1.dp, color = colors.separatorStrong, shape = PosTheme.shapes.md)
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
}

@Composable
private fun ErrorPill(message: String) {
    val colors = PosTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(PosTheme.shapes.md)
            .background(colors.destructive.copy(alpha = 0.10f))
            .border(
                width = 1.dp,
                color = colors.destructive.copy(alpha = 0.30f),
                shape = PosTheme.shapes.md,
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(message, style = PosTheme.typography.footnote, color = colors.destructive)
    }
}

@Composable
private fun DividerWithLabel(text: String) {
    val colors = PosTheme.colors
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
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
            .clip(PosTheme.shapes.md)
            .background(background)
            .border(width = 1.dp, color = borderColor, shape = PosTheme.shapes.md)
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
