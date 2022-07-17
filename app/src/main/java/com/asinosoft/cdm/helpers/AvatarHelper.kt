package com.asinosoft.cdm.helpers

import android.content.Context
import android.graphics.*
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.TextPaint
import androidx.core.content.ContextCompat
import androidx.core.graphics.blue
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.green
import androidx.core.graphics.red
import org.jetbrains.annotations.NotNull


/**
 * Created by Korir https://github.com/AmosKorir/AvatarImageGenerator
 */
class AvatarHelper {

    companion object {
        fun generate(context: Context, name: String, type: Int): BitmapDrawable {// type 1- 2буквы, 2 - 5 букв, 3-картика
            val size = 192
            val fontSize: Int
            if (type==1) {
                fontSize = 30
            }
            else {
                fontSize = 16
            }

            val backgroundColor = getBackgroundColor(name)

            val areaRect = Rect(0, 0, size, size)
            val bitmap = Bitmap.createBitmap(size, size, ARGB_8888)
            val canvas = Canvas(bitmap)
            val painter = Paint().apply { isAntiAlias = true }
            val textPaint = TextPaint().apply {
                isAntiAlias = true
                textSize = fontSize * context.resources.displayMetrics.scaledDensity
            }

            painter.color = backgroundColor
            canvas.drawRect(areaRect, painter)

            var label = firstCharacter(name,type)
            val bounds = RectF(areaRect)
            bounds.right = textPaint.measureText(label, 0, label.length)
            bounds.bottom = textPaint.descent() - textPaint.ascent()
            bounds.left += (size - bounds.right) / 2.0f
            bounds.top += (size - bounds.bottom) / 2.0f

            painter.color = Color.TRANSPARENT
            canvas.drawCircle(size.toFloat() / 2, size.toFloat() / 2, size.toFloat() / 2, painter)

            textPaint.color = getTextColor(backgroundColor)

            if (type==3) {
                val icon = ContextCompat.getDrawable(context, com.asinosoft.cdm.R.drawable.ic_add_contact_new_white) as Drawable
                icon.setBounds(65, 50, 147, 137)
                icon.draw(canvas)
                icon.intrinsicHeight
                icon.intrinsicWidth

            }
            else {
                    canvas.drawText(label, bounds.left, bounds.top - textPaint.ascent(), textPaint)
            }

            return BitmapDrawable(context.resources, bitmap)
        }



        private fun firstCharacter(name: String, type: Int): String {
            return if (type==1) {
                    name.take(2)
                }
                else {
                    name.take(6)
                }
        }

        private fun getBackgroundColor(name: String): Int =
                name.hashCode() and 0xffffff or 0xff000000.toInt()

        private fun getTextColor(backgroundColor: Int): Int =
            if ((backgroundColor.red + backgroundColor.green + backgroundColor.blue) / 3 >= 128) Color.DKGRAY else Color.WHITE
    }

}
