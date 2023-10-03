package com.seentechs.newtaxidriver.common.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.text.TextUtils
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.database.AddFirebaseDatabase
import com.seentechs.newtaxidriver.common.helper.Constants
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.google.locationmanager.TrackingServiceListener
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import com.seentechs.newtaxidriver.home.signinsignup.SigninSignupHomeActivity
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

/**
 * Created by Seen Technologies on 9/7/18.
 */

class RequestCallback : Callback<ResponseBody> {

    lateinit @Inject
    var jsonResp: JsonResponse
    lateinit @Inject
    var context: Context
    lateinit @Inject
    var apiService: ApiService
    lateinit @Inject
    var sessionManager: SessionManager
    private var listener: ServiceListener? = null
    private var requestCode = 0
    private var requestData = ""
    lateinit var trackingServiceListener: TrackingServiceListener
    lateinit internal var addFirebaseDatabase: AddFirebaseDatabase

    private val isOnline: Boolean
        get() {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = connectivityManager.activeNetworkInfo
            return netInfo != null && netInfo.isConnected
        }

    constructor() {
        AppController.getAppComponent().inject(this)
        addFirebaseDatabase = AddFirebaseDatabase()
    }

    constructor(listener: ServiceListener) {
        AppController.getAppComponent().inject(this)
        addFirebaseDatabase = AddFirebaseDatabase()
        this.listener = listener
    }

    constructor(requestCode: Int, listener: ServiceListener) {
        AppController.getAppComponent().inject(this)
        addFirebaseDatabase = AddFirebaseDatabase()
        this.listener = listener
        this.requestCode = requestCode
    }

    constructor(requestCode: Int, listener: ServiceListener, requestData: String) {
        AppController.getAppComponent().inject(this)
        addFirebaseDatabase = AddFirebaseDatabase()
        this.listener = listener
        this.requestCode = requestCode
        this.requestData = requestData
    }

    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
        this.listener!!.onSuccess(getSuccessResponse(call, response), requestData)
    }

    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
        this.listener!!.onFailure(getFailureResponse(call, t), requestData)
    }

    private fun getFailureResponse(call: Call<ResponseBody>?, t: Throwable): JsonResponse? {
        try {
            jsonResp.clearAll()
            if (call != null && call.request() != null) {
                jsonResp.method = call.request().method
                jsonResp.requestCode = requestCode
                jsonResp.url = call.request().url.toString()
                LogManager.i(call.request().toString())
            }
            jsonResp.isOnline = isOnline
            if (isOnline) {
                jsonResp.statusMsg = context.resources.getString(R.string.internal_server_error)
            } else {
                jsonResp.statusMsg = context.resources.getString(R.string.network_failure)
            }
            jsonResp.errorMsg = t.message
            jsonResp.isSuccess = false
            requestData = (if (!isOnline) context.resources.getString(R.string.network_failure) else t.message)!!
            LogManager.e(requestCode.toString())

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return jsonResp
    }


    private fun getSuccessResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?): JsonResponse? {
        try {
            jsonResp.clearAll()
            CommonKeys.driverInActive=1;
            if (call != null && call.request() != null) {
                jsonResp.method = call.request().method
                jsonResp.requestCode = requestCode
                jsonResp.url = call.request().url.toString()
                LogManager.i(call.request().toString())
            }
            if (response != null) {
                //LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Config.UPDATE_UI).putExtra("isInActive",false));

                jsonResp.responseCode = response.code()
                jsonResp.isSuccess = false
                jsonResp.statusMsg = context.resources.getString(R.string.internal_server_error)
                if (response.isSuccessful && response.body() != null) {
                    val strJson = response.body()!!.string()
                    jsonResp.strResponse = strJson
                    jsonResp.statusMsg = this.getStatusMsg(strJson)!!
                    if (jsonResp.statusMsg.equals("Token Expired", ignoreCase = true)) {
                        jsonResp.statusMsg = context.resources.getString(R.string.internal_server_error)
                        /* String urls=call.request().url().toString();
                        urls.replace(oldToken,sessionManager.getToken());*/
                    }
                    jsonResp.isSuccess = isSuccess(strJson)
                    LogManager.e(strJson)
                } else if (response.code() == 401 || response.code() == 404) {
                    //jsonResp.setStatusMsg(context.getResources().getString(R.string.account_deactivated));

                    CommonKeys.driverInActive=0
                    addFirebaseDatabase.removeDriverFromGeofire(context)

                    val lang = sessionManager.language
                    val langCode = sessionManager.languageCode
                    sessionManager.clearToken()
                    sessionManager.clearAll()

                    sessionManager.language = lang
                    sessionManager.languageCode = langCode
                    //((Activity)context).finishAffinity();
                    val intent = Intent(context, SigninSignupHomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra("clearservice",true)
                    context.startActivity(intent)
                    (context as Activity).finish()

                    trackingServiceListener = TrackingServiceListener((context as Activity))
                    trackingServiceListener.stopTrackingService()

                }
                //                else if (response.code() == 403) {
                //                    jsonResp.setStatusMsg(context.getString(R.string.status_pending));
                //                    Intent intent=new Intent(Config.UPDATE_UI);
                //                    intent.putExtra("isInActive",true);
                //                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                //                }


                jsonResp.requestData = requestData
                jsonResp.isOnline = isOnline
                requestData = if (!isOnline) context.resources.getString(R.string.network_failure) else "im Asdmin tyoghc"

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return jsonResp
    }

    private fun isSuccess(jsonString: String): Boolean {
        var isSuccess = false
        try {
            if (!TextUtils.isEmpty(jsonString)) {
                val statusCode = getJsonValue(jsonString, Constants.STATUS_CODE, String::class.java) as String
                isSuccess = !TextUtils.isEmpty(statusCode) && "1" == statusCode
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return isSuccess
    }

    private fun getStatusMsg(jsonString: String): String? {
        var statusMsg = ""
        try {
            if (!TextUtils.isEmpty(jsonString)) {
                statusMsg = getJsonValue(jsonString, Constants.STATUS_MSG, String::class.java) as String
                if (statusMsg.equals("Token Expired", ignoreCase = true)) {
                    val token = getJsonValue(jsonString, Constants.REFRESH_ACCESS_TOKEN, String::class.java) as String
                    //sessionManager.setToken(token);
                    sessionManager.token = token
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return statusMsg
    }

    private fun getJsonValue(jsonString: String, key: String, `object`: Any): Any {
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

}

