package com.asinosoft.cdm.activities

import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.asinosoft.cdm.App
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.Analytics
import com.asinosoft.cdm.helpers.isDefaultDialer
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
            App.instance!!.config.isChanged = false
            return recreate()
        }

        if (!isModelRefreshed) {
            isModelRefreshed = true
            model.refresh()
        }
    }
/*
    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_input_text, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item!!.itemId) {
            R.id.past_menu ->{
                Toast.makeText(applicationContext, "FirstFragment Setting", Toast.LENGTH_LONG).show()
  //              R.id.inputText.setText("Замена текста").toString()
                //              text(R.id.editText)
                /*           editText.setText("Замена текста").toString()

                           val clipboard = textView.context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
                           val clip = ClipData.newPlainText("Order Number", textView.text.toString())
                           clipboard?.setPrimaryClip(clip)
                           true
           */
                return true
            }
            R.id.copy_menu ->{
                Toast.makeText(applicationContext, "SecondFragment Setting", Toast.LENGTH_LONG).show()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
*/
}
