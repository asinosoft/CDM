package ru.n0de.manager

import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import java.io.Serializable
import java.net.URI

class Settings: Serializable {
    var dragPogres = 10
    var sizeCirs = 200
    var marginStartCirs = 100
    var marginTopCirs = 150
    var offsetCirs = 300
    var maxTouch = 200
    var maxPrior = 50
    var difTouch = 150
    var historyListHeight = 1000
    var spliterOfssetGlobal = 0
    var photoType = PhotoType.Full
    var countCirs = 12
    var themeColor = Color.WHITE
    var listHistoryReverse = true
    //var customPhotoFonUrl: Uri? = null
    var photoFilePath = ""
}