package com.asinosoft.cdm.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.asinosoft.cdm.databinding.FragmentIncomingCallBinding
import timber.log.Timber
import kotlin.math.absoluteValue

class IncomingCallFragment : Fragment() {
    private lateinit var v: FragmentIncomingCallBinding

    private var startY = 0f
    private var maxY = 0f
    private var isDragged = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.d("IncomingCallFragment: onCreateView")
        v = FragmentIncomingCallBinding.inflate(inflater, container, false)

        v.root.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> startDrag(e)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> endDrag()
                MotionEvent.ACTION_MOVE -> drag(e)
                else -> false
            }
        }

        animateArrowUp()
        animateArrowDown()

        return v.root
    }

    private fun startDrag(e: MotionEvent): Boolean {
        maxY = (v.accept.top - v.handle.top).absoluteValue.toFloat()
        startY = e.rawY
        isDragged = true

        return true
    }

    private fun endDrag(): Boolean {
        if (!isDragged) {
            return false
        }

        v.handle.animate()
            .translationY(0f)
            .setDuration(300)
            .start()

        v.accept.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .start()

        v.reject.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .start()

        isDragged = false
        return true
    }

    private fun drag(e: MotionEvent): Boolean {
        if (!isDragged) {
            return false
        }

        val position = (e.rawY - startY).coerceAtLeast(-maxY).coerceAtMost(maxY)
        v.handle.translationY = position

        ((maxY - position) / maxY).coerceAtLeast(1f).let {
            v.accept.scaleX = it
            v.accept.scaleY = it
        }

        ((maxY + position) / maxY).coerceAtLeast(1f).let {
            v.reject.scaleX = it
            v.reject.scaleY = it
        }

        return true
    }

    private fun animateArrowUp() {
        v.animatedArrowUp.translationY = 400f

        v.animatedArrowUp.animate()
            .translationY(0.0f)
            .setDuration(999)
            .withEndAction { animateArrowUp() }
            .start()
    }

    private fun animateArrowDown() {
        v.animatedArrowDown.translationY = -400f

        v.animatedArrowDown.animate()
            .translationY(0.0f)
            .setDuration(999)
            .withEndAction { animateArrowDown() }
            .start()
    }
}
