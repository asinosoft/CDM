package com.asinosoft.cdm.activities

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.Loader
import com.asinosoft.cdm.helpers.Keys
import com.asinosoft.cdm.helpers.Metoths

/**
 * Базовый клас с поддержкой тем
 */
open class BaseActivity : AppCompatActivity() {
    private var appTheme: Int = R.style.AppTheme_Light

    override fun onCreate(savedInstanceState: Bundle?) {
        appTheme = Loader.loadSettings(this).theme
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        val image = getBackgroundImage()
        val rootView = findViewById<ViewGroup>(android.R.id.content).rootView
        if (null == image) {
            val backgroundColor = Metoths.getThemeColor(this, android.R.attr.colorBackground)
            rootView.setBackgroundColor(backgroundColor)
        } else {
            rootView.background = image
        }
    }

    override fun getTheme(): Resources.Theme {
        return super.getTheme().apply {
            applyStyle(appTheme, true)
        }
    }

    private fun getBackgroundImage(): Drawable? {
        return getSharedPreferences(Keys.Preference, Context.MODE_PRIVATE)
            .getString(Keys.BACKGROUND_IMAGE, null)
            ?.let {
                contentResolver.openAssetFileDescriptor(Uri.parse(it), "r")?.let {
                    BitmapFactory.decodeStream(
                        it.createInputStream(),
                    ).let {
                        scaleToScreen(it)
                    }
                }
            }
    }

    private fun scaleToScreen(bitmap: Bitmap): BitmapDrawable {
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(background)

        val scale: Float = height.toFloat() / bitmap.height
        val xTranslation: Float = (width - bitmap.width * scale) / 2.0f
        val yTranslation = 0.0f

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
