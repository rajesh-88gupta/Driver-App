package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

class WeeklyStatementModel {

    @Expose
    @SerializedName("statement")
    var statement: List<Statement>? = null
    @Expose
    @SerializedName("driver_statement")
    var driver_statement: DriverStatement? = null
    @Expose
    @SerializedName("status_message")
    var status_message: String? = null
    @Expose
    @SerializedName("status_code")
    var status_code: String? = null
    @Expose
    @SerializedName("total_page")
    var totalPage: Int = 1

    class Statement {
        @Expose
        @SerializedName("created_date")
        var date: String? = null
        @Expose
        @SerializedName("format")
        var format: String? = null
        @Expose
        @SerializedName("driver_earning")
        var driverEarnings: String? = null
    }

    class DriverStatement {
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
        var price: String? = null
        @Expose
        @SerializedName("key")
        var date: String? = null
    }
}
