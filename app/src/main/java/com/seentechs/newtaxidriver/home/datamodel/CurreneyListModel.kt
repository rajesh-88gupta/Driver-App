package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.io.Serializable

/**
 * Created by Seen Technologies on 9/14/18.
 */

class CurreneyListModel : Serializable {
    @SerializedName("code")
    @Expose
    var code: String? = null
    @SerializedName("symbol")
    @Expose
    var symbol: String? = null
}
