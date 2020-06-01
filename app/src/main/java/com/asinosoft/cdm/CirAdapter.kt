package com.asinosoft.cdm

import android.content.ClipData
import android.content.Context
import android.os.Vibrator
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.contains
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.Metoths.Companion.setSize
import com.asinosoft.cdm.Metoths.Companion.vibrateSafety
import com.asinosoft.cdm.databinding.ItemCirBinding
import com.github.tamir7.contacts.Contact
import java.util.Collections.swap

class CirAdapter(var items: ArrayList<CircleImage>, val context: Context, val settings: Settings, val vibrator: Vibrator): RecyclerView.Adapter<CirAdapter.Holder>(){

    var posDrag = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(ItemCirBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    fun setContact(pos: Int, contact: Contact){
        items[pos].contact = contact
        notifyItemChanged(pos)
    }

    override fun onBindViewHolder(holder: CirAdapter.Holder, position: Int) {
        holder.bind(items[position])
    }

    fun addItem(item: CircleImage){
        items.add(item)
        notifyItemInserted(items.lastIndex)
    }

    inner class Holder(val v: ItemCirBinding): RecyclerView.ViewHolder(v.root){

        fun bind(cir: CircleImage){

            v.root.let {
                if (it.contains(cir)) it.removeView(cir)
                if (cir.parent != null) (cir.parent as ViewGroup).removeView(cir)
                it.addView(cir.apply {
                    setActionImage(v.actionView)
                    this.directActions = settings.toDirectActions()
                    deleteListener = {
                        items.removeAt(absoluteAdapterPosition)
                        notifyItemRemoved(absoluteAdapterPosition)
                        posDrag = -1
                    }
                    replaceListenerForHolder = {
                        replaceListener(this@Holder)
                    }
                    dragListener = {
                        posDrag = absoluteAdapterPosition
                        startDragAndDrop(ClipData.newPlainText(Keys.adapterPos, absoluteAdapterPosition.toString()),
                            View.DragShadowBuilder(this), 0, View.DRAG_FLAG_GLOBAL)
                    }
                    pickContactForNum = {
                        pickContact(absoluteAdapterPosition)
                    }

                    setOnDragListener { v, event ->
                        when(event.action){
                            DragEvent.ACTION_DRAG_ENTERED -> {
                                vibrator.vibrateSafety(10)
                                setImageDrawable(items[posDrag].drawable.also { items[posDrag].setImageDrawable(this.drawable) })
                            }
                            DragEvent.ACTION_DRAG_EXITED -> {
                                setImageDrawable(items[posDrag].drawable.also { items[posDrag].setImageDrawable(this.drawable) })
                            }
                            DragEvent.ACTION_DRAG_ENDED -> setOptionalCirsVisible(false)
                            DragEvent.ACTION_DROP -> {
                                setImageDrawable(items[posDrag].drawable.also { items[posDrag].setImageDrawable(this.drawable) })
                                swapItems(posDrag, absoluteAdapterPosition)
                                return@setOnDragListener true}
                        }
                        true
                    }
//                    Log.d("CirAdapter", "Bind --> pos = $adapterPosition; holder = ${this@Holder}")
            })
                it.setSize((cir.size + cir.animationRadius * 2).toInt())
            }
        }

        private fun swapItems(pos: Int, adapterPosition: Int) {
            swap(items, pos, adapterPosition)
            notifyItemChanged(pos)
            notifyItemChanged(adapterPosition)
        }
    }
}