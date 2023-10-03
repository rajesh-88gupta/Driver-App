package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.io.Serializable

/**
 * Created by Seen Technologies on 9/12/18.
 */

class RatingModel : Serializable {

    @SerializedName("status_message")
    @Expose
    var statusMessage: String? = null
    @SerializedName("status_code")
    @Expose
    var statusCode: String? = null
    @SerializedName("total_rating")
    @Expose
    var totalRating: String? = null
    @SerializedName("total_rating_count")
    @Expose
    var totalRatingCount: String? = null
    @SerializedName("driver_rating")
    @Expose
    var driverRating: String? = null
    @SerializedName("five_rating_count")
    @Expose
    var fiveRatingCount: String? = null
}
