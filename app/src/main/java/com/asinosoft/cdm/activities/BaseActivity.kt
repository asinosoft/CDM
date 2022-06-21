package com.asinosoft.cdm.activities

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.asinosoft.cdm.App
import com.asinosoft.cdm.helpers.Metoths
import com.asinosoft.cdm.helpers.getThemeResourceId
import timber.log.Timber

/**
 * Базовый клас с поддержкой тем
 */
open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("THEME = %d", App.instance!!.config.theme)
        setTheme(getThemeResourceId(App.instance!!.config.theme))
    }

    override fun onResume() {
        super.onResume()
        applyBackgroundImage()
    }

    private fun applyBackgroundImage() {
        val image = getBackgroundImage()
        val rootView = findViewById<ViewGroup>(android.R.id.content).rootView
        if (null == image) {
            val backgroundColor = Metoths.getThemeColor(this, android.R.attr.colorBackground)
            rootView.setBackgroundColor(backgroundColor)
        } else {
            rootView.background = image
        }
    }

    private fun getBackgroundImage(): Drawable? {
        return try {
            App.instance!!.config.background?.let { uri ->
                contentResolver.openInputStream(uri).use {
                    scaleToScreen(BitmapFactory.decodeStream(it))
                }
            }
        } catch (ex: Exception) {
            null
        }
    }

    private fun scaleToScreen(bitmap: Bitmap): BitmapDrawable {
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(background)

        val scale: Float = (width.toFloat() / bitmap.width)
            .coerceAtLeast(height.toFloat() / bitmap.height)
        val xTranslation: Float = (width - bitmap.width * scale) / 2.0f
        val yTranslation: Float = (height - bitmap.height * scale) / 2.0f

        val transformation = Matrix()
        transformation.postTranslate(xTranslation, yTranslation)
        transformation.preScale(scale, scale)

        val paint = Paint()
        paint.isFilterBitmap = true

        val backgroundColor = Metoths.getThemeColor(this, android.R.attr.colorBackground)

        canvas.drawColor(backgroundColor)
        canvas.drawBitmap(bitmap, transformation, paint)

        return BitmapDrawable(resources, background)
    }
}
