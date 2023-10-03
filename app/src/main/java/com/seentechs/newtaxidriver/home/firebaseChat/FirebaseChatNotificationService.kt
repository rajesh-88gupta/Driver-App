package com.seentechs.newtaxidriver.home.firebaseChat

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.seentechs.newtaxidriver.common.util.CommonKeys
import com.seentechs.newtaxidriver.home.pushnotification.NotificationUtils
import com.seentechs.newtaxidriver.common.util.CommonMethods.Companion.DebuggableLogD


class FirebaseChatNotificationService : Service(), FirebaseChatHandler.FirebaseChatHandlerInterface {

    lateinit internal var firebaseChatHandler: FirebaseChatHandler
    lateinit internal var notificationUtils: NotificationUtils

    override fun onBind(intent: Intent?): IBinder? {
        // TODO: Return the communication channel to the service.
        return null
    }

    override fun onCreate() {
        super.onCreate()
        DebuggableLogD("chat handler notification", "started")
        // initializing firebaseChatHandler
      //  firebaseChatHandler = FirebaseChatHandler(this, CommonKeys.FirebaseChatServiceTriggeredFrom.backgroundService)
        notificationUtils = NotificationUtils(this)


    }

    override fun onDestroy() {
        super.onDestroy()
        firebaseChatHandler.unRegister()
    }

    override fun pushMessage(firebaseChatModelClass: FirebaseChatModelClass?) {
        if (firebaseChatModelClass!!.type == CommonKeys.FIREBASE_CHAT_TYPE_RIDER)
            notificationUtils.generateFirebaseChatNotification(this, firebaseChatModelClass.message)
        DebuggableLogD("rider message", firebaseChatModelClass.message)
    }
}
