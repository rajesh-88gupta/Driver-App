package com.seentechs.newtaxidriver.home.map

/**
 * @package com.seentechs.newtaxidriver
 * @subpackage map
 * @category GpsService
 * @author Seen Technologies
 *
 */

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PixelFormat
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import android.provider.Settings
import android.text.TextUtils
import android.text.format.Time
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.firebase.database.*
import com.google.gson.Gson
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.database.AddFirebaseDatabase
import com.seentechs.newtaxidriver.common.database.Sqlite
import com.seentechs.newtaxidriver.common.helper.ComplexPreferences
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.common.helper.ManualBookingDialog
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonKeys
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.Enums.REQ_UPDATE_ONLINE
import com.seentechs.newtaxidriver.common.util.GPSBackgroundServiceRestarterBroadcastReceiver
import com.seentechs.newtaxidriver.common.util.RequestCallback
import com.seentechs.newtaxidriver.home.MainActivity
import com.seentechs.newtaxidriver.home.datamodel.LatLngModel
import com.seentechs.newtaxidriver.home.datamodel.UserLocationModel
import com.seentechs.newtaxidriver.home.datamodel.firebase_keys.FirebaseDbKeys
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import com.seentechs.newtaxidriver.home.pushnotification.Config
import com.seentechs.newtaxidriver.home.pushnotification.NotificationUtils
import com.seentechs.newtaxidriver.trips.RequestReceiveActivity
import com.seentechs.newtaxidriver.trips.voip.CallProcessingActivity
import com.seentechs.newtaxidriver.trips.voip.NewTaxiSinchService
import org.json.JSONException
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/* ************************************************************
                      GpsService
Get and update driver current location in server
*************************************************************** */
class GpsService : Service(), ServiceListener {
    lateinit private var params: WindowManager.LayoutParams
    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var sessionManager: SessionManager
    @Inject
    lateinit var commonMethods: CommonMethods
    @Inject
    lateinit var customDialog: CustomDialog

    @Inject
    lateinit var gson: Gson

    lateinit var SqliteDB: Sqlite

    private val MAX_DISTANCE = 750



    private lateinit var fusedLocationClient: FusedLocationProviderClient

    internal var updateLatLng = false
    lateinit var dialog: AlertDialog
    var mLastLocation: Location? = null
    var mCheckLocation: Location? = null
    var count = 0
    var counts = 0
    var oldlatitude = 0.0
    var oldlongitude = 0.0
    var gps_enabled = false
    var network_enabled = false
    var timerTask: MyTimerTask = MyTimerTask()
    var updateLocationandReport = false
    var twoDForm: DecimalFormat = DecimalFormat("#.#######")
    protected var isInternetAvailable: Boolean = false
    private var first = true
    private var isbeginfirst = true
    private var isotherfirst = true
    private var distanceInMeters = 0f
    private var distanceInKM = 0f
    internal var latLngModel: LatLngModel? = null
    internal var currentHourIn12Format: Int = 0
    internal var currentMinIn12Format: Int = 0
    private var totalDistanceInKM = 0f
    private val timer = Timer()
    internal var duration = 10

    private var wakeLock: PowerManager.WakeLock? = null
    internal lateinit var complexPreferences: ComplexPreferences
    private var listener: LocationListener? = null
    private var locationManager: LocationManager? = null
    private var mFirebaseDatabase: DatabaseReference? = null
    private var mSearchedLocationReferenceListener: ValueEventListener? = null
    private var locationUpdatedAt = java.lang.Long.MIN_VALUE
    private val FASTEST_INTERVAL = LOCATION_INTERVAL_CHECK // use whatever suits you
    private var location: Location? = null
    private var lastLocation: Location? = null
    private var currentLocation: Location? = null
    private var distance: Float = 0.toFloat()
    lateinit var carmarker: Marker
    var marker: Marker? = null
    private lateinit var locationCallback: LocationCallback


    var geoFire:GeoFire ?=null
    var ref:DatabaseReference?=null
    var polyline: Polyline? = null
    var startbear = 0f
    var endbear = 0f

    val movepoints = ArrayList<LatLng>()
    val polylinepoints = ArrayList<LatLng>()
    var speed = 13f
    var ETACalculatingwithDistance: Double = 0.0
    var time: String = "1"

    private var mRegistrationBroadcastReceiver: BroadcastReceiver? = null

    var floatWidget: View? = null
    var mWindowManager: WindowManager? = null


    init {

        SqliteDB = Sqlite(this)
    }

    private val mBinder = LocalBinder()


    inner class LocalBinder : Binder() {
        fun getService(): GpsService {
            return this@GpsService
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }


    private var mSearchedRequestReferenceListener: ValueEventListener? = null
    lateinit private var mFirebaseDatabaseRequest: DatabaseReference
    lateinit var queryRequest: Query



    @SuppressLint("InvalidWakeLockTag")
    override fun onCreate() {

        AppController.getAppComponent().inject(this)

        CommonMethods.DebuggableLogD("GPS Service Called", "GPS Service Called")

        val mgr = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock")
        complexPreferences = ComplexPreferences.getComplexPreferences(this, "mypref", Context.MODE_PRIVATE)
        latLngModel = complexPreferences.getObject("latLngList", LatLngModel::class.java)
        latLngList = ArrayList()


        initWidget()


        val mFirebaseInstance = FirebaseDatabase.getInstance()

        mFirebaseDatabaseRequest=mFirebaseInstance.getReference(applicationContext.getString(R.string.real_time_db))



        if (latLngModel != null) {
            latLngList.addAll(latLngModel!!.latLngList)

        }

        dialog = commonMethods.getAlertDialog(this)

        addRequestReceiveListner()


        // get reference to 'Driver LOCATION_AND_WRITEPERMISSION_ARRAY' node
        mFirebaseDatabase = mFirebaseInstance.getReference(applicationContext.getString(R.string.real_time_db))




         locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult ?: return
                    lateinit var lastLocation : Location
                    for (location in locationResult.locations){
                        sessionManager.currentLatitude = location?.latitude.toString()
                        sessionManager.latitude = location?.latitude.toString()
                        sessionManager.currentLongitude = location?.longitude.toString()
                        sessionManager.longitude = location?.longitude.toString()
                        //println("location call : "+location?.latitude.toString()+","+location?.longitude.toString())
                        //startMyOwnForeground()
                        lastLocation = location
                    }


                    //println("LocationListener called : $lastLocation")

                    CommonMethods.DebuggableLogD("GPS Service Loc Changed", lastLocation.toString())
                    println("onLocationChanged called : $lastLocation")
                    sessionManager.currentLatitude = java.lang.Double.toString(lastLocation.latitude)
                    sessionManager.latitude = java.lang.Double.toString(lastLocation.latitude)
                    sessionManager.currentLongitude = java.lang.Double.toString(lastLocation.longitude)
                    sessionManager.longitude = java.lang.Double.toString(lastLocation.longitude)

                    // println("userid"+sessionManager.userId+"vehicleid: "+sessionManager.vehicleId +"driverinactive"+CommonKeys.driverInActive+"driverstatus"+sessionManager.driverStatus)

                    if(sessionManager.isTrip)
                    {
                        AddFirebaseDatabase().removeDriverFromGeofire(this@GpsService)
                    }else{
                        updatedriverlocationingeofire(lastLocation)
                    }


                    val pushNotification = Intent(Config.DISTANCE_CALCULATION)
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(pushNotification)





                    if (sessionManager.tripId != null && sessionManager.tripId != "") {
                        var distance = 0f
                        if (location != null) {
                            distance = lastLocation.distanceTo(location)
                            if (distance < 100) {
                                updateLocationFireBase(lastLocation.latitude,lastLocation.longitude)
                            }
                        }
                    } else {
                        CommonMethods.DebuggableLogE(TAG, "Driver LOCATION_AND_WRITEPERMISSION_ARRAY data removed!")
                        if (mSearchedLocationReferenceListener != null)
                            mFirebaseDatabase!!.removeEventListener(mSearchedLocationReferenceListener!!)
                    }
                    location = lastLocation


                    //locationupdateCall()
                }
            }







        listener = object : LocationListener {
            override fun onLocationChanged(LastLocation: Location) {

               /* println("LocationListener called : $LastLocation")

                CommonMethods.DebuggableLogD("GPS Service Loc Changed", LastLocation.toString())
                println("onLocationChanged called : $LastLocation")
                sessionManager.currentLatitude = java.lang.Double.toString(LastLocation.latitude)
                sessionManager.latitude = java.lang.Double.toString(LastLocation.latitude)
                sessionManager.currentLongitude = java.lang.Double.toString(LastLocation.longitude)
                sessionManager.longitude = java.lang.Double.toString(LastLocation.longitude)

               // println("userid"+sessionManager.userId+"vehicleid: "+sessionManager.vehicleId +"driverinactive"+CommonKeys.driverInActive+"driverstatus"+sessionManager.driverStatus)

                if(sessionManager.isTrip)
                {
                    AddFirebaseDatabase().removeDriverFromGeofire(this@GpsService)
                }else{
                    updatedriverlocationingeofire(LastLocation)
                }


                val pushNotification = Intent(Config.DISTANCE_CALCULATION)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(pushNotification)





                if (sessionManager.tripId != null && sessionManager.tripId != "") {
                    var distance = 0f
                    if (location != null) {
                        distance = LastLocation.distanceTo(location)
                        if (distance < 100) {
                            updateLocationFireBase(java.lang.Double.toString(LastLocation.latitude),
                                    java.lang.Double.toString(LastLocation.longitude))
                        }
                    }
                } else {
                    CommonMethods.DebuggableLogE(TAG, "Driver LOCATION_AND_WRITEPERMISSION_ARRAY data removed!")
                    if (mSearchedLocationReferenceListener != null)
                        mFirebaseDatabase!!.removeEventListener(mSearchedLocationReferenceListener!!)
                }
                location = LastLocation
*/

                //locationupdateCall()
            }

            override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {


            }

            override fun onProviderEnabled(s: String) {


            }

            override fun onProviderDisabled(s: String) {


            }
        }

        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        //exceptions will be thrown if provider is not permitted.
        try {
            gps_enabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }

        try {
            network_enabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
        }
/*

        //don't start listeners if no provider is enabled

        if (network_enabled)

            locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL.toLong(), LOCATION_DISTANCE, listener!!)
        //Newly Added
        */
/*  if (locationManager != null) {
                mLastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (mLastLocation!=null){
                    oldlatitude=mLastLocation.getLatitude();
                    oldlongitude=mLastLocation.getLongitude();
                }
            }*//*

        if (gps_enabled)

            locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL.toLong(), LOCATION_DISTANCE, listener!!)
*/

        if (gps_enabled){
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            startLocationUpdates()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMyOwnForeground()
        }

    }

    private fun updatedriverlocationingeofire(lastLocation: Location?) {
        println("isPool"+sessionManager.isPool)
        if (CommonKeys.driverInActive === 1 ) {


            if(currentLocation!=null)
            {
                val distanceInMeters = lastLocation!!.distanceTo(currentLocation)
                println("distancemeter"+distanceInMeters)
                if(distanceInMeters>15)
                {
                    if (sessionManager.userId != null && !sessionManager.userId.equals("") && sessionManager.driverStatus.equals("Online",ignoreCase = true) && !sessionManager.isTrip) {
                        val  ref=FirebaseDatabase.getInstance().reference.child(applicationContext.getString(R.string.real_time_db)).child(FirebaseDbKeys.GEOFIRE)
                        val  geoFire= GeoFire(ref)
                        geoFire.setLocation( sessionManager.userId, GeoLocation(lastLocation!!.latitude, lastLocation!!.longitude), GeoFire.CompletionListener { key, error ->
                            if (error != null) {
                                System.err.println("There was an error saving the location to GeoFire: $error")
                            } else {
                                println("Location saved on server successfully!")
                            }
                        })
                    }
                }
            }



            //  databaseReference.child(sessionManager.getVehicleId());
        }

    }


    fun locationupdateCall() {
        if (!TextUtils.isEmpty(sessionManager.currentLatitude) && !TextUtils.isEmpty(sessionManager.currentLongitude)) {
            if (!TextUtils.isEmpty(sessionManager.lastLatitude) && !TextUtils.isEmpty(sessionManager.lastLongitude)) {
                val lat = java.lang.Double.valueOf(sessionManager.lastLatitude!!)
                val lng = java.lang.Double.valueOf(sessionManager.lastLongitude!!)
                lastLocation = Location("lastloc")
                lastLocation!!.latitude = lat
                lastLocation!!.longitude = lng
            }

            val lat = java.lang.Double.valueOf(sessionManager.currentLatitude!!)
            val lng = java.lang.Double.valueOf(sessionManager.currentLongitude!!)
            currentLocation = Location("curloc")
            currentLocation!!.latitude = lat
            currentLocation!!.longitude = lng
            println("locationupdateCall called : ")
            CommonMethods.DebuggableLogE("location ", "Update Call currentLocation:" + +currentLocation!!.latitude + " , " + currentLocation!!.longitude)
            if(sessionManager.isTrip)
            {
                AddFirebaseDatabase().removeDriverFromGeofire(this@GpsService)
            }else
            {
                updatedriverlocationingeofire(currentLocation)
            }

            updateLocationChange()
        } /*else {
            startTimer()
        }*/
    }


    /*
   * Get location change and update
   */
    fun updateLocationChange() {

        val tripStatus = sessionManager.tripStatus
        /*String beginTrip = context.getResources().getString(R.string.begin_trip);
        String confirmTrip = context.getResources().getString(R.string.confirm_arrived);*/
        val beginTrip = CommonKeys.TripDriverStatus.BeginTrip
        val confirmTrip = CommonKeys.TripDriverStatus.ConfirmArrived

        /*
         * User offline release wake lock
         */
        if (sessionManager.driverStatus == "Offline") {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                releaseWakeLock()
            }
        } else {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                acquireWakeLock()
            }
        }

        twoDForm = DecimalFormat("#.######")

        /*
         * Send location to broadcast
         */

        if (tripStatus != null && tripStatus == beginTrip) {
            sessionManager.driverStatus = CommonKeys.DriverStatus.Online
        }

        /*
         *  Update location
         */

        if (lastLocation == null)
            lastLocation = currentLocation

        //CommonMethods.DebuggableLogE("locationupdate", "lastLocation:" +  + lastLocation.getLatitude() + " , " + lastLocation.getLongitude());
        //CommonMethods.DebuggableLogE("locationupdate", "currentLocation:" +  + currentLocation.getLatitude() + " , " + currentLocation.getLongitude());

        sessionManager.lastLatitude = currentLocation!!.latitude.toString()
        sessionManager.lastLongitude = currentLocation!!.longitude.toString()
        if (sessionManager.offlineDistance > 0
                || lastLocation!!.latitude != currentLocation!!.latitude
                || lastLocation!!.longitude != currentLocation!!.longitude) {
            if (tripStatus != null && tripStatus == beginTrip) {


                distance = 0f

                val distanceInMeters = lastLocation!!.distanceTo(currentLocation)

                distance = (distanceInMeters / 1000.0).toFloat()


                try {
                    distance = java.lang.Float.valueOf(twoDForm.format(distance.toDouble()).replace(",".toRegex(), "."))
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                CommonMethods.DebuggableLogE("locationupdate", "Distance:" + distance + "Max Distance" + MAX_DISTANCE)
                if (distance * 1000 < MAX_DISTANCE) {
                    val newLatLng = LatLng(java.lang.Double.valueOf(sessionManager.latitude!!), java.lang.Double.valueOf(sessionManager.longitude!!))
                    //latLngList.add(newLatLng);

                    println("latlng : "+sessionManager.latitude!!+","+ sessionManager.longitude!!)


                    SqliteDB.AddUserLocation(UserLocationModel(java.lang.Double.valueOf(sessionManager.latitude!!), java.lang.Double.valueOf(sessionManager.longitude!!), ""))



                    isInternetAvailable = commonMethods.isOnline(this)
                    if (isInternetAvailable) {
                        val offlineDistance = sessionManager.offlineDistance

                        // distance = distance + offlineDistance;
                        sessionManager.onlineDistance = sessionManager.onlineDistance + distance
                        CommonMethods.DebuggableLogE("locationupdate", "Distance:$distance")

                        val locationHashMap = HashMap<String, String>()
                        locationHashMap["latitude"] = sessionManager.latitude!!
                        locationHashMap["longitude"] = sessionManager.longitude!!
                        locationHashMap["total_km"] = String.format("%.6f", distance)
                        locationHashMap["user_type"] = sessionManager.type!!
                        locationHashMap["car_id"] = sessionManager.vehicle_id!!
                        locationHashMap["trip_id"] = sessionManager.tripId!!
                        locationHashMap["status"] = "Trip"
                        locationHashMap["token"] = sessionManager.accessToken!!

                        updateLocation(locationHashMap)
                    } else {
                        CommonMethods.DebuggableLogE("locationupdate", "Offline Distance:" + (sessionManager.offlineDistance + distance))
                        sessionManager.offlineDistance = sessionManager.offlineDistance + distance

                        val now = Time()
                        now.setToNow()
                        val time = now.hour.toString() + ":" + now.minute + ":" + now.second
                        val message = (sessionManager.offlineDistance.toString() + " " + tripStatus + " " + sessionManager.onlineDistance + " "
                                + sessionManager.latitude + "," + sessionManager.longitude + " " + time)


                    }

                }

                //startForegroundService()

            } else if (tripStatus != null && tripStatus == confirmTrip) {

                val locationHashMap = HashMap<String, String>()
                locationHashMap["latitude"] = sessionManager.latitude!!
                locationHashMap["longitude"] = sessionManager.longitude!!
                locationHashMap["total_km"] = "0"
                locationHashMap["user_type"] = sessionManager.type!!
                locationHashMap["car_id"] = sessionManager.vehicle_id!!
                locationHashMap["trip_id"] = sessionManager.tripId!!
                locationHashMap["status"] = sessionManager.driverStatus!!
                locationHashMap["token"] = sessionManager.accessToken!!
                updateLocation(locationHashMap)
            } else {
                duration = 30
                if (sessionManager.driverStatus != null && sessionManager.driverStatus == resources.getString(R.string.online)) {
                    val locationHashMap = HashMap<String, String>()
                    locationHashMap["latitude"] = sessionManager.latitude!!
                    locationHashMap["longitude"] = sessionManager.longitude!!
                    locationHashMap["user_type"] = sessionManager.type!!
                    locationHashMap["car_id"] = sessionManager.vehicle_id!!
                    locationHashMap["status"] = sessionManager.driverStatus!!
                    locationHashMap["token"] = sessionManager.accessToken!!
                    updateLocation(locationHashMap)
                }
            }
        } else {
            val now = Time()
            now.setToNow()
            val time = now.hour.toString() + ":" + now.minute + ":" + now.second
            val message = "same Location $time"


        }


        val j = Intent("location_update")
        j.putExtra("type", "Updates")
        j.putExtra("Lat", java.lang.Double.valueOf(sessionManager.latitude!!))
        j.putExtra("Lng", java.lang.Double.valueOf(sessionManager.longitude!!))
        j.putExtra("km", distance.toString())
        j.putExtra("status", sessionManager.tripStatus)
        sendBroadcast(j)

        /* if (sessionManager.driverStatus != "Offline") {
             startTimer()
         } else {
             WorkerUtils.isWorkManagerRunning = false

         }*/


    }


    /**
     * Update driver current location
     */
    fun updateLocation(locationHashMap: HashMap<String, String>) {
        println("GPS UPDATE CALLING")
        isInternetAvailable = commonMethods.isOnline(this)
        if (isInternetAvailable) {

            val now = Time()
            now.setToNow()
            val time = now.hour.toString() + ":" + now.minute + ":" + now.second
            val message = sessionManager.offlineDistance.toString() + " " + sessionManager.tripStatus + " " + sessionManager.onlineDistance + " " + sessionManager.latitude + "," + sessionManager.longitude + " " + time


            apiService.updateLocation(locationHashMap).enqueue(RequestCallback(REQ_UPDATE_ONLINE, this))
        } else {

            val message = "No Internet"


            CommonMethods.DebuggableLogE("Location ", "Internet unavailable")


        }
    }


    /*
  *  Get direction for given locations
  */
    private fun getDirectionsUrl(origin: LatLng, dest: LatLng): String {

        // Origin of route
        val str_origin = "origin=" + origin.latitude + "," + origin.longitude

        // Destination of route
        val str_dest = "destination=" + dest.latitude + "," + dest.longitude

        // Sensor enabled
        val sensor = "sensor=false"
        val mode = "mode=driving"
        // Building the parameters to the web service
        val parameters = "$str_origin&$str_dest&$sensor&$mode"

        // Output format
        val output = "json"

        // Building the url to the web service


        println("Static Map Url : " + "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + sessionManager.googleMapKey)
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + sessionManager.googleMapKey
    }


    private fun calcMinDiff(lastTime: String, lastTime1: String): Long {

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        val date1: Date = simpleDateFormat.parse(lastTime1)
        val date2: Date = simpleDateFormat.parse(lastTime)



        println("last time : " + lastTime)
        println("last time two : " + lastTime1)


        val difference = date2.time - date1.time
        val diffInSec = TimeUnit.MILLISECONDS.toSeconds(difference)

        return diffInSec

    }


    private fun initWidget() {
        floatWidget = LayoutInflater.from(this).inflate(R.layout.floating_widget, null)


        // register FCM registration complete receiver
        mRegistrationBroadcastReceiver?.let {
            LocalBroadcastManager.getInstance(this).registerReceiver(it,
                    IntentFilter(Config.DISTANCE_CALCULATION))
        }

        params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)


        //Specify the view position
        params.gravity = Gravity.TOP or Gravity.LEFT        //Initially view will be added to top-left corner
        params.x = 0
        params.y = 100




        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        addView()


        var X_Axis: Int = 0
        var Y_Axis: Int = 0

        var TouchX: Float = 0F
        var TouchY: Float = 0F

        var startClickTime: Long = 0L


        floatWidget?.setOnClickListener {

        }


        floatWidget?.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        X_Axis = params.x
                        Y_Axis = params.y
                        TouchX = event.rawX
                        TouchY = event.rawY
                        startClickTime = Calendar.getInstance().timeInMillis
                        return true
                    }
                    MotionEvent.ACTION_UP -> {

                        val clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;

                        if (clickDuration < MAX_CLICK_DURATION && !CallProcessingActivity.isOnCall) {

                            val requestaccept = Intent(applicationContext, MainActivity::class.java)
                            requestaccept.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(requestaccept)


                        }

                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        var Xdiff = Math.round(event.rawX - TouchX)
                        var Ydiff = Math.round(event.rawY - TouchY)


                        //Calculate the X and Y coordinates of the view.
                        params.x = (X_Axis + Xdiff).toInt()
                        params.y = (Y_Axis + Ydiff).toInt()

                        //Update the layout with new X & Y coordinates
                        mWindowManager?.updateViewLayout(floatWidget, params)


                        return true
                    }
                }

                return v?.onTouchEvent(event) ?: true
            }
        })


    }

    private fun addView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this))
            mWindowManager?.addView(floatWidget, params)
    }


    private fun removeWiget() {
        try{
            mWindowManager?.removeView(floatWidget)

        }catch (e : Exception){

        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "com.seentechs.newtaxidriver"
        val channelName = "My Background Service"
        val chan = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(chan)

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.applogo)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
        startForeground(2, notification)
    }

    /*
     * Wake lock started
     */
    private fun acquireWakeLock() {
        try {
            wakeLock!!.acquire()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /*
     * Wake lock end
     */
    private fun releaseWakeLock() {
        try {
            wakeLock!!.release()
        } catch (e: Exception) {

        }

    }


    override fun onDestroy() {

        removeWiget()

       /* try{
            stopLocationUpdates()

        }catch (e : Exception){

        }*/

        if (mSearchedRequestReferenceListener != null) {
            queryRequest.removeEventListener(mSearchedRequestReferenceListener!!)
            mFirebaseDatabaseRequest.removeEventListener(mSearchedRequestReferenceListener!!)
            mSearchedRequestReferenceListener = null

        }

        mRegistrationBroadcastReceiver?.let {
            LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(it)
        }


        if (locationManager != null) {

            locationManager!!.removeUpdates(listener!!)
        }
        timer.cancel()

        val str = sessionManager.tripStatus
        //String str2 = getResources().getString(R.string.begin_trip);
        val str2 = CommonKeys.TripDriverStatus.BeginTrip
        if (str != null && str == str2) {
            latLngModel = LatLngModel()

            val rightNow = Calendar.getInstance()

            currentHourIn12Format = rightNow.get(Calendar.HOUR_OF_DAY)
            currentMinIn12Format = rightNow.get(Calendar.MINUTE)


            latLngModel!!.latLngList = latLngList
            latLngModel!!.hour = currentHourIn12Format
            latLngModel!!.min = currentMinIn12Format
            complexPreferences.putObject("latLngList", latLngModel)
            complexPreferences.commit()
            println("latLngList : " + latLngList.size)
            val broadcastIntent = Intent(this, GPSBackgroundServiceRestarterBroadcastReceiver::class.java)
            sendBroadcast(broadcastIntent)
        }
        println("latlng size one : " + latLngList.size)
        if (latLngList.size > 0) {
            oldlatitude = latLngList[latLngList.size - 1].latitude
            oldlongitude = latLngList[latLngList.size - 1].longitude

        }
        println("old latitude : $oldlatitude")
        println("old longitude : $oldlongitude")

        super.onDestroy()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        timer.scheduleAtFixedRate(timerTask, LOCATION_UPDATE_INTERVAL.toLong(), LOCATION_UPDATE_INTERVAL.toLong())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMyOwnForeground()
        }
        return Service.START_STICKY
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        if (jsonResp.isSuccess) {
            onSuccessloc()
        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            val j = Intent("location_update")
            j.putExtra("type", "Update")
            j.putExtra("Lat", java.lang.Double.valueOf(sessionManager.latitude.toString()))
            j.putExtra("Lng", java.lang.Double.valueOf(sessionManager.longitude.toString()))
            j.putExtra("km", distanceInKM.toString())
            j.putExtra("status", "Else")
            sendBroadcast(j)
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        val j = Intent("location_update")
        j.putExtra("type", "Update")
        j.putExtra("Lat", java.lang.Double.valueOf(sessionManager.latitude))
        j.putExtra("Lng", java.lang.Double.valueOf(sessionManager.longitude))
        j.putExtra("km", distanceInKM.toString())
        j.putExtra("status", "error")
        sendBroadcast(j)
    }

    fun getLocation(): HashMap<String, String> {
        val locationHashMap = HashMap<String, String>()
        locationHashMap["latitude"] = sessionManager.latitude!!
        locationHashMap["longitude"] = sessionManager.longitude!!
        locationHashMap["user_type"] = sessionManager.type!!
        locationHashMap["car_id"] = sessionManager.vehicle_id!!
        locationHashMap["status"] = sessionManager.driverStatus!!
        locationHashMap["token"] = sessionManager.accessToken!!
        return locationHashMap
    }



    fun onSuccessloc() {
        val i = Intent("location_update")
        i.putExtra("type", "Update")
        i.putExtra("Lat", java.lang.Double.valueOf(sessionManager.latitude).toString())
        i.putExtra("Lng", java.lang.Double.valueOf(sessionManager.longitude).toString())
        i.putExtra("km", distanceInKM.toString())
        sendBroadcast(i)

    }


    /**
     * Creating new user node under 'users'
     */
    private fun updateLocationFireBase(lat: Double, lng: Double) {
        // TODO

        val driverLocation = DriverLocation(lat.toString(), lng.toString())


        var poolIds: List<String> = sessionManager.poolIds!!.split(",").map { it.trim() }

        for(i in poolIds.indices){
            if(poolIds.get(i)!=null&&!poolIds.get(i).equals(""))
                mFirebaseDatabase!!.child(FirebaseDbKeys.LIVE_TRACKING_NODE).child(poolIds.get(i)).setValue(driverLocation)
        }


        val j = Intent("location_update")
        j.putExtra("type", "DataBase")
        j.putExtra("Lat", java.lang.Double.valueOf(lat))
        j.putExtra("Lng", java.lang.Double.valueOf(lng))
        //j.putExtra("km",String.valueOf(distanceInKM));
        //j.putExtra("status",sessionManager.getTripStatus());
        sendBroadcast(j)
        /*if(isCheck.equals("check")) {
            isCheck="checked";*/
        if (mSearchedLocationReferenceListener == null) {
            addLatLngChangeListener() // Get Driver Lat Lng

        } else {

        }
        /*
        }*/
    }

    /**
     * Driver Request listener
     */
    private fun addRequestReceiveListner() {

        // User data change listener




            if(::mFirebaseDatabaseRequest.isInitialized)
            {

                queryRequest =  mFirebaseDatabaseRequest.child(FirebaseDbKeys.TRIP_REQUEST).child(sessionManager.userId!!)
                mSearchedRequestReferenceListener = queryRequest.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {


                        println("Gps service : ")
                        try {
                            val json = JSONObject(dataSnapshot.getValue(String::class.java))
                            var requestId = ""


                            val jsonObject = JSONObject(json.toString())
                            if (jsonObject.getJSONObject("custom").has("ride_request")) {
                                requestId = jsonObject.getJSONObject("custom").getJSONObject("ride_request").getString("request_id")
                            }


                            if(!sessionManager.requestId.equals(requestId)){
                                sessionManager.requestId = requestId
                                handleDataMessage(json)
                                initSinchService()
                            }




                        } catch (e: Exception) {

                        }


                    }

                    override fun onCancelled(error: DatabaseError) {


                    }
                })
            }else
            {
                Toast.makeText(this,"Firabase not iniliazited", Toast.LENGTH_SHORT).show()
            }


    }





    /*
     *  Handle push notification message
     */

    private fun handleDataMessage(json: JSONObject) {
        println("push json: $json")


        val TripStatus = sessionManager.tripStatus
        val DriverStatus = sessionManager.driverStatus
        val UserId = sessionManager.accessToken
        try {

            /*
             *  Handle push notification and broadcast message to other activity
             */
            sessionManager.pushJson = json.toString()



            val isPool = json.getJSONObject("custom").getJSONObject("ride_request").getBoolean("is_pool")


                if (!NotificationUtils.isAppIsInBackground(applicationContext)) {
                //CommonMethods.DebuggableLogE(TAG, "IF: " + json.toString());
                // app is in foreground, broadcast the push message
                if (json.getJSONObject("custom").has("ride_request")) {
                    if (DriverStatus == "Online"
                            && (TripStatus == null || TripStatus == CommonKeys.TripDriverStatus.EndTrip || TripStatus == "" || isPool)
                            && UserId != null) {
                        //  Intent rider=new Intent(getApplicationContext(), Riderrating.class);

                        /*val pushNotification = Intent(Config.PUSH_NOTIFICATION)
                        pushNotification.putExtra("message", "message")
                        LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification)*/
                        //startActivity(rider);

                        val jsonObject: JSONObject
                        try {
                            jsonObject = JSONObject(json.toString())
                            if (jsonObject.getJSONObject("custom").has("ride_request")) {
                                count++
                                val requstreceivepage = Intent(applicationContext, RequestReceiveActivity::class.java)
                                //requstreceivepage.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                requstreceivepage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(requstreceivepage)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    }
                }



            } else {
                println( "ELSE: $json")

                // app is in background, show the notification in notification tray
                if (json.getJSONObject("custom").has("ride_request")) {


                    if (DriverStatus == "Online"
                            && (TripStatus == null || TripStatus == CommonKeys.TripDriverStatus.EndTrip || TripStatus == "" || isPool)
                            && UserId != null) {


                        sessionManager.isDriverAndRiderAbleToChat = false
                        CommonMethods.stopFirebaseChatListenerService(applicationContext)
                        val intent = Intent(this, RequestReceiveActivity::class.java)
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)

                        val title = json.getJSONObject("custom").getJSONObject("ride_request").getString("title")

                        val notificationUtils = NotificationUtils(applicationContext)
                        notificationUtils.playNotificationSound()
                        notificationUtils.generateNotification(applicationContext, "", title)

                    }

                }

            }
        } catch (e: JSONException) {
            println("Json Exception: " + e.message)
            e.printStackTrace()
        } catch (e: Exception) {
            println("Exception: " + e.message)
        }

    }


    private fun initSinchService() {
        if (!sessionManager.accessToken.isNullOrEmpty()) {
            startService(Intent(this, NewTaxiSinchService::class.java))
        }
    }

    private fun stopSinchService() {
        CommonMethods.stopSinchService(this)

    }





    fun manualBookingTripBookedInfo(manualBookedPopupType: Int, jsonObject: JSONObject) {
        var riderName = ""
        var riderContactNumber = ""
        var riderPickupLocation = ""
        var riderPickupDateAndTime = ""
        try {
            riderName = jsonObject.getString("rider_first_name") + " " + jsonObject.getString("rider_last_name")
            riderContactNumber = jsonObject.getString("rider_country_code") + " " + jsonObject.getString("rider_mobile_number")
            riderPickupLocation = jsonObject.getString("pickup_location")
            riderPickupDateAndTime = jsonObject.getString("date") + " - " + jsonObject.getString("time")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val dialogs = Intent(applicationContext, ManualBookingDialog::class.java)
        dialogs.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        dialogs.putExtra(CommonKeys.KEY_MANUAL_BOOKED_RIDER_NAME, riderName)
        dialogs.putExtra(CommonKeys.KEY_MANUAL_BOOKED_RIDER_CONTACT_NUMBER, riderContactNumber)
        dialogs.putExtra(CommonKeys.KEY_MANUAL_BOOKED_RIDER_PICKU_LOCATION, riderPickupLocation)
        dialogs.putExtra(CommonKeys.KEY_MANUAL_BOOKED_RIDER_PICKU_DATE_AND_TIME, riderPickupDateAndTime)
        dialogs.putExtra(CommonKeys.KEY_TYPE, manualBookedPopupType)
        startActivity(dialogs)

    }





    /**
     * Driver LOCATION_AND_WRITEPERMISSION_ARRAY change listener
     */
    private fun addLatLngChangeListener() {

        CommonMethods.DebuggableLogE(TAG, "Driver LOCATION_AND_WRITEPERMISSION_ARRAY data called")
        /*if(mSearchedLocationReferenceListener!=null)
            mFirebaseDatabase.removeEventListener(mSearchedLocationReferenceListener);*/
        // User data change listener
        val query = mFirebaseDatabase!!.child(FirebaseDbKeys.LIVE_TRACKING_NODE).child(sessionManager.tripId!!)

        mSearchedLocationReferenceListener = query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (sessionManager.tripId != null) {
                    val driverLocation = dataSnapshot.getValue(DriverLocation::class.java)

                    // Check for null
                    if (driverLocation == null) {
                        CommonMethods.DebuggableLogE(TAG, "Driver LOCATION_AND_WRITEPERMISSION_ARRAY data is null!")
                        return
                    }

                    CommonMethods.DebuggableLogE(TAG, "Driver LOCATION_AND_WRITEPERMISSION_ARRAY data is changed!" + driverLocation.lat + ", " + driverLocation.lng)
                } else {
                    query.removeEventListener(this)
                    mFirebaseDatabase!!.removeEventListener(this)
                    mFirebaseDatabase!!.onDisconnect()
                }

            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                CommonMethods.DebuggableLogE(TAG, "Failed to read user", error.toException())
            }
        })
    }


    /*
     * GPS location update
     */
    inner class MyTimerTask : TimerTask() {


        override fun run() {
            CommonMethods.DebuggableLogD("` ", "ss")
           /* locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult ?: return
                    for (location in locationResult.locations){
                        sessionManager.currentLatitude = location?.latitude.toString()
                        sessionManager.latitude = location?.latitude.toString()
                        sessionManager.currentLongitude = location?.longitude.toString()
                        sessionManager.longitude = location?.longitude.toString()
                        println("location call : "+location?.latitude.toString()+","+location?.longitude.toString())
                        //startMyOwnForeground()
                    }
                }
            }

            startLocationUpdates()


*/
           // removeOfflineDriveronGeoFire()
            locationupdateCall()

        }
    }

    private fun startLocationUpdates() {
        val request = LocationRequest()
        request.setInterval(LOCATION_INTERVAL.toLong());
        request.setFastestInterval(LOCATION_INTERVAL.toLong());
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationClient.requestLocationUpdates(request,
                locationCallback,
                Looper.getMainLooper())
    }



    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
    private fun removeOfflineDriveronGeoFire() {
        val  ref=FirebaseDatabase.getInstance().reference.child(FirebaseDbKeys.GEOFIRE)
        val  geoFire= GeoFire(ref)
        geoFire.removeLocation( sessionManager.userId)
    }

    companion object {

        // LOCATION_AND_WRITEPERMISSION_ARRAY time interval
        val LOCATION_INTERVAL = 1000 * 1 * 1 // 1000 * 60 * 1 for 1 minute 1000 * 10 * 1 for 10 seconds
        val LOCATION_INTERVAL_CHECK = 1000 * 1 * 1
        val LOCATION_UPDATE_INTERVAL = 1000 * 10 * 1 // 1000 * 60 * 1 for 1 minute 1000 * 10 * 1 for 10 seconds
        val MAX_CLICK_DURATION = 200;
            public val BROADCAST_ACTION = "resumeTrip";

        private val LOCATION_DISTANCE = 10f // 30 meters
        private val maxDistance = 0.4.toFloat()
        private val TAG = "GPS Service"
        var latLngList: MutableList<LatLng> = ArrayList()
    }

}
