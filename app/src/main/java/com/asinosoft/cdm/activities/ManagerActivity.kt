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

    /**
     * Один раз после запуска предлагаем поставить сделать приложение звонилкой-по-умолчанию
     */
    private var isDialerOffered: Boolean = false

    private val model: ManagerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        Timber.d("onResume")
        super.onResume()

        withPermission(
            arrayOf(
                READ_CONTACTS,
                READ_CALL_LOG,
                READ_PHONE_STATE,
                CALL_PHONE
            )
        ) { ok ->
            if (ok) initialize()
        }
    }

    override fun onPause() {
        Timber.d("onPause")
        super.onPause()
        isRefreshed = false
    }

    private fun initialize() {
        if (Loader.loadSettings(this, true) != settings) {
            recreate()
        } else {
            refreshModel()
            checkDefaultDialer()
        }
    }

    private fun refreshModel() {
        if (isRefreshed) return

        if (hasPermissions(arrayOf(READ_CONTACTS, READ_CALL_LOG))) {
            model.refresh()
            isRefreshed = true
        }
    }

    private fun checkDefaultDialer() {
        if (isDialerOffered) return

        if (hasPermissions(arrayOf(CALL_PHONE, READ_PHONE_STATE)) &&
            settings.checkDefaultDialer
        ) {
            setDefaultDialer()
            isDialerOffered = true
        }
    }
}
