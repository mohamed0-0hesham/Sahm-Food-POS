package com.coditria.footpos.data.sync

import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

interface RetryPolicy {
    /** Returns the delay before the next attempt, or `null` to give up. */
    fun nextDelay(retryCount: Int): Duration?
}

class ExponentialBackoffRetryPolicy(
    private val baseDelay: Duration = 1.seconds,
    private val maxDelay: Duration = 5.minutes,
    private val maxRetries: Int = 5,
    private val multiplier: Double = 2.0,
) : RetryPolicy {
    override fun nextDelay(retryCount: Int): Duration? {
        if (retryCount >= maxRetries) return null
        val computed = baseDelay * multiplier.pow(retryCount.toDouble())
        return if (computed > maxDelay) maxDelay else computed
    }
}
