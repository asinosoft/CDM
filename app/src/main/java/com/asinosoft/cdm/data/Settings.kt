package com.asinosoft.cdm.data

import android.graphics.Color
import com.asinosoft.cdm.R

/**
 * Информационный класс настроек
 */
data class Settings(
    var sizeCirs: Int = 200,
    var countCirs: Int = 10,
    var theme: Int = R.style.AppTheme_Light,
    var listHistoryReverse: Boolean = true,
    var photoFilePath: String = "",
    var leftButton: Action.Type = Action.Type.WhatsAppChat,
    var rightButton: Action.Type = Action.Type.PhoneCall,
    var topButton: Action.Type = Action.Type.Email,
    var bottomButton: Action.Type = Action.Type.Sms,

    /**
     * Режим главного окна:
     * true: в верхней части находятся избранные контакты, в нижней - история звонков (начиная с недавних звонков)
     * false: в верхней части находится история звонков (заканчивается недавними звонками), в нижней - избранные контакты
     */
    var historyButtom: Boolean = true,

    var cirMenu: Boolean = false,
    var columnsCirs: Int = 3,
    var colorBorder: Int = Color.CYAN,
    var borderWidthCirs: Int = 5
)
