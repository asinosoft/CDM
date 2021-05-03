package com.asinosoft.cdm

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Vibrator
import android.telecom.TelecomManager
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.animation.OvershootInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.Metoths.Companion.vibrateSafety
import com.asinosoft.cdm.adapters.AdapterCallLogs
import com.asinosoft.cdm.adapters.NumbeAdapter
import com.asinosoft.cdm.api.ContactRepositoryImpl
import com.asinosoft.cdm.api.FavoriteContact
import com.asinosoft.cdm.api.FavoriteContactRepositoryImpl
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.databinding.ActivityManagerBinding
import com.asinosoft.cdm.databinding.FavoritesFragmentBinding
import com.asinosoft.cdm.dialer.Utilities
import com.asinosoft.cdm.globals.AlertDialogUtils
import com.jaeger.library.StatusBarUtil
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.android.synthetic.main.activity_manager.*
import org.jetbrains.anko.vibrator
import timber.log.Timber

/**
 * Основной класс приложения, отвечает за работу главного экрана (нового) приложения
 */
class ManagerActivity : AppCompatActivity() {
    private val model: ManagerViewModel by viewModels()

    /**
     * Элемент, хранящий ссылки на все представления привязанного макета
     */
    private lateinit var v: ActivityManagerBinding

    /**
     * Блок избранных контактов
     */
    private lateinit var favoritesView: FavoritesFragmentBinding

    /**
     * Номер избранного контакта, на котором находится палец пользователя - чтобы отрисовать его в последнюю очередь (поверх остальных)
     */
    private var indexOfFrontChild: Int = 0

    private lateinit var adapterCallLogs: AdapterCallLogs
    private lateinit var favoritesAdapter: CirAdapter

    private val PERMISSIONS = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.WRITE_CALL_LOG
    )

    /**
     * Позиция в блоке избранных контактов, для которой был запущен диалог выбора контакта
     * TODO: найти способ пробросить номер позиции через Activity..PickContact
     */
    private var pickedPosition: Int = 0

    /**
     * Настройки приложения
     */
    private val settingsActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            // После изменения настроек пересоздаем весь интерфейс
            initActivity()
            model.refresh(this)
        }

    /**
     * Многофункциональное окно с клавиатурой
     */
    private val searchActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result?.let {
                when (it.resultCode) {
                    SearchActivity.RESULT_OPEN_SETTINGS -> {
                        Timber.d("Open settings")
                        settingsActivityResult.launch(Intent(this, SettingsActivity::class.java))
                    }
                    SearchActivity.RESULT_CALL -> {
                        it.data?.extras?.getString(Keys.number)?.let { phoneNumber ->
                            Timber.d("CALL: $phoneNumber")
                            Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Uri.encode(phoneNumber)))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .let { startActivity(it) }
                        }
                    }
                }
            }
        }

    /**
     * Выбор контакта
     */
    private val pickContact =
        registerForActivityResult(ActivityResultContracts.PickContact()) { uri ->
            if (null != uri) {
                model.getContactByUri(this, uri)?.let { contact ->
                    if (contact.phones.size > 1) {
                        showNumberDialog(contact, pickedPosition)
                    } else {
                        val favorite = FavoriteContact(contact, contact.phones.first().value)
                        favoritesAdapter.setItem(pickedPosition, favorite)
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        v = ActivityManagerBinding.inflate(layoutInflater)
        setContentView(v.root)
        if (!hasPermissions(this, *PERMISSIONS)) {
            requestAllPermissions()
        }
        initActivity()

        model.getLatestCalls().observe(this) { calls ->
            adapterCallLogs.setList(calls)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        pickedPosition = savedInstanceState.getInt("pickedPosition")
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("pickedPosition", pickedPosition)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        Timber.d("onResume")
        super.onResume()
        if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.READ_CONTACTS)) {
            Timber.d("%s NOT PERMITTED!", Manifest.permission.READ_CONTACTS)
            return
        }

        model.refresh(this)
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        offerReplacingDefaultDialer()
        super.onDestroy()
    }

    private fun requestAllPermissions() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, 0)
    }

    private fun hasPermissions(context: Context?, vararg permissions: String): Boolean {
        if (context != null) {
            for (permission in permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }

    private fun initActivity() {
        if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.READ_CONTACTS)) {
            Timber.d("%s NOT PERMITTED!", Manifest.permission.READ_CONTACTS)
            return
        }

        StatusBarUtil.setTranslucentForImageView(this, v.container)

        val callsLayoutManager =
            LockableLayoutManager(this, !Loader.loadSettings(this).historyButtom)

        initFavorites(callsLayoutManager)
        initCallHistory(callsLayoutManager)

        val isDefaultDealer: Boolean = Utilities().checkDefaultDialer(this)
        if (isDefaultDealer) {
            checkPermission()
        }

        fabKeyboard.setOnClickListener {
            searchActivityResult.launch(Intent(this, SearchActivity::class.java))
        }
    }

    private fun initFavorites(callsLayoutManager: LockableLayoutManager) {
        val context = this
        favoritesView = FavoritesFragmentBinding.inflate(
            LayoutInflater.from(this),
            v.root,
            false
        ).apply {
            rvFavorites.layoutManager =
                CirLayoutManager(columns = Loader.loadSettings(context).columnsCirs)
            rvFavorites.itemAnimator = LandingAnimator(OvershootInterpolator())
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

            favoritesAdapter = CirAdapter(
                FavoriteContactRepositoryImpl(
                    context,
                    ContactRepositoryImpl(context)
                ),
                callsLayoutManager,
                btnDelete,
                btnEdit,
                { position -> pickContact(position) },
                { indexOfFrontChild = it },
                context,
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            )
            rvFavorites.adapter = favoritesAdapter

            btnDelete.apply {
                setOnDragListener { view, event ->
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
        adapterCallLogs = AdapterCallLogs(this, favoritesView)

        v.rvCalls.adapter = adapterCallLogs
        v.rvCalls.layoutManager = callsLayoutManager
        v.rvCalls.isNestedScrollingEnabled = true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Timber.i(grantResults.toString())
        initActivity()
        this.onResume()
    }

    private fun checkPermission() {
        Utilities().askForPermissions(this, Utilities().MUST_HAVE_PERMISSIONS)
    }

    private fun offerReplacingDefaultDialer() {
        if (getSystemService(TelecomManager::class.java).defaultDialerPackage != packageName) {
            Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                .let(::startActivity)
        }
    }

    private fun pickContact(position: Int) {
        pickedPosition = position
        pickContact.launch(null)
    }

    private fun showNumberDialog(contact: Contact, position: Int) {
        contact.let {
            val dialog = AlertDialogUtils.dialogListWithoutConfirm(this, "Выберите номер")
            val adapter = NumbeAdapter {
                val favorite = FavoriteContact(contact, it)
                favoritesAdapter.setItem(position, favorite)
                dialog.dismiss()
            }
            val recyclerView = dialog.findViewById<RecyclerView>(R.id.recycler_popup)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter
            adapter.setData(contact.phones.map { it.value })
            dialog.show()
        }
    }
}
