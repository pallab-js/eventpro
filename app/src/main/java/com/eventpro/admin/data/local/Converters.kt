package com.eventpro.admin.data.local

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter fun fromTimestamp(v: Long?): Date? = v?.let { Date(it) }
    @TypeConverter fun dateToTimestamp(d: Date?): Long? = d?.time
}
