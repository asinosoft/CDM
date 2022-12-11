package com.asinosoft.cdm.activities

import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.asinosoft.cdm.App
import com.asinosoft.cdm.helpers.getThemeColor
import com.asinosoft.cdm.helpers.getThemeResourceId
import com.asinosoft.cdm.helpers.hasPermissions
import timber.log.Timber

/**
 * Базовый клас с поддержкой тем
 */
open class BaseActivity : AppCompatActivity() {
    private var onPermissionGranted: () -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("THEME = %d", App.instance!!.config.theme)
        setTheme(getThemeResourceId(App.instance!!.config.theme))
    }

    override fun onResume() {
        super.onResume()
        applyBackgroundImage()
    }

    fun withPermission(permission: String, callback: () -> Unit) {
        val permissions = arrayOf(permission)
        if (hasPermissions(permissions)) {
            callback()
        } else {
            onPermissionGranted = callback
            ActivityCompat.requestPermissions(this, permissions, 1234)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.all { PackageManager.PERMISSION_GRANTED == it }) {
            onPermissionGranted.invoke()
        }
    }

    private fun applyBackgroundImage() {
        val image = getBackgroundImage()
        val rootView = findViewById<ViewGroup>(android.R.id.content).rootView
        if (null == image) {
            val backgroundColor = getThemeColor(android.R.attr.colorBackground)
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

        val backgroundColor = getThemeColor(android.R.attr.colorBackground)

        canvas.drawColor(backgroundColor)
        canvas.drawBitmap(bitmap, transformation, paint)

        return BitmapDrawable(resources, background)
    }
}
