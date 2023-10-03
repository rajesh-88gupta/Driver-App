package com.seentechs.newtaxidriver.common.helper

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.seentechs.newtaxidriver.common.network.AppController

import java.util.ArrayList

import javax.inject.Inject

/**
 * Created by Seen Technologies on 9/7/18.
 */

class RunTimePermission {

    lateinit @Inject
    var context: Context
    var permissionList: ArrayList<String> = ArrayList<String>()

    private val preferences: SharedPreferences

    var isFirstTimePermission: Boolean
        get() = preferences.getBoolean("isFirstTimePermission", false)
        set(isFirstTime) = preferences.edit().putBoolean("isFirstTimePermission", isFirstTime).apply()

    var fcmToken: String?
        get() = preferences.getString("fcmToken", "")
        set(fcmToken) = preferences.edit().putString("fcmToken", fcmToken).apply()

    private val isMarshmallow: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    init {
        AppController.getAppComponent().inject(this)
        preferences = context.getSharedPreferences("mcl_permission", Context.MODE_PRIVATE)
    }

    fun checkHasPermission(context: Activity?, permissions: Array<String>?): ArrayList<String> {
        permissionList.clear()
        if (isMarshmallow && context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(permission)
                }
            }
        }
        return permissionList
    }

    /**
     * To check the permission is blocked or not
     * @param context of the activity it is used in
     * @param permissions array of permissions
     * @return returns boolean value
     */


    fun isPermissionBlocked(context: Activity?, permissions: Array<String>?): Boolean {
        if (isMarshmallow && context != null && permissions != null) {//&& isFirstTimePermission()) {
            for (permission in permissions) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                    return true
                }
            }
        }
        return false
    }


    /**
     * To add list of permissions to be asked in the permissisonList
     * @param permissions array of permission to be requested to the user
     * @param grantResults to check wheather results are grant or not
     * @return array list
     */


    fun onRequestPermissionsResult(permissions: Array<String>, grantResults: IntArray?): ArrayList<String> {
        permissionList.clear()
        if (grantResults != null && grantResults.size > 0) {
            for (i in permissions.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(permissions[i])
                }
            }
        }
        return permissionList
    }
}

