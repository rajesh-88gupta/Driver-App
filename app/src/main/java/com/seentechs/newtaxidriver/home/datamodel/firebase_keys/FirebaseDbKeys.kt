package com.seentechs.newtaxidriver.home.datamodel.firebase_keys


import androidx.annotation.StringDef
import com.seentechs.newtaxidriver.home.datamodel.firebase_keys.FirebaseDbKeys.PaymentChangeMode.Companion.TIPS_ADDED
import com.seentechs.newtaxidriver.home.datamodel.firebase_keys.FirebaseDbKeys.PaymentChangeMode.Companion.TYPE_CASH
import com.seentechs.newtaxidriver.home.datamodel.firebase_keys.FirebaseDbKeys.PaymentChangeMode.Companion.TYPE_CASH_WALLET
import com.seentechs.newtaxidriver.home.datamodel.firebase_keys.FirebaseDbKeys.PaymentChangeMode.Companion.TYPE_PAYPAL
import com.seentechs.newtaxidriver.home.datamodel.firebase_keys.FirebaseDbKeys.PaymentChangeMode.Companion.TYPE_PAYPAL_WALLET
import com.seentechs.newtaxidriver.home.datamodel.firebase_keys.FirebaseDbKeys.PaymentChangeMode.Companion.TYPE_STRIPE
import com.seentechs.newtaxidriver.home.datamodel.firebase_keys.FirebaseDbKeys.PaymentChangeMode.Companion.TYPE_STRIPE_WALLET

object FirebaseDbKeys {

    var Rider = "rider"
    var Driver = "driver"
    var TripId = "trip_id"
    var RELEASE_TYPE = "live"
    var TRIP_PAYMENT_NODE = "trip"
    var TRIP_PAYMENT_NODE_REFRESH_PAYMENT_TYPE_KEY = "refresh_payment_screen"
    var TRIPLIVEPOLYLINE = "path"
    var TRIPETA = "eta_min"
    var GEOFIRE = "GeoFire"
    var Notification = "Notification"

    var TRIP_REQUEST = "trip_request"


    var LIVE_TRACKING_NODE = "live_tracking"
    var chatFirebaseDatabaseName = "driver_rider_trip_chats"

    @StringDef(TYPE_CASH, TYPE_STRIPE, TYPE_PAYPAL, TYPE_CASH_WALLET, TYPE_PAYPAL_WALLET, TYPE_STRIPE_WALLET, TIPS_ADDED)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class PaymentChangeMode {
        companion object {
            const val TYPE_CASH = "C"
            const val TYPE_STRIPE = "S"
            const val TYPE_PAYPAL = "P"
            const val TYPE_CASH_WALLET = "CW"
            const val TYPE_PAYPAL_WALLET = "PW"
            const val TYPE_STRIPE_WALLET = "SW"
            const val TIPS_ADDED = "T"

        }
    }
}
    