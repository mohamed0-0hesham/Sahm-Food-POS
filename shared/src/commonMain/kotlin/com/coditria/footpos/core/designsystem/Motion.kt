package com.coditria.footpos.core.designsystem

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable

/**
 * Motion tokens. Centralising durations and easings keeps animations across the app
 * feeling like one product instead of a patchwork. The easings mirror Material's
 * "emphasized" curves — they look natural for press / reveal / exit transitions.
 */
@Immutable
object PosMotion {
    const val DurationFast = 120
    const val DurationStandard = 220
    const val DurationSlow = 360

    val EaseStandard = CubicBezierEasing(0.20f, 0.00f, 0.00f, 1.00f)
    val EaseAccelerated = CubicBezierEasing(0.30f, 0.00f, 0.80f, 0.15f)
    val EaseDecelerated = CubicBezierEasing(0.05f, 0.70f, 0.10f, 1.00f)

    fun <T> tweenStandard() = tween<T>(durationMillis = DurationStandard, easing = EaseStandard)
    fun <T> tweenFast() = tween<T>(durationMillis = DurationFast, easing = EaseStandard)
    fun <T> tweenSlow() = tween<T>(durationMillis = DurationSlow, easing = EaseStandard)

    fun <T> springGentle() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    fun <T> springSnappy() = spring<T>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium,
    )
}
