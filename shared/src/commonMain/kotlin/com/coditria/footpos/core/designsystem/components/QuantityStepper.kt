package com.coditria.footpos.core.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.coditria.footpos.core.designsystem.PosMotion
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.shapes
import com.coditria.footpos.core.designsystem.typography

/**
 * Pill-shaped stepper with animated count flips. The two affordances are circular
 * outline buttons rather than filled chips — keeps the chrome quiet so the number
 * itself is the focal element, which is what cashiers actually look at.
 */
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
            .clip(PosTheme.shapes.pill)
            .background(colors.backgroundSecondary)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        StepperButton(onClick = onDecrement) {
            Text("−", style = PosTheme.typography.title3, color = colors.labelPrimary)
        }
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                val goingUp = targetState > initialState
                val dir = if (goingUp) 1 else -1
                (slideInVertically(PosMotion.tweenStandard()) { h -> dir * h } + fadeIn(PosMotion.tweenFast()))
                    .togetherWith(
                        slideOutVertically(PosMotion.tweenStandard()) { h -> -dir * h } + fadeOut(PosMotion.tweenFast())
                    )
            },
            label = "qty",
            modifier = Modifier.width(32.dp).wrapContentSize(Alignment.Center),
        ) { v ->
            Text(text = v.toString(), style = PosTheme.typography.headline, color = colors.labelPrimary)
        }
        StepperButton(onClick = onIncrement) {
            Text("+", style = PosTheme.typography.title3, color = colors.labelPrimary)
        }
    }
}

@Composable
private fun StepperButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    val colors = PosTheme.colors
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(colors.backgroundPrimary)
            .border(width = 1.dp, color = colors.separator, shape = CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
