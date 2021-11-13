package com.asinosoft.cdm.activities

import android.Manifest.permission.*
import android.os.Bundle
import androidx.activity.viewModels
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.Loader
import com.asinosoft.cdm.viewmodels.ManagerViewModel
import timber.log.Timber

/**
 * Основной класс приложения, отвечает за работу главного экрана (нового) приложения
 */
class ManagerActivity : BaseActivity() {
    /**
     * Отслеживает случаи, когда onResume срабатывает дважды
     */
    private var isRefreshed: Boolean = false

    private val model: ManagerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        withPermission(
            arrayOf(
                READ_CONTACTS,
                READ_CALL_LOG,
                READ_PHONE_STATE,
                CALL_PHONE
            )
        ) { ok ->
            if (ok && settings.checkDefaultDialer) {
                setDefaultDialer()
            }
        }
    }

    override fun onResume() {
        Timber.d("onResume")
        super.onResume()

        if (Loader.loadSettings(this, true) == settings) {
            refreshModel()
        } else {
            recreate()
        }
    }

    override fun onPause() {
        Timber.d("onPause")
        super.onPause()
        isRefreshed = false
    }

    private fun refreshModel() {
        if (isRefreshed) return

        if (hasPermissions(arrayOf(READ_CONTACTS, READ_CALL_LOG))) {
            model.refresh()
            isRefreshed = true
        }
    }
}
