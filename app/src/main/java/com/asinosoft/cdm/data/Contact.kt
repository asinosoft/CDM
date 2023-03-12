package com.asinosoft.cdm.data

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import com.asinosoft.cdm.helpers.AvatarHelper
import timber.log.Timber
import java.util.*

data class Contact(
    val id: Long,
    val name: String?,
    val phone: String? = null,
    val photo: Uri? = null,
    val starred: Boolean = false,
    val actions: MutableSet<Action> = mutableSetOf()
) {
    companion object {
        fun fromPhone(phone: String): Contact =
            Contact(0, null, phone).apply {
                actions.add(Action(0, Action.Type.PhoneCall, phone, ""))
            }
    }

    val title get() = name ?: phone

    var birthday: Date? = null

    val phones: List<Action> by lazy {
        actions.filter { action -> action.type == Action.Type.PhoneCall }
    }

    val chats: List<Action> by lazy {
        actions.filter { action -> action.type == Action.Type.WhatsAppChat }
    }

    fun getPhoto(context: Context): Drawable? =
        fromUri(context, photo)

    fun getAvatar(context: Context, type: Int): Drawable =
        fromUri(context, photo) ?: generateAvatar(context, type)

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

    private fun generateAvatar(context: Context, type: Int): Drawable {
        return if (null == name) {
            AvatarHelper.generate(context, phone as String, AvatarHelper.IMAGE)
        } else {
            AvatarHelper.generate(context, name, type)
        }
    }
}
