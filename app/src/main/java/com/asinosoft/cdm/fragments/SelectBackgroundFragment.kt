package com.asinosoft.cdm.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.asinosoft.cdm.adapters.BackgroundsAdapter
import com.asinosoft.cdm.databinding.FragmentSelectBackgroundBinding
import com.asinosoft.cdm.viewmodels.SettingsViewModel

/**
 * Интерфейс выбора фоновой картинки
 */
class SelectBackgroundFragment : Fragment(), BackgroundsAdapter.Handler {
    private val model: SettingsViewModel by activityViewModels()

    private val backgroundImageActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.data?.let { uri ->
                model.setBackgroundImage(uri)
                findNavController(this).popBackStack()
                activity?.recreate()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = FragmentSelectBackgroundBinding.inflate(inflater, container, false)
        v.backgrounds.layoutManager =
            GridLayoutManager(requireContext(), 1, GridLayoutManager.VERTICAL, false)

        model.backgroundImages.observe(viewLifecycleOwner) { images ->
            v.backgrounds.adapter = BackgroundsAdapter(images, this)
        }

        model.loadBackgroundImages()

        return v.root
    }

    override fun onSelectBackground(uri: Uri?) {
        model.setBackgroundImage(uri)
        findNavController(this).popBackStack()
        activity?.recreate()
    }

    override fun onCustomBackground() {
        backgroundImageActivityResult.launch(
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
        )
    }
}
