package com.seentechs.newtaxidriver.common.helper

import android.content.Context
import android.net.ConnectivityManager

object NetworkUtil {

    var TYPE_WIFI = 1
    var TYPE_MOBILE = 2
    var TYPE_NOT_CONNECTED = 0


    fun getConnectivityStatus(context: Context): Int {
        val cm = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = cm.activeNetworkInfo
        if (null != activeNetwork) {
            if (activeNetwork.type == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI

            if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE
        }
        return TYPE_NOT_CONNECTED
    }

    fun getConnectivityStatusString(context: Context): String? {
        val conn = NetworkUtil.getConnectivityStatus(context)
        var status: String? = null
        if (conn == NetworkUtil.TYPE_WIFI) {
            status = "Wifi enabled"
        } else if (conn == NetworkUtil.TYPE_MOBILE) {
            status = "Mobile data enabled"
        } else if (conn == NetworkUtil.TYPE_NOT_CONNECTED) {
            status = "Not connected to Internet"
        }
        return status
    }
}
