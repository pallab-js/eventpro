package com.eventpro.admin.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eventpro.admin.ui.theme.SurfaceContainerHigh

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    subtitle: String? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
    showSyncChip: Boolean = true
) {
    TopAppBar(
        title = {
            if (subtitle != null) {
                ListItem(
                    headlineContent = { Text(title, style = MaterialTheme.typography.titleLarge) },
                    supportingContent = { Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )
            } else {
                Text(title, style = MaterialTheme.typography.titleLarge)
            }
        },
        navigationIcon = { navigationIcon?.invoke() },
        actions = {
            actions?.invoke()
            if (showSyncChip) SyncStatusChip()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    )
}

@Composable
fun SyncStatusChip() {
    AssistChip(
        onClick = {},
        label = { Text("Local", style = MaterialTheme.typography.labelSmall) },
        leadingIcon = { Icon(Icons.Default.CloudOff, null, Modifier.size(16.dp)) },
        colors = AssistChipDefaults.assistChipColors(containerColor = SurfaceContainerHigh)
    )
}
