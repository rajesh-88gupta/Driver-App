package com.seentechs.newtaxidriver.home.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import androidx.localbroadcastmanager.content.LocalBroadcastManager

class HeatMapUpdation : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        //Do something every 300 seconds(5min)
        LocalBroadcastManager.getInstance(context).sendBroadcast(Intent("HeatMapTimer"))
    }
}
