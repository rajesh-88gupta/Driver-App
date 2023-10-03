package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.io.Serializable

/**
 * Created by Seen Technologies on 9/12/18.
 */

class RiderFeedBackArrayModel : Serializable {

    @SerializedName("date")
    @Expose
    var date: String? = null
    @SerializedName("rider_rating")
    @Expose
    var riderRating: String? = null

    @SerializedName("rider_comments")
    @Expose
    var riderComments: String? = null

    @SerializedName("trip_id")
    @Expose
    var tripId: String? = null
}
