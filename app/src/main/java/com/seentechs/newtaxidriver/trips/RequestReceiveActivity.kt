package com.seentechs.newtaxidriver.trips

/**
 * @package com.seentechs.newtaxidriver.home
 * @subpackage home
 * @category RequestReceiveActivity
 * @author Seen Technologies
 *
 */

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.text.TextUtils
import android.view.View
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.database.AddFirebaseDatabase
import com.seentechs.newtaxidriver.common.helper.CircularMusicProgressBar
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.common.helper.WaveDrawable
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonKeys
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.RequestCallback
import com.seentechs.newtaxidriver.common.views.CommonActivity
import com.seentechs.newtaxidriver.home.MainActivity
import com.seentechs.newtaxidriver.home.datamodel.TripDetailsModel
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import com.seentechs.newtaxidriver.home.pushnotification.NotificationUtils
import com.seentechs.newtaxidriver.trips.timelineview.LocationTimeLineView
import io.github.krtkush.lineartimer.LinearTimer
import io.github.krtkush.lineartimer.LinearTimerView
import kotlinx.android.synthetic.main.app_activity_request_receive.*
import org.json.JSONException
import org.json.JSONObject
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

/* ************************************************************
                      RequestReceiveActivity
Its used to get RequestReceiveActivity for rider with details
*************************************************************** */
class RequestReceiveActivity : CommonActivity(), LinearTimer.TimerListener, ServiceListener, OnMapReadyCallback {


    private var timingCompleted: Boolean = false

    lateinit @Inject
    var apiService: ApiService

    lateinit @Inject
    var sessionManager: SessionManager

    lateinit @Inject
    var commonMethods: CommonMethods

    lateinit @Inject
    var gson: Gson

    lateinit @Inject
    var customDialog: CustomDialog

    lateinit var mMap: GoogleMap

    lateinit var mapFragment: SupportMapFragment

    @BindView(R.id.maplayout)
    lateinit var rl: RoundedLayout


    @BindView(R.id.tv_seat_count)
    lateinit var tvSeatCount: TextView

    lateinit var dialog: AlertDialog

    lateinit @BindView(R.id.request_receive_dialog_layout)
    var rltAnim: RelativeLayout

    lateinit @BindView(R.id.linearTimer)
    var linearTimerView: LinearTimerView

    lateinit @BindView(R.id.map_snap)
    var map_snap: ImageView

    lateinit @BindView(R.id.req_min)
    var req_min: TextView


    lateinit @BindView(R.id.tv_decline)
    var tvDecline: TextView

    lateinit var waveDrawable: WaveDrawable
    lateinit var mPlayer: MediaPlayer
    lateinit var pd: ProgressDialog
    lateinit var progressBar: CircularMusicProgressBar
    var count = 1

    lateinit var lat: String
    lateinit var log: String

    var JSON_DATA: String = ""
    lateinit var min: String
    lateinit var endtime: String
    lateinit var req_id: String
    lateinit var pickup_address: String
    lateinit var seat_count: String
    var is_pool: Boolean = false
    var dropAddress: String = ""
    lateinit var staticMapURL: String
    protected var isInternetAvailable: Boolean = false
    private var linearTimer: LinearTimer? = null
    private var duration = 0L

    private val addFirebaseDatabase = AddFirebaseDatabase()

    lateinit internal var tripDetailsModel: TripDetailsModel

    @OnClick(R.id.map_snap)
    fun imageclick() {
        if (!timingCompleted)
            accept()
        else
            finish();
    }


    @OnClick(R.id.tv_decline)
    fun decline() {
        try {
            linearTimer!!.pauseTimer()
            waveDrawable.stopAnimation()
            mPlayer.release()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        moveToNext()
    }

    @BindView(R.id.tvcountdowntimer)
    lateinit var tvcountdowntimer: TextView


    lateinit var durationFormated: String

    var timer by Delegates.notNull<Long>()

    @SuppressLint("InvalidWakeLockTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_activity_request_receive)
        val window: Window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        rl = findViewById<RoundedLayout>(R.id.maplayout)
        dialog = commonMethods.getAlertDialog(this)
        val pm = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        /*val wakeLock = pm.newWakeLock(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON  or PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG")
        wakeLock.acquire(10*60*1000L *//*10 minutes*//*)*/
        val win: Window = window
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        durationFormated = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration)
                        - TimeUnit.MINUTES
                        .toSeconds(TimeUnit.MILLISECONDS.toHours(duration)));
        tvcountdowntimer.text = durationFormated

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        /*       val keyguardManager = applicationContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
               val keyguardLock = keyguardManager.newKeyguardLock("TAG")
               keyguardLock.disableKeyguard()*/
        /*
                    *  Common loader and internet check
                    */
        isInternetAvailable = commonMethods.isOnline(this)

        CommonKeys.isRideRequest = false
        NotificationUtils.clearNotifications(this)


        circulatLayoutFile()

        req_min = findViewById<View>(R.id.req_min) as TextView

        /*
                    *  After rider send request driver can receive the request
                    */
        JSON_DATA = sessionManager.pushJson!!
        //JSON_DATA = "{custom={\"end_time\":1619213858,\"id\":1619213843,\"title\":\"Trip Request\",\"ride_request\":{\"fare_estimation\":\"0\",\"seat_count\":\"1\",\"pickup_longitude\":\"78.137664\",\"pickup_location\":\"Vishaal De Mall, No. 31, 5th Floor, Gokhale Rd, Chinna Chokikulam, Tamil Nadu 625002\",\"pickup_latitude\":\"9.924418\",\"is_pool\":false,\"title\":\"Trip Request\",\"drop_location\":\"3/424, Ring Road Junction, Sivagangai Main Rd, near Pandi Kovil, Madurai, Tamil Nadu 625020\",\"request_id\":515,\"min_time\":1}}}"


        /*
                    *   Get Rider details and request details
                    */
        try {

            val jsonObject = JSONObject(JSON_DATA)
            if (jsonObject.getJSONObject("custom").has("ride_request")) {
                min = jsonObject.getJSONObject("custom").getJSONObject("ride_request").getString("min_time")
                req_id = jsonObject.getJSONObject("custom").getJSONObject("ride_request").getString("request_id")
                pickup_address = jsonObject.getJSONObject("custom").getJSONObject("ride_request").getString("pickup_location")
                seat_count = jsonObject.getJSONObject("custom").getJSONObject("ride_request").getString("seat_count")
                is_pool = jsonObject.getJSONObject("custom").getJSONObject("ride_request").getBoolean("is_pool")
                endtime = jsonObject.getJSONObject("custom").getString("end_time")
                duration = commonMethods.difference(commonMethods.getCurrentTimeIntoLong(),endtime.toLong())
                //                    dropAddress = jsonObject.getJSONObject("custom").getJSONObject("ride_request").getString("drop_location");
                if (is_pool) {
                    tvDecline.visibility = View.VISIBLE
                    if (seat_count.equals("2"))
                        tvSeatCount.setText(getString(R.string.pool_request) + seat_count + getString(R.string.persons_braces))
                    else
                        tvSeatCount.text = getString(R.string.pool_request) + seat_count + getString(R.string.person_braces)

                    tvSeatCount.visibility = View.VISIBLE
                } else {
                    tvDecline.visibility = View.VISIBLE
                    tvSeatCount.visibility = View.GONE
                }

                if (Integer.parseInt(min) > 1) {
                    req_min.text = min + " " + resources.getString(R.string.minutes)
                } else {
                    req_min.text = min + " " + resources.getString(R.string.minute)
                }
            }

            val addressArray = ArrayList<String>()
            addressArray.add(pickup_address)
            addressArray.add(jsonObject.getJSONObject("custom").getJSONObject("ride_request").getString("drop_location"))
            val locationTimeLineView = LocationTimeLineView(this, addressArray)
            rv_location.adapter = locationTimeLineView

            progressBar = findViewById<View>(R.id.album_art) as CircularMusicProgressBar

            lat = jsonObject.getJSONObject("custom").getJSONObject("ride_request").getString("pickup_latitude")
            log = jsonObject.getJSONObject("custom").getJSONObject("ride_request").getString("pickup_longitude")
            val pickupstr = "$lat,$log"
            val positionOnMap = "&markers=size:mid|icon:" + getString(R.string.imageUrl) + "man_marker.png|" + pickupstr

            if (commonMethods.checkTimings(commonMethods.getCurrentTime(), commonMethods.getTimeFromLong(endtime.toLong()))) {
                circularProgressfunction()
                progressBar.setValue(100f)
            } else {
                expireAlert()
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }



    private fun expireAlert() {
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setMessage(resources.getString(R.string.request_expire))
        alertDialog.setCancelable(false)
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, resources.getString(R.string.ok)
        ) { dialog, which ->
            dialog.dismiss()
            moveToNext()
        }
        alertDialog.show()
    }

    /*
    *  Request page circular animation function
    */
    private fun circularProgressfunction() {
        val display = windowManager.defaultDisplay
        val width = display.width
        val height = display.height
        var radius: Int
        if (width < height)
            radius = (width / 3.5).toInt()
        else
            radius = (height / 3.5).toInt()
        print("radius: $radius")
        //radius-= 4;
        radius = (radius / resources.displayMetrics.density).toInt()
        print("Height: $height Width: $width")

        waveDrawable = WaveDrawable(resources.getColor(R.color.newtaxi_app_navy), width)


        val vto = linearTimerView.viewTreeObserver
        vto.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                linearTimerView.viewTreeObserver.removeOnPreDrawListener(this)
                val finalHeight1 = linearTimerView.measuredHeight
                val finalWidth1 = linearTimerView.measuredWidth
                print("Height: $finalHeight1 Width: $finalWidth1")
                /*rl.layoutParams.height = (finalHeight1 / 1.5).toInt()
                rl.layoutParams.width = (finalWidth1 / 1.8).toInt()*/
                rl.layoutParams.height = (finalHeight1 + 40).toInt()
                rl.layoutParams.width = (finalWidth1 + 40).toInt()
                rl.requestLayout()
                return true
            }
        })
        print("radius: $radius")
        linearTimerView.circleRadiusInDp = radius
        count = 1

        linearTimer = LinearTimer.Builder()
                .linearTimerView(linearTimerView)
                .duration(duration)
                .timerListener(this)
                .progressDirection(LinearTimer.COUNTER_CLOCK_WISE_PROGRESSION)
                .preFillAngle(0f)
                .endingAngle(360)
                .getCountUpdate(LinearTimer.COUNT_UP_TIMER, 1000)
                .build()

        rltAnim.background = (waveDrawable)
        val interpolator = LinearInterpolator()

        waveDrawable.setWaveInterpolator(interpolator)
        waveDrawable.startAnimation()

        try {
            linearTimerView.clearAnimation()
            linearTimerView.animate()
            linearTimerView.animation = null
            linearTimer!!.startTimer()
            linearTimerView.visibility = View.VISIBLE
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }


        Handler().postDelayed({
            try {
                timingCompleted = true
                println("Handler called ")
                linearTimer!!.pauseTimer()
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, duration)
    }

    /*
    *  After given time automatically to stop animation
    */
    @SuppressLint("InvalidWakeLockTag")
    override fun animationComplete() {

        val pm = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = pm.newWakeLock(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG")
        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)

        val keyguardManager = applicationContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val keyguardLock = keyguardManager.newKeyguardLock("TAG")
        keyguardLock.disableKeyguard()

        waveDrawable.stopAnimation()
        //  finish();
        if (count == 1) {
            count++

            sessionManager.pushJson = ""

            val requestaccept = Intent(applicationContext, MainActivity::class.java)
            startActivity(requestaccept)
            finish()

        }
    }

    private fun circulatLayoutFile() {
        rl.setShapeCircle(false)
        rl.showBorder(false)
        rl.setBorderWidth(0)
    }

    /*
    *   Animation time
    */
    override fun timerTick(tickUpdateInMillis: Long) {
        CommonMethods.DebuggableLogI("Time left", tickUpdateInMillis.toString())

        /*String formattedTime = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(tickUpdateInMillis),
                TimeUnit.MILLISECONDS.toSeconds(tickUpdateInMillis)
                        - TimeUnit.MINUTES
                        .toSeconds(TimeUnit.MILLISECONDS.toHours(tickUpdateInMillis)));*/
        timer = duration - tickUpdateInMillis
        val formattedTime = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timer),
                TimeUnit.MILLISECONDS.toSeconds(timer)
                        - TimeUnit.MINUTES
                        .toSeconds(TimeUnit.MILLISECONDS.toMinutes(timer)))
        CommonMethods.DebuggableLogI("timer", formattedTime)
        tvcountdowntimer.text = formattedTime
        mPlayer = MediaPlayer.create(this, R.raw.newtaxi)
        mPlayer.start()
        mPlayer.setOnCompletionListener { mp -> mp.release() }
    }

    override fun onTimerReset() {

    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        if (jsonResp.isSuccess) {
            onSuccessAccept(jsonResp)
        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            pd.dismiss()
            val requestaccept = Intent(applicationContext, MainActivity::class.java)
            startActivity(requestaccept)
            finish()
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            pd.dismiss()
        }
    }

    override fun onBackPressed() {
        //This Method is Empty Because we have to strict the backpress When Request Receive
    }

    /*
    *   Get request rider details after accept
    */
    private fun acceptDriver() {
        apiService.acceptRequest(sessionManager.type!!, req_id, "Trip", sessionManager.accessToken!!, TimeZone.getDefault().id).enqueue(RequestCallback(this))
    }

    fun onSuccessAccept(jsonResp: JsonResponse) {

        pd.dismiss()
        tripDetailsModel = gson.fromJson(jsonResp.strResponse, TripDetailsModel::class.java)

        addFirebaseDatabase.updateRequestTable(tripDetailsModel.riderDetails.get(0).riderId!!, tripDetailsModel.riderDetails.get(0).tripId.toString(), this)

        sessionManager.riderName = tripDetailsModel.riderDetails.get(0).name
        sessionManager.riderId = tripDetailsModel.riderDetails.get(0).riderId!!
        sessionManager.riderRating = tripDetailsModel.riderDetails.get(0).rating
        sessionManager.riderProfilePic = tripDetailsModel.riderDetails.get(0).profileImage
        sessionManager.bookingType = tripDetailsModel.riderDetails.get(0).bookingType
        sessionManager.tripId = tripDetailsModel.riderDetails.get(0).tripId!!.toString()
        sessionManager.subTripStatus = resources.getString(R.string.confirm_arrived)
        //sessionManager.setTripStatus("CONFIRM YOU'VE ARRIVED");
        sessionManager.tripStatus = CommonKeys.TripDriverStatus.ConfirmArrived
        // sessionManager.paymentMethod = tripDetailsModel.paymentMode

        sessionManager.isTrip = true
        /*  sessionManager.isPool=tripDetailsModel.isPool
          if(!sessionManager.isPool)
          {
              AddFirebaseDatabase().removeDriverFromGeofire(this)
          }*/
        sessionManager.isDriverAndRiderAbleToChat = true
        CommonMethods.startFirebaseChatListenerService(this)

        val requestaccept = Intent(applicationContext, RequestAcceptActivity::class.java)
        requestaccept.putExtra("riderDetails", tripDetailsModel)
        requestaccept.putExtra("tripstatus", resources.getString(R.string.confirm_arrived))
        requestaccept.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(requestaccept)
        finish()
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        val dropmarker: MarkerOptions
        val pickupMarker: MarkerOptions

        val markerUrl = URL(getString(R.string.imageUrl) + "man_marker.png")

        // val bmp = BitmapFactory.decodeStream(markerUrl.openConnection().getInputStream())


//        locationHashMap["pickup_longitude"] = pickup?.longitude.toString()
        val pickupLatLng = LatLng(lat.toDouble(), log.toDouble())

        pickupMarker = MarkerOptions()
        pickupMarker.position(pickupLatLng as LatLng)
        pickupMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.man_marker))


        mMap.addMarker(pickupMarker)


        val builder = LatLngBounds.Builder()

        //the include method will calculate the min and max bound.
        builder.include(pickupMarker.position)

        val bounds = builder.build()

        //val cameraPosition = CameraPosition.Builder().zoom(16.5f).tilt(0f).build()


        val width = resources.displayMetrics.widthPixels / 2
        val height = resources.displayMetrics.heightPixels / 2
        val padding = (width * 0.1).toInt() // offset from edges of the map 10% of screen

        val cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)

        mMap.setMaxZoomPreference(15f)
        mMap.moveCamera(cu)
        // mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        mMap.uiSettings.isScrollGesturesEnabled = false;

        mMap.uiSettings.setAllGesturesEnabled(false);


    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            if (linearTimer != null) {
                linearTimer!!.pauseTimer()
            }
            //linearTimer.resetTimer();
        } catch (e: Exception) {
            e.printStackTrace()
            // Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        try {
            if (mPlayer != null) {
                mPlayer.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    fun accept() {
        try {
            linearTimer!!.pauseTimer()
            //linearTimer.resetTimer();
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            // Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }



        waveDrawable.stopAnimation()
        mPlayer.release()

        pd = ProgressDialog(this@RequestReceiveActivity)
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pd.setMessage(resources.getString(R.string.acceptingpickup))
        pd.setCancelable(false)
        val drawable: Drawable = ProgressBar(this).indeterminateDrawable.mutate()
        drawable.setColorFilter(ContextCompat.getColor(this, R.color.newtaxi_app_navy), PorterDuff.Mode.SRC_IN)
        pd.setIndeterminateDrawable(drawable)
        pd.show()
        /*
        *  Accept request API call
        */


        if (isInternetAvailable) {
            acceptDriver()
        } else {
            commonMethods.showMessage(this@RequestReceiveActivity, dialog, resources.getString(R.string.Interneterror))
        }
    }

    private fun moveToNext() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

}
