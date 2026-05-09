package com.eventpro.admin.domain.usecase

import com.eventpro.admin.domain.model.Event
import com.eventpro.admin.domain.model.EventStatus
import com.eventpro.admin.ui.events.TimeFilter
import java.util.Calendar

class GetFilteredEventsUseCase @javax.inject.Inject constructor() {
    operator fun invoke(
        events: List<Event>,
        selectedStatus: EventStatus?,
        selectedTimeFilter: TimeFilter,
        searchQuery: String,
        clientNameMap: Map<Long, String>
    ): List<Event> {
        val now = System.currentTimeMillis()
        val monthStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val quarterStart = Calendar.getInstance().apply {
            val month = get(Calendar.MONTH)
            set(Calendar.MONTH, (month / 3) * 3)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return events
            .filter { selectedStatus == null || it.status == selectedStatus }
            .filter {
                when (selectedTimeFilter) {
                    TimeFilter.ALL -> true
                    TimeFilter.THIS_MONTH -> it.startDateMillis >= monthStart
                    TimeFilter.THIS_QUARTER -> it.startDateMillis >= quarterStart
                }
            }
            .filter { ev ->
                searchQuery.isBlank() ||
                ev.title.contains(searchQuery, true) ||
                ev.venueName.contains(searchQuery, true) ||
                clientNameMap[ev.clientId]?.contains(searchQuery, true) == true
            }
    }
}