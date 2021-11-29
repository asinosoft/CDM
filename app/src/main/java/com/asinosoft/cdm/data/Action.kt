package com.asinosoft.cdm.data

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.asinosoft.cdm.R
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

/**
 * Конкретное действие для конкретного контакта (звонок/смс/письмо/чат и т.д.)
 */
class Action(
    /**
     * ContactsContract.Data._ID
     */
    val id: Int,
    /**
     * Тип
     */
    val type: Type,
    /**
     * Номер телефона/адрес почты/логин и т.д.
     */
    val value: String,
    /**
     * Тип телефона/почты или название приложения (Whatsapp|Viber и т.д.)
     */
    val description: String
) {
    enum class Group(val order: Int) {
        Email(5),
        Phone(0),
        Skype(3),
        Telegram(2),
        Viber(4),
        WhatsApp(1),
    }

    enum class Type(val group: Group) {
        Email(Group.Email),
        PhoneCall(Group.Phone),
        Sms(Group.Phone),
        SkypeCall(Group.Skype),
        SkypeChat(Group.Skype),
        TelegramChat(Group.Telegram),
        TelegramCall(Group.Telegram),
        TelegramVideo(Group.Telegram),
        ViberCall(Group.Viber),
        ViberChat(Group.Viber),
        WhatsAppCall(Group.WhatsApp),
        WhatsAppChat(Group.WhatsApp),
        WhatsAppVideo(Group.WhatsApp),
    }

    companion object {
        fun resourceByType(type: Type): Int {
            return when (type) {
                Type.Email -> R.drawable.ic_email
                Type.PhoneCall -> R.drawable.ic_phone_call
                Type.Sms -> R.drawable.ic_sms
                Type.SkypeCall -> R.drawable.ic_skype_call
                Type.SkypeChat -> R.drawable.ic_skype_chat
                Type.TelegramChat -> R.drawable.ic_telegram_chat
                Type.TelegramCall -> R.drawable.ic_telegram_call
                Type.TelegramVideo -> R.drawable.ic_telegram_video
                Type.ViberCall -> R.drawable.ic_viber_call
                Type.ViberChat -> R.drawable.ic_viber_chat
                Type.WhatsAppCall -> R.drawable.ic_whatsapp_call
                Type.WhatsAppChat -> R.drawable.ic_whatsapp_chat
                Type.WhatsAppVideo -> R.drawable.ic_whatsapp_video
            }
        }
    }

    fun perform(context: Context) {
        when (type) {
            Type.Email -> email(context)
            Type.PhoneCall -> phoneCall(context)
            Type.Sms -> sms(context)
            Type.SkypeCall -> skypeCall(context)
            Type.SkypeChat -> skypeChat(context)
            Type.TelegramCall -> telegramCall(context)
            Type.TelegramChat -> telegramChat(context)
            Type.TelegramVideo -> telegramVideo(context)
            Type.ViberCall -> viberCall(context)
            Type.ViberChat -> viberChat(context)
            Type.WhatsAppCall -> whatsappCall(context)
            Type.WhatsAppChat -> whatsappChat(context)
            Type.WhatsAppVideo -> whatsappVideo(context)
        }
    }

    private fun phoneCall(context: Context) {
        Log.i("CDM|Action", "phoneCall: $value")
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            )
        ) {
            return
        }
        Firebase.analytics.logEvent("action_phone_call", Bundle.EMPTY)

        Intent(Intent.ACTION_CALL, Uri.fromParts("tel", value, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let { context.startActivity(it) }
    }

    private fun sms(context: Context) {
        Log.i("CDM|Action", "sms: $value")
        Firebase.analytics.logEvent("action_phone_sms", Bundle.EMPTY)
        Intent(Intent.ACTION_SENDTO, Uri.parse("sms:$value"))
            .let { context.startActivity(it) }
    }

    private fun email(context: Context) {
        Log.i("CDM|Action", "email: $value")
        Firebase.analytics.logEvent("action_email", Bundle.EMPTY)
        Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$value"))
            .let { context.startActivity(it) }
    }

    private fun skypeChat(context: Context) {
        Log.i("CDM|Action", "skypeChat: $value")
        Firebase.analytics.logEvent("action_skype_chat", Bundle.EMPTY)
        Intent(Intent.ACTION_VIEW, Uri.parse("skype:$value?chat"))
            .let { context.startActivity(it) }
    }

    private fun skypeCall(context: Context) {
        Log.i("CDM|Action", "skypeCall: $value")
        Firebase.analytics.logEvent("action_skype_call", Bundle.EMPTY)
        Intent(Intent.ACTION_VIEW, Uri.parse("skype:$value"))
            .let { context.startActivity(it) }
    }

    private fun telegramChat(context: Context) {
        Log.i("CDM|Action", "telegramChat: $id ($value)")
        Firebase.analytics.logEvent("action_telegram_chat", Bundle.EMPTY)
        Intent(Intent.ACTION_VIEW)
            .setDataAndType(
                Uri.parse("content://com.android.contacts/data/$id"),
                "vnd.android.cursor.item/vnd.org.telegram.messenger.android.profile"
            )
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let { context.startActivity(it) }
    }

    private fun telegramCall(context: Context) {
        Log.i("CDM|Action", "telegramCall: $id ($value)")
        Firebase.analytics.logEvent("action_telegram_call", Bundle.EMPTY)
        Intent(Intent.ACTION_VIEW)
            .setDataAndType(
                Uri.parse("content://com.android.contacts/data/$id"),
                "vnd.android.cursor.item/vnd.org.telegram.messenger.android.call"
            )
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let { context.startActivity(it) }
    }

    private fun telegramVideo(context: Context) {
        Log.i("CDM|Action", "telegramVideo: $id ($value)")
        Firebase.analytics.logEvent("action_telegram_video", Bundle.EMPTY)
        Intent(Intent.ACTION_VIEW)
            .setDataAndType(
                Uri.parse("content://com.android.contacts/data/$id"),
                "vnd.android.cursor.item/vnd.org.telegram.messenger.android.call.video"
            )
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let { context.startActivity(it) }
    }

    private fun viberChat(context: Context) {
        Log.i("CDM|Action", "viberChat: $id ($value)")
        Firebase.analytics.logEvent("action_viber_chat", Bundle.EMPTY)
        Intent(Intent.ACTION_VIEW)
            .setDataAndType(
                Uri.parse("content://com.android.contacts/data/$id"),
                "vnd.android.cursor.item/vnd.com.viber.voip.viber_number_message"
            )
            .setPackage("com.viber.voip")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let { context.startActivity(it) }
    }

    private fun viberCall(context: Context) {
        Log.i("CDM|Action", "viberCall: $id ($value)")
        Firebase.analytics.logEvent("action_viber_call", Bundle.EMPTY)
        Intent(Intent.ACTION_VIEW)
            .setDataAndType(
                Uri.parse("content://com.android.contacts/data/$id"),
                "vnd.android.cursor.item/vnd.com.viber.voip.viber_number_call"
            )
            .setPackage("com.viber.voip")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let { context.startActivity(it) }
    }

    private fun whatsappChat(context: Context) {
        Log.i("CDM|Action", "whatsappChat: $id ($value)")
        Firebase.analytics.logEvent("action_whatsapp_chat", Bundle.EMPTY)
        Intent(Intent.ACTION_VIEW)
            .setDataAndType(
                Uri.parse("content://com.android.contacts/data/$id"),
                "vnd.android.cursor.item/vnd.com.whatsapp.profile"
            )
            .setPackage("com.whatsapp")
            .let { context.startActivity(it) }
    }

    private fun whatsappCall(context: Context) {
        Log.i("CDM|Action", "whatsappCall: $id ($value)")
        Firebase.analytics.logEvent("action_whatsapp_call", Bundle.EMPTY)
        Intent(Intent.ACTION_VIEW)
            .setDataAndType(
                Uri.parse("content://com.android.contacts/data/$id"),
                "vnd.android.cursor.item/vnd.com.whatsapp.voip.call"
            )
            .setPackage("com.whatsapp")
            .let { context.startActivity(it) }
    }

    private fun whatsappVideo(context: Context) {
        Log.i("CDM|Action", "whatsappVideo: $id ($value)")
        Firebase.analytics.logEvent("action_whatsapp_video", Bundle.EMPTY)
        Intent(Intent.ACTION_VIEW)
            .setDataAndType(
                Uri.parse("content://com.android.contacts/data/$id"),
                "vnd.android.cursor.item/vnd.com.whatsapp.video.call"
            )
            .setPackage("com.whatsapp")
            .let { context.startActivity(it) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Action

        if (type != other.type) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}
