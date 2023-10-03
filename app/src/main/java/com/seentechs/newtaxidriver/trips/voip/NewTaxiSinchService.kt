package com.seentechs.newtaxidriver.trips.voip

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.sinch.android.rtc.*

import com.sinch.android.rtc.calling.Call
import com.sinch.android.rtc.calling.CallClient
import com.sinch.android.rtc.calling.CallClientListener
import com.sinch.android.rtc.calling.CallListener
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonKeys
import com.seentechs.newtaxidriver.home.pushnotification.Config
import com.seentechs.newtaxidriver.home.pushnotification.NotificationUtils
import com.sinch.android.rtc.internal.ManagedPush
import java.text.SimpleDateFormat
import java.util.*

import javax.inject.Inject


class NewTaxiSinchService : Service(), PushTokenRegistrationCallback {
    lateinit var mContext: Context
    internal var mPushTokenIsRegistered: Boolean = true


    @Inject
    lateinit var sessionManager: SessionManager

    internal var userCallId = ""
    private var mBinder = MyBinder()

    val sinchService: NewTaxiSinchService
        get() = this@NewTaxiSinchService

    override fun onBind(intent: Intent?): IBinder? {
        mBinder = MyBinder()
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
        AppController.getAppComponent().inject(this)
        mContext = applicationContext
        createSinchClient(sessionManager.userId!!, sessionManager.sinchKey!!, sessionManager.sinchSecret!!, mContext)
    }

    fun createSinchClient(userCallId: String, sinchKey: String, sinchSecret: String, context: Context) {
        /*if(sinchClient !=null){
            sinchClient.terminate();
        }*/
        this.userCallId = userCallId

        sinchClient = Sinch.getSinchClientBuilder()
                .context(context)
                .userId(userCallId)
                .applicationKey(sinchKey)
                //.applicationSecret(sinchSecret)
                .environmentHost("clientapi.sinch.com")
                .build()


       // sinchClient?.setSupportPushNotifications(true)
       // sinchClient?.setSupportManagedPush(true)
        //sinchClient?.setSupportActiveConnectionInBackground(true)
       // sinchClient?.setPushNotificationDisplayName("you missed a call from")
        //sinchClient?.setSupportCalling(true)
        sinchClient?.startListeningOnActiveConnection()
        //sinchClient.checkManifest();


        sinchClient?.callClient?.addCallClientListener(SinchCallClientListener())

        /*sinchClient.setSupportPushNotifications(true);
        sinchClient.setSupportManagedPush(true);*/

        sinchClient?.start()
        if ((!mPushTokenIsRegistered)) {
            //getManagedPush()!!.registerPushToken(this)
        }

    }

   // fun getManagedPush(): ManagedPush? {
        // create client, but you don't need to start it
        //initClient(username);
       // createSinchClient(sessionManager.userId!!, sessionManager.sinchKey!!, sessionManager.sinchSecret!!, mContext)
        // retrieve ManagedPush
        //return Beta.createManagedPush(sinchClient)
   // }

    fun tokenRegistered() {
        mPushTokenIsRegistered = true

    }

    fun tokenRegistrationFailed(sinchError: SinchError) {
        mPushTokenIsRegistered = false

    }


    private inner class SinchCallClientListener : CallClientListener {
        override fun onIncomingCall(callClient: CallClient, incomingCall: Call) {
            call = incomingCall
            call?.addCallListener(object : CallListener {
                override fun onCallEstablished(p0: Call?) {
                    println("onCallEstablished")
                }

                override fun onCallProgressing(p0: Call?) {
                    println("onCallProgressing")
                }

                fun onShouldSendPushNotification(p0: Call?, p1: MutableList<PushPair>?) {
                    println("onShouldSendPushNotification")
                }

                override fun onCallEnded(p0: Call?) {
                    println("onCallEnded")
                    NotificationUtils.clearNotifications(mContext)
                }

            })
            try{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && NotificationUtils.isAppIsInBackground(mContext)) {
                    val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                    val notificationIntent = Intent(mContext, CallProcessingActivity::class.java)
                    notificationIntent.putExtra(CommonKeys.KEY_TYPE, CallProcessingActivity.CallActivityType.Ringing)
                    notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    //notificationIntent.putExtra(CommonKeys.FIREBASE_CHAT_FROM_PUSH, json.toString())
                    val notificationUtils = NotificationUtils(mContext)
                    notificationUtils.playNotificationSound()
                    val message = CommonKeys.keyCaller
                    val title = mContext.getString(R.string.app_name)
                    notificationUtils.showNotificationMessage(title, message, timeStamp, notificationIntent, null, 0L)
                    val pushNotification = Intent(Config.PUSH_NOTIFICATION)
                    pushNotification.putExtra("message", "message")
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(pushNotification)
                }else{
                    val callScreen = Intent(applicationContext, CallProcessingActivity::class.java)
                    callScreen.putExtra(CommonKeys.KEY_TYPE, CallProcessingActivity.CallActivityType.Ringing)
                    callScreen.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK )
                    applicationContext.startActivity(callScreen)
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }


    }

    fun getSinchClient(): SinchClient? {
        return sinchClient
    }

    inner class MyBinder : Binder() {
        val sinchClient: NewTaxiSinchService
            get() = this@NewTaxiSinchService
    }

    companion object {
         var sinchClient: SinchClient?=null
         var call: Call?= null
    }

    override fun onPushTokenRegistered() {
        TODO("Not yet implemented")
    }

    override fun onPushTokenRegistrationFailed(p0: SinchError?) {
        TODO("Not yet implemented")
    }

}
