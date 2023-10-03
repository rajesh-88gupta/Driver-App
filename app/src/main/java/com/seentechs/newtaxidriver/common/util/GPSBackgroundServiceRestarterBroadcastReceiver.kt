package com.seentechs.newtaxidriver.common.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class GPSBackgroundServiceRestarterBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(GPSBackgroundServiceRestarterBroadcastReceiver::class.java.simpleName, "Service Stops! Oooooooooooooppppssssss!!!!")
        //context.startService(Intent(context, TrackingService::class.java))
    }
}
