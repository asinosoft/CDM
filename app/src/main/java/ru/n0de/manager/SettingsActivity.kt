package ru.n0de.manager

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
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
import com.ramotion.foldingcell.views.FoldingCellView
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
    lateinit var views: SettingsLayoutBinding
    var filePathPhoto = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT // вертикальная
        views = SettingsLayoutBinding.inflate(layoutInflater)
        setContentView(views.root)
//        window.statusBarColor = Color.BLACK
//        window.navigationBarColor = Color.BLACK
    }

    override fun onStart() {
        super.onStart()
        views.seekBar1.onProgressChangedListener = object : OnProgressChangedListener {
            override fun onProgressChanged(
                bubbleSeekBar: BubbleSeekBar?,
                progress: Int,
                progressFloat: Float,
                fromUser: Boolean
            ) {
                views.circleImageView.setSize(progress)
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
