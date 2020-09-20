package com.asinosoft.cdm

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.asinosoft.cdm.Metoths.Companion.dp
import com.asinosoft.cdm.Metoths.Companion.setSize
import com.asinosoft.cdm.databinding.ActivityManagerBinding
import com.github.florent37.runtimepermission.RuntimePermission.askPermission
import com.jaeger.library.StatusBarUtil
import com.skydoves.powermenu.kotlin.powerMenu
import kotlinx.android.synthetic.main.activity_manager.*

/**
 * Основной класс приложения, отвечает за работу главного экрана (нового) приложения
 */
class ManagerActivity : AppCompatActivity() {

    companion object {
        const val ACTIVITY_PICK_CONTACT = 13
        const val ACTIVITY_SETTINGS = 12
    }

    /**
     * Элемент, хранящий ссылки на все представления привязанного макета
     */
    private lateinit var v: ActivityManagerBinding

    /**
     * ViewModel главного экрана, отвечает за всю фоновую логику
     */
    private lateinit var viewModel: ManagerViewModel
    private val moreMenu by powerMenu(MoreMenuFactory::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        v = ActivityManagerBinding.inflate(layoutInflater)
        setContentView(v.root)
        getPermission()
        viewModel = ViewModelProvider(this).get(ManagerViewModel::class.java)
        StatusBarUtil.setTranslucentForImageView(this, v.container)
        startForView()
    }

    private fun getPermission() {
        askPermission(this).onDenied { getPermission() }.ask()
    }


    fun startForView() {
        viewModel.start(
            v,
            this,
            lifecycle,
            pickedContact = { pickContact() },
            settingsOpen = { settingOpen(it) },
            activity = this
        )
        viewModel.initViews()
        fabKeyboard.setOnClickListener {
            if (layout_keyboard.height == 0)
                layout_keyboard.setSize(470.dp)
            else layout_keyboard.setSize(0.dp)
        }
    }

    private fun pickContact() {
        startActivityForResult(
            Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI),
            ACTIVITY_PICK_CONTACT
        )
    }

    private fun settingOpen(settings: Settings) {
        startActivityForResult(
            Intent(this, SettingsActivity::class.java).apply {
                putExtra(
                    Keys.Settings,
                    com.google.gson.Gson().toJson(settings)
                )
            },
            ACTIVITY_SETTINGS
        )
    }

    override fun onStop() {
        viewModel.saveCirs()
        super.onStop()
    }

    override fun onDestroy() {
        Log.d("${this.javaClass}", "onDestroy: Destroy!")
        viewModel.onDestroy()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACTIVITY_PICK_CONTACT && resultCode == Activity.RESULT_OK) {
            viewModel.onResult(requestCode, requestCode, data)
        } else if (requestCode == ACTIVITY_SETTINGS && resultCode == Activity.RESULT_OK) {
            viewModel.start(
                v,
                this,
                lifecycle,
                pickedContact = { pickContact() },
                settingsOpen = { settingOpen(it) },
                activity = this
            )
            viewModel.initViews(false)
        }
    }

}
