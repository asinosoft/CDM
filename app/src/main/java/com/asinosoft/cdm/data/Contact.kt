package com.asinosoft.cdm.data

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.asinosoft.cdm.helpers.AvatarHelper
import com.asinosoft.cdm.helpers.Converters
import timber.log.Timber
import java.util.*

@Entity
@TypeConverters(Converters::class)
data class Contact(
    @PrimaryKey val id: Long,
    val name: String?,
    val photo: String? = null,
    val starred: Boolean = false,
    var birthday: Date? = null
) {
    fun getPhoto(context: Context): Drawable? =
        fromUri(context, photo)

    fun getAvatar(context: Context, type: Int): Drawable =
        fromUri(context, photo) ?: generateAvatar(context, type)

    private fun fromUri(context: Context, uri: String?): Drawable? {
        if (null == uri) {
            return null
        }

        return try {
            Drawable.createFromStream(
                context.contentResolver.openInputStream(Uri.parse(uri)),
                uri.toString()
            )
        } catch (ex: Exception) {
            Timber.e(ex)
            null
        }
    }

    private fun generateAvatar(context: Context, type: Int): Drawable {
        return if (null == name) {
            AvatarHelper.generate(context, phone, AvatarHelper.IMAGE)
        } else {
            AvatarHelper.generate(context, name, type)
        }
    }
}
