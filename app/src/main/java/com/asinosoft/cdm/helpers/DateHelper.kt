package com.asinosoft.cdm.helpers

import java.text.SimpleDateFormat
import java.util.*

object DateHelper {
    /**
     * Возвращает количество полных лет на текущий момент времени
     */
    fun age(date: Date): Int {
        val today = Calendar.getInstance()
        val anniversary = Calendar.getInstance(); anniversary.time = date

        var age = 0
        anniversary.add(Calendar.YEAR, 1)
        while (!anniversary.after(today)) {
            age++
            anniversary.add(Calendar.YEAR, 1)
        }

        return age
    }

    fun shortDate(date: Date): String {
        return SimpleDateFormat("d MMM", Locale.getDefault()).format(date).trimEnd('.')
    }

    fun time(date: Date): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    }
}
