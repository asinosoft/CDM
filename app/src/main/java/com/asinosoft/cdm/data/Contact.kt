package com.asinosoft.cdm.data

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.content.ContextCompat
import com.asinosoft.cdm.R
import com.asinosoft.cdm.helpers.AvatarHelper
import timber.log.Timber

data class Contact(
    val id: Long,
    val name: String?,
    val phone: String? = null
) {
    companion object {
        fun fromPhone(phone: String): Contact =
            Contact(0, null, phone).apply {
                actions.add(Action(0, Action.Type.PhoneCall, phone, ""))
            }
    }

    var photoUri: Uri? = null
    var birthday: String? = null
    var age: Int = 0
    var starred = false

    var actions = mutableSetOf<Action>()
    val phones: List<Action> by lazy {
        actions.filter { action -> action.type == Action.Type.PhoneCall }
    }

    val chats: List<Action> by lazy {
        actions.filter { action -> action.type == Action.Type.WhatsAppChat }
    }

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

    private fun generateAvatar(context: Context, type: Int): Drawable {
        return if (null == name) {
            //ContextCompat.getDrawable(context,R.drawable.ic_add_contact_new) as Drawable
            AvatarHelper.generate(context, phone as String, 3)
        } else {
            AvatarHelper.generate(context, name, type)
        }
    }
}
