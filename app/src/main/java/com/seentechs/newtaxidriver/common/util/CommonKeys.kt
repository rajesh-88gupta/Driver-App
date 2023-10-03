package com.seentechs.newtaxidriver.common.util

import androidx.annotation.IntDef
import androidx.annotation.StringDef

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

object CommonKeys {
    var keyCaller =""
    val KEY_MANUAL_BOOKED_RIDER_NAME = "name"
    val KEY_MANUAL_BOOKED_RIDER_CONTACT_NUMBER = "number"
    val KEY_MANUAL_BOOKED_RIDER_PICKU_LOCATION = "pickuploc"
    val KEY_MANUAL_BOOKED_RIDER_PICKU_DATE_AND_TIME = "datetime"
    val KEY_TYPE = "type"
    val DOCUMENT = "document"
    val DELETE = "delete"
    val EDIT = "edit"
    val KEY_CALLER_ID = "caller_id"
    val YES = 1
    val NO = 0
    var isTripBegin=false
    var getUrlCount=0;
    var isRideRequest=false

    val isWallet=1
    val KEY_IS_NEED_TO_PLAY_SOUND = "playSound"
    var isLoggable: Boolean? = true
    val DeviceTypeAndroid = "2"
    val IncompleteReferralArray = 358// just a random number
    val CompletedReferralArray = 247

    var isSetPaymentMethod =false


    const val WEB_PAY_TO_ADMIN = "web_payment?"
    const val PAY_TO_ADMIN = "pay_to_admin"

    var driverInActive = 1
    var FIREBASE_CHAT_MESSAGE_KEY = "message"
    var FIREBASE_CHAT_TYPE_KEY = "type"
    var FIREBASE_CHAT_TYPE_RIDER = "rider"
    var FIREBASE_CHAT_TYPE_DRIVER = "driver"
    var FIREBASE_CHAT_FROM_PUSH = "chat_push"

    var IS_ALREADY_IN_TRIP = false

    val ACTIVITY_REQUEST_CODE_START_FACEBOOK_ACCOUNT_KIT = 102

    val FIREBASE_CHAT_ACTIVITY_SOURCE_ACTIVITY_TYPE_CODE = "sourceActivityCode"
    val FIREBASE_CHAT_ACTIVITY_REDIRECTED_FROM_NOTIFICATION = 111

    val FACEBOOK_ACCOUNT_KIT_VERIFACATION_SUCCESS = 1
    val FACEBOOK_ACCOUNT_KIT_MESSAGE_KEY = "message"
    val FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY = "phoneNumber"
    val FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY = "countryCode"
    val FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_Name_CODE_KEY = "countryNameCode"
    var TripId = "trip_id"

    // payment types used in payment select activity for shared preference purpose
    val PAYMENT_PAYPAL = "paypal"
    val PAYMENT_CARD = "stripe"
    val PAYMENT_BRAINTREE = "braintree"

    //Distance Check
    val CheckNormalDistanceEverySec = 250
    val CheckGoogleDistanceEvery1M = 1500

    const val STATIC_MAP_STYLE= "feature:administrative|element:geometry.fill|color:0xf6f6f4&style=feature:administrative|element:geometry.stroke|color:0xf6f6f4&style=feature:administrative|element:labels.text.fill|color:0x8d8d8d&style=feature:landscape.man_made|element:geometry.fill|color:0xf6f6f4&style=feature:landscape.man_made|element:geometry.stroke|color:0xcfd4d5&style=feature:landscape.natural|element:geometry.fill|color:0xf6f6f4&style=feature:landscape.natural|element:labels.text.fill|color:0x8d8d8d&style=feature:landscape.natural.terrain|element:geometry|color:0xececec|visibility:off&style=feature:landscape.natural.terrain|element:geometry.fill|color:0xf6f6f4&style=feature:poi|element:geometry.fill|color:0xdde2e3&style=feature:poi|element:labels.text.fill|color:0x8d8d8d&style=feature:poi.park|element:geometry.fill|color:0xc3eea5&style=feature:poi.par|element:geometry.stroke|color:0xbae6a1&style=feature:poi.sports_complex|element:geometry.fill|color:0xf1f1eb&style=feature:poi.sports_complex|element:geometry.stroke|color:0xf1f1eb&style=feature:road.arterial|element:geometry.fill|color:0xfcfcfc&style=feature:road.highway|element:geometry.fill|color:0xeceeed&style=feature:road.highway|element:geometry.stroke|color:0xeceeed&style=feature:road.highway.controlled_access|element:geometry.fill|color:0xeceeed&style=feature:road.local|element:geometry.fill|color:0xfcfcfc&style=feature:transit.line|element:geometry.fill|color:0xc3d3d4&style=feature:transit.line|element:labels.text.fill|color:0xececec&style=feature:transit.station|element:labels.text.fill|color:0xc3d4d6&style=feature:water|element:geometry.fill|color:0xcad2d3&style=feature:administrative.neighborhood|element:labels.text.fill|lightness:25&style=feature:poi|element:labels.icon|saturation:-100&style=feature:poi|element:labels.icon|saturation:-45|lightness:10|visibility:on&style=feature:road.highway|element:labels.icon|visibility:on&style=feature:transit|element:labels.icon|saturation:-70&style=feature:transit.station.airport|element:geometry.fill|saturation:-100|lightness:-5"

    @IntDef(FirebaseChatServiceTriggeredFrom.backgroundService, FirebaseChatServiceTriggeredFrom.chatActivity)
    @Retention(RetentionPolicy.SOURCE)
    annotation class FirebaseChatServiceTriggeredFrom {
        companion object {
           const val backgroundService = 0
           const val chatActivity = 1
        }
    }

    @StringDef(DriverStatus.Online, DriverStatus.Offline)
    @Retention(RetentionPolicy.SOURCE)
    annotation class DriverStatus {
        companion object {
            const val Online = "Online"
            const val Offline = "Offline"
        }
    }

    @StringDef(TripDriverStatus.ConfirmArrived, TripDriverStatus.BeginTrip, TripDriverStatus.EndTrip)
    @Retention(RetentionPolicy.SOURCE)
    annotation class TripDriverStatus {
        companion object {
            const val ConfirmArrived = "CONFIRM YOU'VE ARRIVED"
            const val BeginTrip = "Begin Trip"
            const val EndTrip = "End Trip"
        }
    }

    @StringDef(Intents.DocumentDetailsIntent, Intents.VehicleDetailsIntent)
    @Retention(RetentionPolicy.SOURCE)
    annotation class Intents {
        companion object {
            const val DocumentDetailsIntent = "DOCUMENT DETAILS INTENT"
            const val VehicleDetailsIntent = "VEHICLE DETAILS INTENT"
        }
    }

    @StringDef(DownloadTask.DistCalcResume, DownloadTask.AcceptRequest, DownloadTask.UpdateRoute, DownloadTask.EndTrip)
    @Retention(RetentionPolicy.SOURCE)
    annotation class DownloadTask {
        companion object {
            const val DistCalcResume = "DISTANCE CALC FROM RESUME"
            const val AcceptRequest = "ACCEPT REQUEST"
            const val UpdateRoute = "UPDATE ROUTE"
            const val EndTrip = "END TRIP"
            const val DrawRoute = "DRAW ROUTE"
            const val MoveMarker = "MOVE MARKER"
        }
    }

    @IntDef(ManualBookingPopupType.bookedInfo, ManualBookingPopupType.cancel, ManualBookingPopupType.reminder)
    @Retention(RetentionPolicy.SOURCE)
    annotation class ManualBookingPopupType {
        companion object {
            const val bookedInfo = 1
            const val cancel = 0
            const val reminder = 2
        }
    }

    @StringDef(RideBookedType.schedule, RideBookedType.auto, RideBookedType.manualBooking)
    @Retention(RetentionPolicy.SOURCE)
    annotation class RideBookedType {
        companion object {
            const val schedule = "Schedule Booking"
            const val auto = ""
            const val manualBooking = "Manual Booking"
        }
    }

    @StringDef(TripStatus.Completed, TripStatus.Rating, TripStatus.Cancelled, TripStatus.Payment, TripStatus.Begin_Trip, TripStatus.End_Trip, TripStatus.Scheduled)
    @Retention(RetentionPolicy.SOURCE)
    annotation class TripStatus {
        companion object {
            const val Completed = "Completed"
            const val Rating = "Rating"
            const val Cancelled = "Cancelled"
            const val Payment = "Payment"
            const val Begin_Trip = "Begin trip"
            const val End_Trip = "End trip"
            const val Scheduled = "Scheduled"
        }
    }

    @StringDef(ReferralStatus.Expired, ReferralStatus.Completed)
    @Retention(RetentionPolicy.SOURCE)
    annotation class ReferralStatus {
        companion object {
            const val Expired = "Expired"
            const val Completed = "Completed"
        }
    }

    val UpdatePolyline = "POLYLINE : "
    @StringDef(DirectionParse.DistCalcResume, DirectionParse.AcceptRequest, DirectionParse.UpdateRoute, DirectionParse.EndTrip)
    @Retention(RetentionPolicy.SOURCE)
    annotation class DirectionParse {
        companion object {
            const val DistCalcResume = "DISTANCE CALC FROM RESUME"
            const val AcceptRequest = "ACCEPT REQUEST"
            const val UpdateRoute = "UPDATE ROUTE"
            const val EndTrip = "END TRIP"
        }
    }
}