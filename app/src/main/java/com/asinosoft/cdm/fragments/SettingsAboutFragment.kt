package com.asinosoft.cdm.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.asinosoft.cdm.R
import java.text.SimpleDateFormat

/**
 * Вкладка "Настройки / Информация"
 */
class SettingsAboutFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_settings_about, container, false)

        view.findViewById<TextView>(R.id.version).apply {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            text = context.getString(R.string.version).format(
                info.versionName,
  //              SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT).format(info.lastUpdateTime)
            )
        }

        view.findViewById<TextView>(R.id.shareApp).setOnClickListener {
            startActivity(shareApp())
        }

        view.findViewById<TextView>(R.id.rateUs).setOnClickListener {
            startActivity(rateUs())
        }

        view.findViewById<TextView>(R.id.showLicenses).setOnClickListener {
            showLicenses()
        }

        view.findViewById<TextView>(R.id.privacyPolicy).setOnClickListener {
            privacyPolicy()
        }

        return view
    }

    private fun shareApp(): Intent {
        val url = getString(R.string.share_url)
        val share = getString(R.string.share_via)
        val message = getString(R.string.share_message).format(url)
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, share)
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        return Intent.createChooser(intent, share)
    }

    private fun rateUs(): Intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(getString(R.string.rate_url))
    )

    private fun showLicenses() {
        findNavController().navigate(R.id.action_show_licenses)
    }

    private fun privacyPolicy() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacyPolicy_url)))
        startActivity(intent)
    }
}
