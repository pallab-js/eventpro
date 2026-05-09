package com.eventpro.admin.ui.clients

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.MoreVert
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
import com.eventpro.admin.domain.model.ClientStatus
import com.eventpro.admin.ui.components.AppTopBar
import com.eventpro.admin.ui.components.ClientCard
import com.eventpro.admin.ui.components.EmptyState
import com.eventpro.admin.ui.components.FilterChipRow
import com.eventpro.admin.ui.components.ShimmerList
import com.eventpro.admin.ui.navigation.Screen
import com.eventpro.admin.ui.theme.ErrorRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientListScreen(navController: NavController, vm: ClientListViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
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
        topBar = { AppTopBar("Client Directory", showOfflineBadge = false) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AddEditClient.createRoute()) }) {
                Icon(Icons.Default.Add, "Add Client")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            OutlinedTextField(
                value = state.searchQuery, onValueChange = vm::onSearch,
                placeholder = { Text("Search clients\u2026") },
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
                ShimmerList()
            } else if (state.clients.isEmpty()) {
                EmptyState(Icons.Outlined.Groups, "No clients found", "Add your first client with the + button")
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.clients, key = { it.id }) { client ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    vm.deleteClient(client)
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
                                ClientCard(
                                    client = client,
                                    onClick = { navController.navigate(Screen.ClientDetail.createRoute(client.id)) },
                                    trailing = {}
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
