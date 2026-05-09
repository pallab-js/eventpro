package com.eventpro.admin.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.eventpro.admin.domain.model.*
import com.eventpro.admin.ui.components.*
import com.eventpro.admin.ui.navigation.Screen
import com.eventpro.admin.ui.theme.ErrorRed
import com.eventpro.admin.ui.theme.Slate600
import com.eventpro.admin.ui.theme.SuccessGreen
import com.eventpro.admin.ui.theme.WarningAmber
import com.eventpro.admin.util.CurrencyFormatter
import com.eventpro.admin.util.DateFormatter

// ─── Event List ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(navController: NavController, vm: EventListViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf<com.eventpro.admin.domain.model.Event?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Event Master List",
                showSyncChip = false,
                actions = {
                    IconButton(onClick = vm::onSearchToggle) { Icon(Icons.Outlined.Search, "Search") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AddEditEvent.createRoute()) }) {
                Icon(Icons.Default.Add, "Create Event")
            }
        }
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
                options = listOf("All" to null) + EventStatus.values().map { it.name.replace("_", " ").lowercase().replaceFirstChar { c -> c.uppercase() } to it },
                selected = state.selectedStatus,
                onSelect = vm::onStatusFilter
            )
            Spacer(Modifier.height(4.dp))
            FilterChipRow(
                options = TimeFilter.values().map { it.name.replace("_", " ") to it },
                selected = state.selectedTimeFilter,
                onSelect = { vm.onTimeFilter(it ?: TimeFilter.ALL) }
            )
            Spacer(Modifier.height(4.dp))
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (state.events.isEmpty()) {
                EmptyState(Icons.Outlined.CalendarToday, "No events found", "Create your first event with the + button")
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.events, key = { it.id }) { event ->
                        EventCard(
                            event = event,
                            clientName = state.clientNameMap[event.clientId] ?: "",
                            onClick = { navController.navigate(Screen.EventDetail.createRoute(event.id)) },
                            onEdit = { navController.navigate(Screen.AddEditEvent.createRoute(event.id)) },
                            onDelete = { showDeleteDialog = event }
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

// ─── Event Detail ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(navController: NavController, eventId: Long, vm: EventDetailViewModel = hiltViewModel()) {
    LaunchedEffect(eventId) { vm.load(eventId) }
    val state by vm.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = state.event?.title ?: "Event Detail",
                showSyncChip = false,
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
            // Hero card
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

            // Tabs
            TabRow(selectedTabIndex = state.selectedTab) {
                listOf("Timeline", "Vendors", "Budget").forEachIndexed { i, title ->
                    Tab(selected = state.selectedTab == i, onClick = { vm.onTabSelect(i) }, text = { Text(title) })
                }
            }

            when (state.selectedTab) {
                0 -> TimelineTab(state.timelineItems, vm)
                1 -> VendorTab(state.vendors, state.vendorList, vm)
                2 -> BudgetTab(event, state.client, state.quickNote, vm, navController, onDeleteClick = { showDeleteDialog = true })
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimelineTab(items: List<TimelineItem>, vm: EventDetailViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(Icons.Outlined.CalendarToday, "No milestones", "Add milestones to track progress")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 72.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
                            Box(
                                modifier = Modifier.size(16.dp)
                                    .clip(CircleShape)
                                    .background(if (item.completed) SuccessGreen else MaterialTheme.colorScheme.outline)
                            )
                            if (items.last() != item) {
                                Box(
                                    modifier = Modifier.width(2.dp).height(40.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant)
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f).padding(bottom = 24.dp)) {
                            Text(item.title, style = MaterialTheme.typography.bodyMedium)
                            if (item.description.isNotEmpty()) {
                                Text(item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.CalendarToday, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.width(4.dp))
                                Text(DateFormatter.format(item.scheduledDateMillis), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, "Add Milestone")
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
        var showDatePicker by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Milestone") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title, onValueChange = { title = it },
                        label = { Text("Title") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = DateFormatter.format(dateMillis),
                        onValueChange = {},
                        label = { Text("Date") }, readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Outlined.DateRange, "Pick Date")
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isNotBlank()) {
                        vm.addTimelineItem(title.trim(), "", dateMillis)
                        showAddDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { dateMillis = it }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
            ) { DatePicker(state = datePickerState) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VendorTab(vendors: List<EventVendor>, vendorList: List<Vendor>, vm: EventDetailViewModel) {
    var showAssignDialog by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        if (vendors.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(Icons.Outlined.CalendarToday, "No vendors assigned", "Assign vendors to this event")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 72.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(vendors, key = { it.vendor.id }) { ev ->
                    val icon = when (ev.vendor.category) {
                        VendorCategory.CATERING -> Icons.Outlined.Restaurant
                        VendorCategory.FLORAL -> Icons.Outlined.LocalFlorist
                        VendorCategory.AV -> Icons.Outlined.Videocam
                        VendorCategory.MUSIC -> Icons.Outlined.MusicNote
                        VendorCategory.PHOTOGRAPHY -> Icons.Outlined.CameraAlt
                        VendorCategory.LOGISTICS -> Icons.Outlined.LocalShipping
                        VendorCategory.OTHER -> Icons.Outlined.Handyman
                    }
                    ListItem(
                        leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
                        headlineContent = { Text(ev.vendor.name) },
                        supportingContent = { Text(ev.vendor.category.name) },
                        trailingContent = {
                            val (label, color) = when (ev.status) {
                                VendorStatus.CONFIRMED -> "Confirmed" to SuccessGreen
                                VendorStatus.PENDING_CONTRACT -> "Pending Contract" to WarningAmber
                                VendorStatus.AWAITING_DEPOSIT -> "Awaiting Deposit" to WarningAmber
                                VendorStatus.CANCELLED -> "Cancelled" to ErrorRed
                            }
                            Surface(shape = MaterialTheme.shapes.extraSmall, color = color.copy(alpha = 0.15f)) {
                                Text(label, style = MaterialTheme.typography.labelSmall, color = color, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = { showAssignDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, "Assign Vendor")
        }
    }

    if (showAssignDialog) {
        var selectedVendorId by remember { mutableStateOf<Long?>(null) }
        var selectedStatus by remember { mutableStateOf(VendorStatus.PENDING_CONTRACT) }
        var vendorExpanded by remember { mutableStateOf(false) }
        var statusExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAssignDialog = false },
            title = { Text("Assign Vendor") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(expanded = vendorExpanded, onExpandedChange = { vendorExpanded = it }) {
                        val selectedName = vendorList.find { it.id == selectedVendorId }?.name ?: "Select vendor"
                        OutlinedTextField(
                            value = selectedName, onValueChange = {},
                            readOnly = true, label = { Text("Vendor") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(vendorExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = vendorExpanded, onDismissRequest = { vendorExpanded = false }) {
                            vendorList.forEach { v ->
                                DropdownMenuItem(text = { Text(v.name) }, onClick = { selectedVendorId = v.id; vendorExpanded = false })
                            }
                        }
                    }
                    ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = it }) {
                        OutlinedTextField(
                            value = selectedStatus.name, onValueChange = {},
                            readOnly = true, label = { Text("Status") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(statusExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                            VendorStatus.values().forEach { s ->
                                DropdownMenuItem(text = { Text(s.name) }, onClick = { selectedStatus = s; statusExpanded = false })
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedVendorId?.let { vm.assignVendor(it, selectedStatus) }
                        showAssignDialog = false
                    },
                    enabled = selectedVendorId != null
                ) { Text("Assign") }
            },
            dismissButton = { TextButton(onClick = { showAssignDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun BudgetTab(
    event: Event,
    client: Client?,
    note: String,
    vm: EventDetailViewModel,
    navController: NavController,
    onDeleteClick: () -> Unit
) {
    var noteText by remember { mutableStateOf(note) }
    LaunchedEffect(note) { noteText = note }

    Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                val progress = if (event.totalBudgetCents > 0) (event.spentBudgetCents.toFloat() / event.totalBudgetCents).coerceIn(0f, 1f) else 0f
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Text("Spent: ${CurrencyFormatter.formatCents(event.spentBudgetCents)} / ${CurrencyFormatter.formatCents(event.totalBudgetCents)}", style = MaterialTheme.typography.bodyMedium)
                Text("Remaining: ${CurrencyFormatter.formatCents(event.totalBudgetCents - event.spentBudgetCents)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Paid: ${CurrencyFormatter.formatCents(event.spentBudgetCents)}", style = MaterialTheme.typography.bodyMedium, color = SuccessGreen)
                    Text("Pending: ${CurrencyFormatter.formatCents(event.totalBudgetCents - event.spentBudgetCents)}", style = MaterialTheme.typography.bodyMedium, color = WarningAmber)
                }
            }
        }
        client?.let {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Client", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(it.companyName, style = MaterialTheme.typography.titleMedium)
                    Text(it.contactName, style = MaterialTheme.typography.bodyMedium)
                    Text(it.phone, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { navController.navigate(Screen.ClientDetail.createRoute(it.id)) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("View Profile") }
                }
            }
        }
        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("Quick Notes") },
            modifier = Modifier.fillMaxWidth().onFocusChanged { focusState ->
                if (!focusState.isFocused) {
                    vm.onNoteChange(noteText)
                    vm.saveNote()
                }
            },
            minLines = 3
        )
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onDeleteClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) { Text("Delete Event") }
    }
}

// ─── Add/Edit Event ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEventScreen(navController: NavController, eventId: Long?, vm: AddEditEventViewModel = hiltViewModel()) {
    LaunchedEffect(eventId) { eventId?.let { vm.load(it) } }
    val form by vm.formState.collectAsStateWithLifecycle()
    val clients by vm.clients.collectAsStateWithLifecycle()

    LaunchedEffect(form.savedSuccessfully) {
        if (form.savedSuccessfully) navController.popBackStack()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (eventId == null) "Create Event" else "Edit Event",
                showSyncChip = false,
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = form.title, onValueChange = vm::onTitleChange,
                label = { Text("Event Title *") },
                isError = form.titleError != null,
                supportingText = form.titleError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            // Status dropdown
            var statusExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = it }) {
                OutlinedTextField(
                    value = form.status.name, onValueChange = {},
                    readOnly = true, label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(statusExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                    EventStatus.values().forEach { s ->
                        DropdownMenuItem(text = { Text(s.name) }, onClick = { vm.onStatusChange(s); statusExpanded = false })
                    }
                }
            }
            // Start date picker
            var showStartDatePicker by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = DateFormatter.format(form.startDateMillis),
                onValueChange = {},
                label = { Text("Start Date") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showStartDatePicker = true }) { Icon(Icons.Outlined.DateRange, "Pick Date") }
                }
            )
            if (showStartDatePicker) {
                val datePickerState = rememberDatePickerState(initialSelectedDateMillis = form.startDateMillis)
                DatePickerDialog(
                    onDismissRequest = { showStartDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { vm.onDateChange(it) }
                            showStartDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") } }
                ) { DatePicker(state = datePickerState) }
            }
            // End date picker
            var showEndDatePicker by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = form.endDateMillis?.let { DateFormatter.format(it) } ?: "",
                onValueChange = {},
                label = { Text("End Date (optional)") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showEndDatePicker = true }) { Icon(Icons.Outlined.DateRange, "Pick Date") }
                }
            )
            if (showEndDatePicker) {
                val datePickerState = rememberDatePickerState(initialSelectedDateMillis = form.endDateMillis ?: form.startDateMillis)
                DatePickerDialog(
                    onDismissRequest = { showEndDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { vm.onEndDateChange(it) }
                            showEndDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = { TextButton(onClick = { showEndDatePicker = false }) { Text("Clear") } }
                ) { DatePicker(state = datePickerState) }
            }
            // Client selector
            var clientExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = clientExpanded, onExpandedChange = { clientExpanded = it }) {
                val clientName = clients.find { it.id == form.clientId }?.companyName ?: ""
                OutlinedTextField(
                    value = clientName, onValueChange = {},
                    readOnly = true, label = { Text("Client") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(clientExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = clientExpanded, onDismissRequest = { clientExpanded = false }) {
                    clients.forEach { c ->
                        DropdownMenuItem(text = { Text(c.companyName) }, onClick = { vm.onClientIdChange(c.id); clientExpanded = false })
                    }
                }
            }
            OutlinedTextField(value = form.venueName, onValueChange = vm::onVenueChange, label = { Text("Venue") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = form.estimatedAttendees, onValueChange = vm::onAttendeesChange, label = { Text("Estimated Attendees") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = form.totalBudget, onValueChange = vm::onBudgetChange,
                label = { Text("Total Budget ($)") }, prefix = { Text("$") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(value = form.notes, onValueChange = vm::onNotesChange, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Button(
                onClick = { vm.save(eventId) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !form.isSaving
            ) {
                if (form.isSaving) CircularProgressIndicator(Modifier.size(20.dp)) else Text("Save Event")
            }
        }
    }
}
