package com.asinosoft.cdm

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.keyboard.*
import kotlinx.android.synthetic.main.keyboard.view.*
import org.jetbrains.anko.support.v4.runOnUiThread

/**
 * Класс кастомной клавиатуры.
 */
interface KeyBoardListener {
    fun onOpenSettings()
}

class Keyboard : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.keyboard, container, false)
        rootView.setOnTouchListener { view, motionEvent ->
            true
        }
        rootView.findViewById<View>(R.id.one_btn)
            .setOnClickListener {
                rootView.ripple1.startRippleAnimation()
                Handler().postDelayed(
                    { runOnUiThread { rootView.ripple1.stopRippleAnimation() } },
                    500 / 3
                )
                takeValue("1")
            }
        rootView.findViewById<View>(R.id.two_btn)
            .setOnClickListener {
                rootView.ripple2.startRippleAnimation()
                Handler().postDelayed(
                    { runOnUiThread { rootView.ripple2.stopRippleAnimation() } },
                    500 / 3
                )
                takeValue("2")
            }
        rootView.findViewById<View>(R.id.three_btn)
            .setOnClickListener {
                rootView.ripple3.startRippleAnimation()
                Handler().postDelayed(
                    { runOnUiThread { rootView.ripple3.stopRippleAnimation() } },
                    500 / 3
                )
                takeValue("3")
            }
        rootView.findViewById<View>(R.id.four_btn)
            .setOnClickListener {
                rootView.ripple4.startRippleAnimation()
                Handler().postDelayed(
                    { runOnUiThread { rootView.ripple4.stopRippleAnimation() } },
                    500 / 3
                )
                takeValue("4")
            }
        rootView.findViewById<View>(R.id.five_btn)
            .setOnClickListener {
                rootView.ripple5.startRippleAnimation()
                Handler().postDelayed(
                    { runOnUiThread { rootView.ripple5.stopRippleAnimation() } },
                    500 / 3
                )
                takeValue("5")
            }
        rootView.findViewById<View>(R.id.six_btn)
            .setOnClickListener {
                rootView.ripple6.startRippleAnimation()
                Handler().postDelayed(
                    { runOnUiThread { rootView.ripple6.stopRippleAnimation() } },
                    500 / 3
                )
                takeValue("6")
            }
        rootView.findViewById<View>(R.id.seven_btn)
            .setOnClickListener {
                rootView.ripple7.startRippleAnimation()
                Handler().postDelayed(
                    { runOnUiThread { rootView.ripple7.stopRippleAnimation() } },
                    500 / 3
                )
                takeValue("7")
            }
        rootView.findViewById<View>(R.id.eight_btn)
            .setOnClickListener {
                rootView.ripple8.startRippleAnimation()
                Handler().postDelayed(
                    { runOnUiThread { rootView.ripple8.stopRippleAnimation() } },
                    500 / 3
                )
                takeValue("8")
            }
        rootView.findViewById<View>(R.id.nine_btn)
            .setOnClickListener {
                rootView.ripple9.startRippleAnimation()
                Handler().postDelayed(
                    { runOnUiThread { rootView.ripple9.stopRippleAnimation() } },
                    500 / 3
                )
                takeValue("9")
            }
        rootView.findViewById<View>(R.id.zero_btn).setOnLongClickListener {
            takeValue("+")
            true
        }
        rootView.findViewById<View>(R.id.star).setOnClickListener {
            takeValue("*")
        }
        rootView.findViewById<View>(R.id.star).setOnLongClickListener {
            takeValue("#")
            true
        }
        rootView.findViewById<View>(R.id.zero_btn)
            .setOnClickListener {
                rootView.ripple0.startRippleAnimation()
                Handler().postDelayed(
                    {
                        runOnUiThread {
                            rootView.ripple0.stopRippleAnimation()
                        }
                    },
                    500 / 3
                )
                takeValue("0")
            }
        rootView.image_backspace.setOnClickListener {
            input_text.text = input_text.text.dropLast(1)
        }
        rootView.image_backspace.setOnLongClickListener {
            input_text.text = ""
            true
        }
        rootView.image_clear.setOnClickListener {
            input_text.text = ""
            activity?.onBackPressed()
        }

        rootView.settingsButton.setOnClickListener {
            (activity as KeyBoardListener).onOpenSettings()
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        input_text.addTextChangedListener(texWatcher)
    }

    private fun takeValue(num: String) {
        input_text.text = input_text.text.toString().plus(num)
    }

    val texWatcher = object : TextWatcher {

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }
    }
}
