/*
 * Copyright 2019 Anton Tananaev (anton@traccar.org)
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

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import com.google.android.gms.location.*
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonKeys
import javax.inject.Inject

class AndroidPositionProvider(context: Context, listener: PositionListener?) : PositionProvider(context, listener!!) {
    @Inject
    lateinit var sessionManager: SessionManager

    private val locationManager: LocationManager
    private val provider: String
    lateinit var  mFusedLocationClient: FusedLocationProviderClient
    lateinit private var locationCallback: LocationCallback

    @SuppressLint("MissingPermission")
    override fun startUpdates() {
        try {


            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

            initLocationCallBack()

            val req = LocationRequest()
            req.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            req.fastestInterval = 2000
            req.interval = 2000

            mFusedLocationClient.requestLocationUpdates(req,
                    locationCallback,
                    Looper.getMainLooper())

        } catch (e: RuntimeException) {
            listener.onPositionError(e)
        }
    }

    override fun stopUpdates() {
        mFusedLocationClient.removeLocationUpdates(locationCallback)
     //   locationManager.removeUpdates(this)
    }



    private fun initLocationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    if (lastLocation == null) {
                        lastLocation = location
                        lastUpdateLocation = location
                        lastDistanceCalculationLocation = location
                    }

                    if (location.time - lastLocation!!.time >= interval) {
                        processLocation(location)
                    }
                    if (location.time - lastUpdateLocation!!.time >= intervalUpdateLocation) {
                        updateLocationToServer(location)
                    }
                    if (sessionManager.tripStatus.equals(CommonKeys.TripDriverStatus.BeginTrip, true)) {
                        try {
                            if (lastDistanceCalculationLocation!=null) {
                                if (location.time - lastDistanceCalculationLocation!!.time >= intervalDistanceCalculation) {
                                    calculateDistance(location)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun requestSingleLocation() {
        try {
            val location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            if (location != null) {
                listener.onPositionUpdate(Position(location, getBatteryLevel(context)))
            } else {
                locationManager.requestSingleUpdate(provider, object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        listener.onPositionUpdate(Position(location, getBatteryLevel(context)))
                    }

                    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }, Looper.myLooper())
            }
        } catch (e: RuntimeException) {
            listener.onPositionError(e)
        }
    }



    companion object {
        private fun getProvider(accuracy: String): String {
            return when (accuracy) {
                "high" -> LocationManager.GPS_PROVIDER
                "low" -> LocationManager.PASSIVE_PROVIDER
                else -> LocationManager.NETWORK_PROVIDER
            }
        }
    }

    init {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        provider = getProvider("high")
        AppController.getAppComponent().inject(this)
    }


}