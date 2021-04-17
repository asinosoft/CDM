package com.asinosoft.cdm

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

/**
 * Лейаут с возможностью блокировки пролистывания,
 * которая используется, когда CircleImage переходит в режим выбора действия
 */
class LockableLayoutManager(
    context: Context,
    reverse: Boolean
) : LinearLayoutManager(context, VERTICAL, reverse) {
    private var isScrollingEnabled = true

    override fun canScrollVertically(): Boolean {
        return isScrollingEnabled
    }

    override fun supportsPredictiveItemAnimations(): Boolean {
        return false
    }

    fun setScrollingEnabled(flag: Boolean) {
        isScrollingEnabled = flag
    }
}
