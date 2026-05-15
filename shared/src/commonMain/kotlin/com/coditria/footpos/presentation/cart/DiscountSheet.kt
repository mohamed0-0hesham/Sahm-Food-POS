package com.coditria.footpos.presentation.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.components.PrimaryButton
import com.coditria.footpos.core.designsystem.components.TertiaryButton
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

    Column(Modifier.fillMaxWidth().background(colors.backgroundPrimary).padding(20.dp)) {
        Text("Apply Discount", style = PosTheme.typography.title2, color = colors.labelPrimary)
        Spacer(Modifier.size(16.dp))
        Text("Amount", style = PosTheme.typography.headline, color = colors.labelPrimary)
        Spacer(Modifier.size(8.dp))
        InputBox(value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } }, keyboardType = KeyboardType.Decimal, placeholder = "0.00")
        Spacer(Modifier.size(16.dp))
        Text("Reason (optional)", style = PosTheme.typography.headline, color = colors.labelPrimary)
        Spacer(Modifier.size(8.dp))
        InputBox(value = reason, onValueChange = { reason = it }, keyboardType = KeyboardType.Text, placeholder = "e.g. Staff discount")
        Spacer(Modifier.size(20.dp))
        PrimaryButton(
            text = "Apply",
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
private fun InputBox(value: String, onValueChange: (String) -> Unit, keyboardType: KeyboardType, placeholder: String) {
    val colors = PosTheme.colors
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.backgroundSecondary)
            .padding(14.dp),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            cursorBrush = SolidColor(colors.accent),
            textStyle = PosTheme.typography.body.copy(color = colors.labelPrimary),
            singleLine = true,
        )
        if (value.isEmpty()) {
            Text(placeholder, style = PosTheme.typography.body, color = colors.labelTertiary)
        }
    }
}
