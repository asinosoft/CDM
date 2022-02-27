package com.asinosoft.cdm.helpers

import android.content.Context
import android.net.Uri
import com.asinosoft.cdm.R

fun Context.getBackgroundUrl(index: Int): Uri? {
    val backgrounds = resources.obtainTypedArray(R.array.backgrounds)
    try {
        if ((index < 0) or (index >= backgrounds.length())) return null

        val resource = backgrounds.getResourceId(index, 0)
        return if (0 == resource) null else Uri.parse("android.resource://com.asinosoft.cdm/drawable/$resource")
    } finally {
        backgrounds.recycle()
    }
}

fun getThemeResourceId(index: Int): Int =
    when (index) {
        0 -> R.style.AppTheme_Light
        1 -> R.style.AppTheme_Gray
        2 -> R.style.AppTheme_Dark
        3 -> R.style.AppTheme_Dark2
        else -> R.style.AppTheme_Light
    }
