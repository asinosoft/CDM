package com.asinosoft.cdm.db

import android.net.Uri
import androidx.room.TypeConverter
import java.util.*

class Converters {
    @TypeConverter
    fun toUri(value: String?): Uri? = value?.let { Uri.parse(it) }

    @TypeConverter
    fun fromUri(value: Uri?): String? = value?.toString()

    @TypeConverter
    fun toDate(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun fromDate(value: Date?): Long? = value?.time
}
