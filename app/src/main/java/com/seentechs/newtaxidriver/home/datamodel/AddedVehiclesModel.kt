package com.seentechs.newtaxidriver.home.datamodel

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class AddedVehiclesModel: BaseObservable(), Serializable {

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

    @SerializedName("vehicles_details")
    @Expose
    var vehicle=  ArrayList<VehiclesModel>()
}