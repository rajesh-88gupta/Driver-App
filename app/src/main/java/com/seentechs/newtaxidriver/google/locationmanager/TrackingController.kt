/*
 * Copyright 2015 - 2019 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seentechs.newtaxidriver.google.locationmanager

import android.content.Context
import android.os.Handler
import android.util.Log
import com.seentechs.newtaxidriver.BuildConfig
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonKeys
import com.seentechs.newtaxidriver.common.util.CommonKeys.CheckNormalDistanceEverySec
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.google.locationmanager.PositionProviderFactory.create
import javax.inject.Inject

class TrackingController(private val context: Context) : PositionProvider.PositionListener, NetworkManager.NetworkHandler {
    @Inject
    lateinit var sessionManager: SessionManager
    @Inject
    lateinit var commonMethods: CommonMethods

    private var isOnline: Boolean
    private val handler: Handler = Handler()
    private val buffer = false
    private val positionProvider: PositionProvider = create(context, this)
    private val networkManager: NetworkManager = NetworkManager(context, this)
    fun start() {
        try {
            positionProvider.startUpdates()
        } catch (exception:SecurityException) {
            Log.w(TAG, exception)
        }
        networkManager.start()
    }

    fun stop() {
        networkManager.stop()
        try {
            positionProvider.stopUpdates()
        } catch (e: SecurityException) {
            Log.w(TAG, e)
        }
        handler.removeCallbacksAndMessages(null)
    }

    override fun onPositionUpdate(position: Position?) {
        if (position != null) {
           // Toast.makeText(context,"position updated"+position,Toast.LENGTH_SHORT).show();

        }
    }

    override fun on1SDistanceUpdate(distance: Double) {
        if (sessionManager.tripStatus.equals(CommonKeys.TripDriverStatus.BeginTrip,true)) {
            if (distance<CheckNormalDistanceEverySec) {
                val totaldistance = sessionManager.totalDistanceEverySec + distance
                sessionManager.totalDistanceEverySec = totaldistance.toFloat()
                if (BuildConfig.DEBUG) {
                    if (!commonMethods.getFileWriter()) {
                        commonMethods.createFileAndUpdateDistance(context)
                    }
                    commonMethods.updateDistanceInFile(sessionManager.totalDistanceEverySec,"DistanceEvery1Sec")
                }
            }
        }
    }

    override fun on1mDistanceUpdate(distance: Double) {
        commonMethods.updateDistanceInLocal(distance)
    }

    override fun on1mGoogleDistanceUpdate(distance: Double) {
        commonMethods.updateDistanceInLocal(distance)
    }



    override fun onPositionError(error: Throwable?) {}
    override fun onNetworkUpdate(isOnline: Boolean) {
        //int message = isOnline ? R.string.status_network_online : R.string.status_network_offline;
        //StatusActivity.addMessage(context.getString(message));
        if (!this.isOnline && isOnline) {
        }
        this.isOnline = isOnline
    }

    private fun retry() {
        handler.postDelayed({
            if (isOnline) {
            }
        }, RETRY_DELAY.toLong())
    }

    companion object {
        private val TAG = TrackingController::class.java.simpleName
        private const val RETRY_DELAY = 10 * 1000
        private const val WAKE_LOCK_TIMEOUT = 120 * 1000
    }

    init {
        isOnline = networkManager.isOnline
        AppController.getAppComponent().inject(this)
    }
}