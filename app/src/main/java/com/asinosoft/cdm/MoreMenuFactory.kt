package com.asinosoft.cdm

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.asinosoft.cdm.Metoths.Companion.dp
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import com.skydoves.powermenu.kotlin.createPowerMenu

class MoreMenuFactory : PowerMenu.Factory() {

    override fun create(context: Context, lifecycle: LifecycleOwner): PowerMenu {
        return createPowerMenu(context) {
            addItem(PowerMenuItem("Переместить"))
            addItem(PowerMenuItem("Редактировать"))
            addItem(PowerMenuItem("Удалить"))
            setAutoDismiss(true)
            lifecycle.let(::setLifecycleOwner)
            setAnimation(MenuAnimation.SHOWUP_TOP_RIGHT)
            setTextColor(Color.CYAN)
            setTextSize(12)
            setMenuRadius(36f)
            setTextGravity(Gravity.START)
            setTextTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
            setSelectedTextColor(Color.RED)
            setMenuColor(Color.GRAY)
            setInitializeRule(Lifecycle.Event.ON_CREATE, 0)
        }
    }
}