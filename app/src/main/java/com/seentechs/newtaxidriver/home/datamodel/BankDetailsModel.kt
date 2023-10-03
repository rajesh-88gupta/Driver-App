package com.seentechs.newtaxidriver.home.datamodel

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.io.Serializable

class BankDetailsModel : BaseObservable(), Serializable {

    @SerializedName("holder_name")
    @Expose
    @get:Bindable
    var account_holder_name: String? = null
        set(accountHolderName) {
            field = accountHolderName
            notifyPropertyChanged(BR.account_holder_name)
        }

    @SerializedName("account_number")
    @Expose
    @get:Bindable
    var account_number: String? = null
        set(accountnumber) {
            field = accountnumber
            notifyPropertyChanged(BR.account_number)
        }

    @SerializedName("bank_name")
    @Expose
    @get:Bindable
    var bank_name: String? = null
        set(bank_name) {
            field = bank_name
            notifyPropertyChanged(BR.bank_name)
        }

    @SerializedName("bank_location")
    @Expose
    @get:Bindable
    var bank_location: String? = null
        set(bank_location) {
            field = bank_location
            notifyPropertyChanged(BR.bank_location)
        }

    @SerializedName("code")
    @Expose
    @get:Bindable
    var bank_code: String? = null
        set(bank_code) {
            field = bank_code
            notifyPropertyChanged(BR.bank_code)

        }
}
