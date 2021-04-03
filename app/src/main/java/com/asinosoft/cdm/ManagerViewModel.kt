package com.asinosoft.cdm

import android.content.Context
import android.net.Uri
import android.os.Vibrator
import android.provider.ContactsContract
import android.view.DragEvent
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.OvershootInterpolator
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.asinosoft.cdm.Metoths.Companion.dp
import com.asinosoft.cdm.Metoths.Companion.setSize
import com.asinosoft.cdm.Metoths.Companion.vibrateSafety
import com.asinosoft.cdm.adapters.AdapterCallLogs
import com.asinosoft.cdm.api.FavoriteContact
import com.asinosoft.cdm.api.FavoriteContactRepositoryImpl
import com.asinosoft.cdm.databinding.ActivityManagerBinding
import com.asinosoft.cdm.detail_contact.Contact
import jp.wasabeef.recyclerview.animators.LandingAnimator
import org.jetbrains.anko.vibrator
import timber.log.Timber

/**
 * Класс фоновой логики главного экрана
 */
class ManagerViewModel : ViewModel() {
    companion object {
        const val VIBRO = 30L
    }

    private var settings = Loader.loadSettings()
    private lateinit var v: ActivityManagerBinding
    private lateinit var context: Context
    private lateinit var pickContact: (Int) -> Unit

    private val adapterCallLogs: AdapterCallLogs by lazy {
        AdapterCallLogs(
            context = context,
            onAdd = { v.scrollView.scrollBy(65.dp * it, 65.dp * it) }
        )
    }
    private val favoritesAdapter: CirAdapter by lazy {
        CirAdapter(
            FavoriteContactRepositoryImpl(
                App.contactRepository,
                context.getSharedPreferences(Keys.ManagerPreference, Context.MODE_PRIVATE)
            ),
            v.scrollView,
            v.deleteCir,
            v.editCir,
            pickContact,
            context,
            settings,
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        )
    }

    fun showHiddenCallHistoryItems() {
        adapterCallLogs.showHiddenItems()
    }

    fun updateLists() {
        adapterCallLogs.setList(App.callHistoryRepository.getLatestHistory())
    }

    fun start(
        v: ActivityManagerBinding,
        context: Context,
        pickContact: (Int) -> Unit
    ) {
        this.v = v
        this.context = context
        this.pickContact = pickContact
        initViews()
    }

    private fun initViews(updateHistory: Boolean = true) {
        Timber.d("initViews %s", updateHistory)

        initRecyclerViewFavorites()
        initRecyclerViewHistory()
        initButtons()
    }

    private fun initRecyclerViewHistory() {
        if (settings.historyButtom) {
            v.rvDown.visibility = VISIBLE
            v.rvTop.visibility = GONE
        } else {
            v.rvDown.visibility = GONE
            v.rvTop.visibility = VISIBLE
        }

        with(if (settings.historyButtom) v.recyclerViewHistoryBottom else v.recyclerViewHistory) {
            layoutManager = object : LinearLayoutManager(
                context,
                VERTICAL,
                !settings.historyButtom
            ) {
                override fun supportsPredictiveItemAnimations(): Boolean {
                    return false
                }
            }
            isNestedScrollingEnabled = true
            adapter = adapterCallLogs.apply { if (settings.historyButtom) onAdd = {} }
        }
    }

    private fun initRecyclerViewFavorites() {
        v.rvFavorites.adapter = favoritesAdapter
        v.rvFavorites.layoutManager =
            CirLayoutManager(
                { v.rvFavorites.setSize(height = it) },
                columns = settings.columnsCirs
            )
        v.rvFavorites.itemAnimator = LandingAnimator(OvershootInterpolator())
    }

    private fun initButtons() {
        v.deleteCir.apply {
            setOnDragListener { view, event ->
                try {
                    when (event.action) {
                        DragEvent.ACTION_DRAG_ENTERED -> {
                            context.vibrator.vibrateSafety(VIBRO)
                        }
                        DragEvent.ACTION_DROP -> {
                            event.clipData.getItemAt(0)?.text.let {
                                val position: Int = Integer.parseInt(it.toString())
                                favoritesAdapter.deleteItem(position)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                true
            }
        }

        v.editCir.setOnDragListener { _, event ->
            if (event.action == DragEvent.ACTION_DRAG_ENTERED)
                favoritesAdapter.addItem(FavoriteContact())
            true
        }
    }

    fun setFavoriteContact(position: Int, contact: FavoriteContact) {
        favoritesAdapter.setItem(position, contact)
    }

    fun getContacts() = App.contactRepository.getContacts().sortedBy { it.name }

    fun getContactIdFromIntent(uri: Uri): Contact? {
        val projections = arrayOf(ContactsContract.Contacts._ID)
        val cursor =
            App.INSTANCE.contentResolver.query(uri, projections, null, null, null)
        var id = 0L
        if (cursor != null && cursor.moveToFirst()) {
            val i = cursor.getColumnIndex(projections[0])
            id = cursor.getLong(i)
        }
        cursor?.close()
        return App.contactRepository.getContactById(id)
    }
}
