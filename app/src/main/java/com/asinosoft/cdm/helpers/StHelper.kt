package com.asinosoft.cdm.helpers

import androidx.annotation.RequiresApi
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import java.time.LocalDate
import java.time.Period
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
        now.set(Calendar.AM_PM, Calendar.AM)
        now.set(Calendar.HOUR, 0)
        now.set(Calendar.MINUTE, 0)
        now.set(Calendar.SECOND, 0)
        now.set(Calendar.MILLISECOND, 0)
        return now.time
    }
}
