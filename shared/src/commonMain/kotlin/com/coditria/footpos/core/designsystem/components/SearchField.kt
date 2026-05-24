package com.coditria.footpos.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.coditria.footpos.core.designsystem.PosMotion
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.shapes
import com.coditria.footpos.core.designsystem.typography

/**
 * Soft-fill search field with a focus-animated border. Sits on the primary
 * background (not secondary) so it feels like a first-class control rather
 * than another chip.
 */
@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
) {
    val colors = PosTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = if (focused) colors.labelPrimary else colors.separator,
        animationSpec = PosMotion.tweenStandard(),
        label = "search-border",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(PosTheme.shapes.md)
            .background(colors.backgroundSecondary)
            .border(width = 1.dp, color = borderColor, shape = PosTheme.shapes.md)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("⌕", style = PosTheme.typography.title3, color = colors.labelTertiary)
        Box(Modifier.padding(start = 10.dp).fillMaxWidth(if (value.isNotEmpty()) 0.92f else 1f)) {
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
                interactionSource = interaction,
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
