package com.coditria.footpos.presentation.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
    var passwordVisible by remember { mutableStateOf(false) }

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
            .warmGradientCorner(accent = colors.accent)
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(horizontal = spacing.xl, vertical = spacing.lg)),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        BrandHeader()
        Spacer(Modifier.height(spacing.xl))

        // Headline crossfades when the mode flips so the transition reads as one
        // animated experience rather than two separate screens.
        AnimatedContent(
            targetState = state.mode,
            transitionSpec = {
                fadeIn(PosMotion.tweenStandard()) togetherWith fadeOut(PosMotion.tweenFast())
            },
            label = "auth-headline",
        ) { mode ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    if (mode == AuthMode.SignIn) "Welcome back" else "Create account",
                    style = PosTheme.typography.display,
                    color = colors.labelPrimary,
                )
                Text(
                    if (mode == AuthMode.SignIn)
                        "Sign in to continue to your store."
                    else
                        "Sign up to start ringing up orders.",
                    style = PosTheme.typography.body,
                    color = colors.labelSecondary,
                )
            }
        }

        Spacer(Modifier.height(spacing.md))

        AnimatedVisibility(
            visible = state.mode == AuthMode.SignUp,
            enter = fadeIn(PosMotion.tweenStandard()) + expandVertically(PosMotion.tweenStandard()),
            exit = fadeOut(PosMotion.tweenFast()) + shrinkVertically(PosMotion.tweenFast()),
        ) {
            Column {
                FilledField(
                    label = "Display name",
                    value = state.displayName,
                    onValueChange = vm::onDisplayNameChanged,
                    placeholder = "Your name",
                    keyboard = KeyboardType.Text,
                )
                Spacer(Modifier.height(spacing.md))
            }
        }

        FilledField(
            label = "Email",
            value = state.email,
            onValueChange = vm::onEmailChanged,
            placeholder = "manager@sahm.food",
            keyboard = KeyboardType.Email,
        )

        FilledField(
            label = "Password",
            value = state.password,
            onValueChange = vm::onPasswordChanged,
            placeholder = "••••••••",
            keyboard = KeyboardType.Password,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailing = {
                Text(
                    if (passwordVisible) "Hide" else "Show",
                    style = PosTheme.typography.subhead,
                    color = colors.labelSecondary,
                    modifier = Modifier
                        .clickable { passwordVisible = !passwordVisible }
                        .padding(start = 8.dp),
                )
            },
            // "Forgot?" rides at the top-right of the label row, paired with the field
            // header — common modern eCommerce pattern, less noisy than below the field.
            headerTrailing = if (state.mode == AuthMode.SignIn) {
                {
                    Text(
                        "Forgot?",
                        style = PosTheme.typography.subhead,
                        color = colors.labelSecondary,
                        modifier = Modifier.clickable { /* not implemented yet */ },
                    )
                }
            } else null,
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
            text = if (state.mode == AuthMode.SignIn) "Sign in" else "Create account",
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

        Spacer(Modifier.height(spacing.lg))
        FooterModeSwitch(
            mode = state.mode,
            onSwitch = {
                vm.onModeChanged(if (state.mode == AuthMode.SignIn) AuthMode.SignUp else AuthMode.SignIn)
            },
        )
        Spacer(Modifier.height(spacing.lg))
    }
}

@Composable
private fun BrandHeader() {
    val colors = PosTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(PosTheme.shapes.md)
                .background(colors.accent),
            contentAlignment = Alignment.Center,
        ) {
            Text("S", style = PosTheme.typography.title3, color = colors.onAccent)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Sahm Food", style = PosTheme.typography.headline, color = colors.labelPrimary)
            Text("POINT OF SALE", style = PosTheme.typography.label, color = colors.labelTertiary)
        }
    }
}

@Composable
private fun FooterModeSwitch(mode: AuthMode, onSwitch: () -> Unit) {
    val colors = PosTheme.colors
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            if (mode == AuthMode.SignIn) "Don't have an account?" else "Already have an account?",
            style = PosTheme.typography.body,
            color = colors.labelSecondary,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            if (mode == AuthMode.SignIn) "Sign up" else "Sign in",
            style = PosTheme.typography.headline,
            color = colors.accent,
            modifier = Modifier.clickable(onClick = onSwitch),
        )
    }
}

@Composable
private fun FilledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboard: KeyboardType,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null,
    headerTrailing: (@Composable () -> Unit)? = null,
) {
    val colors = PosTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = PosTheme.typography.caption1, color = colors.labelSecondary, modifier = Modifier.weight(1f))
            if (headerTrailing != null) headerTrailing()
        }
        Row(
            Modifier
                .fillMaxWidth()
                .clip(PosTheme.shapes.md)
                .background(colors.backgroundSecondary)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.weight(1f)) {
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
            if (trailing != null) trailing()
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

/**
 * Soft radial wash of the accent in the top-right corner. Matches splash so the
 * launch → auth handoff feels continuous, and gives the otherwise white screen
 * warmth without painting all the chrome.
 */
private fun Modifier.warmGradientCorner(accent: Color): Modifier = this.drawWithCache {
    val brush = Brush.radialGradient(
        colors = listOf(accent.copy(alpha = 0.16f), Color.Transparent),
        center = Offset(size.width * 0.95f, size.height * 0.08f),
        radius = size.minDimension * 0.75f,
    )
    onDrawBehind { drawRect(brush) }
}
