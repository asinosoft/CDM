package com.asinosoft.cdm.data

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import com.asinosoft.cdm.helpers.AvatarHelper
import timber.log.Timber
import java.util.Date

data class Contact(
    val id: Long,
    val name: String,
) {
    var photoUri: Uri? = null
    var birthday: Date? = null
    var starred = false

    var actions = mutableSetOf<Action>()
    val phones: List<Action> by lazy {
        actions.filter { action -> action.type == Action.Type.PhoneCall }
    }

    val chats: List<Action> by lazy {
        actions.filter { action -> action.type == Action.Type.WhatsAppChat }
    }

    fun getPhoto(context: Context): Drawable? =
        fromUri(context, photoUri)

    fun getAvatar(context: Context, type: Int): Drawable =
        fromUri(context, photoUri) ?: generateAvatar(context, type)

    private fun fromUri(context: Context, uri: Uri?): Drawable? {
        if (null == uri) {
            return null
        }

        return try {
            Drawable.createFromStream(
                context.contentResolver.openInputStream(uri),
                uri.toString()
            )
        } catch (ex: Exception) {
            Timber.e(ex)
            null
        }
    }

    private fun generateAvatar(context: Context, type: Int): Drawable =
        AvatarHelper.generate(context, name, type)
}
