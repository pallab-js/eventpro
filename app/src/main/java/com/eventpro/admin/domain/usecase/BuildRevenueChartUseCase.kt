package com.eventpro.admin.domain.usecase

import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

class BuildRevenueChartUseCase @javax.inject.Inject constructor() {
    operator fun invoke(points: List<Pair<Long, Float>>): List<Pair<String, Float>> {
        val zoneId = ZoneId.systemDefault()
        val today = Instant.now().atZone(zoneId).toLocalDate()
        return (0..6).map { i ->
            val day = today.minusDays((6 - i).toLong())
            val label = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            val dayStart = day.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val dayEnd = day.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
            val total = points.filter { it.first in dayStart..dayEnd }.sumOf { it.second.toDouble() }.toFloat()
            label to total
        }
    }
}