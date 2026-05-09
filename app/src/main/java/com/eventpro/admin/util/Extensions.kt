package com.eventpro.admin.util

import androidx.compose.ui.graphics.Color

fun Long.toCentsString(): String = (this / 100.0).toString()

fun String.toColor(alpha: Float = 1f): Color {
    val hash = this.hashCode()
    val r = ((hash shr 16) and 0xFF) / 255f
    val g = ((hash shr 8) and 0xFF) / 255f
    val b = (hash and 0xFF) / 255f
    return Color(r.coerceIn(0.3f, 0.8f), g.coerceIn(0.3f, 0.8f), b.coerceIn(0.3f, 0.8f), alpha)
}

fun String.initials(): String = split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
