package com.asinosoft.cdm.data

/**
 * Контактный телефон/мейл или иной способ
 */
open class ContactItem(
    val type: Type,
    val value: String,
) {
    enum class Type {
        BIRTHDAY {
            override fun mimeType(): String = "vnd.android.cursor.item/contact_event"
        },
        EMAIL {
            override fun mimeType(): String = "vnd.android.cursor.item/email_v2"
        },
        PHONE {
            override fun mimeType(): String = "vnd.android.cursor.item/phone_v2"
        },
        SKYPE {
            override fun mimeType(): String = "vnd.android.cursor.item/com.skype4life.message"
        },
        TELEGRAM {
            override fun mimeType(): String =
                "vnd.android.cursor.item/vnd.org.telegram.messenger.android.profile"
        },
        VIBER {
            override fun mimeType(): String =
                "vnd.android.cursor.item/vnd.com.viber.voip.viber_number_message"
        },
        WHATSAPP {
            override fun mimeType(): String = "vnd.android.cursor.item/vnd.com.whatsapp.profile"
        };

        abstract fun mimeType(): String

        companion object {
            /**
             * Определяет тип контакта по mime-метке
             */
            fun mimeToType(mimeType: String): Type? =
                when (mimeType) {
                    "vnd.android.cursor.item/contact_event" -> BIRTHDAY
                    "vnd.android.cursor.item/email_v2" -> EMAIL
                    "vnd.android.cursor.item/phone_v2" -> PHONE
                    "vnd.android.cursor.item/com.skype4life.message" -> SKYPE
                    "vnd.android.cursor.item/vnd.org.telegram.messenger.android.profile" -> TELEGRAM
                    "vnd.android.cursor.item/vnd.org.telegram.messenger.android.call.video" -> TELEGRAM
                    "vnd.android.cursor.item/vnd.org.telegram.messenger.android.call" -> TELEGRAM
                    "vnd.android.cursor.item/vnd.com.viber.voip.viber_number_message" -> VIBER
                    "vnd.android.cursor.item/vnd.com.whatsapp.profile" -> WHATSAPP
                    "vnd.android.cursor.item/vnd.com.whatsapp.voip.call" -> WHATSAPP
                    "vnd.android.cursor.item/vnd.com.whatsapp.video.call" -> WHATSAPP
                    else -> null
                }
        }
    }
}
