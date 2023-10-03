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

import android.content.Context
import android.location.Location
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.firebase.database.*
import com.google.gson.Gson
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.database.Sqlite
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonKeys
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.Enums
import com.seentechs.newtaxidriver.common.util.RequestCallback
import com.seentechs.newtaxidriver.home.datamodel.UserLocationModel
import com.seentechs.newtaxidriver.home.datamodel.firebase_keys.FirebaseDbKeys
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import com.seentechs.newtaxidriver.home.map.DriverLocation
import java.text.DecimalFormat
import java.util.*
import javax.inject.Inject


class UpdateLocations(context: Context) : ServiceListener {

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var customDialog: CustomDialog

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var sessionManager: SessionManager

    private var sqliteDB: Sqlite
    private var mFirebaseDatabase: DatabaseReference? = null
    private var mSearchedLocationReferenceListener: ValueEventListener? = null


    init {
        AppController.getAppComponent().inject(this)
        sqliteDB = Sqlite(context)
        val mFirebaseInstance = FirebaseDatabase.getInstance()
        mFirebaseDatabase = mFirebaseInstance.getReference(context.getString(R.string.real_time_db))
    }

    fun updateLocationInSession(location: Location) {
        sessionManager.currentLatitude = location.latitude.toString()
        sessionManager.latitude = location.latitude.toString()
        sessionManager.currentLongitude = location.longitude.toString()
        sessionManager.longitude = location.longitude.toString()
    }

    fun updateLocationInFirebaseDB(location: Location, currentDistance: Double, context: Context) {
        val driverLocation = DriverLocation(location.latitude.toString(), location.longitude.toString())
        if (sessionManager.isTrip && sessionManager.tripId != null) {
            mFirebaseDatabase?.child(FirebaseDbKeys.LIVE_TRACKING_NODE)?.child(sessionManager.tripId!!)?.setValue(driverLocation)
        }
        /*
       // Taxi Ride Firebase update functions
       if(sessionManager.isTrip)
        {
            AddFirebaseDatabase().removeDriverFromGeofire(context)
        }else{
            updateDriverLocationInGeoFire(location,currentDistance,context)
        }


        if (sessionManager.tripId != null && sessionManager.tripId != "") {
            if (currentDistance < 100) {
                updateLocationFireBase(location.latitude,
                        location.longitude))
            }
        } else {
            if (mSearchedLocationReferenceListener != null)
                mFirebaseDatabase!!.removeEventListener(mSearchedLocationReferenceListener!!)
        }*/
    }

    fun updateDriverLocationInGeoFire(location: Location?, currentDistance: Double, context: Context) {
        if (CommonKeys.driverInActive == 1) {

            if (location != null) {
                if (currentDistance > 15) {
                    if (sessionManager.userId != null && !sessionManager.userId.equals("") && sessionManager.driverStatus.equals("Online", ignoreCase = true) && (sessionManager.isPool && sessionManager.isTrip || !sessionManager.isTrip)) {
                        val ref = FirebaseDatabase.getInstance().reference.child(context.getString(R.string.real_time_db)).child(FirebaseDbKeys.GEOFIRE)
                        val geoFire = GeoFire(ref)
                        geoFire.setLocation(sessionManager.userId, GeoLocation(location.latitude, location.longitude), GeoFire.CompletionListener { key, error ->
                            if (error != null) {
                                System.err.println("There was an error saving the location to GeoFire: $error")
                            } else {
                                println("Location saved on server successfully!")
                            }
                        })
                    }
                }
            }
        }

    }

    fun updateLocationFireBaseForPool(lat: Double, lng: Double) {
            val driverLocation = DriverLocation(lat.toString(), lng.toString())


            val poolIds: List<String> = sessionManager.poolIds!!.split(",").map { it.trim() }

            for (i in poolIds.indices) {
                if (poolIds[i] != "")
                    mFirebaseDatabase!!.child(FirebaseDbKeys.LIVE_TRACKING_NODE).child(poolIds.get(i)).setValue(driverLocation)
            }

            if (mSearchedLocationReferenceListener == null) {
                addLatLngChangeListener() // Get Driver Lat Lng
            }
    }

    /**
     * Driver LOCATION_AND_WRITEPERMISSION_ARRAY change listener
     */
    private fun addLatLngChangeListener() {

        val query = mFirebaseDatabase!!.child(FirebaseDbKeys.LIVE_TRACKING_NODE).child(sessionManager.tripId!!)

        mSearchedLocationReferenceListener = query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (sessionManager.tripId != null) {
                    val driverLocation = dataSnapshot.getValue(DriverLocation::class.java)

                    // Check for null
                    if (driverLocation == null) {
                        CommonMethods.DebuggableLogE("UpdateLocations", "Driver LOCATION_AND_WRITEPERMISSION_ARRAY data is null!")
                        return
                    }

                    CommonMethods.DebuggableLogE("UpdateLocations", "Driver LOCATION_AND_WRITEPERMISSION_ARRAY data is changed!" + driverLocation.lat + ", " + driverLocation.lng)
                } else {
                    query.removeEventListener(this)
                    mFirebaseDatabase!!.removeEventListener(this)
                    mFirebaseDatabase!!.onDisconnect()
                }

            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                CommonMethods.DebuggableLogE("UpdateLocations", "Failed to read user", error.toException())
            }
        })
    }

    /*
  * Get location change and update
  */
    fun updateLocationChange(currentLocation: Location, lastLocation: Location, distance: Double, maximumDistance: Double, context: Context) {
        var twoDForm: DecimalFormat = DecimalFormat("#.#######")
        val tripStatus = sessionManager.tripStatus
        val beginTrip = CommonKeys.TripDriverStatus.BeginTrip
        var distanceKM = 0.0
        if (tripStatus != null && tripStatus == beginTrip) {
            sessionManager.driverStatus = CommonKeys.DriverStatus.Online
        }

        /*
         *  Update location
         */
        if (tripStatus != null && tripStatus == beginTrip) {
            var distanceKM = (distance / 1000.0).toFloat()

            try {
                distanceKM = java.lang.Float.valueOf(twoDForm.format(distanceKM.toDouble()).replace(",".toRegex(), "."))
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (distance < maximumDistance) {
                sqliteDB.AddUserLocation(UserLocationModel(java.lang.Double.valueOf(sessionManager.latitude!!), java.lang.Double.valueOf(sessionManager.longitude!!), ""))
            }
        }

        updateLocation(distanceKM)
    }


    private fun updateLocation(distance: Double) {
        val tripStatus = sessionManager.tripStatus
        val beginTrip = CommonKeys.TripDriverStatus.BeginTrip
        val confirmTrip = CommonKeys.TripDriverStatus.ConfirmArrived

        val locationHashMap = HashMap<String, String>()
        locationHashMap["latitude"] = sessionManager.latitude!!
        locationHashMap["longitude"] = sessionManager.longitude!!
        locationHashMap["user_type"] = sessionManager.type!!
        locationHashMap["car_id"] = sessionManager.vehicle_id!!

        if (tripStatus != null && tripStatus == beginTrip) {
            locationHashMap["total_km"] = String.format("%.6f", distance)
            locationHashMap["status"] = "Trip"
            locationHashMap["trip_id"] = sessionManager.tripId!!
        } else if (tripStatus != null && tripStatus == confirmTrip) {
            locationHashMap["total_km"] = "0"
            locationHashMap["trip_id"] = sessionManager.tripId!!
            locationHashMap["status"] = "Trip"
        } else {
            locationHashMap["status"] = sessionManager.driverStatus!!
        }
        println("UPDATE FROM UPDATELOCATION")
        locationHashMap["token"] = sessionManager.accessToken!!
        apiService.updateLocation(locationHashMap).enqueue(RequestCallback(Enums.REQ_UPDATE_ONLINE, this))
    }

    override fun onSuccess(jsonResp: JsonResponse?, data: String?) {
        //TODO("Not yet implemented")
    }

    override fun onFailure(jsonResp: JsonResponse?, data: String?) {
        //TODO("Not yet implemented")
    }
}