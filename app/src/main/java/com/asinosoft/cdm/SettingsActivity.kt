package com.asinosoft.cdm

import android.animation.ValueAnimator
import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.DragEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.asinosoft.cdm.Metoths.Companion.setSize
import com.asinosoft.cdm.adapters.ActionListAdapter
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.Settings
import com.asinosoft.cdm.databinding.SettingsLayoutBinding
import com.jaeger.library.StatusBarUtil
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import com.xw.repo.BubbleSeekBar
import com.xw.repo.BubbleSeekBar.OnProgressChangedListener
import kotlinx.android.synthetic.main.settings_layout.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.image
import org.jetbrains.anko.sdk27.coroutines.onClick

/**
 * Класс экрана настроек приложения
 */
class SettingsActivity : AppCompatActivity(), ColorPickerDialogListener {

    lateinit var settings: Settings
    lateinit var v: SettingsLayoutBinding
    var filePathPhoto = ""
    var widthScreen: Int? = null
        set(value) {
            field = value
            updateSeekSize()
            v.seekBarSizeButtons.setProgress(settings.sizeCirs.toFloat())
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        v = SettingsLayoutBinding.inflate(layoutInflater)
        setContentView(v.root)

        StatusBarUtil.setTranslucentForImageView(this, scrollView)
    }

    override fun onPause() {
        saveAll()
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        widthScreen = scrollView.width
    }

    override fun onStart() {
        super.onStart()
        v.scrollView.setScrollingEnabled(true)
        initAll()
        setData()
    }

    private fun initAll() {
        settings = Loader.loadSettings(this)
        initFavoritesBlock()
        initLayoutBlock()
        initActionsBlock()
        setAllCirs(settings.borderWidthCirs, settings.colorBorder)
    }

    private fun saveAll() {
        Loader.saveSettings(
            this,
            settings.copy(
                sizeCirs = v.seekBarSizeButtons.progress,
                rightButton = v.cirRight.action,
                leftButton = v.cirLeft.action,
                topButton = v.cirTop.action,
                bottomButton = v.cirBottom.action,
                columnsCirs = v.seekBarColumnsButtons.progress,
                borderWidthCirs = v.seekBarBorderButtons.progress,
            )
        )
    }

    private fun setData() {
        v.seekBarSizeButtons.setProgress(settings.sizeCirs.toFloat())
        v.cirRight.action = settings.rightButton
        v.cirLeft.action = settings.leftButton
        v.cirTop.action = settings.topButton
        v.cirBottom.action = settings.bottomButton
        v.cirRight.let(this::setCirData)
        v.cirLeft.let(this::setCirData)
        v.cirTop.let(this::setCirData)
        v.cirBottom.let(this::setCirData)
        v.seekBarColumnsButtons.setProgress(settings.columnsCirs.toFloat().takeIf { it > 0 } ?: 0f)
        v.seekBarBorderButtons.setProgress(settings.borderWidthCirs.toFloat())
    }

    private fun setCirData(cir: CircularImageView) {
        cir.setImageResource(Action.resourceByType(cir.action))
    }

    private fun initFavoritesBlock() {
        if (v.favoritesExpandable.isExpanded) v.favoritesCross.cross() else v.favoritesCross.plus()
        v.favoritesHeader.setOnClickListener {
            v.favoritesExpandable.toggle(true)
            v.favoritesCross.toggle(500L)
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

        updateSeekSize()

        seekBarBorderButtons.onProgressChangedListener = object : OnProgressChangedListener {
            override fun onProgressChanged(
                bubbleSeekBar: BubbleSeekBar?,
                progress: Int,
                progressFloat: Float,
                fromUser: Boolean
            ) {
                circleImageView.borderWidth = progress
                setAllCirs(width = progress)
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

        v.seekBarColumnsButtons.onProgressChangedListener = object : OnProgressChangedListener {
            override fun onProgressChanged(
                bubbleSeekBar: BubbleSeekBar?,
                progress: Int,
                progressFloat: Float,
                fromUser: Boolean
            ) {
                updateSeekSize()
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
    }

    private fun initLayoutBlock() {
        if (v.layoutModeExpandable.isExpanded) v.layoutModeCross.cross() else v.layoutModeCross.plus()
        v.layoutModeHeader.setOnClickListener {
            v.layoutModeExpandable.toggle(true)
            v.layoutModeCross.toggle(500L)
        }
        v.btnFavoritesFirst.onClick { setFavoritesLayout(true) }
        v.btnFavoritesLast.onClick { setFavoritesLayout(false) }
        setFavoritesLayout(settings.historyButtom)
    }

    private fun initActionsBlock() {
        if (v.actionsExpandable.isExpanded) v.actionsCross.cross() else v.actionsCross.plus()
        v.actionsHeader.setOnClickListener {
            v.actionsExpandable.toggle(true)
            v.actionsCross.toggle(500L)
        }
        v.cirBottom.let(this@SettingsActivity::setDragListener)
        v.cirTop.let(this@SettingsActivity::setDragListener)
        v.cirLeft.let(this@SettingsActivity::setDragListener)
        v.cirRight.let(this@SettingsActivity::setDragListener)

        v.rvActions.layoutManager = GridLayoutManager(this, 1 + settings.columnsCirs)
        v.rvActions.adapter = ActionListAdapter(settings).apply {
            setActions(Action.Type.values().asList())
        }
    }

    private fun setDragListener(cir: CircularImageView) {
        cir.setOnLongClickListener {
            it.bringToFront()
            val myShadow = View.DragShadowBuilder(it)

            it.startDrag(
                ClipData.newPlainText(cir.action.name, cir.action.name),
                myShadow,
                cir,
                0
            )
        }

        cir.setOnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    when (event.localState) {
                        is CircularImageView -> true
                        is Action.Type -> true
                        else -> false
                    }
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    val item = event.localState
                    when (item) {
                        is CircularImageView -> {
                            cir.swapCir(item)
                            v.invalidate()
                            cir.invalidate()
                        }
                        is Action.Type -> {
                            cir.setImageResource(Action.resourceByType(item))
                        }
                    }
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    val item = event.localState
                    when (item) {
                        is CircularImageView -> {
                            cir.swapCir(item)
                            v.invalidate()
                        }
                        is Action.Type -> {
                            cir.setImageResource(Action.resourceByType(cir.action))
                        }
                    }
                    true
                }
                DragEvent.ACTION_DROP -> {
                    when (val item = event.localState) {
                        is Action.Type -> {
                            cir.action = item
                            cir.setImageResource(Action.resourceByType(item))
                        }
                    }
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    private fun CircularImageView.swapCir(c: CircularImageView) {
        this.image = c.image.also { c.image = this.image }
        this.action = c.action.also { c.action = this.action }
    }

    private fun updateSeekSize() {
        if (widthScreen == null) return
        val maxSize = widthScreen!! * when (v.seekBarColumnsButtons.progress) {
            1 -> 0.4f
            2 -> 0.4f
            3 -> 0.28f
            4 -> 0.23f
            else -> 0f
        }
        val minSize = widthScreen!! / 7
        Log.d("${this.javaClass}", "updateSeekSize: min = $minSize; max = $maxSize")
        v.seekBarSizeButtons.configBuilder.apply {
            max(maxSize.toFloat())
            min(minSize.toFloat())
        }.build()
        v.seekBarSizeButtons.setProgress(
            widthScreen!! * when (v.seekBarColumnsButtons.progress) {
                1 -> 0.3f
                2 -> 0.25f
                3 -> 0.19f
                4 -> 0.16f
                else -> 0f
            }
        )

        v.colorPicker.setOnClickListener {
            ColorPickerDialog.newBuilder().setColor(Color.RED).show(this)
        }
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

    override fun onDialogDismissed(dialogId: Int) {
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        settings.colorBorder = color
        circleImageView.borderColor = color
        setAllCirs(color = color)
    }

    private fun setAllCirs(width: Int? = null, @ColorInt color: Int? = null) {
        width?.let {
            cirBottom.borderWidth = it.toFloat()
            cirTop.borderWidth = it.toFloat()
            cirRight.borderWidth = it.toFloat()
            cirLeft.borderWidth = it.toFloat()
            circleImageView.borderWidth = it
        }

        color?.let {
            cirBottom.borderColor = it
            cirTop.borderColor = it
            cirRight.borderColor = it
            cirLeft.borderColor = it
            circleImageView.borderColor = it
        }
    }

    private fun setFavoritesLayout(mode: Boolean) {
        settings.historyButtom = mode
        v.btnFavoritesFirst.backgroundColor =
            if (mode) Color.rgb(0x67, 0x3a, 0xb7) else Color.LTGRAY
        v.btnFavoritesLast.backgroundColor =
            if (!mode) Color.rgb(0x67, 0x3a, 0xb7) else Color.LTGRAY
    }
}
