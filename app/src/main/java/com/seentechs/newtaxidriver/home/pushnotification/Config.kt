package com.seentechs.newtaxidriver.home.pushnotification

/**
 * @author Seen Technologies
 *
 * @package com.seentechs.newtaxidriver.home.pushnotification
 * @subpackage pushnotification model
 * @category Config
 */


/* ************************************************************
                Config
Its used to get global pushnotification Service function
*************************************************************** */

object Config {

    // global topic to receive app wide push notifications
    val TOPIC_GLOBAL = "global"

    // broadcast receiver intent filters
    val REGISTRATION_COMPLETE = "registrationComplete"
    val PUSH_NOTIFICATION = "pushNotification"
    val DISTANCE_CALCULATION = "DistanceCalculation"
    val NETWORK_CHANGES = "networkChanges"

    val UPDATE_UI = "updateUi"

    // id to handle the notification in the notification tray
    val NOTIFICATION_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"

    val SHARED_PREF = "ah_firebase"
}
