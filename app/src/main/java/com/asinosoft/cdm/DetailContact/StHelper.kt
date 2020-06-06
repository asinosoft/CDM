package com.asinosoft.cdm.DetailContact

import android.util.Log
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber

class StHelper {

    fun convertNumber(number: String): String{
        var numberStr: String
        try {
            val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
            val numberForm: Phonenumber.PhoneNumber? = phoneUtil.parse(number, "RU")
            numberStr = phoneUtil.format( numberForm , PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL )
        } catch (e: Exception) {
            numberStr = "-1"
            Log.e(
                "myLogHelper",
                if (e.message != null) e.message else "null error message"
            )
        }
        return numberStr
    }



}