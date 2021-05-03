package com.asinosoft.cdm.data

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.asinosoft.cdm.R
import timber.log.Timber

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
                Type.PhoneCall -> R.drawable.telephony_call_192
                Type.Sms -> R.drawable.ic_sms
                Type.SkypeCall -> R.drawable.skype_call
                Type.SkypeChat -> R.drawable.skype_message
                Type.TelegramChat -> R.drawable.ic_telegram
                Type.ViberCall -> R.drawable.ic_viber_call
                Type.ViberChat -> R.drawable.ic_viber_chat
                Type.WhatsAppCall -> R.drawable.whatsapp_call
                Type.WhatsAppChat -> R.drawable.whatsapp_192
                Type.WhatsAppVideo -> R.drawable.whatsapp_call
            }
        }
    }

    fun perform(context: Context) {
        if (0 == id) {
            Toast.makeText(context, "%s отсутствует".format(type.name), Toast.LENGTH_LONG).show()
        } else when (type) {
            Type.Email -> email(context)
            Type.PhoneCall -> phoneCall(context)
            Type.Sms -> sms(context)
            Type.SkypeCall -> skypeCall(context)
            Type.SkypeChat -> skypeChat(context)
            Type.TelegramChat -> telegramChat(context)
            Type.ViberCall -> viberCall(context)
            Type.ViberChat -> viberChat(context)
            Type.WhatsAppCall -> whatsappCall(context)
            Type.WhatsAppChat -> whatsappChat(context)
            Type.WhatsAppVideo -> whatsappVideo(context)
        }
    }

    private fun phoneCall(context: Context) {
        Timber.i("phoneCall: %s", value)
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            )
        ) {
            return
        }
        Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Uri.encode(value)))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let { context.startActivity(it) }
    }

    private fun sms(context: Context) {
        Timber.i("sendMsg: %s", value)
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$value"))
        context.startActivity(intent)
    }

    private fun email(context: Context) {
        Timber.i("sendEmail: %s", value)
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$value"))
        context.startActivity(intent)
    }

    private fun skypeChat(context: Context) {
        Timber.i("skypeCall: %s", value)
        val intent = Intent("android.intent.action.VIEW", Uri.parse("skype:$value?chat"))
        context.startActivity(intent)
    }

    private fun skypeCall(context: Context) {
        Timber.i("skypeCall: %s", value)
        val intent = Intent("android.intent.action.VIEW", Uri.parse("skype:$value"))
        context.startActivity(intent)
    }

    private fun telegramChat(context: Context) {
        Timber.i("openTelegramNow: %s", id)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(
            Uri.parse("content://com.android.contacts/data/$id"),
            "vnd.android.cursor.item/vnd.org.telegram.messenger.android.profile"
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun telegramCall(context: Context) {
        TODO()
    }

    private fun telegramVideo(context: Context) {
        TODO()
    }

    private fun viberChat(context: Context) {
        val intent = Intent()
            .setAction(Intent.ACTION_VIEW)
            .setDataAndType(
                Uri.parse("content://com.android.contacts/data/$id"),
                "vnd.android.cursor.item/vnd.com.viber.voip.viber_number_message"
            )
            .setPackage("com.viber.voip")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun viberCall(context: Context) {
        val intent = Intent()
            .setAction(Intent.ACTION_VIEW)
            .setDataAndType(
                Uri.parse("content://com.android.contacts/data/$id"),
                "vnd.android.cursor.item/vnd.com.viber.voip.viber_number_call"
            )
            .setPackage("com.viber.voip")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun whatsappChat(context: Context) {
        val intent = Intent().setAction(Intent.ACTION_VIEW)
        intent.setDataAndType(
            Uri.parse("content://com.android.contacts/data/$id"),
            "vnd.android.cursor.item/vnd.com.whatsapp.profile"
        )
        intent.setPackage("com.whatsapp")
        context.startActivity(intent)
    }

    private fun whatsappCall(context: Context) {
        val intent = Intent().setAction(Intent.ACTION_VIEW)
        intent.setDataAndType(
            Uri.parse("content://com.android.contacts/data/$id"),
            "vnd.android.cursor.item/vnd.com.whatsapp.voip.call"
        )
        intent.setPackage("com.whatsapp")
        context.startActivity(intent)
    }

    private fun whatsappVideo(context: Context) {
        val intent = Intent().setAction(Intent.ACTION_VIEW)
        intent.setDataAndType(
            Uri.parse("content://com.android.contacts/data/$id"),
            "vnd.android.cursor.item/vnd.com.whatsapp.video.call"
        )
        intent.setPackage("com.whatsapp")
        context.startActivity(intent)
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
