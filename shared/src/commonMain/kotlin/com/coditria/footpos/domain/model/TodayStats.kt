package com.coditria.footpos.domain.model

/**
 * Owner-dashboard snapshot for the current day. All values derive from the local
 * order history — the dashboard is a view over data, not its own source of truth.
 */
data class TodayStats(
    val revenue: Money,
    val orderCount: Int,
    val avgTicket: Money,
    val topItem: Product?,
    val topItemUnitsSold: Int,
    /**
     * Revenue per local hour from [hourStart] to [hourEnd], inclusive. Empty list means
     * no orders today. The chart uses these directly without any further bucketing.
     */
    val hourlyRevenue: List<HourlyBucket>,
    val peakHour: HourlyBucket?,
    val hourStart: Int = 9,
    val hourEnd: Int = 21,
)

data class HourlyBucket(
    val hour: Int,       // 0..23, local time
    val revenue: Money,
    val orderCount: Int,
)
