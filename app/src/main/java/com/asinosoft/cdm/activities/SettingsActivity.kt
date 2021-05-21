package com.asinosoft.cdm.activities

import android.content.ClipData
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.asinosoft.cdm.adapters.ActionsAdapter
import com.asinosoft.cdm.api.Loader
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.Settings
import com.asinosoft.cdm.databinding.SettingsLayoutBinding
import com.asinosoft.cdm.helpers.Metoths.Companion.setSize
import com.asinosoft.cdm.views.CircularImageView
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import com.xw.repo.BubbleSeekBar
import com.xw.repo.BubbleSeekBar.OnProgressChangedListener
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.image
import org.jetbrains.anko.sdk27.coroutines.onClick
import kotlin.math.roundToInt

/**
 * Класс экрана настроек приложения
 */
class SettingsActivity : AppCompatActivity(), ColorPickerDialogListener {

    lateinit var oldSettings: Settings
    lateinit var v: SettingsLayoutBinding

    private var columnsCount: Int = 0
        set(value) {
            if (field == value) return
            field = value
            v.seekBarColumnsButtons.setProgress(value.toFloat())
            updateSeekSize()
        }

    private var circleSize: Int = 0
        set(value) {
            if (field == value) return
            field = value
            v.seekBarSizeButtons.setProgress(value.toFloat())
            v.circleImageView.setSize(value)
        }

    private var borderWidth: Int = 0
        set(value) {
            if (field == value) return
            field = value
            v.seekBarBorderButtons.setProgress(value.toFloat())
            v.circleImageView.borderWidth = value
            setAllCirs(width = value)
        }

    private var borderColor: Int = 0
        set(value) {
            if (field == value) return
            field = value
            v.circleImageView.borderColor = value
            setAllCirs(color = value)
        }

    private var favoritesLayout: Boolean = true
        set(mode) {
            field = mode
            v.btnFavoritesFirst.backgroundColor =
                if (mode) Color.rgb(0x67, 0x3a, 0xb7) else Color.LTGRAY
            v.btnFavoritesLast.backgroundColor =
                if (!mode) Color.rgb(0x67, 0x3a, 0xb7) else Color.LTGRAY
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.analytics.logEvent("activity_settings", Bundle.EMPTY)
        v = SettingsLayoutBinding.inflate(layoutInflater)
        setContentView(v.root)
    }

    override fun onStart() {
        super.onStart()
        v.scrollView.setScrollingEnabled(true)
        initFavoritesBlock()
        initLayoutBlock()
        initActionsBlock()
    }

    override fun onResume() {
        super.onResume()
        loadSettings()
    }

    override fun onPause() {
        saveSettings()
        super.onPause()
    }

    private fun loadSettings() {
        val settings = Loader.loadSettings(this)
        v.cirRight.action = settings.rightButton
        v.cirLeft.action = settings.leftButton
        v.cirTop.action = settings.topButton
        v.cirBottom.action = settings.bottomButton
        v.cirRight.let(this::setCirData)
        v.cirLeft.let(this::setCirData)
        v.cirTop.let(this::setCirData)
        v.cirBottom.let(this::setCirData)
        columnsCount = settings.columnsCirs
        circleSize = settings.sizeCirs
        borderWidth = settings.borderWidthCirs
        borderColor = settings.colorBorder
        favoritesLayout = settings.historyButtom
        oldSettings = settings
    }

    private fun saveSettings() {
        val newSettings = oldSettings.copy(
            sizeCirs = circleSize,
            rightButton = v.cirRight.action,
            leftButton = v.cirLeft.action,
            topButton = v.cirTop.action,
            bottomButton = v.cirBottom.action,
            columnsCirs = columnsCount,
            borderWidthCirs = borderWidth,
            colorBorder = borderColor,
            historyButtom = favoritesLayout
        )

        if (newSettings != oldSettings) {
            Loader.saveSettings(this, newSettings)

            Firebase.analytics.logEvent(
                "global_settings",
                Bundle().apply {
                    putInt("size", newSettings.sizeCirs)
                    putInt("columns", newSettings.columnsCirs)
                    putInt("borderWidth", newSettings.borderWidthCirs)
                    putInt("borderColor", newSettings.colorBorder)
                    putString("action_up", newSettings.topButton.name)
                    putString("action_down", newSettings.bottomButton.name)
                    putString("action_left", newSettings.leftButton.name)
                    putString("action_right", newSettings.rightButton.name)
                    putString("layout", if (newSettings.historyButtom) "down" else "up")
                }
            )
        }
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
                circleSize = progress
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

        v.seekBarBorderButtons.onProgressChangedListener = object : OnProgressChangedListener {
            override fun onProgressChanged(
                bubbleSeekBar: BubbleSeekBar?,
                progress: Int,
                progressFloat: Float,
                fromUser: Boolean
            ) {
                borderWidth = progress
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
                columnsCount = progress
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

        v.colorPicker.setOnClickListener {
            ColorPickerDialog.newBuilder().setColor(Color.RED).show(this)
        }
    }

    private fun initLayoutBlock() {
        if (v.layoutModeExpandable.isExpanded) v.layoutModeCross.cross() else v.layoutModeCross.plus()
        v.layoutModeHeader.setOnClickListener {
            v.layoutModeExpandable.toggle(true)
            v.layoutModeCross.toggle(500L)
        }
        v.btnFavoritesFirst.onClick { favoritesLayout = true }
        v.btnFavoritesLast.onClick { favoritesLayout = false }
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

        v.rvActions.layoutManager = GridLayoutManager(this, 5)
        v.rvActions.adapter = ActionsAdapter().apply {
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
                    when (val item = event.localState) {
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
                    when (val item = event.localState) {
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
                            Firebase.analytics.logEvent(
                                "global_set_action",
                                Bundle().apply {
                                    putString("direction", cir.direction.name)
                                    putString("action", item.name)
                                }
                            )
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
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels

        val maxSize: Int = (screenWidth * getColumnsFactor()).roundToInt()
        val minSize: Int = (screenWidth / 7.0).roundToInt()

        v.seekBarSizeButtons.configBuilder.apply {
            min(minSize.toFloat())
            max(maxSize.toFloat())
        }.build()

        circleSize = circleSize.coerceAtLeast(minSize).coerceAtMost(maxSize)
    }

    private fun getColumnsFactor(): Float =
        when (columnsCount) {
            1 -> 0.4f
            2 -> 0.4f
            3 -> 0.28f
            4 -> 0.23f
            else -> 0f
        }

    override fun onDialogDismissed(dialogId: Int) {
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        borderColor = color
    }

    private fun setAllCirs(width: Int? = null, @ColorInt color: Int? = null) {
        width?.let {
            v.cirBottom.borderWidth = it.toFloat()
            v.cirTop.borderWidth = it.toFloat()
            v.cirRight.borderWidth = it.toFloat()
            v.cirLeft.borderWidth = it.toFloat()
            v.circleImageView.borderWidth = it
        }

        color?.let {
            v.cirBottom.borderColor = it
            v.cirTop.borderColor = it
            v.cirRight.borderColor = it
            v.cirLeft.borderColor = it
            v.circleImageView.borderColor = it
        }
    }
}
