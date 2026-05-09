package com.eventpro.admin.util

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatter {
    private val displayFmt = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    private val timeFmt = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    private val zoneId = ZoneId.systemDefault()

    fun format(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate().format(displayFmt)

    fun formatTime(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(zoneId).toLocalTime().format(timeFmt)

    fun daysUntil(millis: Long): Long =
        Duration.between(LocalDate.now(zoneId).atStartOfDay(),
            Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate().atStartOfDay()).toDays()

    fun todayStartMillis(): Long =
        LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant().toEpochMilli()

    fun todayEndMillis(): Long =
        LocalDate.now(zoneId).atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()
}
