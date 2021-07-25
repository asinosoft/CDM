package com.asinosoft.cdm.activities

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.os.Bundle
import android.telecom.TelecomManager
import android.util.Log
import androidx.activity.viewModels
import com.asinosoft.cdm.api.Loader
import com.asinosoft.cdm.databinding.ActivityMainBinding
import com.asinosoft.cdm.dialer.isQPlus
import com.asinosoft.cdm.viewmodels.ManagerViewModel

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
        Log.d("Main", "Created")
        super.onCreate(savedInstanceState)
        setContentView(ActivityMainBinding.inflate(layoutInflater).root)

        withPermission(arrayOf(Manifest.permission.CALL_PHONE)) { permitted ->
            if (permitted) offerReplaceDefaultDialer()
        }
    }

    override fun onResume() {
        Log.d("Main", "Resumed")
        super.onResume()

        if (Loader.loadSettings(this) == settings) {
            refreshModel()
        } else {
            recreate()
        }
    }

    override fun onPause() {
        Log.d("Main", "Paused")
        super.onPause()
        isRefreshed = false
    }

    private fun refreshModel() {
        if (isRefreshed) return

        withPermission(
            arrayOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_CALL_LOG
            )
        ) { permitted ->
            if (permitted) model.refresh()
        }
        isRefreshed = true
    }

    private fun offerReplaceDefaultDialer() {
        val currentDialer = getSystemService(TelecomManager::class.java).defaultDialerPackage
        if (currentDialer != packageName) {
            Log.d("Main", "Менеджер звонков: $currentDialer")
            if (isQPlus()) {
                val roleManager = getSystemService(RoleManager::class.java)
                if (roleManager.isRoleAvailable(RoleManager.ROLE_DIALER) &&
                    !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
                ) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                    launcher.launch(intent)
                }
            } else {
                val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                    .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                launcher.launch(intent)
            }
        }
    }
}
