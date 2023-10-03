package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.util.ArrayList

class TripInvoiceModel {

    @SerializedName("status_code")
    @Expose
    var statusCode: String? = null
    @SerializedName("status_message")
    @Expose
    var statusMessage: String? = null
    @SerializedName("payment_mode")
    @Expose
    var paymentMode: String? = null
    @SerializedName("payment_status")
    @Expose
    var paymentStatus: String? = null
    @SerializedName("invoice")
    @Expose
    var invoice: ArrayList<InvoiceModel>? = null
    @SerializedName("total_fare")
    @Expose
    var totalFare: String? = null
    @SerializedName("trip_status")
    @Expose
    var tripStatus: String? = null
    @SerializedName("trip_id")
    @Expose
    var tripId: Int? = null
    @SerializedName("rider_name")
    @Expose
    var riderName: String? = null

}