package com.asinosoft.cdm.fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.asinosoft.cdm.api.Analytics
import com.asinosoft.cdm.databinding.KeyboardBinding
import com.asinosoft.cdm.helpers.runOnUiThread

/**
 * Класс кастомной клавиатуры.
 */
class KeyboardFragment : Fragment() {
    private var v: KeyboardBinding? = null
    private var settingsButtonClickCallback: () -> Unit = {}
    private var callButtonClickCallback: (phoneNumber: String) -> Unit = {}
    private var closeButtonClickCallback: () -> Unit = {}

    /**
     * Реакция на изменение текста в строке поиска
     */
    fun doOnTextChanged(callback: (String) -> Unit) {
        v!!.inputText.doOnTextChanged { s, _, _, _ -> callback(s.toString()) }
    }

    /**
     * Реакия на кнопку "Вызов"
     */
    fun onCallButtonClick(callback: (phoneNumber: String) -> Unit) {
        callButtonClickCallback = callback
    }

    /**
     * Реакция на кнопку "Настройки"
     */
    fun onSettingsButtonClick(callback: () -> Unit) {
        settingsButtonClickCallback = callback
    }

    /**
     * Реакция на кнопку "Закрыть" (клик по кнопке "Очистить", когда строка поиска уже пустая)
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
        v!!.oneBtn.setOnClickListener {
            v!!.ripple1.startRippleAnimation()
            Handler().postDelayed(
                {
                    context?.runOnUiThread { v!!.ripple1.stopRippleAnimation() }
                },
                167
            )
            takeValue("1")
        }
        v!!.twoBtn.setOnClickListener {
            v!!.ripple2.startRippleAnimation()
            Handler().postDelayed(
                { context?.runOnUiThread { v!!.ripple2.stopRippleAnimation() } },
                167
            )
            takeValue("2")
        }
        v!!.threeBtn.setOnClickListener {
            v!!.ripple3.startRippleAnimation()
            Handler().postDelayed(
                { context?.runOnUiThread { v!!.ripple3.stopRippleAnimation() } },
                167
            )
            takeValue("3")
        }
        v!!.fourBtn.setOnClickListener {
            v!!.ripple4.startRippleAnimation()
            Handler().postDelayed(
                { context?.runOnUiThread { v!!.ripple4.stopRippleAnimation() } },
                167
            )
            takeValue("4")
        }
        v!!.fiveBtn.setOnClickListener {
            v!!.ripple5.startRippleAnimation()
            Handler().postDelayed(
                { context?.runOnUiThread { v!!.ripple5.stopRippleAnimation() } },
                167
            )
            takeValue("5")
        }
        v!!.sixBtn.setOnClickListener {
            v!!.ripple6.startRippleAnimation()
            Handler().postDelayed(
                { context?.runOnUiThread { v!!.ripple6.stopRippleAnimation() } },
                167
            )
            takeValue("6")
        }
        v!!.sevenBtn.setOnClickListener {
            v!!.ripple7.startRippleAnimation()
            Handler().postDelayed(
                { context?.runOnUiThread { v!!.ripple7.stopRippleAnimation() } },
                167
            )
            takeValue("7")
        }
        v!!.eightBtn.setOnClickListener {
            v!!.ripple8.startRippleAnimation()
            Handler().postDelayed(
                { context?.runOnUiThread { v!!.ripple8.stopRippleAnimation() } },
                167
            )
            takeValue("8")
        }
        v!!.nineBtn.setOnClickListener {
            v!!.ripple9.startRippleAnimation()
            Handler().postDelayed(
                { context?.runOnUiThread { v!!.ripple9.stopRippleAnimation() } },
                167
            )
            takeValue("9")
        }
        v!!.zeroBtn.setOnLongClickListener {
            takeValue("+")
            true
        }
        v!!.star.setOnClickListener {
            takeValue("*")
        }
        v!!.star.setOnLongClickListener {
            takeValue("#")
            true
        }
        v!!.zeroBtn.setOnClickListener {
            v!!.ripple0.startRippleAnimation()
            Handler().postDelayed(
                {
                    context?.runOnUiThread {
                        v!!.ripple0.stopRippleAnimation()
                    }
                },
                167
            )
            takeValue("0")
        }
        v!!.imageBackspace.setOnClickListener {
            Analytics.logSearchKeyboardDel()
            v!!.inputText.text = v!!.inputText.text.dropLast(1)
        }
        v!!.imageBackspace.setOnLongClickListener {
            Analytics.logSearchKeyboardClear()
            v!!.inputText.text = ""
            true
        }
        v!!.imageClear.setOnClickListener {
            if (v!!.inputText.text.isNotEmpty()) {
                v!!.inputText.text = ""
            } else {
                closeButtonClickCallback()
            }
        }
        v!!.btnCall.setOnClickListener {
            if (v!!.inputText.text.isNotEmpty()) {
                callButtonClickCallback(v!!.inputText.text.toString())
            }
        }

        v!!.settingsButton.setOnClickListener {
            settingsButtonClickCallback()
        }

        return v!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        v = null
    }

    private fun takeValue(num: String) {
        Analytics.logKeyboardButton()
        v!!.inputText.text = v!!.inputText.text.toString().plus(num)
    }
}
