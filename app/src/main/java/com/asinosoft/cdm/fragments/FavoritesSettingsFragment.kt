package com.asinosoft.cdm.fragments

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.asinosoft.cdm.R
import com.asinosoft.cdm.databinding.FragmentFavoritesSettingsBinding
import com.asinosoft.cdm.helpers.Keys
import com.asinosoft.cdm.helpers.Metoths
import com.asinosoft.cdm.helpers.Metoths.Companion.setSize
import com.asinosoft.cdm.viewmodels.SettingsViewModel
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.xw.repo.BubbleSeekBar
import kotlinx.android.synthetic.main.activity_detail_history.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import kotlin.math.roundToInt

/**
 * Настройки блока избранных контактов
 */
class FavoritesSettingsFragment : Fragment() {
    private val model: SettingsViewModel by activityViewModels()
    private lateinit var v: FragmentFavoritesSettingsBinding
    private val colorSelected: Int by lazy {
        Metoths.getThemeColor(
            requireContext(),
            R.attr.colorAccent
        )
    }
    private val colorNotSelected: Int by lazy {
        Metoths.getThemeColor(
            requireContext(),
            R.attr.colorSecondary
        )
    }

    private val backgroundImageActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.data?.let { uri ->
                requireContext()
                    .getSharedPreferences(Keys.Preference, Context.MODE_PRIVATE)
                    .edit()
                    .putString(Keys.BACKGROUND_IMAGE, uri.toString())
                    .apply()
                activity?.recreate()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v = FragmentFavoritesSettingsBinding.inflate(inflater, container, false)
        return v.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initEventHandlers()

        v.sbFavoriteSize.setProgress(model.settings.sizeCirs.toFloat())

        v.sbColumnCount.setProgress(model.settings.columnsCirs.toFloat())
        updateFavoritesSizeRange(model.settings.columnsCirs)

        v.sbBorderWidth.setProgress(model.settings.borderWidthCirs.toFloat())

        v.imgFavorite.setSize(model.settings.sizeCirs)
        v.imgFavorite.borderWidth = model.settings.borderWidthCirs
        v.imgFavorite.borderColor = model.settings.colorBorder

        setFavoritesLayout(model.settings.historyButtom)

        model.buttonColor.observe(viewLifecycleOwner) { color ->
            v.imgFavorite.borderColor = color
            v.pickBorderColor.image?.setColorFilter(color)
        }

        when (model.settings.theme) {
            R.style.AppTheme_Light -> v.lightThemeButton.setBackgroundColor(colorSelected)
            R.style.AppTheme_Gray -> v.grayThemeButton.setBackgroundColor(colorSelected)
            R.style.AppTheme_Dark -> v.darkThemeButton.setBackgroundColor(colorSelected)
        }
    }

    private fun setFavoritesLayout(layout: Boolean) {

        model.settings.historyButtom = layout
        if (layout) {
            v.btnFavoritesFirst.setBackgroundColor(colorSelected)
            v.btnFavoritesLast.setBackgroundColor(colorNotSelected)
        } else {
            v.btnFavoritesFirst.setBackgroundColor(colorNotSelected)
            v.btnFavoritesLast.setBackgroundColor(colorSelected)
        }
    }

    private fun updateFavoritesSizeRange(columnsCount: Int) {
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels

        val maxSize: Int = (screenWidth * getColumnsFactor(columnsCount)).roundToInt()
        val minSize: Int = (screenWidth / 7.0).roundToInt()

        if (v.sbFavoriteSize.min.toInt() != minSize || v.sbFavoriteSize.max.toInt() != maxSize) {
            // Подгоняем нынешний размер кнопок под изменившийся диапазон
            val btnSize = model.settings.sizeCirs.coerceAtLeast(minSize).coerceAtMost(maxSize)

            v.sbFavoriteSize.configBuilder.apply {
                min(minSize.toFloat())
                max(maxSize.toFloat())
                progress(btnSize.toFloat())
            }.build()

            v.imgFavorite.setSize(btnSize)
        }
    }

    private fun getColumnsFactor(columnsCount: Int): Float =
        when (columnsCount) {
            1 -> 0.4f
            2 -> 0.4f
            3 -> 0.28f
            4 -> 0.23f
            else -> 0f
        }

    private fun initEventHandlers() {
        v.sbFavoriteSize.onProgressChangedListener = object :
            BubbleSeekBar.OnProgressChangedListener {
            override fun onProgressChanged(
                bubbleSeekBar: BubbleSeekBar?,
                progress: Int,
                progressFloat: Float,
                fromUser: Boolean
            ) {
                model.settings.sizeCirs = progress
                v.imgFavorite.setSize(progress)
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

        v.sbColumnCount.onProgressChangedListener = object :
            BubbleSeekBar.OnProgressChangedListener {
            override fun onProgressChanged(
                bubbleSeekBar: BubbleSeekBar?,
                progress: Int,
                progressFloat: Float,
                fromUser: Boolean
            ) {
                model.settings.columnsCirs = progress
                updateFavoritesSizeRange(progress)
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

        v.sbBorderWidth.onProgressChangedListener = object :
            BubbleSeekBar.OnProgressChangedListener {
            override fun onProgressChanged(
                bubbleSeekBar: BubbleSeekBar?,
                progress: Int,
                progressFloat: Float,
                fromUser: Boolean
            ) {
                model.settings.borderWidthCirs = progress
                v.imgFavorite.borderWidth = progress
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

        v.pickBorderColor.setOnClickListener {
            ColorPickerDialog.newBuilder().setColor(model.settings.colorBorder).show(activity)
        }

        v.btnFavoritesFirst.setOnClickListener { setFavoritesLayout(true) }

        v.btnFavoritesLast.setOnClickListener { setFavoritesLayout(false) }

        v.lightThemeButton.onClick {
            model.settings.theme = R.style.AppTheme_Light
            activity?.recreate()
        }
        v.grayThemeButton.onClick {
            model.settings.theme = R.style.AppTheme_Gray
            activity?.recreate()
        }
        v.darkThemeButton.onClick {
            model.settings.theme = R.style.AppTheme_Dark
            activity?.recreate()
        }

        v.btnSelectBackground.onClick {
            backgroundImageActivityResult.launch(
                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "image/*"
                }
            )
        }

        v.btnClearBackground.onClick {
            requireContext()
                .getSharedPreferences(Keys.Preference, Context.MODE_PRIVATE)
                .edit()
                .remove(Keys.BACKGROUND_IMAGE)
                .apply()
            activity?.recreate()
        }
    }
}
