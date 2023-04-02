package com.asinosoft.cdm.data

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.asinosoft.cdm.helpers.Converters
import timber.log.Timber
import java.util.*

@Entity
@TypeConverters(Converters::class)
data class Contact(
    @PrimaryKey val id: Long,
    val name: String,
    val photo: String? = null,
    val starred: Boolean = false,
    var birthday: Date? = null
) {
    fun getPhoto(context: Context): Drawable? =
        fromUri(context, photo)

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
}
