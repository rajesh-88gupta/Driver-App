package com.seentechs.newtaxidriver.google.locationmanager

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startForegroundService
import com.seentechs.newtaxidriver.common.network.AppController
import java.util.*


class TrackingServiceListener(activity: Activity?) {
    private var alarmManager: AlarmManager? = null
    private var alarmIntent: PendingIntent? = null
    private var activity : Activity? = null

    companion object {
        private const val ALARM_MANAGER_INTERVAL = 15000
        private const val PERMISSIONS_REQUEST_LOCATION = 2
    }

    init {
        this?.activity = activity
        AppController.getAppComponent().inject(this)
        alarmManager = this.activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

        alarmIntent =
            if (Build.VERSION.SDK_INT >= 31){
                PendingIntent.getBroadcast(
                    this.activity,
                    0,
                    Intent(this.activity, AutoStartReceiver::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            }else {

                PendingIntent.getBroadcast(
                    this.activity,
                    0,
                    Intent(this.activity, AutoStartReceiver::class.java),
                    0
                )
            }

    }

    fun startTrackingService(checkPermission: Boolean, permission: Boolean) {
        var permission = permission
        if (checkPermission) {
            val requiredPermissions: MutableSet<String> = HashSet()
            if (ContextCompat.checkSelfPermission(this.activity!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            if (ContextCompat.checkSelfPermission(this.activity!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                    && ContextCompat.checkSelfPermission(this.activity!!, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }*/
            permission = requiredPermissions.isEmpty()
            if (!permission) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(this.activity!!, requiredPermissions.toTypedArray(), PERMISSIONS_REQUEST_LOCATION)
                }
                return
            }
        }
        if (permission) {
            //setPreferencesEnabled(false);
            startForegroundService(this.activity!!, Intent(this.activity, TrackingService::class.java))
            alarmManager!!.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    ALARM_MANAGER_INTERVAL.toLong(), ALARM_MANAGER_INTERVAL.toLong(), alarmIntent)
        } else {
            //sharedPreferences.edit().putBoolean(KEY_STATUS, false).apply();
            //TwoStatePreference preference = findPreference(KEY_STATUS);
            //preference.setChecked(false);
        }
    }

    fun stopTrackingService() {
        this.activity=activity
        alarmManager!!.cancel(alarmIntent)
        this.activity?.stopService(Intent(this.activity, TrackingService::class.java))
        // setPreferencesEnabled(true);
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            var granted = true
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false
                    break
                }
            }
            startTrackingService(false, granted)
        }
    }
}