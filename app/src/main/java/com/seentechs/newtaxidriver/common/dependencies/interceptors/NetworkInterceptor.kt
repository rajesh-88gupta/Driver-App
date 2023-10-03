package com.seentechs.newtaxidriver.common.dependencies.interceptors

/**
 * @package com.seentechs.newtaxidriver
 * @subpackage dependencies.interceptors
 * @category NetWorkInterceptor
 * @author Seen Technologies
 *
 */

import android.content.Context
import android.net.ConnectivityManager

import org.json.JSONException
import org.json.JSONObject

import java.io.IOException

import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody

/*****************************************************************
 * NetWork Interceptor
 */
class NetworkInterceptor(private val mContext: Context) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var response = chain.proceed(chain.request())
        try {
            if (!isOnline(mContext)) {
                response = Response.Builder().protocol(Protocol.HTTP_1_1).message("No internet").body(getNetworkFailResponse(response)).code(600).request(chain.request()).build()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return response
    }


    /**
     * Method to Check Online Status
     *
     * @param context
     * @return Context of the activity uses NetworkInterceptor
     */

    private fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = connectivityManager.activeNetworkInfo
        return netInfo != null && netInfo.isConnected
    }


    /**
     * Method that returns network failure response
     *
     * @param response pass response value
     * @return returns response body
     */

    private fun getNetworkFailResponse(response: Response): ResponseBody {
        val jsonObject = JSONObject()
        val contentType = response.body!!.contentType()
        try {
            jsonObject.put("code", 600)
            jsonObject.put("status", "Cancel")
            jsonObject.put("message", "No network connection")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return ResponseBody.create(contentType, jsonObject.toString())
    }
}
