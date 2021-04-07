package com.asinosoft.cdm

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telecom.TelecomManager
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.Metoths.Companion.makeTouch
import com.asinosoft.cdm.adapters.NumbeAdapter
import com.asinosoft.cdm.api.FavoriteContact
import com.asinosoft.cdm.databinding.ActivityManagerBinding
import com.asinosoft.cdm.detail_contact.Contact
import com.asinosoft.cdm.dialer.Utilities
import com.asinosoft.cdm.globals.AlertDialogUtils
import com.jaeger.library.StatusBarUtil
import kotlinx.android.synthetic.main.activity_manager.*
import kotlinx.android.synthetic.main.keyboard.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import timber.log.Timber

/**
 * Основной класс приложения, отвечает за работу главного экрана (нового) приложения
 */
class ManagerActivity : AppCompatActivity(), KeyBoardListener {
    companion object {
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

    /**
     * Позиция в блоке избранных контактов, для которой был запущен диалог выбора контакта
     * TODO: найти способ пробросить номер позиции через Activity..PickContact
     */
    private var pickedPosition: Int = 0

    /**
     * Запуск окна настроек приложения
     */
    private val openSettings =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            initActivity()
            viewModel.updateLists()
        }

    /**
     * Запуск диалога выбора избранного контакта
     */
    private val pickContact =
        registerForActivityResult(ActivityResultContracts.PickContact()) { uri ->
            if (null != uri) {
                viewModel.getContactIdFromIntent(uri)?.let { contact ->
                    if (clearDuplicateNumbers(contact.mPhoneNumbers).size > 1) {
                        showNumberDialog(contact, pickedPosition)
                    } else {
                        viewModel.setFavoriteContact(
                            pickedPosition,
                            FavoriteContact(contact, contact.mPhoneNumbers.first())
                        )
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        v = ActivityManagerBinding.inflate(layoutInflater)
        setContentView(v.root)
        if (!hasPermissions(this, *PERMISSIONS)) {
            requestAllPermissions()
        }
        initActivity()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        pickedPosition = savedInstanceState.getInt("pickedPosition")
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("pickedPosition", pickedPosition)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        Timber.d("onResume")
        super.onResume()
        if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.READ_CONTACTS)) {
            Timber.d("%s NOT PERMITTED!", Manifest.permission.READ_CONTACTS)
            return
        }

        Timber.d("ManagerActivity.onResume")
        recyclerViewHistory.makeTouch(MotionEvent.ACTION_UP)
        recyclerViewHistoryBottom.makeTouch(MotionEvent.ACTION_UP)
        viewModel.updateLists()
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        offerReplacingDefaultDialer()
        super.onDestroy()
    }

    override fun onOpenSettings() {
        if (v.layoutKeyboard.isVisible) {
            hideContacts()
        }
        openSettings.launch(Intent(this, SettingsActivity::class.java))
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
        if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.READ_CONTACTS)) {
            Timber.d("%s NOT PERMITTED!", Manifest.permission.READ_CONTACTS)
            return
        }

        StatusBarUtil.setTranslucentForImageView(this, v.container)

        viewModel.start(
            v,
            this,
            pickContact = { position -> pickContact(position) }
        )

        val isDefaultDealer: Boolean = Utilities().checkDefaultDialer(this)
        if (isDefaultDealer) {
            checkPermission(null)
        }

        buttonRVTopUpdate.onClick { viewModel.showHiddenCallHistoryItems() }
        buttonRVDownUpdate.onClick { viewModel.showHiddenCallHistoryItems() }

        fabKeyboard.setOnClickListener {
            showContacts()
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

        recyclerViewContact.layoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.VERTICAL
            initialPrefetchItemCount = 11
        }
    }

    private fun showContacts() {
        recyclerViewContact.adapter =
            AdapterContacts(
                viewModel.getContacts().filter { it.mPhoneNumbers.isNotEmpty() },
                {},
                false
            )
        recyclerViewContact.visibility = View.VISIBLE
        keyboard.input_text.text = ""
        v.layoutKeyboard.visibility = View.VISIBLE
        v.fabKeyboard.visibility = View.GONE
    }

    private fun hideContacts() {
        v.layoutKeyboard.visibility = View.GONE
        v.fabKeyboard.visibility = View.VISIBLE
        recyclerViewContact.visibility = View.GONE
    }

    override fun onBackPressed() {
        if (v.layoutKeyboard.isVisible) {
            hideContacts()
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

    private fun pickContact(position: Int) {
        pickedPosition = position
        pickContact.launch(null)
    }

    private fun showNumberDialog(contact: Contact, position: Int) {
        contact.let {
            val dialog = AlertDialogUtils.dialogListWithoutConfirm(this, "Выберите номер")
            val adapter = NumbeAdapter {
                viewModel.setFavoriteContact(position, FavoriteContact(contact, it))
                dialog.dismiss()
            }
            val recyclerView = dialog.findViewById<RecyclerView>(R.id.recycler_popup)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter
            adapter.setData(contact.mPhoneNumbers)
            dialog.show()
        }
    }

    private fun clearDuplicateNumbers(numbers: MutableList<String>): List<String> {
        val res: MutableList<String> = mutableListOf()
        numbers.forEach { it ->
            val cleanedNumber = it.replace(" ", "")
                .replace("-", "").trim()
            res.firstOrNull { it == cleanedNumber }
                ?: res.add(it)
        }
        return res
    }
}
