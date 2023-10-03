package com.seentechs.newtaxidriver.home.datamodel

import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.BindingAdapter
import androidx.databinding.library.baseAdapters.BR
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.home.managevehicles.FeaturesInCarModel
import java.io.Serializable

class VehiclesModel: Serializable,BaseObservable() {

    @SerializedName("id")
    @Expose
    @get:Bindable
    var id: String = ""
        set(id) {
            field = id
            notifyPropertyChanged(BR.id)
        }

    @SerializedName("vehicle_name")
    @Expose
    @get:Bindable
    var vehicleName: String? = null
        set(vehicleName) {
            field = vehicleName
            notifyPropertyChanged(BR.vehicleName)
        }


    @SerializedName("color")
    @Expose
    @get:Bindable
    var vehicleColor: String? = null
        set(vehicleColor) {
            field = vehicleColor
            notifyPropertyChanged(BR.vehicleColor)
        }

    @SerializedName("make")
    @Expose
    @get:Bindable
    var make: Make? = null
        set(make) {
            field = make
            notifyPropertyChanged(BR.make)
        }


    @SerializedName("model")
    @Expose
    @get:Bindable
    var model: Model? = null
        set(model) {
            field = model
            notifyPropertyChanged(BR.model)
        }


    @SerializedName("status")
    @Expose
    @get:Bindable
    var vehicleStatus: String? = null
        set(vehicleStatus) {
            field = vehicleStatus
            notifyPropertyChanged(BR.vehicleStatus)
        }


    @SerializedName("is_default")
    @Expose
    var isDefault: String?=null

    @SerializedName("is_active")
    @Expose
    var isActive: Int?=null

    @SerializedName("year")
    @Expose
    @get:Bindable
    var year: String = ""
        set(year) {
            field = year
            notifyPropertyChanged(BR.year)
        }

    @SerializedName("license_number")
    @Expose
    @get:Bindable
    var licenseNumber: String? = null
        set(licenseNumber) {
            field = licenseNumber
            notifyPropertyChanged(BR.licenseNumber)
        }

    @SerializedName("vechile_documents")
    @Expose
    @get:Bindable
    var document=  ArrayList<DocumentsModel>()
        set(document) {
            field = document
            notifyPropertyChanged(BR.document)
        }

    @SerializedName("vehicleImageURL")
    @Expose
    @get:Bindable
    var vehicleImageURL : String ?=null
    set(vehicleImageURL) {
        field = vehicleImageURL
        notifyPropertyChanged(BR.vehicleImageURL)
    }

    @SerializedName("vehicle_types")
    @Expose
    @get:Bindable
    var vehicleTypes=  ArrayList<VehicleTypes>()
        set(vehicleTypes) {
            field = vehicleTypes
            notifyPropertyChanged(BR.vehicleTypes)
        }

    @SerializedName("request_options")
    @Expose
    @get:Bindable
    var requestOptions=  ArrayList<FeaturesInCarModel>()
        set(requestOptionsInVehicle) {
            field = requestOptionsInVehicle
            notifyPropertyChanged(BR.requestOptions)
        }


    companion object {

        @BindingAdapter("android:changeColor")
        @JvmStatic
        fun changeColor(view: TextView, status: Int) {
            if (status==1)
                view.setTextColor(ContextCompat.getColor(view.context,R.color.newtaxi_app_navy))
            else
                view.setTextColor(ContextCompat.getColor(view.context,R.color.red_text))

        }
    }
}
