package com.coditria.footpos.presentation.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.shapes
import com.coditria.footpos.core.designsystem.typography
import com.coditria.footpos.domain.model.HourlyBucket
import com.coditria.footpos.domain.model.Money
import com.coditria.footpos.domain.model.TodayStats

/**
 * Always-on owner snapshot for the Account tab.
 *
 * Visual model: a dark "executive card" — the only true dark surface in the app —
 * so the dashboard reads as a distinct, premium layer. Bars are inert grey, the
 * peak bar lights up in accent. Numbers animate in (alpha fade) on first compose
 * so the card feels alive when the cashier lands on the tab.
 *
 * Renders even with zero data: empty state shows a flat baseline + EGP 0.00 so
 * the layout doesn't shift after the first sale of the day comes in.
 */
@Composable
fun OwnerDashboardCard(
    stats: TodayStats?,
    modifier: Modifier = Modifier,
) {
    val cardBg = Color(0xFF09090B)
    val cardSubtle = Color(0xFF27272A)
    val onCardPrimary = Color(0xFFFAFAFA)
    val onCardSecondary = Color(0xFFA1A1AA)
    val onCardTertiary = Color(0xFF71717A)
    val accent = PosTheme.colors.accent

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(PosTheme.shapes.lg)
            .background(cardBg)
            .accentHaloCorner(accent = accent),
    ) {
        Column(Modifier.padding(20.dp)) {
            // ----- headline row ------------------------------------------------
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "REVENUE TODAY",
                        style = PosTheme.typography.label,
                        color = onCardTertiary,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = (stats?.revenue ?: Money(0)).format(),
                        style = PosTheme.typography.display,
                        color = onCardPrimary,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "ORDERS",
                        style = PosTheme.typography.label,
                        color = onCardTertiary,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = (stats?.orderCount ?: 0).toString(),
                        style = PosTheme.typography.largeTitle,
                        color = accent,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ----- hourly bar chart -------------------------------------------
            HourlyBarChart(
                stats = stats,
                inertColor = cardSubtle,
                accentColor = accent,
                labelColor = onCardTertiary,
            )

            Spacer(Modifier.height(20.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(cardSubtle),
            )
            Spacer(Modifier.height(16.dp))

            // ----- stats row --------------------------------------------------
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatTile(
                    label = "AVG TICKET",
                    value = (stats?.avgTicket ?: Money(0)).format(),
                    labelColor = onCardTertiary,
                    valueColor = onCardPrimary,
                    modifier = Modifier.weight(1f),
                )
                Divider(cardSubtle)
                StatTile(
                    label = "TOP ITEM",
                    value = stats?.topItem?.name ?: "—",
                    sub = stats?.topItem?.let { "${stats.topItemUnitsSold} sold" },
                    labelColor = onCardTertiary,
                    valueColor = onCardPrimary,
                    subColor = onCardSecondary,
                    modifier = Modifier.weight(1.2f),
                )
                Divider(cardSubtle)
                StatTile(
                    label = "PEAK",
                    value = stats?.peakHour?.hour?.let(::formatHour) ?: "—",
                    sub = stats?.peakHour?.revenue?.format(),
                    labelColor = onCardTertiary,
                    valueColor = onCardPrimary,
                    subColor = onCardSecondary,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun HourlyBarChart(
    stats: TodayStats?,
    inertColor: Color,
    accentColor: Color,
    labelColor: Color,
) {
    val hourStart = stats?.hourStart ?: 9
    val hourEnd = stats?.hourEnd ?: 21
    val hours = (hourStart..hourEnd).toList()
    val peakHour = stats?.peakHour?.hour
    val bucketsByHour: Map<Int, HourlyBucket> =
        stats?.hourlyRevenue?.associateBy { it.hour }.orEmpty()
    val maxCents = bucketsByHour.values.maxOfOrNull { it.revenue.amountInCents }?.coerceAtLeast(1L) ?: 1L

    Column {
        // Bars themselves — fixed 96dp tall canvas, bars right-aligned to bottom.
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .height(96.dp),
        ) {
            Row(
                Modifier.fillMaxWidth().fillMaxHeight(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                hours.forEach { hour ->
                    val cents = bucketsByHour[hour]?.revenue?.amountInCents ?: 0L
                    val ratio = (cents.toFloat() / maxCents.toFloat()).coerceIn(0f, 1f)
                    // Animate from zero so the chart reads as "data arriving" the
                    // first time the user lands on the tab.
                    val animated by animateFloatAsState(
                        targetValue = ratio,
                        animationSpec = tween(durationMillis = 600),
                        label = "bar-$hour",
                    )
                    // Empty hours still get a sliver so the baseline is visible.
                    val effective = if (animated == 0f) 0.04f else animated
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(effective)
                            .clip(PosTheme.shapes.xs)
                            .background(if (hour == peakHour) accentColor else inertColor),
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        // Axis labels: 9a / 12p / 3p / 6p / 9p — only the boundary ticks so the
        // axis stays uncluttered.
        Row(Modifier.fillMaxWidth()) {
            val ticks = listOf(9, 12, 15, 18, 21)
            ticks.forEachIndexed { i, h ->
                Text(
                    formatHourCompact(h),
                    style = PosTheme.typography.label,
                    color = labelColor,
                    modifier = Modifier.weight(1f),
                )
                if (i != ticks.lastIndex) Spacer(Modifier.width(0.dp))
            }
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier,
    sub: String? = null,
    subColor: Color = labelColor,
) {
    Column(modifier = modifier.padding(horizontal = 4.dp)) {
        Text(label, style = PosTheme.typography.label, color = labelColor)
        Spacer(Modifier.height(6.dp))
        Text(
            value,
            style = PosTheme.typography.headline,
            color = valueColor,
            maxLines = 1,
        )
        if (sub != null) {
            Spacer(Modifier.height(2.dp))
            Text(sub, style = PosTheme.typography.caption2, color = subColor)
        }
    }
}

@Composable
private fun Divider(color: Color) {
    Box(
        Modifier
            .padding(horizontal = 8.dp)
            .width(1.dp)
            .height(28.dp)
            .background(color),
    )
}

/** Subtle accent radial glow in the top-right of the dark card — same gesture as
 * splash / auth, scaled down. Keeps the dashboard visually tied to the brand. */
private fun Modifier.accentHaloCorner(accent: Color): Modifier = this.drawWithCache {
    val brush = Brush.radialGradient(
        colors = listOf(accent.copy(alpha = 0.30f), Color.Transparent),
        center = Offset(size.width * 1.02f, size.height * -0.10f),
        radius = size.minDimension * 0.85f,
    )
    onDrawBehind { drawRect(brush) }
}

/** "1p" / "11a" / "12p". 24h → 12h short form for the peak tile and axis labels. */
private fun formatHour(hour24: Int): String {
    val h = ((hour24 + 11) % 12) + 1
    val suffix = if (hour24 < 12) "a" else "p"
    return "$h$suffix"
}

private fun formatHourCompact(hour24: Int): String = formatHour(hour24)
