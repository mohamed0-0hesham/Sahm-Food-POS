package com.coditria.footpos.presentation.settings

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.animateColorAsState
import com.coditria.footpos.core.designsystem.PosMotion
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.components.PosCard
import com.coditria.footpos.core.designsystem.components.PosTopBar
import com.coditria.footpos.core.designsystem.components.SectionLabel
import com.coditria.footpos.core.designsystem.shapes
import com.coditria.footpos.core.designsystem.typography
import com.coditria.footpos.domain.model.Currency
import com.coditria.footpos.domain.model.TaxRate
import org.koin.compose.viewmodel.koinViewModel

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
            .padding(PaddingValues(horizontal = 20.dp, vertical = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Account / brand hero — feels like a profile header in a modern app.
        Column {
            Text("Account", style = PosTheme.typography.largeTitle, color = colors.labelPrimary)
            Spacer(Modifier.size(4.dp))
            Text(state.settings.storeName, style = PosTheme.typography.body, color = colors.labelSecondary)
        }

        Section(label = "Store") {
            SettingsRow(
                title = "Store name",
                value = state.settings.storeName,
                onClick = { editor = SettingsEditor.StoreName },
            )
            Divider()
            SettingsRow(
                title = "Tax rate",
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

        Section(label = "Hardware") {
            SettingsRow(title = "Printer", value = "Mock Printer (Demo)")
            Divider()
            ToggleRow(
                title = "Auto-print receipts",
                checked = state.settings.autoPrintReceipts,
                onCheckedChange = vm::onAutoPrintChanged,
            )
        }

        Section(label = "Sync") {
            SettingsRow(title = "Status", value = "Background")
        }

        Section(label = "About") {
            SettingsRow(title = "Version", value = "1.0.0")
            Divider()
            SettingsRow(title = "About Sahm POS", value = "›", onClick = onOpenAbout)
        }

        Spacer(Modifier.height(8.dp))
    }

    when (editor) {
        SettingsEditor.StoreName -> TextEditDialog(
            title = "Store name",
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
private fun Section(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel(text = label)
        PosCard {
            content()
        }
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
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = PosTheme.typography.body, color = colors.labelPrimary, modifier = Modifier.weight(1f))
        Text(value, style = PosTheme.typography.body, color = colors.labelSecondary)
    }
}

@Composable
private fun ToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val colors = PosTheme.colors
    val bg by animateColorAsState(
        targetValue = if (checked) colors.accent else colors.separatorStrong,
        animationSpec = PosMotion.tweenStandard(),
        label = "switch-bg",
    )
    Row(
        Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = PosTheme.typography.body, color = colors.labelPrimary, modifier = Modifier.weight(1f))
        Box(
            Modifier
                .size(width = 46.dp, height = 28.dp)
                .clip(PosTheme.shapes.pill)
                .background(bg),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Box(
                Modifier
                    .padding(3.dp)
                    .size(22.dp)
                    .clip(PosTheme.shapes.pill)
                    .background(colors.backgroundPrimary),
            )
        }
    }
}

@Composable
private fun Divider() {
    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(PosTheme.colors.separator))
}

// ---- Editor dialogs --------------------------------------------------------

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
        text = { EditField(value = text, onValueChange = { text = it }, keyboard = keyboard) },
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
    val initialText = (initial.value * 100).let {
        if (it == it.toInt().toDouble()) it.toInt().toString() else it.toString()
    }
    var text by remember { mutableStateOf(initialText) }
    val parsed: Double? = text.toDoubleOrNull()
    val valid = parsed != null && parsed in 0.0..100.0
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tax rate (%)", style = PosTheme.typography.title3) },
        text = {
            EditField(
                value = text,
                onValueChange = { text = it.filter { ch -> ch.isDigit() || ch == '.' } },
                keyboard = KeyboardType.Decimal,
            )
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
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// AboutScreen lives here for cohesion with the Settings stack.
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val colors = PosTheme.colors
    Column(Modifier.fillMaxSize().background(colors.backgroundPrimary)) {
        PosTopBar(title = "About", onBack = onBack)
        Column(
            Modifier.fillMaxSize().padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                Modifier
                    .size(80.dp)
                    .clip(PosTheme.shapes.lg)
                    .background(colors.accent),
                contentAlignment = Alignment.Center,
            ) {
                Text("S", style = PosTheme.typography.display, color = colors.onAccent)
            }
            Spacer(Modifier.size(20.dp))
            Text("Sahm Food POS", style = PosTheme.typography.title1, color = colors.labelPrimary)
            Spacer(Modifier.size(6.dp))
            Text("Version 1.0.0", style = PosTheme.typography.body, color = colors.labelSecondary)
            Spacer(Modifier.size(28.dp))
            Text("Built with Kotlin Multiplatform", style = PosTheme.typography.body, color = colors.labelSecondary)
            Spacer(Modifier.size(4.dp))
            Text("© 2026 Sahm Food", style = PosTheme.typography.footnote, color = colors.labelTertiary)
        }
    }
}
