package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.io.Serializable
import java.util.ArrayList

/**
 * Created by Seen Technologies on 9/14/18.
 */

class CurrencyDetailsModel : Serializable {
    @SerializedName("success_message")
    @Expose
    var statusMessage: String? = null
    @SerializedName("status_code")
    @Expose
    var statusCode: String? = null
    @SerializedName("currency_list")
    @Expose
    var curreneyListModels = ArrayList<CurreneyListModel>()
}
