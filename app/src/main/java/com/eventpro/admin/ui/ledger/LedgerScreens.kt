package com.eventpro.admin.ui.ledger

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.eventpro.admin.data.local.dao.CategoryTotal
import com.eventpro.admin.domain.model.ExpenseCategory
import com.eventpro.admin.domain.model.TransactionType
import com.eventpro.admin.ui.components.*
import com.eventpro.admin.ui.navigation.Screen
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

// ─── Ledger Screen ────────────────────────────────────────────────────────────

@Composable
fun LedgerScreen(navController: NavController, vm: LedgerViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    state.exportStatus?.let { msg ->
        LaunchedEffect(msg) {
            kotlinx.coroutines.delay(3000)
            vm.clearExportStatus()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Financial Ledger",
                showSyncChip = false,
                actions = {
                    IconButton(onClick = { vm.exportCsv(context) }) {
                        Icon(Icons.Outlined.FileDownload, "Export CSV")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AddTransaction.route) }) {
                Icon(Icons.Default.Add, "Add Transaction")
            }
        },
        snackbarHost = {
            state.exportStatus?.let {
                SnackbarHost(hostState = remember { SnackbarHostState() }.also { host ->
                    LaunchedEffect(it) { host.showSnackbar(it) }
                })
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(Modifier.padding(padding)) {
            // Summary metrics
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard(
                    label = "Total Revenue",
                    value = CurrencyFormatter.formatCentsCompact(state.totalIncomeCents),
                    trend = "+12%",
                    trendPositive = true,
                    modifier = Modifier.weight(1f)
                )
                ElevatedCard(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.elevatedCardColors(containerColor = WarningAmber.copy(alpha = 0.12f))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Pending Invoices", style = MaterialTheme.typography.labelSmall, color = WarningAmber)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            CurrencyFormatter.formatCentsCompact(state.pendingInvoicesCents),
                            style = MaterialTheme.typography.headlineMedium,
                            color = WarningAmber
                        )
                    }
                }
                MetricCard(
                    label = "Total Expenses",
                    value = CurrencyFormatter.formatCentsCompact(state.totalExpenseCents),
                    trendPositive = false,
                    modifier = Modifier.weight(1f)
                )
            }
            // Operating margin
            val margin = if (state.totalIncomeCents > 0)
                ((state.totalIncomeCents - state.totalExpenseCents).toFloat() / state.totalIncomeCents * 100)
            else 0f
            MetricCard(
                label = "Operating Margin",
                value = "%.1f%%".format(margin),
                trendPositive = margin >= 0,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(8.dp))

            // Category breakdown chips + chart
            if (state.categoryTotals.isNotEmpty()) {
                SectionHeader("Expense Breakdown")
                Row(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.categoryTotals.take(4).forEach { cat ->
                        Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.secondaryContainer) {
                            Text(
                                "${cat.category}: ${CurrencyFormatter.formatCentsCompact(cat.total)}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                ExpenseChart(state.categoryTotals)
                Spacer(Modifier.height(8.dp))
            }

            // Transactions list
            val displayedTxs = if (state.showAll) state.transactions else state.transactions.take(10)
            SectionHeader(
                title = "Transactions",
                action = if (state.transactions.size > 10) (if (state.showAll) "Show Less" else "View All") else null,
                onAction = vm::toggleShowAll
            )
            if (state.transactions.isEmpty()) {
                EmptyState(Icons.Outlined.Receipt, "No transactions", "Add your first transaction with the + button")
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(displayedTxs, key = { it.id }) { tx ->
                        TransactionRow(tx)
                        Divider(Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

// ─── Add Transaction Screen ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(navController: NavController, vm: AddTransactionViewModel = hiltViewModel()) {
    val form by vm.formState.collectAsStateWithLifecycle()
    val events by vm.events.collectAsStateWithLifecycle()

    LaunchedEffect(form.savedSuccessfully) {
        if (form.savedSuccessfully) navController.popBackStack()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Add Transaction",
                showSyncChip = false,
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Outlined.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Type segmented button
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(TransactionType.INCOME, TransactionType.EXPENSE).forEach { type ->
                    FilterChip(
                        selected = form.type == type,
                        onClick = { vm.onTypeChange(type) },
                        label = { Text(type.name) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            OutlinedTextField(value = form.description, onValueChange = vm::onDescriptionChange, label = { Text("Description *") }, isError = form.descriptionError != null, supportingText = form.descriptionError?.let { { Text(it) } }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = form.amountStr, onValueChange = vm::onAmountChange, label = { Text("Amount *") }, prefix = { Text("$") }, isError = form.amountError != null, supportingText = form.amountError?.let { { Text(it) } }, modifier = Modifier.fillMaxWidth())

            // Date picker
            var showDatePicker by remember { mutableStateOf(false) }
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = form.dateMillis)
            Box {
                OutlinedTextField(
                    value = DateFormatter.format(form.dateMillis),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date") },
                    trailingIcon = { Icon(Icons.Outlined.CalendarToday, "Select date") },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(Modifier.matchParentSize().clickable { showDatePicker = true })
            }
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { vm.onDateChange(it) }
                            showDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                    }
                ) { DatePicker(state = datePickerState) }
            }

            // Category dropdown
            var catExpanded by remember { mutableStateOf(false) }
            val categoryItems = categoriesForType(form.type)
            val selectedCategoryLabel = categoryItems.find { it.second == form.category }?.first ?: form.category.name
            ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }) {
                OutlinedTextField(value = selectedCategoryLabel, onValueChange = {}, readOnly = true, label = { Text("Category") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                    categoryItems.forEach { (label, cat) ->
                        DropdownMenuItem(text = { Text(label) }, onClick = { vm.onCategoryChange(cat); catExpanded = false })
                    }
                }
            }

            OutlinedTextField(value = form.referenceNumber, onValueChange = vm::onReferenceChange, label = { Text("Reference # (optional)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = form.clientOrVendorName, onValueChange = vm::onCounterpartyChange, label = { Text("Client / Vendor Name") }, modifier = Modifier.fillMaxWidth())

            // Linked event dropdown
            if (events.isNotEmpty()) {
                var eventExpanded by remember { mutableStateOf(false) }
                val selectedEventName = events.find { it.id == form.linkedEventId }?.title ?: "None"
                ExposedDropdownMenuBox(expanded = eventExpanded, onExpandedChange = { eventExpanded = it }) {
                    OutlinedTextField(value = selectedEventName, onValueChange = {}, readOnly = true, label = { Text("Linked Event (optional)") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(eventExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expanded = eventExpanded, onDismissRequest = { eventExpanded = false }) {
                        DropdownMenuItem(text = { Text("None") }, onClick = { vm.onEventChange(null); eventExpanded = false })
                        events.forEach { e -> DropdownMenuItem(text = { Text(e.title) }, onClick = { vm.onEventChange(e.id); eventExpanded = false }) }
                    }
                }
            }

            Button(onClick = vm::save, modifier = Modifier.fillMaxWidth(), enabled = !form.isSaving) {
                if (form.isSaving) CircularProgressIndicator(Modifier.size(20.dp)) else Text("Save Transaction")
            }
        }
    }
}

@Composable
private fun ExpenseChart(categoryTotals: List<CategoryTotal>) {
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(categoryTotals) {
        modelProducer.runTransaction {
            columnSeries { series(categoryTotals.map { it.total.toFloat() }) }
        }
    }
    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = { _, x, _ -> categoryTotals.getOrNull(x.toInt())?.category ?: "" }
            )
        ),
        modelProducer = modelProducer,
        modifier = Modifier.fillMaxWidth().height(160.dp).padding(horizontal = 16.dp)
    )
}
