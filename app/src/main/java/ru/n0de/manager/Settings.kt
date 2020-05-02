package ru.n0de.manager

import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import java.io.Serializable
import java.net.URI

data class Settings(
    var dragPogres: Int = 10,
    var sizeCirs: Int = 200,
    var marginStartCirs: Int = 100,
    var marginTopCirs: Int = 150,
    var offsetCirs: Int = 300,
    var maxTouch: Int = 200,
    var maxPrior: Int = 50,
    var difTouch: Int = 150,
    var historyListHeight: Int = 1000,
    var spliterOfssetGlobal: Int = 0,
    var photoType: PhotoType = PhotoType.Full,
    var countCirs: Int = 12,
    var themeColor: Int = Color.WHITE,
    var listHistoryReverse: Boolean = true,
    //var customPhotoFonUrl: Uri? = null
    var photoFilePath: String = "",
    var leftButton: Actions = Actions.WhatsApp,
    var rightButton: Actions = Actions.PhoneCall,
    var topButton: Actions = Actions.Email,
    var bottomButton: Actions = Actions.Sms,
    var chooserButton1: Actions = Actions.Telegram,
    var chooserButton2: Actions = Actions.Viber
)

enum class Actions{
    WhatsApp, Viber, Telegram, PhoneCall, Email, Sms
}