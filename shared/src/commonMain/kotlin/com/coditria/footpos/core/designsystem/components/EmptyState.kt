package com.coditria.footpos.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.typography

@Composable
fun EmptyState(
    glyph: String,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = PosTheme.colors
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(glyph, style = PosTheme.typography.largeTitle, color = colors.labelTertiary)
        Spacer(Modifier.size(16.dp))
        Text(title, style = PosTheme.typography.title2, color = colors.labelPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.size(8.dp))
        Text(message, style = PosTheme.typography.body, color = colors.labelSecondary, textAlign = TextAlign.Center)
        if (action != null) {
            Spacer(Modifier.size(20.dp))
            action()
        }
    }
}
