package com.asinosoft.cdm

import android.util.Log
import android.util.Log.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.IndexOutOfBoundsException
import kotlin.properties.Delegates

/**
 * Менеджер макета для кнопок избранных контактов.
 */
class   CirLayoutManager(var onChangeHeight: (Int) -> Unit, var offsetCirs: Int = -1, var columns: Int = 2): RecyclerView.LayoutManager() {

    var heightUsed: Int by Delegates.observable(0, { _, _, newVal ->
        onChangeHeight(newVal)
    })

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        recycler?.let(this::detachAndScrapAttachedViews)
        (0 until itemCount).forEach { i ->
            (recycler?.getViewForPosition(i))?.let {
                addView(it)
                val cir = (it as ConstraintLayout).findViewById<CircleImage>(Keys.idCir)
                val sizeSpec = (cir.size + cir.animationRadius * 2).toInt()
                var marginLeft = width * (getMarginLeftCof(columns)) - (sizeSpec / 2)
                marginLeft += width * getHorizontalCirs(i, columns)
                val marginTop = width * getVerticalCirs(i, columns)
                measureChildWithMargins(it, sizeSpec, sizeSpec)
                layoutDecoratedWithMargins(
                    it,
                    marginLeft.toInt(),
                    marginTop.toInt(),
                    sizeSpec + marginLeft.toInt(),
                    sizeSpec + marginTop.toInt()
                )
                if (i == itemCount-1)
                    heightUsed = it.measuredHeight + marginTop.toInt()
            }
        }
    }

    private fun getHorizontalCirs(i: Int, columns: Int): Float = when (columns) {
        1 -> 0f
        2 -> 0.4f
        3 -> 0.28f
        4 -> 0.23f
        else -> throw IndexOutOfBoundsException()
    } * (i % (columns))

    private fun getVerticalCirs(i: Int, columns: Int): Float = when (columns) {
        in 1..3 -> 0.28f
        4 -> 0.23f
        else -> throw IndexOutOfBoundsException()
    } * (i / (columns))

    private fun getMarginLeftCof(columns: Int): Float = when (columns) {
        1 -> 0.5f
        2 -> 0.3f
        3 -> 0.22f
        4 -> 0.15f
        else -> throw IndexOutOfBoundsException()
    }

}