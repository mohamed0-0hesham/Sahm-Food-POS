package com.coditria.footpos.presentation.checkout

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.components.PrimaryButton
import com.coditria.footpos.core.designsystem.components.TertiaryButton
import com.coditria.footpos.core.designsystem.typography
import com.coditria.footpos.domain.model.PaymentMethod
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CheckoutSheet(vm: CheckoutViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = PosTheme.colors

    Column(
        Modifier.fillMaxWidth().background(colors.backgroundPrimary).padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Confirm Order", style = PosTheme.typography.title1, color = colors.labelPrimary, modifier = Modifier.weight(1f))
            Text("✕", style = PosTheme.typography.title2, color = colors.labelSecondary,
                modifier = Modifier.clickable { vm.onCancel() }.padding(8.dp))
        }
        Spacer(Modifier.size(16.dp))
        SummaryCard(state)
        Spacer(Modifier.size(20.dp))
        Text("Payment Method", style = PosTheme.typography.headline, color = colors.labelPrimary)
        Spacer(Modifier.size(8.dp))
        SegmentedControl(
            selected = state.paymentMethod,
            onSelect = vm::onPaymentMethodChanged,
        )
        Spacer(Modifier.size(16.dp))
        when (state.paymentMethod) {
            PaymentMethod.CASH -> CashPaymentInputs(state, onAmountChanged = vm::onAmountChanged)
            PaymentMethod.CARD -> CardPaymentMockUI()
            PaymentMethod.OTHER -> OtherPaymentMockUI()
        }
        state.error?.let {
            Spacer(Modifier.size(8.dp))
            Text(it, style = PosTheme.typography.footnote, color = colors.destructive)
        }
        Spacer(Modifier.size(20.dp))
        PrimaryButton(
            text = if (state.processing) "Processing…" else "Complete & Print Receipt",
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
            .clip(RoundedCornerShape(12.dp))
            .background(colors.backgroundSecondary)
            .padding(16.dp),
    ) {
        Text("${state.cart.items.sumOf { it.quantity }} items", style = PosTheme.typography.subhead, color = colors.labelSecondary)
        Spacer(Modifier.size(8.dp))
        SummaryLine("Subtotal", state.cart.subtotal.format())
        SummaryLine("Tax", state.cart.taxAmount.format())
        if (!state.cart.discount.amount.isZero()) {
            SummaryLine("Discount", "-" + state.cart.discount.amount.formatAmount())
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.separator).padding(vertical = 6.dp))
        Spacer(Modifier.size(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Total", style = PosTheme.typography.title2, color = colors.labelPrimary, modifier = Modifier.weight(1f))
            Text(state.total.format(), style = PosTheme.typography.title1, color = colors.labelPrimary)
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    val colors = PosTheme.colors
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label, style = PosTheme.typography.body, color = colors.labelSecondary, modifier = Modifier.weight(1f))
        Text(value, style = PosTheme.typography.body, color = colors.labelPrimary)
    }
}

@Composable
private fun SegmentedControl(selected: PaymentMethod, onSelect: (PaymentMethod) -> Unit) {
    val colors = PosTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
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
    val bg = if (selected) colors.backgroundTertiary else androidx.compose.ui.graphics.Color.Transparent
    val fg = if (selected) colors.labelPrimary else colors.labelSecondary
    Box(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = PosTheme.typography.subhead, color = fg)
    }
}

@Composable
private fun CashPaymentInputs(state: CheckoutState, onAmountChanged: (Long) -> Unit) {
    val colors = PosTheme.colors
    var text by remember { mutableStateOf("") }
    Text("Amount Received", style = PosTheme.typography.headline, color = colors.labelPrimary)
    Spacer(Modifier.size(8.dp))
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.backgroundSecondary)
            .padding(14.dp),
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
    Spacer(Modifier.size(12.dp))
    Row(Modifier.fillMaxWidth()) {
        Text("Change Due", style = PosTheme.typography.body, color = colors.labelSecondary, modifier = Modifier.weight(1f))
        Text(state.change.format(), style = PosTheme.typography.title2, color = colors.success)
    }
}

@Composable
private fun CardPaymentMockUI() {
    val colors = PosTheme.colors
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.backgroundSecondary)
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("Card terminal ready", style = PosTheme.typography.body, color = colors.labelSecondary)
    }
}

@Composable
private fun OtherPaymentMockUI() {
    val colors = PosTheme.colors
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.backgroundSecondary)
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("Recorded as 'Other' — note will be saved with the order", style = PosTheme.typography.body, color = colors.labelSecondary)
    }
}
