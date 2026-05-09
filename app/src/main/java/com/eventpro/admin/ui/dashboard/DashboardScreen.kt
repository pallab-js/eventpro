package com.eventpro.admin.ui.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.eventpro.admin.data.preferences.ThemeMode
import com.eventpro.admin.domain.model.AgendaItem
import com.eventpro.admin.domain.model.Event
import com.eventpro.admin.ui.components.AppTopBar
import com.eventpro.admin.ui.components.EmptyState
import com.eventpro.admin.ui.components.SectionHeader
import com.eventpro.admin.ui.components.StatusBadge
import com.eventpro.admin.ui.navigation.Screen
import com.eventpro.admin.ui.theme.ErrorRed
import com.eventpro.admin.ui.theme.SuccessGreen
import com.eventpro.admin.ui.theme.WarningAmber
import com.eventpro.admin.util.CurrencyFormatter
import com.eventpro.admin.util.DateFormatter
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, vm: DashboardViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val todayEvents by vm.todayEvents.collectAsStateWithLifecycle()
    val currentTheme by vm.themeMode.collectAsState(initial = ThemeMode.SYSTEM.value)
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddAgenda by remember { mutableStateOf(false) }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { msg ->
            val result = snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = "UNDO",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                vm.undoDeleteAgenda()
            } else {
                vm.clearSnackbar()
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "EventPro Admin",
                subtitle = "Operations Manager",
                showOfflineBadge = true,
                actions = {
                    val icon = when (currentTheme) {
                        ThemeMode.DARK.value -> Icons.Outlined.LightMode
                        else -> Icons.Outlined.DarkMode
                    }
                    IconButton(onClick = vm::toggleTheme) {
                        Icon(icon, "Toggle theme")
                    }
                }
            )
        },
        floatingActionButton = {
            SmallFloatingActionButton(onClick = { showAddAgenda = true }) {
                Icon(Icons.Default.Add, "Add agenda item")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        if (state.isInitialLoad) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = vm::refresh,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Quick Actions
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { navController.navigate(Screen.AddEditEvent.createRoute()) }) {
                        Text("+ Create Event")
                    }
                    OutlinedButton(onClick = { navController.navigate(Screen.AddEditClient.createRoute()) }) {
                        Text("+ New Client")
                    }
                }

                // Revenue Card
                ElevatedCard(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Recent Revenue · Trailing 30 Days", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text(CurrencyFormatter.formatCents(state.revenueYtdCents), style = MaterialTheme.typography.headlineMedium)
                        if (state.revenueChangePercent != 0f) {
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (state.revenueChangePercent >= 0) Icons.Outlined.TrendingUp else Icons.Outlined.TrendingDown,
                                    null, modifier = Modifier.size(16.dp),
                                    tint = if (state.revenueChangePercent >= 0) SuccessGreen else ErrorRed
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "${if (state.revenueChangePercent >= 0) "+" else ""}${"%.1f".format(state.revenueChangePercent)}% vs last period",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (state.revenueChangePercent >= 0) SuccessGreen else ErrorRed
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Pending: ${CurrencyFormatter.formatCents(state.pendingCents)}", style = MaterialTheme.typography.bodyMedium, color = WarningAmber)
                            Text("Expenses: ${CurrencyFormatter.formatCents(state.overdueCents)}", style = MaterialTheme.typography.bodyMedium, color = ErrorRed)
                        }
                        Spacer(Modifier.height(12.dp))
                        if (state.revenueChartData.isNotEmpty()) {
                            RevenueChart(state.revenueChartData)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Critical Milestones
                SectionHeader("Critical Milestones")
                if (state.criticalMilestones.isEmpty()) {
                    EmptyState(Icons.Outlined.EventNote, "No upcoming events", "Create an event to see milestones here")
                } else {
                    state.criticalMilestones.forEach { event ->
                        MilestoneCard(event, onClick = { navController.navigate(Screen.EventDetail.createRoute(event.id)) })
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Today's Agenda
                SectionHeader("Today's Agenda")
                if (state.todayAgenda.isEmpty()) {
                    EmptyState(Icons.Outlined.EventNote, "No agenda items today", "Tap + to add agenda items")
                } else {
                    state.todayAgenda.forEach { item ->
                        AgendaRow(item, onDelete = { vm.deleteAgendaItem(item) })
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showAddAgenda) {
        AddAgendaDialog(
            todayEvents = todayEvents,
            onDismiss = { showAddAgenda = false },
            onConfirm = { title, desc, millis, eventId ->
                vm.addAgendaItem(title, desc, millis, eventId)
                showAddAgenda = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAgendaDialog(
    todayEvents: List<Event>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long, Long?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedEventId by remember { mutableStateOf<Long?>(null) }
    var eventExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Agenda Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Title *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("Description") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = DateFormatter.format(dateMillis),
                    onValueChange = {},
                    label = { Text("Date") }, readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Outlined.CalendarToday, "Pick Date")
                        }
                    }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(DateFormatter.formatTime(dateMillis), style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { showTimePicker = true }) { Text("Change Time") }
                }
                if (todayEvents.isNotEmpty()) {
                    ExposedDropdownMenuBox(expanded = eventExpanded, onExpandedChange = { eventExpanded = it }) {
                        val eventName = todayEvents.find { it.id == selectedEventId }?.title ?: "None"
                        OutlinedTextField(
                            value = eventName, onValueChange = {},
                            readOnly = true, label = { Text("Link to Event") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(eventExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = eventExpanded, onDismissRequest = { eventExpanded = false }) {
                            DropdownMenuItem(text = { Text("None") }, onClick = { selectedEventId = null; eventExpanded = false })
                            todayEvents.forEach { e ->
                                DropdownMenuItem(text = { Text(e.title) }, onClick = { selectedEventId = e.id; eventExpanded = false })
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (title.isNotBlank()) onConfirm(title, description, dateMillis, selectedEventId) },
                enabled = title.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
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

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = java.util.Calendar.getInstance().apply { timeInMillis = dateMillis }.get(java.util.Calendar.HOUR_OF_DAY),
            initialMinute = java.util.Calendar.getInstance().apply { timeInMillis = dateMillis }.get(java.util.Calendar.MINUTE)
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val cal = java.util.Calendar.getInstance().apply { timeInMillis = dateMillis }
                    cal.set(java.util.Calendar.HOUR_OF_DAY, timePickerState.hour)
                    cal.set(java.util.Calendar.MINUTE, timePickerState.minute)
                    dateMillis = cal.timeInMillis
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AgendaRow(item: AgendaItem, onDelete: () -> Unit) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Row(
        Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            .combinedClickable(
                onClick = {},
                onLongClick = { showDeleteConfirm = true }
            )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(56.dp)) {
            Text(DateFormatter.formatTime(item.scheduledDateMillis), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Box(Modifier.size(8.dp).clip(CircleShape).background(if (item.eventId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant))
        }
        Spacer(Modifier.width(8.dp))
        Box(Modifier.width(2.dp).fillMaxHeight().background(MaterialTheme.colorScheme.outlineVariant))
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.bodyMedium)
            if (item.description.isNotEmpty()) {
                Text(item.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (item.eventId != null) {
                Spacer(Modifier.height(2.dp))
                Surface(shape = MaterialTheme.shapes.extraSmall, color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text("Linked to Event", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete agenda item?") },
            text = { Text("Are you sure you want to delete \"${item.title}\"?") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun RevenueChart(data: List<Pair<String, Float>>) {
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(data) {
        modelProducer.runTransaction {
            columnSeries { series(data.map { it.second }) }
        }
    }
    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = { _, x, _ -> data.getOrNull(x.toInt())?.first ?: "" }
            )
        ),
        modelProducer = modelProducer,
        modifier = Modifier.fillMaxWidth().height(160.dp)
    )
}

@Composable
private fun MilestoneCard(event: Event, onClick: () -> Unit) {
    val daysUntil = DateFormatter.daysUntil(event.startDateMillis)
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(event.title, style = MaterialTheme.typography.titleMedium)
                StatusBadge(event.status)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(4.dp))
                Text(event.venueName.ifEmpty { "Venue TBD" }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        when {
                            daysUntil < 0 -> "Past"
                            daysUntil == 0L -> "Today"
                            else -> "In $daysUntil days"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (daysUntil <= 7) MaterialTheme.colorScheme.error else SuccessGreen
                    )
                    if (event.estimatedAttendees > 0) {
                        Spacer(Modifier.width(12.dp))
                        Icon(Icons.Outlined.Groups, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(2.dp))
                        Text("Est. ${event.estimatedAttendees}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
