package com.asinosoft.cdm

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PointF
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.ContactsContract
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.annotation.ColorInt
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.Actions.*
import com.asinosoft.cdm.Metoths.Companion.Direction.*
import com.github.tamir7.contacts.Contact
import net.cachapa.expandablelayout.util.FastOutSlowInInterpolator
import org.jetbrains.anko.toast
import org.jetbrains.anko.wrapContent
import java.io.IOException
import kotlin.math.absoluteValue
import kotlin.math.sign
import kotlin.math.withSign

/**
 * Класс важный методов, представляет собой набор низкоуровневых методов.
 */
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

        fun <T : ViewModel, A> singleArgViewModelFactory(constructor: (A) -> T):
                    (A) -> ViewModelProvider.NewInstanceFactory {
            return { arg: A ->
                object : ViewModelProvider.NewInstanceFactory() {
                    @Suppress("UNCHECKED_CAST")
                    override fun <V : ViewModel> create(modelClass: Class<V>): V {
                        return constructor(arg) as V
                    }
                }
            }
        }

        fun ArrayList<HistoryItem>.containsNumber(num: String): Boolean{
            forEach {
                if (it.numberContact == num) return true
            }
            return false
        }

        fun sendMsg(telNum: String, context: Context){
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$telNum"))
            context.startActivity(intent)
        }

        fun sendEmail(email: String, context: Context){
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
            context.startActivity(intent)
        }

        fun skypeCall(userName: String, context: Context){
            val sky = Intent("android.intent.action.VIEW")
            sky.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            sky.data = Uri.parse("skype:$userName")
            Log.d("UTILS", "tel:$userName")
            context.startActivity(sky)
        }

        fun skypeMsg(skName: String, context: Context){
            val sky = Intent("android.intent.action.VIEW")
            sky.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            sky.data = Uri.parse("skype:$skName?chat")
            context.startActivity(sky)
        }

        fun callWhatsApp(id: String, context: Context){
            val intent = Intent().setAction(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse("content://com.android.contacts/data/$id"), "vnd.android.cursor.item/vnd.com.whatsapp.voip.call")
            intent.setPackage("com.whatsapp")
            context.startActivity(intent)
        }
        fun videoCallWhatsApp(id: String, context: Context){
            val intent = Intent().setAction(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse("content://com.android.contacts/data/$id"), "vnd.android.cursor.item/vnd.com.whatsapp.video.call")
            intent.setPackage("com.whatsapp")
            context.startActivity(intent)
        }

        fun openWhatsAppMsg(number: String, context: Context){
            val uri = Uri.parse("smsto:$number")
            val sendIntent = Intent(Intent.ACTION_SENDTO, uri)
            sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            sendIntent.setPackage("com.whatsapp")
            context.startActivity(Intent.createChooser(sendIntent, "").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }

        fun viberMsg(id: String, context: Context){
            val intent = Intent().setAction(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse("content://com.android.contacts/data/$id"), "vnd.android.cursor.item/vnd.com.viber.voip.viber_number_message")
            intent.setPackage("com.viber.voip")
            context.startActivity(intent)
        }

        fun viberCall(number: String, context: Context){
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            number.toUri().let { uri ->
                intent.setDataAndType(uri, "vnd.android.cursor.item/vnd.com.viber.voip.viber_number_call")
                intent.setPackage("com.viber.voip")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }

        }

        /**
         * Возвращает набор букв кнопки клавиатуры по его номеру
         */
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

        enum class Direction {
            LEFT, RIGHT, TOP, DOWN, UNKNOWN
        }

        fun View.makeTouch(action: Int, x: Float = 0f, y: Float = 0f, downTime: Long = SystemClock.uptimeMillis(), eventTime: Long = downTime + 100) {
            this.dispatchTouchEvent(MotionEvent.obtain(downTime, eventTime, action, x, y, 0))
        }

        fun List<Contact>.getFilteredWithNum(num: String): List<Contact> {
            val r = ArrayList<Contact>()
            this.forEach { con ->
                con.phoneNumbers.filter { !it.normalizedNumber.isNullOrEmpty() }
                    .forEach { if (it.normalizedNumber.contains(num)) r.addUnique(con) }
            }
            return r
        }

        fun <E> java.util.ArrayList<E>.addUnique(el: E) {
            if (!this.contains(el)) this.add(el)
        }

        fun MotionEvent.toPointF() = PointF(this.rawX, this.rawY)
        fun View.toPointF() = PointF(this.x, this.y)

        val Int.dp: Int
            get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
        val Float.dp: Int
            get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

        fun View.setSize(height: Int = -1, width: Int = -1){
            this.layoutParams = this.layoutParams.apply { if (height != -1) this.height = height; if (width != -1) this.width = width}
        }

        fun View.setSize(size: Int){
            this.layoutParams = this.layoutParams.apply { width = size; height = size}
        }

        fun View.animateTranslation(cirStart: PointF, pointF: PointF, toPointF: PointF, duration: Long = 0L){
            this.animate()
                .x(cirStart.x - (pointF.x - toPointF.x))
                .y(cirStart.y - (pointF.y - toPointF.y))
                .setDuration(duration)
                .start()
        }

        /**
         * Открыть карточку контакта по его id
         */
        fun openCardContact(idContact: String, context: Context) {
            Intent(Intent.ACTION_VIEW).apply { data = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, idContact) }.let(context::startActivity)
        }

        fun openDetailContact(num: String, context: Context){

            Intent(context, DetailHistoryActivity::class.java).apply {
                putExtra(Keys.number, num)
            }.let(context::startActivity)
        }

        fun View.translateDiff(cirStart: PointF, diff: PointF, duration: Long = 0L){
            this.animate()
                .x(cirStart.x - (diff.x))
                .y(cirStart.y - (diff.y))
                .setDuration(duration)
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
        }

        fun View.translateTo(toPointF: PointF, duration: Long = 0L){
            this.animate()
                .x(toPointF.x)
                .y(toPointF.y)
                .setDuration(duration)
                .start()
        }

        fun View.translateTo(view: View, offset: Float = 0f, duration: Long = 0L){
            this.animate()
                .x(view.x + offset)
                .y(view.y + offset)
                .setDuration(duration)
                .start()
        }

        fun View.setTranslate(toPointF: PointF, duration: Long = 0L){
            this.animate()
                .x(toPointF.x)
                .y(toPointF.y)
                .setDuration(duration)
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
        }


        fun PointF.diff(pointF: PointF) = PointF(this.x - pointF.x, this.y - pointF.y)
        fun View.diff(pointF: PointF) = PointF(this.x - pointF.x, this.y - pointF.y)
        fun PointF.diff(event: MotionEvent, radius: Float? = null, dropX: Boolean = false, dropY: Boolean = false): PointF {
            val pointF = event.toPointF()
            var r = PointF(this.x - pointF.x, this.y - pointF.y)
            radius?.let {r.set(if(r.x.absoluteValue > it) it * r.x.sign else r.x, if(r.y.absoluteValue > it) it * r.y.sign else r.y) }
            if (dropX) r.x = 0f
            if (dropY) r.y = 0f
            if (r.x.absoluteValue > r.y.absoluteValue) r.y = 0f
            if (r.x.absoluteValue <= r.y.absoluteValue) r.x = 0f
            return r
        }

        fun PointF.checkMoving(radius: Float) = this.x.absoluteValue <= radius && this.y.absoluteValue <= radius

        fun PointF.diffVisible(radius: Float) = x / radius * x.sign > 0.7f || y / radius * y.sign > 0.7f
        fun PointF.diffAction(radius: Float): Direction {
            val diffX = x / radius
            val diffY = y / radius
            return if (diffX.absoluteValue > 0.7f) if (x.sign < 0) RIGHT else LEFT
            else if (diffY.absoluteValue > 0.7f) if (y.sign < 0) DOWN else TOP
            else UNKNOWN
        }

        fun DirectActions.action(direction: Direction) = when (direction){
                LEFT -> this.left
                RIGHT -> this.right
                TOP -> this.top
                DOWN -> this.down
                UNKNOWN -> null
        }

        fun ImageView.setImageAction(actions: Actions){
            this.setImageResource(when(actions){
                WhatsApp -> R.drawable.whatsapp_192
                Viber -> R.drawable.viber
                Telegram -> R.drawable.telegram
                PhoneCall -> R.drawable.telephony_call_192
                Email -> R.drawable.email_192
                Sms -> R.drawable.sms_192
            })
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
            ValueAnimator.ofInt(this.measuredHeight, if (this.height == 1) wrapContent else 1).apply {
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

        fun Vibrator.vibrateSafety(ms: Long){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.EFFECT_HEAVY_CLICK))
            }else vibrate(ms)
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

        fun getFormatedTime(duration: Int): String {
            var str = "0:00"
            try {
                str = "${duration / 60}:${duration % 60}"
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
                return
            }
            context.startActivity(intent)
        }

        fun sendSMS(phone: String, context: Context) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phone, null)))
        }

        fun mailToEmail(email: String, context: Context) {
            val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null))
            context.startActivity(Intent.createChooser(emailIntent, "Send email..."))
        }


        fun openTelegram(phone: String, context: Context) {
            val telegram =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/InfotechAvl_bot"))
            context.startActivity(telegram)
        }

        fun openTelegramNow(id: String, context: Context){
            val isAppInstalled = appInstalledOrNot("org.telegram.messenger", context)
            if (isAppInstalled) {
                val intent = Intent().setAction(Intent.ACTION_VIEW)
                intent.setDataAndType(Uri.parse("content://com.android.contacts/data/$id"), "vnd.android.cursor.item/vnd.org.telegram.messenger.android.profile")
                context.startActivity(intent)
            } else {
                context.toast("Ошибка! Telegram не установлен!")
            }
            Log.e("Action: ", "Telegram open!")
        }

        fun openViber(phone: String, context: Context) {
            val intent = Intent("android.intent.action.VIEW")
            intent.setClassName("com.viber.voip", "com.viber.voip.WelcomeActivity")
            intent.data = "tel:$phone".toUri()
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