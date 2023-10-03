package com.seentechs.newtaxidriver.common.util

import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityOptions
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.braintreepayments.api.models.ThreeDSecureAdditionalInformation
import com.braintreepayments.api.models.ThreeDSecurePostalAddress
import com.braintreepayments.api.models.ThreeDSecureRequest
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.seentechs.newtaxidriver.BuildConfig
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.custompalette.FontCache
import com.seentechs.newtaxidriver.common.database.AddFirebaseDatabase
import com.seentechs.newtaxidriver.common.helper.CommonDialog
import com.seentechs.newtaxidriver.common.helper.Constants
import com.seentechs.newtaxidriver.common.helper.Constants.RequestEndTime
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.common.helper.ManualBookingDialog
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.home.MainActivity
import com.seentechs.newtaxidriver.home.datamodel.TripDetailsModel
import com.seentechs.newtaxidriver.home.datamodel.firebase_keys.FirebaseDbKeys
import com.seentechs.newtaxidriver.home.firebaseChat.ActivityChat
import com.seentechs.newtaxidriver.home.pushnotification.Config
import com.seentechs.newtaxidriver.home.pushnotification.NotificationUtils
import com.seentechs.newtaxidriver.home.signinsignup.SigninSignupHomeActivity
import com.seentechs.newtaxidriver.trips.RequestAcceptActivity
import com.seentechs.newtaxidriver.trips.RequestReceiveActivity
import com.seentechs.newtaxidriver.trips.voip.NewTaxiSinchService
import com.seentechs.newtaxidriver.trips.voip.NewTaxiSinchService.Companion.sinchClient
import kotlinx.android.synthetic.main.app_common_button_large.view.*
import kotlinx.android.synthetic.main.app_common_header.view.*
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.jvm.Throws

/**
 * Created by Seen Technologies on 9/7/18.
 */

class CommonMethods {

    lateinit @Inject
    internal var sessionManager: SessionManager


    @Inject
    lateinit var gson: Gson

    private val TAG = CommonMethods::class.java.simpleName

    private var stripe: Stripe? = null
    private lateinit var auth: FirebaseAuth

    lateinit var tripfile: File
    lateinit var writer: FileWriter

    lateinit var mProgressDialog: Dialog

    init {
        AppController.getAppComponent().inject(this)
    }

    fun getJsonValue(jsonString: String, key: String, `object`: Any): Any {
        var objct = `object`
        try {
            val jsonObject = JSONObject(jsonString)
            if (jsonObject.has(key)) objct = jsonObject.get(key)
        } catch (e: Exception) {
            e.printStackTrace()
            return Any()
        }

        return objct
    }

    fun showProgressDialog(context: Context) {
        try {
            if (this::mProgressDialog.isInitialized && mProgressDialog != null && mProgressDialog.isShowing) {
                mProgressDialog.dismiss()
            }

            mProgressDialog = getLoadingDialog(context, R.layout.app_loader_view)
            mProgressDialog.setCancelable(true)
            mProgressDialog.setCanceledOnTouchOutside(false)
            mProgressDialog.setOnKeyListener { dialog, keyCode, event -> keyCode == KeyEvent.KEYCODE_BACK }
            mProgressDialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getLoadingDialog(mContext: Context, mLay: Int): Dialog {
        val mDialog = getDialog(mContext, mLay)
        mDialog.setCancelable(true)
        mDialog.setCanceledOnTouchOutside(true)

        mDialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT)
        mDialog.window!!.setGravity(Gravity.CENTER)

        return mDialog
    }

    private fun getDialog(mContext: Context, mLayout: Int): Dialog {
        val mDialog = Dialog(mContext)
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        mDialog.setContentView(mLayout)
        mDialog.window!!.setBackgroundDrawable(
                ColorDrawable(Color.TRANSPARENT))
        mDialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT)
        return mDialog
    }


    fun hideProgressDialog() {
        if (this::mProgressDialog.isInitialized && mProgressDialog.isShowing) {
            try {
                mProgressDialog.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }


    fun showProgressDialog(mActivity: AppCompatActivity?, customDialog: CustomDialog?) {
        try {
            if (mActivity == null || customDialog == null || customDialog.dialog != null && customDialog.dialog!!.isShowing)
                return
            progressDialog = CustomDialog(true, mActivity.resources.getString(R.string.loading))
            progressDialog!!.show(mActivity.supportFragmentManager, "")
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    //Show Dialog with message
    fun showMessage(context: Context?, dialog: AlertDialog?, msg: String) {
        if (context != null && dialog != null && !(context as Activity).isFinishing) {
            dialog.setMessage(msg)
            dialog.show()
        }
    }

    //Create and Get Dialog
    fun getAlertDialog(context: Context): AlertDialog {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setPositiveButton(context.resources.getString(R.string.ok)) { dialogInterface, i -> dialogInterface.dismiss() }
        val dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) //before
        return dialog
    }


    fun showProgressDialogPaypal(mActivity: AppCompatActivity?, customDialog: CustomDialog?, message: String) {
        if (mActivity == null || customDialog == null || customDialog.dialog != null && customDialog.dialog!!.isShowing)
            return
        progressDialogPaypal = CustomDialog(true, message)
        progressDialogPaypal!!.show(mActivity.supportFragmentManager, "")
    }

    fun hideProgressDialogPaypal() {
        if (progressDialogPaypal == null || progressDialogPaypal!!.dialog == null || !progressDialogPaypal!!.dialog?.isShowing!!)
            return
        progressDialogPaypal!!.dismissAllowingStateLoss()
        progressDialogPaypal = null
    }

    fun imageChangeforLocality(context: Context, image: ImageView) {
        if (context.resources.getString(R.string.layout_direction) == "1") {
            image.rotation = 180f
        } else
            image.rotation = 0f

    }

    fun imageChangeforLocality(context: Context, image: TextView) {
        if (context.resources.getString(R.string.layout_direction) == "1") {
            image.rotation = 180f
        } else
            image.rotation = 0f

    }

    fun cameraFilePath(context: Context): File {
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return File(getDefaultStoragePath(context.getResources().getString(R.string.app_name)), context.getResources().getString(R.string.app_name) + System.currentTimeMillis() + ".png")
        } else {
            return File(getDefaultCameraPath(context), context.resources.getString(R.string.app_name) + System.currentTimeMillis() + ".png")
        }*/
        return File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), context.resources.getString(R.string.app_name) + System.currentTimeMillis() + ".png")
    }


    fun refreshGallery(context: Context, file: File) {
        try {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(file) //out is your file you saved/deleted/moved/copied
            mediaScanIntent.data = contentUri
            context.sendBroadcast(mediaScanIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    /*
    *  Handle push notification message
    */

    fun handleDataMessage(json: JSONObject, context: Context) {
        if (json.getJSONObject("custom").has("id")) {
            val notificationId = json.getJSONObject("custom").getString("id")
            if (sessionManager.notificationID.equals(notificationId))
                return
            else
                sessionManager.notificationID = json.getJSONObject("custom").getString("id")
        }

        DebuggableLogE(TAG, "push json: $json")
        val TripStatus = sessionManager.tripStatus
        val DriverStatus = sessionManager.driverStatus
        val UserId = sessionManager.accessToken
        try {
            val mFirebaseDatabase = FirebaseDatabase.getInstance().getReference(context.getString(R.string.real_time_db))
            mFirebaseDatabase!!.child(FirebaseDbKeys.Notification).child(sessionManager.userId!!).removeValue()

            if (json.getJSONObject("custom").has("request_id")) {
                val requestId = json.getJSONObject("custom").getString("request_id")
                if (sessionManager.requestId.equals(requestId))
                    return
                else
                    sessionManager.requestId = json.getJSONObject("custom").getString("request_id")
            }
            DebuggableLogE(TAG, "push json: $json")

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
                    var isPool = false
                    val jsonObject = JSONObject(json.toString())
                    if (jsonObject.getJSONObject("custom").has("ride_request")) {
                        isPool = json.getJSONObject("custom").getJSONObject("ride_request").getBoolean("is_pool")
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
                                context.startActivity(requstreceivepage)

                                val pushNotification = Intent(Config.PUSH_NOTIFICATION)
                                pushNotification.putExtra("message", "message")
                                LocalBroadcastManager.getInstance(context).sendBroadcast(pushNotification)
                                //startActivity(rider);
                            }
                        }

                    }


                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (json.getJSONObject("custom").has("cancel_trip")) {

                    //val tripriders = json.getJSONObject("custom").getJSONObject("cancel_trip").getJSONArray("trip_riders")
                    sessionManager.isTrip = false
                    sessionManager.clearTripID()
                    sessionManager.clearTripStatus()
                    sessionManager.isDriverAndRiderAbleToChat = false
                    stopFirebaseChatListenerService(context)
                    stopSinchService(context)
                    val dialogs = Intent(context, CommonDialog::class.java)
                    println("Langugage " + context.resources.getString(R.string.yourtripcanceledrider))
                    sessionManager.dialogMessage = context.resources.getString(R.string.yourtripcanceledrider)
                    dialogs.putExtra("message", context.resources.getString(R.string.yourtripcanceledrider))
                    dialogs.putExtra("type", 1)
                    dialogs.putExtra("status", 1)
                    dialogs.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(dialogs)


                } else if (json.getJSONObject("custom").has("trip_payment")) {
                    val riderProfile = json.getJSONObject("custom").getJSONObject("trip_payment").getString("rider_thumb_image")
                    sessionManager.riderProfilePic = riderProfile

                    val dialogs = Intent(context, CommonDialog::class.java)
                    dialogs.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    dialogs.putExtra("message", context.resources.getString(R.string.paymentcompleted))
                    dialogs.putExtra("type", 1)
                    dialogs.putExtra("status", 2)
                    context.startActivity(dialogs)

                } else if (json.getJSONObject("custom").has("custom_message")) {
                    val notificationUtils = NotificationUtils(context)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        notificationUtils.playNotificationSound()
                    }
                    val message = json.getJSONObject("custom").getJSONObject("custom_message").getString("message_data")
                    val title = json.getJSONObject("custom").getJSONObject("custom_message").getString("title")
                    notificationUtils.generateNotification(context, message, title)
                } else if (json.getJSONObject("custom").has("manual_booking_trip_booked_info")) {
                    manualBookingTripBookedInfo(CommonKeys.ManualBookingPopupType.bookedInfo, json.getJSONObject("custom").getJSONObject("manual_booking_trip_booked_info"), context)
                } else if (json.getJSONObject("custom").has("manual_booking_trip_reminder")) {
                    manualBookingTripBookedInfo(CommonKeys.ManualBookingPopupType.reminder, json.getJSONObject("custom").getJSONObject("manual_booking_trip_reminder"), context)
                } else if (json.getJSONObject("custom").has("manual_booking_trip_canceled_info")) {
                    manualBookingTripBookedInfo(CommonKeys.ManualBookingPopupType.cancel, json.getJSONObject("custom").getJSONObject("manual_booking_trip_canceled_info"), context)
                } else if (json.getJSONObject("custom").has("manual_booking_trip_assigned")) {
                    manualBookingTripStarts(json.getJSONObject("custom").getJSONObject("manual_booking_trip_assigned"), context)
                } else if (json.getJSONObject("custom").has("user_calling")) {
                    initSinchService(context)
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
                        LocalBroadcastManager.getInstance(context).sendBroadcast(pushNotification)
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
                            println("Get Session requestId " + sessionManager.requestId)
                            println("Get requestId " + requestId)
                            if (!sessionManager.requestId.equals(requestId)) {
                                sessionManager.requestId = requestId

                                var msg = json.getJSONObject("custom").getJSONObject("ride_request").getString("title")

                                if (jsonObject.getJSONObject("custom").has("ride_request")) {
                                    msg = context.resources.getString(R.string.trip_request)
                                }

                                val endtime = jsonObject.getJSONObject("custom").getString("end_time")

                                val duration = difference(getCurrentTimeIntoLong(), endtime.toLong())
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && NotificationUtils.isAppIsInBackground(context)) {
                                    CommonKeys.isRideRequest = true
                                    RequestEndTime = json.getJSONObject("custom").getString("end_time")
                                    val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                                    val intent = Intent(context, RequestReceiveActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    //notificationIntent.putExtra(CommonKeys.FIREBASE_CHAT_FROM_PUSH, json.toString())
                                    val notificationUtils = NotificationUtils(context)
                                    notificationUtils.soundNotification()
                                    val pm: PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                                    val isScreenOn: Boolean = if (Build.VERSION.SDK_INT >= 20) pm.isInteractive() else pm.isScreenOn() // check if screen is on

                                    if (!isScreenOn) {
                                        val wl: PowerManager.WakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "myApp:notificationLock")
                                        wl.acquire(3000) //set your time in milliseconds
                                    }
                                    val title = context.getString(R.string.app_name)
                                    notificationUtils.showNotificationMessage(title, msg, timeStamp, intent, null, duration)
                                    val pushNotification = Intent(Config.PUSH_NOTIFICATION)
                                    pushNotification.putExtra("message", "message")
                                    LocalBroadcastManager.getInstance(context).sendBroadcast(pushNotification)
                                } else {
                                    val notificationUtils = NotificationUtils(context)
                                    notificationUtils.playNotificationSound()
                                    notificationUtils.generateNotification(context, "", msg)
                                    sessionManager.isDriverAndRiderAbleToChat = false
                                    CommonMethods.stopFirebaseChatListenerService(context)
                                    val intent = Intent(context, RequestReceiveActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                }

                            }


                        } catch (e: Exception) {

                        }


                    }

                } else if (json.getJSONObject("custom").has("cancel_trip")) {
                    sessionManager.clearTripID()
                    sessionManager.clearTripStatus()
                    /*val tripriders = json.getJSONObject("custom").getJSONObject("cancel_trip").getJSONArray("trip_riders")
                    if (tripriders.length() > 0) {
                        sessionManager.isTrip = true
                    } else {
                        sessionManager.isTrip = false
                    }*/
                    sessionManager.isTrip = false
                    sessionManager.isDriverAndRiderAbleToChat = false
                    CommonMethods.stopFirebaseChatListenerService(context)
                    stopSinchService(context)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && NotificationUtils.isAppIsInBackground(context)) {
                        AddFirebaseDatabase().removeNodesAfterCompletedTrip(context)
                        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                        val intent = Intent(context, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        //notificationIntent.putExtra(CommonKeys.FIREBASE_CHAT_FROM_PUSH, json.toString())
                        val notificationUtils = NotificationUtils(context)
                        notificationUtils.playNotificationSound()
                        val message = context.resources.getString(R.string.yourtripcanceledrider)
                        val title = context.getString(R.string.app_name)
                        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, null, 0L)
                        val pushNotification = Intent(Config.PUSH_NOTIFICATION)
                        pushNotification.putExtra("message", "message")
                        LocalBroadcastManager.getInstance(context).sendBroadcast(pushNotification)
                    } else {
                        val dialogs = Intent(context, CommonDialog::class.java)
                        dialogs.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        dialogs.putExtra("message", context.resources.getString(R.string.yourtripcanceledrider))
                        dialogs.putExtra("type", 1)
                        dialogs.putExtra("status", 1)
                        context.startActivity(dialogs)
                    }

                } else if (json.getJSONObject("custom").has("trip_payment")) {
                    val riderProfile = json.getJSONObject("custom").getJSONObject("trip_payment").getString("rider_thumb_image")
                    sessionManager.riderProfilePic = riderProfile


                    AddFirebaseDatabase().removeNodesAfterCompletedTrip(context)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && NotificationUtils.isAppIsInBackground(context)) {
                        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                        val intent = Intent(context, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        //notificationIntent.putExtra(CommonKeys.FIREBASE_CHAT_FROM_PUSH, json.toString())
                        val notificationUtils = NotificationUtils(context)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                            notificationUtils.playNotificationSound()
                        val message = context.resources.getString(R.string.paymentcompleted)
                        val title = context.getString(R.string.app_name)
                        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, null, 0L)
                        val pushNotification = Intent(Config.PUSH_NOTIFICATION)
                        pushNotification.putExtra("message", "message")
                        LocalBroadcastManager.getInstance(context).sendBroadcast(pushNotification)
                    } else {
                        val dialogs = Intent(context, CommonDialog::class.java)
                        dialogs.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        dialogs.putExtra("message", context.resources.getString(R.string.paymentcompleted))
                        dialogs.putExtra("type", 1)
                        dialogs.putExtra("status", 2)
                        context.startActivity(dialogs)
                    }

                } else if (json.getJSONObject("custom").has("custom_message")) {
                    val notificationUtils = NotificationUtils(context)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        notificationUtils.playNotificationSound()
                    }
                    val message = json.getJSONObject("custom").getJSONObject("custom_message").getString("message_data")
                    val title = json.getJSONObject("custom").getJSONObject("custom_message").getString("title")
                    notificationUtils.generateNotification(context, message, title)
                } else if (json.getJSONObject("custom").has("manual_booking_trip_booked_info")) {
                    manualBookingTripBookedInfo(CommonKeys.ManualBookingPopupType.bookedInfo, json.getJSONObject("custom").getJSONObject("manual_booking_trip_booked_info"), context)
                } else if (json.getJSONObject("custom").has("manual_booking_trip_reminder")) {
                    manualBookingTripBookedInfo(CommonKeys.ManualBookingPopupType.reminder, json.getJSONObject("custom").getJSONObject("manual_booking_trip_reminder"), context)
                } else if (json.getJSONObject("custom").has("manual_booking_trip_canceled_info")) {
                    manualBookingTripBookedInfo(CommonKeys.ManualBookingPopupType.cancel, json.getJSONObject("custom").getJSONObject("manual_booking_trip_canceled_info"), context)
                } else if (json.getJSONObject("custom").has("manual_booking_trip_assigned")) {
                    manualBookingTripStarts(json.getJSONObject("custom").getJSONObject("manual_booking_trip_assigned"), context)
                } else if (json.getJSONObject("custom").has("user_calling")) {
                    CommonKeys.keyCaller = json.getJSONObject("custom").getJSONObject("user_calling").getString("title")
                    initSinchService(context)
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


    ///////////////// CAMERA ///////////////////////////////////
    fun getDefaultFileName(context: Context): File {
        return File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), context.resources.getString(R.string.app_name) + System.currentTimeMillis() + ".png")
    }


    fun isOnline(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = connectivityManager.activeNetworkInfo
        return netInfo != null && netInfo.isConnected
    }

    fun snackBar(message: String, buttonmessage: String, buttonvisible: Boolean, duration: Int, edt: EditText, txt: TextView, getRes: Resources, ctx: Activity) {
        // Create the Snackbar
        val snackbar: Snackbar
        val snackbar_background: RelativeLayout
        val snack_button: TextView
        val snack_message: TextView
        // Snack bar visible duration
        if (duration == 1)
            snackbar = Snackbar.make(edt, "", Snackbar.LENGTH_INDEFINITE)
        else if (duration == 2)
            snackbar = Snackbar.make(edt, "", Snackbar.LENGTH_LONG)
        else
            snackbar = Snackbar.make(edt, "", Snackbar.LENGTH_SHORT)

        // Get the Snackbar's layout view
        val layout = snackbar.view as Snackbar.SnackbarLayout
        // Hide the text
        val textView = layout.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
        textView.visibility = View.INVISIBLE


        // Inflate our custom view
        val snackView = ctx.layoutInflater.inflate(R.layout.snackbar, null)
        // Configure the view

        snackbar_background = snackView.findViewById<View>(R.id.snackbar) as RelativeLayout
        snack_button = snackView.findViewById<View>(R.id.snack_button) as TextView
        snack_message = snackView.findViewById<View>(R.id.snackbar_text) as TextView

        snackbar_background.setBackgroundColor(getRes.getColor(R.color.white)) // Background Color

        if (buttonvisible)
        // set Right side button visible or gone
            snack_button.visibility = View.VISIBLE
        else
            snack_button.visibility = View.GONE

        snack_button.setTextColor(getRes.getColor(R.color.app_background)) // set right side button text color
        snack_button.text = buttonmessage // set right side button text
        snack_button.setOnClickListener {
            println("onclikedmsg$message")
            if (message == getRes.getString(R.string.invalidelogin)) {
                edt.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                edt.setSelection(edt.length())
                txt.setText(R.string.hide)
            } else if (message == getRes.getString(R.string.emailalreadyexits)) {
                println("oncliked")
                val login = Intent(ctx, SigninSignupHomeActivity::class.java)
                login.putExtra("email", edt.text.toString())
                val bndlanimation = ActivityOptions.makeCustomAnimation(ctx, R.anim.trans_left_in, R.anim.trans_left_out).toBundle()
                ctx.startActivity(login, bndlanimation)
            }
        }

        snack_message.setTextColor(getRes.getColor(R.color.app_background)) // set left side main message text color
        snack_message.text = message  // set left side main message text

        // Add the view to the Snackbar's layout
        layout.addView(snackView, 0)
        // Show the Snackbar
        val snackBarView = snackbar.view
        snackBarView.setBackgroundColor(getRes.getColor(R.color.white))
        snackbar.show()
        println("Snack bar ended")

    }


    /**
     * This ThreeDSecureRequest for Custom Ui
     * It may differ for Custom UI
     * @return ThreeDSecureRequest For 3D Secure
     */
    fun threeDSecureRequest(amount: String): ThreeDSecureRequest {
        val address = ThreeDSecurePostalAddress()
                .givenName(sessionManager!!.firstName)
                .phoneNumber(sessionManager!!.phoneNumber)

        val additionalInformation = ThreeDSecureAdditionalInformation()
                .shippingAddress(address)

        return ThreeDSecureRequest()
                .amount(amount)
                .billingAddress(address)
                .versionRequested(ThreeDSecureRequest.VERSION_2)
                .additionalInformation(additionalInformation)
    }


    /*
     *  Check service is running or not
     */
    public fun isMyServiceRunning(serviceClass: Class<*>, context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }


    companion object {
        var progressDialog: CustomDialog? = null
        var progressDialogPaypal: CustomDialog? = null
        fun gotoMainActivityFromChatActivity(mActivity: Activity) {
            val mainActivityIntent = Intent(mActivity, MainActivity::class.java)
            mainActivityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            mActivity.startActivity(mainActivityIntent)

        }

        fun getAppVersionNameFromGradle(context: Context): String {
            var versionName: String
            try {
                versionName = AppController.context!!.packageManager
                        .getPackageInfo(AppController.context!!.packageName, 0).versionName
            } catch (e: PackageManager.NameNotFoundException) {
                versionName = "0.0"
                e.printStackTrace()
            }

            return versionName
        }

        val appPackageName: String
            get() {
                var packageName: String
                try {
                    packageName = AppController.context!!.packageName
                } catch (e: Exception) {
                    packageName = ""
                    e.printStackTrace()
                }

                return packageName
            }

        fun DebuggableLogE(tag: String, message: String?) {
            if (CommonKeys.isLoggable!!) {
                Log.e(tag, message.toString())
            }
        }

        fun DebuggableLogE(tag: String, message: String?, tr: Throwable) {
            if (CommonKeys.isLoggable!!) {
                Log.e(tag, message, tr)
            }
        }

        fun DebuggableLogI(tag: String, message: String?) {
            if (CommonKeys.isLoggable!!) {
                Log.i(tag, message.toString())
            }
        }

        fun DebuggableLogD(tag: String, message: String?) {
            if (CommonKeys.isLoggable!!) {
                Log.d(tag, message.toString())
            }
        }

        fun DebuggablePrintln(tag: String, message: String?) {
            if (CommonKeys.isLoggable!!) {
                println(tag + " : " + message)
            }
        }

        fun DebuggableLogV(tag: String, message: String?) {
            if (CommonKeys.isLoggable!!) {
                Log.v(tag, message.toString())
            }
        }

        fun DebuggableToast(mContext: Context, message: String?) {
            if (CommonKeys.isLoggable!!) {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
            }
        }

        fun showUserMessage(view: View, mContext: Context, message: String?) {
            val snackbar = Snackbar.make(view, message!!, Snackbar.LENGTH_LONG)
            snackbar.show()
        }

        fun showUserMessage(message: String?) {
            try {
                if (!TextUtils.isEmpty(message)) {
                    Toast.makeText(AppController.context, message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun showServerInternalErrorMessage(context: Context) {
            showUserMessage(context.resources.getString(R.string.internal_server_error))
        }

        fun startFirebaseChatListenerService(mContext: Context) {
            // mContext.startService(Intent(mContext, FirebaseChatNotificationService::class.java))
        }

        fun stopFirebaseChatListenerService(mContext: Context) {
            //mContext.stopService(Intent(mContext, FirebaseChatNotificationService::class.java))
        }

        fun isMyBackgroundServiceRunning(serviceClass: Class<*>, mContext: Context): Boolean {
            val manager = mContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
            return false
        }

        fun getCustomHeaderForAlert(mContext: Context, title: String): TextView {
            val headerFontTextView = TextView(mContext)
            headerFontTextView.setPadding(50, 50, 50, 10)
            headerFontTextView.setTextColor(mContext.resources.getColor(R.color.ub__black))
            headerFontTextView.text = title
            headerFontTextView.textSize = 20f
            headerFontTextView.setTypeface(FontCache.getTypeface(mContext.resources.getString(R.string.fonts_UBERMedium), mContext), Typeface.BOLD)
            headerFontTextView.gravity = Gravity.CENTER


            return headerFontTextView
        }

        fun openPlayStore(context: Context) {
            val appPackageName = appPackageName // getPackageName() from Context or Activity object
            try {
                val playstoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName"))
                playstoreIntent.setPackage("com.android.vending")
                context.startActivity(playstoreIntent)
            } catch (anfe: android.content.ActivityNotFoundException) {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
            }

        }


        fun copyContentToClipboard(mContext: Context, textToBeCopied: String) {
            var cManager = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val cData = ClipData.newPlainText("text", textToBeCopied)
            if (!TextUtils.isEmpty(textToBeCopied)) {
                cManager.setPrimaryClip(cData)
                showUserMessage(mContext.resources.getString(R.string.referral_code_copied))
            } else {
                showUserMessage(mContext.resources.getString(R.string.referral_code_not_copied))
            }

        }

        fun playVibration() {
            try {
                val v = AppController.context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                // Vibrate for 500 milliseconds
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    //deprecated in API 26
                    v.vibrate(500)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun startSinchService(mContext: Context) {
            mContext.startService(Intent(mContext, NewTaxiSinchService::class.java))
        }

        fun stopSinchService(mContext: Context) {
            try {

                if (sinchClient != null) {
                    sinchClient?.stopListeningOnActiveConnection()
                    sinchClient?.terminateGracefully()
                    sinchClient = null
                }
                /*  sinchClient?.stopListeningOnActiveConnection()
                  sinchClient?.terminateGracefully()*/
                //sinchClient = null

                mContext.stopService(Intent(mContext, NewTaxiSinchService::class.java))
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun showInternetNotAvailableForStoredDataViewer(mContext: Context) {
            /*val inflater: LayoutInflater = mContext.getLayoutInflater()
            val layout: View = inflater.inflate(R.layout.toast_layout,mContext.findViewById(android.R.id.toast_layout_root) as ViewGroup?)
            val toast = Toast(mContext)
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
            toast.duration = Toast.LENGTH_LONG
            toast.setView(layout)
            toast.show()*/

            val toast: Toast = Toast.makeText(mContext, mContext.resources.getString(R.string.you_are_viewing_old_data), Toast.LENGTH_LONG)
            /* val toastView = toast.view // This'll return the default View of the Toast.
             val toastMessage = toastView?.findViewById(android.R.id.message)
             toastMessage.setTextSize(20f)
             toastMessage.setTextColor(Color.RED)
             //toastMessage.setCompoundDrawablesWithIntrinsicBounds(android.R.mipmap.ic_fly, 0, 0, 0)
             toastMessage.gravity = Gravity.CENTER
             toastMessage.compoundDrawablePadding = 16
             toastView.setBackgroundColor(Color.navy)*/
            toast.show()
        }

        fun showNoInternetAlert(context: Context, iNoInternetCustomAlertCallBack: INoInternetCustomAlertCallback) {
            val builder = AlertDialog.Builder(context)
            builder.setCancelable(false)
            builder.setIcon(R.drawable.ic_wifi_off)
            builder.setTitle(context.resources.getString(R.string.no_connection))
            builder.setMessage(context.resources.getString(R.string.enable_connection_and_come_back))

            builder.setPositiveButton(context.resources.getString(R.string.ok)) { dialogInterface, i ->
                iNoInternetCustomAlertCallBack.onOkayClicked()
                dialogInterface.dismiss()
            }
            builder.setNeutralButton(context.resources.getString(R.string.retry)) { dialogInterface, i ->
                iNoInternetCustomAlertCallBack.onRetryClicked()
                dialogInterface.dismiss()
            }
            val dialog = builder.create()
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) //before
            dialog.window!!.setBackgroundDrawable(context.getDrawable(R.drawable.round_shape_corner_20dp))
            dialog.show()
        }

    }

    fun getFireBaseToken(): String {
        var pos = ""
        /* FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
             pos = it.token
             sessionManager.deviceId=pos
             println("Get id $pos")
         }*/


        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(OnCompleteListener {

                    if (it.isSuccessful()) {
                        //return "";
                        pos = it.result?.token!!
                        sessionManager.deviceId = pos
                        println("Device Id  : " + sessionManager.deviceId)

                    } else {
                        println("Device Id Exception : " + it.exception)

                    }


                });

        return pos

    }

    /**
     * init Stripe
     */
    fun initStripeData(context: Context) {
        PaymentConfiguration.init(context, sessionManager.stripePublishKey)
        stripe = Stripe(context, PaymentConfiguration.getInstance(context).publishableKey)
    }

    /**
     * Stripe Instance
     */
    fun stripeInstance(): Stripe? {
        return stripe
    }

    /**
     * Get Client Secret From Response
     */
    fun getClientSecret(jsonResponse: JsonResponse, activity: AppCompatActivity) {
        val clientSecret = getJsonValue(jsonResponse.strResponse, "two_step_id", String::class.java) as String
        if (stripeInstance() != null) {
            stripeInstance()!!.confirmPayment(activity, createPaymentIntentParams(clientSecret, activity.applicationContext))
        } else {
            hideProgressDialog()
            Toast.makeText(activity.applicationContext, activity.applicationContext.resources.getString(R.string.internal_server_error), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Create a Payment to Start Payment Process
     */
    private fun createPaymentIntentParams(clientSecret: String, context: Context): ConfirmPaymentIntentParams {
        return ConfirmPaymentIntentParams.create(clientSecret, context.getString(R.string.PrivacyURL))
    }

    /**
     *  Update Total Distance in Session
     */
    fun updateDistanceInLocal(distance: Double) {
        if (sessionManager.tripStatus.equals(CommonKeys.TripDriverStatus.BeginTrip, true)) {
            if (distance < CommonKeys.CheckGoogleDistanceEvery1M) {
                val totaldistance = sessionManager.totalDistance + distance
                sessionManager.totalDistance = totaldistance.toFloat()
                updateDistanceInFile(sessionManager.totalDistance, "GoogleDistanceEvery1Min")
            }
        }
    }


    fun manualBookingTripBookedInfo(manualBookedPopupType: Int, jsonObject: JSONObject, context: Context) {
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
        context.startActivity(dialogs)

    }

    fun manualBookingTripStarts(jsonResp: JSONObject, context: Context) {


        val riderModel = gson.fromJson(jsonResp.toString(), TripDetailsModel::class.java)
        sessionManager.riderName = riderModel.riderDetails.get(0).name
        sessionManager.riderId = riderModel.riderDetails.get(0).riderId!!
        sessionManager.riderRating = riderModel.riderDetails.get(0).rating
        sessionManager.riderProfilePic = riderModel.riderDetails.get(0).profileImage
        sessionManager.bookingType = riderModel.riderDetails.get(0).bookingType
        sessionManager.tripId = riderModel.riderDetails.get(0).tripId.toString()
        sessionManager.subTripStatus = context.resources.getString(R.string.confirm_arrived)
        //sessionManager.setTripStatus("CONFIRM YOU'VE ARRIVED");
        sessionManager.tripStatus = CommonKeys.TripDriverStatus.ConfirmArrived
        //sessionManager.paymentMethod = riderModel.paymentMode

        sessionManager.isDriverAndRiderAbleToChat = true
        CommonMethods.startFirebaseChatListenerService(context)


        /* if (!WorkerUtils.isWorkRunning(CommonKeys.WorkTagForUpdateGPS)) {
             DebuggableLogE("locationupdate", "StartWork:")
             WorkerUtils.startWorkManager(CommonKeys.WorkKeyForUpdateGPS, CommonKeys.WorkTagForUpdateGPS, UpdateGPSWorker::class.java,this,sessionManager.driverStatus)
         }*/

        //  acceptedDriverDetails = new AcceptedDriverDetails(ridername, mobilenumber, profileimg, ratingvalue, cartype, pickuplocation, droplocation, pickuplatitude, droplatitude, droplongitude, pickuplongitude);
        //        mPlayer.stop();
        val requestaccept = Intent(context, RequestAcceptActivity::class.java)
        requestaccept.putExtra("riderDetails", riderModel)
        requestaccept.putExtra("tripstatus", context.resources.getString(R.string.confirm_arrived))
        requestaccept.putExtra(CommonKeys.KEY_IS_NEED_TO_PLAY_SOUND, CommonKeys.YES)
        requestaccept.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(requestaccept)
    }


    private fun initSinchService(context: Context) {
        if (!sessionManager.accessToken.isNullOrEmpty()) {
            context.startService(Intent(context, NewTaxiSinchService::class.java))
        }
    }

    fun createFileAndUpdateDistance(context: Context) {
        try {
            val distanceFile = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), context.getResources().getString(R.string.app_name) + "_TripDistanceCalculation")
            if (!distanceFile.exists()) {
                distanceFile.mkdir()
            }
            tripfile = File(distanceFile, "TripId_" + sessionManager.tripId!!.toString() + ".txt")
            writer = FileWriter(tripfile)
            writer.append("Trip Id ==> " + sessionManager.tripId.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateDistanceInFile(distance: Float, distanceUpdateType: String) {
        try {
            writer.append("\n" + getDate() + " ==> " + distanceUpdateType + " -> " + distance.toString())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun finalDistanceUpdateInFile(googleDistance: Float, distanceCalculatedEvery1Sec: Float, calculatedDistance: Float) {
        try {
            writer.append("\n")
            writer.append("\n ***** finalDistanceUpdateInFile ***** ")
            writer.append("\n${getDate()}")
            writer.append("\nGoogle Calculation ===> $googleDistance")
            writer.append("\n1Sec Distance Calculation ===> $distanceCalculatedEvery1Sec")
            writer.append("\n1Min Distance Calculation ===> $calculatedDistance")
            writer.flush()
            writer.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun getDate(): String {
        val today = Date()
        val format = SimpleDateFormat("dd-MM-yyyy hh:mm:ss a", Locale.ENGLISH)
        return format.format(today)
    }

    fun getFileWriter(): Boolean {
        return ::writer.isInitialized
    }

    fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.ENGLISH)
        return sdf.format(Date())
    }

    fun checkTimings(time: String, endtime: String): Boolean {
        val pattern = "dd MMM yyyy HH:mm:ss"
        val sdf = SimpleDateFormat(pattern, Locale.ENGLISH)
        try {
            val date1 = sdf.parse(time)
            val date2 = sdf.parse(endtime)
            return date1.compareTo(date2) < 0  // Date 2
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return false
    }

    fun getTimeFromLong(time: Long): String {
        val date = Date(time * 1000L) // *1000 is to convert seconds to milliseconds
        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.ENGLISH) // the format of your date
        //sdf.timeZone = TimeZone.getTimeZone("GMT-4")
        return sdf.format(date)
    }

    /* fun getTimeFromLong(timestamp: Long): String {
         val cal = Calendar.getInstance(Locale.ENGLISH)
         cal.timeInMillis = timestamp * 1000L
         return DateFormat.format("dd MMM yyyy HH:mm:ss", cal).toString()
     }*/


    fun vectorToBitmap(@DrawableRes id: Int, context: Context): BitmapDescriptor? {
        val vectorDrawable: Drawable = ResourcesCompat.getDrawable(context.resources, id, null)!!
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


    interface INoInternetCustomAlertCallback {
        fun onOkayClicked()
        fun onRetryClicked()
    }

    fun setButtonText(string: String, view: View) {
        view.button.setText(string)
    }

    fun setheaderText(string: String, view: View) {
        view.headertext.text = string
    }

    fun getCurrentTimeIntoLong(): Long {
        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.ENGLISH)
        try {
            val date: Date = sdf.parse(getCurrentTime())
            return date.time / 1000L
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    fun difference(currentTimeLong: Long, endTimelong: Long): Long {
        return ((endTimelong - currentTimeLong) * 1000)
    }


    fun cameraIntent(imageFile: File, activity: AppCompatActivity) {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val imageUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", imageFile)
        try {
            val resolvedIntentActivities = activity.packageManager.queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolvedIntentInfo in resolvedIntentActivities) {
                val packageName = resolvedIntentInfo.activityInfo.packageName
                activity.grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            cameraIntent.putExtra("return-data", true)
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        activity.startActivityForResult(cameraIntent, Constants.PICK_IMAGE_REQUEST_CODE)
        refreshGallery(activity, imageFile)
    }

    fun galleryIntent(activity: AppCompatActivity){
        val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(i, Constants.SELECT_FILE)
    }

    /**
     * Input output Stream
     */
    @Throws(IOException::class)
    fun copyStream(input: InputStream?, output: FileOutputStream) {
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (input!!.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
        }
    }

    fun clearImageCacheWhenAppOpens(context: Context){
        val dir = File (context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString())
        if (dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                File(dir, children[i]).delete()
            }
        }
    }

}
