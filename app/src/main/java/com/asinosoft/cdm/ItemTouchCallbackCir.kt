package com.asinosoft.cdm

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.util.*

/**
 * Обработчик перетаскивания кнопок избранных контактов
 */
class ItemTouchCallbackCir : ItemTouchHelper.Callback() {

    /**
     * Настройка возможности автоматического включения по долгому нажатию
     */
    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    /**
     * Флаги (траектории) движения
     */
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int = makeFlag(
        ItemTouchHelper.ACTION_STATE_DRAG,
        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    )

    /**
     * Обработчик смещения кнопки на интерактивный объект
     */
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        (recyclerView.adapter as CirAdapter).swapItems(
            viewHolder.absoluteAdapterPosition,
            target.absoluteAdapterPosition
        )
        return true
    }

    /**
     * Обработчик свайпа
     */
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }
}
