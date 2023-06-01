package com.asinosoft.cdm.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.asinosoft.cdm.R
import com.asinosoft.cdm.activities.CallActivity
import com.asinosoft.cdm.api.Analytics
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

    fun perform(context: Context, sim: Int = 0) {
        when (type) {
            Type.Email -> email(context)
            Type.PhoneCall -> phoneCall(context, sim)
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

    private fun phoneCall(context: Context, sim: Int = 0) {
        Timber.i("phoneCall: %s", value)
        Analytics.logActionPhoneCall()
        Intent(context, CallActivity::class.java)
            .setData(Uri.fromParts("tel", value, null))
            .putExtra("sim", sim)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let { context.startActivity(it) }
    }

    private fun sms(context: Context) {
        Timber.i("sms: %s", value)
        Analytics.logActionPhoneSms()
        Intent(Intent.ACTION_SENDTO, Uri.parse("sms:$value"))
            .let { context.startActivity(it) }
    }

    private fun email(context: Context) {
        Timber.i("email: %s", value)
        Analytics.logActionEmail()
        Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$value"))
            .let { context.startActivity(it) }
    }

    private fun skypeChat(context: Context) {
        Timber.i("skypeChat: %s", value)
        Analytics.logActionSkypeChat()
        Intent(Intent.ACTION_VIEW, Uri.parse("skype:$value?chat"))
            .let { context.startActivity(it) }
    }

    private fun skypeCall(context: Context) {
        Timber.i("skypeCall: %s", value)
        Analytics.logActionSkypeCall()
        Intent(Intent.ACTION_VIEW, Uri.parse("skype:$value"))
            .let { context.startActivity(it) }
    }

    private fun telegramChat(context: Context) {
        Timber.i("telegramChat: %s (%s)", id, value)
        Analytics.logActionTelegramChat()
        Intent(Intent.ACTION_VIEW)
            .setDataAndType(
                Uri.parse("content://com.android.contacts/data/$id"),
                "vnd.android.cursor.item/vnd.org.telegram.messenger.android.profile"
            )
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let { context.startActivity(it) }
    }

    private fun telegramCall(context: Context) {
        Timber.i("telegramCall: %s (%s)", id, value)
        Analytics.logActionTelegramCall()
        Intent(Intent.ACTION_VIEW)
            .setDataAndType(
                Uri.parse("content://com.android.contacts/data/$id"),
                "vnd.android.cursor.item/vnd.org.telegram.messenger.android.call"
            )
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let { context.startActivity(it) }
    }

    private fun telegramVideo(context: Context) {
        Timber.i("telegramVideo: %s (%s)", id, value)
        Analytics.logActionTelegramVideo()
        Intent(Intent.ACTION_VIEW)
            .setDataAndType(
                Uri.parse("content://com.android.contacts/data/$id"),
                "vnd.android.cursor.item/vnd.org.telegram.messenger.android.call.video"
            )
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let { context.startActivity(it) }
    }

    private fun viberChat(context: Context) {
        Timber.i("viberChat: %s (%s)", id, value)
        Analytics.logActionViberChat()
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
        Timber.i("viberCall: %s (%s)", id, value)
        Analytics.logActionViberCall()
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
        Timber.i("whatsappChat: %s (%s)", id, value)
        Analytics.logActionWhatsappChat()
        if (id == 0) {
            value.substring(1, value.length - 1)
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$value"))
            with(context) {
                startActivity(intent)
            }
        } else {
            Intent(Intent.ACTION_VIEW)
                .setDataAndType(
                    Uri.parse("content://com.android.contacts/data/$id"),
                    "vnd.android.cursor.item/vnd.com.whatsapp.profile"
                )
                .setPackage("com.whatsapp")
                .let { context.startActivity(it) }
        }
    }

    private fun whatsappCall(context: Context) {
        Timber.i("whatsappCall: %s (%s)", id, value)
        Analytics.logActionWhatsappCall()
        Intent(Intent.ACTION_VIEW)
            .setDataAndType(
                Uri.parse("content://com.android.contacts/data/$id"),
                "vnd.android.cursor.item/vnd.com.whatsapp.voip.call"
            )
            .setPackage("com.whatsapp")
            .let { context.startActivity(it) }
    }

    private fun whatsappVideo(context: Context) {
        Timber.i("whatsappVideo: %s (%s)", id, value)
        Analytics.logActionWhatsappVideo()
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

    override fun toString() = value
}
