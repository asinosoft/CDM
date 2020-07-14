package com.asinosoft.cdm.detail_contact

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import kotlin.collections.ArrayList

class Contact () {

    val MIME_TYPE_PHONE = "vnd.android.cursor.item/phone_v2"
    val MIME_TYPE_E_MAIL = "vnd.android.cursor.item/email_v2"
    val MIME_TYPE_WHATSAPP_CALL = "vnd.android.cursor.item/vnd.com.whatsapp.voip.call"
    val MIME_TYPE_WHATSAPP_VIDEO = "vnd.android.cursor.item/vnd.com.whatsapp.video.call"
    val MIME_TYPE_VIBER_MSG = "vnd.android.cursor.item/vnd.com.viber.voip.viber_number_message"
    val MIME_TYPE_TELEGRAM = "vnd.android.cursor.item/vnd.org.telegram.messenger.android.profile"
    val MIME_TYPE_SKYPE = "vnd.android.cursor.item/com.skype4life.message"
    val MIME_TYPE_BIRTHDAY = "vnd.android.cursor.item/contact_event"

    val TYPE_SKYPE = 121
    val TYPE_WHATSAPP = 122
    val TYPE_VIBER = 123
    val TYPE_EMAIL = 124
    val TYPE_TELEGRAM = 125
    val TYPE_BIRTHDAY = 126

    var name: String? = null
    var contactID: String? = null
    var mSkypeName: String? = null
    var mEmailAdress = ArrayList<String>()
    var mPhoneNumbers = ArrayList<String>()
    var mPhoneTypes = ArrayList<Int>()
    var mEmailType = ArrayList<Int>()
    var mWhatsAppNumbers = ArrayList<String>()
    var mWhatsAppNumbers2 = ArrayList<String>()
    var mWhatsAppCallId = ArrayList<String>()
    var mWhatsAppVideoId = ArrayList<String>()
    var mViberNumbers = ArrayList<String>()
    var mViberId = ArrayList<String>()
    var mTelegram = ArrayList<String>()
    var mTelegramId = ArrayList<String>()
    var mBirthDay = ArrayList<String>()
    var mBirthDayType = ArrayList<Int>()
    var mAge = ArrayList<String>()


    fun parseDataCursor(id: String, context: Context){
         var isFirst: Boolean = true

         val cursor: Cursor = context.contentResolver.query(ContactsContract.Data.CONTENT_URI, null,
            ContactsContract.Data.CONTACT_ID + "=?", arrayOf(id),null)!!

        cursor.moveToFirst()
        while(!cursor.isAfterLast()){
             val _id =
                 cursor.getString(cursor.getColumnIndex(ContactsContract.Data._ID))
             val contactId =
                 cursor.getString(cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID))
             val displayName =
                 cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME))
             val mimeType =
                 cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE))
             val data =
                 cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA1))
             val data2 =
                 cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA2))
             val data4 =
                 cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA4))
             val data14 =
                 cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA14))

            Log.d("myLogParseDataCursor", _id + " " + displayName + " " + mimeType + " contact id = "
                        + contactID + " data = " + data + " data4 = " + data4 + " data14 = " + data14
                        + " data2 = " + data2 )

            if(isFirst){
                isFirst = false
                name = displayName
                contactID = contactId
            }

             when(mimeType){
                 MIME_TYPE_PHONE -> getNumberFormData(data, data4, data2)
                 MIME_TYPE_E_MAIL -> getEmailFromData(data, data2)
                 MIME_TYPE_WHATSAPP_CALL -> getWhatsAppCallFromData(_id, data)
                 MIME_TYPE_WHATSAPP_VIDEO -> getWhatsAppVideoFromData(_id, data)
                 MIME_TYPE_VIBER_MSG -> getViberFromData(_id, data)
                 MIME_TYPE_TELEGRAM -> getTelegramFromData(_id, data)
                 MIME_TYPE_SKYPE -> getSkypeFromData(data)
                 MIME_TYPE_BIRTHDAY -> getBithdayFromData(data, data2)
             }
             cursor.moveToNext()
        }
    }

    private fun getNumberFormData(data: String, data4: String, data2: String){
        if(null != data){
             if(null != data4){
                 val stHelper = StHelper()
                 val numberStr: String = stHelper.convertNumber(data)
                 if(!isHasEqualNumber(numberStr)){
                     mPhoneNumbers.add(numberStr)
                     if(null != data2){
                         val type: Int = Integer.parseInt(data2)
                         mPhoneTypes.add(type)
                     }else mPhoneTypes.add(-1)
                 }
             }
        }else{
            if(!isHasEqualNumber(data)){
                mPhoneNumbers.add(data)
                 if(null != data2){
                     val type: Int = Integer.parseInt(data2)
                     mPhoneTypes.add(type)
                 }else this.mPhoneTypes.add(-1)
            }
        }
    }

    private fun getEmailFromData(data: String, data2: String){
        if(null != data){
            this.mEmailAdress.add(data)
            if(null != data2){
                val type = Integer.parseInt(data2)
                this.mEmailType.add(type)
            }else this.mEmailType.add(-1)
        }
    }

    private fun getBithdayFromData(data: String, data2: String){
        if(null != data){
            val stHelper = StHelper()
            val date = stHelper.parseDateToddMMyyyy(data)
            val date1 = stHelper.parseToMillis(data)
            mBirthDay.add(date!!)
            mAge.add(date1!!)
            if(null != data2){
                val type = Integer.parseInt(data2)
                this.mBirthDayType.add(type)
            }else this.mBirthDayType.add(-1)
        }
    }

    private fun getSkypeFromData(data: String?) {
        if (data != null) {
            this.mSkypeName = data
        }
    }

    private fun getWhatsAppCallFromData(id: String, data: String){
        if((id != null) && (data != null)){
            val number: String = getNumberStr(data)
            var isNumberWasAdded = false
            for (tNumber in this.mWhatsAppNumbers) {
                if (tNumber == number) {
                    isNumberWasAdded = true
                }
            }
            if (!isNumberWasAdded) {
                this.mWhatsAppCallId.add(id)
                this.mWhatsAppNumbers.add(getNumberStr(data))
            }
        }
    }

    private fun getWhatsAppVideoFromData(id: String, data: String){
        if((id != null) && (data != null)){
            val number: String = getNumberStr(data)
            var isNumberWasAdded = false
            for (tNumber in this.mWhatsAppNumbers2) {
                if (tNumber == number) {
                    isNumberWasAdded = true
                }
            }
            if (!isNumberWasAdded) {
                this.mWhatsAppVideoId.add(id)
                this.mWhatsAppNumbers2.add(getNumberStr(data))
            }
        }
    }

    private fun getViberFromData(id: String, data: String?) {
        if (data != null) {
            val number = getNumberStr(data)
            var isNumberWasAdded = false
            for (tNumber in this.mViberNumbers) {
                if (tNumber == number) {
                    isNumberWasAdded = true
                }
            }
            if (!isNumberWasAdded) {
                Log.e("myLogParseDataCursor", "viber add number = $number")
                this.mViberId.add(id)
                this.mViberNumbers.add(getNumberStr(data))
            }
        }
    }

    private fun getTelegramFromData(id: String, data: String){
        if (data != null) {
            val number = getNumberStr(data)
            var isNumberWasAdded = false
            for (tNumber in this.mTelegram) {
                if (tNumber == number) {
                    isNumberWasAdded = true
                }
            }
            if (!isNumberWasAdded) {
                Log.e("myLogParseDataCursor", "viber add number = $number")
                this.mTelegramId.add(id)
                this.mTelegram.add(getNumberStr(data))
            }
        }
    }

    private fun isHasEqualNumber(number: String): Boolean {
        for (numb in mPhoneNumbers) {
            if (numb == number) return true
        }
        return false
    }

    private fun getNumberStr(data: String): String {
        val index = data.indexOf("@")
        return if (-1 != index) {
            val res = data.split("@".toRegex()).toTypedArray()
            res[0]
        } else data
    }


    
    fun getContactListForDetail(): ArrayList<ContactDetailListElement>{

        var result = ArrayList<ContactDetailListElement>()

        for (i in mPhoneNumbers.indices) {
            val numberStr: String = mPhoneNumbers[i]
            val element = ContactDetailListElement(0, numberStr, mPhoneTypes[i])
            result.add(element)
        }

        if(mWhatsAppNumbers.size > 0){
            for(i in mWhatsAppNumbers.indices){
                val numberStr = mWhatsAppNumbers[i]
                val element = ContactDetailListElement(1, numberStr, TYPE_WHATSAPP, mWhatsAppCallId[i], mWhatsAppVideoId[i], mPhoneNumbers[i])
                result.add(element)
            }
        }
        if(mViberNumbers.size > 0){
            for(i in mViberNumbers.indices){
                val numberStr = mViberNumbers[i]
                val element = ContactDetailListElement(2, numberStr, TYPE_VIBER, mViberId[i], null, mPhoneNumbers[i])
                result.add(element)
            }
        }
        if(mTelegramId.size > 0){
            for(i in mTelegramId.indices){
                val numberStr = mTelegramId[i]
                val element = ContactDetailListElement(3, numberStr, TYPE_TELEGRAM, null, null, mPhoneNumbers[i])
                result.add(element)
            }
        }
        if(mSkypeName != null){
            val numberStr = mSkypeName
            val element = ContactDetailListElement(4, numberStr, TYPE_SKYPE, null, null, name)
            result.add(element)
        }
        if(mEmailAdress.size > 0){
            for(i in mEmailAdress.indices){
                val numberStr = mEmailAdress[i]
                val element = ContactDetailListElement(5, numberStr, null, null, null, null, mEmailType[i])
                result.add(element)
            }
        }

        if(mBirthDay.size > 0){
            for(i in mBirthDay.indices){
                val numberStr = mBirthDay[i]
                val element = ContactDetailListElement(6, numberStr, TYPE_BIRTHDAY, mAge[i])
                result.add(element)
            }
        }
        return result
    }
}
