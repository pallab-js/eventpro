package com.eventpro.admin.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eventpro.admin.domain.model.EventStatus
import com.eventpro.admin.ui.theme.ErrorContainer
import com.eventpro.admin.ui.theme.ErrorRed
import com.eventpro.admin.ui.theme.Navy300
import com.eventpro.admin.ui.theme.Navy700
import com.eventpro.admin.ui.theme.OnSurfaceVariant
import com.eventpro.admin.ui.theme.Slate200
import com.eventpro.admin.ui.theme.Slate600
import com.eventpro.admin.ui.theme.SurfaceContainerHigh

@Composable
fun StatusBadge(status: EventStatus) {
    val (label, bg, fg) = when (status) {
        EventStatus.DRAFT -> Triple("Draft", SurfaceContainerHigh, OnSurfaceVariant)
        EventStatus.CONFIRMED -> Triple("Confirmed", Color(0xFFD1FAE5), Color(0xFF065F46))
        EventStatus.IN_PROGRESS -> Triple("In Progress", Slate200, Slate600)
        EventStatus.COMPLETED -> Triple("Completed", Navy300.copy(alpha = 0.2f), Navy700)
        EventStatus.CANCELLED -> Triple("Cancelled", ErrorContainer, ErrorRed)
    }
    Surface(shape = MaterialTheme.shapes.extraSmall, color = bg) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
