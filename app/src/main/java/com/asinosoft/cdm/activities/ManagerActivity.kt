package com.asinosoft.cdm.activities

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.asinosoft.cdm.App
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.Analytics
import com.asinosoft.cdm.helpers.isDefaultDialer
import com.asinosoft.cdm.helpers.setDefaultDialer
import com.asinosoft.cdm.viewmodels.ManagerViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.ktx.remoteConfig
import timber.log.Timber

/**
 * Основной класс приложения, отвечает за работу главного экрана (нового) приложения
 */
class ManagerActivity : BaseActivity() {
    private val model: ManagerViewModel by viewModels()

    /**
     * Отслеживает случаи, когда onResume срабатывает дважды
     */
    private var isModelRefreshed: Boolean = false

    private var remoteConfigInitialized = false

    private val launcher = registerForActivityResult(StartActivityForResult()) {
        if (isDefaultDialer()) {
            Analytics.logDefaultDialer()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (App.instance!!.config.isFirstRun) {
            Timber.d("Wait for RemoteConfig")
            installSplashScreen().setKeepOnScreenCondition { !remoteConfigInitialized }
        } else {
            Timber.d("Wait for Model")
            installSplashScreen().setKeepOnScreenCondition { !model.initialized }
        }

        Firebase.remoteConfig.setConfigSettingsAsync(
            FirebaseRemoteConfigSettings.Builder()
                .setFetchTimeoutInSeconds(3)
                .setMinimumFetchIntervalInSeconds(1)
                .build()
        )
        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener(this) {
            Timber.d("RemoteConfig initialized")
            remoteConfigInitialized = true
            if (App.instance!!.config.isFirstRun) {
                App.instance!!.config.applyRemoteConfig()
                recreate()
            }
        }

        Timber.d("onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if ((application as App).config.checkDefaultDialer) {
            setDefaultDialer(launcher)
        }
    }

    override fun onPause() {
        super.onPause()
        isModelRefreshed = false
    }

    override fun onResume() {
        Timber.d("onResume")
        super.onResume()

        if (App.instance!!.config.isChanged) {
            recreate()
        } else if (!isModelRefreshed) {
            isModelRefreshed = true
            model.refresh()
        }
    }
}
