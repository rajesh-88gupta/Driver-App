package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ReferredFriendsModel {
    @SerializedName("status_code")
    @Expose
    var statusCode: String? = null
    @SerializedName("status_message")
    @Expose
    var statusMessage: String? = null
    @SerializedName("referral_code")
    @Expose
    var referralCode: String? = null
    @SerializedName("referral_link")
    @Expose
    var referralLink: String? = null
    @SerializedName("total_earning")
    @Expose
    var totalEarning: String? = null
    @SerializedName("pending_referrals")
    @Expose
    var pendingReferrals: List<CompletedOrPendingReferrals>? = null
    @SerializedName("completed_referrals")
    @Expose
    var completedReferrals: List<CompletedOrPendingReferrals>? = null

    @SerializedName("referral_amount")
    @Expose
    var referralAmount: String? = null
    @SerializedName("pending_amount")
    @Expose
    var remainingReferral: String? = null
}
