package com.eventpro.admin.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eventpro.admin.domain.model.InventoryCategory
import com.eventpro.admin.domain.model.InventoryItem
import com.eventpro.admin.domain.model.StockStatus
import com.eventpro.admin.ui.theme.*

@Composable
fun StockStatusChip(status: StockStatus) {
    val (label, bg, fg) = when (status) {
        StockStatus.IN_STOCK -> Triple("In Stock", Color(0xFFD1FAE5), SuccessGreen)
        StockStatus.LOW_STOCK -> Triple("Low Stock", Color(0xFFFEF3C7), WarningAmber)
        StockStatus.OUT_OF_STOCK -> Triple("Out of Stock", ErrorContainer, ErrorRed)
    }
    Surface(shape = MaterialTheme.shapes.extraSmall, color = bg) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = fg, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
    }
}

@Composable
fun InventoryCard(item: InventoryItem, onViewDetails: () -> Unit) {
    val categoryIcon = when (item.category) {
        InventoryCategory.AUDIO -> Icons.Outlined.MusicNote
        InventoryCategory.VISUAL -> Icons.Outlined.Videocam
        InventoryCategory.LIGHTING -> Icons.Outlined.Lightbulb
        InventoryCategory.STAGING -> Icons.Outlined.MapsHomeWork
        InventoryCategory.FURNITURE -> Icons.Outlined.Chair
        InventoryCategory.OTHER -> Icons.Outlined.Inventory2
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(categoryIcon, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(item.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                StockStatusChip(item.stockStatus)
            }
            Spacer(Modifier.height(4.dp))
            Text(item.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            Divider(Modifier.padding(vertical = 8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Total: ${item.totalUnits}  Available: ${item.availableUnits}", style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(onClick = onViewDetails) { Text("Details") }
            }
        }
    }
}
