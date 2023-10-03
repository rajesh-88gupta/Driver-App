package com.seentechs.newtaxidriver.home.pushnotification

/**
 * @package com.seentechs.newtaxidriver.home.pushnotification
 * @subpackage pushnotification model
 * @category MyFirebaseInstanceIDService
 * @author Seen Technologies
 *
 */

import com.google.firebase.messaging.FirebaseMessagingService
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.RequestCallback
import java.util.*
import javax.inject.Inject

/* ************************************************************
                MyFirebaseInstanceIDService
Its used to get the push notification FirebaseInstanceIDService function
*************************************************************** */
class MyFirebaseInstanceIDService : FirebaseMessagingService(), ServiceListener {
    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    val deviceId: HashMap<String, String>
        get() {
            val driverStatusHashMap = HashMap<String, String>()
            driverStatusHashMap["user_type"] = sessionManager.type!!
            driverStatusHashMap["device_type"] = sessionManager.deviceType!!
            driverStatusHashMap["device_id"] = sessionManager.deviceId!!
            driverStatusHashMap["token"] = sessionManager.accessToken!!
            return driverStatusHashMap
        }


    override fun onNewToken(p0: String) {
        super.onNewToken(p0)

        AppController.getAppComponent().inject(this)
        //val refreshedToken = commonMethods.getFireBaseToken()
        val refreshedToken = p0

        println("On New Token : "+p0)


        // Saving reg id to shared preferences
        storeRegIdInPref(refreshedToken)

        // sending reg id to your server
        sendRegistrationToServer(refreshedToken)
    }

   /* override fun onTokenRefresh() {
        super.onTokenRefresh()


    }*/

    private fun sendRegistrationToServer(token: String?) {
        // sending FCM token to server
        println("sendRegistrationToServer: " + token)
        sessionManager.deviceId = token!!

        if (sessionManager.accessToken != null) {
            updateDeviceId()
        }
    }

    /*
    * Update driver device id
    */
    private fun storeRegIdInPref(token: String?) {
        val pref = applicationContext.getSharedPreferences(Config.SHARED_PREF, 0)
        val editor = pref.edit()
        editor.putString("regId", token)
        editor.commit()
    }

    fun updateDeviceId() {
        if (!sessionManager.accessToken.isNullOrEmpty() && !sessionManager.deviceId.isNullOrEmpty()) {

            apiService.updateDevice(deviceId).enqueue(RequestCallback(this))
        }
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {

    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {

    }

    companion object {

        private val TAG = MyFirebaseInstanceIDService::class.java.simpleName
    }

}

