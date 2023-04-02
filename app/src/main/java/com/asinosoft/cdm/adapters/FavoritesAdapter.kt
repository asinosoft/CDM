package com.asinosoft.cdm.adapters

import android.content.ClipData
import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.Analytics
import com.asinosoft.cdm.api.Config
import com.asinosoft.cdm.api.FavoriteContactRepository
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.data.FavoriteContact
import com.asinosoft.cdm.databinding.ItemFavoriteBinding
import com.asinosoft.cdm.helpers.AvatarHelper
import com.asinosoft.cdm.helpers.ItemTouchCallbackCir
import com.asinosoft.cdm.helpers.Keys
import com.asinosoft.cdm.helpers.Metoths.Companion.setSize
import com.asinosoft.cdm.helpers.Metoths.Companion.vibrateSafety
import com.asinosoft.cdm.views.CircularImageView
import com.asinosoft.cdm.views.LockableLayoutManager

class FavoritesAdapter(
    val config: Config,
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
            v.actionView.setSize(config.favoritesSize)
            v.circleImage.apply {
                setActionImage(v.actionView)

                val contact = favorite.contact
                if (null == contact) {
                    setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_add_contact,
                            null
                        )
                    )
                } else {
                    setImageDrawable(
                        contact.getPhoto(context) ?: AvatarHelper.generate(
                            context,
                            contact.name,
                            AvatarHelper.LONG
                        )
                    )
                }

                lockableNestedScrollView = callsLayoutManager
                deleteCir = deleteButton
                editCir = editButton
                size = config.favoritesSize
                id = Keys.idCir
                tag = n
                borderWidth = config.favoritesBorderWidth.toFloat()
                config.favoritesBorderColor?.let { borderColor = it }
                replaceListener = {
                    touchHelper.startDrag(it)
                }

                directActions = favorite.contact?.let { config.getContactSettings(it) }
                deleteListener = {
                    favorites.removeContact(absoluteAdapterPosition)
                    notifyItemRemoved(absoluteAdapterPosition)
                }
                replaceListenerForHolder = {
                    replaceListener(this@Holder)
                }
                dragListener = {
                    ViewCompat.startDragAndDrop(
                        v.root,
                        ClipData.newPlainText(
                            Keys.adapterPos,
                            absoluteAdapterPosition.toString()
                        ),
                        View.DragShadowBuilder(this), 0,
                        if (Build.VERSION.SDK_INT >= 24) View.DRAG_FLAG_GLOBAL else 0
                    )
                    // long click
                }
                openContact = {
                    Analytics.logFavoriteClick()
                    this@FavoritesAdapter.openContact(it)
                }
                pickContact = {
                    Analytics.logFavoriteClick()
                    pickContact(absoluteAdapterPosition)
                }
                touchDownForIndex = {
                    touchDown(absoluteAdapterPosition)
                    onTouch(absoluteAdapterPosition)
                }

                setOnDragListener { _, event ->
                    when (event.action) {
                        DragEvent.ACTION_DRAG_ENTERED -> {
                            vibrator.vibrateSafety(Keys.VIBRO, 255)
                            Analytics.logFavoriteLongClick()
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
