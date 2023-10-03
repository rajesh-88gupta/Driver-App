package com.seentechs.newtaxidriver.google.locationmanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.SparseArray
import androidx.core.content.ContextCompat

abstract class WakefulBroadcastReceiver : BroadcastReceiver() {
    companion object {
        private const val EXTRA_WAKE_LOCK_ID = "android.support.content.wakelockid"
        private val mActiveWakeLocks = SparseArray<PowerManager.WakeLock>()
        private var mNextId = 1

        fun startWakefulForegroundService(context: Context, intent: Intent) {
            synchronized(mActiveWakeLocks) {
                val id = mNextId
                mNextId++
                if (mNextId <= 0) {
                    mNextId = 1
                }
                intent.putExtra(EXTRA_WAKE_LOCK_ID, id)
                ContextCompat.startForegroundService(context, intent)
                val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        WakefulBroadcastReceiver::class.java.simpleName)
                wl.setReferenceCounted(false)
                wl.acquire(60 * 1000.toLong())
                mActiveWakeLocks.put(id, wl)
            }
        }

        fun completeWakefulIntent(intent: Intent): Boolean {
            val id = intent.getIntExtra(EXTRA_WAKE_LOCK_ID, 0)
            if (id == 0) {
                return false
            }
            synchronized(mActiveWakeLocks) {
                val wl = mActiveWakeLocks[id]
                if (wl != null) {
                    wl.release()
                    mActiveWakeLocks.remove(id)
                    return true
                }
                return true
            }
        }
    }
}