package com.asinosoft.cdm

import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.core.view.size
import androidx.recyclerview.widget.RecyclerView
import java.util.Observable
import com.asinosoft.cdm.Metoths.Companion.dp
import com.google.android.material.chip.ChipDrawable
import kotlinx.android.synthetic.main.activity_search.view.*
import org.jetbrains.anko.margin
import kotlin.properties.Delegates

class CirLayoutManager(var onChangeHeight: (Int) -> Unit): RecyclerView.LayoutManager() {

    var heightUsed: Int by Delegates.observable(0, { _, _, newVal ->
        onChangeHeight(newVal)})

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT)
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        recycler?.let(this::detachAndScrapAttachedViews)
        var i = 0
        var columns: Int? = null
        while (i < itemCount)
        (recycler?.getViewForPosition(i))?.let {
            addView(it)
            val cir = (it as ConstraintLayout).findViewById<CircleImage>(R.id.Cir1)
            var sizeWithShadow = cir.size - cir.shadowRadius * 2
            val sizeSpec = (cir.size + cir.animationRadius * 2).toInt()
            if (columns == null) columns = (width / (sizeSpec * 0.75)).toInt()
            if (columns == 0) columns = 1
            measureChildWithMargins(it, sizeSpec, sizeSpec)
            val rowCount = (itemCount - 1) / columns!!
            val row = i / columns!!
            val cirInRowCount = getCirsInRowCount(itemCount, columns!!, row, rowCount)
            val marginCenter = ((width - sizeSpec * 0.75 * cirInRowCount - sizeWithShadow / 2) / 2).toInt()
            val marginLeft = (sizeWithShadow + cir.animationRadius).toInt() * (i % columns!!)  + marginCenter
            val marginTop = ((sizeWithShadow + cir.animationRadius) * row).toInt()
//            Log.d("CirLayoutManager.kt", "i = $i; sizeSpec = $sizeSpec; columns = $columns; rowCount = $rowCount; itemCount = $itemCount; row = $row; cirInRowCount = $cirInRowCount; marginCenter = $marginCenter; marginLeft = $marginLeft; marginTop = $marginTop")
            layoutDecoratedWithMargins(it, marginLeft, marginTop, sizeSpec + marginLeft, sizeSpec + marginTop)
            i++
//            if (i == itemCount) heightUsed = (cir.size + cir.animationRadius.toInt()) * i + cir.animationRadius.toInt()
            if (i == itemCount)
                heightUsed = it.measuredHeight + ((sizeWithShadow + cir.animationRadius) * ((i / columns!!).takeIf { rowit -> rowit > 0 }?: 1)).toInt()
        }
    }

    private fun getCirsInRowCount(itemCount: Int, columns: Int, row: Int, rowCount: Int): Int {
        return if (row == rowCount) if (itemCount % columns == 0) columns else itemCount % columns
        else columns
    }
}