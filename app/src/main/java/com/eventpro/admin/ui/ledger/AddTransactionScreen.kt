package com.eventpro.admin.ui.ledger

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.eventpro.admin.domain.model.Event
import com.eventpro.admin.domain.model.ExpenseCategory
import com.eventpro.admin.domain.model.TransactionType
import com.eventpro.admin.ui.components.AppTopBar
import com.eventpro.admin.util.DateFormatter

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
                showOfflineBadge = false,
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Outlined.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TransactionType.entries.forEach { type ->
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
