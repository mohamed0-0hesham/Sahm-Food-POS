package com.coditria.footpos.presentation.receipt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.components.PrimaryButton
import com.coditria.footpos.core.designsystem.components.TertiaryButton
import com.coditria.footpos.core.designsystem.shapes
import com.coditria.footpos.core.designsystem.typography

@Composable
fun ReceiptSheet(
    printedText: String?,
    onDone: () -> Unit,
    onReprint: () -> Unit,
) {
    val colors = PosTheme.colors
    val ok = printedText != null
    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.backgroundPrimary)
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .padding(bottom = 16.dp)
                .clip(PosTheme.shapes.pill)
                .background(colors.separator)
                .size(width = 40.dp, height = 4.dp),
        )

        // Success / warning halo — ringed circle with status glyph.
        Box(
            Modifier
                .size(72.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background((if (ok) colors.success else colors.warning).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                if (ok) "✓" else "⚠",
                style = PosTheme.typography.title1,
                color = if (ok) colors.success else colors.warning,
            )
        }
        Spacer(Modifier.size(14.dp))
        Text(
            if (ok) "Order complete" else "Saved locally",
            style = PosTheme.typography.title1,
            color = colors.labelPrimary,
        )
        Spacer(Modifier.size(4.dp))
        Text(
            if (ok) "Receipt printed successfully." else "Printer offline — receipt saved for later.",
            style = PosTheme.typography.body,
            color = colors.labelSecondary,
        )

        Spacer(Modifier.size(20.dp))

        // Receipt paper. Soft border and slight cream tint so it reads as a physical
        // artifact, not just another panel.
        Box(
            Modifier
                .fillMaxWidth()
                .clip(PosTheme.shapes.md)
                .background(Color(0xFFFEFAF1))
                .border(1.dp, colors.separator, PosTheme.shapes.md)
                .padding(20.dp),
        ) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = printedText ?: "(no printed copy available)",
                    style = PosTheme.typography.mono.copy(color = Color.Black, fontFamily = FontFamily.Monospace),
                )
            }
        }

        Spacer(Modifier.size(20.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TertiaryButton(text = "Reprint", onClick = onReprint, modifier = Modifier.weight(1f))
            PrimaryButton(text = "Done", onClick = onDone, modifier = Modifier.weight(2f))
        }
    }
}
