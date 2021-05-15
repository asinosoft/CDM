package com.asinosoft.cdm.data

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.content.ContextCompat
import com.asinosoft.cdm.R
import java.io.IOException

data class Contact(
    val id: Long,
    val name: String
) {
    var photoUri: String? = null
    var birthday: String? = null
    var age: String? = null

    var actions = mutableSetOf<Action>()
    val phones: List<Action> by lazy {
        actions.filter { action -> action.type == Action.Type.PhoneCall }
    }

    val chats: List<Action> by lazy {
        actions.filter { action -> action.type == Action.Type.WhatsAppChat }
    }

    private var cachedPhoto: Drawable? = null

    fun getPhoto(context: Context): Drawable {
        if (null == cachedPhoto) {
            cachedPhoto = photoUri?.let {
                getPhotoByUri(context, it)
            } ?: getDefaultPhoto(context)
        }
        return cachedPhoto!!
    }

    private fun getPhotoByUri(context: Context, photoUri: String): Drawable? {
        return try {
            context.contentResolver.openAssetFileDescriptor(Uri.parse(photoUri), "r")?.let {
                BitmapDrawable(
                    context.resources,
                    BitmapFactory.decodeStream(it.createInputStream())
                )
            }
        } catch (e: IOException) {
            null
        }
    }

    private fun getDefaultPhoto(context: Context): Drawable =
        ContextCompat.getDrawable(
            context,
            R.drawable.contact_unfoto
        ) as Drawable
}
