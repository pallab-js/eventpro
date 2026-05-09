package com.eventpro.admin.ui.events

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eventpro.admin.domain.model.Client
import com.eventpro.admin.domain.model.Event
import com.eventpro.admin.domain.model.EventVendor
import com.eventpro.admin.domain.model.InventoryCategory
import com.eventpro.admin.domain.model.InventoryItem
import com.eventpro.admin.domain.model.InventoryReservation
import com.eventpro.admin.domain.model.TimelineItem
import com.eventpro.admin.domain.model.Transaction
import com.eventpro.admin.domain.model.TransactionType
import com.eventpro.admin.domain.model.Vendor
import com.eventpro.admin.domain.model.VendorCategory
import com.eventpro.admin.domain.model.VendorStatus
import com.eventpro.admin.repository.safeValueOf
import com.eventpro.admin.ui.components.EmptyState
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
fun TimelineTab(items: List<TimelineItem>, vm: EventDetailViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(Icons.Outlined.Timeline, "No milestones", "Add milestones to track progress")
            }
        } else {
            val lastItem = items.lastOrNull()
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
                            if (item != lastItem) {
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
fun VendorTab(vendors: List<EventVendor>, vendorList: List<Vendor>, vm: EventDetailViewModel) {
    val context = LocalContext.current
    var showAssignDialog by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        if (vendors.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(Icons.Outlined.Business, "No vendors assigned", "Assign vendors to this event")
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
                        supportingContent = { Text(ev.vendor.category.displayName) },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (ev.vendor.phone.isNotBlank()) {
                                    IconButton(onClick = {
                                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${ev.vendor.phone}")))
                                    }) { Icon(Icons.Outlined.Phone, "Call ${ev.vendor.name}", Modifier.size(18.dp)) }
                                }
                                if (ev.vendor.email.isNotBlank()) {
                                    IconButton(onClick = {
                                        context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${ev.vendor.email}")))
                                    }) { Icon(Icons.Outlined.Email, "Email ${ev.vendor.name}", Modifier.size(18.dp)) }
                                }
                                val (label, color) = when (ev.status) {
                                    VendorStatus.CONFIRMED -> ev.status.displayName to SuccessGreen
                                    VendorStatus.PENDING_CONTRACT -> ev.status.displayName to WarningAmber
                                    VendorStatus.AWAITING_DEPOSIT -> ev.status.displayName to WarningAmber
                                    VendorStatus.CANCELLED -> ev.status.displayName to ErrorRed
                                }
                                Surface(shape = MaterialTheme.shapes.extraSmall, color = color.copy(alpha = 0.15f)) {
                                    Text(label, style = MaterialTheme.typography.labelSmall, color = color, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
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
                            value = selectedStatus.displayName, onValueChange = {},
                            readOnly = true, label = { Text("Status") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(statusExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                            VendorStatus.entries.forEach { s ->
                                DropdownMenuItem(text = { Text(s.displayName) }, onClick = { selectedStatus = s; statusExpanded = false })
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
fun BudgetTab(
    event: Event,
    client: Client?,
    note: String,
    eventTransactions: List<Transaction>,
    vm: EventDetailViewModel,
    navController: NavController,
    onDeleteClick: () -> Unit
) {
    var noteText by remember { mutableStateOf(note) }
    LaunchedEffect(note) { noteText = note }

    val categoryTotals = remember(eventTransactions) {
        eventTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, txs) -> txs.sumOf { it.amountCents } }
            .entries
            .sortedByDescending { it.value }
    }

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
        if (categoryTotals.isNotEmpty()) {
            val modelProducer = remember { CartesianChartModelProducer() }
            LaunchedEffect(categoryTotals) {
                modelProducer.runTransaction {
                    columnSeries { series(categoryTotals.map { it.value.toFloat() }) }
                }
            }
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Expense Breakdown by Category", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(12.dp))
                    CartesianChartHost(
                        chart = rememberCartesianChart(
                            rememberColumnCartesianLayer(),
                            startAxis = VerticalAxis.rememberStart(),
                            bottomAxis = HorizontalAxis.rememberBottom(
                                valueFormatter = { _, x, _ -> categoryTotals.getOrNull(x.toInt())?.key?.displayName ?: "" }
                            )
                        ),
                        modelProducer = modelProducer,
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                    )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryTab(
    reservations: List<InventoryReservation>,
    inventoryItems: List<InventoryItem>,
    vm: EventDetailViewModel
) {
    var showReserveDialog by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        if (reservations.isEmpty() && inventoryItems.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(Icons.Outlined.Inventory2, "No items reserved", "Reserve inventory items for this event")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 72.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (reservations.isNotEmpty()) {
                    item {
                        Text("Reserved Items", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    items(reservations, key = { it.id }) { r ->
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(r.itemName, style = MaterialTheme.typography.bodyMedium)
                                    Text("${r.itemCategory.displayName} · Qty: ${r.quantityReserved}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { vm.releaseReservation(r.itemId) }) {
                                    Icon(Icons.Outlined.Delete, "Release reservation")
                                }
                            }
                        }
                    }
                }
                if (inventoryItems.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text("Available Items", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    val reservedItemIds = reservations.map { it.itemId }.toSet()
                    val availableItems = inventoryItems.filter { it.id !in reservedItemIds && it.availableUnits > 0 }
                    items(availableItems, key = { it.id }) { item ->
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(item.name, style = MaterialTheme.typography.bodyMedium)
                                    Text("${item.category.displayName} · Available: ${item.availableUnits}/${item.totalUnits}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                var qty by remember { mutableStateOf("1") }
                                OutlinedTextField(
                                    value = qty, onValueChange = { qty = it.filter { c -> c.isDigit() } },
                                    modifier = Modifier.width(56.dp), singleLine = true,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                                )
                                Spacer(Modifier.width(4.dp))
                                IconButton(onClick = {
                                    val q = qty.toIntOrNull() ?: return@IconButton
                                    if (q > 0 && q <= item.availableUnits) {
                                        vm.reserveItem(item.id, q)
                                        qty = "1"
                                    }
                                }) {
                                    Icon(Icons.Outlined.Add, "Reserve item")
                                }
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { showReserveDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, "Reserve inventory item")
        }
    }

    if (showReserveDialog) {
        var selectedItemId by remember { mutableStateOf<Long?>(null) }
        var quantity by remember { mutableStateOf("1") }
        val reservedItemIds = reservations.map { it.id }.toSet()
        val availableItems = remember(inventoryItems, reservations) {
            inventoryItems.filter { it.id !in reservedItemIds && it.availableUnits > 0 }
        }

        AlertDialog(
            onDismissRequest = { showReserveDialog = false },
            title = { Text("Reserve Item") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        val selectedName = availableItems.find { it.id == selectedItemId }?.name ?: "Select item"
                        OutlinedTextField(
                            value = selectedName, onValueChange = {},
                            readOnly = true, label = { Text("Item") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            availableItems.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text("${item.name} (${item.availableUnits} avail)") },
                                    onClick = { selectedItemId = item.id; expanded = false }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = quantity, onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                        label = { Text("Quantity") }, singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = selectedItemId ?: return@TextButton
                        val q = quantity.toIntOrNull() ?: return@TextButton
                        if (q > 0) {
                            vm.reserveItem(id, q)
                            showReserveDialog = false
                        }
                    },
                    enabled = selectedItemId != null && (quantity.toIntOrNull() ?: 0) > 0
                ) { Text("Reserve") }
            },
            dismissButton = { TextButton(onClick = { showReserveDialog = false }) { Text("Cancel") } }
        )
    }
}
