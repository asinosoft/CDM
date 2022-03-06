package com.asinosoft.cdm.helpers

import androidx.annotation.RequiresApi
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.util.* // ktlint-disable no-wildcard-imports

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

    /**
     * Возвращает количество полных лет на текущий момент времени
     */
    fun getAge(birthday: String): Int {
        try {
            // После апгрейда до SDK-26 можно заменить на LocalDate + Period
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(birthday)
                ?.let { date ->
                    val today = Calendar.getInstance()
                    val anniversary = Calendar.getInstance(); anniversary.time = date

                    var age = 0
                    anniversary.add(Calendar.YEAR, 1)
                    while (!anniversary.after(today)) {
                        age++
                        anniversary.add(Calendar.YEAR, 1)
                    }

                    return age
                } ?: 0
        } catch (e: Exception) {
            return 0
        }
    }

    @RequiresApi(26)
    fun getAgeSdk26(birthday: String): Int {
        return try {
            Period.between(LocalDate.parse(birthday), LocalDate.now()).years
        } catch (e: Exception) {
            0
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
