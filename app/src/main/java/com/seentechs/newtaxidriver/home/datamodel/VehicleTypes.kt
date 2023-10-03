package com.seentechs.newtaxidriver.home.datamodel
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class VehicleTypes: Serializable,BaseObservable()  {


    @SerializedName("id")
    @Expose
    @get:Bindable
    var id: String? = null
        set(id) {
            field = id
            notifyPropertyChanged(BR.id)
        }


    @SerializedName("type")
    @Expose
    @get:Bindable
    var name: String? = null
        set(name) {
            field = name
            notifyPropertyChanged(BR.name)
        }


    @SerializedName("is_checked")
    @Expose
    @get:Bindable
    var isChecked: Boolean = false
        set(isChecked) {
            field = isChecked
            notifyPropertyChanged(BR.checked)
        }



    @SerializedName("location")
    @Expose
    @get:Bindable
    var location: String? = null
        set(location) {
            field = location
            notifyPropertyChanged(BR.location)
        }

}
