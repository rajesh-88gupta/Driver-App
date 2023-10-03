package com.seentechs.newtaxidriver.common.util

import android.util.Log

import com.seentechs.newtaxidriver.BuildConfig

/**
 * Created by Seen Technologies on 9/7/18.
 */

object LogManager {

    private val TAG = "MCL"

    /**
     * Log Level Error
     */
    fun e(message: String) {
        if (BuildConfig.DEBUG) CommonMethods.DebuggableLogE(TAG, buildLogMsg(message))
    }

    /**
     * Log Level Warning
     */
    fun w(message: String) {
        if (BuildConfig.DEBUG) Log.w(TAG, buildLogMsg(message))
    }

    /**
     * Log Level Information
     */
    fun i(message: String) {
        if (BuildConfig.DEBUG) CommonMethods.DebuggableLogI(TAG, buildLogMsg(message))
    }

    /**
     * Log Level Debug
     */
    fun d(message: String) {
        if (BuildConfig.DEBUG) CommonMethods.DebuggableLogD(TAG, buildLogMsg(message))
    }

    /**
     * Log Level Verbose
     */
    fun v(message: String) {
        if (BuildConfig.DEBUG) CommonMethods.DebuggableLogV(TAG, buildLogMsg(message))
    }

    private fun buildLogMsg(message: String): String {
        val ste = Thread.currentThread().stackTrace[4]
        val sb: StringBuilder
        sb = StringBuilder()
        sb.append("[")
        sb.append(ste.fileName.replace(".java", ""))
        sb.append("::")
        sb.append(ste.methodName)
        sb.append("]")
        sb.append(message)

        return sb.toString()

    }

}
