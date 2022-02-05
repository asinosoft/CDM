package com.asinosoft.cdm.api

import android.os.Bundle
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

/**
 * Интеграция с сервисом сбора статистики использования приложения
 */
class Analytics {
    companion object {
        fun logActivityContact() =
            Firebase.analytics.logEvent("activity_contact", Bundle.EMPTY)

        fun logActivitySearch() =
            Firebase.analytics.logEvent("activity_search", Bundle.EMPTY)

        fun logActivitySettings() =
            Firebase.analytics.logEvent("activity_settings", Bundle.EMPTY)

        fun logActionEmail() =
            Firebase.analytics.logEvent("action_email", Bundle.EMPTY)

        fun logActionPhoneCall() =
            Firebase.analytics.logEvent("action_phone_call", Bundle.EMPTY)

        fun logActionPhoneSms() =
            Firebase.analytics.logEvent("action_phone_sms", Bundle.EMPTY)

        fun logActionSkypeChat() =
            Firebase.analytics.logEvent("action_skype_chat", Bundle.EMPTY)

        fun logActionSkypeCall() =
            Firebase.analytics.logEvent("action_skype_call", Bundle.EMPTY)

        fun logActionTelegramChat() =
            Firebase.analytics.logEvent("action_telegram_chat", Bundle.EMPTY)

        fun logActionTelegramCall() =
            Firebase.analytics.logEvent("action_telegram_call", Bundle.EMPTY)

        fun logActionTelegramVideo() =
            Firebase.analytics.logEvent("action_telegram_video", Bundle.EMPTY)

        fun logActionViberChat() =
            Firebase.analytics.logEvent("action_viber_chat", Bundle.EMPTY)

        fun logActionViberCall() =
            Firebase.analytics.logEvent("action_viber_call", Bundle.EMPTY)

        fun logActionWhatsappChat() =
            Firebase.analytics.logEvent("action_whatsapp_chat", Bundle.EMPTY)

        fun logActionWhatsappCall() =
            Firebase.analytics.logEvent("action_whatsapp_call", Bundle.EMPTY)

        fun logActionWhatsappVideo() =
            Firebase.analytics.logEvent("action_whatsapp_video", Bundle.EMPTY)

        fun logBackground() =
            Firebase.analytics.logEvent("background", Bundle.EMPTY)

        fun logCallFromSearch() =
            Firebase.analytics.logEvent("call_from_search", Bundle.EMPTY)

        fun logCallHistoryClick() =
            Firebase.analytics.logEvent("call_history_click", Bundle.EMPTY)

        fun logContactDetailsTab() =
            Firebase.analytics.logEvent("contact_details_tab", Bundle.EMPTY)

        fun logContactHistoryTab() =
            Firebase.analytics.logEvent("contact_history_tab", Bundle.EMPTY)

        fun logContactSettingsTab() =
            Firebase.analytics.logEvent("contact_settings_tab", Bundle.EMPTY)

        fun logContactSetAction(direction: String, action: String) =
            Firebase.analytics.logEvent(
                "contact_set_action",
                Bundle().apply {
                    putString("direction", direction)
                    putString("action", action)
                }
            )

        fun logDefaultDialer() =
            Firebase.analytics.logEvent("default_dialer", Bundle.EMPTY)

        fun logCheckDefaultDialer(default: Boolean) =
            Firebase.analytics.logEvent(
                "check_default_dialer",
                Bundle().apply { putBoolean("default", default) }
            )

        fun logDoubleCall() =
            Firebase.analytics.logEvent("double_call", Bundle.EMPTY)

        fun logFavoriteAction(direction: String) =
            Firebase.analytics.logEvent("favorite_action_$direction", Bundle.EMPTY)

        fun logFavoriteAdd(position: Int) =
            Firebase.analytics.logEvent(
                "favorite_add",
                Bundle().apply { putInt("position", position) }
            )

        fun logFavoriteClick() =
            Firebase.analytics.logEvent("favorite_click", Bundle.EMPTY)

        fun logFavoriteLongClick() =
            Firebase.analytics.logEvent("favorite_long_click", Bundle.EMPTY)

        fun logFavoritePlus() =
            Firebase.analytics.logEvent("favorite_plus", Bundle.EMPTY)

        fun logFavoriteRemove(position: Int) =
            Firebase.analytics.logEvent(
                "favorite_remove",
                Bundle().apply { putInt("position", position) }
            )

        fun logFavoriteSwap() =
            Firebase.analytics.logEvent("favorite_swap", Bundle.EMPTY)

        fun logFavoritesBorderColor() =
            Firebase.analytics.logEvent("favorites_border_color", Bundle.EMPTY)

        fun logFavoritesBorderWidth(width: Int) =
            Firebase.analytics.logEvent(
                "favorites_border_width",
                Bundle().apply { putInt("width", width) }
            )

        fun logFavoritesCount(count: Int) =
            Firebase.analytics.logEvent(
                "favorites_count",
                Bundle().apply { putInt("count", count) }
            )

        fun logFavoritesPosition(position: String) =
            Firebase.analytics.logEvent(
                "favorites_position",
                Bundle().apply { putString("position", position) }
            )

        fun logFavoritesSize(size: Int) =
            Firebase.analytics.logEvent("favorites_size", Bundle().apply { putInt("size", size) })

        fun logGlobalSetAction(direction: String, action: String) =
            Firebase.analytics.logEvent(
                "global_set_action",
                Bundle().apply {
                    putString("direction", direction)
                    putString("action", action)
                }
            )

        fun logHistorySwipeLeft() =
            Firebase.analytics.logEvent("history_swipe_left", Bundle.EMPTY)

        fun logHistorySwipeRight() =
            Firebase.analytics.logEvent("history_swipe_right", Bundle.EMPTY)

        fun logContactHistorySwipeLeft() =
            Firebase.analytics.logEvent("contact_history_swipe_left", Bundle.EMPTY)

        fun logContactHistorySwipeRight() =
            Firebase.analytics.logEvent("contact_history_swipe_right", Bundle.EMPTY)

        fun logKeyboardButton() =
            Firebase.analytics.logEvent("keyboard_button", Bundle.EMPTY)

        fun logLoadCallHistory() =
            Firebase.analytics.logEvent("load_call_history", Bundle.EMPTY)

        fun logSearchKeyboardClose() =
            Firebase.analytics.logEvent("search_keyboard_close", Bundle.EMPTY)

        fun logSearchKeyboardClear() =
            Firebase.analytics.logEvent("search_keyboard_clear", Bundle.EMPTY)

        fun logSearchKeyboardDel() =
            Firebase.analytics.logEvent("search_keyboard_del", Bundle.EMPTY)

        fun logSearchSwipeLeft() =
            Firebase.analytics.logEvent("search_swipe_left", Bundle.EMPTY)

        fun logSearchSwipeRight() =
            Firebase.analytics.logEvent("search_swipe_right", Bundle.EMPTY)

        fun logSettingsOutfitTab() =
            Firebase.analytics.logEvent("settings_outfit_tab", Bundle.EMPTY)

        fun logSettingsActionTab() =
            Firebase.analytics.logEvent("settings_action_tab", Bundle.EMPTY)

        fun logSettingsDialerTab() =
            Firebase.analytics.logEvent("settings_dialer_tab", Bundle.EMPTY)

        fun logSettingsAboutTab() =
            Firebase.analytics.logEvent("settings_about_tab", Bundle.EMPTY)



        fun logTheme(theme: Int) =
            Firebase.analytics.logEvent("theme", Bundle().apply { putInt("theme", theme) })
    }
}
