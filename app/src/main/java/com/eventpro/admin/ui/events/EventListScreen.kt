package com.eventpro.admin.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.eventpro.admin.domain.model.EventStatus
import com.eventpro.admin.ui.components.AppTopBar
import com.eventpro.admin.ui.components.ConfirmDeleteDialog
import com.eventpro.admin.ui.components.EmptyState
import com.eventpro.admin.ui.components.EventCard
import com.eventpro.admin.ui.components.FilterChipRow
import com.eventpro.admin.ui.components.ShimmerList
import com.eventpro.admin.ui.navigation.Screen
import com.eventpro.admin.ui.theme.ErrorRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(navController: NavController, vm: EventListViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf<com.eventpro.admin.domain.model.Event?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { msg ->
            val result = snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = "UNDO",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                vm.undoDelete()
            } else {
                vm.clearSnackbar()
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Event Master List",
                showOfflineBadge = false,
                actions = {
                    IconButton(onClick = vm::onSearchToggle) { Icon(Icons.Outlined.Search, "Search") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AddEditEvent.createRoute()) }) {
                Icon(Icons.Default.Add, "Create Event")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            if (state.isSearchActive) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = vm::onSearchQuery,
                    placeholder = { Text("Search events…") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true
                )
            }
            FilterChipRow(
                options = listOf("All" to null) + EventStatus.entries.map { it.displayName to it },
                selected = state.selectedStatus,
                onSelect = vm::onStatusFilter
            )
            Spacer(Modifier.height(4.dp))
            FilterChipRow(
                options = TimeFilter.entries.map { it.name.replace("_", " ") to it },
                selected = state.selectedTimeFilter,
                onSelect = { vm.onTimeFilter(it ?: TimeFilter.ALL) }
            )
            Spacer(Modifier.height(4.dp))
            if (state.isLoading) {
                ShimmerList()
            } else if (state.events.isEmpty()) {
                EmptyState(Icons.Outlined.CalendarToday, "No events found", "Create your first event with the + button")
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.events, key = { it.id }) { event ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    vm.deleteEvent(event)
                                    true
                                } else false
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                Box(
                                    Modifier.fillMaxSize().background(ErrorRed).padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(Icons.Outlined.Delete, "Delete", tint = Color.White)
                                }
                            },
                            enableDismissFromStartToEnd = false,
                            content = {
                                EventCard(
                                    event = event,
                                    clientName = state.clientNameMap[event.clientId] ?: "",
                                    onClick = { navController.navigate(Screen.EventDetail.createRoute(event.id)) },
                                    onEdit = { navController.navigate(Screen.AddEditEvent.createRoute(event.id)) },
                                    onDelete = { showDeleteDialog = event }
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { event ->
        ConfirmDeleteDialog(
            itemName = event.title,
            onConfirm = { vm.deleteEvent(event); showDeleteDialog = null },
            onDismiss = { showDeleteDialog = null }
        )
    }
}
