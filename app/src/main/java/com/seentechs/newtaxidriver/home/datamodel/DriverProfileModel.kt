package com.seentechs.newtaxidriver.home.datamodel

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable

import androidx.databinding.library.baseAdapters.BR

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.io.Serializable

/**
 * Created by Seen Technologies on 9/14/18.
 */

class DriverProfileModel : BaseObservable(), Serializable {


    @SerializedName("status_message")
    @Expose
    @get:Bindable
    var statusMessage: String? = null
        set(statusMessage) {
            field = statusMessage
            notifyPropertyChanged(BR.statusMessage)

        }
    @SerializedName("status_code")
    @Expose
    var statusCode: String? = null
    @SerializedName("status")
    @Expose
    var status: String? = null
    @SerializedName("first_name")
    @Expose
    lateinit var firstName: String
    @SerializedName("last_name")
    @Expose
    var lastName: String? = null
    @SerializedName("mobile_number")
    @Expose
    lateinit var mobileNumber: String
    @SerializedName("country_code")
    @Expose
    var countryCode: String? = null
    @SerializedName("car_type")
    @Expose
    var carType: String? = null
    @SerializedName("profile_image")
    @Expose
    var profileImage: String? = null
    @SerializedName("city")
    @Expose
    var city: String? = null
    @SerializedName("state")
    @Expose
    var state: String? = null
    @SerializedName("vehicleName")
    @Expose
    var vehicleName: String? = null
    @SerializedName("vehicle_number")
    @Expose
    var vehicleNumber: String? = null

    @SerializedName("currency_code")
    @Expose
    var currencyCode: String? = null
    @SerializedName("currency_symbol")
    @Expose
    var currencySymbol: String? = null
    @SerializedName("bank_details")
    @Expose
    var bank_detail: BankDetailsModel? = null

    @SerializedName("company_id")
    @Expose
    var companyId: Int = 0

    @SerializedName("company_name")
    @Expose
    var companyName: String? = null

    @SerializedName("owe_amount")
    @Expose
    lateinit var oweAmount: String

    @SerializedName("driver_referral_earning")
    @Expose
    lateinit var driverReferralEarning: String

    @SerializedName("car_active_image")
    @Expose
    var carActiveImage: String? = null

    @SerializedName("driver_documents")
    @Expose
    var driverDocuments=  ArrayList<DocumentsModel>()

    @SerializedName("vehicle_details")
    @Expose
    var vehicle=  ArrayList<VehiclesModel>()
}
