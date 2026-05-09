package com.eventpro.admin.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eventpro.admin.domain.model.Transaction
import com.eventpro.admin.domain.model.TransactionType
import com.eventpro.admin.ui.theme.ErrorRed
import com.eventpro.admin.ui.theme.SuccessGreen
import com.eventpro.admin.util.CurrencyFormatter
import com.eventpro.admin.util.DateFormatter

@Composable
fun TransactionRow(transaction: Transaction) {
    ListItem(
        leadingContent = {
            Icon(
                when (transaction.category.name) {
                    "VENUE" -> Icons.Outlined.LocationOn
                    "CATERING" -> Icons.Outlined.Restaurant
                    "STAFF" -> Icons.Outlined.Groups
                    "MARKETING" -> Icons.Outlined.Campaign
                    else -> Icons.Outlined.Receipt
                },
                null
            )
        },
        headlineContent = { Text(transaction.description, style = MaterialTheme.typography.titleSmall) },
        supportingContent = {
            Text(
                "${transaction.referenceNumber.ifEmpty { "—" }}  ·  ${DateFormatter.format(transaction.dateMillis)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Text(
                (if (transaction.type == TransactionType.INCOME) "+" else "-") + CurrencyFormatter.formatCents(transaction.amountCents),
                style = MaterialTheme.typography.titleSmall,
                color = if (transaction.type == TransactionType.INCOME) SuccessGreen else ErrorRed
            )
        }
    )
}
