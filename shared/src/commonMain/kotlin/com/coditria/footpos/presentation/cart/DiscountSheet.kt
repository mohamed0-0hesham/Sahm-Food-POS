package com.coditria.footpos.presentation.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.components.PrimaryButton
import com.coditria.footpos.core.designsystem.components.TertiaryButton
import com.coditria.footpos.core.designsystem.shapes
import com.coditria.footpos.core.designsystem.typography
import com.coditria.footpos.domain.model.Money
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DiscountSheet(
    onDismiss: () -> Unit,
    vm: CartViewModel = koinViewModel(),
) {
    val colors = PosTheme.colors
    var amount by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.backgroundPrimary)
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
    ) {
        DragHandle()
        Text("Apply a discount", style = PosTheme.typography.title2, color = colors.labelPrimary)
        Spacer(Modifier.size(6.dp))
        Text(
            "Subtracted from the order before payment.",
            style = PosTheme.typography.body,
            color = colors.labelSecondary,
        )
        Spacer(Modifier.size(20.dp))
        LabeledField(
            label = "Amount",
            value = amount,
            onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
            keyboard = KeyboardType.Decimal,
            placeholder = "0.00",
        )
        Spacer(Modifier.size(14.dp))
        LabeledField(
            label = "Reason (optional)",
            value = reason,
            onValueChange = { reason = it },
            keyboard = KeyboardType.Text,
            placeholder = "e.g. Staff discount",
        )
        Spacer(Modifier.size(20.dp))
        PrimaryButton(
            text = "Apply discount",
            onClick = {
                val cents = (amount.toDoubleOrNull() ?: 0.0).times(100).toLong()
                vm.onApplyDiscount(Money(cents), reason.ifBlank { null })
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.size(8.dp))
        TertiaryButton(text = "Cancel", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboard: KeyboardType,
    placeholder: String,
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
                .padding(14.dp),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                keyboardOptions = KeyboardOptions(keyboardType = keyboard),
                cursorBrush = SolidColor(colors.accent),
                textStyle = PosTheme.typography.body.copy(color = colors.labelPrimary),
                singleLine = true,
            )
            if (value.isEmpty()) {
                Text(placeholder, style = PosTheme.typography.body, color = colors.labelTertiary)
            }
        }
    }
}

@Composable
private fun DragHandle() {
    val colors = PosTheme.colors
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
}
