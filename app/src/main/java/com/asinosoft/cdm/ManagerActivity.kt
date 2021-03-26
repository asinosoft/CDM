package com.asinosoft.cdm

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.telecom.TelecomManager
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.Metoths.Companion.makeTouch
import com.asinosoft.cdm.Metoths.Companion.toggle
import com.asinosoft.cdm.adapters.NumbeAdapter
import com.asinosoft.cdm.databinding.ActivityManagerBinding
import com.asinosoft.cdm.detail_contact.Contact
import com.asinosoft.cdm.dialer.Utilities
import com.asinosoft.cdm.globals.AlertDialogUtils
import com.jaeger.library.StatusBarUtil
import kotlinx.android.synthetic.main.activity_manager.*
import kotlinx.android.synthetic.main.keyboard.*
import timber.log.Timber

/**
 * Основной класс приложения, отвечает за работу главного экрана (нового) приложения
 */
class ManagerActivity : AppCompatActivity(), KeyBoardListener {
    companion object {
        const val ACTIVITY_PICK_CONTACT = 13
        const val ACTIVITY_SETTINGS = 12
        const val REQUEST_PERMISSION1 = 1
    }

    /**
     * Элемент, хранящий ссылки на все представления привязанного макета
     */
    private lateinit var v: ActivityManagerBinding

    /**
     * ViewModel главного экрана, отвечает за всю фоновую логику
     */
    private val viewModel: ManagerViewModel by viewModels()
    private lateinit var keyboard: Keyboard
    private val PERMISSIONS = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.WRITE_CALL_LOG
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        v = ActivityManagerBinding.inflate(layoutInflater)
        setContentView(v.root)
        if (!hasPermissions(this, *PERMISSIONS)) {
            requestAllPermissions()
        }
        initActivity()
    }

    private fun requestAllPermissions() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION1)

    }

    private fun hasPermissions(context: Context?, vararg permissions: String): Boolean {
        if (context != null && permissions != null) {
            for (permission in permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }

    private fun initActivity() {
        if(PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.READ_CONTACTS)) {
            Timber.d("%s NOT PERMITTED!", Manifest.permission.READ_CONTACTS)
            return
        }

        StatusBarUtil.setTranslucentForImageView(this, v.container)
        startForView()

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
            }.exceptionOrNull()?.printStackTrace()
        }
    }

    private fun toggleContacts(keyButton: View) {
        v.layoutKeyboard.toggle()
        keyButton.toggle(animation = false)
        recyclerViewContact.visibility = if (recyclerViewContact.isVisible) {
            keyboard.input_text.text = ""
            View.GONE
        } else {
            recyclerViewContact.adapter =
                AdapterContacts(viewModel.getContacts(), View.OnClickListener {}, false)
            View.VISIBLE
        }
    }

    private fun initContacts() {
        Timber.d("initContacts")
        recyclerViewContact.layoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.VERTICAL
            initialPrefetchItemCount = 11
        }
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Timber.i(grantResults.toString())
        initActivity()
        this.onResume()
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

    private fun startForView() {
        viewModel.start(
            v,
            this,
            lifecycle,
            pickedContact = { pickContact() },
            settingsOpen = { settingOpen(it) }
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

    override fun onResume() {
        super.onResume()
        if(PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.READ_CONTACTS)) {
            Timber.d("%s NOT PERMITTED!", Manifest.permission.READ_CONTACTS)
            return
        }

        Timber.d("ManagerActivity.onResume")
        initContacts()
        recyclerViewHistory.makeTouch(MotionEvent.ACTION_UP)
        recyclerViewHistoryBottom.makeTouch(MotionEvent.ACTION_UP)
        viewModel.updateLists()
    }

    override fun onDestroy() {
        Timber.d("onDestroy: Destroy!")
        viewModel.onDestroy()
        offerReplacingDefaultDialer()
        super.onDestroy()
    }

    override fun onOpenSettings() {
        settingOpen(viewModel.settings)
    }

    private fun showNumberDialog(contact: Contact) {
        contact.let {
            val dialog = AlertDialogUtils.dialogListWithoutConfirm(this, "Выберите номер")
            val adapter = NumbeAdapter {
                viewModel.onResult(contact, number = it)
                dialog.dismiss()
            }
            val recyclerView = dialog.findViewById<RecyclerView>(R.id.recycler_popup)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter
            adapter.setData(contact.mPhoneNumbers)
            dialog.show()
        }
    }

    private fun clearDuplicateNumbers(numbers: MutableList<String>)
            : List<String> {
        val res: MutableList<String> = mutableListOf()
        numbers.forEach { it ->
            val cleanedNumber = it.replace(" ", "")
                .replace("-", "").trim()
            res.firstOrNull { it == cleanedNumber }
                ?: res.add(it)
        }
        return res
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        scrollView.setScrollingEnabled(true)
        if (requestCode == ACTIVITY_PICK_CONTACT && resultCode == Activity.RESULT_OK) {
            val contact = viewModel.getContactIdFromIntent(data!!)
            contact?.let {
                if (clearDuplicateNumbers(it.mPhoneNumbers).size > 1) {
                    showNumberDialog(it)
                } else {
                    viewModel.onResult(contact, number = it.mPhoneNumbers.first())
                }
            }

        } else if (requestCode == ACTIVITY_SETTINGS && resultCode == Activity.RESULT_OK) {
            viewModel.start(
                v,
                this,
                lifecycle,
                pickedContact = { pickContact() },
                settingsOpen = { settingOpen(it) }
            )
            viewModel.initViews(false)
        }
    }
}
