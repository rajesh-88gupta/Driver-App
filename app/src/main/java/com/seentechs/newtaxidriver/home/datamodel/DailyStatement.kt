package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.util.ArrayList

class DailyStatement {

    @Expose
    @SerializedName("daily_statement")
    var daily_statement: List<Statement>? = null

    @Expose
    @SerializedName("driver_statement")
    var driver_statement: Driver_statement? = null

    @Expose
    @SerializedName("status_message")
    var status_message: String? = null

    @Expose
    @SerializedName("status_code")
    var status_code: String? = null

    @Expose
    @SerializedName("total_page")
    var totalPage: Int = 0

    class Statement {
        @Expose
        @SerializedName("time")
        var time: String? = null
        @Expose
        @SerializedName("driver_earning")
        var driver_earning: String? = null
        @Expose
        @SerializedName("id")
        var id: String? = null
    }

    class Driver_statement {
        @Expose
        @SerializedName("footer")
        var footer: List<Footer>? = null
        @Expose
        @SerializedName("content")
        var content: ArrayList<InvoiceContent>? = null
        @Expose
        @SerializedName("title")
        var title: String? = null
        @Expose
        @SerializedName("header")
        var header: Header? = null
    }

    class Footer {
        @Expose
        @SerializedName("value")
        var value: String? = null
        @Expose
        @SerializedName("key")
        var key: String? = null
    }


    class Header {
        @Expose
        @SerializedName("value")
        var value: String? = null
        @Expose
        @SerializedName("key")
        var key: String? = null
    }
}
