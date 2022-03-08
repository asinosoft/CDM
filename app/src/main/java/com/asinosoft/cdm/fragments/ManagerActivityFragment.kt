package com.asinosoft.cdm.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telecom.PhoneAccountHandle
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.PickContact
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.App
import com.asinosoft.cdm.R
import com.asinosoft.cdm.activities.BaseActivity
import com.asinosoft.cdm.adapters.CallsAdapter
import com.asinosoft.cdm.adapters.FavoritesAdapter
import com.asinosoft.cdm.adapters.PermissionRationaleAdapter
import com.asinosoft.cdm.adapters.StringsWithIconsAdapter
import com.asinosoft.cdm.api.Config
import com.asinosoft.cdm.api.ContactRepositoryImpl
import com.asinosoft.cdm.api.FavoriteContactRepositoryImpl
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.data.FavoriteContact
import com.asinosoft.cdm.databinding.ActivityManagerBinding
import com.asinosoft.cdm.databinding.FavoritesFragmentBinding
import com.asinosoft.cdm.helpers.*
import com.asinosoft.cdm.helpers.Metoths.Companion.vibrateSafety
import com.asinosoft.cdm.viewmodels.ManagerViewModel
import com.asinosoft.cdm.views.CirLayoutManager
import com.asinosoft.cdm.views.LockableLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import timber.log.Timber
import java.security.Permission

/**
 * Интерфейс главного окна (избранные + последние звонки)
 */
class ManagerActivityFragment : Fragment(), CallsAdapter.Handler {
    private var v: ActivityManagerBinding? = null
    private val model: ManagerViewModel by activityViewModels()
    private val config: Config = App.instance!!.config

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

    private lateinit var callsAdapter: CallsAdapter

    /**
     * Позиция в блоке избранных контактов, для которой был запущен диалог выбора контакта
     * TODO: найти способ пробросить номер позиции через Activity..PickContact
     */
    private var pickedPosition: Int = 0

    /**
     * Выбор контакта
     */
    private val pickContact =
        registerForActivityResult(PickContact()) { uri ->
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
        favoritesFirst = config.favoritesFirst
        val callsLayoutManager = LockableLayoutManager(requireContext(), !favoritesFirst)

        initFavorites(callsLayoutManager)
        initCallHistory(callsLayoutManager)

        v!!.fabKeyboard.supportImageTintList = null
        v!!.fabKeyboard.setOnClickListener {
            findNavController().navigate(R.id.action_open_search)
        }

        model.isBlocked.observe(viewLifecycleOwner) { isBlocked ->
            if (isBlocked && v!!.rvCalls.adapter !is PermissionRationaleAdapter) {
                v!!.rvCalls.adapter = PermissionRationaleAdapter(favoritesView.root) {
                    requestPermissions()
                }
            } else if (!isBlocked && v!!.rvCalls.adapter !is CallsAdapter) {
                v!!.rvCalls.adapter = callsAdapter
            }
        }

        model.calls.observe(viewLifecycleOwner) { calls ->
            callsAdapter.setList(calls)
        }
    }

    private fun launchDialog() {
        (activity as BaseActivity).withPermission(arrayOf(Manifest.permission.READ_PHONE_STATE)) {
            selectPhoneAccount { sim -> Timber.d("Selected %s", sim.toString()) }
        }
    }

    @SuppressLint("MissingPermission")
    private fun selectPhoneAccount(onSelect: (PhoneAccountHandle) -> Unit) {
        Timber.d("Выбрать SIM для исходящего звонка")
        val accounts = requireContext().telecomManager.callCapablePhoneAccounts
        if (1 == accounts.size || Build.VERSION.SDK_INT < 26) {
            Timber.d("Без вариантов SIM -> ${accounts[0]}")
            onSelect(accounts[0])
        } else {
            val slots: Array<String> =
                accounts.mapNotNull {
                    requireContext().telephonyManager.createForPhoneAccountHandle(
                        it
                    )?.simOperatorName
                }
                    .toTypedArray()
            val icons: Array<Int> =
                arrayOf(R.drawable.sim1, R.drawable.sim2, R.drawable.sim3)
            val adapter = StringsWithIconsAdapter(requireContext(), slots, icons)

//            AlertDialog.Builder(context)
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.sim_selection_title)
                .setAdapter(adapter) { dialog, index ->
                    Timber.d("Выбран SIM -> ${accounts[index]}")
                    onSelect(accounts[index])
                    dialog.dismiss()
                }
                .setOnDismissListener {}
                .create()
                .show()
        }
    }

    private fun requestPermissions() {
        Timber.d("requestPermissions")
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALL_LOG),
            1235
        )
    }

    private fun initFavorites(callsLayoutManager: LockableLayoutManager) {
        val context: Context = requireContext()
        favoritesView = FavoritesFragmentBinding.inflate(
            layoutInflater,
            v!!.root,
            false
        ).apply {
            rvFavorites.layoutManager =
                CirLayoutManager(columns = config.favoritesColumnCount)
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
                config,
                FavoriteContactRepositoryImpl(context, ContactRepositoryImpl(context)),
                callsLayoutManager,
                btnDelete,
                btnEdit,
                { contact -> onClickContact(contact) },
                { position ->
                    pickedPosition = position; launchDialog() /* pickContact.launch(null) */
                },
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
        callsAdapter = CallsAdapter(config, requireContext(), favoritesView, this)
        v!!.rvCalls.layoutManager = callsLayoutManager
        v!!.rvCalls.isNestedScrollingEnabled = true

        // Подгрузка истории звонков, когда список докрутился до последнего элемента
        v!!.rvCalls.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollVertically(if (favoritesFirst) 1 else -1)) {
                    model.getMoreCalls()
                }
            }
        })
    }
}
