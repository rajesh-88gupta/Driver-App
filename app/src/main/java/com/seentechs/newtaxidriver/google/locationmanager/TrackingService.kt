package com.seentechs.newtaxidriver.google.locationmanager

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationListener
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.firebase.database.*
import com.google.gson.Gson
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.helper.Constants.notificationIcon
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonKeys
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.home.MainActivity
import com.seentechs.newtaxidriver.home.datamodel.firebase_keys.FirebaseDbKeys
import com.seentechs.newtaxidriver.home.pushnotification.NotificationUtils
import com.seentechs.newtaxidriver.trips.RequestReceiveActivity
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

class TrackingService : Service() {
    private var wakeLock: PowerManager.WakeLock? = null
    private var trackingController: TrackingController? = null

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var customDialog: CustomDialog

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var sessionManager: SessionManager

    private var mSearchedRequestReferenceListener: ValueEventListener? = null
    private lateinit var mFirebaseDatabaseRequest: DatabaseReference
    lateinit var queryRequest: Query
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var locationCallback: LocationCallback
    private var listener: LocationListener? = null

    class HideNotificationService : Service() {
        override fun onBind(intent: Intent): IBinder? {
            return null
        }

        override fun onCreate() {
            startForeground(NOTIFICATION_ID, createNotification(this))
            stopForeground(true)
        }

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            stopSelfResult(startId)
            return START_NOT_STICKY
        }
    }

    @SuppressLint("WakelockTimeout")
    override fun onCreate() {
        Log.i(TAG, "service create")
        //StatusActivity.addMessage(getString(R.string.status_service_create));
        AppController.getAppComponent().inject(this)

        // Open request Activity while receive from Firebase DB
        val mFirebaseInstance = FirebaseDatabase.getInstance()

        mFirebaseDatabaseRequest = mFirebaseInstance.getReference(applicationContext.getString(com.seentechs.newtaxidriver.R.string.real_time_db))
        //addRequestReceiveListener()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMyOwnForeground()
        } else {
            startForeground(NOTIFICATION_ID, createNotification(this))
        }
        //startForeground(NOTIFICATION_ID, createNotification(this))

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, javaClass.name)
            wakeLock?.acquire()

            trackingController = TrackingController(this)
            trackingController!!.start()
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, Intent(this, HideNotificationService::class.java))
        }
        //locationService()
        //onLocationChange()
        //fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        //startLocationUpdates()

    }


    /*private fun startLocationUpdates() {
        val request = LocationRequest()
        request.setInterval(GpsService.LOCATION_INTERVAL.toLong());
        request.setFastestInterval(GpsService.LOCATION_INTERVAL.toLong());
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationClient.requestLocationUpdates(request,
                locationCallback,
                Looper.getMainLooper())
    }*/

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // AutoStartReceiver.completeWakefulIntent(intent)
        return START_STICKY
    }

    override fun onDestroy() {
        Log.i(TAG, "service destroy")
        Log.e("onStop Update", "onDestroy service")
        //StatusActivity.addMessage(getString(R.string.status_service_destroy));
        stopForeground(true)
        if (wakeLock != null && wakeLock!!.isHeld) {
            wakeLock!!.release()
        }

        if (mSearchedRequestReferenceListener != null) {
            queryRequest.removeEventListener(mSearchedRequestReferenceListener!!)
            mFirebaseDatabaseRequest.removeEventListener(mSearchedRequestReferenceListener!!)
            mSearchedRequestReferenceListener = null

        }

        if (trackingController != null) {
            trackingController!!.stop()
        }
    }

    /**
     * Driver Request listener
     */
    private fun addRequestReceiveListener() {
        if (::mFirebaseDatabaseRequest.isInitialized) {

            queryRequest = mFirebaseDatabaseRequest.child(FirebaseDbKeys.TRIP_REQUEST).child(sessionManager.userId!!)
            mSearchedRequestReferenceListener = queryRequest.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    try {
                        val json = JSONObject(dataSnapshot.getValue(String::class.java))
                        var requestId = ""
                        val jsonObject = JSONObject(json.toString())
                        if (jsonObject.getJSONObject("custom").has("ride_request")) {
                            requestId = jsonObject.getJSONObject("custom").getJSONObject("ride_request").getString("request_id")
                        }
                        if (!sessionManager.requestId.equals(requestId)) {
                            sessionManager.requestId = requestId
                            handleDataMessage(json)
                            initSinchService()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        } else {
            Toast.makeText(this, "Firabase not iniliazited", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     *  Handle push notification message
     */

    private fun handleDataMessage(json: JSONObject) {

        val tripStatus = sessionManager.tripStatus
        val driverStatus = sessionManager.driverStatus
        val userId = sessionManager.accessToken
        try {

            /*
             *  Handle push notification and broadcast message to other activity
             */
            sessionManager.pushJson = json.toString()


            val isPool = json.getJSONObject("custom").getJSONObject("ride_request").getBoolean("is_pool")


            if (!NotificationUtils.isAppIsInBackground(applicationContext)) {

                if (json.getJSONObject("custom").has("ride_request")) {
                    if (driverStatus == "Online"
                            && (tripStatus == null || tripStatus == CommonKeys.TripDriverStatus.EndTrip || tripStatus == "" || isPool)
                            && userId != null) {
                        val jsonObject: JSONObject
                        try {
                            jsonObject = JSONObject(json.toString())
                            if (jsonObject.getJSONObject("custom").has("ride_request")) {
                                val requestReceiveIntent = Intent(applicationContext, RequestReceiveActivity::class.java)
                                requestReceiveIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(requestReceiveIntent)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    }
                }


            } else {
                println("ELSE: $json")

                // app is in background, show the notification in notification tray
                if (json.getJSONObject("custom").has("ride_request")) {


                    if (driverStatus == "Online"
                            && (tripStatus == null || tripStatus == CommonKeys.TripDriverStatus.EndTrip || tripStatus == "" || isPool)
                            && userId != null) {
                        sessionManager.isDriverAndRiderAbleToChat = false
                        CommonMethods.stopFirebaseChatListenerService(applicationContext)

                        val requestReceiveIntent = Intent(applicationContext, RequestReceiveActivity::class.java)
                        requestReceiveIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(requestReceiveIntent)

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
            /* startService(Intent(this, NewTaxiSinchService::class.java))*/
        }
    }

    companion object {
        private val TAG = TrackingService::class.java.simpleName
        private const val NOTIFICATION_ID = 1
        private fun createNotification(context: Context): Notification {
            val builder = NotificationCompat.Builder(context, "default")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
            //if (!BuildConfig.HIDDEN_APP) {
            val intent = Intent(context, MainActivity::class.java)
            //val intent = Intent(context, LocationListenerCheck::class.java)
            builder
                    .setContentTitle(context.getString(R.string.settings_status_on_summary))
                    .setTicker(context.getString(R.string.settings_status_on_summary))
                    .color = ContextCompat.getColor(context, R.color.color_primary)
            /*} else {intent = Intent(Settings.ACTION_SETTINGS)
                }*/
            if (Build.VERSION.SDK_INT >= 31){
                builder.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE))
            }else{
                builder.setContentIntent(PendingIntent.getActivity(context, 0, intent, 0))
            }
            return builder.build()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "Driver"
        val channelName = "Driver Background Service"
        val chan = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH)
        chan.lightColor = Color.YELLOW
        chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(chan)

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(notificationIcon)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setColor(ContextCompat.getColor(this,R.color.newtaxi_app_black))
                .build()
        startForeground(2, notification)
    }
}