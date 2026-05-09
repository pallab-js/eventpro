package com.eventpro.admin.domain.usecase

import com.eventpro.admin.domain.model.Event
import com.eventpro.admin.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCriticalMilestonesUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    operator fun invoke(): Flow<List<Event>> = eventRepository.getCriticalMilestones()
}