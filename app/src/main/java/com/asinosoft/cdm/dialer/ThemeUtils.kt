package com.asinosoft.cdm.dialer

import android.content.Context
import android.util.TypedValue
import androidx.annotation.StyleRes
import com.asinosoft.cdm.R

class ThemeUtils {

    annotation class ThemeType

    val TYPE_NORMAL = 0
    val TYPE_NO_ACTION_BAR = 1
    val TYPE_TRANSPARENT_STATUS_BAR = 2

    @StyleRes
    fun themeFromId(themeId: String?, @ThemeType type: Int): Int {
        when (type) {
            ThemeUtils().TYPE_NORMAL -> return themeNormalFromId(themeId)
            ThemeUtils().TYPE_NO_ACTION_BAR -> return ThemeUtils().themeNoActionBarFromId(themeId)
            ThemeUtils().TYPE_TRANSPARENT_STATUS_BAR -> return ThemeUtils().themeTransparentStatusBarFromId(themeId)
        }
        return ThemeUtils().themeNormalFromId(themeId)
    }

    @StyleRes
    fun themeNormalFromId(themeId: String?): Int {
        when (themeId) {
            "light;blue" -> return R.style.AppTheme_Light_Blue
            "light;pink" -> return R.style.AppTheme_Light_Pink
            "light;cream" -> return R.style.AppTheme_Light_Cream
            "light;green" -> return R.style.AppTheme_Light_Green
            "dark;blue" -> return R.style.AppTheme_Dark_Blue
            "dark;pink" -> return R.style.AppTheme_Dark_Pink
            "dark;green" -> return R.style.AppTheme_Dark_Green
            "dark;cream" -> return R.style.AppTheme_Dark_Cream
            "amoled;blue" -> return R.style.AppTheme_AMOLED_Blue
            "amoled;pink" -> return R.style.AppTheme_AMOLED_Pink
            "amoled;green" -> return R.style.AppTheme_AMOLED_Green
            "amoled;cream" -> return R.style.AppTheme_AMOLED_Cream
        }
        return R.style.AppTheme_Light_Pink
    }

    @StyleRes
    fun themeNoActionBarFromId(themeId: String?): Int {
        when (themeId) {
            "light;blue" -> return R.style.AppTheme_Light_Blue_NoActionBar
            "light;pink" -> return R.style.AppTheme_Light_Pink_NoActionBar
            "light;green" -> return R.style.AppTheme_Light_Green_NoActionBar
            "light;cream" -> return R.style.AppTheme_Light_Cream_NoActionBar
            "dark;blue" -> return R.style.AppTheme_Dark_Blue_NoActionBar
            "dark;pink" -> return R.style.AppTheme_Dark_Pink_NoActionBar
            "dark;green" -> return R.style.AppTheme_Dark_Green_NoActionBar
            "dark;cream" -> return R.style.AppTheme_Dark_Cream_NoActionBar
            "amoled;blue" -> return R.style.AppTheme_AMOLED_Blue_NoActionBar
            "amoled;pink" -> return R.style.AppTheme_AMOLED_Pink_NoActionBar
            "amoled;green" -> return R.style.AppTheme_AMOLED_Green_NoActionBar
            "amoled;cream" -> return R.style.AppTheme_AMOLED_Cream_NoActionBar
        }
        return R.style.AppTheme_Light_Pink_NoActionBar
    }

    @StyleRes
    fun themeTransparentStatusBarFromId(themeId: String?): Int {
        when (themeId) {
            "light;blue" -> return R.style.AppTheme_Light_Blue_TransparentStatusBar
            "light;pink" -> return R.style.AppTheme_Light_Pink_TransparentStatusBar
            "light;green" -> return R.style.AppTheme_Light_Green_TransparentStatusBar
            "light;cream" -> return R.style.AppTheme_Light_Cream_TransparentStatusBar
            "dark;blue" -> return R.style.AppTheme_Dark_Blue_TransparentStatusBar
            "dark;pink" -> return R.style.AppTheme_Dark_Pink_TransparentStatusBar
            "dark;green" -> return R.style.AppTheme_Dark_Green_TransparentStatusBar
            "dark;cream" -> return R.style.AppTheme_Dark_Cream_TransparentStatusBar
            "amoled;blue" -> return R.style.AppTheme_AMOLED_Blue_TransparentStatusBar
            "amoled;pink" -> return R.style.AppTheme_AMOLED_Pink_TransparentStatusBar
            "amoled;green" -> return R.style.AppTheme_AMOLED_Green_TransparentStatusBar
            "amoled;cream" -> return R.style.AppTheme_AMOLED_Cream_TransparentStatusBar
        }
        return R.style.AppTheme_Light_Pink_TransparentStatusBar
    }

    /**
     * Вернуть текущий акцентный цвет
     *
     * @param context
     * @return цвет int значение
     */
    fun getAccentColor(context: Context): Int {
        val typedValue = TypedValue()
        val a =
            context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.secondaryAccentColor))
        val color = a.getColor(0, 0)
        a.recycle()
        return color
    }

}