package com.coditria.footpos.core.common

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Single point of access to "now" so callers don't repeat OptIn boilerplate. */
@OptIn(ExperimentalTime::class)
fun now(): Instant = Clock.System.now()
