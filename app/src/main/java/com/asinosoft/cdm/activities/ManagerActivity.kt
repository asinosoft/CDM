package com.asinosoft.cdm.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telecom.TelecomManager
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.asinosoft.cdm.api.Loader
import com.asinosoft.cdm.data.Settings
import com.asinosoft.cdm.databinding.ActivityMainBinding
import com.asinosoft.cdm.dialer.Utilities
import com.asinosoft.cdm.viewmodels.ManagerViewModel
import timber.log.Timber

/**
 * Основной класс приложения, отвечает за работу главного экрана (нового) приложения
 */
class ManagerActivity : BaseActivity() {
    /**
     * Отслеживает случаи, когда onRefresh срабатывает дважды
     */
    private var isRefreshed: Boolean = false

    private val model: ManagerViewModel by viewModels()
    private var oldSettings: Settings = Settings()

    private val PERMISSIONS = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.WRITE_CALL_LOG
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        oldSettings = Loader.loadSettings(this)
        setContentView(ActivityMainBinding.inflate(layoutInflater).root)

        if (!hasPermissions(*PERMISSIONS)) {
            requestAllPermissions()
        }

        if (Utilities().checkDefaultDialer(this)) {
            checkPermission()
        }
    }

    override fun onResume() {
        super.onResume()

        if (Loader.loadSettings(this) == oldSettings) {
            refreshModel()
        } else {
            recreate()
        }
    }

    override fun onPause() {
        super.onPause()
        isRefreshed = false
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        offerReplacingDefaultDialer()
        super.onDestroy()
    }

    private fun requestAllPermissions() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, 0)
    }

    private fun hasPermissions(vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        model.refresh()
        checkSelfPermission(Manifest.permission.READ_CONTACTS)
        this.onResume()
    }

    private fun refreshModel() {
        if (!isRefreshed && PackageManager.PERMISSION_GRANTED == checkSelfPermission(Manifest.permission.READ_CONTACTS)) {
            isRefreshed = true
            model.refresh()
        }
    }

    private fun checkPermission() {
        Utilities().askForPermissions(this, Utilities().MUST_HAVE_PERMISSIONS)
    }

    private fun offerReplacingDefaultDialer() {
        if (getSystemService(TelecomManager::class.java).defaultDialerPackage != packageName) {
            Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                .let(::startActivity)
        }
    }
}
