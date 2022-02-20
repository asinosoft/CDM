package com.asinosoft.cdm.activities

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.* // ktlint-disable no-wildcard-imports
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.asinosoft.cdm.App
import com.asinosoft.cdm.helpers.Metoths
import com.asinosoft.cdm.helpers.getThemeResourceId
import com.asinosoft.cdm.helpers.hasPermissions

/**
 * Базовый клас с поддержкой тем
 */
open class BaseActivity : AppCompatActivity() {
    private var actionWithPermission: (Boolean) -> Unit = {}

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        applyBackgroundImage()
    }

    override fun getTheme(): Resources.Theme {
        return super.getTheme().apply {
            applyStyle(getThemeResourceId(App.instance!!.config.theme), true)
        }
    }

    fun withPermission(permissions: Array<String>, callback: (Boolean) -> Unit) {
        actionWithPermission = {}
        if (hasPermissions(permissions)) {
            callback(true)
        } else {
            actionWithPermission = callback
            ActivityCompat.requestPermissions(this, permissions, 1234)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        actionWithPermission.invoke(grantResults.all { PackageManager.PERMISSION_GRANTED == it })
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
