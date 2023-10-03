/*
 * Copyright 2013 - 2019 Anton Tananaev (anton@traccar.org)
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
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.BatteryManager
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.helper.Constants.GoogleDistanceType
import com.seentechs.newtaxidriver.common.util.CommonKeys
import com.seentechs.newtaxidriver.google.direction.DirectionDataModel
import com.seentechs.newtaxidriver.google.direction.GetDirectionData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

abstract class PositionProvider {

    interface PositionListener {
        fun onPositionUpdate(position: Position?)
        fun on1SDistanceUpdate(distance: Double) {}
        fun on10DistanceUpdate(distance: Double) {}
        fun on1mDistanceUpdate(distance: Double) {}
        fun on1mGoogleDistanceUpdate(distance: Double) {}
        fun onCalculatedDistanceForEndTrip(distance: Double) {}
        fun onPositionError(error: Throwable?)
    }

    constructor(context: Context, listener: PositionListener) {
        this.context = context
        this.listener = listener
    }

    constructor(context: Context) {
        this.context = context
    }

    private var googleDistance: Int = 0
    lateinit var context: Context
    lateinit var listener: PositionListener
    val uiScope = CoroutineScope(Dispatchers.Main)

    var distance: Double = 50.0

    // Minimum distance to check moved or not
    private var minDistance: Double = 0.0

    // Check for GPS issues
    protected var maxDistance: Double = 750.0
    var angle: Double = 0.0

    var minimumInterval: Long = 1000

    // Update location in Firebase and GeoFire every second
    var interval: Long = 1 * 1000.toLong()

    // Update location in server every 10 second
    protected var intervalUpdateLocation: Long = 10 * 1000.toLong()

    // Calculate distance while trip every minutes
    protected var intervalDistanceCalculation: Long = 60 * 1000.toLong()

    var lastStoredLocation = Location("")

//    private var updateLocations= UpdateLocations (context)

    private var totalDistance = 0.0
    private var totalLastDistance = 0.0
    private var totalCalculateDistance = 0.0

    abstract fun startUpdates()
    abstract fun stopUpdates()
    abstract fun requestSingleLocation()
    protected fun processLocation(location: Location?) {
        if (location != null && (lastLocation == null || location.time - lastLocation!!.time >= interval
                        || distance > 0 && location.distanceTo(lastLocation) >= distance || angle > 0
                        && abs(location.bearing - lastLocation!!.bearing) >= angle)) {

            val currentDistance = location.distanceTo(lastLocation).toDouble()

            if (lastLocation != null && location != lastLocation && currentDistance > minDistance) {
                totalDistance += currentDistance

                listener.onPositionUpdate(Position(location, getBatteryLevel(context)))
                listener.on1SDistanceUpdate(currentDistance)

                // listener.on1DistanceUpdate(currentDistance)

                val updateLocations = UpdateLocations(context)
                updateLocations.updateLocationInSession(location)
                updateLocations.updateLocationInFirebaseDB(location, currentDistance, context)
                updateLocations.updateDriverLocationInGeoFire(location, currentDistance, context)
                updateLocations.updateLocationFireBaseForPool(location.latitude, location.longitude)
                println("UpdateLocations updated")
            } else {
                Log.i(TAG, if (location != null) "same location" else "location nil")
            }
            lastLocation = location
        } else {
            Log.i(TAG, if (location != null) "location ignored" else "location nil")
        }
    }

    /**
     * Update location to server every 10 second
     * @param location
     */
    protected fun updateLocationToServer(location: Location?) {
        if (location != null && (lastUpdateLocation == null || location.time - lastUpdateLocation!!.time >= interval || distance > 0 && location.distanceTo(lastUpdateLocation) >= distance || angle > 0 && Math.abs(location.bearing - lastUpdateLocation!!.bearing) >= angle)) {
            val currentDistance = location.distanceTo(lastUpdateLocation).toDouble()
            if (lastUpdateLocation != null && location !== lastUpdateLocation && currentDistance > minDistance) {
                totalLastDistance += currentDistance
                val totalKM = totalLastDistance / 1000
                listener.on10DistanceUpdate(currentDistance)
                val updateLocations = UpdateLocations(context)
                lastUpdateLocation?.let { updateLocations.updateLocationChange(location, it, currentDistance, maxDistance, context) }

            } else {
                Log.i(TAG, if (location != null) "same location" else "location nil")
            }
            lastUpdateLocation = location
        } else {
            Log.i(TAG, if (location != null) "location ignored" else "location nil")
        }
    }

    /**
     * Calculate the distance from google every minutes
     * @param location
     */
    fun calculateDistance(location: Location?) {
        if (location != null && (lastDistanceCalculationLocation == null || location.time - lastDistanceCalculationLocation!!.time >= interval || distance > 0 && location.distanceTo(lastDistanceCalculationLocation) >= distance || angle > 0 && Math.abs(location.bearing - lastDistanceCalculationLocation!!.bearing) >= angle)) {
            if (lastDistanceCalculationLocation == null) {
                lastDistanceCalculationLocation = lastStoredLocation
            }
            val currentDistance = location.distanceTo(lastDistanceCalculationLocation).toDouble()
            if (lastDistanceCalculationLocation != null && location != lastDistanceCalculationLocation && currentDistance > minDistance) {
                listener.onPositionUpdate(Position(location, getBatteryLevel(context)))
                if (context.resources.getString(R.string.distancetype).equals(GoogleDistanceType, true)) {
                    callGoogleDistance(location, lastDistanceCalculationLocation)
                } /*else {
                        callNormalDistance(currentDistance)
                    }*/
            } else {
                Log.i(TAG, if (location != null) "same location" else "location nil")
                listener.onCalculatedDistanceForEndTrip(0.0)
            }
            lastDistanceCalculationLocation = location
        } else {
            Log.i(TAG, if (location != null) "location ignored" else "location nil")
            listener.onCalculatedDistanceForEndTrip(0.0)
        }
    }

    private fun callNormalDistance(currentDistance: Double) {
        totalCalculateDistance += currentDistance
        val totalKM = totalCalculateDistance / 1000
        println("calculateDistance *** $totalKM")
        //Toast.makeText(context,"Total KM  "+totalKM,Toast.LENGTH_SHORT).show();
        //listener.on1mDistanceUpdate(totalKM);
        listener.on1mDistanceUpdate(currentDistance)
    }

    private fun callGoogleDistance(location: Location, lastLocation: Location?) {
        uiScope.launch {
            var getDirectionData = GetDirectionData(context)
            var directionDataModel: DirectionDataModel? = null
            googleDistance++
            println(CommonKeys.UpdatePolyline + " callGoogleDistance ${googleDistance.toString()}")
            directionDataModel = getDirectionData.directionParse(CommonKeys.DirectionParse.DistCalcResume
                    , LatLng(lastLocation!!.latitude, lastLocation!!.longitude), LatLng(location.latitude, location.longitude))

            withContext(Dispatchers.Main) {
                if (directionDataModel.polyLineType == CommonKeys.DirectionParse.DistCalcResume) {
                    try {
                        listener.on1mGoogleDistanceUpdate(directionDataModel.distances.toDouble())
                    } catch (e: Exception) {
                        e.printStackTrace()
                        listener.on1mGoogleDistanceUpdate(0.00)
                    }
                }
            }

        }
    }

    protected fun getBatteryLevel(context: Context): Double {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        if (batteryIntent != null) {
            val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 1)
            return level * 100.0 / scale
        }
        return 0.0
    }

    companion object {
        private val TAG = PositionProvider::class.java.simpleName
        var lastLocation: Location? = null
        var lastUpdateLocation: Location? = null
        var lastDistanceCalculationLocation: Location? = null
    }
}