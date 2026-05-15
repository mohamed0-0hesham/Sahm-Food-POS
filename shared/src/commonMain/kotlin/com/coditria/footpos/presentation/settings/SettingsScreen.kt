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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.typography

@Composable
fun SettingsScreen(onOpenAbout: () -> Unit) {
    val colors = PosTheme.colors
    var autoPrint by remember { mutableStateOf(true) }

    Column(
        Modifier.fillMaxSize().background(colors.backgroundPrimary)
            .padding(PaddingValues(20.dp)),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text("Settings", style = PosTheme.typography.largeTitle, color = colors.labelPrimary)
        Section(title = "STORE") {
            SettingsRow(title = "Store Name", value = "Sahm Food Demo")
            Divider()
            SettingsRow(title = "Tax Rate", value = "14%")
            Divider()
            SettingsRow(title = "Currency", value = "EGP")
        }
        Section(title = "HARDWARE") {
            SettingsRow(title = "Printer", value = "Mock Printer (Demo)")
            Divider()
            ToggleRow(title = "Auto-print receipts", checked = autoPrint, onCheckedChange = { autoPrint = it })
        }
        Section(title = "SYNC") {
            SettingsRow(title = "Status", value = "Background")
        }
        Section(title = "ABOUT") {
            SettingsRow(title = "Version", value = "1.0.0")
            Divider()
            SettingsRow(title = "About Sahm POS", value = "›", onClick = onOpenAbout)
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, style = PosTheme.typography.caption1, color = PosTheme.colors.labelTertiary, modifier = Modifier.padding(bottom = 8.dp))
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
    Row(
        Modifier.fillMaxWidth().let { m -> if (onClick != null) m.clickable { onClick() } else m }.padding(16.dp),
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
            Box(Modifier.size(22.dp).padding(2.dp).clip(RoundedCornerShape(11.dp)).background(androidx.compose.ui.graphics.Color.White))
        }
    }
}

@Composable
private fun Divider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(PosTheme.colors.separator))
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val colors = PosTheme.colors
    Column(Modifier.fillMaxSize().background(colors.backgroundPrimary)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", style = PosTheme.typography.title2, color = colors.accent, modifier = Modifier.clickable { onBack() }.padding(end = 8.dp))
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
