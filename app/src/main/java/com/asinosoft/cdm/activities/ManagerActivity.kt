package com.asinosoft.cdm.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import com.asinosoft.cdm.App
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.Analytics
import com.asinosoft.cdm.helpers.isDefaultDialer
import com.asinosoft.cdm.helpers.requestDrawOverlays
import com.asinosoft.cdm.helpers.setDefaultDialer
import com.asinosoft.cdm.viewmodels.ManagerViewModel
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

    private val launcher = registerForActivityResult(StartActivityForResult()) {
        if (isDefaultDialer()) {
            Analytics.logDefaultDialer()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Timber.d("Wait for Model")
        installSplashScreen().setKeepOnScreenCondition { !model.initialized }

        if ((application as App).config.checkDefaultDialer) {
            launcher.launch(setDefaultDialer())
        }

        requestDrawOverlays(findViewById(R.id.nav_host))

        if (intent.action === Intent.ACTION_DIAL) {
            intent.data?.schemeSpecificPart?.let { phone ->
                val host = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
                host.navController.navigate(R.id.searchFragment, bundleOf("phone" to phone))
            }
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
            App.instance!!.config.isChanged = false
            return recreate()
        }

        if (!isModelRefreshed) {
            isModelRefreshed = true
            model.refresh()
        }
    }
}
