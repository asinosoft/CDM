package com.asinosoft.cdm

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.telecom.TelecomManager
import android.util.Log
import android.view.View
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.core.net.toUri
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.asinosoft.cdm.Metoths.Companion.toggle
import com.asinosoft.cdm.databinding.ActivityManagerBinding
import com.asinosoft.cdm.detail_contact.ContactDetailListElement
import com.asinosoft.cdm.dialer.Utilities
import com.github.florent37.runtimepermission.RuntimePermission.askPermission
import com.github.tamir7.contacts.Contacts
import com.jaeger.library.StatusBarUtil
import com.skydoves.powermenu.kotlin.powerMenu
import kotlinx.android.synthetic.main.activity_manager.*
import kotlinx.android.synthetic.main.keyboard.*

/**
 * Основной класс приложения, отвечает за работу главного экрана (нового) приложения
 */
class ManagerActivity : AppCompatActivity() {

    companion object {
        const val ACTIVITY_PICK_CONTACT = 13
        const val ACTIVITY_SETTINGS = 12
        const val REQUEST_PERMISSION = 0
    }

    /**
     * Элемент, хранящий ссылки на все представления привязанного макета
     */
    private lateinit var v: ActivityManagerBinding

    /**
     * ViewModel главного экрана, отвечает за всю фоновую логику
     */
    private lateinit var viewModel: ManagerViewModel
    private lateinit var keyboard: Keyboard
    private val moreMenu by powerMenu(MoreMenuFactory::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        v = ActivityManagerBinding.inflate(layoutInflater)
        setContentView(v.root)
        getPermission()
        viewModel = ViewModelProvider(this).get(ManagerViewModel::class.java)
        StatusBarUtil.setTranslucentForImageView(this, v.container)
        startForView()

        layout_keyboard?.toggle()
        val isDefaultDealer: Boolean = Utilities().checkDefaultDialer(this)
        if (isDefaultDealer) {
            checkPermission(null)
        }

        fabKeyboard.setOnClickListener {
            v.layoutKeyboard.toggle()
            recyclerViewContact.visibility = if (layout_keyboard.height != 1) {
                kotlin.runCatching {
                    keyboard.input_text.text = ""
                }
                View.GONE
            } else View.VISIBLE
        }
        keyboard = supportFragmentManager.findFragmentById(R.id.keyboard) as Keyboard



        keyboard.input_text.doOnTextChanged { text, start, count, after ->
            kotlin.runCatching {
                (recyclerViewContact.adapter as AdapterContacts).setFilter(
                    text.toString(),
                    context = baseContext
                )
//                viewModel.filterCallLogs(text.toString())
            }.exceptionOrNull()?.printStackTrace()
        }
    }

    private fun initContacts(){
        val list = Contacts.getQuery().find().filter { !it.phoneNumbers.isNullOrEmpty() }

        recyclerViewContact.layoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.VERTICAL
            initialPrefetchItemCount = 11
        }

        recyclerViewContact.adapter = AdapterContacts(list, View.OnClickListener {}, false)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION && PermissionChecker.PERMISSION_GRANTED in grantResults) {
            makeCall()
        }
    }

    private fun checkPermission(@Nullable grantResults: IntArray?) {
        if (grantResults != null && Utilities().checkPermissionsGranted(grantResults)) {
            Utilities().checkPermissionsGranted(this, Utilities().MUST_HAVE_PERMISSIONS)
        } else {
            Utilities().askForPermissions(this, Utilities().MUST_HAVE_PERMISSIONS)
        }
    }

    private fun offerReplacingDefaultDialer() {
        if (getSystemService(TelecomManager::class.java).defaultDialerPackage != packageName) {
            Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                .let(::startActivity)
        }
    }

    private fun makeCall() {
        if (PermissionChecker.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) == PermissionChecker.PERMISSION_GRANTED
        ) {
            val uri = "tel:${ContactDetailListElement().active}".toUri()
            startActivity(Intent(Intent.ACTION_CALL, uri))
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE),
                REQUEST_PERMISSION
            )
        }
    }

    private fun getPermission() {
        askPermission(this).onDenied { getPermission() }.onAccepted { initContacts() }.ask()
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
        offerReplacingDefaultDialer()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        scrollView.setScrollingEnabled(true)
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
