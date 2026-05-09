package com.eventpro.admin.ui.clients

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.eventpro.admin.domain.model.ClientStatus
import com.eventpro.admin.domain.model.ClientTier
import com.eventpro.admin.ui.components.AppTopBar
import com.eventpro.admin.ui.components.EmptyState
import com.eventpro.admin.ui.components.EventCard
import com.eventpro.admin.ui.components.SectionHeader
import com.eventpro.admin.ui.navigation.Screen
import com.eventpro.admin.ui.theme.Navy800
import com.eventpro.admin.ui.theme.OnSurfaceVariant
import com.eventpro.admin.ui.theme.SuccessGreen
import com.eventpro.admin.ui.theme.SurfaceContainerHigh
import com.eventpro.admin.util.initials
import com.eventpro.admin.util.toColor

@Composable
fun ClientDetailScreen(navController: NavController, clientId: Long, vm: ClientDetailViewModel = hiltViewModel()) {
    LaunchedEffect(clientId) { vm.load(clientId) }
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            AppTopBar(
                title = state.client?.companyName ?: "Client",
                showOfflineBadge = false,
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Outlined.ArrowBack, "Back") } },
                actions = {
                    state.client?.let { client ->
                        TextButton(onClick = { navController.navigate(Screen.AddEditClient.createRoute(client.id)) }) { Text("Edit") }
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading || state.client == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        val client = state.client!!
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState())) {
            ElevatedCard(Modifier.fillMaxWidth().padding(16.dp)) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(64.dp).clip(CircleShape).background(client.companyName.toColor()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(client.companyName.initials(), color = Color.White, style = MaterialTheme.typography.headlineMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(client.companyName, style = MaterialTheme.typography.titleLarge)
                    Text(client.contactName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    val (tierLabel, tierBg, tierFg) = when (client.tier) {
                        ClientTier.VIP -> Triple("VIP", Color(0xFFFEF3C7), Color(0xFF92400E))
                        ClientTier.ENTERPRISE -> Triple("Enterprise", Navy800, Color.White)
                        ClientTier.STANDARD -> Triple("Standard", SurfaceContainerHigh, OnSurfaceVariant)
                    }
                    Surface(shape = MaterialTheme.shapes.extraSmall, color = tierBg) {
                        Text(tierLabel, style = MaterialTheme.typography.labelSmall, color = tierFg, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                    }
                    Spacer(Modifier.height(4.dp))
                    val (statusLabel, statusBg, statusFg) = if (client.status == ClientStatus.ACTIVE) {
                        Triple("ACTIVE", Color(0xFFD1FAE5), SuccessGreen)
                    } else {
                        Triple("PAST", SurfaceContainerHigh, OnSurfaceVariant)
                    }
                    Surface(shape = MaterialTheme.shapes.extraSmall, color = statusBg) {
                        Text(statusLabel, style = MaterialTheme.typography.labelSmall, color = statusFg, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${client.phone}")))
                    }) {
                        Icon(Icons.Outlined.Phone, "Call ${client.phone}", Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(client.phone)
                    }
                    TextButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${client.email}")))
                    }) {
                        Icon(Icons.Outlined.Email, "Email ${client.email}", Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(client.email)
                    }
                }
            }

            SectionHeader("Linked Events")
            if (state.linkedEvents.isEmpty()) {
                EmptyState(Icons.Outlined.EventNote, "No linked events", "Events assigned to this client will appear here")
            } else {
                state.linkedEvents.forEach { event ->
                    EventCard(
                        event = event,
                        onClick = { navController.navigate(Screen.EventDetail.createRoute(event.id)) },
                        onEdit = { navController.navigate(Screen.AddEditEvent.createRoute(event.id)) },
                        onDelete = {}
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
