package com.seentechs.newtaxidriver.home.service

/**
 * @package com.seentechs.newtaxidriver
 * @subpackage utils
 * @category Location Service
 * @author Seen Technologies
 *
 */

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat

import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods

import javax.inject.Inject

/*****************************************************************
 * LocationService
 */
/*
 * This class can be used to find out the status of the network connection and
 * the gps mode. The status can be checked every 5 seconds. If the GPS is on,
 * the location can be calculated from the GPS, otherwise the location can be
 * calculated from network location.
 */
class LocationService {
    private var lm: LocationManager? = null
    private var locationResult: LocationResult? = null
    private var gps_enabled = false
    private var network_enabled = false
    private var context: Context? = null

    @Inject
    lateinit var sessionManager: SessionManager
    @Inject
    lateinit var commonMethods: CommonMethods

    init {
        AppController.getAppComponent().inject(this)
    }
    internal var locationListenerGps: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            println("Check3")
            locationResult!!.gotLocation(location)
            try {
                val hasLocationPermission = ContextCompat.checkSelfPermission(context!!, Manifest.permission_group.LOCATION)
                if (hasLocationPermission == PackageManager.PERMISSION_GRANTED || hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                    lm!!.removeUpdates(this)
                    lm!!.removeUpdates(locationListenerNetwork)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        /**
         * Called when the provider is disabled by the user. If
         * requestLocationUpdates is called on an already disabled provider,
         * this method is called immediately.
         *
         * @param provider
         * the name of the location provider associated with this
         * update.
         */
        override fun onProviderDisabled(provider: String) {}

        /**
         * Called when the provider is enabled by the user.
         *
         * @param provider
         * the name of the location provider associated with this
         * update.
         */
        override fun onProviderEnabled(provider: String) {}

        /**
         * Called when the provider status changes. This method is called when a
         * provider is unable to fetch a location or if the provider has
         * recently become available after a period of unavailability.
         *
         * @param provider
         * the name of the location provider associated with this
         * update.
         * @param status
         * OUT_OF_SERVICE if the provider is out of service, and this
         * is not expected to change in the near future;
         * TEMPORARILY_UNAVAILABLE if the provider is temporarily
         * unavailable but is expected to be available shortly; and
         * AVAILABLE if the provider is currently available.
         * @param extras
         * an optional Bundle which will contain provider specific
         * status variables. A number of common key/value pairs for
         * the extras Bundle are listed below. Providers that use any
         * of the keys on this list must provide the corresponding
         * value as described below. satellites - the number of
         * satellites used to derive the fix
         */
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
    }
    internal var locationListenerNetwork: LocationListener = object : LocationListener {
        /**
         * Called when the location has changed. There are no restrictions on
         * the use of the supplied Location object.
         *
         * @param location
         * The new location, as a Location object.
         */
        override fun onLocationChanged(location: Location) {

            locationResult!!.gotLocation(location)
            try {
                val hasLocationPermission = ContextCompat.checkSelfPermission(context!!, Manifest.permission_group.LOCATION)
                if (hasLocationPermission == PackageManager.PERMISSION_GRANTED || hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                    lm!!.removeUpdates(this)
                    lm!!.removeUpdates(locationListenerGps)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        /**
         * Called when the provider is disabled by the user. If
         * requestLocationUpdates is called on an already disabled provider,
         * this method is called immediately.
         *
         * @param provider
         * the name of the location provider associated with this
         * update.
         */
        override fun onProviderDisabled(provider: String) {}

        /**
         * Called when the provider is enabled by the user.
         *
         * @param provider
         * the name of the location provider associated with this
         * update.
         */
        override fun onProviderEnabled(provider: String) {}

        /**
         * Called when the provider status changes. This method is called when a
         * provider is unable to fetch a location or if the provider has
         * recently become available after a period of unavailability.
         *
         * @param provider
         * the name of the location provider associated with this
         * update.
         * @param status
         * OUT_OF_SERVICE if the provider is out of service, and this
         * is not expected to change in the near future;
         * TEMPORARILY_UNAVAILABLE if the provider is temporarily
         * unavailable but is expected to be available shortly; and
         * AVAILABLE if the provider is currently available.
         * @param extras
         * an optional Bundle which will contain provider specific
         * status variables. A number of common key/value pairs for
         * the extras Bundle are listed below. Providers that use any
         * of the keys on this list must provide the corresponding
         * value as described below. satellites - the number of
         * satellites used to derive the fix
         */
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
    }

    fun getLocation(context: Context, result: LocationResult): Boolean {
        // I use LocationResult callback class to pass location value from
        // MyLocation to user code.
        println("Check6")
        locationResult = result
        this.context = context
        if (lm == null)
            lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // exceptions will be thrown if provider is not permitted.
        try {
            gps_enabled = lm!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            network_enabled = lm!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        // don't start listeners if no provider is enabled
        if (!gps_enabled && !network_enabled)
            return false

        try {
            Handler(Looper.getMainLooper()).post {
                val hasLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission_group.LOCATION)
                if (hasLocationPermission == PackageManager.PERMISSION_GRANTED || hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                    if (gps_enabled) {
                        lm!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListenerGps)
                    }
                    if (network_enabled) {
                        lm!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListenerNetwork)
                    }
                    GetLastLocation()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return true
    }

    /**
     * Checks the Gps status
     *
     * @return boolean value of location status
     */
    fun isLocationAvailable(context: Context): Boolean {
        if (lm == null)
            lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // exceptions will be thrown if provider is not permitted.
        try {
            gps_enabled = lm!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            network_enabled = lm!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return gps_enabled || network_enabled
    }

    /**
     * This abstract class is used to get the location from other class.
     */
    abstract class LocationResult {
        abstract fun gotLocation(location: Location)
    }

    /**
     * The GPS location and Network location to be calculated in every 5 seconds
     * with the help of this class
     */
    fun GetLastLocation() {

        try {
            println("Check8")
            val hasLocationPermission = ContextCompat.checkSelfPermission(context!!, Manifest.permission_group.LOCATION)
            if (hasLocationPermission == PackageManager.PERMISSION_GRANTED || hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                lm!!.removeUpdates(locationListenerGps)
                lm!!.removeUpdates(locationListenerNetwork)

                var net_loc: Location? = null
                var gps_loc: Location? = null
                if (gps_enabled)
                    gps_loc = lm!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                if (network_enabled)
                    net_loc = lm!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                lateinit var location: Location
                // if there are both values use the latest one
                if (gps_loc != null && net_loc != null) {
                    println("Check2")
                    if (gps_loc.time > net_loc.time) {
                        //locationResult.gotLocation(gps_loc);
                        location = gps_loc
                    } else {
                        // locationResult.gotLocation(net_loc);
                        location = net_loc
                    }
                    sessionManager.currentLatitude = location.latitude.toString()
                    sessionManager.currentLongitude = location.longitude.toString()
                    return
                }

                if (gps_loc != null) {
                    location = gps_loc
                    println("Check9")
                    // locationResult.gotLocation(gps_loc);
                    sessionManager.currentLatitude = location.latitude.toString()
                    sessionManager.currentLongitude = location.longitude.toString()
                    return
                }
                if (net_loc != null) {
                    location = net_loc
                    println("Check10")
                    //locationResult.gotLocation(net_loc);
                    sessionManager.currentLatitude = location.latitude.toString()
                    sessionManager.currentLongitude = location.longitude.toString()
                    return
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //locationResult.gotLocation(null);
    }

    companion object {

        var locationService: LocationService? = null

        fun defaultHandler(): LocationService {
            if (locationService == null) {
                locationService = LocationService()
            }
            return this.locationService!!
        }
    }
}
