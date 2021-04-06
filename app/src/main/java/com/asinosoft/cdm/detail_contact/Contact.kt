package com.asinosoft.cdm.detail_contact

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.content.ContextCompat
import com.asinosoft.cdm.App
import com.asinosoft.cdm.R
import java.io.IOException

class Contact(
    val id: Long,
    val name: String
) {

    companion object {
        const val MIME_TYPE_PHONE = "vnd.android.cursor.item/phone_v2"
        const val MIME_TYPE_E_MAIL = "vnd.android.cursor.item/email_v2"
        const val MIME_TYPE_WHATSAPP_CALL = "vnd.android.cursor.item/vnd.com.whatsapp.voip.call"
        const val MIME_TYPE_WHATSAPP_VIDEO = "vnd.android.cursor.item/vnd.com.whatsapp.video.call"
        const val MIME_TYPE_VIBER_MSG = "vnd.android.cursor.item/vnd.com.viber.voip.viber_number_message"
        const val MIME_TYPE_TELEGRAM = "vnd.android.cursor.item/vnd.org.telegram.messenger.android.profile"
        const val MIME_TYPE_SKYPE = "vnd.android.cursor.item/com.skype4life.message"
        const val MIME_TYPE_BIRTHDAY = "vnd.android.cursor.item/contact_event"

        const val TYPE_SKYPE = 121
        const val TYPE_WHATSAPP = 122
        const val TYPE_VIBER = 123
        const val TYPE_EMAIL = 124
        const val TYPE_TELEGRAM = 125
        const val TYPE_BIRTHDAY = 126
    }

    var photoUri: String? = null
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

    @Transient
    private var cachedPhoto: Drawable? = null

    fun getPhoto(): Drawable {
        if (null == cachedPhoto) {
            cachedPhoto = photoUri?.let {
                getPhotoByUri(it)
            } ?: defaultPhoto
        }
        return cachedPhoto!!
    }

    private fun getPhotoByUri(photoUri: String): Drawable? {
        return try {
            App.INSTANCE.contentResolver.openAssetFileDescriptor(Uri.parse(photoUri), "r")?.let {
                BitmapDrawable(
                    App.INSTANCE.resources,
                    BitmapFactory.decodeStream(it.createInputStream())
                )
            }
        } catch (e: IOException) {
            null
        }
    }

    private val defaultPhoto: Drawable by lazy {
        ContextCompat.getDrawable(
            App.INSTANCE,
            R.drawable.contact_unfoto
        ) as Drawable
    }

    fun getContactListForDetail(): ArrayList<ContactDetailListElement> {
        val result = ArrayList<ContactDetailListElement>()

        for (i in mPhoneNumbers.indices) {
            val numberStr: String = mPhoneNumbers[i]
            val element = ContactDetailListElement(0, numberStr, mPhoneTypes[i])
            result.add(element)
        }

        if (mWhatsAppNumbers.size > 0) {
            for (i in mWhatsAppNumbers.indices) {
                val numberStr = mWhatsAppNumbers[i]
                val element = ContactDetailListElement(1, numberStr, TYPE_WHATSAPP, mWhatsAppCallId[i], mWhatsAppVideoId[i], mWhatsAppNumbers[i])
                result.add(element)
            }
        }
        if (mViberNumbers.size > 0) {
            for (i in mViberNumbers.indices) {
                val numberStr = mViberNumbers[i]
                val element = ContactDetailListElement(2, numberStr, TYPE_VIBER, mViberId[i], null, mViberNumbers[i])
                result.add(element)
            }
        }
        if (mTelegramId.size > 0) {
            for (i in mTelegramId.indices) {
                val numberStr = mTelegramId[i]
                val element = ContactDetailListElement(3, numberStr, TYPE_TELEGRAM, null, null, mTelegram[i])
                result.add(element)
            }
        }
        if (mSkypeName != null) {
            val numberStr = mSkypeName
            val element = ContactDetailListElement(4, numberStr, TYPE_SKYPE, null, null, name)
            result.add(element)
        }
        if (mEmailAdress.size > 0) {
            for (i in mEmailAdress.indices) {
                val numberStr = mEmailAdress[i]
                val element = ContactDetailListElement(5, numberStr, null, null, null, null, mEmailType[i])
                result.add(element)
            }
        }

        if (mBirthDay.size > 0) {
            for (i in mBirthDay.indices) {
                val numberStr = mBirthDay[i]
                val element = ContactDetailListElement(6, numberStr, TYPE_BIRTHDAY, mAge[i])
                result.add(element)
            }
        }
        return result
    }
}
