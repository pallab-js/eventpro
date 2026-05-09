package com.eventpro.admin.repository

import com.eventpro.admin.data.local.entity.*
import com.eventpro.admin.domain.model.*

fun EventEntity.toDomain() = Event(id, title, EventStatus.valueOf(status), startDateMillis, endDateMillis, clientId, venueName, estimatedAttendees, notes, totalBudgetCents, spentBudgetCents, createdAtMillis, updatedAtMillis)
fun Event.toEntity() = EventEntity(id, title, status.name, startDateMillis, endDateMillis, clientId, venueName, estimatedAttendees, notes, totalBudgetCents, spentBudgetCents, createdAtMillis, updatedAtMillis)

fun ClientEntity.toDomain() = Client(id, companyName, contactName, phone, email, ClientTier.valueOf(tier), ClientStatus.valueOf(status), lastEventDateMillis, createdAtMillis)
fun Client.toEntity() = ClientEntity(id, companyName, contactName, phone, email, tier.name, status.name, lastEventDateMillis, createdAtMillis)

fun VendorEntity.toDomain() = Vendor(id, name, VendorCategory.valueOf(category), phone, email)
fun Vendor.toEntity() = VendorEntity(id, name, category.name, phone, email)

fun InventoryItemEntity.toDomain() = InventoryItem(id, name, InventoryCategory.valueOf(category), description, totalUnits, availableUnits, materialIconName)
fun InventoryItem.toEntity() = InventoryItemEntity(id, name, category.name, description, totalUnits, availableUnits, materialIconName)

fun TransactionEntity.toDomain() = Transaction(id, description, amountCents, TransactionType.valueOf(type), ExpenseCategory.valueOf(category), dateMillis, referenceNumber, eventId, clientOrVendorName)
fun Transaction.toEntity() = TransactionEntity(id, description, amountCents, type.name, category.name, dateMillis, referenceNumber, eventId, clientOrVendorName)

fun TimelineItemEntity.toDomain() = TimelineItem(id, eventId, title, description, scheduledDateMillis, completed)
fun TimelineItem.toEntity() = TimelineItemEntity(id, eventId, title, description, scheduledDateMillis, completed)

fun AgendaItemEntity.toDomain() = AgendaItem(id, title, description, scheduledDateMillis, eventId)
fun AgendaItem.toEntity() = AgendaItemEntity(id, title, description, scheduledDateMillis, eventId)
