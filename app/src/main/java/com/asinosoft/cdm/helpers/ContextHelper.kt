package com.asinosoft.cdm.helpers

import android.app.KeyguardManager
import android.app.NotificationManager
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.Vibrator
import android.provider.Settings
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.TypedValue
import android.view.View
import com.asinosoft.cdm.R
import com.google.android.material.snackbar.Snackbar

inline val Context.audioManager: AudioManager
    get() = getSystemService(AudioManager::class.java)

inline val Context.keyguardManager: KeyguardManager
    get() = getSystemService(KeyguardManager::class.java)

inline val Context.notificationManager: NotificationManager
    get() = getSystemService(NotificationManager::class.java) as NotificationManager

inline val Context.powerManager: PowerManager
    get() = getSystemService(PowerManager::class.java)

inline val Context.roleManager: RoleManager
    get() = getSystemService(RoleManager::class.java)

inline val Context.telecomManager: TelecomManager
    get() = getSystemService(TelecomManager::class.java)

inline val Context.telephonyManager: TelephonyManager
    get() = getSystemService(TelephonyManager::class.java)

inline val Context.vibrator: Vibrator
    get() = getSystemService(Vibrator::class.java)

fun Context.runOnUiThread(f: Context.() -> Unit) {
    if (Looper.getMainLooper() === Looper.myLooper()) f() else ContextHelper.handler.post { f() }
}

private object ContextHelper {
    val handler = Handler(Looper.getMainLooper())
}

fun Context.getThemeColor(attribute: Int): Int {
    val typedValue = TypedValue()
    this.theme.resolveAttribute(attribute, typedValue, true)
    return typedValue.data
}

fun Context.hasNavBar(): Boolean {
    val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
    return id > 0 && resources.getBoolean(id)
}

fun Context.hasPermissions(permissions: Array<String>): Boolean {
    return permissions.all {
        PackageManager.PERMISSION_GRANTED == checkSelfPermission(it)
    }
}

fun Context.navBarHeight(): Int {
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
}

fun Context.requestDrawOverlays(view: View) {
    if (!Settings.canDrawOverlays(this)) {
        Snackbar
            .make(view, R.string.request_draw_overlays, Snackbar.LENGTH_INDEFINITE)
            .setTextColor(Color.WHITE)
            .setActionTextColor(Color.WHITE)
            .setBackgroundTint(Color.MAGENTA)
            .setAction(R.string.ok) {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.fromParts("package", view.context.packageName, null)
                    )
                )
            }
            .show()
    }
}
