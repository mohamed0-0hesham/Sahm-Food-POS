package com.coditria.footpos.presentation.settings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.typography
import com.coditria.footpos.domain.model.Currency
import com.coditria.footpos.domain.model.TaxRate
import org.koin.compose.viewmodel.koinViewModel

/** Which inline editor (if any) is currently open. */
private sealed interface SettingsEditor {
    object StoreName : SettingsEditor
    object TaxRate : SettingsEditor
    object Currency : SettingsEditor
}

@Composable
fun SettingsScreen(
    onOpenAbout: () -> Unit,
    vm: SettingsViewModel = koinViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = PosTheme.colors
    var editor by remember { mutableStateOf<SettingsEditor?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundPrimary)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(20.dp)),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text("Settings", style = PosTheme.typography.largeTitle, color = colors.labelPrimary)

        Section(title = "STORE") {
            SettingsRow(
                title = "Store Name",
                value = state.settings.storeName,
                onClick = { editor = SettingsEditor.StoreName },
            )
            Divider()
            SettingsRow(
                title = "Tax Rate",
                value = state.settings.taxRate.toPercentLabel(),
                onClick = { editor = SettingsEditor.TaxRate },
            )
            Divider()
            SettingsRow(
                title = "Currency",
                value = state.settings.currency.code,
                onClick = { editor = SettingsEditor.Currency },
            )
        }

        Section(title = "HARDWARE") {
            SettingsRow(title = "Printer", value = "Mock Printer (Demo)")
            Divider()
            ToggleRow(
                title = "Auto-print receipts",
                checked = state.settings.autoPrintReceipts,
                onCheckedChange = vm::onAutoPrintChanged,
            )
        }

        Section(title = "SYNC") {
            SettingsRow(title = "Status", value = "Background")
        }

        Section(title = "ABOUT") {
            SettingsRow(title = "Version", value = "1.0.0")
            Divider()
            SettingsRow(title = "About Sahm POS", value = "›", onClick = onOpenAbout)
        }

        Spacer(Modifier.height(8.dp))
    }

    when (editor) {
        SettingsEditor.StoreName -> TextEditDialog(
            title = "Store Name",
            initial = state.settings.storeName,
            keyboard = KeyboardType.Text,
            validate = { it.isNotBlank() },
            onConfirm = { vm.onStoreNameChanged(it); editor = null },
            onDismiss = { editor = null },
        )
        SettingsEditor.TaxRate -> TaxRateEditDialog(
            initial = state.settings.taxRate,
            onConfirm = { vm.onTaxRateChanged(it); editor = null },
            onDismiss = { editor = null },
        )
        SettingsEditor.Currency -> CurrencyPickerDialog(
            selected = state.settings.currency,
            onSelect = { vm.onCurrencyChanged(it); editor = null },
            onDismiss = { editor = null },
        )
        null -> Unit
    }
}

private fun TaxRate.toPercentLabel(): String {
    val pct = value * 100
    val asInt = pct.toInt()
    return if (pct == asInt.toDouble()) "$asInt%" else "${(pct * 10).toInt() / 10.0}%"
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            title,
            style = PosTheme.typography.caption1,
            color = PosTheme.colors.labelTertiary,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PosTheme.colors.backgroundSecondary),
        ) { content() }
    }
}

@Composable
private fun SettingsRow(title: String, value: String, onClick: (() -> Unit)? = null) {
    val colors = PosTheme.colors
    val clickModifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickModifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = PosTheme.typography.body, color = colors.labelPrimary, modifier = Modifier.weight(1f))
        Text(value, style = PosTheme.typography.body, color = colors.labelSecondary)
    }
}

@Composable
private fun ToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val colors = PosTheme.colors
    Row(
        Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = PosTheme.typography.body, color = colors.labelPrimary, modifier = Modifier.weight(1f))
        Box(
            Modifier
                .size(width = 44.dp, height = 26.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(if (checked) colors.success else colors.separator),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Box(
                Modifier
                    .padding(2.dp)
                    .size(22.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Color.White),
            )
        }
    }
}

@Composable
private fun Divider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(PosTheme.colors.separator))
}

// ---------- Editors -----------------------------------------------------------

@Composable
private fun TextEditDialog(
    title: String,
    initial: String,
    keyboard: KeyboardType,
    validate: (String) -> Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = PosTheme.typography.title3) },
        text = {
            EditField(value = text, onValueChange = { text = it }, keyboard = keyboard)
        },
        confirmButton = {
            TextButton(enabled = validate(text), onClick = { onConfirm(text) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun TaxRateEditDialog(
    initial: TaxRate,
    onConfirm: (TaxRate) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf((initial.value * 100).let { if (it == it.toInt().toDouble()) it.toInt().toString() else it.toString() }) }
    val parsed: Double? = text.toDoubleOrNull()
    val valid = parsed != null && parsed in 0.0..100.0
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tax Rate (%)", style = PosTheme.typography.title3) },
        text = {
            EditField(value = text, onValueChange = { text = it.filter { ch -> ch.isDigit() || ch == '.' } }, keyboard = KeyboardType.Decimal)
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = { onConfirm(TaxRate((parsed!! / 100).coerceIn(0.0, 1.0))) },
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun CurrencyPickerDialog(
    selected: Currency,
    onSelect: (Currency) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = PosTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Currency", style = PosTheme.typography.title3) },
        text = {
            Column {
                Currency.entries.forEach { c ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(c) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(c.code, style = PosTheme.typography.body, color = colors.labelPrimary, modifier = Modifier.weight(1f))
                        if (c == selected) {
                            Text("✓", style = PosTheme.typography.body, color = colors.accent)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
    )
}

@Composable
private fun EditField(value: String, onValueChange: (String) -> Unit, keyboard: KeyboardType) {
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
            keyboardOptions = KeyboardOptions(keyboardType = keyboard),
            cursorBrush = SolidColor(colors.accent),
            textStyle = PosTheme.typography.body.copy(color = colors.labelPrimary),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// AboutScreen is part of the Settings stack but stays unchanged.
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val colors = PosTheme.colors
    Column(Modifier.fillMaxSize().background(colors.backgroundPrimary)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "‹",
                style = PosTheme.typography.title2,
                color = colors.accent,
                modifier = Modifier.clickable { onBack() }.padding(end = 8.dp),
            )
            Text("About", style = PosTheme.typography.headline, color = colors.labelPrimary)
        }
        Column(
            Modifier.fillMaxSize().padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Sahm Food POS", style = PosTheme.typography.title1, color = colors.labelPrimary)
            Spacer(Modifier.size(8.dp))
            Text("Version 1.0.0", style = PosTheme.typography.body, color = colors.labelSecondary)
            Spacer(Modifier.size(20.dp))
            Text("Built with Kotlin Multiplatform", style = PosTheme.typography.body, color = colors.labelSecondary)
            Text("© 2026 Sahm Food", style = PosTheme.typography.footnote, color = colors.labelTertiary)
        }
    }
}
