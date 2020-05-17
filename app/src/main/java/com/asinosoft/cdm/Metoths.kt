package com.asinosoft.cdm

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.opengl.Visibility
import android.provider.ContactsContract
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.ColorInt
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.tamir7.contacts.Contact
import org.jetbrains.anko.toast
import java.io.IOException

class Metoths {

    companion object {
        /**
         * Паттерны кнопок клавиатуры (Массив букв под клавишами).
         * @param nums Используемые цифры.
         * @param context Контекст.
         * @return Готовый паттерн кнопок клавиуты для регулярных выражений.
         */
        fun getPattern(nums: String, context: Context): String {
            var r = ""
            nums.forEach {
                getWords(it, context).replace("\n", "").let {words ->
                    if (words.isNotBlank()) r = r.plus("(?:[$words])")
                }
            }
            return r
        }

        private fun getWords(it: Char, context: Context): String {
            return when (it){
//                '0' -> context.getString(R.string.digit_zero_text)
//                '1' -> context.getString(R.string.digit_one_text)
                '2' -> context.getString(R.string.digit_two_text)
                '3' -> context.getString(R.string.digit_three_text)
                '4' -> context.getString(R.string.digit_four_text)
                '5' -> context.getString(R.string.digit_five_text)
                '6' -> context.getString(R.string.digit_six_text)
                '7' -> context.getString(R.string.digit_seven_text)
                '8' -> context.getString(R.string.digit_eight_text)
                '9' -> context.getString(R.string.digit_nine_text)
                else -> ""
            }
        }


        fun List<Contact>.getFiltered(text: String): List<Contact> {
            val r = ArrayList<Contact>()
            this.forEach { con ->
                con.phoneNumbers.filter { !it.normalizedNumber.isNullOrEmpty() }
                    .forEach { if (it.normalizedNumber.contains(text)) r.addUnique(con) }
            }
            return r
        }

        fun <E> java.util.ArrayList<E>.addUnique(el: E) {
            if (!this.contains(el)) this.add(el)
        }


        fun View.setSize(height: Int = -1, width: Int = -1){
            this.layoutParams = this.layoutParams.apply { if (height != -1) this.height = height; if (width != -1) this.width = width}
        }

        fun Boolean.toVisibility(gone: Boolean = false) = if (this) View.VISIBLE else if (!gone) View.INVISIBLE else View.GONE

        fun TextView.plus(text: String){
            this.text = this.text.toString().plus(text)
        }

        fun TextView.setColoredText(text: String, @ColorInt color: Int = Color.BLUE){
            SpannableString(this.text).apply {setSpan(ForegroundColorSpan(color), this.indexOf(text), this.indexOf(text) +
                    text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)}.let { this.text = it }
        }

        fun String.setColorText(text: String, @ColorInt color: Int = Color.BLUE) =
            SpannableString(this).apply {setSpan(ForegroundColorSpan(color), this.indexOf(text), this.indexOf(text) +
                    text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)}

        fun View.toggle(duration: Long = 500L){
            ValueAnimator.ofInt(this.measuredHeight, if (this.height == 1) 0 else 1).apply {
                this.duration = duration
                addUpdateListener { layoutParams = layoutParams.apply { height = animatedValue as Int } }
            }.start()
        }

        fun getPhoto(contactId: Long, context: Context): Bitmap? {
            val contactUri =
                ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
            val displayPhotoUri = Uri.withAppendedPath(contactUri,
                ContactsContract.Contacts.Photo.DISPLAY_PHOTO
            )
            try {
                val fd = context.contentResolver.openAssetFileDescriptor(displayPhotoUri, "r")
                return BitmapFactory.decodeStream(fd!!.createInputStream())
            } catch (e: IOException) {
                return null
            }

        }

        fun getFormatedTime(duration: String): String {
            var str = "0:00"
            try {
                str = "${duration.toInt() / 60}:${duration.toInt() % 60}"
            } catch (e: Exception) {
                Log.e("Exception ", e.message ?: "")
            }
            return str
        }

        @SuppressLint("MissingPermission")
        fun callPhone(telNum: String, context: Context){
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$telNum"))
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            context.startActivity(intent)
        }

        fun openWhatsApp(num: String, context: Context) {
            val isAppInstalled = appInstalledOrNot("com.whatsapp", context)
            if (isAppInstalled) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$num"))
                context.startActivity(intent)
            } else {
                context.toast("Ошибка! WhatsApp не установлен!")
            }
            Log.e("Action: ", "WhatsApp open!")
        }

        private fun appInstalledOrNot(uri: String, context: Context): Boolean {
            val pm = context.packageManager
            return try {
                pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }
}