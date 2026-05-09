package com.eventpro.admin.ui.events

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.eventpro.admin.domain.model.EventStatus
import com.eventpro.admin.ui.components.AppTopBar
import com.eventpro.admin.ui.components.ConfirmDeleteDialog
import com.eventpro.admin.ui.components.StatusBadge
import com.eventpro.admin.ui.navigation.Screen
import com.eventpro.admin.util.DateFormatter
import com.eventpro.admin.domain.model.InventoryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(navController: NavController, eventId: Long, vm: EventDetailViewModel = hiltViewModel()) {
    LaunchedEffect(eventId) { vm.load(eventId) }
    val state by vm.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.navigationEvent.collect { newId ->
            navController.navigate(Screen.EventDetail.createRoute(newId))
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = state.event?.title ?: "Event Detail",
                showOfflineBadge = false,
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") } },
                actions = {
                    OutlinedButton(onClick = { navController.navigate(Screen.AddEditEvent.createRoute(eventId)) }) { Text("Edit") }
                    Spacer(Modifier.width(8.dp))
                    state.event?.let { event ->
                        if (event.status != EventStatus.COMPLETED && event.status != EventStatus.CANCELLED) {
                            Button(onClick = { vm.markComplete(event) }) { Text("Mark Complete") }
                            Spacer(Modifier.width(8.dp))
                        }
                    }
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Outlined.MoreVert, "More options")
                        }
                        DropdownMenu(expanded = showOverflowMenu, onDismissRequest = { showOverflowMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Duplicate") },
                                onClick = { showOverflowMenu = false; vm.duplicateEvent() },
                                leadingIcon = { Icon(Icons.Outlined.ContentCopy, null) }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading || state.event == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        val event = state.event!!
        Column(Modifier.padding(padding)) {
            ElevatedCard(Modifier.fillMaxWidth().padding(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusBadge(event.status)
                        val days = DateFormatter.daysUntil(event.startDateMillis)
                        Surface(shape = MaterialTheme.shapes.extraSmall, color = MaterialTheme.colorScheme.secondaryContainer) {
                            Text(if (days >= 0) "In $days days" else "Past", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(event.venueName.ifEmpty { "Venue TBD" }, style = MaterialTheme.typography.bodyLarge)
                    Text(DateFormatter.format(event.startDateMillis), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            TabRow(selectedTabIndex = state.selectedTab) {
                listOf("Timeline", "Vendors", "Budget", "Inventory").forEachIndexed { i, title ->
                    Tab(selected = state.selectedTab == i, onClick = { vm.onTabSelect(i) }, text = { Text(title) })
                }
            }

            when (state.selectedTab) {
                0 -> TimelineTab(state.timelineItems, vm)
                1 -> VendorTab(state.vendors, state.vendorList, vm)
                2 -> BudgetTab(event, state.client, state.quickNote, state.eventTransactions, vm, navController, onDeleteClick = { showDeleteDialog = true })
                3 -> InventoryTab(state.reservations, state.inventoryItems, vm)
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            itemName = state.event?.title ?: "event",
            onConfirm = { state.event?.let { vm.deleteEvent(it) }; navController.popBackStack(); showDeleteDialog = false },
            onDismiss = { showDeleteDialog = false }
        )
    }
}
