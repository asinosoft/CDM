package com.asinosoft.cdm.adapters

import android.content.ClipData
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.databinding.ActionItemBinding
import com.asinosoft.cdm.helpers.Metoths.Companion.vibrateSafety
import com.asinosoft.cdm.helpers.vibrator

/**
 * Адаптер списка действий доступных для контакта (звонок/смс/письмо/вотсап-чат/вотсап-звонок и т.д.)
 */
class ActionsAdapter : RecyclerView.Adapter<ActionsAdapter.Holder>() {

    private var actions: List<Action.Type> = listOf()

    fun setActions(actions: List<Action.Type>) {
        this.actions = actions.sortedBy { it.group.order }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = ActionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(actions[position])
    }

    override fun getItemCount(): Int = actions.size

    inner class Holder(private val actionItem: ActionItemBinding) :
        RecyclerView.ViewHolder(actionItem.layout) {
        fun bind(actionType: Action.Type) {
            with(actionItem.imgAction) {
                action = actionType
                setImageResource(
                    Action.resourceByType(actionType)
                )
                setOnLongClickListener {
                    it.context.vibrator.vibrateSafety(500)
                    startDrag(
                        ClipData.newPlainText(
                            actionType.name,
                            actionType.name
                        ),
                        View.DragShadowBuilder(it),
                        actionType,
                        0
                    )
                }
                setOnDragListener { v, event ->
                    false
                }
            }
        }
    }
}
