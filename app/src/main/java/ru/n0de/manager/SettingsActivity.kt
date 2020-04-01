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
import android.widget.Spinner
import org.jetbrains.anko.find
import com.google.gson.Gson
import android.provider.MediaStore


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
    var filePathPhoto = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT // вертикальная
        setContentView(R.layout.activity_settings)
        supportActionBar!!.hide()
//        window.statusBarColor = Color.BLACK
//        window.navigationBarColor = Color.BLACK
    }

    override fun onStart() {
        super.onStart()
        initilaze()
        setValues()
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
