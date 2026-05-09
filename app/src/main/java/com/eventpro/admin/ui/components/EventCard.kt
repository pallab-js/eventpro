package com.eventpro.admin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eventpro.admin.domain.model.Event
import com.eventpro.admin.domain.model.EventStatus
import com.eventpro.admin.ui.theme.ErrorRed
import com.eventpro.admin.ui.theme.Navy400
import com.eventpro.admin.ui.theme.OutlineColor
import com.eventpro.admin.ui.theme.Slate600
import com.eventpro.admin.ui.theme.SuccessGreen
import com.eventpro.admin.ui.theme.WarningAmber
import com.eventpro.admin.util.CurrencyFormatter
import com.eventpro.admin.util.DateFormatter

private fun EventStatus.borderColor() = when (this) {
    EventStatus.DRAFT -> OutlineColor
    EventStatus.CONFIRMED -> SuccessGreen
    EventStatus.IN_PROGRESS -> Slate600
    EventStatus.COMPLETED -> Navy400
    EventStatus.CANCELLED -> ErrorRed
}

@Composable
fun EventCard(
    event: Event,
    clientName: String = "",
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row {
            Box(Modifier.width(4.dp).fillMaxHeight().background(event.status.borderColor()))
            Column(Modifier.padding(12.dp).weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(event.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    StatusBadge(event.status)
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Outlined.MoreVert, "More")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(text = { Text("Edit") }, onClick = { showMenu = false; onEdit() })
                            DropdownMenuItem(text = { Text("Delete") }, onClick = { showMenu = false; onDelete() })
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarToday, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text(DateFormatter.format(event.startDateMillis), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (event.endDateMillis != null) {
                        Spacer(Modifier.width(4.dp))
                        Text("- ${DateFormatter.format(event.endDateMillis)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (clientName.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Business, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(4.dp))
                        Text(clientName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, null, Modifier.size(14.dp), tint = if (event.venueName.isEmpty()) WarningAmber else MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        event.venueName.ifEmpty { "Venue Pending" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (event.venueName.isEmpty()) WarningAmber else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (event.estimatedAttendees > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Groups, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(4.dp))
                        Text("${event.estimatedAttendees} attendees", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (event.totalBudgetCents > 0) {
                    Spacer(Modifier.height(8.dp))
                    val progress = (event.spentBudgetCents.toFloat() / event.totalBudgetCents).coerceIn(0f, 1f)
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    Text(
                        "${CurrencyFormatter.formatCents(event.spentBudgetCents)} / ${CurrencyFormatter.formatCents(event.totalBudgetCents)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
