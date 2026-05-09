package com.eventpro.admin.domain.model

import androidx.compose.runtime.Immutable

enum class InventoryCategory(val displayName: String) {
    AUDIO("Audio"),
    VISUAL("Visual"),
    LIGHTING("Lighting"),
    STAGING("Staging"),
    FURNITURE("Furniture"),
    OTHER("Other")
}
enum class StockStatus { IN_STOCK, LOW_STOCK, OUT_OF_STOCK }

@Immutable
data class InventoryItem(
    val id: Long = 0,
    val name: String,
    val category: InventoryCategory,
    val description: String,
    val totalUnits: Int,
    val availableUnits: Int,
    val materialIconName: String
) {
    val stockStatus: StockStatus get() = when {
        availableUnits == 0 -> StockStatus.OUT_OF_STOCK
        availableUnits.toFloat() / totalUnits <= 0.20f -> StockStatus.LOW_STOCK
        else -> StockStatus.IN_STOCK
    }
}
