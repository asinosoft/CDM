package com.asinosoft.cdm

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.ContactsContract.Contacts.Photo.DISPLAY_PHOTO
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.RelativeLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.pwittchen.swipe.library.rx2.Swipe
import com.github.pwittchen.swipe.library.rx2.SwipeEvent
import com.github.pwittchen.swipe.library.rx2.SwipeListener
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.find
import org.jetbrains.anko.longToast
import org.jetbrains.anko.sdk27.coroutines.onTouch
import org.jetbrains.anko.toast
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.math.abs
import kotlin.math.sign


class MainActivity : FragmentActivity() {


    companion object {
        const val ACTIVITYSETTINGS = 12
        const val developerWebsite = "https://kwork.ru/user/n0de"
        const val nameTitle = "Manager@n0de"
        const val nullNum = -9999
    }

    enum class Direction {
        LEFT, RIGHT, TOP, DOWN, UNKNOWN
    }

    lateinit var root: View
    var cirs: ArrayList<CirView> = ArrayList()
    lateinit var IDCIRS: Array<Int>
    var settings: Settings = Settings()
    var countCirsPred = nullNum
    lateinit var loader: Loader
    lateinit var buttonSettings: Button
    lateinit var vibrator: Vibrator
    var startX: Float = 1.0f
    var startY: Float = 1.0f
    var marginStart = 1
    var marginTop = 1
    var yPos = 1.0f
    var xPos = 1.0f
    var upped = false
    var difXPrior = false
    var priorIsSet = false
    var marginTopSlider = nullNum
    var marginTopFragment = -200
    var marginTopCirs = 0
    var heightList = 65
    var sliderIsLongClickAvalable = true
    var sliderIsLongClick = false
    var splittedOffset = 0
    var marginsBackAvalable = false
    var marginsTopOnBack = arrayOf(nullNum, nullNum)
    var scrollY = 0
    var recyclerDownY = 0f
    var recyclerEndHistory = false
    lateinit var cirTemp: CirView
    lateinit var recyclerView: RecyclerView
    lateinit var deleteImage: android.widget.ImageView
    lateinit var relativeLayout: RelativeLayout
    private lateinit var scrollView: LockableNestedScrollView

    lateinit var swipe: Swipe
    var swipeEvent: SwipeEvent = SwipeEvent.SWIPED_LEFT

    private var marStart = 0
    private var marEndTime = 0
    private var openDetail = true
    private val DIF = 100
    private val MAXT = 200
    private val onClick = true

    private var index: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loader = Loader(this)
        settings = loader.loadSettings()
        buttonSettings = find(R.id.ButtonSettings)
        cirTemp = find(R.id.CirTemp)
        deleteImage = find(R.id.deleteImage)
        root = buttonSettings.rootView
        recyclerView = find(R.id.recyclerView)
        recyclerView.isNestedScrollingEnabled = false
        scrollView = find(R.id.scrollView)
        scrollView.setScrollingEnabled(true)
        relativeLayout = find(R.id.relativeLayout)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        buttonSettings.setOnClickListener {
            openSettings()
//            Intent(this, SearchActivity::class.java).let(this::startActivity)
        }

//        setSwipe()
        /*relativeLayout.onLongClick {
            if (sliderIsLongClickAvalable) {
                sliderIsLongClick = true
                vibrate()
                longToast("Сдвинуть и сохранить положение")
            }
        }
        relativeLayout.onTouch { v, event ->
            Log.i("Slider: ", "Event = ${event.actionMasked}")
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    marginTopCirs = cirs[0].marginTop
                    heightList = recyclerView.height
                    yPos = event.rawY
//                    scrollY = recyclerView.height
                    true
                }

                MotionEvent.ACTION_UP -> {
                    if (sliderIsLongClick) {
                        sliderIsLongClick = false
                        splitterSetGolobalMargins(
                            recyclerView.height,
                            cirs[0].marginTop,
                            historyListBack = heightList,
                            cirsMarginBack = marginTopCirs
                        )
                        heightList = recyclerView.height
                        loader.saveSettings(settings)
                    } else {
                        *//* fragmentListHistory.view!!.setMargins(marginTopFragment)
                        cirsRefresh(0)*//*
                    }
                    //Log.i("Slider: ", "spliterOfssetGlobal: ${settings.spliterOfssetGlobal}")
                    sliderIsLongClickAvalable = true
                }

                MotionEvent.ACTION_MOVE -> {
//                    Log.i("ScrollState:", "${recyclerView.scrollState}")
//                    if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_DRAGGING) return@onTouch
                    var difY = event.rawY - yPos
                    if (heightList + difY.toInt() < 1) difY = -heightList.toFloat()
//                    if (difY.sign == -1f && heightList + difY.toInt() < 1) return@onTouch
                    sliderIsLongClickAvalable = if (sliderIsLongClick) true else difY == 0f
                    cirsRefresh(difY.toInt(), marginTopCirs)
                    recyclerView.setHeight(heightList + difY.toInt())
                    recyclerView.scrollToPosition(recyclerView.adapter!!.itemCount - 1)
//                    recyclerView.scrollTo(recyclerView.scrollX, scrollY - difY.toInt())
//                    Log.i("MainActivity.Scroll: ", "scrollY = $scrollY ; rec.scrollY = ${recyclerView.scrollY} ; difY = $difY")
                    //Log.i("Slider: ", "difY = $difY / MarginTopSlider = $marginTopSlider / rawY = ${event.rawY} / listHistory.marTop = ${fragmentListHistory.view!!.marginTop}")
                }
            }
        }*/
       /* recyclerView.onTouch { v, event ->

            Log.i("MainActivity.recyclerView.Touch: ", "event = ${event.actionMasked}")

            when(event.actionMasked){

                MotionEvent.ACTION_DOWN ->{
                    recyclerDownY = event.rawY
                    heightList = recyclerView.height
                    marginTopCirs = cirs[0].marginTop
                }

                MotionEvent.ACTION_UP ->{
                    recyclerDownY = 0f
                }

                MotionEvent.ACTION_MOVE ->{
                    if(recyclerDownY == 0f) {
                        recyclerDownY = event.rawY
                        heightList = recyclerView.height
                        marginTopCirs = cirs[0].marginTop
                    }

                    var difY = event.rawY - recyclerDownY

                    if (recyclerView.height >= relativeLayout.height){

                        val visibleItemCount = recyclerView.layoutManager!!.childCount
                        val totalItemCount = recyclerView.layoutManager!!.itemCount
                        val pastVisiblesItems = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        recyclerEndHistory = ( (visibleItemCount + pastVisiblesItems) >= totalItemCount)
                        if (recyclerEndHistory)
                            recyclerView.setHeight(heightList + difY.toInt())
                    }
                    else
                        recyclerView.setHeight(heightList + difY.toInt())
                    //recyclerView.scrollToPosition(recyclerView.adapter!!.itemCount)
                    if(recyclerView.height < relativeLayout.height)
                    cirsRefresh(difY.toInt(), marginTopCirs)
//                    Log.i("MainActivity.recyclerView.Touch: ", " down = $recyclerDownY ; rawY = ${event.rawY} ; difY = $difY ; heightList = $heightList ; recyclerView.height = ${recyclerView.height}")
                }
            }

        }*/

        checkPermissionNow()
        initilazeCirs()
        prefGet()
        setHistory()
    }

    private fun setSwipe() {
        swipe = Swipe()
        swipe.setListener(object: SwipeListener{
            override fun onSwipedUp(event: MotionEvent?): Boolean {
                swipeEvent = SwipeEvent.SWIPED_UP
                return true
            }

            override fun onSwipedDown(event: MotionEvent?): Boolean {
                swipeEvent = SwipeEvent.SWIPED_DOWN
                return true
            }

            override fun onSwipingUp(event: MotionEvent?) {
                swipeEvent = SwipeEvent.SWIPING_UP
            }

            override fun onSwipedRight(event: MotionEvent?): Boolean {
                swipeEvent = SwipeEvent.SWIPED_RIGHT
                return true
            }

            override fun onSwipingLeft(event: MotionEvent?) {
                swipeEvent = SwipeEvent.SWIPING_LEFT
            }

            override fun onSwipingRight(event: MotionEvent?) {
                swipeEvent = SwipeEvent.SWIPING_RIGHT
            }

            override fun onSwipingDown(event: MotionEvent?) {
                swipeEvent = SwipeEvent.SWIPING_DOWN
            }

            override fun onSwipedLeft(event: MotionEvent?): Boolean {
                swipeEvent = SwipeEvent.SWIPED_LEFT
                return true
            }

        })
    }

    override fun onStart() {
        super.onStart()
        scrollToEnd()
    }

    private fun scrollToEnd() {
        scrollView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val scrollViewHeight = scrollView.height
                if (scrollViewHeight > 0) {
                    scrollView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    val lastView = scrollView.getChildAt(scrollView.childCount - 1)
                    val lastViewBottom = lastView.bottom + scrollView.paddingBottom
                    val deltaScrollY = lastViewBottom - scrollViewHeight - scrollView.scrollY
                    /* If you want to see the scroll animation, call this. */
                    //nestedScrollView.smoothScrollBy(0, deltaScrollY);
                    /* If you don't want, call this. */
                    scrollView.scrollBy(0, deltaScrollY)
                }
            }
        })
    }

    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(1, VibrationEffect.DEFAULT_AMPLITUDE))
        } else vibrator.vibrate(20)
    }

    override fun onBackPressed() {
       /* if (marginsTopOnBack[0] != nullNum) {
            splitterSetGolobalMargins(marginsTopOnBack[0], marginsTopOnBack[1], refresh = true)
            return
        } else if (settings.historyListHeight != recyclerView.height) {
            //recyclerView.setHeight(settings.historyListHeight)
            cirsRefresh(0)
        } else*/
            super.onBackPressed()
    }

    private fun splitterSetGolobalMargins(
        historyList: Int,
        cirsMargin: Int,
        historyListBack: Int = nullNum,
        cirsMarginBack: Int = nullNum,
        refresh: Boolean = false
    ) {
        marginsTopOnBack[0] = historyListBack
        marginsTopOnBack[1] = cirsMarginBack
        settings.historyListHeight = historyList
        settings.marginTopCirs = cirsMargin
        if (refresh) {
            //recyclerView.setHeight(historyList)
            cirsRefresh(0)
        }
    }

    private fun View.setMargins(top: Int = nullNum, start: Int = nullNum) {
        val layout = this.layoutParams as RelativeLayout.LayoutParams
        if (top != nullNum) layout.topMargin = top
        if (start != nullNum) layout.marginStart = start
        this.layoutParams = layout
    }

    private fun View.setHeight(height: Int = nullNum) {
        val layout = this.layoutParams as RelativeLayout.LayoutParams
        if (height != nullNum) layout.height = height
        this.layoutParams = layout
    }

    private fun View.changeMargins(top: Int = nullNum, start: Int = nullNum) {
        val layout = this.layoutParams as RelativeLayout.LayoutParams
        if (top != nullNum) layout.topMargin += top
        if (start != nullNum) layout.marginStart += start
        this.layoutParams = layout
    }

    private fun  ArrayList<CirView>.changeMargins(top: Int = nullNum, start: Int = nullNum){
        forEach {
            it.changeMargins(top, start)
        }
    }

    private fun  ArrayList<CirView>.setMargins(top: Int = nullNum, start: Int = nullNum){
        forEach {
            it.setMargins(top, start)
        }
    }

    private fun cirsRefresh(difY: Int, marginTop: Int = nullNum) {
        for (i in cirs.indices) {
            var layoutParams = cirs[i].layoutParams as RelativeLayout.LayoutParams
            layoutParams.marginStart = settings.marginStartCirs + settings.offsetCirs * (i % 3)
            layoutParams.topMargin =
                (if (marginTop == nullNum) settings.marginTopCirs else marginTop) + settings.offsetCirs * (i / 3) + difY
            cirs[i].layoutParams = layoutParams
        }
    }

    private fun setHistory() {
        val list = getHistoryListLatest(300)
        val listUnique = ArrayList<HistoryCell>()
        list.forEach {
            if (!listUnique.containNumber(it.numberContact)) {
                /*if (Regex("""\D""").replace(it.nameContact, "").length < 3)
                    it.image = Funcs.retrieveContactPhoto(this, it.numberContact)*/
                if (it.contactID != "") it.image = getPhotoSaffety(it.contactID.toLong())
                listUnique.add(it)
            }
        }
//        (fragmentListHistory as HistoryListFrame).setAllParams(
//            ArrayList<HistoryCell>(), //listUnique,
//            list,
//            if (settings.photoFilePath == "") settings.themeColor else nullNum
//        )

        val listItem = ArrayList<HistoryItem>()
        listUnique.forEach {
            listItem.add(it.toItem())
        }

        var myAdapter = AdapterHistory(listItem.reversed() as java.util.ArrayList<HistoryItem>, null)
//            override fun onTouch(item: HistoryItem, event: MotionEvent) {
//                    Log.d("OnTouchHistory: ", "Event = ${event.actionMasked}")
//                    when(event.actionMasked){
//                        MotionEvent.ACTION_DOWN -> {
//                            xPos = event.rawX
//                            yPos = event.rawY
//                            marStart = imageCon.marginStart
//                            marEndTime = timeCon.marginEnd
//                            openDetail = true
//                        }
//
//                        MotionEvent.ACTION_MOVE -> {
//                            var difX = xPos - event.rawX
//                            openDetail = Math.abs(difX) <= 10
//                            difX = if (Math.abs(difX) > MAXT) MAXT * (difX / Math.abs(difX)) else difX
//
//                            imageCon.setMargins(start = marStart - difX.toInt())
//                            timeCon.setMargins(end = marEndTime + difX.toInt())
//                            dateCon.setMargins(end = marEndTime + difX.toInt())
//
//                            imageLeft.visibility = if(Math.abs(difX) >= DIF) if(difX / Math.abs(difX) == -1f) View.VISIBLE else View.INVISIBLE else View.INVISIBLE
//                            imageRight.visibility = if(Math.abs(difX) >= DIF) if(difX / Math.abs(difX) == 1f) View.VISIBLE else View.INVISIBLE else View.INVISIBLE
//                            /*if (scrollView.context != null) {
//                                var difY = (yPos - event.rawX).toInt()
//                                scrollView.scrollTo(0, 100)
//                            }*/
//                        }
//
//                        MotionEvent.ACTION_UP -> {
//                            imageCon.setMargins(start = marStart)
//                            timeCon.setMargins(end = marEndTime)
//                            dateCon.setMargins(end = marEndTime)
//                            if(imageLeft.visibility == View.VISIBLE) Metoths.callPhone(item.numberContact, context)
//                            if(imageRight.visibility == View.VISIBLE) Metoths.openWhatsApp(item.numberContact, context)
//                            imageLeft.visibility = View.INVISIBLE
//                            imageRight.visibility = View.INVISIBLE
//                            /*var difX = xPos - event.rawX
//                            openDetail = Math.abs(difX) <= 10*/
//                            if(onClick && openDetail){
//                                openDialog(items)
//                                openDetail = false
//                            }
//                        }
//                    }
//            }
//        })
        var lim = LinearLayoutManager(this)
        lim.orientation = LinearLayoutManager.VERTICAL
//        var mDividerItemDecoration = DividerItemDecoration(recyclerView.context, lim.orientation)
//        recyclerView.addItemDecoration(mDividerItemDecoration)
        recyclerView.layoutManager = lim
        //recyclerView.addOnItemTouchListener(RecyclerTouchListerner(this, RecyclerTouchListerner.SimpleItemMotionEventListener()))
        //recyclerView.setOnTouchListener { v, event -> return@setOnTouchListener true }
        recyclerView.adapter = myAdapter
        recyclerView.scrollToPosition(listItem.count() - 1)

        /*val callback = HistoryItemTouchHelperCallback(myAdapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(recyclerView)*/
    }

    fun HistoryCell.toItem() =
        HistoryItem(this.image, this.nameContact, this.numberContact, this.time, this.typeCall, this.duration, this.date, this.contactID)


    private fun ArrayList<HistoryCell>.containNumber(num: String): Boolean {
        this.forEach {
            if (it.numberContact == num) return true
        }
        return false
    }

    private fun getHistoryListLatest(count: Int): ArrayList<HistoryCell> {
        val list = ArrayList<HistoryCell>()
        val managedCursor = managedQuery(CallLog.Calls.CONTENT_URI, null, null, null, null)
        val number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER)
        val type = managedCursor.getColumnIndex(CallLog.Calls.TYPE)
        val date = managedCursor.getColumnIndex(CallLog.Calls.DATE)
        val name = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
        val id = managedCursor.getColumnIndex(CallLog.Calls._ID)
        val dur = managedCursor.getColumnIndex(CallLog.Calls.DURATION)
        var i = count
        val b = getSortedCursor(managedCursor)
        if (b) {
            managedCursor.moveToLast()
            managedCursor.moveToNext()
        }else {
            managedCursor.moveToFirst()
            managedCursor.moveToPrevious()
        }

        while ((if (b){ managedCursor.moveToPrevious() && !managedCursor.isBeforeFirst} else {managedCursor.moveToNext() && !managedCursor.isAfterLast}) && if(i == -1) true else --i >= 0) {
            var num = managedCursor.getString(number)
            if (num == "") continue
            //val cir = getCirWithNum(num)
            var callDayTime = managedCursor.getLong(date)
            var date = Date(callDayTime)
            var sdf =
                java.text.SimpleDateFormat("HH:mm", Locale.getDefault(Locale.Category.DISPLAY))
            var formattedDate = sdf.format(date)
            sdf = java.text.SimpleDateFormat("dd.MM", Locale.getDefault(Locale.Category.DISPLAY))
            var historyCell = HistoryCell(
                number = num,
                type = managedCursor.getInt(type),
                time = formattedDate,
                image = getDrawable(R.drawable.contact_unfoto)!!,
                name = managedCursor.getString(name) ?: num,
                ContactID = Funcs.getContactID(this, num) ?: "",
                duration = managedCursor.getString(dur),
                //date =  sdf.format(date)
                date = if (sdf.format(date) == sdf.format(Calendar.getInstance().time)) "Сегодня" else sdf.format(
                    date
                )
            )
            Log.d("History: ", "$i = ${historyCell.nameContact} / ${historyCell.date}")
            list.add(historyCell)
        }
        return list//if(b) list.reversed() as ArrayList<HistoryCell> else list
    }

    private fun getSortedCursor(managedCursor: Cursor): Boolean {
        val list = ArrayList<Long>()
        with(managedCursor) {
            moveToFirst()
            while (moveToNext() && list.count() < 2) {
                val temp = getLong(getColumnIndex(CallLog.Calls.DATE))
                if(temp < 0) continue
                list.add(getLong(getColumnIndex(CallLog.Calls.DATE)))
            }
            moveToFirst()
        }
        return (list[0] < list[1])
    }

    private fun getPhoto(imageDataRow: Int): Drawable {
        val c = contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Photo.PHOTO),
            ContactsContract.Data._ID + "=?",
            arrayOf(imageDataRow.toString()),
            null
        )
        var imageBytes: ByteArray? = null
        if (c != null) {
            if (c.moveToFirst()) {
                imageBytes = c.getBlob(0)
            }
            c.close()
        }
        Log.d("getPhoto: ", "imageBytes = $imageBytes")
        return if (imageBytes == null) getDrawable(R.drawable.contact_unfoto)!!
        else BitmapDrawable(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
    }

    private fun getPhoto(urlQ: String): String {
        val uri =
            Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(urlQ))
        val cursor = contentResolver.query(uri, null, null, null, null)
        var image_uri = ""
        if (cursor != null && cursor!!.moveToNext()) {
            image_uri =
                cursor!!.getString(cursor!!.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_URI))
            //Log.d(TAG, "image_uri "+image_uri);
        }
        if (cursor != null)
            cursor!!.close()
        return image_uri
    }

    private fun getCirWithNum(num: String): CirView? {
        var cir: CirView? = null
        for (it in cirs) {
            if (it.inContact()) continue
            if (it.compareNumber(num)) {
                return it
            }
        }
        //Log.d("GetCirWithNum: ", "Cir = $cir")
        return cir
    }

    private fun shareDeveloper() {
        val intentWeb = Intent(Intent.ACTION_VIEW, Uri.parse(developerWebsite))
        startActivity(intentWeb)
    }

    private fun clearAll() {
        cirs.forEach {
            it.clear(if (settings.themeColor == Color.BLACK) R.drawable.sharp_control_point_white_48 else R.drawable.sharp_control_point_black_48)
        }
    }

    private fun openSettings() {
        startActivityForResult(Intent(this, SettingsActivity::class.java).apply {putExtra(Keys.Settings, com.google.gson.Gson().toJson(settings))}, ACTIVITYSETTINGS)
    }

    private fun loadData() {
        settings = loader.loadSettings()
        if (countCirsPred == nullNum) countCirsPred = settings.countCirs
        root.backgroundColor = settings.themeColor
        setStatusBarColor()
        if (settings.photoFilePath != "") root.background =
            BitmapDrawable(BitmapFactory.decodeFile(settings.photoFilePath))
        buttonSettings.setBackgroundResource(
            if (settings.themeColor == Color.BLACK || settings.themeColor == resources.getColor(
                    R.color.costomGray
                )
            ) R.drawable.baseline_settings_white_48 else R.drawable.baseline_settings_black_48
        )
        deleteImage.setImageResource(
            if (settings.themeColor == Color.BLACK || settings.themeColor == resources.getColor(
                    R.color.costomGray
                )
            ) R.drawable.baseline_delete_white_48 else R.drawable.baseline_delete_black_48
        )
        for (i in cirs.indices) {
            var layoutParams = cirs[i].layoutParams as RelativeLayout.LayoutParams
            layoutParams.width = settings.sizeCirs
            layoutParams.height = settings.sizeCirs
            layoutParams.marginStart = settings.marginStartCirs + settings.offsetCirs * (i % 3)
            layoutParams.topMargin =
                settings.marginTopCirs + settings.offsetCirs * (i / 3) + settings.spliterOfssetGlobal.toInt()
            cirs[i].layoutParams = layoutParams
            if (cirs[i].inContact()) cirs[i].setImageResource(
                if (settings.themeColor == Color.BLACK || settings.themeColor == resources.getColor(
                        R.color.costomGray
                    )
                ) R.drawable.sharp_control_point_white_48 else R.drawable.sharp_control_point_black_48
            )
            cirs[i].visibility = if (i >= settings.countCirs) View.GONE else View.VISIBLE
        }
        setHistory()
        //recyclerView.setHeight(settings.historyListHeight)
        if (!loader.settingsExists()) countCirsPred = -1
        setButtonMargins()
    }

    private fun setButtonMargins() {
        if (countCirsPred == settings.countCirs) return
        //TODO: Временное решение
        return
//        var size = Point()
//        windowManager.defaultDisplay.getSize(size)
//        changeAllMarginTop(size.y - (cirs[settings.countCirs - 1].marginTop + cirs[settings.countCirs - 1].height + settings.offsetCirs))
//        countCirsPred = settings.countCirs
    }

    private fun changeAllMarginTop(i: Int) {
        cirs.changeMargins(top = i)
        //TODO: доработай
        loadData()
    }

    private fun getHeightRecycler(): Int {
        var size = Point()
        windowManager.defaultDisplay.getSize(size)
        var h =  size.y - ((settings.sizeCirs + settings.offsetCirs) * (settings.countCirs/3))
        h += if (settings.countCirs/3 == 1) -settings.offsetCirs/2 else if (settings.countCirs/3 > 2)  settings.offsetCirs/2 * (settings.countCirs/3 - 2)  else 0
        if (h.sign == -1) h = 0
        return h
    }

    private fun setStatusBarColor() {
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        if (settings.themeColor == Color.WHITE) return
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = window.statusBarColor
    }

    fun CirView.setSize(size: Int) {
        var layoutParams = this.layoutParams as RelativeLayout.LayoutParams
        layoutParams.width = size
        layoutParams.height = size
        this.layoutParams = layoutParams
    }

    private fun changeGlobalOffset(difY: Int) {
        setGlobalOffset(difY - splittedOffset)
        splittedOffset += difY
    }

    private fun setGlobalOffset(difY: Int) {
//        fragmentListHistory.view!!.changeMargins(top = difY)
        cirs.forEach { it.changeMargins(top = difY) }
    }

    private fun getSaffety(value: Int, setted: Int): Int = if (value == 0) setted else value

    private fun prefGet() {
        val tempCirs = loader.loadCirs()
        if (tempCirs != null)
            for (i in cirs.indices)
                cirs[i].fromCir(tempCirs[i])
        cirs.forEach { cir ->
            if (cir.idContact != "") {
                cir.setImageDrawable(getPhotoSaffety(cir.idContact.toLong()))
            }

        }
        loadData()
        /*try {
            for (i in cirs.indices) {
                var it = myPref.getString(i.toString(), "")!!
                if (it.length < 7) continue
                var ind = it.substring(0, it.indexOf('/')).toInt()
                var temp = it.substring(it.indexOf('/') + 1, it.length)
                var idCon = temp.substring(0, temp.indexOf("/"))
                var idtel = temp.substring(temp.indexOf('/') + 1, temp.lastIndexOf('/'))
                var idEmail = temp.substring(temp.lastIndexOf('/') + 1, temp.length)
                cirs[ind].idContact = idCon
                cirs[ind].number = idtel
                cirs[ind].email = idEmail

                Log.d("load: ", "$ind / $idCon / $idtel / $idEmail")
                var photo = if (photoMethodTh) openPhoto(idCon.toLong()) else  openDisplayPhoto(contactId = idCon.toLong())
                if (photo != null)
                    cirs[ind].cirIt.setImageBitmap(photo)
                else
                    cirs[ind].cirIt.setImageResource(R.drawable.contact)
            }
            loadData()
        }catch (e: Exception){Log.e("prefGet: ", e.toString())}
        val contacts = myPref.getStringSet("Contacts", HashSet<String>())
        val numbers = myPref.getStringSet("Numbers", HashSet<String>())
        val emails = myPref.getStringSet("Emails", HashSet<String>())

        if (emails != null)
            for (i in cirs.indices){
                cirs[i].number = numbers!!.toTypedArray<String>()[i]
                cirs[i].idContact = contacts!!.toTypedArray<String>()[i]
                cirs[i].email = emails!!.toTypedArray<String>()[i]
                val temp = openDisplayPhoto(contactId = contacts!!.toTypedArray()[i].toLong())
                if(temp != null)
                    cirs[i].cirIt.setImageBitmap(temp)
                else
                    cirs[i].cirIt.setImageResource(R.drawable.contact)
            }
        val fileName = this.baseContext.filesDir.name + "/cirs.txt"
        val lineList = mutableListOf<String>()
        File(fileName).useLines { lines -> lines.forEach { lineList.add(it) }}
        lineList.forEach {

            cirs[ind].doAsync {  }
        }
        */
    }

    private fun getPhotoSaffety(id: Long): Drawable {
        val photo = getPhotoNow(id)
        return if (photo != null) BitmapDrawable(photo) else getDrawable(R.drawable.contact_unfoto) as Drawable
    }

    private fun getPhotoNow(id: Long): Bitmap? =
        if (settings.photoType == PhotoType.Thum) openPhoto(id) else openPhoto(contactId = id)


    override fun onStop() {
        saveAll()
        super.onStop()
    }

    override fun onDestroy() {
        saveAll()
        return super.onDestroy()
    }

    private fun saveAll() {
        /*  val contacts =  HashSet<String>()
        val numbers = HashSet<String>()
        val emails = HashSet<String>()
        val emailArr = arrayOfNulls<String>(12)

        val e = myPref.edit()
        for(i in cirs.indices) {
            contacts.add(if(cirs[i].idContact == "") "/" else cirs[i].idContact)
            numbers.add(cirs[i].number)
            emails.add(cirs[i].email)
            emailArr[i] = if (cirs[i].email == "") "//" else cirs[i].email
        }
        e.putStringSet("Contacts", contacts)
        e.putStringSet("Numbers", numbers)
        e.putStringSet("Emails", emails)
        e.putStringSet("EmArr", emailArr.toHashSet())
        e.apply()

        val fileName = this.baseContext.filesDir.name + "/cirs.txt"
        val myfile = File(fileName)
        myfile.createNewFile()*/

        loader.saveCirs(cirs)
    }

    private fun initilazeCirs() {
        IDCIRS = arrayOf(
            R.id.Cir1,
            R.id.Cir2,
            R.id.Cir3,
            R.id.Cir4,
            R.id.Cir5,
            R.id.Cir6,
            R.id.Cir7,
            R.id.Cir8,
            R.id.Cir9,
            R.id.Cir10,
            R.id.Cir11,
            R.id.Cir12,
            R.id.Cir13,
            R.id.Cir14,
            R.id.Cir15,
            R.id.Cir16,
            R.id.Cir17,
            R.id.Cir18
        )
        for ((ind, value) in IDCIRS.withIndex()) {
            val cir = find<CirView>(value)
            cir.setOnClickListener {
                if (cir.inContact()) {
                    index = ind
                    val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                    startActivityForResult(intent, 1)
                } else if (cir.openCard) openCardContact(cir.idContact)
            }

            cir.setOnLongClickListener {
                // Create a new ClipData.
                // This is done in two steps to provide clarity. The convenience method
                // ClipData.newPlainText() can create a plain text ClipData in one step.
                if (!(it as CirView).isDrag || it.inContact()) return@setOnLongClickListener false
                it.bringToFront()
                deleteImage.visibility = View.VISIBLE
                // Create a new ClipData.Item from the ImageView object's tag
                val item = ClipData.Item(it.tag as? CharSequence)

                // Create a new ClipData using the tag as a label, the plain text MIME type, and
                // the already-created item. This will create a new ClipDescription object within the
                // ClipData, and set its MIME type entry to "text/plain"
                val dragData = ClipData(
                    it.tag as? CharSequence,
                    arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                    item
                )

                // Instantiates the drag shadow builder.
                val myShadow = View.DragShadowBuilder(it)

                // Starts the drag
                it.startDrag(
                    dragData,   // the data to be dragged
                    myShadow,   // the drag shadow builder
                    null,       // no need to use local data
                    0           // flags (not currently used, set to 0)
                )
            }

            cir.setOnDragListener { v, event ->
                if (cir == cirs[index]) return@setOnDragListener false
                // Handles each of the expected events
                Log.d(
                    "OnDrag: ",
                    "Event = ${event.action}, v = ${(v as CirView).name}, it = ${cir.name}"
                )
                when (event.action) {
                    DragEvent.ACTION_DRAG_STARTED -> {
                        // Determines if this View can accept the dragged data
                        event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                    }
                    DragEvent.ACTION_DRAG_ENTERED -> {
                        // Applies a green tint to the View. Return true; the return value is ignored.
                        cir.swapCir(cirs[index])

                        // Invalidate the view to force a redraw in the new tint
                        cirs[index].invalidate()
                        cir.invalidate()
                        true
                    }

                    DragEvent.ACTION_DRAG_LOCATION ->
                        // Ignore the event
                        true
                    DragEvent.ACTION_DRAG_EXITED -> {
                        // Re-sets the color tint to blue. Returns true; the return value is ignored.
                        cir.swapCir(cirs[index])

                        // Invalidate the view to force a redraw in the new tint
                        v.invalidate()
                        true
                    }
                    DragEvent.ACTION_DROP -> {
                        deleteImage.visibility = View.INVISIBLE
                        scrollView.setScrollingEnabled(true)
                        false
                    }

                    DragEvent.ACTION_DRAG_ENDED -> {
                        deleteImage.visibility = View.INVISIBLE
                        scrollView.setScrollingEnabled(true)
                        false
                    }
                    else -> {
                        // An unknown action type was received.
                        Log.e("DragDrop Example", "Unknown action type received by OnDragListener.")
                        false
                    }
                }
            }

            cir.onTouch { _, event ->
                if (cir.inContact()) return@onTouch
                var difX = startX - event.rawX
                var difY = startY - event.rawY
                if (!priorIsSet) {
                    difXPrior = difX > difY
                    priorIsSet = true
                }
                difX =
                    if (abs(difX) > settings.maxTouch.toFloat()) settings.maxTouch.toFloat() * difX / abs(
                        difX
                    ) else difX
                difY =
                    if (abs(difY) > settings.maxTouch.toFloat()) settings.maxTouch.toFloat() * difY / abs(
                        difY
                    ) else difY

                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    scrollView.setScrollingEnabled(false)
                    index = ind
                    upped = false
                    startX = event.rawX
                    startY = event.rawY
                    marginStart = cir.marginStart
                    marginTop = cir.marginTop
                    copyCirs(cir, cirTemp)
                    cir.bringToFront()
                    //Log.d("CirTemp: ", "layparam = ${cirTemp.layoutParams.height} / ${cirTemp.layoutParams.width} / ${cirTemp.marginStart} / ${cirTemp.marginTop}")
                } else if (event.actionMasked == MotionEvent.ACTION_MOVE && !cir.inContact()) {
                    val layoutMarginStart = getMarginStart(difX, difY)
//                    layoutParams.topMargin =
//                        marginTop - (if (layoutParams.marginStart == marginStart) difY.toInt() else 0)
//                    cir.layoutParams = layoutParams
                    cir.animate()
                        .y(marginTop.toFloat() - (if (layoutMarginStart == marginStart) difY else 0.toFloat()))
                        .x(layoutMarginStart.toFloat())
                        .setDuration(0)
                        .start()
                    setActionImage(cir, difX, difY, marginStart, marginTop)
                    cir.isDrag = abs(difX) + abs(difY) <= settings.dragPogres
                    deleteImage.visibility = View.INVISIBLE
                    Log.d("MainActivity.kt: ", "SwipeEvent: ${swipeEvent.name}")
                } else if (event.actionMasked == MotionEvent.ACTION_UP) {
//                    val layoutParams = cir.layoutParams as RelativeLayout.LayoutParams
//                    layoutParams.marginStart = marginStart
//                    layoutParams.topMargin = marginTop
//                    cir.layoutParams = layoutParams
                    cir.animate()
                        .x(marginStart.toFloat())
                        .y(marginTop.toFloat())
                        .setDuration(0)
                        .start()
                    cir.openCard = difX == 0f && difY == 0f
                    cir.isDrag = true
                    deleteImage.visibility = View.INVISIBLE
                    cirTemp.visibility = View.INVISIBLE
                    scrollView.setScrollingEnabled(true)

                    if (abs(difX) < settings.difTouch && abs(difY) < settings.difTouch || upped) return@onTouch

                    when (getAction(difX, difY)) {
                        Direction.LEFT -> openAction(settings.leftButton, cir)// Влево
                        Direction.DOWN -> openAction(settings.bottomButton, cir) // Вниз
                        Direction.TOP -> openAction(settings.topButton, cir) // Вверх
                        Direction.RIGHT -> openAction(settings.rightButton, cir) // вправо
                        Direction.UNKNOWN -> Log.e("Direction: ", "UNKNOWN!")
                    }
                    vibrate()
                    deleteImage.visibility = View.INVISIBLE
                    upped = true
                    priorIsSet = false
                }
                Log.d("Action: ", "${event.actionMasked}")
            }
            cirs.add(cir)
        }

        deleteImage.setOnDragListener { v, event ->
            // Handles each of the expected events
            when (event.action) {

                DragEvent.ACTION_DRAG_STARTED -> {
                    // Determines if this View can accept the dragged data
                    event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                }

                DragEvent.ACTION_DRAG_ENTERED -> {
                    // Applies a green tint to the View. Return true; the return value is ignored.
                    cirs[index].tempDel(if (settings.themeColor == Color.BLACK) R.drawable.sharp_control_point_white_48 else R.drawable.sharp_control_point_black_48)

                    // Invalidate the view to force a redraw in the new tint
                    cirs[index].invalidate()
                    vibrate()
                    true
                }

                DragEvent.ACTION_DRAG_EXITED -> {
                    // Re-sets the color tint to blue. Returns true; the return value is ignored.
                    cirs[index].undoDel()
                    // Invalidate the view to force a redraw in the new tint
                    v.invalidate()
                    true
                }

                else -> {
                    Log.e("deleteImage: ", "Action unknown!")
                    false
                }
            }
        }
    }

    private fun openAction(action: Actions, cir: CirView) {
        when(action){
            Actions.WhatsApp -> openWhatsApp(cir.number)
            Actions.Sms -> sendSMS(cir.number)
            Actions.Email -> mailToEmail(cir.email)
            Actions.PhoneCall -> callPhone(cir.number)
            Actions.Viber -> openViber(cir.number)
            Actions.Telegram -> openTelegram()
        }
    }

    private fun openTelegram() {
        val telegram =
            Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/InfotechAvl_bot"))
        startActivity(telegram)
    }

    private fun openViber(phone: String) {
        val intent = Intent("android.intent.action.VIEW")
        intent.setClassName("com.viber.voip", "com.viber.voip.WelcomeActivity")
        intent.data = "tel:$phone".toUri()
        startActivity(intent)
    }

    private fun copyCirs(cir: CirView, cirTemp: CirView) {
        val layoutParams = cirTemp.layoutParams as RelativeLayout.LayoutParams
        layoutParams.marginStart = cir.marginStart + settings.sizeCirs / 10
        layoutParams.topMargin = cir.marginTop + settings.sizeCirs / 10
        layoutParams.width = cir.width
        layoutParams.height = cir.height
        cirTemp.layoutParams = layoutParams
    }

    private fun setActionImage(cir: CirView, difX: Float, difY: Float, startX: Int, startY: Int) {

        cirTemp.visibility =
            if (abs(difX) < settings.difTouch && abs(difY) < settings.difTouch) View.INVISIBLE else View.VISIBLE
        cirTemp.setSize((settings.sizeCirs * 0.8).toInt())
        //Log.d("CirTemp: ", "Visibility = ${cirTemp.visibility == View.VISIBLE}")
        cirTemp.setBackgroundResource(R.drawable.sms_192)

        when (getAction(cir, startX, startY)) {
            Direction.LEFT -> cirTemp.setImageResource(getResDrawable(settings.leftButton)) // Влево
            Direction.DOWN -> cirTemp.setImageResource(getResDrawable(settings.bottomButton)) // Вниз
            Direction.TOP -> cirTemp.setImageResource(getResDrawable(settings.topButton)) // Вверх
            Direction.RIGHT -> cirTemp.setImageResource(getResDrawable(settings.rightButton)) // вправо
            Direction.UNKNOWN -> Log.e("ActionImage: ", "UNKNOWN!")
        }
    }

    private fun getResDrawable(action: Actions): Int {
        return when (action){
            Actions.WhatsApp -> R.drawable.whatsapp_192
            Actions.Sms -> R.drawable.sms_192
            Actions.Email -> R.drawable.email_192
            Actions.PhoneCall -> R.drawable.telephony_call_192
            Actions.Viber -> R.drawable.viber
            Actions.Telegram -> R.drawable.telegram
        }
    }

    private fun openCardContact(idContact: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, idContact)
        startActivity(intent)
    }

    private fun getAction(difX: Float, difY: Float): Direction {
        val _difX = abs(difX)
        val _difY = abs(difY)
        if (_difX > _difY) return if (difX / _difX == 1.0f) Direction.LEFT else Direction.RIGHT
        if (_difX < _difY) return if (difY / _difY == 1.0f) Direction.TOP else Direction.DOWN
        return Direction.UNKNOWN
    }

    private fun getAction(cir: CirView, startX: Int, startY: Int): Direction {

        val difX = startX - cir.x
        val difY = startY - cir.y

        if (abs(difX) > abs(difY)) return if (difX.sign == 1.0f) Direction.LEFT else Direction.RIGHT
        if (abs(difX) < abs(difY)) return if (difY.sign == 1.0f) Direction.TOP else Direction.DOWN
        return Direction.UNKNOWN
    }

    private fun getMarginStart(difX: Float, difY: Float): Int {
        return marginStart - if (abs(difX) + (if (difXPrior) settings.maxPrior else -settings.maxPrior) >= abs(
                difY
            )
        ) difX.toInt() else 0
    }

    private fun openWhatsApp(num: String) {
        val isAppInstalled = appInstalledOrNot("com.whatsapp")
        if (isAppInstalled) {
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$num"))
            startActivity(intent)
        } else {
            toast("Ошибка! WhatsApp не установлен!")
        }
        Log.e("Action: ", "WhatsApp open!")
    }

    private fun appInstalledOrNot(uri: String): Boolean {
        val pm = packageManager
        return try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun sendSMS(telNum: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", telNum, null)))
        Log.e("Action", "Sms open")
    }

    private fun mailToEmail(email: String) {
        if (email.isEmpty()) {
            toast("Email is empty!")
            return
        }
        val emailIntent = Intent(
            Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", email, null
            )
        )
        startActivity(Intent.createChooser(emailIntent, "Send email..."))
        Log.e("Action", "Mail open")
    }

    @SuppressLint("MissingPermission")
    fun callPhone(telNum: String) {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$telNum"))
        startActivity(intent)
    }

    private fun checkPermissionNow() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_CONTACTS
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.VIBRATE
                    ),
                    1
                )

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            if (requestCode == ACTIVITYSETTINGS) {
                if (resultCode == Activity.RESULT_OK) loadData()
                return
            }
            /*if (requestCode == 20) {
                    cirs[index].number = data!!.getStringExtra("Number"); return
                }*/
            val contactData = data!!.data
            var contactId = ""
            val listContactId = ArrayList<String>()
            val c = contentResolver.query(contactData!!, null, null, null, null)
            if (c!!.moveToFirst()) {
                val name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID))
                if (contactId != null && !listContactId.contains(contactId)) listContactId.add(
                    contactId
                )

                val emailAddress = getEmail(contactId)

                val temp =
                    if (settings.photoType == PhotoType.Thum) openPhoto(contactId = contactId.toLong()) else openDisplayPhoto(
                        contactId = contactId.toLong()
                    )
                if (temp != null)
                    cirs[index].setImageBitmap(temp)
                else
                    cirs[index].setImageResource(R.drawable.contact_unfoto)

                cirs[index].email = emailAddress
                cirs[index].name = name
                cirs[index].idContact = contactId
            }
            c.close()
            if (listContactId.count() > 1) longToast("Ошибка! Контакт имеет нестолько ID, сообщите разработчику!")
            //Log.d("ActivityResult: ", "ContactIDs = $contactId ${listContactId.size} / ${listContactId[0]}}")
            val intentRes = Intent(applicationContext, ActivityPhoneChoose::class.java)
            val nums = getPhone(contactId)
            val items: Array<String> = nums.toTypedArray()
            if (nums.size <= 1 || checkNumbers(items)) cirs[index].number =
                (nums.toArray())[0].toString()
            else {
                dialogNumbers(items)
            }
        } catch (e: Exception) {
            Log.e("ActivityResult: ", "Exception = ${e.message}")
        }
    }

    private fun checkNumbers(items: Array<String>): Boolean {
        val list = ArrayList<String>()
        items.forEach {
            list.add(Regex("""\D""").replace(it, ""))
        }
        list.forEach {
            if (list.component1() != it) return false
        }
        return true
    }


    private fun dialogNumbers(items: Array<String>) {
        val builder = AlertDialog.Builder(this)
        with(builder)
        {
            setTitle("Выберите номер телефона:")
            setCancelable(false)
            setItems(items) { _, which ->
                cirs[index].number = items[which]
            }
            show()
        }
    }


    private fun getPhone(contactId: String): HashSet<String> {
        // Using the contact ID now we will get contact phone number
        val cursorPhone = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf<String>(ContactsContract.CommonDataKinds.Phone.NUMBER),
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                    ContactsContract.CommonDataKinds.Phone.TYPE_HOME +
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE +
                    ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK +
                    ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME +
                    ContactsContract.CommonDataKinds.Phone.TYPE_PAGER +
                    ContactsContract.CommonDataKinds.Phone.TYPE_OTHER +
                    ContactsContract.CommonDataKinds.Phone.TYPE_CALLBACK +
                    ContactsContract.CommonDataKinds.Phone.TYPE_CAR +
                    ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN +
                    ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX +
                    ContactsContract.CommonDataKinds.Phone.TYPE_RADIO +
                    ContactsContract.CommonDataKinds.Phone.TYPE_TELEX +
                    ContactsContract.CommonDataKinds.Phone.TYPE_TTY_TDD +
                    ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE +
                    ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER +
                    ContactsContract.CommonDataKinds.Phone.TYPE_ASSISTANT +
                    ContactsContract.CommonDataKinds.Phone.TYPE_MMS,

            arrayOf(contactId), null
        )

        val nums = HashSet<String>()

        while (cursorPhone!!.moveToNext()) {
            val contactNumber =
                cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA))
            nums.add(contactNumber)
            Log.d("Num", "Contact Phone Number: $contactNumber")
        }
        cursorPhone.close()
        return nums
    }

    private fun getEmail(contactId: String): String {
        var res = ""
        val emails = contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = $contactId",
            null,
            null
        )
        while (emails!!.moveToNext()) {
            res =
                emails.run { getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)) }
        }
        emails.close()
        return res
    }

    fun openPhoto(contactId: Long): Bitmap? {
        val contactUri =
            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        val photoUri =
            Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY)
        val cursor = contentResolver.query(
            photoUri,
            arrayOf(ContactsContract.Contacts.Photo.PHOTO), null, null, null
        ) ?: return null
        cursor.use { cursor ->
            if (cursor.moveToFirst()) {
                val data = cursor.getBlob(0)
                return if (data != null) {
                    BitmapFactory.decodeStream(ByteArrayInputStream(data))
                } else return null
            }
        }
        return null
    }

    private fun openDisplayPhoto(contactId: Long): Bitmap? {
        val contactUri =
            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        val displayPhotoUri = Uri.withAppendedPath(contactUri, DISPLAY_PHOTO)
        try {
            val fd = contentResolver.openAssetFileDescriptor(displayPhotoUri, "r")
            return BitmapFactory.decodeStream(fd!!.createInputStream())
        } catch (e: IOException) {
            return null
        }

    }

    private fun getBitmapFromView(view: View): Bitmap {
        //Define a bitmap with the same size as the view
        val returnedBitmap =
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        //Bind a canvas to it
        val canvas = Canvas(returnedBitmap)
        //Get the view's background
        val bgDrawable = view.background
        if (bgDrawable != null)
        //has background drawable, then draw it on the canvas
            bgDrawable!!.draw(canvas)
        else
        //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE)
        // draw the view on the canvas
        view.draw(canvas)
        //return the bitmap
        return returnedBitmap
    }
}


