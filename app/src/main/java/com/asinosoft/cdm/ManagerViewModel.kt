package com.asinosoft.cdm

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Vibrator
import android.util.Log
import android.view.DragEvent
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.asinosoft.cdm.Metoths.Companion.dp
import com.asinosoft.cdm.Metoths.Companion.setSize
import com.asinosoft.cdm.Metoths.Companion.vibrateSafety
import com.asinosoft.cdm.adapters.AdapterCallLogs
import com.asinosoft.cdm.api.CursorApi.getHistoryListLatest
import com.asinosoft.cdm.databinding.ActivityManagerBinding
import com.github.tamir7.contacts.Contact
import com.github.tamir7.contacts.Contacts
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.wickerlabs.logmanager.LogsManager
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.coroutines.*
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.vibrator
import timber.log.Timber

/**
 * Класс фоновой логики главного экрана
 */
class ManagerViewModel : ViewModel() {
    var selectedContactsLoaded = false
    companion object {
        const val VIBRO = 30L
    }

    private var i = 0
    private var posPicker = -1
    lateinit var settings: Settings
    private lateinit var touchHelper: ItemTouchHelper
    private lateinit var v: ActivityManagerBinding
    private lateinit var context: Context
    private lateinit var lifecycle: Lifecycle
    private lateinit var pickedContact: () -> Unit
    private lateinit var settingsOpen: (Settings) -> Unit
    private lateinit var logsManager: LogsManager
    private lateinit var activity: AppCompatActivity
    private var indexOfFrontChild = 0
    private val listCirs: MutableList<CircleImage> = mutableListOf()

    private var cirLayoutHeight = 0
    private val adapterCallLogs: AdapterCallLogs by lazy {
        AdapterCallLogs(
            arrayListOf(),
            context = context,
            onAdd = { v.scrollView.scrollBy(65.dp * it, 65.dp * it) })
    }
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(Keys.ManagerPreference, Context.MODE_PRIVATE)
    }
    private val moshi: Moshi by lazy {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }
    private val adapterCirMoshi: JsonAdapter<CirPairData> by lazy {
        moshi.adapter(CirPairData().javaClass)
    }
    fun updateLists(){
        this.v.recyclerView.adapter?.notifyDataSetChanged()
    }

    fun start(
        v: ActivityManagerBinding,
        context: Context,
        lifecycle: Lifecycle,
        pickedContact: () -> Unit,
        settingsOpen: (Settings) -> Unit,
        activity: AppCompatActivity
    ) {
        this.v = v

        this.context = context
        this.lifecycle = lifecycle
        this.pickedContact = pickedContact
        this.settingsOpen = settingsOpen
        Contacts.initialize(context)
        settings = Loader(context).loadSettings()
        touchHelper = ItemTouchHelper(ItemTouchCallbackCir())
        touchHelper.attachToRecyclerView(v.recyclerView)
        logsManager = LogsManager(context)
        this.activity = activity
    }

    private suspend fun updateRvHistoryHeight() {
        withContext(Dispatchers.Default) {
            with(if (settings.historyButtom) v.recyclerViewHistoryBottom else v.recyclerViewHistory) {
                adapter?.let { adapterThis ->
                    val h = adapterThis.itemCount * 65.dp
                    withContext(Dispatchers.Main) {
                        setSize(height = h)
                    }
                }
            }
        }
    }

    private fun updateHistoryList() {
        setHistoryVisibleDown(settings.historyButtom)
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

            this.setItemViewCacheSize(20)

            GlobalScope.launch(Dispatchers.IO) {
                Timber.d("getHistoryListLatest")
                getHistoryListLatest(context!!)?.let {
                    context?.runOnUiThread {
                        Timber.d("adapterCallLogs.setList %d records", it.size)
                        adapterCallLogs.setList(it)
                    }
                }
            }
        }
    }

    private fun setHistoryVisibleDown(Visibility: Boolean) {
        v.rvDown.visibility = if (Visibility) VISIBLE else GONE
        v.rvTop.visibility = if (!Visibility) VISIBLE else GONE
    }

    private suspend fun loadCirs(onNext: (CircleImage) -> Unit = {}): ArrayList<CircleImage>? =
        withContext(Dispatchers.IO) {
            sharedPreferences.getString(Keys.Cirs, null)?.let {
                val list = it.split("<end>").dropLast(1)
                val r = ArrayList<CircleImage>()
                list.forEach { item ->
                    adapterCirMoshi.fromJson(item)?.let { pair ->
                        r.add(newCir(settings, touchHelper, pair).also(onNext))
                    }
                }
                if (r.size == 0) (1..9).forEach {
                    r.add(newCir(settings, touchHelper).also(onNext))
                }
                return@withContext r
            }
            return@withContext null
        }

    private  fun getCirs(): MutableList<CircleImage>?
    {
        val r = mutableListOf<CircleImage>()
            sharedPreferences.getString(Keys.Cirs, null)?.let {
                val list = it.split("<end>").dropLast(1)
                list.forEach { item ->
                    adapterCirMoshi.fromJson(item)?.let { pair ->
                        r.add(newCir(settings, touchHelper, pair))
                    }
                }
                if (r.size == 0) (1..9).forEach {
                    r.add(newCir(settings, touchHelper))
                }
                return r
            } ?: kotlin.run {
                (1..9).forEach {
                r.add(newCir(settings, touchHelper))
            }
            }
            return r
        }


    private fun onChangeHeightCir(h: Int) {
        cirLayoutHeight = h
        v.recyclerView.setSize(height = h)
    }

    fun initViews(updateHistory: Boolean = true) {
        Timber.d("initViews %s", updateHistory)

        if (updateHistory) updateHistoryList()
        v.recyclerView.adapter = CirAdapter(
            listCirs,
            context,
            settings,
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        )
        listCirs.forEach {
            it.size = settings.sizeCirs
        }

        v.recyclerView.layoutManager =
            CirLayoutManager({ onChangeHeightCir(it) }, columns = settings.columnsCirs)
        v.recyclerView.itemAnimator = LandingAnimator(OvershootInterpolator())
        GlobalScope.launch { withTimeout(2_000L) { v.recyclerView.adapter?.notifyDataSetChanged() } }
        v.recyclerView.setChildDrawingOrderCallback { childCount, iteration ->
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
        if(!selectedContactsLoaded) {
            listCirs.addAll(getCirs() as List<CircleImage>)
            selectedContactsLoaded = true
        }
        v.deleteCir.apply {
            setOnDragListener { view, event ->
                try {
                    val viewIt =
                        (v.recyclerView.adapter as CirAdapter).items[((v.recyclerView.adapter as CirAdapter).posDrag)]
                    when (event.action) {
                        DragEvent.ACTION_DRAG_ENTERED -> {
                            context.vibrator.vibrateSafety(VIBRO)
                        }
                        DragEvent.ACTION_DROP -> viewIt.deleteListener()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                true
            }

        }

        v.fab.setOnClickListener { fab ->
            newCir(settings, touchHelper).let {
                (v.recyclerView.adapter as CirAdapter).addItem(it)
            }
        }
        v.editCir.setOnDragListener { _, event ->
            if (event.action == DragEvent.ACTION_DRAG_ENTERED)
                newCir(settings, touchHelper).let {
                    (v.recyclerView.adapter as CirAdapter).addItem(it)
                }
            true
        }
        v.fabSettings.setOnClickListener { fab ->
            settingsOpen(settings)
        }
        v.fabSearch.setOnClickListener { fab ->
            Intent(context, SearchActivity::class.java).let(context::startActivity)
        }
        v.settingsButton.setOnClickListener {
            settingsOpen(settings)
        }
        v.buttonRVDownUpdate.setOnClickListener {
            adapterCallLogs.upIntoBuffer()
        }
        v.buttonRVTopUpdate.setOnClickListener {
            adapterCallLogs.upIntoBuffer()
        }
    }

    fun newCir(settings: Settings, touchHelper: ItemTouchHelper) = CircleImage(
        context,
        powerMenu = MoreMenuFactory().create(context) { return@create lifecycle }, swiping = true,
        lockableNestedScrollView = v.scrollView,
        menuEnable = settings.cirMenu,
        deleteCir = v.deleteCir, editCir = v.editCir
    ).apply {
        size = settings.sizeCirs
        id = Keys.idCir
        tag = i++
        borderWidth = settings.borderWidthCirs.toFloat()
        borderColor = settings.colorBorder
        replaceListener = {
            touchHelper.startDrag(it)
        }
        pickContact = { pos ->
            posPicker = pos
            pickedContact()
        }
        touchDown = {
            indexOfFrontChild = it
        }
    }

    fun newCir(settings: Settings, touchHelper: ItemTouchHelper, pair: CirPairData) =
        newCir(settings, touchHelper).apply {
            pair.selectedNumber?.let {
                selectedNumber = it
            }
            pair.contact?.let {
                contact = it
            }
            replaceListener = {
                touchHelper.startDrag(it)
            }
            pickContact = { pos ->
                posPicker = pos
                pickedContact()
            }
        }

    fun saveCirs() {
        var str = ""
        listCirs.forEach {
            str += adapterCirMoshi.toJson(CirPairData(it.contact, it.contactSettings,it.selectedNumber)) + "<end>"
        }
        sharedPreferences.edit().apply {
            putString(Keys.Cirs, str)
        }.apply()
        Log.d("${this.javaClass}", "saveCirs: saved = $str")
    }

    fun onDestroy() {
        saveCirs()
    }

    fun onResult(contact: Contact, number : String? = null){
        (v.recyclerView.adapter as CirAdapter).setContact(posPicker, contact, number)
    }

}
