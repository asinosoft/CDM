package com.asinosoft.cdm.adapters

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
import com.asinosoft.cdm.*
import com.asinosoft.cdm.api.FavoriteContactRepository
import com.asinosoft.cdm.api.Loader
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.data.FavoriteContact
import com.asinosoft.cdm.databinding.ItemFavoriteBinding
import com.asinosoft.cdm.helpers.ItemTouchCallbackCir
import com.asinosoft.cdm.helpers.Keys
import com.asinosoft.cdm.helpers.Metoths.Companion.setSize
import com.asinosoft.cdm.helpers.Metoths.Companion.vibrateSafety
import com.asinosoft.cdm.views.CircularImageView
import com.asinosoft.cdm.views.LockableLayoutManager

class FavoritesAdapter(
    val favorites: FavoriteContactRepository,
    val callsLayoutManager: LockableLayoutManager,
    val deleteButton: CircularImageView,
    val editButton: CircularImageView,
    val openContact: (Contact) -> Unit,
    val pickContact: (Int) -> Unit,
    val onTouch: (Int) -> Unit, // Через колбэк передаётся позиция контакта, на котором находится палец пользователя
    val context: Context,
    val vibrator: Vibrator
) : RecyclerView.Adapter<FavoritesAdapter.Holder>() {

    private val touchHelper = ItemTouchHelper(ItemTouchCallbackCir())
    private val settings = Loader.loadSettings(context)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        touchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = favorites.getContacts().size

    override fun onBindViewHolder(holder: Holder, position: Int) {
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
        notifyItemChanged(pos)
        notifyItemChanged(adapterPosition)
    }

    inner class Holder(val v: ItemFavoriteBinding) : RecyclerView.ViewHolder(v.root) {

        fun bind(n: Int, favorite: FavoriteContact) {
            v.actionView.setSize(settings.sizeCirs)
            v.circleImage.apply {
                contact = favorite.contact

                setActionImage(v.actionView)
                if (null == contact) {
                    setImageResource(R.drawable.plus)
                } else {
                    setImageURI(contact?.photoUri)
                }
                lockableNestedScrollView = callsLayoutManager
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

                directActions = favorite.contact?.let { Loader.loadContactSettings(context, it) }
                deleteListener = {
                    favorites.removeContact(absoluteAdapterPosition)
                    notifyItemRemoved(absoluteAdapterPosition)
                }
                borderWidth = settings.borderWidthCirs.toFloat()
                borderColor = settings.colorBorder
                replaceListenerForHolder = {
                    replaceListener(this@Holder)
                }
                dragListener = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        startDragAndDrop(
                            ClipData.newPlainText(
                                Keys.adapterPos,
                                absoluteAdapterPosition.toString()
                            ),
                            View.DragShadowBuilder(this), 0, View.DRAG_FLAG_GLOBAL
                        )
                    }
                }
                openContact = this@FavoritesAdapter.openContact
                pickContact = { pickContact(absoluteAdapterPosition) }
                touchDownForIndex = {
                    touchDown(absoluteAdapterPosition)
                    onTouch(absoluteAdapterPosition)
                }

                setOnDragListener { _, event ->
                    when (event.action) {
                        DragEvent.ACTION_DRAG_ENTERED -> {
                            vibrator.vibrateSafety(Keys.VIBRO)
                        }
                        DragEvent.ACTION_DRAG_EXITED -> {
                        }
                        DragEvent.ACTION_DRAG_ENDED -> {
                            setOptionalCirsVisible(false)
                        }
                        DragEvent.ACTION_DROP -> {
                            val draggedPosition =
                                event.clipData.getItemAt(0).text.toString().toInt()
                            swapItems(draggedPosition, absoluteAdapterPosition)
                        }
                    }
                    true
                }
            }
            v.root.setSize((v.circleImage.size + v.circleImage.animationRadius * 2).toInt())
        }
    }
}
