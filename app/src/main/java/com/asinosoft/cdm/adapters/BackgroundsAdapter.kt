package com.asinosoft.cdm.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.asinosoft.cdm.databinding.BackgroundItemCustomBinding
import com.asinosoft.cdm.databinding.BackgroundItemDefaultBinding
import com.asinosoft.cdm.databinding.BackgroundItemImageBinding
import org.jetbrains.anko.sdk27.coroutines.onClick

/**
 * Адаптер списка фоновых изображений
 */
class BackgroundsAdapter(
    private val backgrounds: List<Int>,
    private val handler: Handler
) :
    RecyclerView.Adapter<BackgroundsAdapter.BackgroundItemHolder>() {

    interface Handler {
        fun onSelectBackground(uri: String?)
        fun onCustomBackground()
    }

    companion object {
        const val BACKGROUND_DEFAULT = 1
        const val BACKGROUND_IMAGE = 2
        const val BACKGROUND_CUSTOM = 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackgroundItemHolder {
        val view: ViewBinding = when (viewType) {
            BACKGROUND_DEFAULT ->
                BackgroundItemDefaultBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ).also {
                    it.root.onClick {
                        handler.onSelectBackground(null)
                    }
                }
            BACKGROUND_CUSTOM ->
                BackgroundItemCustomBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ).also {
                    it.root.onClick {
                        handler.onCustomBackground()
                    }
                }
            else ->
                BackgroundItemImageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ).also {
                    it.root.clipToOutline = true
                }
        }
        return BackgroundItemHolder(view)
    }

    override fun onBindViewHolder(holder: BackgroundItemHolder, position: Int) {
        when (holder.view) {
            is BackgroundItemImageBinding ->
                holder.view.apply {
                    image.setImageResource(backgrounds[position - 1])
                    root.onClick {
                        val uri =
                            "android.resource://com.asinosoft.cdm/drawable/" + backgrounds[position - 1]
                        handler.onSelectBackground(uri)
                    }
                }
        }
    }

    override fun getItemCount() = 2 + backgrounds.size

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> BACKGROUND_DEFAULT
        1 + backgrounds.size -> BACKGROUND_CUSTOM
        else -> BACKGROUND_IMAGE
    }

    class BackgroundItemHolder(val view: ViewBinding) : RecyclerView.ViewHolder(view.root)
}
