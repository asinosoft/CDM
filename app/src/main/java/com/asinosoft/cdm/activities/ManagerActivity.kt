package com.asinosoft.cdm.activities

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.Analytics
import com.asinosoft.cdm.api.Loader
import com.asinosoft.cdm.helpers.isDefaultDialer
import com.asinosoft.cdm.helpers.setDefaultDialer
import timber.log.Timber

/**
 * Основной класс приложения, отвечает за работу главного экрана (нового) приложения
 */
class ManagerActivity : BaseActivity() {

    private val launcher = registerForActivityResult(StartActivityForResult()) {
        if (isDefaultDialer()) {
            Analytics.logDefaultDialer()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (settings.checkDefaultDialer) {
            setDefaultDialer(launcher)
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
