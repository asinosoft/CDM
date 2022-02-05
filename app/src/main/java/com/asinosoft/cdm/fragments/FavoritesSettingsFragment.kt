package com.asinosoft.cdm.fragments

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.Analytics
import com.asinosoft.cdm.databinding.FragmentFavoritesSettingsBinding
import com.asinosoft.cdm.helpers.Metoths
import com.asinosoft.cdm.helpers.Metoths.Companion.setSize
import com.asinosoft.cdm.viewmodels.SettingsViewModel
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.xw.repo.BubbleSeekBar
import kotlin.math.roundToInt

/**
 * Настройки блока избранных контактов
 */
class FavoritesSettingsFragment : Fragment() {
    private val model: SettingsViewModel by activityViewModels()
    private var v: FragmentFavoritesSettingsBinding? = null
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v = FragmentFavoritesSettingsBinding.inflate(inflater, container, false)
        return v!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        v = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initEventHandlers()

        v!!.sbFavoriteSize.setProgress(model.settings.sizeCirs.toFloat())

        v!!.sbColumnCount.setProgress(model.settings.columnsCirs.toFloat())
        updateFavoritesSizeRange(model.settings.columnsCirs)

        v!!.sbBorderWidth.setProgress(model.settings.borderWidthCirs.toFloat())

        v!!.imgFavorite.setSize(model.settings.sizeCirs)
        v!!.imgFavorite.borderWidth = model.settings.borderWidthCirs
        v!!.imgFavorite.borderColor = model.settings.colorBorder

        setFavoritesLayout(model.settings.historyButtom)

        model.buttonColor.observe(viewLifecycleOwner) { color ->
            v!!.imgFavorite.borderColor = color
            v!!.pickBorderColor.setHintTextColor(color)
        }

        val themeNames = resources.getStringArray(R.array.themeNames)
        v!!.themes.text = themeNames.elementAtOrElse(model.settings.theme) { themeNames[0] }
    }

    private fun setFavoritesLayout(layout: Boolean) {
        model.settings.historyButtom = layout
        if (layout) {
            v!!.btnFavoritesFirst.setBackgroundColor(colorSelected)
            v!!.btnFavoritesLast.setBackgroundColor(colorNotSelected)
        } else {
            v!!.btnFavoritesFirst.setBackgroundColor(colorNotSelected)
            v!!.btnFavoritesLast.setBackgroundColor(colorSelected)
        }

        Analytics.logFavoritesPosition(if (layout) "bottom" else "top")
    }

    private fun updateFavoritesSizeRange(columnsCount: Int) {
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels

        val maxSize: Int = (screenWidth * getColumnsFactor(columnsCount)).roundToInt()
        val minSize: Int = (screenWidth / 7.0).roundToInt()

        if (v!!.sbFavoriteSize.min.toInt() != minSize || v!!.sbFavoriteSize.max.toInt() != maxSize) {
            // Подгоняем нынешний размер кнопок под изменившийся диапазон
            val btnSize = model.settings.sizeCirs.coerceAtLeast(minSize).coerceAtMost(maxSize)

            v!!.sbFavoriteSize.configBuilder.apply {
                min(minSize.toFloat())
                max(maxSize.toFloat())
                progress(btnSize.toFloat())
            }.build()

            v!!.imgFavorite.setSize(btnSize)
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
        v!!.sbFavoriteSize.onProgressChangedListener = object :
            BubbleSeekBar.OnProgressChangedListener {
            override fun onProgressChanged(
                bubbleSeekBar: BubbleSeekBar?,
                progress: Int,
                progressFloat: Float
            ) {
                model.settings.sizeCirs = progress
                v!!.imgFavorite.setSize(progress)
                Analytics.logFavoritesSize(progress)
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
                progressFloat: Float
            ) {
            }
        }

        v!!.sbColumnCount.onProgressChangedListener = object :
            BubbleSeekBar.OnProgressChangedListener {
            override fun onProgressChanged(
                bubbleSeekBar: BubbleSeekBar?,
                progress: Int,
                progressFloat: Float
            ) {
                model.settings.columnsCirs = progress
                updateFavoritesSizeRange(progress)
                Analytics.logFavoritesCount(progress)
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
                progressFloat: Float
            ) {
            }
        }

        v!!.sbBorderWidth.onProgressChangedListener = object :
            BubbleSeekBar.OnProgressChangedListener {
            override fun onProgressChanged(
                bubbleSeekBar: BubbleSeekBar?,
                progress: Int,
                progressFloat: Float
            ) {
                model.settings.borderWidthCirs = progress
                v!!.imgFavorite.borderWidth = progress
                Analytics.logFavoritesBorderWidth(progress)
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
                progressFloat: Float
            ) {
            }
        }

        v!!.pickBorderColor.setOnClickListener {
            ColorPickerDialog.newBuilder().setColor(model.settings.colorBorder).show(activity)
        }

        v!!.btnFavoritesFirst.setOnClickListener { setFavoritesLayout(true) }

        v!!.btnFavoritesLast.setOnClickListener { setFavoritesLayout(false) }

        v!!.themes.setOnClickListener {
            ThemeSelectionDialog { theme ->
                Analytics.logTheme(theme)
                model.settings.theme = theme
                activity?.recreate()
            }.show(parentFragmentManager, "Select theme")
        }

        v!!.backgrounds.setOnClickListener {
            findNavController().navigate(R.id.action_select_background)
        }
    }
}
