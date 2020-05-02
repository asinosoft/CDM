package ru.n0de.manager

import android.animation.ValueAnimator
import android.app.Activity
import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.*
import org.jetbrains.anko.find
import com.google.gson.Gson
import android.provider.MediaStore
import android.util.Log
import android.view.DragEvent
import com.xw.repo.BubbleSeekBar
import com.xw.repo.BubbleSeekBar.OnProgressChangedListener
import org.jetbrains.anko.sdk27.coroutines.onClick
import ru.n0de.manager.databinding.SettingsLayoutBinding


class SettingsActivity : AppCompatActivity() {

    lateinit var sizeEd: EditText
    lateinit var marginStartEd: EditText
    lateinit var marginTopEd: EditText
    lateinit var offsetEd: EditText
    lateinit var maxTouch: EditText
    lateinit var maxPrior: EditText
    lateinit var difTouch: EditText
    private lateinit var listPhoto: Spinner
    lateinit var listCount: Spinner
    lateinit var listColorFon: Spinner
    lateinit var settings: Settings
    lateinit var loader: Loader
    lateinit var v: SettingsLayoutBinding
    lateinit var draggedCir: CirView
    var filePathPhoto = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT // вертикальная
        v = SettingsLayoutBinding.inflate(layoutInflater)
        setContentView(v.root)
//        window.statusBarColor = Color.BLACK
//        window.navigationBarColor = Color.BLACK
    }

    override fun onStart() {
        super.onStart()
        v.scrollView.setScrollingEnabled(true)
        initAll()
        setData()
    }

    private fun initAll() {
        settings = Gson().fromJson(intent.getStringExtra(Keys.Settings), Settings::class.java)
        loader = Loader(this)
        initSeek1()
        initSeek2()
        initSeek3()
        initSeek4()
        initSave()
    }

    private fun initSave() {
        v.save.setOnClickListener {
            saveAll()
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun saveAll() {
        loader.saveSettings(settings.copy(countCirs = v.seekBarCountButtons.progress, sizeCirs = v.seekBarSizeButtons.progress, rightButton = v.cirRight.action,
        leftButton = v.cirLeft.action, topButton = v.cirTop.action, bottomButton = v.cirBottom.action, chooserButton1 = v.cirChoose1.action, chooserButton2 = v.cirChoose2.action))
    }

    private fun setData() {
        v.seekBarCountButtons.setProgress(settings.countCirs.toFloat())
        v.seekBarSizeButtons.setProgress(settings.sizeCirs.toFloat())
        v.cirRight.action = settings.rightButton
        v.cirLeft.action = settings.leftButton
        v.cirTop.action = settings.topButton
        v.cirBottom.action = settings.bottomButton
        v.cirChoose1.action = settings.chooserButton1
        v.cirChoose2.action = settings.chooserButton2
        v.cirRight.let(this::setCirData)
        v.cirLeft.let(this::setCirData)
        v.cirTop.let(this::setCirData)
        v.cirBottom.let(this::setCirData)
        v.cirChoose1.let(this::setCirData)
        v.cirChoose2.let(this::setCirData)
    }

    private fun setCirData(cir: CirView) {
        cir.setImageResource(getResDrawable(cir.action))
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

    private fun initSeek4() {
        if(v.expandable4.isExpanded) v.cross4.cross() else v.cross4.plus()
        v.cardViewText4.onClick {
            v.expandable4.toggle(true)
            v.cross4.toggle(500L)
        }
        v.cirBottom.let(this@SettingsActivity::setDragListener)
        v.cirTop.let(this@SettingsActivity::setDragListener)
        v.cirLeft.let(this@SettingsActivity::setDragListener)
        v.cirRight.let(this@SettingsActivity::setDragListener)
        v.cirChoose1.let(this@SettingsActivity::setDragListener)
        v.cirChoose2.let(this@SettingsActivity::setDragListener)
    }

    private fun setDragListener(cir: CirView) {
        cir.setOnLongClickListener {
            it.bringToFront()
            draggedCir = cir
            val item = ClipData.Item(it.tag as? CharSequence)
            val dragData = ClipData(it.tag as? CharSequence, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), item)
            val myShadow = View.DragShadowBuilder(it)

            it.startDrag(
                dragData,   // the data to be dragged
                myShadow,   // the drag shadow builder
                null,       // no need to use local data
                0           // flags (not currently used, set to 0)
            )
        }

        cir.setOnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    // Determines if this View can accept the dragged data
                    event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    // Applies a green tint to the View. Return true; the return value is ignored.
                    cir.swapCir(draggedCir)

                    // Invalidate the view to force a redraw in the new tint
                    v.invalidate()
                    cir.invalidate()
                    true
                }

                DragEvent.ACTION_DRAG_LOCATION ->
                    // Ignore the event
                    true
                DragEvent.ACTION_DRAG_EXITED -> {
                    // Re-sets the color tint to blue. Returns true; the return value is ignored.
                    cir.swapCir(draggedCir)

                    // Invalidate the view to force a redraw in the new tint
                    v.invalidate()
                    true
                }
                DragEvent.ACTION_DROP -> {
                    this.v.scrollView.setScrollingEnabled(true)
                    false
                }

                DragEvent.ACTION_DRAG_ENDED -> {
                    this.v.scrollView.setScrollingEnabled(true)
                    false
                }
                else -> {
                    // An unknown action type was received.
                    Log.e("DragDrop Example", "Unknown action type received by OnDragListener.")
                    false
                }
            }
        }
    }

    private fun initSeek3() {
        if(v.expandable3.isExpanded) v.cross3.cross() else v.cross3.plus()
        v.cardViewText3.onClick {
            v.expandable3.toggle(true)
            v.cross3.toggle(500L)
        }
    }

    private fun initSeek1() {
        if(v.expandable1.isExpanded) v.cross1.cross() else v.cross1.plus()
        v.cardViewText1.onClick {
            v.expandable1.toggle(true)
            v.cross1.toggle(500L)
        }
    }

    private fun initSeek2() {
        if(v.expandable2.isExpanded) v.cross2.cross() else v.cross2.plus()
        v.cardViewText2.onClick {
            v.expandable2.toggle(true)
            v.cross2.toggle(500L)
        }

        v.seekBarSizeButtons.onProgressChangedListener = object : OnProgressChangedListener {
            override fun onProgressChanged(
                bubbleSeekBar: BubbleSeekBar?,
                progress: Int,
                progressFloat: Float,
                fromUser: Boolean
            ) {
                v.circleImageView.setSize(progress)
            }

            override fun getProgressOnActionUp(
                bubbleSeekBar: BubbleSeekBar?,
                progress: Int,
                progressFloat: Float
            ) {
            }

            override fun getProgressOnFinally(
                bubbleSeekBar: BubbleSeekBar?,
                progress: Int,
                progressFloat: Float,
                fromUser: Boolean
            ) {
            }
    }
    /*views.foldingCell1.initialize(1000, Color.DKGRAY, 2);
    views.foldingCell1.initialize(30, 1000, Color.DKGRAY, 2);
    views.foldingCell1.setOnClickListener {
        views.foldingCell1.toggle(false)
    }*/
//        initilaze()
//        setValues()
    }

    private fun increaseViewSize(view: View, height: Int) {
        val valueAnimator = ValueAnimator.ofInt(view.measuredHeight, height)
        valueAnimator.duration = 500L
        valueAnimator.addUpdateListener {
            val animatedValue = valueAnimator.animatedValue as Int
            val layoutParams = view.layoutParams
            layoutParams.height = animatedValue
            view.layoutParams = layoutParams
        }
        valueAnimator.start()
    }

    fun View.setSize(size: Int){
        val l = this.layoutParams
        l.width = size
        l.height = size
        this.layoutParams = l
    }

    private fun saveData() {
        settings.sizeCirs = sizeEd.text.toString().toInt()
        settings.marginStartCirs = marginStartEd.text.toString().toInt()
        settings.marginTopCirs = marginTopEd.text.toString().toInt()
        settings.offsetCirs = offsetEd.text.toString().toInt()
        settings.maxTouch = maxTouch.text.toString().toInt()
        settings.maxPrior = maxPrior.text.toString().toInt()
        settings.difTouch = difTouch.text.toString().toInt()
        settings.photoFilePath = filePathPhoto
        loader.saveSettings(settings)
    }

    private fun setValues() {
        settings.run {
        sizeEd.setText(sizeCirs.toString())
        marginStartEd.setText(marginStartCirs.toString())
        marginTopEd.setText(marginTopCirs.toString())
        offsetEd.setText(offsetCirs.toString())
        this@SettingsActivity.maxTouch.setText(maxTouch.toString())
        this@SettingsActivity.maxPrior.setText(maxPrior.toString())
        this@SettingsActivity.difTouch.setText(difTouch.toString())
        }
    }

    private fun initilaze() {
        sizeEd = find(R.id.CirSizeEditText)
        marginStartEd = find(R.id.MarginStartEditText)
        marginTopEd = find(R.id.MarginTopEditText)
        offsetEd = find(R.id.OffsetEditText)
        maxPrior = find(R.id.MaxPriorET)
        maxTouch = find(R.id.MaxTouchET)
        difTouch = find(R.id.DifTouchET)
        listPhoto = find(R.id.ListPhotoMethod)
        listCount = find(R.id.ListCountCirs)
        listColorFon = find(R.id.ListColorsFon)
        settings = Gson().fromJson(intent.getStringExtra(Keys.Settings), Settings::class.java)
        loader = Loader(this)
        listPhoto.adapter = ArrayAdapter(
            this,
            R.layout.support_simple_spinner_dropdown_item,
            arrayOf("Сжатый", "Полный")
        )
        listPhoto.prompt = "Выберите метод получения фото:"
        listPhoto.setSelection(PhotoType.getInt(settings.photoType))
        listPhoto.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                itemSelected: View, selectedItemPosition: Int, selectedId: Long
            ) {
                settings.photoType = PhotoType.getType(selectedItemPosition)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        val arrayCount = resources.getStringArray(R.array.count_curs)
        listCount.adapter =
            ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, arrayCount)
        listCount.prompt = "Выберите колличество иконок:"
        listCount.setSelection(arrayCount.indexOf(settings.countCirs.toString()))
        listCount.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                itemSelected: View, selectedItemPosition: Int, selectedId: Long
            ) {
                settings.countCirs = arrayCount[selectedItemPosition].toInt()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        listColorFon.run {
            val arrayColor = arrayOf("Белый", "Серый", "Черный")
            adapter = ArrayAdapter(this@SettingsActivity, R.layout.support_simple_spinner_dropdown_item, arrayColor)
            prompt = "Выберите цвет фона:"
            setSelection(if (settings.themeColor == Color.WHITE) 0 else if (settings.themeColor == Color.BLACK) 2 else 1)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    itemSelected: View, selectedItemPosition: Int, selectedId: Long
                ) {
                    when (selectedItemPosition) {
                        0 -> settings.themeColor = Color.WHITE
                        1 -> settings.themeColor = resources.getColor(R.color.costomGray)
                        2 -> settings.themeColor = Color.BLACK
                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }

    fun onClickButton(view: View) {
        saveData()
        setResult(Activity.RESULT_OK)
        finish()
    }

    fun onClickButtonPhoto(view: View) {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 12)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != 12 || resultCode != Activity.RESULT_OK) return
        val uri = data!!.data
        val projection = arrayOf(MediaStore.Images.Media.DATA)

        val cursor = contentResolver.query(uri!!, projection, null, null, null)
        cursor!!.moveToFirst()

        val columnIndex = cursor.getColumnIndex(projection[0])
        val filepath = cursor.getString(columnIndex)
        cursor.close()
        filePathPhoto = filepath
    }
}
