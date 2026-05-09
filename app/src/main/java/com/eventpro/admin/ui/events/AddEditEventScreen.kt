package com.eventpro.admin.ui.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.eventpro.admin.domain.model.EventStatus
import com.eventpro.admin.ui.components.AppTopBar
import com.eventpro.admin.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEventScreen(navController: NavController, eventId: Long?, vm: AddEditEventViewModel = hiltViewModel()) {
    LaunchedEffect(eventId) { eventId?.let { vm.load(it) } }
    val form by vm.formState.collectAsStateWithLifecycle()
    val clients by vm.clients.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var showDiscardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(form.savedSuccessfully) {
        if (form.savedSuccessfully) navController.popBackStack()
    }

    BackHandler(enabled = form.hasUnsavedChanges) { showDiscardDialog = true }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard changes?") },
            text = { Text("Your unsaved changes will be lost.") },
            confirmButton = { TextButton(onClick = { navController.popBackStack() }) { Text("Discard") } },
            dismissButton = { TextButton(onClick = { showDiscardDialog = false }) { Text("Keep editing") } }
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (eventId == null) "Create Event" else "Edit Event",
                showOfflineBadge = false,
                navigationIcon = { IconButton(onClick = { if (form.hasUnsavedChanges) showDiscardDialog = true else navController.popBackStack() }) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") } }
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
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            var statusExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = it }) {
                OutlinedTextField(
                    value = form.status.displayName, onValueChange = {},
                    readOnly = true, label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(statusExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                    EventStatus.entries.forEach { s ->
                        DropdownMenuItem(text = { Text(s.displayName) }, onClick = { vm.onStatusChange(s); statusExpanded = false })
                    }
                }
            }
            var showStartDatePicker by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = DateFormatter.format(form.startDateMillis),
                onValueChange = {},
                label = { Text("Start Date") },
                readOnly = true,
                isError = form.dateError != null,
                supportingText = form.dateError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
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
            var showEndDatePicker by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = form.endDateMillis?.let { DateFormatter.format(it) } ?: "",
                onValueChange = {},
                label = { Text("End Date (optional)") },
                readOnly = true,
                isError = form.dateError != null,
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
            OutlinedTextField(value = form.venueName, onValueChange = vm::onVenueChange, label = { Text("Venue") }, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = form.estimatedAttendees, onValueChange = vm::onAttendeesChange,
                label = { Text("Estimated Attendees") },
                isError = form.attendeesError != null,
                supportingText = form.attendeesError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = form.totalBudget, onValueChange = vm::onBudgetChange,
                label = { Text("Total Budget ($)") }, prefix = { Text("$") },
                isError = form.budgetError != null,
                supportingText = form.budgetError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(value = form.notes, onValueChange = vm::onNotesChange, label = { Text("Notes") }, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { vm.save(eventId) }), modifier = Modifier.fillMaxWidth(), minLines = 3)
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
