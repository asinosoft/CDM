package com.asinosoft.cdm.activities

import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.Loader

/**
 * Базовый клас с поддержкой тем
 */
open class BaseActivity : AppCompatActivity() {
    private var appTheme: Int = R.style.AppTheme_Light

    override fun onCreate(savedInstanceState: Bundle?) {
        appTheme = Loader.loadSettings(this).theme
        super.onCreate(savedInstanceState)
    }

    override fun getTheme(): Resources.Theme {
        return super.getTheme().apply {
            applyStyle(appTheme, true)
        }
    }
}
