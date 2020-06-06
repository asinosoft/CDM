package com.asinosoft.cdm

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Vibrator
import android.provider.ContactsContract
import android.util.Log
import android.view.DragEvent
import android.view.animation.OvershootInterpolator
import androidx.core.view.doOnLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.asinosoft.cdm.Metoths.Companion.dp
import com.asinosoft.cdm.Metoths.Companion.setSize
import com.asinosoft.cdm.Metoths.Companion.vibrateSafety
import com.asinosoft.cdm.adapters.AdapterCallLogs
import com.asinosoft.cdm.databinding.ActivityManagerBinding
import com.github.tamir7.contacts.Contact
import com.github.tamir7.contacts.Contacts
import com.wickerlabs.logmanager.LogObject
import com.wickerlabs.logmanager.LogsManager
import org.jetbrains.anko.vibrator
import java.util.*
import com.asinosoft.cdm.api.CursorApi
import com.asinosoft.cdm.api.CursorApi.Companion.getCallLogs
import com.asinosoft.cdm.api.CursorApi.Companion.getHistoryListLatest
import com.google.gson.Gson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import jp.wasabeef.recyclerview.adapters.SlideInLeftAnimationAdapter
import jp.wasabeef.recyclerview.animators.*
import kotlinx.android.synthetic.main.activity_manager.view.*
import kotlinx.coroutines.*
import org.jetbrains.anko.runOnUiThread
import kotlin.collections.ArrayList


class ManagerViewModel(): ViewModel() {

    private var i = 0
    private var posPicker = -1
    private lateinit var settings: Settings
    private lateinit var touchHelper: ItemTouchHelper
    private lateinit var v: ActivityManagerBinding
    private lateinit var context: Context
    private lateinit var  lifecycle: Lifecycle
    private lateinit var pickedContact: () -> Unit
    private lateinit var settingsOpen: (Settings) -> Unit
    private lateinit var logsManager: LogsManager
    private val listCirs: ArrayList<CircleImage> by lazy {
        GlobalScope.launch { loadCirs { listCirs.add(it)}; context.runOnUiThread { v.recyclerView.adapter?.notifyDataSetChanged() } }
        arrayListOf<CircleImage>()
    }

    private var cirLayoutHeight = 0
    private var callLogs = listOf<LogObject>()
    private val adapterCallLogs: AdapterCallLogs by lazy{
        AdapterCallLogs(arrayListOf(), context = context, onAdd = {v.scrollView.scrollBy(65.dp * it, 65.dp * it)})
    }
    private val sharedPreferences: SharedPreferences by lazy{
        context.getSharedPreferences(Keys.ManagerPreference, Context.MODE_PRIVATE)
    }
    private val moshi: Moshi by lazy {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }
    private val adapterCirMoshi: JsonAdapter<CirPairData> by lazy {
        moshi.adapter(CirPairData().javaClass)
    }

    fun start(
        v: ActivityManagerBinding,
        context: Context,
        lifecycle: Lifecycle,
        pickedContact: () -> Unit,
        settingsOpen: (Settings) -> Unit
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
        callLogs = logsManager.getLogs(LogsManager.ALL_CALLS)
    }

    private fun updateHistoryList(){
        with(if (settings.historyButtom) v.recyclerViewHistoryBottom else v.recyclerViewHistory) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, !settings.historyButtom)
            itemAnimator = SlideInLeftAnimator(OvershootInterpolator())
            adapter = adapterCallLogs
        }

        GlobalScope.launch {
            getHistoryListLatest(context, onNext = { context.runOnUiThread { adapterCallLogs.addItem(it) }})
//            val list =  getCallLogs(context).awaitAll() as ArrayList<HistoryItem>
//            context.runOnUiThread {
//                adapter.setList(list)
//            }
        }
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
                return@withContext r
            }
            return@withContext null
        }


    private fun onChangeHeightCir(h: Int){
        cirLayoutHeight = h
        v.recyclerView.setSize(height = h)
    }

    fun initViews(updateHistory: Boolean = true) {
        if(updateHistory) updateHistoryList()
        listCirs.forEach {
            it.size = settings.sizeCirs
        }
        v.recyclerView.layoutManager = CirLayoutManager {onChangeHeightCir(it)}
        if (listCirs.isNotEmpty()) listCirs.updateItems(v.scrollView)
        v.recyclerView.adapter = CirAdapter(listCirs, context, settings, context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
        v.recyclerView.itemAnimator = LandingAnimator(OvershootInterpolator())
        v.recyclerView.adapter?.notifyDataSetChanged()
        v.deleteCir.apply {
            setOnDragListener { view, event ->
                try {
                    val viewIt = (v.recyclerView.adapter as CirAdapter).items[((v.recyclerView.adapter as CirAdapter).posDrag)]
                    when(event.action){
                        DragEvent.ACTION_DRAG_ENTERED -> {
                            context.vibrator.vibrateSafety(10)
                        }
                        DragEvent.ACTION_DROP -> viewIt.deleteListener()
                    }
                }catch (e: Exception) {e.printStackTrace()}
                true
            }

        }

        v.editCir.apply {
            setOnDragListener { view, event ->
                when (event.action) {
                    DragEvent.ACTION_DRAG_ENTERED -> {
                        context.vibrator.vibrateSafety(10)
                        (view as CircularImageView).imageTintList =
                            ColorStateList.valueOf(Color.BLUE)
                    }
                    else -> {
                        (view as CircularImageView).imageTintList =
                            ColorStateList.valueOf(Color.TRANSPARENT)
                    }
                }
                true
            }
        }

        v.fab.setOnClickListener { fab ->
            newCir(settings, touchHelper).let {
                (v.recyclerView.adapter as CirAdapter).addItem(it)
            }
        }
        v.fabSettings.setOnClickListener { fab ->
            settingsOpen(settings)
        }
        v.fabSearch.setOnClickListener { fab ->
            Intent(context, SearchActivity::class.java).let(context::startActivity)
        }
    }

    fun newCir(settings: Settings, touchHelper: ItemTouchHelper) = CircleImage(context,
        powerMenu = MoreMenuFactory().create(context) { return@create lifecycle }, swiping = true,
        lockableNestedScrollView = v.scrollView,
        menuEnable = settings.cirMenu,
        deleteCir = v.deleteCir, editCir = v.editCir).apply {
        size = settings.sizeCirs
        id = R.id.Cir1
        tag = i++
        borderWidth = 3.dp.toFloat()
        borderColor = Color.CYAN
        replaceListener = {
            touchHelper.startDrag(it)
        }
        pickContact = { pos ->
            posPicker = pos
            pickedContact()
        }
    }

    fun newCir(settings: Settings, touchHelper: ItemTouchHelper, pair: CirPairData) = CircleImage(context,
        powerMenu = MoreMenuFactory().create(context) { return@create lifecycle }, swiping = true,
        lockableNestedScrollView = v.scrollView,
        menuEnable = settings.cirMenu,
        deleteCir = v.deleteCir, editCir = v.editCir).apply {
        size = settings.sizeCirs
        id = R.id.Cir1
        tag = i++
        borderWidth = 3.dp.toFloat()
        borderColor = Color.CYAN
        pair.contactSettings?.let {
            contactSettings = it
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

    fun saveCirs(){
        var str = ""
        listCirs.forEach {
            str += adapterCirMoshi.toJson(CirPairData(it.contact, it.contactSettings)) + "<end>"
        }
        sharedPreferences.edit().apply {
            putString(Keys.Cirs, str)
        }.apply()
        Log.d("${this.javaClass}", "saveCirs: saved = $str")
    }

    fun onDestroy(){
        saveCirs()
//        GlobalScope.launch {
//            withContext(Dispatchers.IO) {
//                saveCirs()
//            }
//        }
    }

    fun onResult(requestCode: Int, requestCode1: Int, data: Intent?) {
        val uri = data!!.data
        val projections = arrayOf(ContactsContract.Contacts._ID)
        val cursor = context.contentResolver.query(uri!!, projections, null, null, null)
        var id = 0L
        if(cursor != null && cursor.moveToFirst()){
            val i = cursor.getColumnIndex(projections[0])
            id = cursor.getLong(i)
        }
        cursor?.close()
        val contact = Contacts.getQuery().whereEqualTo(Contact.Field.ContactId, id).find()
        (v.recyclerView.adapter as CirAdapter).setContact(posPicker, contact.first())
    }
}

private fun  ArrayList<CircleImage>.updateItems(lockableNestedScrollView: LockableNestedScrollView) {
    forEach{
        it.lockableNestedScrollView = lockableNestedScrollView
    }
}
