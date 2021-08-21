package com.asinosoft.cdm.fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.asinosoft.cdm.databinding.KeyboardBinding
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import org.jetbrains.anko.support.v4.runOnUiThread

/**
 * Класс кастомной клавиатуры.
 */
class KeyboardFragment : Fragment() {
    private lateinit var v: KeyboardBinding
    private var settingsButtonClickCallback: () -> Unit = {}
    private var callButtonClickCallback: (phoneNumber: String) -> Unit = {}
    private var closeButtonClickCallback: () -> Unit = {}

    /**
     * Реакция на изменение текста в строке поиска
     */
    fun doOnTextChanged(callback: (String) -> Unit) {
        v.inputText.doOnTextChanged { s, _, _, _ -> callback(s.toString()) }
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
        v.oneBtn.setOnClickListener {
            v.ripple1.startRippleAnimation()
            Handler().postDelayed(
                { runOnUiThread { v.ripple1.stopRippleAnimation() } },
                500 / 3
            )
            takeValue("1")
        }
        v.twoBtn.setOnClickListener {
            v.ripple2.startRippleAnimation()
            Handler().postDelayed(
                { runOnUiThread { v.ripple2.stopRippleAnimation() } },
                500 / 3
            )
            takeValue("2")
        }
        v.threeBtn.setOnClickListener {
            v.ripple3.startRippleAnimation()
            Handler().postDelayed(
                { runOnUiThread { v.ripple3.stopRippleAnimation() } },
                500 / 3
            )
            takeValue("3")
        }
        v.fourBtn.setOnClickListener {
            v.ripple4.startRippleAnimation()
            Handler().postDelayed(
                { runOnUiThread { v.ripple4.stopRippleAnimation() } },
                500 / 3
            )
            takeValue("4")
        }
        v.fiveBtn.setOnClickListener {
            v.ripple5.startRippleAnimation()
            Handler().postDelayed(
                { runOnUiThread { v.ripple5.stopRippleAnimation() } },
                500 / 3
            )
            takeValue("5")
        }
        v.sixBtn.setOnClickListener {
            v.ripple6.startRippleAnimation()
            Handler().postDelayed(
                { runOnUiThread { v.ripple6.stopRippleAnimation() } },
                500 / 3
            )
            takeValue("6")
        }
        v.sevenBtn.setOnClickListener {
            v.ripple7.startRippleAnimation()
            Handler().postDelayed(
                { runOnUiThread { v.ripple7.stopRippleAnimation() } },
                500 / 3
            )
            takeValue("7")
        }
        v.eightBtn.setOnClickListener {
            v.ripple8.startRippleAnimation()
            Handler().postDelayed(
                { runOnUiThread { v.ripple8.stopRippleAnimation() } },
                500 / 3
            )
            takeValue("8")
        }
        v.nineBtn.setOnClickListener {
            v.ripple9.startRippleAnimation()
            Handler().postDelayed(
                { runOnUiThread { v.ripple9.stopRippleAnimation() } },
                500 / 3
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
                    runOnUiThread {
                        v.ripple0.stopRippleAnimation()
                    }
                },
                500 / 3
            )
            takeValue("0")
        }
        v.imageBackspace.setOnClickListener {
            v.inputText.text = v.inputText.text.dropLast(1)
        }
        v.imageBackspace.setOnLongClickListener {
            v.inputText.text = ""
            true
        }
        v.imageClear.setOnClickListener {
            if (v.inputText.text.isNotEmpty()) {
                v.inputText.text = ""
            } else {
                closeButtonClickCallback()
            }
        }
        v.btnCall.setOnClickListener {
            if (v.inputText.text.isNotEmpty()) {
                callButtonClickCallback(v.inputText.text.toString())
            }
        }

        v.settingsButton.setOnClickListener {
            settingsButtonClickCallback()
        }

        return v.root
    }

    private fun takeValue(num: String) {
        Firebase.analytics.logEvent("keyboard_button", Bundle.EMPTY)
        v.inputText.text = v.inputText.text.toString().plus(num)
    }
}