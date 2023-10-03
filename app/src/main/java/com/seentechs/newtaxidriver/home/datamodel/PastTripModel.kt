package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.io.Serializable
import java.util.ArrayList

/**
 * Created by Seen Technologies on 9/12/18.
 */

class PastTripModel : Serializable {


    var type: String? = null
    @SerializedName("id")
    @Expose
    var id: String? = null
    @SerializedName("pickup_latitude")
    @Expose
    var pickupLatitude: String? = null
    @SerializedName("pickup_longitude")
    @Expose
    var pickupLongitude: String? = null
    @SerializedName("drop_latitude")
    @Expose
    var dropLatitude: String? = null
    @SerializedName("drop_longitude")
    @Expose
    var dropLongitude: String? = null
    @SerializedName("pickup_location")
    @Expose
    var pickupLocation: String? = null
    @SerializedName("drop_location")
    @Expose
    var dropLocation: String? = null
    @SerializedName("status")
    @Expose
    var status: String? = null
    @SerializedName("payment_method")
    @Expose
    var paymentMethod: String? = null
    @SerializedName("invoice")
    @Expose
    var invoice: ArrayList<InvoiceModel>? = null
    @SerializedName("rider_name")
    @Expose
    var riderName: String? = null

}
