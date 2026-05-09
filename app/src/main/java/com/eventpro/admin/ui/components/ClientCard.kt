package com.eventpro.admin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eventpro.admin.domain.model.Client
import com.eventpro.admin.domain.model.ClientStatus
import com.eventpro.admin.ui.theme.SuccessGreen
import com.eventpro.admin.util.DateFormatter
import com.eventpro.admin.util.initials
import com.eventpro.admin.util.toColor

@Composable
fun ClientCard(client: Client, onClick: () -> Unit, trailing: @Composable () -> Unit = {}) {
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Avatar
            Box(
                Modifier.size(48.dp).clip(CircleShape).background(client.companyName.toColor()),
                contentAlignment = Alignment.Center
            ) {
                Text(client.companyName.initials(), color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(if (client.status == ClientStatus.ACTIVE) SuccessGreen else MaterialTheme.colorScheme.outline))
                    Spacer(Modifier.width(6.dp))
                    Text(client.companyName, style = MaterialTheme.typography.titleMedium)
                }
                Text(client.contactName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Phone, "Phone", Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text(client.phone, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Email, "Email", Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text(client.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                client.lastEventDateMillis?.let {
                    Text("Last event: ${DateFormatter.format(it)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            trailing()
            Icon(Icons.Outlined.ChevronRight, "View details", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
