package com.asinosoft.cdm.api

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.content.ContextCompat
import com.asinosoft.cdm.App
import com.asinosoft.cdm.R
import com.asinosoft.cdm.data.*
import java.io.IOException

class Contact(
    val id: Long,
    val name: String
) {
    var photoUri: String? = null

    // Полный список контактов
    var items = mutableListOf<ContactItem>()

    // Список контактов, разбитый по типам
    var birthday: ContactItem? = null
    var emails = mutableListOf<EmailItem>()
    var phones = mutableListOf<PhoneItem>()
    var skypes = mutableListOf<SkypeItem>()
    var telegrams = mutableListOf<TelegramItem>()
    var vibers = mutableListOf<ViberItem>()
    var whatsapps = mutableListOf<WhatsAppItem>()

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
}
