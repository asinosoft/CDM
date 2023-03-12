package com.asinosoft.cdm.fragments

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.Analytics
import com.asinosoft.cdm.databinding.KeyboardBinding
import com.asinosoft.cdm.helpers.Metoths.Companion.vibrateSafety
import com.asinosoft.cdm.helpers.runOnUiThread
import com.asinosoft.cdm.helpers.telecomManager
import com.asinosoft.cdm.helpers.vibrator
import com.skyfishjy.library.RippleBackground

/**
 * Фрагмент со всплывающей клавиатурой
 */
class KeyboardFragment : Fragment() {
    private lateinit var v: KeyboardBinding
    private var settingsButtonClickCallback: () -> Unit = {}
    private var callButtonClickCallback: (phoneNumber: String, sim: Int) -> Unit = { _, _ -> }
    private var closeButtonClickCallback: () -> Unit = {}

    var text: String
        get() = v.inputText.text.toString()
        set(text) {
            v.inputText.text = text
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Если в телефоне установлены две симки, то показываем двойную кнопку звонка взамен стандартной
        if (PackageManager.PERMISSION_GRANTED == requireContext().checkSelfPermission((Manifest.permission.READ_PHONE_STATE))) {
            (requireContext().telecomManager.callCapablePhoneAccounts.size >= 2).let { isDualSim ->
                v.btnCall.visibility = if (isDualSim) View.GONE else View.VISIBLE
                v.btnCallSim1.visibility = if (isDualSim) View.VISIBLE else View.GONE
                v.btnCallSim2.visibility = if (isDualSim) View.VISIBLE else View.GONE
            }
        }
    }

    /**
     * Реакция на изменение текста в строке поиска
     */
    fun doOnTextChanged(callback: (String) -> Unit) {
        v.inputText.doOnTextChanged { s, _, _, _ -> callback(s.toString()) }
    }

    /**
     * Реакция на кнопку "Вызов"
     */
    fun onCallButtonClick(callback: (phoneNumber: String, sim: Int) -> Unit) {
        callButtonClickCallback = callback
    }

    /**
     * Реакция на кнопку "Настройки"
     */
    fun onSettingsButtonClick(callback: () -> Unit) {
        settingsButtonClickCallback = callback
    }

    /**
     * Реакция на кнопку "Убрать клавиатуру"
     */
    fun onCloseButtonClick(callback: () -> Unit) {
        closeButtonClickCallback = callback
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v = KeyboardBinding.inflate(inflater, container, false)
        v.oneBtn.setOnClickListener {
            v.ripple1.ripple()
            takeValue("1")
        }
        v.twoBtn.setOnClickListener {
            v.ripple2.ripple()
            takeValue("2")
        }
        v.threeBtn.setOnClickListener {
            v.ripple3.ripple()
            takeValue("3")
        }
        v.fourBtn.setOnClickListener {
            v.ripple4.ripple()
            takeValue("4")
        }
        v.fiveBtn.setOnClickListener {
            v.ripple5.ripple()
            takeValue("5")
        }
        v.sixBtn.setOnClickListener {
            v.ripple6.ripple()
            takeValue("6")
        }
        v.sevenBtn.setOnClickListener {
            v.ripple7.ripple()
            takeValue("7")
        }
        v.eightBtn.setOnClickListener {
            v.ripple8.ripple()
            takeValue("8")
        }
        v.nineBtn.setOnClickListener {
            v.ripple9.ripple()
            takeValue("9")
        }
        v.zeroBtn.setOnLongClickListener {
            takeValue("+")
            true
        }
        v.star.setOnClickListener {
            takeValue("*")
        }
        v.star.setOnLongClickListener {
            takeValue("#")
            true
        }
        v.zeroBtn.setOnClickListener {
            v.ripple0.ripple()
            takeValue("0")
        }
        v.imageBackspace.setOnClickListener {
            Analytics.logSearchKeyboardDel()
            v.inputText.text = v.inputText.text.dropLast(1)
            if (v.inputText.text.isEmpty())
                v.settingsButton.setImageResource(R.drawable.ic_cog)
        }
        v.imageBackspace.setOnLongClickListener {
            Analytics.logSearchKeyboardClear()
            v.inputText.text = ""
            context?.vibrator?.vibrateSafety(2, 255)
            v.settingsButton.setImageResource(R.drawable.ic_cog)
            true
        }
        v.imageClear.setOnClickListener {
            closeButtonClickCallback()
        }
        v.btnCall.setOnClickListener {
            if (v.inputText.text.isNotEmpty()) {
                callButtonClickCallback(v.inputText.text.toString(), 0)
            }
        }
        v.btnCallSim1.setOnClickListener {
            if (v.inputText.text.isNotEmpty()) {
                callButtonClickCallback(v.inputText.text.toString(), 1)
            }
        }
        v.btnCallSim2.setOnClickListener {
            if (v.inputText.text.isNotEmpty()) {
                callButtonClickCallback(v.inputText.text.toString(), 2)
            }
        }

        v.settingsButton.setOnClickListener {
            if (v.inputText.text.isNotEmpty()) {
                addNewContact(v.inputText.text)
            } else {
                settingsButtonClickCallback()
            }
        }

        v.inputText.setOnLongClickListener { _ ->
            val myClipboard =
                requireContext().getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager?

            if (v.inputText.text.isEmpty()) {
                val abc = myClipboard?.primaryClip
                val item = abc?.getItemAt(0)
                context?.vibrator?.vibrateSafety(2, 255)
                item?.text?.let {
                    v.inputText.text = it
                }
                Analytics.logKeyboardPaste()
                v.settingsButton.setImageResource(R.drawable.ic_add_contact_new)
            } else {
                val clip: ClipData = ClipData.newPlainText("simple text", v.inputText.text)
                myClipboard?.setPrimaryClip(clip)
                context?.vibrator?.vibrateSafety(2, 255)
                Toast.makeText(requireContext(), R.string.copied, Toast.LENGTH_LONG)
                    .show()
                Analytics.logKeyboardCopy()
            }
            true
        }

        return v.root
    }

    private fun addNewContact(number: CharSequence) {
        Intent().apply {
            action = Intent.ACTION_INSERT_OR_EDIT
            type = "vnd.android.cursor.item/contact"
            putExtra("phone", number)
            launchActivityIntent(this)
        }
    }

    private fun launchActivityIntent(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.add_error, Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun takeValue(num: String) {
        Analytics.logKeyboardButton()
        v.settingsButton.setImageResource(R.drawable.ic_add_contact_new)
        v.inputText.text = v.inputText.text.toString().plus(num)
    }
}

fun RippleBackground.ripple() {
    startRippleAnimation()
    Handler(Looper.getMainLooper()).postDelayed(
        {
            context?.runOnUiThread { stopRippleAnimation() }
        },
        167
    )
}
