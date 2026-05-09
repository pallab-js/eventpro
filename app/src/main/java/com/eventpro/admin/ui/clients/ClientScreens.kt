package com.eventpro.admin.ui.clients

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
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
import com.eventpro.admin.ui.components.*
import com.eventpro.admin.ui.navigation.Screen
import com.eventpro.admin.ui.theme.*
import com.eventpro.admin.util.initials
import com.eventpro.admin.util.toColor
import androidx.compose.foundation.background

// ─── Client List ──────────────────────────────────────────────────────────────

@Composable
fun ClientListScreen(navController: NavController, vm: ClientListViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf<com.eventpro.admin.domain.model.Client?>(null) }

    Scaffold(
        topBar = { AppTopBar("Client Directory", showSyncChip = false) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AddEditClient.createRoute()) }) {
                Icon(Icons.Default.Add, "Add Client")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            OutlinedTextField(
                value = state.searchQuery, onValueChange = vm::onSearch,
                placeholder = { Text("Search clients…") },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )
            FilterChipRow(
                options = listOf("All" to null, "Active" to ClientStatus.ACTIVE, "Past" to ClientStatus.PAST),
                selected = state.selectedStatus,
                onSelect = vm::onStatusFilter
            )
            Spacer(Modifier.height(4.dp))
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (state.clients.isEmpty()) {
                EmptyState(Icons.Outlined.Groups, "No clients found", "Add your first client with the + button")
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.clients, key = { it.id }) { client ->
                        var menuExpanded by remember { mutableStateOf(false) }
                        ClientCard(
                            client = client,
                            onClick = { navController.navigate(Screen.ClientDetail.createRoute(client.id)) },
                            trailing = {
                                Box {
                                    IconButton(onClick = { menuExpanded = true }) {
                                        Icon(Icons.Outlined.MoreVert, "More options")
                                    }
                                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                        DropdownMenuItem(
                                            text = { Text("Delete") },
                                            onClick = { menuExpanded = false; showDeleteDialog = client },
                                            leadingIcon = { Icon(Icons.Outlined.Delete, null) }
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { client ->
        ConfirmDeleteDialog(
            itemName = client.companyName,
            onConfirm = { vm.deleteClient(client); showDeleteDialog = null },
            onDismiss = { showDeleteDialog = null }
        )
    }
}

// ─── Client Detail ────────────────────────────────────────────────────────────

@Composable
fun ClientDetailScreen(navController: NavController, clientId: Long, vm: ClientDetailViewModel = hiltViewModel()) {
    LaunchedEffect(clientId) { vm.load(clientId) }
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            AppTopBar(
                title = state.client?.companyName ?: "Client",
                showSyncChip = false,
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
            // Profile card
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
                    // Tier badge
                    val (tierLabel, tierBg, tierFg) = when (client.tier) {
                        ClientTier.VIP -> Triple("VIP", Color(0xFFFEF3C7), Color(0xFF92400E))
                        ClientTier.ENTERPRISE -> Triple("Enterprise", Navy800, Color.White)
                        ClientTier.STANDARD -> Triple("Standard", SurfaceContainerHigh, OnSurfaceVariant)
                    }
                    Surface(shape = MaterialTheme.shapes.extraSmall, color = tierBg) {
                        Text(tierLabel, style = MaterialTheme.typography.labelSmall, color = tierFg, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                    }
                    Spacer(Modifier.height(4.dp))
                    // Status badge
                    val (statusLabel, statusBg, statusFg) = if (client.status == ClientStatus.ACTIVE) {
                        Triple("ACTIVE", Color(0xFFD1FAE5), SuccessGreen)
                    } else {
                        Triple("PAST", SurfaceContainerHigh, OnSurfaceVariant)
                    }
                    Surface(shape = MaterialTheme.shapes.extraSmall, color = statusBg) {
                        Text(statusLabel, style = MaterialTheme.typography.labelSmall, color = statusFg, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    // Tappable phone
                    TextButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${client.phone}")))
                    }) {
                        Icon(Icons.Outlined.Phone, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(client.phone)
                    }
                    // Tappable email
                    TextButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${client.email}")))
                    }) {
                        Icon(Icons.Outlined.Email, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(client.email)
                    }
                }
            }

            SectionHeader("Linked Events")
            if (state.linkedEvents.isEmpty()) {
                EmptyState(Icons.Outlined.CalendarToday, "No linked events", "Events assigned to this client will appear here")
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

// ─── Add/Edit Client ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditClientScreen(navController: NavController, clientId: Long?, vm: AddEditClientViewModel = hiltViewModel()) {
    LaunchedEffect(clientId) { clientId?.let { vm.load(it) } }
    val form by vm.formState.collectAsStateWithLifecycle()

    LaunchedEffect(form.savedSuccessfully) {
        if (form.savedSuccessfully) navController.popBackStack()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (clientId == null) "Add Client" else "Edit Client",
                showSyncChip = false,
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Outlined.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = form.companyName, onValueChange = vm::onCompanyChange, label = { Text("Company Name *") }, isError = form.companyNameError != null, supportingText = form.companyNameError?.let { { Text(it) } }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = form.contactName, onValueChange = vm::onContactChange, label = { Text("Contact Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = form.phone, onValueChange = vm::onPhoneChange, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = form.email, onValueChange = vm::onEmailChange, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())

            var tierExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = tierExpanded, onExpandedChange = { tierExpanded = it }) {
                OutlinedTextField(value = form.tier.name, onValueChange = {}, readOnly = true, label = { Text("Tier") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(tierExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                ExposedDropdownMenu(expanded = tierExpanded, onDismissRequest = { tierExpanded = false }) {
                    ClientTier.values().forEach { t -> DropdownMenuItem(text = { Text(t.name) }, onClick = { vm.onTierChange(t); tierExpanded = false }) }
                }
            }

            var statusExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = it }) {
                OutlinedTextField(value = form.status.name, onValueChange = {}, readOnly = true, label = { Text("Status") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(statusExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                    ClientStatus.values().forEach { s -> DropdownMenuItem(text = { Text(s.name) }, onClick = { vm.onStatusChange(s); statusExpanded = false }) }
                }
            }

            Button(onClick = { vm.save(clientId) }, modifier = Modifier.fillMaxWidth(), enabled = !form.isSaving) {
                if (form.isSaving) CircularProgressIndicator(Modifier.size(20.dp)) else Text("Save Client")
            }
        }
    }
}


