package com.eventpro.admin.usecase

import com.eventpro.admin.domain.usecase.BuildRevenueChartUseCase
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class BuildRevenueChartUseCaseTest {

    private val useCase = BuildRevenueChartUseCase()
    private val zoneId = ZoneId.systemDefault()
    private val today = Instant.now().atZone(zoneId).toLocalDate()

    @Test
    fun `returns 7 data points`() {
        val points = emptyList<Pair<Long, Float>>()
        val result = useCase(points)
        assertEquals(7, result.size)
    }

    @Test
    fun `each label is a 3-character day abbreviation`() {
        val points = emptyList<Pair<Long, Float>>()
        val result = useCase(points)
        result.forEach { (label, _) ->
            assertEquals(3, label.length)
        }
    }

    @Test
    fun `points within today are counted`() {
        val todayStart = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val todayMid = todayStart + 43_200_000L
        val points = listOf(todayMid to 100.0f)
        val result = useCase(points)
        val todayEntry = result.last()
        assertEquals(100.0f, todayEntry.second, 0.01f)
    }

    @Test
    fun `empty points result in zero totals`() {
        val result = useCase(emptyList())
        result.forEach { (_, total) ->
            assertEquals(0.0f, total, 0.01f)
        }
    }

    @Test
    fun `points sum correctly within same day`() {
        val todayStart = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val points = listOf(
            todayStart + 1000L to 50.0f,
            todayStart + 2000L to 75.0f,
            todayStart + 3000L to 25.0f
        )
        val result = useCase(points)
        assertEquals(150.0f, result.last().second, 0.01f)
    }

    @Test
    fun `points outside 7-day window are excluded`() {
        val eightDaysAgo = today.minusDays(8).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val points = listOf(eightDaysAgo to 500.0f)
        val result = useCase(points)
        result.forEach { (_, total) ->
            assertEquals(0.0f, total, 0.01f)
        }
    }
}
