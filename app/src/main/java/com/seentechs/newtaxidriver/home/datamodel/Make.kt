package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


class Make : Serializable {
    @SerializedName("id")
    @Expose
    var id: String = ""

    @SerializedName("name")
    @Expose
    var name: String=  ""

    @SerializedName("is_selected")
    @Expose
    var isSelected = false

    @SerializedName("model")
    @Expose
    var model=  ArrayList<Model>()




}
