package com.asinosoft.cdm.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.webkit.WebViewAssetLoader
import com.asinosoft.cdm.R
import com.asinosoft.cdm.helpers.LocalContentWebViewClient

class LicensesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_licenses, container, false)

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(requireContext()))
            .addPathHandler("/res/", WebViewAssetLoader.ResourcesPathHandler(requireContext()))
            .build()

        view.findViewById<WebView>(R.id.license_text).apply {
            webViewClient = LocalContentWebViewClient(assetLoader)
            loadUrl("https://appassets.androidplatform.net/assets/licenses.html")
        }

        return view
    }
}
