package com.asinosoft.cdm.dialer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.telecom.TelecomManager
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.asinosoft.cdm.R
import org.jetbrains.annotations.NotNull
import timber.log.Timber
import java.util.*

class Utilities {

    val DEFAULT_DIALER_RC = 11
    val PERMISSION_RC = 10
    val MUST_HAVE_PERMISSIONS = arrayOf(Manifest.permission.CALL_PHONE)
    lateinit var sLocale: Locale
    val LONG_VIBRATE_LENGTH: Long = 500
    val SHORT_VIBRATE_LENGTH: Long = 20
    val DEFAULT_VIBRATE_LENGTH: Long = 100

    @RequiresApi(Build.VERSION_CODES.N)
    fun setUpLocale(@NotNull context: Context) {
        Utilities().sLocale = context.resources.configuration.locales.get(0)
    }

    fun hasNavBar(context: Context): Boolean {
        val resources = context.resources
        val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return id > 0 && resources.getBoolean(id)
    }

    fun navBarHeight(context: Context): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    fun toggleViewActivation(view: View) {
        view.isActivated = !view.isActivated
    }

    fun getAccentColor(context: Context): Int {
        val typedValue = TypedValue()
        val a = context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.secondaryAccentColor))
        val color = a.getColor(0, 0)
        a.recycle()
        return color
    }

    fun checkDefaultDialer(activity: FragmentActivity): Boolean {
        val packageName = activity.application.packageName
        return try {
            if (activity.getSystemService(TelecomManager::class.java).defaultDialerPackage != packageName) {
                // Prompt the user with a dialog to select this app to be the default phone app
                val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                    .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                activity.startActivityForResult(intent, DEFAULT_DIALER_RC)
                return false
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun checkPermissionsGranted(grantResults: IntArray): Boolean {
        for (result in grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) return false
        }
        return true
    }

    fun checkPermissionsGranted(context: Context?, permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (!checkPermissionsGranted(context, permissions)) return false
        }
        return true
    }

    fun askForPermissions(activity: FragmentActivity?, permissions: Array<String>) {
        ActivityCompat.requestPermissions(activity!!, permissions!!, PERMISSION_RC)
    }

    fun vibrate(@NotNull context: Context, millis: Long) {
        val vibrator: Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if(vibrator == null) {
            return
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
        }else{
            vibrator.vibrate(millis)
        }
    }

    fun vibrate(@NotNull context:Context){
        vibrate(context, DEFAULT_VIBRATE_LENGTH)
    }

    fun inViewInBounds(view: View, x: Int, y: Int, buttonVicinityOffset: Int): Boolean {
        var outRect = Rect()
        val location = IntArray(2)
        view.getDrawingRect(outRect)
        view.getLocationOnScreen(location)
        outRect.offset(location[0], location[1])
        val e = convertDpToPixel(view.context, buttonVicinityOffset.toFloat()) as Int
        outRect = Rect(outRect.left - e, outRect.top - e, outRect.right + e, outRect.bottom + e)
        Timber.d("outRect: %s, x and y: %d, %d", outRect.toShortString(), x, y)
        return outRect.contains(x, y)
    }

    fun convertDpToPixel(context: Context?, dp: Float): Float {
        return dp * (dpi(context!!) / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun dpi(context: Context): Float {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.densityDpi.toFloat()
    }

}