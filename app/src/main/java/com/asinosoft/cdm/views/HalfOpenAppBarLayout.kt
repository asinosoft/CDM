package com.asinosoft.cdm.views

import android.content.Context
import android.util.AttributeSet
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.appbar.AppBarLayout

/**
 * Модифицированный AppBarLayout, который при запуске наполовину сворачивается
 * Используется в окне Контакта для отрисовки фото
 */
class HalfOpenAppBarLayout(context: Context, attributeSet: AttributeSet) :
    AppBarLayout(context, attributeSet) {
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        halfOpen()
    }

    private fun halfOpen() {
        val behavior = (layoutParams as CoordinatorLayout.LayoutParams).behavior
        behavior?.onNestedPreScroll(
            parent as CoordinatorLayout,
            this,
            this,
            0,
            height / 2,
            intArrayOf(0, 0),
            ViewCompat.TYPE_TOUCH
        )
    }
}
