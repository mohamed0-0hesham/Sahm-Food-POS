package com.coditria.footpos.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.typography

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
) {
    val colors = PosTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.backgroundSecondary)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("⌕", style = PosTheme.typography.body, color = colors.labelTertiary)
        Box(Modifier.padding(start = 8.dp).fillMaxWidth(if (value.isNotEmpty()) 0.92f else 1f)) {
            if (value.isEmpty()) {
                Text(placeholder, style = PosTheme.typography.body, color = colors.labelTertiary)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = LocalTextStyle.current.merge(
                    PosTheme.typography.body.copy(color = colors.labelPrimary)
                ),
                cursorBrush = SolidColor(colors.accent),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (value.isNotEmpty()) {
            Text(
                "✕",
                style = PosTheme.typography.body,
                color = colors.labelTertiary,
                modifier = Modifier.clickable { onValueChange("") }.padding(4.dp),
            )
        }
    }
}
