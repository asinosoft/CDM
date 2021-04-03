package com.asinosoft.cdm

import android.content.ClipData
import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.Metoths.Companion.setSize
import com.asinosoft.cdm.Metoths.Companion.vibrateSafety
import com.asinosoft.cdm.api.FavoriteContact
import com.asinosoft.cdm.api.FavoriteContactRepository
import com.asinosoft.cdm.databinding.ItemCirBinding
import kotlinx.android.synthetic.main.item_cir.view.*

class CirAdapter(
    val favorites: FavoriteContactRepository,
    val scrollView: LockableNestedScrollView,
    val deleteButton: CircularImageView,
    val editButton: CircularImageView,
    val pickedContact: (Int) -> Unit,
    val context: Context,
    val settings: Settings,
    val vibrator: Vibrator
) : RecyclerView.Adapter<CirAdapter.Holder>() {
    var posDrag = -1

    private val touchHelper = ItemTouchHelper(ItemTouchCallbackCir())

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        touchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemCirBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = favorites.getContacts().size

    override fun onBindViewHolder(holder: CirAdapter.Holder, position: Int) {
        holder.bind(position, favorites.getContacts()[position])
    }

    fun addItem(contact: FavoriteContact) {
        favorites.addContact(contact)
        notifyItemInserted(favorites.getContacts().lastIndex)
    }

    fun deleteItem(position: Int) {
        favorites.removeContact(position)
        notifyItemRemoved(position)
    }

    fun setItem(position: Int, contact: FavoriteContact) {
        favorites.replaceContact(position, contact)
        notifyItemChanged(position)
    }

    fun swapItems(pos: Int, adapterPosition: Int) {
        favorites.swapContacts(pos, adapterPosition)
        notifyItemMoved(pos, adapterPosition)
    }

    inner class Holder(val v: ItemCirBinding) : RecyclerView.ViewHolder(v.root) {

        fun bind(n: Int, favorite: FavoriteContact) {
            v.actionView.setSize(settings.sizeCirs)
            v.circleImage.apply {
                selectedNumber = favorite.phone
                contact = favorite.contact

                setActionImage(v.actionView)
                if (null == contact) {
                    setImageResource(R.drawable.plus)
                } else {
                    setImageDrawable(contact!!.getPhoto())
                }
                lockableNestedScrollView = scrollView
                deleteCir = deleteButton
                editCir = editButton
                size = settings.sizeCirs
                id = Keys.idCir
                tag = n
                borderWidth = settings.borderWidthCirs.toFloat()
                borderColor = settings.colorBorder
                replaceListener = {
                    touchHelper.startDrag(it)
                }
                pickContact = { pos ->
                    pickedContact(pos)
                }

                directActions = getCircleDirectActions(selectedNumber)
                deleteListener = {
                    favorites.removeContact(absoluteAdapterPosition)
                    notifyItemRemoved(absoluteAdapterPosition)
                    posDrag = -1
                }
                borderWidth = settings.borderWidthCirs.toFloat()
                borderColor = settings.colorBorder
                replaceListenerForHolder = {
                    replaceListener(this@Holder)
                }
                dragListener = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        posDrag = absoluteAdapterPosition
                        startDragAndDrop(
                            ClipData.newPlainText(
                                Keys.adapterPos,
                                absoluteAdapterPosition.toString()
                            ),
                            View.DragShadowBuilder(this), 0, View.DRAG_FLAG_GLOBAL
                        )
                    }
                }
                pickContactForNum = {
                    pickContact(absoluteAdapterPosition)
                }
                touchDownForIndex = {
                    touchDown(absoluteAdapterPosition)
                }

                setOnDragListener { _, event ->
                    when (event.action) {
                        DragEvent.ACTION_DRAG_ENTERED -> {
                            vibrator.vibrateSafety(ManagerViewModel.VIBRO)
                            /*setImageDrawable(
                                items[posDrag].drawable.also {
                                    items[posDrag].setImageDrawable(
                                        this.drawable
                                    )
                                }
                            )*/
                        }
                        DragEvent.ACTION_DRAG_EXITED -> {
                            /*setImageDrawable(
                                items[posDrag].drawable.also {
                                    items[posDrag].setImageDrawable(
                                        this.drawable
                                    )
                                }
                            )*/
                        }
                        DragEvent.ACTION_DRAG_ENDED -> setOptionalCirsVisible(false)
                        DragEvent.ACTION_DROP -> {
                            /*setImageDrawable(
                                items[posDrag].drawable.also {
                                    items[posDrag].setImageDrawable(
                                        this.drawable
                                    )
                                }
                            )*/
                            // TODO: val startPosition = event.clipData.getItemAt(0).text.toString().toInt()
                            swapItems(posDrag, absoluteAdapterPosition)
                        }
                    }
                    true
                }
            }
            v.root.setSize((v.circleImage.size + v.circleImage.animationRadius * 2).toInt())
        }
    }

    private fun getCircleDirectActions(phone: String?): DirectActions {
        return if (null == phone) {
            settings.toDirectActions()
        } else {
            Loader.loadContactSettings(phone).toDirectActions()
        }
    }
}
