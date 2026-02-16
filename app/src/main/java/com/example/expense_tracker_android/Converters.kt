package com.example.expense_tracker_android

import androidx.room.TypeConverter
import java.sql.Date
import java.sql.Time

class Converters {
    @TypeConverter
    fun fromSqlDate(value: Date?): Long? = value?.time

    @TypeConverter
    fun toSqlDate(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun fromSqlTime(value: Time?): Long? = value?.time

    @TypeConverter
    fun toSqlTime(value: Long?): Time? = value?.let { Time(it) }
}

