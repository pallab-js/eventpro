package com.eventpro.admin.ui.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eventpro.admin.domain.model.InventoryItem
import com.eventpro.admin.ui.components.StockStatusChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryDetailBottomSheet(
    item: InventoryItem,
    onDismiss: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onSave: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleLarge)
            Text(item.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Category: ${item.category.name}", style = MaterialTheme.typography.bodyMedium)
            Divider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Available Units", style = MaterialTheme.typography.bodyLarge)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilledIconButton(onClick = onDecrement, enabled = item.availableUnits > 0) {
                        Icon(Icons.Outlined.Remove, "Decrease")
                    }
                    Text("${item.availableUnits}", style = MaterialTheme.typography.headlineMedium)
                    FilledIconButton(onClick = onIncrement, enabled = item.availableUnits < item.totalUnits) {
                        Icon(Icons.Outlined.Add, "Increase")
                    }
                }
            }
            Text("Total: ${item.totalUnits}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            StockStatusChip(item.stockStatus)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) { Text("Save Changes") }
            Spacer(Modifier.height(16.dp))
        }
    }
}
