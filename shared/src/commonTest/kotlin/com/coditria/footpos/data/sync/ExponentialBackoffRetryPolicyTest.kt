package com.coditria.footpos.data.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class ExponentialBackoffRetryPolicyTest {

    @Test
    fun `doubles delay each attempt up to the cap`() {
        val policy = ExponentialBackoffRetryPolicy(
            baseDelay = 1.seconds,
            maxDelay = 60.seconds,
            maxRetries = 10,
            multiplier = 2.0,
        )
        assertEquals(1.seconds, policy.nextDelay(0))
        assertEquals(2.seconds, policy.nextDelay(1))
        assertEquals(4.seconds, policy.nextDelay(2))
        assertEquals(8.seconds, policy.nextDelay(3))
    }

    @Test
    fun `clamps to maxDelay`() {
        val policy = ExponentialBackoffRetryPolicy(
            baseDelay = 1.seconds,
            maxDelay = 5.seconds,
            maxRetries = 10,
            multiplier = 2.0,
        )
        assertEquals(5.seconds, policy.nextDelay(5))
    }

    @Test
    fun `returns null after maxRetries`() {
        val policy = ExponentialBackoffRetryPolicy(maxRetries = 3, maxDelay = 5.minutes)
        assertNull(policy.nextDelay(3))
        assertNull(policy.nextDelay(99))
    }
}
