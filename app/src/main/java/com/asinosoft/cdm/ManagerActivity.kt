package com.asinosoft.cdm

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.telecom.TelecomManager
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.Metoths.Companion.makeTouch
import com.asinosoft.cdm.Metoths.Companion.toggle
import com.asinosoft.cdm.adapters.NumbeAdapter
import com.asinosoft.cdm.databinding.ActivityManagerBinding
import com.asinosoft.cdm.detail_contact.ContactDetailListElement
import com.asinosoft.cdm.dialer.Utilities
import com.asinosoft.cdm.globals.AlertDialogUtils
import com.asinosoft.cdm.globals.Globals
import com.asinosoft.cdm.globals.Ut
import com.github.florent37.runtimepermission.RuntimePermission.askPermission
import com.github.tamir7.contacts.Contact
import com.github.tamir7.contacts.Contacts
import com.jaeger.library.StatusBarUtil
import com.skydoves.powermenu.kotlin.powerMenu
import kotlinx.android.synthetic.main.activity_manager.*
import kotlinx.android.synthetic.main.alert_dialog_product_search_without_confirm.*
import kotlinx.android.synthetic.main.keyboard.*

/**
 * Основной класс приложения, отвечает за работу главного экрана (нового) приложения
 */
class ManagerActivity : AppCompatActivity(), KeyBoardListener {

    companion object {
        const val ACTIVITY_PICK_CONTACT = 13
        const val ACTIVITY_SETTINGS = 12
        const val REQUEST_PERMISSION = 0
        const val REQUEST_PERMISSION1 = 1
    }

    /**
     * Элемент, хранящий ссылки на все представления привязанного макета
     */
    private lateinit var v: ActivityManagerBinding

    /**
     * ViewModel главного экрана, отвечает за всю фоновую логику
     */
    private  var viewModel: ManagerViewModel? =null
    private lateinit var keyboard: Keyboard
    private val moreMenu by powerMenu(MoreMenuFactory::class)
    val PERMISSIONS = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.WRITE_CALL_LOG
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        v = ActivityManagerBinding.inflate(layoutInflater)
        setContentView(v.root)
        Contacts.initialize(this)
        //getPermission()
        if (!hasPermissions(this, *PERMISSIONS)) {
            requestAllPermisions()
        } else {
            initContacts()
            initActivity()
        }

    }

    private fun requestAllPermisions() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION1)

    }

    fun hasPermissions(context: Context?, vararg permissions: String?): Boolean {
        if (context != null && permissions != null) {
            for (permission in permissions) {
                if (checkSelfPermission(permission!!) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }

    private fun initActivity() {
        viewModel = ViewModelProvider(this).get(ManagerViewModel::class.java)
        StatusBarUtil.setTranslucentForImageView(this, v.container)
        startForView()

        // layout_keyboard?.toggle()
        val isDefaultDealer: Boolean = Utilities().checkDefaultDialer(this)
        if (isDefaultDealer) {
            checkPermission(null)
        }

        fabKeyboard.setOnClickListener {
            toggleContacts(it)
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

    private fun toggleContacts(keyButton: View) {
        v.layoutKeyboard.toggle()
        keyButton.toggle(animation = false)
        recyclerViewContact.visibility = if (recyclerViewContact.isVisible) {
            kotlin.runCatching {
                keyboard.input_text.text = ""
            }
            View.GONE
        } else View.VISIBLE
    }

    private fun initContacts() {
        val list = Contacts.getQuery().find().filter { !it.phoneNumbers.isNullOrEmpty() }

        recyclerViewContact.layoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.VERTICAL
            initialPrefetchItemCount = 11
        }

        recyclerViewContact.adapter = AdapterContacts(list, View.OnClickListener {}, false)
    }

    override fun onBackPressed() {
        if (v.layoutKeyboard.isVisible) {
            toggleContacts(fabKeyboard)
        } else {
            super.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when {
            requestCode == REQUEST_PERMISSION && PermissionChecker.PERMISSION_GRANTED in grantResults -> makeCall()
            requestCode == REQUEST_PERMISSION1 -> kotlin.run { initContacts();initActivity(); updateLogs() }
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
        askPermission(this).onAccepted {
            initContacts()
            initActivity()
        }
            .ask {
                if (it.isAccepted) {
                    initContacts();initActivity()
                }
            }
    }


    fun startForView() {
        viewModel?.start(
            v,
            this,
            lifecycle,
            pickedContact = { pickContact() },
            settingsOpen = { settingOpen(it) },
            activity = this
        )
        viewModel?.initViews()
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
        viewModel?.saveCirs()
        super.onStop()
    }

    val handler: Handler = Handler()

    override fun onResume() {
        super.onResume()
        recyclerViewHistory.makeTouch(MotionEvent.ACTION_UP)
        recyclerViewHistoryBottom.makeTouch(MotionEvent.ACTION_UP)
        Globals.adapterLogs?.let {
            it.notifyDataSetChanged()
        }
        viewModel?.let {
            it.updateLists()
        }
        if (Globals.firstLaunch) {
            updateLogs()
        }
    }

    private fun updateLogs() {
        Globals.firstLaunch = false
        handler.postDelayed({
            Globals.adapterLogs?.let {
                it.notifyDataSetChanged()
            }
        }, 2000)
    }

    override fun onDestroy() {
        Log.d("${this.javaClass}", "onDestroy: Destroy!")
        viewModel?.onDestroy()
        offerReplacingDefaultDialer()
        super.onDestroy()
    }

    override fun onOpenSettings() {
        settingOpen(viewModel?.settings as Settings)
    }

    fun showNumberDialog(contact: Contact) {
        contact?.let {
            val numbersStr = mutableListOf<String>().apply {
                contact.phoneNumbers.forEach {
                    add(it.number)
                }
            }
            val dialog = AlertDialogUtils.dialogListWithoutConfirm(this, "Выберите номер")
            val adapter = NumbeAdapter {
                viewModel?.onResult(contact, number = it)
                dialog.dismiss()
            }
            val recyclerView = dialog.findViewById<RecyclerView>(R.id.recycler_popup)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter
            adapter.setData(numbersStr)
            dialog.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        scrollView.setScrollingEnabled(true)
        Globals.adapterLogs?.let {
            it.notifyDataSetChanged()
        }
        if (requestCode == ACTIVITY_PICK_CONTACT && resultCode == Activity.RESULT_OK) {
            val contact = Ut.getContactFromIntent(data)
            contact?.let {
                showNumberDialog(it)
            }

        } else if (requestCode == ACTIVITY_SETTINGS && resultCode == Activity.RESULT_OK) {
            viewModel?.start(
                v,
                this,
                lifecycle,
                pickedContact = { pickContact() },
                settingsOpen = { settingOpen(it) },
                activity = this
            )
            viewModel?.initViews(false)
        }
    }

}
