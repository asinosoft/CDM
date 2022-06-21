package com.asinosoft.cdm.helpers

import java.text.SimpleDateFormat
import java.util.*

object DateHelper {
    fun shortDate(date: Date): String {
        return SimpleDateFormat("d MMM", Locale.getDefault()).format(date).trimEnd('.')
    }

    fun time(date: Date): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    }
}
