package com.seentechs.newtaxidriver.home.pushnotification

/**
 * @package com.seentechs.newtaxidriver.home.pushnotification
 * @subpackage pushnotification model
 * @category MyFirebaseMessagingService
 * @author Seen Technologies
 *
 */

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.PowerManager
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.sinch.android.rtc.SinchHelpers
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.helper.CommonDialog
import com.seentechs.newtaxidriver.common.helper.ManualBookingDialog
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonKeys
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.CommonMethods.Companion.DebuggableLogE
import com.seentechs.newtaxidriver.home.datamodel.TripDetailsModel
import com.seentechs.newtaxidriver.home.firebaseChat.ActivityChat
import com.seentechs.newtaxidriver.trips.RequestAcceptActivity
import com.seentechs.newtaxidriver.trips.RequestReceiveActivity
import com.seentechs.newtaxidriver.trips.voip.NewTaxiSinchService
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


/* ************************************************************
                MyFirebaseMessagingService
Its used to get the pushnotification FirebaseMessagingService function
*************************************************************** */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var sessionManager: SessionManager
    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var gson: Gson


    override fun onCreate() {
        super.onCreate()
        AppController.getAppComponent().inject(this)
        setLocale()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        DebuggableLogE(TAG, "From: " + remoteMessage.from)
        wakeUpScreen()

        if (SinchHelpers.isSinchPushPayload(remoteMessage.data)) {
            // it's Sinch message - relay it to SinchClient
            //AppController.createSinchClient(sessionManager.getUserId(),sessionManager.getSinchKey(), sessionManager.getSinchSecret());
            /*NotificationResult result = NewTaxiSinchService.sinchClient.relayRemotePushNotificationPayload(remoteMessage.getData());*/
            initSinchService()
        } else {

            // Check if message contains a data payload.
            if (remoteMessage.data.size > 0) {
                DebuggableLogE(TAG, "Data Payload: " + remoteMessage.data.toString())

                try {
                    val json = JSONObject(remoteMessage.data.toString())
                    commonMethods.handleDataMessage(json,this)
                    if (remoteMessage.notification != null) {
                        DebuggableLogE(TAG, "Notification Body: " + remoteMessage.notification?.body)
                    }

                } catch (e: Exception) {
                    DebuggableLogE(TAG, "Exception: " + e.message)
                }

            }
        }


    }

    @SuppressLint("InvalidWakeLockTag")
    private fun wakeUpScreen() {
        val pm = this.getSystemService(POWER_SERVICE) as PowerManager
        val isScreenOn = pm.isScreenOn
        Log.e("screen on......", "" + isScreenOn)
        if (!isScreenOn) {
            val wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE, "MyLock")
            wl.acquire(10000)
            val wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyCpuLock")
            wl_cpu.acquire(10000)
        }
    }


    /*
     *  Handle push notification message
     */

    private fun handleDataMessage(json: JSONObject , context: Context) {
        DebuggableLogE(TAG, "push json: $json")
        val TripStatus = sessionManager.tripStatus
        val DriverStatus = sessionManager.driverStatus
        val UserId = sessionManager.accessToken
        try {

            /*
             *  Handle push notification and broadcast message to other activity
             */
            sessionManager.pushJson = json.toString()

            if (!NotificationUtils.isAppIsInBackground(context)) {
                //CommonMethods.DebuggableLogE(TAG, "IF: " + json.toString());
                // app is in foreground, broadcast the push message


                try {
                    val json = JSONObject(json.toString())
                    var requestId = ""
                    val isPool = json.getJSONObject("custom").getJSONObject("ride_request").getBoolean("is_pool")


                    val jsonObject = JSONObject(json.toString())
                    if (jsonObject.getJSONObject("custom").has("ride_request")) {
                        requestId = jsonObject.getJSONObject("custom").getJSONObject("ride_request").getString("request_id")
                    }


                    if (!sessionManager.requestId.equals(requestId)) {
                        sessionManager.requestId = requestId


                        if (json.getJSONObject("custom").has("ride_request")) {
                            if (DriverStatus == "Online"
                                    && (TripStatus == null || TripStatus == CommonKeys.TripDriverStatus.EndTrip || TripStatus == "" || isPool)
                                    && UserId != null) {
                                //  Intent rider=new Intent(getApplicationContext(), Riderrating.class);


                                val requstreceivepage = Intent(context, RequestReceiveActivity::class.java)
                                requstreceivepage.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                requstreceivepage.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(requstreceivepage)

                                /* val pushNotification = Intent(Config.PUSH_NOTIFICATION)
                                 pushNotification.putExtra("message", "message")
                                 LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification)*/
                                //startActivity(rider);
                            }
                        }

                    }


                } catch (e: Exception) {

                }






                if (json.getJSONObject("custom").has("cancel_trip")) {

                    val tripriders=json.getJSONObject("custom").getJSONObject("cancel_trip").getJSONArray("trip_riders")
                    if(tripriders.length()>0){
                        sessionManager.isTrip=true
                    }else{
                        sessionManager.isTrip=false
                    }
                    sessionManager.clearTripID()
                    sessionManager.clearTripStatus()
                    sessionManager.isDriverAndRiderAbleToChat = false
                    CommonMethods.stopFirebaseChatListenerService(context)
                    stopSinchService()
                    val dialogs = Intent(context, CommonDialog::class.java)
                    println("Langugage " + resources.getString(R.string.yourtripcanceledrider))
                    sessionManager.dialogMessage = resources.getString(R.string.yourtripcanceledrider)
                    dialogs.putExtra("message", resources.getString(R.string.yourtripcanceledrider))
                    dialogs.putExtra("type", 1)
                    dialogs.putExtra("status", 1)
                    dialogs.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(dialogs)


                } else if (json.getJSONObject("custom").has("trip_payment")) {
                    val riderProfile = json.getJSONObject("custom").getJSONObject("trip_payment").getString("rider_thumb_image")
                    sessionManager.riderProfilePic = riderProfile

                    val dialogs = Intent(context, CommonDialog::class.java)
                    dialogs.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    dialogs.putExtra("message", resources.getString(R.string.paymentcompleted))
                    dialogs.putExtra("type", 1)
                    dialogs.putExtra("status", 2)
                    startActivity(dialogs)

                } else if (json.getJSONObject("custom").has("custom_message")) {
                    val notificationUtils = NotificationUtils(context)
                    notificationUtils.playNotificationSound()

                    val message = json.getJSONObject("custom").getJSONObject("custom_message").getString("message_data")
                    val title = json.getJSONObject("custom").getJSONObject("custom_message").getString("title")

                    notificationUtils.generateNotification(context, message, title)
                } else if (json.getJSONObject("custom").has("manual_booking_trip_booked_info")) {
                    manualBookingTripBookedInfo(CommonKeys.ManualBookingPopupType.bookedInfo, json.getJSONObject("custom").getJSONObject("manual_booking_trip_booked_info"),context)
                } else if (json.getJSONObject("custom").has("manual_booking_trip_reminder")) {
                    manualBookingTripBookedInfo(CommonKeys.ManualBookingPopupType.reminder, json.getJSONObject("custom").getJSONObject("manual_booking_trip_reminder"),context)
                } else if (json.getJSONObject("custom").has("manual_booking_trip_canceled_info")) {
                    manualBookingTripBookedInfo(CommonKeys.ManualBookingPopupType.cancel, json.getJSONObject("custom").getJSONObject("manual_booking_trip_canceled_info"),context)
                } else if (json.getJSONObject("custom").has("manual_booking_trip_assigned")) {
                    manualBookingTripStarts(json.getJSONObject("custom").getJSONObject("manual_booking_trip_assigned"),context)
                } else if (json.getJSONObject("custom").has("user_calling")) {
                    initSinchService()
                } else if (json.getJSONObject("custom").has("chat_notification")) {

                    val tripId = json.getJSONObject("custom").getJSONObject("chat_notification").getString("trip_id")

                    val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                    //String timeStamp = new SimpleDateFormat("HH.mm.ss").format(new Date());
                    if (!ActivityChat.isOnChat || !sessionManager.tripId!!.equals(tripId)) {
                        /*  val notificationIntent = Intent(context, ActivityChat::class.java)
                          notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                          notificationIntent.putExtra(CommonKeys.FIREBASE_CHAT_FROM_PUSH, json.toString())

                          val notificationUtils = NotificationUtils(context)
                          notificationUtils.playNotificationSound()
                          val message = json.getJSONObject("custom").getJSONObject("chat_notification").getString("message_data")
                          val title = context.getString(R.string.app_name)
                          println("ChatNotification : Driver" + message)
                          notificationUtils.showNotificationMessage(title, message, timeStamp, notificationIntent, null)
  */
                        val pushNotification = Intent(Config.PUSH_NOTIFICATION)
                        pushNotification.putExtra("message", "message")
                        LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification)
                        //startActivity(rider);
                    }
                } else {
                    DebuggableLogE("Ride Request Received", "unable to process")
                }


            } else {
                DebuggableLogE(TAG, "ELSE: $json")

                // app is in background, show the notification in notification tray
                if (json.getJSONObject("custom").has("ride_request")) {

                    val isPool = json.getJSONObject("custom").getJSONObject("ride_request").getBoolean("is_pool")

                    if (DriverStatus == "Online"
                            && (TripStatus == null || TripStatus == CommonKeys.TripDriverStatus.EndTrip || TripStatus == "" || isPool)
                            && UserId != null) {


                        try {
                            val json = JSONObject(json.toString())
                            var requestId = ""


                            val jsonObject = JSONObject(json.toString())
                            if (jsonObject.getJSONObject("custom").has("ride_request")) {
                                requestId = jsonObject.getJSONObject("custom").getJSONObject("ride_request").getString("request_id")
                            }

                            if (!sessionManager.requestId.equals(requestId)) {
                                sessionManager.requestId = requestId
                                val title = json.getJSONObject("custom").getJSONObject("ride_request").getString("title")

                                val notificationUtils = NotificationUtils(context)
                                notificationUtils.playNotificationSound()
                                notificationUtils.generateNotification(context, "", title)
                                sessionManager.isDriverAndRiderAbleToChat = false
                                CommonMethods.stopFirebaseChatListenerService(context)
                                val intent = Intent(this, RequestReceiveActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                            }
                        } catch (e: Exception) {

                        }
                    }
                } else if (json.getJSONObject("custom").has("cancel_trip")) {
                    sessionManager.clearTripID()
                    sessionManager.clearTripStatus()
                    val tripriders=json.getJSONObject("custom").getJSONObject("cancel_trip").getJSONArray("trip_riders")
                    if(tripriders.length()>0){
                        sessionManager.isTrip=true
                    }else{
                        sessionManager.isTrip=false
                    }
                    sessionManager.isDriverAndRiderAbleToChat = false
                    CommonMethods.stopFirebaseChatListenerService(context)
                    stopSinchService()
                    val dialogs = Intent(context, CommonDialog::class.java)
                    dialogs.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    dialogs.putExtra("message", resources.getString(R.string.yourtripcanceledrider))
                    dialogs.putExtra("type", 1)
                    dialogs.putExtra("status", 1)
                    startActivity(dialogs)


                } else if (json.getJSONObject("custom").has("trip_payment")) {
                    val riderProfile = json.getJSONObject("custom").getJSONObject("trip_payment").getString("rider_thumb_image")
                    sessionManager.riderProfilePic = riderProfile

                    val dialogs = Intent(context, CommonDialog::class.java)
                    dialogs.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    dialogs.putExtra("message", resources.getString(R.string.paymentcompleted))
                    dialogs.putExtra("type", 1)
                    dialogs.putExtra("status", 2)
                    startActivity(dialogs)

                }/* else if (json.getJSONObject("custom").has("custom_message")) {
                    val notificationUtils = NotificationUtils(context)
                    notificationUtils.playNotificationSound()
                    val message = json.getJSONObject("custom").getJSONObject("custom_message").getString("message_data")
                    val title = json.getJSONObject("custom").getJSONObject("custom_message").getString("title")

                    notificationUtils.generateNotification(context, message, title)
                }*/ else if (json.getJSONObject("custom").has("manual_booking_trip_booked_info")) {
                    manualBookingTripBookedInfo(CommonKeys.ManualBookingPopupType.bookedInfo, json.getJSONObject("custom").getJSONObject("manual_booking_trip_booked_info"),context)
                } else if (json.getJSONObject("custom").has("manual_booking_trip_reminder")) {
                    manualBookingTripBookedInfo(CommonKeys.ManualBookingPopupType.reminder, json.getJSONObject("custom").getJSONObject("manual_booking_trip_reminder"),context)
                } else if (json.getJSONObject("custom").has("manual_booking_trip_canceled_info")) {
                    manualBookingTripBookedInfo(CommonKeys.ManualBookingPopupType.cancel, json.getJSONObject("custom").getJSONObject("manual_booking_trip_canceled_info"),context)
                } else if (json.getJSONObject("custom").has("manual_booking_trip_assigned")) {
                    manualBookingTripStarts(json.getJSONObject("custom").getJSONObject("manual_booking_trip_assigned"),context)
                } else if (json.getJSONObject("custom").has("user_calling")) {
                    initSinchService()
                } else if (json.getJSONObject("custom").has("chat_notification")) {


                    val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                    //String timeStamp = new SimpleDateFormat("HH.mm.ss").format(new Date());


                    val notificationIntent = Intent(context, ActivityChat::class.java)
                    notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    notificationIntent.putExtra(CommonKeys.FIREBASE_CHAT_FROM_PUSH, json.toString())

                    sessionManager.chatJson = json.toString()
                    val notificationUtils = NotificationUtils(context)
                    notificationUtils.playNotificationSound()

                    val message = json.getJSONObject("custom").getJSONObject("chat_notification").getString("message_data")
                    //val title = context.getString(R.string.app_name)
                    val title = json.getJSONObject("custom").getJSONObject("chat_notification").getString("user_name")
                    println("ChatNotification : Driver" + message)
                    notificationUtils.showNotificationMessage(title, message, timeStamp, notificationIntent, null, 0L)


                } else {
                    DebuggableLogE("Ride Request Received", "unable to process")
                }

            }
        } catch (e: JSONException) {
            DebuggableLogE(TAG, "Json Exception: " + e.message)
            e.printStackTrace()
        } catch (e: Exception) {
            DebuggableLogE(TAG, "Exception: " + e.message)
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

    fun manualBookingTripBookedInfo(manualBookedPopupType: Int, jsonObject: JSONObject , context: Context) {
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

        val dialogs = Intent(context, ManualBookingDialog::class.java)
        dialogs.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        dialogs.putExtra(CommonKeys.KEY_MANUAL_BOOKED_RIDER_NAME, riderName)
        dialogs.putExtra(CommonKeys.KEY_MANUAL_BOOKED_RIDER_CONTACT_NUMBER, riderContactNumber)
        dialogs.putExtra(CommonKeys.KEY_MANUAL_BOOKED_RIDER_PICKU_LOCATION, riderPickupLocation)
        dialogs.putExtra(CommonKeys.KEY_MANUAL_BOOKED_RIDER_PICKU_DATE_AND_TIME, riderPickupDateAndTime)
        dialogs.putExtra(CommonKeys.KEY_TYPE, manualBookedPopupType)
        startActivity(dialogs)

    }

    fun manualBookingTripStarts(jsonResp: JSONObject, context: Context) {


        val riderModel = gson.fromJson(jsonResp.toString(), TripDetailsModel::class.java)
        sessionManager.riderName = riderModel.riderDetails.get(0).name
        sessionManager.riderId = riderModel.riderDetails.get(0).riderId!!
        sessionManager.riderRating = riderModel.riderDetails.get(0).rating
        sessionManager.riderProfilePic = riderModel.riderDetails.get(0).profileImage
        sessionManager.bookingType = riderModel.riderDetails.get(0).bookingType
        sessionManager.tripId = riderModel.riderDetails.get(0).tripId.toString()
        sessionManager.subTripStatus = resources.getString(R.string.confirm_arrived)
        //sessionManager.setTripStatus("CONFIRM YOU'VE ARRIVED");
        sessionManager.tripStatus = CommonKeys.TripDriverStatus.ConfirmArrived
        //sessionManager.paymentMethod = riderModel.paymentMode

        sessionManager.isDriverAndRiderAbleToChat = true
        CommonMethods.startFirebaseChatListenerService(this)


        /* if (!WorkerUtils.isWorkRunning(CommonKeys.WorkTagForUpdateGPS)) {
             DebuggableLogE("locationupdate", "StartWork:")
             WorkerUtils.startWorkManager(CommonKeys.WorkKeyForUpdateGPS, CommonKeys.WorkTagForUpdateGPS, UpdateGPSWorker::class.java,this,sessionManager.driverStatus)
         }*/

        //  acceptedDriverDetails = new AcceptedDriverDetails(ridername, mobilenumber, profileimg, ratingvalue, cartype, pickuplocation, droplocation, pickuplatitude, droplatitude, droplongitude, pickuplongitude);
        //        mPlayer.stop();
        val requestaccept = Intent(context, RequestAcceptActivity::class.java)
        requestaccept.putExtra("riderDetails", riderModel)
        requestaccept.putExtra("tripstatus", resources.getString(R.string.confirm_arrived))
        requestaccept.putExtra(CommonKeys.KEY_IS_NEED_TO_PLAY_SOUND, CommonKeys.YES)
        requestaccept.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(requestaccept)
    }




    fun setLocale() {
        val lang = sessionManager.language

        if (lang != "") {
            val langC = sessionManager.languageCode
            val locale = Locale(langC)
            Locale.setDefault(locale)
            val config = Configuration()
            config.locale = locale
            resources.updateConfiguration(
                    config,
                    resources.displayMetrics
            )
        } else {
            sessionManager.language = "English"
            sessionManager.languageCode = "en"
        }


    }

    companion object {

        private val TAG = MyFirebaseMessagingService::class.java.simpleName
    }
}
