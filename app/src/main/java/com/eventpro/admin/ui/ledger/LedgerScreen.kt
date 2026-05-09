package com.eventpro.admin.ui.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.eventpro.admin.data.local.dao.CategoryTotal
import com.eventpro.admin.ui.components.AppTopBar
import com.eventpro.admin.ui.components.EmptyState
import com.eventpro.admin.ui.components.MetricCard
import com.eventpro.admin.ui.components.SectionHeader
import com.eventpro.admin.ui.components.TransactionRow
import com.eventpro.admin.ui.navigation.Screen
import com.eventpro.admin.ui.theme.ErrorRed
import com.eventpro.admin.ui.theme.WarningAmber
import com.eventpro.admin.util.CurrencyFormatter
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
fun LedgerScreen(navController: NavController, vm: LedgerViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
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

    LaunchedEffect(state.exportStatus) {
        state.exportStatus?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Financial Ledger",
                showOfflineBadge = false,
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(Modifier.padding(padding)) {
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
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    vm.deleteTransaction(tx)
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
                                Column {
                                    TransactionRow(tx)
                                    Divider(Modifier.padding(horizontal = 16.dp))
                                }
                            }
                        )
                    }
                }
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
