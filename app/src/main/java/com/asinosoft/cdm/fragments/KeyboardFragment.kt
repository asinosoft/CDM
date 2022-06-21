package com.asinosoft.cdm.fragments

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
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

/**
 * Класс кастомной клавиатуры.
 */
class KeyboardFragment : Fragment() {
    private val KEY_PHONE = "phone"
    private lateinit var v: KeyboardBinding
    private var settingsButtonClickCallback: () -> Unit = {}
    private var callButtonClickCallback: (phoneNumber: String, sim: Int) -> Unit = { _, _ -> }
    private var closeButtonClickCallback: () -> Unit = {}

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
     * Реакия на кнопку "Вызов"
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
            v.ripple1.startRippleAnimation()
            Handler().postDelayed(
                {
                    context?.runOnUiThread { v.ripple1.stopRippleAnimation() }
                },
                167
            )
            takeValue("1")
        }
        v.twoBtn.setOnClickListener {
            v.ripple2.startRippleAnimation()
            Handler().postDelayed(
                { context?.runOnUiThread { v.ripple2.stopRippleAnimation() } },
                167
            )
            takeValue("2")
        }
        v.threeBtn.setOnClickListener {
            v.ripple3.startRippleAnimation()
            Handler().postDelayed(
                { context?.runOnUiThread { v.ripple3.stopRippleAnimation() } },
                167
            )
            takeValue("3")
        }
        v.fourBtn.setOnClickListener {
            v.ripple4.startRippleAnimation()
            Handler().postDelayed(
                { context?.runOnUiThread { v.ripple4.stopRippleAnimation() } },
                167
            )
            takeValue("4")
        }
        v.fiveBtn.setOnClickListener {
            v.ripple5.startRippleAnimation()
            Handler().postDelayed(
                { context?.runOnUiThread { v.ripple5.stopRippleAnimation() } },
                167
            )
            takeValue("5")
        }
        v.sixBtn.setOnClickListener {
            v.ripple6.startRippleAnimation()
            Handler().postDelayed(
                { context?.runOnUiThread { v.ripple6.stopRippleAnimation() } },
                167
            )
            takeValue("6")
        }
        v.sevenBtn.setOnClickListener {
            v.ripple7.startRippleAnimation()
            Handler().postDelayed(
                { context?.runOnUiThread { v.ripple7.stopRippleAnimation() } },
                167
            )
            takeValue("7")
        }
        v.eightBtn.setOnClickListener {
            v.ripple8.startRippleAnimation()
            Handler().postDelayed(
                { context?.runOnUiThread { v.ripple8.stopRippleAnimation() } },
                167
            )
            takeValue("8")
        }
        v.nineBtn.setOnClickListener {
            v.ripple9.startRippleAnimation()
            Handler().postDelayed(
                { context?.runOnUiThread { v.ripple9.stopRippleAnimation() } },
                167
            )
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
            v.ripple0.startRippleAnimation()
            Handler().postDelayed(
                {
                    context?.runOnUiThread {
                        v.ripple0.stopRippleAnimation()
                    }
                },
                167
            )
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
                addNewContact()
            } else {
                settingsButtonClickCallback()
            }
        }

        v.inputText.setOnLongClickListener { view ->
            val myClipboard =
                requireContext().getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager?

            if (v.inputText.text.isEmpty()) {
                val abc = myClipboard?.primaryClip
                val item = abc?.getItemAt(0)
                val text = item?.text.toString()
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

    private fun addNewContact() {
        Intent().apply {
            action = Intent.ACTION_INSERT_OR_EDIT
            type = "vnd.android.cursor.item/contact"
            putExtra(KEY_PHONE, v.inputText.text)
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
