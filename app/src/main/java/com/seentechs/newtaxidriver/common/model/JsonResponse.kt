package com.seentechs.newtaxidriver.common.model

/**
 * Created by Seen Technologies on 9/7/18.
 */

class JsonResponse {
    var url: String? = null


     val statusCode: String? = null
    var method: String? = null
    var responseCode: Int = 0
    var requestCode: Int = 0
    var errorMsg: String? = null
    var isOnline: Boolean = false
    var statusMsg: String=""
    var isSuccess: Boolean = false
    var strResponse: String=""
    var requestData: String? = null

    fun clearAll() {
        this.url = ""
        this.method = ""
        this.errorMsg = ""
        this.statusMsg = ""
        this.strResponse = ""
        this.requestData = ""
        this.requestCode = 0
        this.responseCode = 0
        this.isOnline = false
        this.isSuccess = false
    }
}

