package com.asinosoft.cdm.helpers

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import java.text.SimpleDateFormat
import java.util.*

object StHelper {
    private val clearNonNumbers =
        Regex("\\D+") // чтобы не компилировать регексп на каждый вызов функции

    fun convertNumber(number: String, country: String? = null): String {
        return try {
            val clearNumber = clearNonNumbers.replace(number, "")
            val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
            val numberForm: Phonenumber.PhoneNumber? =
                phoneUtil.parse(clearNumber, country ?: Locale.getDefault().country)
            if (phoneUtil.isPossibleNumber(numberForm))
                phoneUtil.format(numberForm, PhoneNumberUtil.PhoneNumberFormat.E164)
            else
                clearNumber
        } catch (e: Exception) {
            number
        }
    }

    fun parseDateToddMMyyyy(time: String): String {
        try {
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(time)?.let { date ->
                return SimpleDateFormat("dd MMMM yyyy г.", Locale.getDefault()).format(date)
            } ?: ""
        } catch (e: Exception) {
            return ""
        }
    }

    fun parseToYears(time: String): Int {
        try {
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(time)?.let { date ->
                val dateToMillis = System.currentTimeMillis()
                val ageInMillis = dateToMillis - date.time
                val age = ageInMillis / 1000 / 60 / 60 / 24 / 366
                return age.toInt()
            } ?: 0
        } catch (e: Exception) {
            return 0
        }
    }

    fun today(): Date {
        val now = Calendar.getInstance()
        now.set(Calendar.HOUR, 0)
        now.set(Calendar.MINUTE, 0)
        now.set(Calendar.SECOND, 0)
        now.set(Calendar.MILLISECOND, 0)
        return now.time
    }
}
