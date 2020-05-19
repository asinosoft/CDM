package com.asinosoft.cdm

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import org.jetbrains.anko.toast


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

        fun sendMsg(telNum: String, context: Context){
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$telNum"))
            context.startActivity(intent)
        }

        fun sendEmail(email: String, context: Context){
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
            context.startActivity(intent)
        }

        fun callWhatsApp(id: Int, context: Context){
            val intent = Intent().setAction(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse("content://com.android.contacts/data/$id"), "vnd.android.cursor.item/vnd.com.whatsapp.voip.call")
            intent.setPackage("com.whatsapp")
            context.startActivity(intent)
        }
        fun videoCallWhatsApp(id: Int, context: Context){
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

        fun viberMsg(id: Int, context: Context){
            val intent = Intent().setAction(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse("content://com.android.contacts/data/$id"), "vnd.android.cursor.item/vnd.com.viber.voip.viber_number_message")
            intent.setPackage("com.viber.voip")
            context.startActivity(intent)
        }

        fun viberCall(uri: Uri, context: Context){
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            if (null != uri) {
                intent.setDataAndType(uri, "vnd.android.cursor.item/vnd.com.viber.voip.viber_number_call")
                intent.setPackage("com.viber.voip")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }

        }

        fun openTelegram(id: String, context: Context){
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