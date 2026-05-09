package com.eventpro.admin.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateFormatter {
    private val displayFmt = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private val timeFmt = SimpleDateFormat("h:mm a", Locale.getDefault())

    fun format(millis: Long): String = displayFmt.format(Date(millis))
    fun formatTime(millis: Long): String = timeFmt.format(Date(millis))

    fun daysUntil(millis: Long): Long {
        val diff = millis - System.currentTimeMillis()
        return TimeUnit.MILLISECONDS.toDays(diff)
    }

    fun todayStartMillis(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun todayEndMillis(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
    }.timeInMillis
}
