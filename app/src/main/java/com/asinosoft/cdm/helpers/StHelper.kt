package com.asinosoft.cdm.helpers

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.YEAR

object StHelper {
    private val clearNonNumbers = Regex("\\D+") // чтобы не компилировать регексп на каждый вызов функции

    fun convertNumber(number: String): String {
        return try {
            val clearNumber = clearNonNumbers.replace(number, "")
            val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
            val numberForm: Phonenumber.PhoneNumber? = phoneUtil.parse(clearNumber, "RU")
            phoneUtil.format(numberForm, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
        } catch (e: Exception) {
            number
        }
    }

    fun parseDateToddMMyyyy(time: String?): String? {
        val inputPattern = "yyyy-MM-dd"
        val outputPattern = "dd MMMM yyyy г."
        val inputFormat = SimpleDateFormat(inputPattern)
        val outputFormat = SimpleDateFormat(outputPattern)
        var date: Date? = null
        var str: String? = null
        try {
            date = inputFormat.parse(time)
            str = outputFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return str
    }

    fun parseToMillis(time: String?): String {
        val str_date = time
        val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val date = formatter.parse(str_date) as Date
        val dateToMillis = System.currentTimeMillis()
        val ageInMillis = dateToMillis - date.time
        val age = ageInMillis / 1000 / 60 / 60 / 24 / 366
        return age.toString()
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
