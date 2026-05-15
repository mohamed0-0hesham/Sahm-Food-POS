package com.coditria.footpos.presentation.receipt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
import com.coditria.footpos.core.designsystem.typography

@Composable
fun ReceiptSheet(
    printedText: String?,
    onDone: () -> Unit,
    onReprint: () -> Unit,
) {
    val colors = PosTheme.colors
    Column(
        Modifier.fillMaxWidth().background(colors.backgroundPrimary).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Receipt", style = PosTheme.typography.title1, color = colors.labelPrimary)
        Spacer(Modifier.size(16.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, colors.separator, RoundedCornerShape(12.dp))
                .padding(20.dp),
        ) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = printedText ?: "(printer offline — receipt saved locally)",
                    style = PosTheme.typography.mono.copy(color = Color.Black, fontFamily = FontFamily.Monospace),
                )
            }
        }
        Spacer(Modifier.size(16.dp))
        if (printedText != null) {
            Text("✓ Printed successfully", style = PosTheme.typography.body, color = colors.success)
        } else {
            Text("⚠ Printer offline — saved locally", style = PosTheme.typography.body, color = colors.warning)
        }
        Spacer(Modifier.size(20.dp))
        PrimaryButton(text = "Done", onClick = onDone, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.size(8.dp))
        TertiaryButton(text = "Reprint", onClick = onReprint, modifier = Modifier.fillMaxWidth())
    }
}
