package com.seentechs.newtaxidriver.home.interfaces

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import java.util.*

/* ************************************************************
                      ApiService
    Contains all api service call methods
*************************************************************** */

interface ApiService {

    // login numbervalidation

    // Upload Documents image
    @POST("document_upload")
    fun uploadDocumentImage(@Body RequestBody: RequestBody, @Query("token") token: String): Call<ResponseBody>

    @POST("update_document")
    fun updateDocument(@Body RequestBody: RequestBody, @Query("token") token: String): Call<ResponseBody>

    // Upload Profile image
    @POST("upload_profile_image")
    fun uploadProfileImage(@Body RequestBody: RequestBody, @Query("token") token: String): Call<ResponseBody>


    // Update location with lat,lng
    @GET("send_message")
    fun updateChat(@QueryMap hashMap: HashMap<String, String>): Call<ResponseBody>

    //Login
    @GET("login")
    fun login(@Query("mobile_number") mobilenumber: String, @Query("user_type") usertype: String, @Query("country_code") countrycode: String, @Query("password") password: String, @Query("device_id") deviceid: String, @Query("device_type") devicetype: String, @Query("language") language: String): Call<ResponseBody>

    @GET("get_referral_details")
    fun getReferralDetails(@Query("token") token: String): Call<ResponseBody>

    //Login
    @GET("vehicle_details")
    fun vehicleDetails(@Query("vehicle_id") vehicleid: Long, @Query("vehicleName") vehiclename: String, @Query("vehicle_type") vehicletype: String, @Query("vehicle_number") vehiclenumber: String, @Query("token") token: String): Call<ResponseBody>

    //Forgot password
    @GET("forgotpassword")
    fun forgotpassword(@Query("mobile_number") mobile_number: String, @Query("user_type") user_type: String, @Query("country_code") country_code: String, @Query("password") password: String, @Query("device_type") device_type: String, @Query("device_id") device_id: String, @Query("language") language: String): Call<ResponseBody>

    //Number Validation
    @GET("numbervalidation")
    fun numberValidation(@Query("user_type") type: String, @Query("mobile_number") mobilenumber: String, @Query("country_code") countrycode: String, @Query("forgotpassword") forgotpassword: String, @Query("language") language: String): Call<ResponseBody>

    @GET("add_payout")
    fun addPayout(@Query("email_id") emailId: String, @Query("user_type") userType: String, @Query("token") token: String): Call<ResponseBody>


    //Cancel trip
    @GET("cancel_trip")
    fun cancelTrip(@Query("user_type") type: String, @Query("cancel_reason_id") cancel_reason: String, @Query("cancel_comments") cancel_comments: String, @Query("trip_id") trip_id: String, @Query("token") token: String): Call<ResponseBody>


    // Cancel Reason
    @GET("cancel_reasons")
    fun cancelReasons(@Query("token") token: String): Call<ResponseBody>

    //Forgot password
    @GET("accept_request")
    fun acceptRequest(@Query("user_type") type: String, @Query("request_id") request_id: String, @Query("status") status: String, @Query("token") token: String,@Query("timezone") timezone:String): Call<ResponseBody>


    //Forgot password
    @GET("vehicle_descriptions")
    fun getVehicleDescription(@Query("token") token: String): Call<ResponseBody>


    @FormUrlEncoded
    @POST("update_vehicle")
    fun updateVehicle(@FieldMap hashMap: LinkedHashMap<String, String>): Call<ResponseBody>

    //Confirm Arrival
    @GET("cash_collected")
    fun cashCollected(@Query("trip_id") trip_id: String, @Query("token") token: String): Call<ResponseBody>


    //Default vehicle
    @GET("update_default_vehicle")
    fun updateDefaultVehicle(@Query("vehicle_id") vehicle_id: String, @Query("token") token: String): Call<ResponseBody>

    //Confirm Arrival
    @GET("arive_now")
    fun ariveNow(@Query("trip_id") trip_id: String, @Query("token") token: String): Call<ResponseBody>

    //Begin Trip
    @GET("begin_trip")
    fun beginTrip(@Query("trip_id") trip_id: String, @Query("begin_latitude") begin_latitude: String, @Query("begin_longitude") begin_longitude: String, @Query("token") token: String): Call<ResponseBody>

    //End Trip
    @POST("end_trip")
    fun endTrip(@Body RequestBody: RequestBody): Call<ResponseBody>

    //Trip Rating
    @GET("trip_rating")
    fun tripRating(@Query("trip_id") trip_id: String, @Query("rating") rating: String,
                   @Query("rating_comments") rating_comments: String, @Query("user_type") user_type: String, @Query("token") token: String): Call<ResponseBody>


    // Update location with lat,lng and driverStatus
    @GET("updatelocation")
    fun updateLocation(@QueryMap hashMap: HashMap<String, String>): Call<ResponseBody>


    @GET("update_device")
    fun updateDevice(@QueryMap hashMap: HashMap<String, String>): Call<ResponseBody>


    // driverStatus Check
    @GET("check_status")
    fun updateCheckStatus(@QueryMap hashMap: HashMap<String, String>): Call<ResponseBody>

    @GET("earning_chart")
    fun updateEarningChart(@QueryMap hashMap: HashMap<String, String>): Call<ResponseBody>

    @GET("driver_rating")
    fun updateDriverRating(@QueryMap hashMap: HashMap<String, String>): Call<ResponseBody>

    @GET("rider_feedback")
    fun updateRiderFeedBack(@QueryMap hashMap: HashMap<String, String>): Call<ResponseBody>


    //Number Validation
    @GET("register")
    fun registerOtp(@Query("user_type") type: String, @Query("mobile_number") mobilenumber: String, @Query("country_code") countrycode: String, @Query("email_id") emailid: String, @Query("first_name") first_name: String, @Query("last_name") last_name: String, @Query("password") password: String, @Query("city") city: String, @Query("device_id") device_id: String, @Query("device_type") device_type: String, @Query("language") languageCode: String, @Query("referral_code") referral: String?,@Query("auth_type") authType : String,@Query("auth_id") authId : String,@Query("gender") gender : String): Call<ResponseBody>


    //Driver Profile
    @GET("get_driver_profile")
    fun getDriverProfile(@Query("token") token: String): Call<ResponseBody>


    //Driver Profile
    @GET("get_payout_list")
    fun getPayoutDetails(@QueryMap hashMap: HashMap<String, String>): Call<ResponseBody>

    //Currency list
    @GET("currency_list")
    fun getCurrency(@Query("token") token: String): Call<ResponseBody>

    //language Update
    @GET("language")
    fun language(@Query("language") languageCode: String, @Query("token") token: String): Call<ResponseBody>

    // Update User Currency
    @GET("update_user_currency")
    fun updateCurrency(@Query("currency_code") currencyCode: String, @Query("token") token: String): Call<ResponseBody>

    @GET("update_driver_profile")
    fun updateDriverProfile(@QueryMap hashMap: LinkedHashMap<String, String>): Call<ResponseBody>

    //Upload payout
    @POST("update_payout_preference")
    fun UpdatePayoutDetails(@QueryMap hashMap: HashMap<String, String>): Call<ResponseBody>

    //Sign out
    @GET("logout")
    fun logout(@Query("user_type") type: String, @Query("token") token: String): Call<ResponseBody>

    //Payout Details
    @GET("payout_details")
    fun payoutDetails(@Query("token") token: String): Call<ResponseBody>

    //Get Country List
    @GET("country_list")
    fun getCountryList(@Query("token") token: String): Call<ResponseBody>

    //List of Stripe Supported Countries
    @GET("stripe_supported_country_list")
    fun stripeSupportedCountry(@Query("token")  token: String): Call<ResponseBody>

    //Get pre_payment
    @GET("payout_changes")
    fun payoutChanges(@Query("token") token: String, @Query("payout_id") payout_id: String, @Query("type") type: String): Call<ResponseBody>

    // Add stripe payout preference
    @POST("update_payout_preference")
    fun uploadStripe(@Body RequestBody: RequestBody, @Query("token") token: String): Call<ResponseBody>

    //get Additional fee reasons
    @GET("toll_reasons")
    fun getToll_reasons(@Query("token") tokenx: String): Call<ResponseBody>

    // get Trip invoice Details  Rider
    @GET("get_invoice")
    fun getInvoice(@Query("token") token: String, @Query("trip_id") TripId: String, @Query("user_type") userType: String): Call<ResponseBody>

    //Force Update API
    @GET("check_version")
    fun checkVersion(@Query("version") code: String, @Query("user_type") type: String, @Query("device_type") deviceType: String): Call<ResponseBody>

    // Get Card
    @GET("get_card_details")
    fun viewCard(@Query("token") token: String): Call<ResponseBody>


    // Add to cart
    @GET("add_card_details")
    fun addCard(@Query("intent id") stripeId: String, @Query("token") token: String): Call<ResponseBody>


    // GET PAYMENTMETHODLIST
    @FormUrlEncoded
    @POST("get_payment_list")
    fun getPaymentMethodlist(@Field("token") token: String, @Field("is_wallet")isWallet:Int): Call<ResponseBody>

    @FormUrlEncoded
    @POST("delete_vehicle")
    fun deleteVehicle(@Field("token") token: String, @Field("id")id:String): Call<ResponseBody>

    // Send OwnAmount
    @FormUrlEncoded
    @POST("pay_to_admin")
    fun payToAdmin(@Field("amount") amount: String, @Field("applied_referral_amount") applyRefer: String, @Field("token") token: String): Call<ResponseBody>

    // Send OwnAmount
    @FormUrlEncoded
    @POST("pay_to_admin")
    fun payToAdmin(@FieldMap walletParams: LinkedHashMap<String, String>): Call<ResponseBody>


    //Daily Statement
    @GET("daily_statement")
    fun dailyStatement(@Query("token") token: String, @Query("date") date: String, @Query("timezone") timeZone: String,@Query("page") page: String): Call<ResponseBody>

    //Weekly Payout
    @GET("weekly_statement")
    fun weeklyStatement(@Query("token") token: String, @Query("user_type") userType: String, @Query("date") startDate: String): Call<ResponseBody>

    //Weekly Trip Payout
    @GET("weekly_trip")
    fun weeklyTripStatement(@Query("token") token: String,@Query("page") page: String): Call<ResponseBody>

    //Check user Mobile Number
    @GET("numbervalidation")
    fun numbervalidation(@Query("mobile_number") mobile_number: String, @Query("country_code") country_code: String, @Query("user_type") user_type: String, @Query("language") language: String, @Query("forgotpassword") forgotpassword: String): Call<ResponseBody>

    //Heat map
    @GET("heat_map")
    fun heatMap(@Query("token") token: String, @Query("timezone") timeZone: String): Call<ResponseBody>

    //Common Data
    @POST("common_data")
    fun commonData(@Query("token") token: String): Call<ResponseBody>

    // get profile picture and name of opponent caller for sinch call page
    @GET("get_caller_detail")
    fun getCallerDetail(@Query("token") token: String, @Query("user_id") userID: String, @Query("send_push_notification") pushStatus: String): Call<ResponseBody>

    // Get given trip details
    @GET("get_trip_details")
    fun getTripDetails(@Query("token") token: String, @Query("trip_id") trip_id: String): Call<ResponseBody>

    // Get completed trip list
    @GET("get_completed_trips")
    fun getPastTrips(@Query("token") token: String, @Query("page") page: String): Call<ResponseBody>

    //pending
    @GET("get_pending_trips")
    fun getPendingTrips(@Query("token") token: String, @Query("page") page: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("currency_conversion")
    fun currencyConversion(@Field("amount") amount: String, @Field("token") token: String, @Field("payment_type") paymentType:  String): Call<ResponseBody>

    @GET("otp_verification")
    fun otpVerification(@QueryMap hashMap: HashMap<String, String>): Call<ResponseBody>

    // Check for send request 
}


