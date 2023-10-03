package com.seentechs.newtaxidriver.common.configs

/**
 * @package com.seentechs.newtaxidriver
 * @subpackage configs
 * @category SessionManager
 * @author Seen Technologies
 *
 */

import android.content.SharedPreferences

import com.seentechs.newtaxidriver.common.network.AppController

import javax.inject.Inject

/*****************************************************************
 * Session manager to set and get glopal values
 */
class SessionManager {
    lateinit @Inject
    var sharedPreferences: SharedPreferences

    var token: String?
        get() = sharedPreferences.getString("token", "")
        set(token) = sharedPreferences.edit().putString("token", token).apply()

    val accessToken: String?
        get() = sharedPreferences.getString("access_token", "")

    var firebaseCustomToken: String?
        get() = sharedPreferences.getString("firebaseCustomToken", "")
        set(firebaseCustomToken) = sharedPreferences.edit().putString("firebaseCustomToken", firebaseCustomToken).apply()

    var isFirebaseTokenUpdated: Boolean
        get() = sharedPreferences.getBoolean("isFirebaseTokenUpdated", false)
        set(isFirebaseTokenUpdated) = sharedPreferences.edit().putBoolean("isFirebaseTokenUpdated", isFirebaseTokenUpdated).apply()

    var googleMapKey: String?
        get() = sharedPreferences.getString("google_map_key", "")
        set(google_map_key) = sharedPreferences.edit().putString("google_map_key", google_map_key).apply()

    var driverStatus: String?
        get() = sharedPreferences.getString("driverstatus", "")
        set(access_token) = sharedPreferences.edit().putString("driverstatus", access_token).apply()

    var driverSignupStatus: String?
        get() = sharedPreferences.getString("driversignupstatus", "")
        set(access_token) = sharedPreferences.edit().putString("driversignupstatus", access_token).apply()

    var pushJson: String?
        get() = sharedPreferences.getString("json", "")
        set(PushJson) = sharedPreferences.edit().putString("json", PushJson).apply()

    var type: String?
        get() = sharedPreferences.getString("type", "")
        set(type) = sharedPreferences.edit().putString("type", type).apply()

    var deviceType: String?
        get() = sharedPreferences.getString("devicetype", "")
        set(devicetype) = sharedPreferences.edit().putString("devicetype", devicetype).apply()

    var totalDistance: Float
        get() = sharedPreferences.getFloat("total_distance", 0f)
        set(Latitude) = sharedPreferences.edit().putFloat("total_distance", Latitude).apply()

    var totalDistanceEverySec: Float
        get() = sharedPreferences.getFloat("totalDistanceEverySec", 0f)
        set(totalDistanceEvery10Sec) = sharedPreferences.edit().putFloat("totalDistanceEverySec", totalDistanceEvery10Sec).apply()

    var latitude: String?
        get() = sharedPreferences.getString("latitude", "")
        set(Latitude) = sharedPreferences.edit().putString("latitude", Latitude).apply()

    var longitude: String?
        get() = sharedPreferences.getString("longitude", "")
        set(longitude) = sharedPreferences.edit().putString("longitude", longitude).apply()

    var lastLongitude: String?
        get() = sharedPreferences.getString("lastLong", "")
        set(longitude) = sharedPreferences.edit().putString("lastLong", longitude).apply()

    var lastLatitude: String?
        get() = sharedPreferences.getString("lastLat", "")
        set(longitude) = sharedPreferences.edit().putString("lastLat", longitude).apply()

    var currentLongitude: String?
        get() = sharedPreferences.getString("currentlong", "")
        set(longitude) = sharedPreferences.edit().putString("currentlong", longitude).apply()

    var currentLatitude: String?
        get() = sharedPreferences.getString("currentlat", "")
        set(longitude) = sharedPreferences.edit().putString("currentlat", longitude).apply()

    var language: String?
        get() = sharedPreferences.getString("language", "")
        set(language) = sharedPreferences.edit().putString("language", language).apply()

    var languageCode: String?
        get() = sharedPreferences.getString("languagecode", "en")
        set(languagecode) = sharedPreferences.edit().putString("languagecode", languagecode).apply()

    var gender: String?
        get() = sharedPreferences.getString("gender", "")
        set(gender) = sharedPreferences.edit().putString("gender", gender).apply()


    var chatJson: String?
        get() = sharedPreferences.getString("chatJson", "")
        set(chatJson) = sharedPreferences.edit().putString("chatJson", chatJson).apply()

    var currency: String?
        get() = sharedPreferences.getString("currency", "")
        set(currency) = sharedPreferences.edit().putString("currency", currency).apply()

    var countryCurrencyType: String?
        get() = sharedPreferences.getString("setCountryCurrencyType", "")
        set(setCountryCurrencyType) = sharedPreferences.edit().putString("setCountryCurrencyType", setCountryCurrencyType).apply()

    var country: String?
        get() = sharedPreferences.getString("country", "")
        set(country) = sharedPreferences.edit().putString("country", country).apply()

    var firstName: String?
        get() = sharedPreferences.getString("firstname", "")
        set(firstName) = sharedPreferences.edit().putString("firstname", firstName).apply()

    var lastName: String?
        get() = sharedPreferences.getString("lastname", "")
        set(lastName) = sharedPreferences.edit().putString("lastname", lastName).apply()

    var password: String?
        get() = sharedPreferences.getString("password", "")
        set(password) = sharedPreferences.edit().putString("password", password).apply()

    var phoneNumber: String?
        get() = sharedPreferences.getString("phoneNumber", "")
        set(phoneNumber) = sharedPreferences.edit().putString("phoneNumber", phoneNumber).apply()

    var temporaryPhonenumber: String?
        get() = sharedPreferences.getString("TemporaryPhonenumber", "")
        set(phoneNumber) = sharedPreferences.edit().putString("TemporaryPhonenumber", phoneNumber).apply()

    var temporaryCountryCode: String?
        get() = sharedPreferences.getString("TemporaryCountryCode", "")
        set(countryCode) = sharedPreferences.edit().putString("TemporaryCountryCode", countryCode).apply()


    var countryCode: String?
        get() = sharedPreferences.getString("countryCode", "")
        set(countryCode) = sharedPreferences.edit().putString("countryCode", countryCode).apply()

    var deviceId: String?
        get() = sharedPreferences.getString("deviceId", "")
        set(deviceId) = sharedPreferences.edit().putString("deviceId", deviceId).apply()



    var requestId: String
        get() = sharedPreferences.getString("requestId", "").toString()
        set(requestId) = sharedPreferences.edit().putString("requestId", requestId).apply()

    var subTripId: String?
        get() = sharedPreferences.getString("subTripId", "")
        set(subTripId) = sharedPreferences.edit().putString("subTripId", subTripId).apply()

    var tripId: String?
        get() = sharedPreferences.getString("tripId", "")
        set(tripId) = sharedPreferences.edit().putString("tripId", tripId).apply()


    var poolIds: String?
        get() = sharedPreferences.getString("poolIds", "")
        set(poolIds) = sharedPreferences.edit().putString("poolIds", poolIds).apply()

    var tripStatus: String?
        get() = sharedPreferences.getString("tripStatus", "")
        set(tripStatus) = sharedPreferences.edit().putString("tripStatus", tripStatus).apply()

    var isDriverAndRiderAbleToChat: Boolean
        get() = sharedPreferences.getBoolean("setDriverAndRiderAbleToChat", false)
        set(status) = sharedPreferences.edit().putBoolean("setDriverAndRiderAbleToChat", status).apply()


    var subTripStatus: String?
        get() = sharedPreferences.getString("SubTripStatus", "")
        set(SubTripStatus) = sharedPreferences.edit().putString("SubTripStatus", SubTripStatus).apply()

    var vehicle_id: String?
        get() = sharedPreferences.getString("vehicle_id", "")
        set(vehicle_id) = sharedPreferences.edit().putString("vehicle_id", vehicle_id).apply()


    var doc1: String?
        get() = sharedPreferences.getString("doc1", "")
        set(doc1) = sharedPreferences.edit().putString("doc1", doc1).apply()

    var doc2: String?
        get() = sharedPreferences.getString("doc2", "")
        set(doc2) = sharedPreferences.edit().putString("doc2", doc2).apply()

    var doc3: String?
        get() = sharedPreferences.getString("doc3", "")
        set(doc3) = sharedPreferences.edit().putString("doc3", doc3).apply()

    var doc4: String?
        get() = sharedPreferences.getString("doc4", "")
        set(doc4) = sharedPreferences.edit().putString("doc4", doc4).apply()

    var doc5: String?
        get() = sharedPreferences.getString("doc5", "")
        set(doc5) = sharedPreferences.edit().putString("doc5", doc5).apply()

    var currencyCode: String?
        get() = sharedPreferences.getString("currencyCode", "")
        set(currencyCode) = sharedPreferences.edit().putString("currencyCode", currencyCode).apply()

    var currencySymbol: String?
        get() = sharedPreferences.getString("currencysymbol", "")
        set(currencySymbol) = sharedPreferences.edit().putString("currencysymbol", currencySymbol).apply()

    var userId: String?
        get() = sharedPreferences.getString("UserId", "")
        set(UserId) = sharedPreferences.edit().putString("UserId", UserId).apply()


    var city: String?
        get() = sharedPreferences.getString("city", "")
        set(city) = sharedPreferences.edit().putString("city", city).apply()

    var profileDetail: String?
        get() = sharedPreferences.getString("profilearratdetail", "")
        set(profilearratdetail) = sharedPreferences.edit().putString("profilearratdetail", profilearratdetail).apply()


    var paymentMethodkey: String
        get() = sharedPreferences.getString("paymentMethodkey", "").toString()
        set(paymentMethodkey) = sharedPreferences.edit().putString("paymentMethodkey", paymentMethodkey).apply()


    var paymentMethod: String?
        get() = sharedPreferences.getString("paymentMethod", "")
        set(paymentMethod) = sharedPreferences.edit().putString("paymentMethod", paymentMethod).apply()

    var paymentMethodImage: String
        get() = sharedPreferences.getString("paymentMethodImage", "").toString()
        set(paymentMethodImage) = sharedPreferences.edit().putString("paymentMethodImage", paymentMethodImage).apply()

    var cardValue: String?
        get() = sharedPreferences.getString("cardValue", "")
        set(cardValue) = sharedPreferences.edit().putString("cardValue", cardValue).apply()

    var cardBrand: String?
        get() = sharedPreferences.getString("cardBrand", "")
        set(cardBrand) = sharedPreferences.edit().putString("cardBrand", cardBrand).apply()

    var carType: String?
        get() = sharedPreferences.getString("CarType", "")
        set(CarType) = sharedPreferences.edit().putString("CarType", CarType).apply()

    var oweAmount: String?
        get() = sharedPreferences.getString("oweAmount", "")
        set(oweAmount) = sharedPreferences.edit().putString("oweAmount", oweAmount).apply()

    var driverReferral: String?
        get() = sharedPreferences.getString("driverReferral", "")
        set(driverReferral) = sharedPreferences.edit().putString("driverReferral", driverReferral).apply()

    var paypalEmail: String?
        get() = sharedPreferences.getString("PaypalEmail", "")
        set(PaypalEmail) = sharedPreferences.edit().putString("PaypalEmail", PaypalEmail).apply()

    var docCount: String?
        get() = sharedPreferences.getString("imagecount", "")
        set(imagecount) = sharedPreferences.edit().putString("imagecount", imagecount).apply()

    var isRegister: Boolean = false
        get() = sharedPreferences.getBoolean("IsRegister", false)

    var riderProfilePic: String?
        get() = sharedPreferences.getString("riderProfilePic", "")
        set(url) = sharedPreferences.edit().putString("riderProfilePic", url).apply()

    var riderRating: String?
        get() = sharedPreferences.getString("ratingValue", "")
        set(ratingvalue) = sharedPreferences.edit().putString("ratingValue", ratingvalue).apply()

    var riderName: String?
        get() = sharedPreferences.getString("riderName", "")
        set(drivername) = sharedPreferences.edit().putString("riderName", drivername).apply()

    var riderId: String
        get() = sharedPreferences.getString("riderId", "").toString()
        set(riderId) = sharedPreferences.edit().putString("riderId", riderId).apply()


    var countryName2: String?
        get() = sharedPreferences.getString("countryname2", "")
        set(currencyName2) = sharedPreferences.edit().putString("countryname2", currencyName2).apply()

    var countryName: String?
        get() = sharedPreferences.getString("countryname", "")
        set(currencyName) = sharedPreferences.edit().putString("countryname", currencyName).apply()

    var currencyName2: String?
        get() = sharedPreferences.getString("currencyname2", "")
        set(currencyName2) = sharedPreferences.edit().putString("currencyname2", currencyName2).apply()

    var stripeCountryCode: String?
        get() = sharedPreferences.getString("StripeCountryCode", "")
        set(stripeCountryCode) = sharedPreferences.edit().putString("StripeCountryCode", stripeCountryCode).apply()

    var payPalCountryCode: String?
        get() = sharedPreferences.getString("paypalcountrycode", "")
        set(payPalCountryCode) = sharedPreferences.edit().putString("paypalcountrycode", payPalCountryCode).apply()

    var bookingType: String?
        get() = sharedPreferences.getString("bookingType", "")
        set(bookingType) = sharedPreferences.edit().putString("bookingType", bookingType).apply()

    var dialogMessage: String?
        get() = sharedPreferences.getString("DialogMessage", "")
        set(DialogMessage) = sharedPreferences.edit().putString("DialogMessage", DialogMessage).apply()

    var userType: String?
        get() = sharedPreferences.getString("UserType", "")
        set(UserType) = sharedPreferences.edit().putString("UserType", UserType).apply()


    var documentId: String?
        get() = sharedPreferences.getString("documentId", "")
        set(UserType) = sharedPreferences.edit().putString("documentId", UserType).apply()


    var vehicleId: String?
        get() = sharedPreferences.getString("vehicleId", "")
        set(vehicleId) = sharedPreferences.edit().putString("vehicleId", vehicleId).apply()

    var walletCard: Int
        get() = sharedPreferences.getInt("walletCard", 0)
        set(walletCard) = sharedPreferences.edit().putInt("walletCard", walletCard).apply()

    var isExtraFeeCollectable: Boolean
        get() = sharedPreferences.getBoolean("extra_fee", false)
        set(status) = sharedPreferences.edit().putBoolean("extra_fee", status).apply()

    val isReferralOptionEnabled: Boolean
        get() = sharedPreferences.getBoolean("safkey64", true)

    var isHeatMapChecked: Boolean
        get() = sharedPreferences.getBoolean("heatMap", false)
        set(HeatMapChecked) = sharedPreferences.edit().putBoolean("heatMap", HeatMapChecked).apply()

    var sinchKey: String?
        get() = sharedPreferences.getString("weasqr", "")
        set(sinchKey) = sharedPreferences.edit().putString("weasqr", sinchKey).apply()

    var sinchSecret: String?
        get() = sharedPreferences.getString("udueuw", "")
        set(sinchSecret) = sharedPreferences.edit().putString("udueuw", sinchSecret).apply()

    var offlineDistance: Float
        get() = sharedPreferences.getFloat("offline_distance", 0f)
        set(offlineDistance) = sharedPreferences.edit().putFloat("offline_distance", offlineDistance).apply()

    var onlineDistance: Float
        get() = sharedPreferences.getFloat("online_distance", 0f)
        set(onlineDistance) = sharedPreferences.edit().putFloat("online_distance", onlineDistance).apply()

    var beginLatitude: String?
        get() = sharedPreferences.getString("beginLatitude", "")
        set(Latitude) = sharedPreferences.edit().putString("beginLatitude", Latitude).apply()

    var isTrip: Boolean
        get() = sharedPreferences.getBoolean("isTrip", false)
        set(isTrip) = sharedPreferences.edit().putBoolean("isTrip", isTrip).apply()

    var isPool: Boolean
        get() = sharedPreferences.getBoolean("isPool", true)
        set(isPool) = sharedPreferences.edit().putBoolean("isPool", isPool).apply()

    var beginLongitude: String?
        get() = sharedPreferences.getString("beginLongitude", "")
        set(longitude) = sharedPreferences.edit().putString("beginLongitude", longitude).apply()


    var brainTreeClientToken: String?
        get() = sharedPreferences.getString("BrainTreeClientToken", "")
        set(BrainTreeClientToken) = sharedPreferences.edit().putString("BrainTreeClientToken", BrainTreeClientToken).apply()

    init {
        AppController.getAppComponent().inject(this)
    }


    fun clearToken() {
        sharedPreferences.edit().putString("token", "").apply()
    }

    fun clearAll() {
        sharedPreferences.edit().clear().apply()
        //setType("");
    }

    fun setAcesssToken(access_token: String) {
        sharedPreferences.edit().putString("access_token", access_token).apply()
    }

    fun getemail(): String? {
        return sharedPreferences.getString("email", "")
    }

    fun clearTripID() {
        sharedPreferences.edit().remove("tripId").apply()
    }

    fun clearTripStatus() {
        sharedPreferences.edit().remove("tripStatus").apply()
    }

    fun setIsrequest(isrequest: Boolean) {
        sharedPreferences.edit().putBoolean("isrequest", isrequest).apply()
    }

    fun setIsTrip(istrip: Boolean) {
        sharedPreferences.edit().putBoolean("istrip", istrip).apply()
    }


    fun getisEdit(): Boolean {
        return sharedPreferences.getBoolean("isEdit", false)
    }

    fun setisEdit(isEdit: Boolean) {
        sharedPreferences.edit().putBoolean("isEdit", isEdit).apply()
    }

    fun clearRiderNameRatingAndProfilePicture() {
        val editor = sharedPreferences.edit()
        editor.remove("ratingValue")
        editor.remove("riderName")
        editor.remove("riderProfilePic")
        editor.apply()
    }

    fun setReferralOpiton(referralStatus: Boolean) {
        sharedPreferences.edit().putBoolean("safkey64", referralStatus).apply()
    }


    var stripePublishKey: String
        get() = sharedPreferences.getString("StripePublishKey", "").toString()
        set(stripePublishKey) =sharedPreferences.edit().putString("StripePublishKey",stripePublishKey).apply()


    var paypal_mode: Int
        get() = sharedPreferences.getInt("paypal_mode", 0)
        set(paypal_mode) =sharedPreferences.edit().putInt("paypal_mode",paypal_mode).apply()

    var paypal_app_id: String
        get() = sharedPreferences.getString("paypal_app_id", "").toString()
        set(paypal_app_id) =sharedPreferences.edit().putString("paypal_app_id",paypal_app_id).apply()

    var braintree_public_key: String
        get() = sharedPreferences.getString("braintree_public_key", "").toString()
        set(braintree_public_key) =sharedPreferences.edit().putString("braintree_public_key",braintree_public_key).apply()

    var braintree_env: String
        get() = sharedPreferences.getString("braintree_env", "").toString()
        set(braintree_env) =sharedPreferences.edit().putString("braintree_env",braintree_env).apply()

    var gateway_type: String
        get() = sharedPreferences.getString("gateway_type", "").toString()
        set(gateway_type) =sharedPreferences.edit().putString("gateway_type",gateway_type).apply()

    var isEndTripCalled: Boolean
        get() = sharedPreferences.getBoolean("isEndTripCalled", false)
        set(isEndTripCalled) = sharedPreferences.edit().putBoolean("isEndTripCalled", isEndTripCalled).apply()

    var payementModeWebView: Boolean?
        get() = sharedPreferences.getBoolean("payementModeWebView", false)
        set(payementModeWebView) =sharedPreferences.edit().putBoolean("payementModeWebView",payementModeWebView!!).apply()

    var isGeoFireUpdatedWhenOnline: Boolean?
        get() = sharedPreferences.getBoolean("isGeoFireUpdatedWhenOnline", false)
        set(isGeoFireUpdatedWhenOnline) =sharedPreferences.edit().putBoolean("isGeoFireUpdatedWhenOnline",isGeoFireUpdatedWhenOnline!!).apply()

    var isLocationUpdatedForOneTime: Boolean?
        get() = sharedPreferences.getBoolean("isLocationUpdatedForOneTime", false)
        set(isLocationUpdatedForOneTime) =sharedPreferences.edit().putBoolean("isLocationUpdatedForOneTime",isLocationUpdatedForOneTime!!).apply()

    var notificationID: String
        get() = sharedPreferences.getString("notificationID", "").toString()
        set(notificationID) = sharedPreferences.edit().putString("notificationID", notificationID).apply()

}