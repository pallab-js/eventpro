package com.eventpro.admin.repository

import com.eventpro.admin.data.local.entity.AgendaItemEntity
import com.eventpro.admin.data.local.entity.ClientEntity
import com.eventpro.admin.data.local.entity.EventEntity
import com.eventpro.admin.data.local.entity.InventoryItemEntity
import com.eventpro.admin.data.local.entity.TimelineItemEntity
import com.eventpro.admin.data.local.entity.TransactionEntity
import com.eventpro.admin.data.local.entity.VendorEntity
import com.eventpro.admin.domain.model.AgendaItem
import com.eventpro.admin.domain.model.Client
import com.eventpro.admin.domain.model.ClientStatus
import com.eventpro.admin.domain.model.ClientTier
import com.eventpro.admin.domain.model.Event
import com.eventpro.admin.domain.model.EventStatus
import com.eventpro.admin.domain.model.ExpenseCategory
import com.eventpro.admin.domain.model.InventoryCategory
import com.eventpro.admin.domain.model.InventoryItem
import com.eventpro.admin.domain.model.TimelineItem
import com.eventpro.admin.domain.model.Transaction
import com.eventpro.admin.domain.model.TransactionType
import com.eventpro.admin.domain.model.Vendor
import com.eventpro.admin.domain.model.VendorCategory
import com.eventpro.admin.domain.model.VendorStatus

inline fun <reified T : Enum<T>> safeValueOf(name: String, default: T): T =
    runCatching { enumValueOf<T>(name) }.getOrDefault(default)

fun EventEntity.toDomain() = Event(id, title, safeValueOf(status, EventStatus.DRAFT), startDateMillis, endDateMillis, clientId, venueName, estimatedAttendees, notes, totalBudgetCents, spentBudgetCents, createdAtMillis, updatedAtMillis)
fun Event.toEntity() = EventEntity(id, title, status.name, startDateMillis, endDateMillis, clientId, venueName, estimatedAttendees, notes, totalBudgetCents, spentBudgetCents, createdAtMillis, updatedAtMillis)

fun ClientEntity.toDomain() = Client(id, companyName, contactName, phone, email, safeValueOf(tier, ClientTier.STANDARD), safeValueOf(status, ClientStatus.ACTIVE), lastEventDateMillis, createdAtMillis)
fun Client.toEntity() = ClientEntity(id, companyName, contactName, phone, email, tier.name, status.name, lastEventDateMillis, createdAtMillis)

fun VendorEntity.toDomain() = Vendor(id, name, safeValueOf(category, VendorCategory.OTHER), phone, email)
fun Vendor.toEntity() = VendorEntity(id, name, category.name, phone, email)

fun InventoryItemEntity.toDomain() = InventoryItem(id, name, safeValueOf(category, InventoryCategory.OTHER), description, totalUnits, availableUnits, materialIconName)
fun InventoryItem.toEntity() = InventoryItemEntity(id, name, category.name, description, totalUnits, availableUnits, materialIconName)

fun TransactionEntity.toDomain() = Transaction(id, description, amountCents, safeValueOf(type, TransactionType.INCOME), safeValueOf(category, ExpenseCategory.MISC), dateMillis, referenceNumber, eventId, clientOrVendorName)
fun Transaction.toEntity() = TransactionEntity(id, description, amountCents, type.name, category.name, dateMillis, referenceNumber, eventId, clientOrVendorName)

fun TimelineItemEntity.toDomain() = TimelineItem(id, eventId, title, description, scheduledDateMillis, completed)
fun TimelineItem.toEntity() = TimelineItemEntity(id, eventId, title, description, scheduledDateMillis, completed)

fun AgendaItemEntity.toDomain() = AgendaItem(id, title, description, scheduledDateMillis, eventId)
fun AgendaItem.toEntity() = AgendaItemEntity(id, title, description, scheduledDateMillis, eventId)
