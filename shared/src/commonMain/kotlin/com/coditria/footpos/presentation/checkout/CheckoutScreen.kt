package com.coditria.footpos.presentation.checkout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.coditria.footpos.core.designsystem.PosMotion
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.components.IconChip
import com.coditria.footpos.core.designsystem.components.PrimaryButton
import com.coditria.footpos.core.designsystem.components.SectionLabel
import com.coditria.footpos.core.designsystem.components.TertiaryButton
import com.coditria.footpos.core.designsystem.shapes
import com.coditria.footpos.core.designsystem.typography
import com.coditria.footpos.domain.model.PaymentMethod
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CheckoutSheet(vm: CheckoutViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = PosTheme.colors

    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.backgroundPrimary)
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
    ) {
        // Drag handle + close button mirror the iOS-style sheet pattern.
        Box(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .clip(PosTheme.shapes.pill)
                    .background(colors.separator)
                    .size(width = 40.dp, height = 4.dp),
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Checkout", style = PosTheme.typography.title1, color = colors.labelPrimary)
                Text("Confirm and collect payment", style = PosTheme.typography.subhead, color = colors.labelSecondary)
            }
            IconChip(glyph = "✕", onClick = vm::onCancel)
        }
        Spacer(Modifier.size(20.dp))

        SummaryCard(state)
        Spacer(Modifier.size(20.dp))

        SectionLabel("Payment method")
        Spacer(Modifier.size(10.dp))
        SegmentedControl(selected = state.paymentMethod, onSelect = vm::onPaymentMethodChanged)

        Spacer(Modifier.size(16.dp))

        // Animated panel swap so the cashier feels the input area "morph" rather
        // than jump when they switch payment methods.
        AnimatedContent(
            targetState = state.paymentMethod,
            transitionSpec = {
                fadeIn(PosMotion.tweenStandard()) togetherWith fadeOut(PosMotion.tweenFast())
            },
            label = "payment-panel",
        ) { method ->
            when (method) {
                PaymentMethod.CASH -> CashPaymentInputs(state, onAmountChanged = vm::onAmountChanged)
                PaymentMethod.CARD -> ReadyPanel(message = "Card terminal ready")
                PaymentMethod.OTHER -> ReadyPanel(message = "Recorded as 'Other' — note will be saved with the order")
            }
        }

        state.error?.let {
            Spacer(Modifier.size(12.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(PosTheme.shapes.md)
                    .background(colors.destructive.copy(alpha = 0.10f))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Text(it, style = PosTheme.typography.footnote, color = colors.destructive)
            }
        }
        Spacer(Modifier.size(20.dp))
        PrimaryButton(
            text = if (state.processing) "Processing…" else "Complete & print receipt",
            onClick = vm::onComplete,
            enabled = state.canComplete,
            loading = state.processing,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.size(8.dp))
        TertiaryButton(text = "Cancel", onClick = vm::onCancel, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun SummaryCard(state: CheckoutState) {
    val colors = PosTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .clip(PosTheme.shapes.lg)
            .background(colors.backgroundSecondary)
            .padding(18.dp),
    ) {
        Text(
            "${state.cart.items.sumOf { it.quantity }} ITEMS",
            style = PosTheme.typography.label,
            color = colors.labelTertiary,
        )
        Spacer(Modifier.size(10.dp))
        SummaryLine("Subtotal", state.cart.subtotal.format())
        Spacer(Modifier.size(4.dp))
        SummaryLine("Tax", state.cart.taxAmount.format())
        if (!state.cart.discount.amount.isZero()) {
            Spacer(Modifier.size(4.dp))
            SummaryLine("Discount", "-" + state.cart.discount.amount.formatAmount(), valueColor = colors.success)
        }
        Spacer(Modifier.size(12.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.separator))
        Spacer(Modifier.size(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Total due", style = PosTheme.typography.title3, color = colors.labelPrimary, modifier = Modifier.weight(1f))
            Text(state.total.format(), style = PosTheme.typography.title1, color = colors.accent)
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String, valueColor: Color? = null) {
    val colors = PosTheme.colors
    Row(Modifier.fillMaxWidth()) {
        Text(label, style = PosTheme.typography.body, color = colors.labelSecondary, modifier = Modifier.weight(1f))
        Text(value, style = PosTheme.typography.body, color = valueColor ?: colors.labelPrimary)
    }
}

@Composable
private fun SegmentedControl(selected: PaymentMethod, onSelect: (PaymentMethod) -> Unit) {
    val colors = PosTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(PosTheme.shapes.md)
            .background(colors.backgroundSecondary)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        PaymentMethod.entries.forEach { method ->
            Segment(
                label = method.displayName(),
                selected = method == selected,
                onClick = { onSelect(method) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private fun PaymentMethod.displayName(): String = when (this) {
    PaymentMethod.CASH -> "Cash"
    PaymentMethod.CARD -> "Card"
    PaymentMethod.OTHER -> "Other"
}

@Composable
private fun Segment(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val colors = PosTheme.colors
    val bg by animateColorAsState(
        targetValue = if (selected) colors.backgroundPrimary else Color.Transparent,
        animationSpec = PosMotion.tweenStandard(),
        label = "seg-bg",
    )
    val fg by animateColorAsState(
        targetValue = if (selected) colors.labelPrimary else colors.labelSecondary,
        animationSpec = PosMotion.tweenStandard(),
        label = "seg-fg",
    )
    Box(
        modifier
            .clip(PosTheme.shapes.sm)
            .background(bg)
            .clickable { onClick() }
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = PosTheme.typography.subhead, color = fg)
    }
}

@Composable
private fun CashPaymentInputs(state: CheckoutState, onAmountChanged: (Long) -> Unit) {
    val colors = PosTheme.colors
    var text by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Amount received", style = PosTheme.typography.caption1, color = colors.labelSecondary)
        Box(
            Modifier
                .fillMaxWidth()
                .clip(PosTheme.shapes.md)
                .background(colors.backgroundPrimary)
                .border(width = 1.dp, color = colors.separatorStrong, shape = PosTheme.shapes.md)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            BasicTextField(
                value = text,
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() || it == '.' }
                    text = digits
                    val cents = (digits.toDoubleOrNull() ?: 0.0).times(100).toLong()
                    onAmountChanged(cents)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                cursorBrush = SolidColor(colors.accent),
                textStyle = PosTheme.typography.title2.copy(color = colors.labelPrimary),
                singleLine = true,
            )
            if (text.isEmpty()) {
                Text("0.00", style = PosTheme.typography.title2, color = colors.labelTertiary)
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .clip(PosTheme.shapes.md)
                .background(colors.success.copy(alpha = 0.10f))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Change due", style = PosTheme.typography.body, color = colors.labelSecondary, modifier = Modifier.weight(1f))
            Text(state.change.format(), style = PosTheme.typography.title3, color = colors.success)
        }
    }
}

@Composable
private fun ReadyPanel(message: String) {
    val colors = PosTheme.colors
    Box(
        Modifier
            .fillMaxWidth()
            .clip(PosTheme.shapes.md)
            .background(colors.backgroundSecondary)
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(message, style = PosTheme.typography.body, color = colors.labelSecondary)
    }
}
