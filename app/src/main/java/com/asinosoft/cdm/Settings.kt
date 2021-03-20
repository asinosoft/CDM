package com.asinosoft.cdm

import android.graphics.Color

/**
 * Информационный класс настроек
 */
data class Settings(
    var sizeCirs: Int = 200,
    var countCirs: Int = 10,
    var themeColor: Int = Color.WHITE,
    var listHistoryReverse: Boolean = true,
    var photoFilePath: String = "",
    var leftButton: Actions = Actions.WhatsApp,
    var rightButton: Actions = Actions.PhoneCall,
    var topButton: Actions = Actions.Email,
    var bottomButton: Actions = Actions.Sms,
    var chooserButton1: Actions = Actions.Telegram,
    var chooserButton2: Actions = Actions.Viber,
    var historyButtom: Boolean = true,
    var cirMenu: Boolean = false,
    var columnsCirs: Int = 3,
    var colorBorder: Int = Color.CYAN,
    var borderWidthCirs: Int = 5
){

    fun toDirectActions() = DirectActions(leftButton, rightButton, topButton, bottomButton)
}

data class DirectActions(val left: Actions = Actions.WhatsApp, val right: Actions = Actions.PhoneCall, val top: Actions = Actions.Email, val down: Actions = Actions.Sms)

enum class Actions{
    WhatsApp, Viber, Telegram, PhoneCall, Email, Sms
}