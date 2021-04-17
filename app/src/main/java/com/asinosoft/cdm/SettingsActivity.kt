package com.asinosoft.cdm

import android.animation.ValueAnimator
import android.app.Activity
import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.DragEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import com.asinosoft.cdm.Metoths.Companion.setSize
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
    lateinit var draggedCir: CircularImageView
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
        settings = Loader.loadSettings()
        initFavoritesBlock()
        initLayoutBlock()
        initActionsBlock()
        setAllCirs(settings.borderWidthCirs, settings.colorBorder)
    }

    private fun saveAll() {
        Loader.saveSettings(
            settings.copy(
                sizeCirs = v.seekBarSizeButtons.progress,
                rightButton = v.cirRight.action,
                leftButton = v.cirLeft.action,
                topButton = v.cirTop.action,
                bottomButton = v.cirBottom.action,
                chooserButton1 = v.cirChoose1.action,
                chooserButton2 = v.cirChoose2.action,
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
        v.cirChoose1.action = settings.chooserButton1
        v.cirChoose2.action = settings.chooserButton2
        v.cirRight.let(this::setCirData)
        v.cirLeft.let(this::setCirData)
        v.cirTop.let(this::setCirData)
        v.cirBottom.let(this::setCirData)
        v.cirChoose1.let(this::setCirData)
        v.cirChoose2.let(this::setCirData)
        v.seekBarColumnsButtons.setProgress(settings.columnsCirs.toFloat().takeIf { it > 0 } ?: 0f)
        v.seekBarBorderButtons.setProgress(settings.borderWidthCirs.toFloat())
    }

    private fun setCirData(cir: CircularImageView) {
        cir.setImageResource(getResDrawable(cir.action))
    }

    private fun getResDrawable(action: Actions): Int {
        return when (action) {
            Actions.WhatsApp -> R.drawable.whatsapp_192
            Actions.Sms -> R.drawable.sms_192
            Actions.Email -> R.drawable.email_192
            Actions.PhoneCall -> R.drawable.telephony_call_192
            Actions.Viber -> R.drawable.viber
            Actions.Telegram -> R.drawable.telegram
        }
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
        v.cirChoose1.let(this@SettingsActivity::setDragListener)
        v.cirChoose2.let(this@SettingsActivity::setDragListener)
    }

    private fun setDragListener(cir: CircularImageView) {
        cir.setOnLongClickListener {
            it.bringToFront()
            draggedCir = cir
            val item = ClipData.Item(it.tag as? CharSequence)
            val dragData = ClipData(
                it.tag as? CharSequence,
                arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                item
            )
            val myShadow = View.DragShadowBuilder(it)

            it.startDrag(
                dragData,
                myShadow,
                null,
                0
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
            cirChoose1.borderWidth = it.toFloat()
            cirChoose2.borderWidth = it.toFloat()
            cirTop.borderWidth = it.toFloat()
            cirRight.borderWidth = it.toFloat()
            cirLeft.borderWidth = it.toFloat()
            circleImageView.borderWidth = it
        }

        color?.let {
            cirBottom.borderColor = it
            cirChoose1.borderColor = it
            cirChoose2.borderColor = it
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
