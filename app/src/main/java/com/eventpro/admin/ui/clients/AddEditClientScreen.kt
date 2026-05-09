package com.eventpro.admin.ui.clients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.eventpro.admin.domain.model.ClientStatus
import com.eventpro.admin.domain.model.ClientTier
import com.eventpro.admin.ui.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditClientScreen(navController: NavController, clientId: Long?, vm: AddEditClientViewModel = hiltViewModel()) {
    LaunchedEffect(clientId) { clientId?.let { vm.load(it) } }
    val form by vm.formState.collectAsStateWithLifecycle()

    LaunchedEffect(form.savedSuccessfully) {
        if (form.savedSuccessfully) navController.popBackStack()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (clientId == null) "Add Client" else "Edit Client",
                showOfflineBadge = false,
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Outlined.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = form.companyName, onValueChange = vm::onCompanyChange, label = { Text("Company Name *") }, isError = form.companyNameError != null, supportingText = form.companyNameError?.let { { Text(it) } }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = form.contactName, onValueChange = vm::onContactChange, label = { Text("Contact Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = form.phone, onValueChange = vm::onPhoneChange, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = form.email, onValueChange = vm::onEmailChange, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())

            var tierExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = tierExpanded, onExpandedChange = { tierExpanded = it }) {
                OutlinedTextField(value = form.tier.displayName, onValueChange = {}, readOnly = true, label = { Text("Tier") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(tierExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                ExposedDropdownMenu(expanded = tierExpanded, onDismissRequest = { tierExpanded = false }) {
                    ClientTier.entries.forEach { t -> DropdownMenuItem(text = { Text(t.displayName) }, onClick = { vm.onTierChange(t); tierExpanded = false }) }
                }
            }

            var statusExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = it }) {
                OutlinedTextField(value = form.status.displayName, onValueChange = {}, readOnly = true, label = { Text("Status") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(statusExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                    ClientStatus.entries.forEach { s -> DropdownMenuItem(text = { Text(s.displayName) }, onClick = { vm.onStatusChange(s); statusExpanded = false }) }
                }
            }

            Button(onClick = { vm.save(clientId) }, modifier = Modifier.fillMaxWidth(), enabled = !form.isSaving) {
                if (form.isSaving) CircularProgressIndicator(Modifier.size(20.dp)) else Text("Save Client")
            }
        }
    }
}
