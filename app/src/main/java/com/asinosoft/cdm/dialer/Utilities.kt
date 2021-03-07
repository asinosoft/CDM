package com.asinosoft.cdm.dialer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.TelecomManager
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.asinosoft.cdm.Funcs
import com.asinosoft.cdm.detail_contact.Contact

class Utilities {

    val DEFAULT_DIALER_RC = 11
    val PERMISSION_RC = 10
    val MUST_HAVE_PERMISSIONS = arrayOf(Manifest.permission.CALL_PHONE)
    var contactDialer = Contact()

    fun toggleViewActivation(view: View) {
        view.isActivated = !view.isActivated
    }

    fun checkDefaultDialer(activity: FragmentActivity): Boolean {
        val packageName = activity.application.packageName
        return try {
            if (activity.getSystemService(TelecomManager::class.java).defaultDialerPackage != packageName) {
                // Prompt the user with a dialog to select this app to be the default phone app
                val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                    .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                activity.startActivityForResult(intent, DEFAULT_DIALER_RC)
                return false
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun checkPermissionsGranted(grantResults: IntArray): Boolean {
        for (result in grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) return false
        }
        return true
    }

    fun checkPermissionsGranted(context: Context?, permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (!checkPermissionsGranted(context, permissions)) return false
        }
        return true
    }

    fun askForPermissions(activity: FragmentActivity?, permissions: Array<String>) {
        ActivityCompat.requestPermissions(activity!!, permissions, PERMISSION_RC)
    }

    fun getNameFromPhoneNumber(context: Context, number: String): String? {
        val id = Funcs.getContactID(context, "$number")
        if (id != null) {
            if (context != null) {
                contactDialer.parseDataCursor(id, context)
            }
        }

        return contactDialer.name
    }

    fun getPhotoUriFromPhoneNumber(context: Context?, number: String): String {

        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
        val projection = arrayOf(
            ContactsContract.PhoneLookup.PHOTO_URI
        )

        try {
            val cursor = context!!.contentResolver.query(uri, projection, null, null, null)
            cursor.use {
                if (cursor?.moveToFirst() == true) {
                    return cursor.getStringValue(ContactsContract.PhoneLookup.PHOTO_URI) ?: ""
                }
            }
        } catch (e: Exception) {
        }

        return ""
    }

    fun hasNavBar(context: Context): Boolean {
        val resources = context.resources
        val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return id > 0 && resources.getBoolean(id)
    }

    fun navBarHeight(context: Context): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

}