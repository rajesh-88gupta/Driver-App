package com.seentechs.newtaxidriver.home.datamodel

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.squareup.picasso.Picasso
import java.io.Serializable
import java.util.*

class RiderDetailsModelList : Serializable {

    @SerializedName("name")
    @Expose
    var name: String? = null
    @SerializedName("id")
    @Expose
    var riderId: String? = null
    @SerializedName("payment_mode")
    @Expose
    var paymentMode: String? = null
    @SerializedName("trip_id")
    @Expose
    var tripId: String? = null
    @SerializedName("image")
    @Expose
    var profileImage: String? = null
    @SerializedName("pickup")
    @Expose
    var pickupAddress: String? = null
    @SerializedName("drop")
    @Expose
    var destAddress: String? = null

    @SerializedName("pickup_lat")
    @Expose
    var pickup_lat: String? = null

    @SerializedName("pickup_lng")
    @Expose
    var pickup_lng: String? = null

    @SerializedName("drop_lat")
    @Expose
    var drop_lat: String? = null

    @SerializedName("drop_lng")
    @Expose
    var drop_lng: String? = null

    @SerializedName("otp")
    @Expose
    var otp: String? = null

    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("mobile_number")
    @Expose
    var mobile_number: String = ""
    @SerializedName("rating")
    @Expose
    var rating: String?=null

    @SerializedName("currency_symbol")
    @Expose
    var currencySymbol: String? = null

    @SerializedName("booking_type")
    @Expose
    lateinit var bookingType: String

    @SerializedName("driver_payout")
    @Expose
    var driverPayout: String? = null

    @SerializedName("total_fare")
    @Expose
    var totalFare: String? = null
    @SerializedName("driver_earnings")
    @Expose
    var driverEarnings: String? = null

    @SerializedName("total_time")
    @Expose
    var totalTime: String? = null
    @SerializedName("total_km")
    @Expose
    var totalKm: String? = null

    @SerializedName("trip_path")
    @Expose
    var tripPath: String? = null
    @SerializedName("map_image")
    @Expose
    var mapImage: String? = null
    @SerializedName("car_type")
    @Expose
    var carType: String? = null
    @SerializedName("car_active_image")
    @Expose
    var carActiveImage: String? = null

    @SerializedName("schedule_display_date")
    @Expose
    var scheduleDisplayDate: String? = null


    @SerializedName("invoice")
    @Expose
    var invoice: ArrayList<InvoiceModel>? = null

    companion object {
        @BindingAdapter("android:loadImage")
        @JvmStatic
        fun loadImage(view: ImageView, url: String) {

            if(!url.equals(""))
            {
                Picasso.get().load(url).into(view)
            }


        }
    }

}