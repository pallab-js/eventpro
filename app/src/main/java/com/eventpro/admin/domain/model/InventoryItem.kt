package com.eventpro.admin.domain.model

enum class InventoryCategory { AUDIO, VISUAL, LIGHTING, STAGING, FURNITURE, OTHER }
enum class StockStatus { IN_STOCK, LOW_STOCK, OUT_OF_STOCK }

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
