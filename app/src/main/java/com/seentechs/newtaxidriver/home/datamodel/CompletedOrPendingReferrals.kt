package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CompletedOrPendingReferrals {
    @SerializedName("id")
    @Expose
    var id: Long? = null
    @SerializedName("name")
    @Expose
    var name: String? = null
    @SerializedName("profile_image")
    @Expose
    var profileImage: String? = null
    @SerializedName("remaining_days")
    @Expose
    var remainingDays: Long? = null
    @SerializedName("trips")
    @Expose
    var trips: Long? = null
    @SerializedName("remaining_trips")
    @Expose
    var remainingTrips: Long = 0

    @SerializedName("earnable_amounts")
    @Expose
    var earnableAmount: String? = null
    @SerializedName("payment_status")
    @Expose
    var status: String? = null
}
