package com.asinosoft.cdm.api

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import androidx.core.net.toUri
import com.asinosoft.cdm.R
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.data.DirectActions
import com.google.gson.Gson
import java.io.File
import kotlin.math.roundToInt

class ConfigImpl(private val context: Context) : Config {
    private val settings = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private enum class Keys {
        THEME,

        CHECK_DEFAULT_DIALER,

        FAVORITES_LAYOUT,
        FAVORITES_COLUMNS_COUNT,
        FAVORITES_SIZE,
        FAVORITES_BORDER_COLOR,
        FAVORITES_BORDER_WIDTH,

        SWIPE_LEFT_ACTION,
        SWIPE_RIGHT_ACTION,
        SWIPE_UP_ACTION,
        SWIPE_DOWN_ACTION,

        LIST_DIVIDER,
    }

    /**
     * Дефолтный размер аватарки избранного контакта
     */
    private val defaultFavoritesSize: Int by lazy {
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        (screenWidth / 5.0).roundToInt()
    }

    override var isChanged = false

    override var theme: Int
        get() = settings.getInt(Keys.THEME.name, R.style.AppTheme_Light)
        set(theme) = settings.edit().apply {
            Analytics.logTheme(theme)
            putInt(Keys.THEME.name, theme)
            // При смене темы сбрасываем цвет рамки аватара на дефолтный
            remove(Keys.FAVORITES_BORDER_COLOR.name)
        }.apply()

    override var background: Uri?
        get() = File(context.filesDir, "background").let {
            if (it.isFile) it.toUri() else null
        }
        set(uri) {
            if (null == uri) {
                context.deleteFile("background")
            } else {
                context.contentResolver.openInputStream(uri)?.copyTo(
                    File(context.filesDir, "background").outputStream()
                )

                // Переключаемся в специальную тёмную тему
                theme = 3
            }
            Analytics.logBackground()
            isChanged = true
        }

    override var checkDefaultDialer: Boolean
        get() = settings.getBoolean(Keys.CHECK_DEFAULT_DIALER.name, true)
        set(value) = settings.edit().putBoolean(Keys.CHECK_DEFAULT_DIALER.name, value).apply()
            .also { isChanged = true }

    override var favoritesFirst: Boolean
        get() = settings.getBoolean(Keys.FAVORITES_LAYOUT.name, false)
        set(value) = settings.edit().putBoolean(Keys.FAVORITES_LAYOUT.name, value).apply()
            .also { isChanged = true }

    override var favoritesColumnCount: Int
        get() = settings.getInt(Keys.FAVORITES_COLUMNS_COUNT.name, 4)
        set(count) = settings.edit().putInt(Keys.FAVORITES_COLUMNS_COUNT.name, count).apply()
            .also { isChanged = true }

    override var favoritesSize: Int
        get() = settings.getInt(Keys.FAVORITES_SIZE.name, defaultFavoritesSize)
        set(size) = settings.edit().putInt(Keys.FAVORITES_SIZE.name, size).apply()
            .also { isChanged = true }

    override var favoritesBorderColor: Int?
        get() = if (settings.contains(Keys.FAVORITES_BORDER_COLOR.name))
            settings.getInt(Keys.FAVORITES_BORDER_COLOR.name, Color.BLACK)
        else null
        set(color) = settings.edit().apply {
            if (null == color)
                remove(Keys.FAVORITES_BORDER_COLOR.name)
            else
                putInt(Keys.FAVORITES_BORDER_COLOR.name, color)
        }.apply()
            .also { isChanged = true }

    override var favoritesBorderWidth: Int
        get() = settings.getInt(Keys.FAVORITES_BORDER_WIDTH.name, 5)
        set(size) = settings.edit().putInt(Keys.FAVORITES_BORDER_WIDTH.name, size).apply()
            .also { isChanged = true }

    override var swipeLeftAction: Action.Type
        get() = settings.getString(Keys.SWIPE_LEFT_ACTION.name, Action.Type.WhatsAppChat.name)
            ?.let { Action.Type.valueOf(it) }
            ?: Action.Type.WhatsAppChat
        set(action) = settings.edit().putString(Keys.SWIPE_LEFT_ACTION.name, action.name).apply()
            .also { Analytics.logGlobalSetAction("LEFT", action.name) }
            .also { isChanged = true }

    override var swipeRightAction: Action.Type
        get() = settings.getString(Keys.SWIPE_RIGHT_ACTION.name, Action.Type.PhoneCall.name)
            ?.let { Action.Type.valueOf(it) }
            ?: Action.Type.PhoneCall
        set(action) = settings.edit().putString(Keys.SWIPE_RIGHT_ACTION.name, action.name).apply()
            .also { Analytics.logGlobalSetAction("RIGHT", action.name) }
            .also { isChanged = true }

    override var swipeUpAction: Action.Type
        get() = settings.getString(Keys.SWIPE_UP_ACTION.name, Action.Type.Email.name)
            ?.let { Action.Type.valueOf(it) }
            ?: Action.Type.Email
        set(action) = settings.edit().putString(Keys.SWIPE_UP_ACTION.name, action.name).apply()
            .also { Analytics.logGlobalSetAction("UP", action.name) }
            .also { isChanged = true }

    override var swipeDownAction: Action.Type
        get() = settings.getString(Keys.SWIPE_DOWN_ACTION.name, Action.Type.TelegramChat.name)
            ?.let { Action.Type.valueOf(it) }
            ?: Action.Type.Sms
        set(action) = settings.edit().putString(Keys.SWIPE_DOWN_ACTION.name, action.name).apply()
            .also { Analytics.logGlobalSetAction("DOWN", action.name) }
            .also { isChanged = true }

    override var listDivider: Boolean
        get() = settings.getBoolean(Keys.LIST_DIVIDER.name, false)
        set(value) = settings.edit().putBoolean(Keys.LIST_DIVIDER.name, value).apply()
            .also { isChanged = true }

    override fun getContactSettings(contact: Contact): DirectActions {
        val preferences = context.getSharedPreferences("contacts", Context.MODE_PRIVATE)
        val settings = preferences.getString("actions-${contact.id}", null)
        return if (settings == null) {
            getDefaultContactActions(contact)
        } else {
            Gson().fromJson(settings, ContactSettings::class.java)
                ?.let { getContactActions(contact, it) }
                ?: getDefaultContactActions(contact)
        }
    }

    override fun setContactSettings(contact: Contact, actions: DirectActions) {
        val settings = ContactSettings(
            ActionSettings(actions.left.id, actions.left.type),
            ActionSettings(actions.right.id, actions.right.type),
            ActionSettings(actions.top.id, actions.top.type),
            ActionSettings(actions.down.id, actions.down.type),
        )
        context.getSharedPreferences("contacts", Context.MODE_PRIVATE)
            .edit()
            .putString("actions-${contact.id}", Gson().toJson(settings))
            .apply()
    }

    /**
     * Возвращает набор действий для контакта в соответствии с его настройками
     */
    private fun getContactActions(
        contact: Contact,
        settings: ContactSettings
    ): DirectActions {
        val left = getContactAction(contact, settings.left)
        val right = getContactAction(contact, settings.right)
        val top = getContactAction(contact, settings.up)
        val bottom = getContactAction(contact, settings.down)
        return DirectActions(left, right, top, bottom)
    }

    /**
     * Ищет подходящее действие: сначала по ID+типу, затем просто по типу
     */
    private fun getContactAction(contact: Contact, like: ActionSettings): Action {
        return contact.actions.find { it.id == like.id && it.type == like.type }
            ?: contact.actions.firstOrNull { it.type == like.type }
            ?: Action(0, like.type, "", "")
    }

    /**
     * Возвращает набор действий для контакта в соответствии с глобальными дефолтными настройками
     */
    private fun getDefaultContactActions(contact: Contact): DirectActions {
        val left = getActionByType(contact, swipeLeftAction)
        val right = getActionByType(contact, swipeRightAction)
        val top = getActionByType(contact, swipeUpAction)
        val bottom = getActionByType(contact, swipeDownAction)
        return DirectActions(left, right, top, bottom)
    }

    private fun getActionByType(contact: Contact, type: Action.Type): Action {
        return contact.actions.firstOrNull { it.type == type }
            ?: Action(0, type, "", "")
    }

    /**
     * Адапторы для сериализации настроек в json
     */
    class ContactSettings(
        var left: ActionSettings,
        var right: ActionSettings,
        var up: ActionSettings,
        var down: ActionSettings,
    )

    class ActionSettings(
        val id: Int,
        val type: Action.Type,
    )
}
