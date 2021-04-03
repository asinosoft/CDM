package com.asinosoft.cdm.detail_contact

import android.util.Log
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

object StHelper {

    fun convertNumber(number: String): String {
        var numberStr: String
        try {
            val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
            val numberForm: Phonenumber.PhoneNumber? = phoneUtil.parse(number, "RU")
            numberStr = phoneUtil.format(numberForm, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
        } catch (e: Exception) {
            numberStr = "-1"
            Log.e(
                "myLogHelper",
                e.message ?: "null error message"
            )
        }
        return numberStr
    }

    fun parseDateToddMMyyyy(time: String?): String? {
        val inputPattern = "yyyy-MM-dd"
        val outputPattern = "dd MMM yyyy г."
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
}
