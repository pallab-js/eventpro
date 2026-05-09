package com.eventpro.admin.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eventpro.admin.R
import com.eventpro.admin.ui.theme.SurfaceContainerHigh

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    subtitle: String? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
    showOfflineBadge: Boolean = true
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
            if (showOfflineBadge) OfflineBadge()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    )
}

@Composable
fun OfflineBadge() {
    AssistChip(
        onClick = {},
        label = { Text(stringResource(R.string.status_local), style = MaterialTheme.typography.labelSmall) },
        leadingIcon = { Icon(Icons.Default.CloudOff, null, Modifier.size(16.dp)) },
        colors = AssistChipDefaults.assistChipColors(containerColor = SurfaceContainerHigh)
    )
}
