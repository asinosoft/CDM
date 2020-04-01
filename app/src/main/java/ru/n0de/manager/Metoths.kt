package ru.n0de.manager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import org.jetbrains.anko.activityManager
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast
import java.io.Serializable

class Metoths {

    companion object {

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