package com.eventpro.admin.data.local

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.eventpro.admin.data.local.entity.AgendaItemEntity
import com.eventpro.admin.data.local.entity.ClientEntity
import com.eventpro.admin.data.local.entity.EventEntity
import com.eventpro.admin.data.local.entity.EventVendorEntity
import com.eventpro.admin.data.local.entity.InventoryItemEntity
import com.eventpro.admin.data.local.entity.TimelineItemEntity
import com.eventpro.admin.data.local.entity.TransactionEntity
import com.eventpro.admin.data.local.entity.VendorEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class DatabaseSeedCallback(
    private val context: () -> Context,
    private val scope: CoroutineScope
) : RoomDatabase.Callback() {
    override fun onCreate(database: SupportSQLiteDatabase) {
        super.onCreate(database)
        scope.launch(Dispatchers.IO) { seed(AppDatabase.getInstance(context())) }
    }

    private suspend fun seed(db: AppDatabase) {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        cal.set(2026, Calendar.JANUARY, 10); val c1 = cal.timeInMillis
        cal.set(2026, Calendar.FEBRUARY, 5); val c2 = cal.timeInMillis
        cal.set(2025, Calendar.NOVEMBER, 20); val c3 = cal.timeInMillis

        val clientId1 = db.clientDao().upsertClient(ClientEntity(companyName = "Apex Innovations", contactName = "Sarah Chen", phone = "+1-555-0101", email = "sarah@apex.com", tier = "VIP", status = "ACTIVE", lastEventDateMillis = c1, createdAtMillis = now))
        val clientId2 = db.clientDao().upsertClient(ClientEntity(companyName = "Meridian Corp", contactName = "James Okafor", phone = "+1-555-0202", email = "james@meridian.com", tier = "ENTERPRISE", status = "ACTIVE", lastEventDateMillis = c2, createdAtMillis = now))
        val clientId3 = db.clientDao().upsertClient(ClientEntity(companyName = "Bloom & Co", contactName = "Priya Nair", phone = "+1-555-0303", email = "priya@bloom.co", tier = "STANDARD", status = "PAST", lastEventDateMillis = c3, createdAtMillis = now))

        cal.set(2026, Calendar.JUNE, 15); val e1Start = cal.timeInMillis
        cal.set(2026, Calendar.JUNE, 16); val e1End = cal.timeInMillis
        cal.set(2026, Calendar.MAY, 20); val e2Start = cal.timeInMillis
        cal.set(2026, Calendar.JULY, 8); val e3Start = cal.timeInMillis

        val eventId1 = db.eventDao().upsertEvent(EventEntity(title = "Apex Annual Gala", status = "CONFIRMED", startDateMillis = e1Start, endDateMillis = e1End, clientId = clientId1, venueName = "Grand Ballroom, Hilton", estimatedAttendees = 350, notes = "Black-tie event. Confirm AV setup 48h prior.", totalBudgetCents = 8500000L, spentBudgetCents = 3200000L, createdAtMillis = now, updatedAtMillis = now))
        val eventId2 = db.eventDao().upsertEvent(EventEntity(title = "Meridian Product Launch", status = "IN_PROGRESS", startDateMillis = e2Start, endDateMillis = null, clientId = clientId2, venueName = "Tech Hub Conference Center", estimatedAttendees = 120, notes = "Live demo stage required.", totalBudgetCents = 4200000L, spentBudgetCents = 2800000L, createdAtMillis = now, updatedAtMillis = now))
        db.eventDao().upsertEvent(EventEntity(title = "Bloom Summer Retreat", status = "DRAFT", startDateMillis = e3Start, endDateMillis = null, clientId = clientId3, venueName = "", estimatedAttendees = 60, notes = "Venue TBD.", totalBudgetCents = 1500000L, spentBudgetCents = 0L, createdAtMillis = now, updatedAtMillis = now))

        val v1 = db.vendorDao().upsertVendor(VendorEntity(name = "Elite Catering Co.", category = "CATERING", phone = "+1-555-0401", email = "ops@elitecatering.com"))
        val v2 = db.vendorDao().upsertVendor(VendorEntity(name = "SoundWave AV", category = "AV", phone = "+1-555-0402", email = "book@soundwave.com"))
        val v3 = db.vendorDao().upsertVendor(VendorEntity(name = "Petal & Bloom Florals", category = "FLORAL", phone = "+1-555-0403", email = "hello@petalbloom.com"))
        val v4 = db.vendorDao().upsertVendor(VendorEntity(name = "ShutterPro Photography", category = "PHOTOGRAPHY", phone = "+1-555-0404", email = "studio@shutterpro.com"))

        db.vendorDao().upsertEventVendor(EventVendorEntity(eventId1, v1, "CONFIRMED", 150000L, true))
        db.vendorDao().upsertEventVendor(EventVendorEntity(eventId1, v2, "AWAITING_DEPOSIT", 0L, false))
        db.vendorDao().upsertEventVendor(EventVendorEntity(eventId1, v3, "CONFIRMED", 80000L, true))
        db.vendorDao().upsertEventVendor(EventVendorEntity(eventId2, v2, "CONFIRMED", 120000L, true))
        db.vendorDao().upsertEventVendor(EventVendorEntity(eventId2, v4, "PENDING_CONTRACT", 0L, false))

        db.inventoryDao().upsertItem(InventoryItemEntity(name = "Line Array Speaker System", category = "AUDIO", description = "Professional 12-unit line array with subwoofers", totalUnits = 4, availableUnits = 3, materialIconName = "speaker"))
        db.inventoryDao().upsertItem(InventoryItemEntity(name = "LED Par Can Lights", category = "LIGHTING", description = "RGBW LED par cans, DMX controlled", totalUnits = 24, availableUnits = 4, materialIconName = "lightbulb"))
        db.inventoryDao().upsertItem(InventoryItemEntity(name = "6ft Banquet Tables", category = "FURNITURE", description = "Folding banquet tables, seats 8", totalUnits = 30, availableUnits = 22, materialIconName = "table_restaurant"))
        db.inventoryDao().upsertItem(InventoryItemEntity(name = "HD Projector 5000lm", category = "VISUAL", description = "4K capable projector with HDMI/wireless", totalUnits = 3, availableUnits = 0, materialIconName = "videocam"))

        val t = { months: Int, day: Int ->
            cal.set(2026, months, day); cal.timeInMillis
        }
        db.transactionDao().upsertTransaction(TransactionEntity(description = "Apex Gala — Client Deposit", amountCents = 4250000L, type = "INCOME", category = "MISC", dateMillis = t(Calendar.MARCH, 1), referenceNumber = "INV-2026-001", eventId = eventId1, clientOrVendorName = "Apex Innovations", isPaid = true))
        db.transactionDao().upsertTransaction(TransactionEntity(description = "Meridian Launch — Full Payment", amountCents = 4200000L, type = "INCOME", category = "MISC", dateMillis = t(Calendar.APRIL, 15), referenceNumber = "INV-2026-002", eventId = eventId2, clientOrVendorName = "Meridian Corp", isPaid = true))
        db.transactionDao().upsertTransaction(TransactionEntity(description = "Elite Catering — Deposit", amountCents = 150000L, type = "EXPENSE", category = "CATERING", dateMillis = t(Calendar.MARCH, 10), referenceNumber = "EXP-2026-001", eventId = eventId1, clientOrVendorName = "Elite Catering Co.", isPaid = true))
        db.transactionDao().upsertTransaction(TransactionEntity(description = "SoundWave AV — Equipment Hire", amountCents = 320000L, type = "EXPENSE", category = "LOGISTICS", dateMillis = t(Calendar.APRIL, 2), referenceNumber = "EXP-2026-002", eventId = eventId2, clientOrVendorName = "SoundWave AV", isPaid = true))
        db.transactionDao().upsertTransaction(TransactionEntity(description = "Petal & Bloom — Floral Deposit", amountCents = 80000L, type = "EXPENSE", category = "MISC", dateMillis = t(Calendar.MARCH, 20), referenceNumber = "EXP-2026-003", eventId = eventId1, clientOrVendorName = "Petal & Bloom Florals", isPaid = false))
        db.transactionDao().upsertTransaction(TransactionEntity(description = "Venue Hire — Tech Hub", amountCents = 250000L, type = "EXPENSE", category = "VENUE", dateMillis = t(Calendar.FEBRUARY, 28), referenceNumber = "EXP-2026-004", eventId = eventId2, clientOrVendorName = "Tech Hub Conference Center", isPaid = true))
        db.transactionDao().upsertTransaction(TransactionEntity(description = "Marketing Materials Print", amountCents = 45000L, type = "EXPENSE", category = "MARKETING", dateMillis = t(Calendar.APRIL, 10), referenceNumber = "EXP-2026-005", eventId = null, clientOrVendorName = "PrintFast Ltd", isPaid = true))
        db.transactionDao().upsertTransaction(TransactionEntity(description = "Event Staff — Meridian Launch", amountCents = 180000L, type = "EXPENSE", category = "STAFF", dateMillis = t(Calendar.MAY, 18), referenceNumber = "EXP-2026-006", eventId = eventId2, clientOrVendorName = "StaffPro Agency", isPaid = false))

        cal.set(2026, Calendar.MAY, 25); val tl1 = cal.timeInMillis
        cal.set(2026, Calendar.JUNE, 1); val tl2 = cal.timeInMillis
        db.timelineDao().upsertTimelineItem(TimelineItemEntity(eventId = eventId1, title = "Confirm final headcount", description = "Get confirmed RSVP count from Apex", scheduledDateMillis = tl1, completed = true))
        db.timelineDao().upsertTimelineItem(TimelineItemEntity(eventId = eventId1, title = "AV equipment delivery", description = "SoundWave to deliver and test all equipment", scheduledDateMillis = tl2, completed = false))

        val todayStart = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 9); set(Calendar.MINUTE, 0) }.timeInMillis
        val todayMid = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 14); set(Calendar.MINUTE, 30) }.timeInMillis
        db.agendaDao().upsertAgendaItem(AgendaItemEntity(title = "Call SoundWave AV", description = "Confirm deposit and delivery schedule", scheduledDateMillis = todayStart, eventId = eventId1))
        db.agendaDao().upsertAgendaItem(AgendaItemEntity(title = "Review Meridian budget", description = "Check remaining spend vs. budget", scheduledDateMillis = todayMid, eventId = eventId2))
    }
}