package com.asinosoft.cdm.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.asinosoft.cdm.R
import com.asinosoft.cdm.adapters.StringsWithIconsAdapter
import com.asinosoft.cdm.api.Analytics
import com.asinosoft.cdm.helpers.isDefaultDialer
import com.asinosoft.cdm.helpers.setDefaultDialer
import com.asinosoft.cdm.helpers.telecomManager
import com.asinosoft.cdm.helpers.telephonyManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import timber.log.Timber

/**
 * Невидимая активность для выбора симки и запуска звонка
 */
class DialActivity : AppCompatActivity() {
    private lateinit var phone: Uri
    private var sim: Int = 0

    private val launcher =
        registerForActivityResult(StartActivityForResult()) {
            if (isDefaultDialer()) {
                Analytics.logDefaultDialer()
                placeCall()
            } else {
                startActivity(Intent(Intent.ACTION_DIAL, phone))
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate")
        super.onCreate(savedInstanceState)

        if (intent.data != null) {
            Timber.d("onCreate → ${intent.data}")
            phone = intent.data!!
            sim = intent.getIntExtra("sim", 0)
            if (isDefaultDialer()) {
                placeCall()
            } else {
                setDefaultDialer(launcher)
            }
        } else {
            finish()
        }
    }

    @SuppressLint("MissingPermission")
    private fun placeCall() {
        Timber.d("Запустить звонок → $phone")
        selectPhoneAccount { phoneAccount ->
            Bundle().apply {
                putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccount)
                putBoolean(TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE, false)
                putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, false)
            }.let { telecomManager.placeCall(phone, it) }
        }
    }

    @SuppressLint("MissingPermission")
    private fun withDefaultPhoneAccount(onSelect: (PhoneAccountHandle) -> Unit) {
        val account = telecomManager.getDefaultOutgoingPhoneAccount(PhoneAccount.SCHEME_TEL)
        account?.let(onSelect)
        finish()
    }

    @SuppressLint("MissingPermission")
    private fun selectPhoneAccount(onSelect: (PhoneAccountHandle) -> Unit) {
        Timber.d("Выбрать SIM для исходящего звонка")

        val default = telecomManager.getDefaultOutgoingPhoneAccount(PhoneAccount.SCHEME_TEL)
        val accounts = telecomManager.callCapablePhoneAccounts

        if (1 == accounts.size || Build.VERSION.SDK_INT < 26) {
            Timber.d("Без вариантов SIM -> ${accounts[0]}")
            onSelect(accounts[0])
            finish()
        } else if (sim > 0 && sim <= accounts.size) {
            onSelect(accounts[sim - 1])
            finish()
        } else if (null != default) {
            onSelect(default)
            finish()
        } else {
            val slots: Array<String> =
                accounts.mapNotNull { telephonyManager.createForPhoneAccountHandle(it)?.simOperatorName }
                    .toTypedArray()
            val icons: Array<Int> =
                arrayOf(R.drawable.ic_sim1, R.drawable.ic_sim2, R.drawable.ic_sim3)
            val adapter = StringsWithIconsAdapter(this, slots, icons)

            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.sim_selection_title)
                .setAdapter(adapter) { dialog, index ->
                    Timber.d("Выбран SIM -> ${accounts[index]}")
                    onSelect(accounts[index])
                    dialog.dismiss()
                }
                .setOnDismissListener {
                    finish()
                }
                .create()
                .show()
        }
    }
}
