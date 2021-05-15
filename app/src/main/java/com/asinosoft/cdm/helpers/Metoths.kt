package com.asinosoft.cdm.helpers

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.res.Resources
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
import android.view.MotionEvent
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import com.asinosoft.cdm.R
import com.asinosoft.cdm.helpers.Metoths.Companion.Direction.*
import com.asinosoft.cdm.activities.DetailHistoryActivity
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.data.DirectActions
import net.cachapa.expandablelayout.util.FastOutSlowInInterpolator
import org.jetbrains.anko.wrapContent
import timber.log.Timber
import kotlin.math.absoluteValue
import kotlin.math.sign

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
                getWords(it, context).replace("\n", "").let { words ->
                    if (words.isNotBlank()) r = r.plus("(?:[$words])")
                }
            }
            return r
        }

        /**
         * Возвращает набор букв кнопки клавиатуры по его номеру
         */
        private fun getWords(it: Char, context: Context): String {
            return when (it) {
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

        fun View.makeTouch(
            action: Int,
            x: Float = 0f,
            y: Float = 0f,
            downTime: Long = SystemClock.uptimeMillis(),
            eventTime: Long = downTime + 100
        ) {
            this.dispatchTouchEvent(MotionEvent.obtain(downTime, eventTime, action, x, y, 0))
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

        fun View.setSize(height: Int = -1, width: Int = -1) {
            this.layoutParams = this.layoutParams.apply {
                if (height != -1) this.height = height; if (width != -1) this.width = width
            }
        }

        fun View.setSize(size: Int) {
            this.layoutParams = this.layoutParams.apply { width = size; height = size }
        }

        fun View.animateTranslation(
            cirStart: PointF,
            pointF: PointF,
            toPointF: PointF,
            duration: Long = 0L
        ) {
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
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, idContact)
            }.let(context::startActivity)
        }

        fun openDetailContact(num: String, contact: Contact, context: Context) {
            Timber.i("Окно контакта: [%s] %s (%s)", contact.id, contact.name, num)
            Intent(context, DetailHistoryActivity::class.java).apply {
                putExtra(Keys.number, num)
                putExtra(Keys.id, contact.id)
            }.let(context::startActivity)
        }

        fun View.translateDiff(cirStart: PointF, diff: PointF, duration: Long = 0L) {
            this.animate()
                .x(cirStart.x - (diff.x))
                .y(cirStart.y - (diff.y))
                .setDuration(duration)
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
        }

        fun View.translateTo(view: View, offset: Float = 0f, duration: Long = 0L) {
            this.animate()
                .x(view.x + offset)
                .y(view.y + offset)
                .setDuration(duration)
                .start()
        }

        fun View.setTranslate(toPointF: PointF, duration: Long = 0L) {
            this.animate()
                .x(toPointF.x)
                .y(toPointF.y)
                .setDuration(duration)
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
        }

        fun PointF.diff(
            event: MotionEvent,
            radius: Float? = null,
            dropX: Boolean = false,
            dropY: Boolean = false
        ): PointF {
            val pointF = event.toPointF()
            var r = PointF(this.x - pointF.x, this.y - pointF.y)
            radius?.let {
                r.set(
                    if (r.x.absoluteValue > it) it * r.x.sign else r.x,
                    if (r.y.absoluteValue > it) it * r.y.sign else r.y
                )
            }
            if (dropX) r.x = 0f
            if (dropY) r.y = 0f
            if (r.x.absoluteValue > r.y.absoluteValue) r.y = 0f
            if (r.x.absoluteValue <= r.y.absoluteValue) r.x = 0f
            return r
        }

        fun PointF.checkMoving(radius: Float) =
            this.x.absoluteValue <= radius && this.y.absoluteValue <= radius

        fun PointF.diffVisible(radius: Float) =
            x / radius * x.sign > 0.7f || y / radius * y.sign > 0.7f

        fun PointF.diffAction(radius: Float): Direction {
            val diffX = x / radius
            val diffY = y / radius
            return if (diffX.absoluteValue > 0.7f) if (x.sign < 0) RIGHT else LEFT
            else if (diffY.absoluteValue > 0.7f) if (y.sign < 0) DOWN else TOP
            else UNKNOWN
        }

        fun DirectActions.action(direction: Direction) = when (direction) {
            LEFT -> this.left
            RIGHT -> this.right
            TOP -> this.top
            DOWN -> this.down
            UNKNOWN -> null
        }

        fun ImageView.setImageAction(action: Action.Type) {
            this.setImageResource(
                Action.resourceByType(action)
            )
        }

        fun Boolean.toVisibility(gone: Boolean = false) =
            if (this) View.VISIBLE else if (!gone) View.INVISIBLE else View.GONE

        fun TextView.setColoredText(text: String, @ColorInt color: Int = Color.BLUE) {
            SpannableString(this.text).apply {
                setSpan(
                    ForegroundColorSpan(color),
                    this.indexOf(text),
                    this.indexOf(text) + text.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }.let { this.text = it }
        }

        fun View.toggle(duration: Long = 500L, animation: Boolean = false) {
            if (animation) {
                ValueAnimator.ofInt(this.measuredHeight, if (this.height == 1) wrapContent else 1)
                    .apply {
                        this.duration = duration
                        addUpdateListener {
                            layoutParams = layoutParams.apply { height = animatedValue as Int }
                        }
                    }.start()
            } else {
                if (isVisible) {
                    visibility = INVISIBLE
                } else {
                    visibility = VISIBLE
                }
            }
        }

        fun Vibrator.vibrateSafety(ms: Long) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.EFFECT_HEAVY_CLICK))
            } else vibrate(ms)
        }

        fun getFormattedTime(duration: Long): String {
            var str = "0:00"
            try {
                str = "${duration / 60}:${duration % 60}"
            } catch (e: Exception) {
                Timber.e(e)
            }
            return str
        }
    }
}
