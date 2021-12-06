package com.asinosoft.cdm.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.widget.Toast
import com.asinosoft.cdm.R
import com.asinosoft.cdm.adapters.StringsWithIconsAdapter
import com.asinosoft.cdm.helpers.telecomManager
import com.asinosoft.cdm.helpers.telephonyManager
import timber.log.Timber

/**
 * Невидимая активность для выбора симки и запуска звонка
 */
class DialActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate")
        super.onCreate(savedInstanceState)

        if (intent.action == Intent.ACTION_CALL && intent.data != null) {
            Timber.d("onCreate → ${intent.data}")
            withPermission(arrayOf(Manifest.permission.CALL_PHONE)) { permitted ->
                if (permitted) placeCall(intent.data)
                else {
                    Toast.makeText(this, "Не могу сделать вызов", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        } else {
            finish()
        }
    }

    @SuppressLint("MissingPermission")
    private fun placeCall(contact: Uri?) {
        Timber.d("Запустить звонок → $contact")
        withDefaultPhoneAccount { phoneAccount ->
            Bundle().apply {
                putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccount)
                putBoolean(TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE, false)
                putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, false)
                telecomManager.placeCall(contact, this)
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun withDefaultPhoneAccount(onSelect: (PhoneAccountHandle) -> Unit) {
        val account = telecomManager.getDefaultOutgoingPhoneAccount("tel")
        account?.let(onSelect)
    }

    @SuppressLint("MissingPermission")
    private fun selectPhoneAccount(onSelect: (PhoneAccountHandle) -> Unit) {
        Timber.d("Выбрать SIM для исходящего звонка")
        val accounts = telecomManager.callCapablePhoneAccounts
        if (1 == accounts.size || Build.VERSION.SDK_INT < 26) {
            Timber.d("Без вариантов SIM -> ${accounts[0]}")
            onSelect(accounts[0])
        } else {
            val slots: Array<String> =
                accounts.mapNotNull { telephonyManager.createForPhoneAccountHandle(it)?.simOperatorName }
                    .toTypedArray()
            val icons: Array<Int> =
                arrayOf(R.drawable.sim1, R.drawable.sim2, R.drawable.sim3)
            val adapter = StringsWithIconsAdapter(this, slots, icons)

            AlertDialog.Builder(this)
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
