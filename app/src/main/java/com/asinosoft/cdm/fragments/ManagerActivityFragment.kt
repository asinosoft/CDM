package com.asinosoft.cdm.fragments

import android.content.Context
import android.os.Bundle
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.R
import com.asinosoft.cdm.adapters.CallsAdapter
import com.asinosoft.cdm.adapters.FavoritesAdapter
import com.asinosoft.cdm.api.ContactRepositoryImpl
import com.asinosoft.cdm.api.FavoriteContactRepositoryImpl
import com.asinosoft.cdm.api.Loader
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.data.FavoriteContact
import com.asinosoft.cdm.databinding.ActivityManagerBinding
import com.asinosoft.cdm.databinding.FavoritesFragmentBinding
import com.asinosoft.cdm.helpers.Keys
import com.asinosoft.cdm.helpers.Metoths.Companion.vibrateSafety
import com.asinosoft.cdm.helpers.SelectPhoneDialog
import com.asinosoft.cdm.helpers.vibrator
import com.asinosoft.cdm.viewmodels.ManagerViewModel
import com.asinosoft.cdm.views.CirLayoutManager
import com.asinosoft.cdm.views.LockableLayoutManager

/**
 * Интерфейс главного окна (избранные + последние звонки)
 */
class ManagerActivityFragment : Fragment(), CallsAdapter.Handler {
    private var v: ActivityManagerBinding? = null
    private val model: ManagerViewModel by activityViewModels()

    /**
     * Блок избранных контактов
     */
    private lateinit var favoritesView: FavoritesFragmentBinding

    /**
     * Раскладка окна - избранные вверху/внизу
     */
    private var favoritesFirst: Boolean = true

    /**
     * Номер избранного контакта, на котором находится палец пользователя - чтобы отрисовать его в последнюю очередь (поверх остальных)
     */
    private var indexOfFrontChild: Int = 0

    private lateinit var favoritesAdapter: FavoritesAdapter

    /**
     * Позиция в блоке избранных контактов, для которой был запущен диалог выбора контакта
     * TODO: найти способ пробросить номер позиции через Activity..PickContact
     */
    private var pickedPosition: Int = 0

    /**
     * Выбор контакта
     */
    private val pickContact =
        registerForActivityResult(ActivityResultContracts.PickContact()) { uri ->
            model.getContactByUri(requireContext(), uri)?.let { contact ->
                if (contact.phones.count() > 1) {
                    SelectPhoneDialog(
                        requireContext(),
                        contact.phones,
                        { action ->
                            run {
                                model.setContactPhone(contact, action)
                                favoritesAdapter.setItem(pickedPosition, FavoriteContact(contact))
                            }
                        }
                    ).show()
                } else {
                    favoritesAdapter.setItem(pickedPosition, FavoriteContact(contact))
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v = ActivityManagerBinding.inflate(layoutInflater)
        pickedPosition = savedInstanceState?.getInt("pickedPosition") ?: 0
        return v!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        v = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("pickedPosition", pickedPosition)
        super.onSaveInstanceState(outState)
    }

    override fun onClickContact(contact: Contact) {
        findNavController().navigate(
            R.id.action_open_contact_fragment,
            bundleOf("contactId" to contact.id)
        )
    }

    override fun onClickPhone(phone: String) {
        findNavController().navigate(R.id.action_open_phone_history, bundleOf("phone" to phone))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        favoritesFirst = !Loader.loadSettings(requireContext()).historyButtom
        val callsLayoutManager = LockableLayoutManager(requireContext(), favoritesFirst)

        initFavorites(callsLayoutManager)
        initCallHistory(callsLayoutManager)

        v!!.fabKeyboard.supportImageTintList = null
        v!!.fabKeyboard.setOnClickListener {
            findNavController().navigate(R.id.action_open_search)
        }

        model.calls.observe(viewLifecycleOwner) { calls ->
            (v!!.rvCalls.adapter as CallsAdapter).setList(calls)
        }
    }

    private fun initFavorites(callsLayoutManager: LockableLayoutManager) {
        val context: Context = requireContext()
        favoritesView = FavoritesFragmentBinding.inflate(
            layoutInflater,
            v!!.root,
            false
        ).apply {
            rvFavorites.layoutManager =
                CirLayoutManager(columns = Loader.loadSettings(context).columnsCirs)
            rvFavorites.setChildDrawingOrderCallback { childCount, iteration ->
                // Изменяем порядок отрисовки избранных контактов, чтобы контакт
                // на котором находится палец пользователя, отрисовывался в последнюю очередь,
                // поверх остальных контактов
                var childPos: Int = iteration
                if (indexOfFrontChild < childCount) {
                    if (iteration == childCount - 1) {
                        childPos = indexOfFrontChild
                    } else if (iteration >= indexOfFrontChild) {
                        childPos = iteration + 1
                    }
                }
                childPos
            }

            favoritesAdapter = FavoritesAdapter(
                FavoriteContactRepositoryImpl(context, ContactRepositoryImpl(context)),
                callsLayoutManager,
                btnDelete,
                btnEdit,
                { contact -> onClickContact(contact) },
                { position -> pickedPosition = position; pickContact.launch(null) },
                { indexOfFrontChild = it },
                context,
                context.vibrator
            )
            rvFavorites.adapter = favoritesAdapter

            btnDelete.apply {
                setOnDragListener { _, event ->
                    try {
                        when (event.action) {
                            DragEvent.ACTION_DRAG_ENTERED -> {
                                context.vibrator.vibrateSafety(Keys.VIBRO)
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

            btnEdit.setOnDragListener { _, event ->
                if (event.action == DragEvent.ACTION_DRAG_ENTERED)
                    favoritesAdapter.addItem(FavoriteContact())
                true
            }
        }
    }

    private fun initCallHistory(callsLayoutManager: LockableLayoutManager) {
        v!!.rvCalls.adapter = CallsAdapter(requireContext(), favoritesView, this)
        v!!.rvCalls.layoutManager = callsLayoutManager
        v!!.rvCalls.isNestedScrollingEnabled = true

        // Подгрузка истории звонков, когда список докрутился до последнего элемента
        v!!.rvCalls.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollVertically(if (favoritesFirst) -1 else 1)) {
                    model.getMoreCalls()
                }
            }
        })
    }
}
