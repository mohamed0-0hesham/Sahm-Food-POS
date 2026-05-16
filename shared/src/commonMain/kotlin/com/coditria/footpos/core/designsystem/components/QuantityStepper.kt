package com.coditria.footpos.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.typography

@Composable
fun QuantityStepper(
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PosTheme.colors
    Row(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colors.backgroundSecondary)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        StepperButton(onClick = onDecrement) {
            Text("−", style = PosTheme.typography.title3, color = colors.labelPrimary)
        }
        Text(
            text = value.toString(),
            style = PosTheme.typography.headline,
            color = colors.labelPrimary,
            modifier = Modifier.width(28.dp).wrapContentSize(Alignment.Center),
        )
        StepperButton(onClick = onIncrement) {
            Text("+", style = PosTheme.typography.title3, color = colors.labelPrimary)
        }
    }
}

@Composable
private fun StepperButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    val colors = PosTheme.colors
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(colors.backgroundTertiary)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
