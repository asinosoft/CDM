package com.asinosoft.cdm.activities

import android.os.Bundle
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.Loader
import timber.log.Timber

/**
 * Основной класс приложения, отвечает за работу главного экрана (нового) приложения
 */
class ManagerActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (settings.checkDefaultDialer) {
            setDefaultDialer()
        }
    }

    override fun onResume() {
        Timber.d("onResume")
        super.onResume()

        if (Loader.loadSettings(this, true) != settings) {
            recreate()
        }
    }
}
