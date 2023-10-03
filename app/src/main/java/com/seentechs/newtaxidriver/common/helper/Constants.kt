package com.seentechs.newtaxidriver.common.helper

import com.seentechs.newtaxidriver.R

/**
 * @author Seen Technologies
 *
 * @package com.seentechs.newtaxidriver.common.helper
 * @subpackage helper
 * @category Constants
 */

/* ************************************************************
Constants values for shared preferences
*************************************************************** */

object Constants {

    const val notificationIcon = R.drawable.app_a_app_notification_icon
    const val PayToAdmin = 250
    var RequestEndTime= "0"

    val REQUEST_CODE_GALLERY = 5

    val STATUS_MSG = "status_message"
    val STATUS_CODE = "status_code"
    val REFRESH_ACCESS_TOKEN = "refresh_token"

    val Male = "male"
    val Female = "female"

    val GoogleDistanceType = "google"
    val No = "no"

    val PROFILEIMAGE = "image_url"

    const val PICK_IMAGE_REQUEST_CODE = 1888
    const val SELECT_FILE = 1

    const val PAGE_START = 1
    const val VIEW_TYPE_ITEM = 0
    const val VIEW_TYPE_LOADING = 1

    const val SKIP_UPDATE = "skip_update"
    const val FORCE_UPDATE = "force_update"


    /**************** DB Keys*************/
    val DB_KEY_TRIP_DETAILS: String? = "TripDetails"
    val DB_KEY_PAY_STATEMENTS_WEEKLY: String? = "WeeklyPaymentList"
    val DB_KEY_PAY_STATEMENTS_WEEKLY_DETAILS: String? = "WeeklyPaymentListDetails"
    val DB_KEY_PAY_STATEMENTS_DAILY_DETAILS: String? = "DailyPaymentListDetails"


    val DB_KEY_DRIVER_PROFILE: String? = "DriverProfile"
    val DB_KEY_DRIVER_VEHICLE: String? = "DriverVehicleDetails"
    val DB_KEY_RIDER_COMMENTS: String? = "RiderCommentsForDriver"

    val DB_KEY_PENDING_TRIPS: String? = "DriverPendingTrips"
    val DB_KEY_COMPLETED_TRIPS: String? = "DriverCompletedTrips"
}
