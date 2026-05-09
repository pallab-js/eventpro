package com.eventpro.admin.domain.usecase

import com.eventpro.admin.domain.model.Event
import com.eventpro.admin.domain.repository.EventRepository
import javax.inject.Inject

class SaveEventUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    sealed class Result {
        data class Success(val eventId: Long) : Result()
        data class ValidationError(val field: String, val message: String) : Result()
    }

    suspend operator fun invoke(event: Event, existingId: Long?): Result {
        if (event.title.isBlank()) return Result.ValidationError("title", "Title is required")
        return Result.Success(eventRepository.upsertEvent(event))
    }
}