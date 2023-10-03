package com.seentechs.newtaxidriver.home.payouts.payout_model_classed

/**
 *
 * @package     com.seentechs.newtaxidriver
 * @subpackage  model
 * @category    Makent_model
 * @author      Seen Technologies
 *
 */

import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable

import java.io.Serializable

class Makent_model : Serializable {
    var explore_room_image: String? = null
    var roomid: String? = null
    var roomname: String? = null
    var roomprice: String? = null
    var roomrating: String? = null
    var roomreview: String? = null
    var roomiswishlist: String? = null
    var roomcountryname: String? = null
    var currencycode: String? = null
    var currencysymbol: String? = null
    var roomlat: String? = null
    var roomlong: String? = null
    var roomtype: String? = null
    var instantBook: String? = null
    var wishlistImage: String? = null
    var wishlistId: String? = null
    var wishlistName: String? = null
    var wishlistPrivacy: String? = null
    var tripsType: String? = null
    var tripsTypeCount: String? = null
    var countryId: String? = null
    private var id: String? = null
    var amenities: String? = null
    var amenitiesId: String? = null
    var name: String? = null
    var countryName: String?=null
    var genderType: String? = null
    var shareName: String? = null
        private set
    var amenitiesSelected: Boolean = false
    var amenities_image: Char = ' '
    var reviewUserName: String? = null
    var reviewUserImage: String? = null
    var reviewDate: String? = null
    var reviewMessage: String? = null
    lateinit  var reservationId: String
    lateinit var serviceFee: String
    lateinit var hostFee: String
    lateinit var hostId: String
    lateinit var roomId: String
    lateinit var tripStatus: String
    //Detailed list space

    lateinit var specialofferid: String
    lateinit var specialofferstatus: String
    lateinit var bookingStatus: String
    lateinit var tripDate: String
    lateinit var roomName: String
    lateinit var roomLocation: String
    lateinit var hostUserName: String
    lateinit var hostThumbImage: String
    lateinit var lastMessage: String
    lateinit var isMessageRead: String
    lateinit var totalCost: String
    lateinit var checkInTime: String
    lateinit var checkOutTime: String
    lateinit var requestUserId: String
    lateinit var shareIcon: Drawable
    lateinit var shareItem: ResolveInfo
    var coupon_amount: String? = null
    var host_penalty_amount: String? = null


    lateinit var type: String

    lateinit var days: String
    lateinit var percentage: String

    lateinit var discountId: String
    lateinit var discountType: String
    lateinit var period: String
    lateinit var discount: String
    lateinit var availableId: String
    lateinit var availableType: String
    lateinit var minimumStay: String
    lateinit var maximumStay: String
    lateinit var startDate: String
    lateinit var endDate: String
    lateinit var during: String
    lateinit var text: String
    lateinit var start_date_formatted: String
    lateinit var end_date_formatted: String

     var countryCode: String?=null


    constructor() {

    }

    constructor(type: String) {
        this.type = type
    }

    constructor(type: String, Explore_room_image: String, roomid: String, roomname: String, roomprice: String, roomrating: String, roomreview: String, roomiswishlist: String, roomcountryname: String, currencycode: String, currencysymbol: String,
                id: String, amenitiesselected: Boolean, amenities_image: Char, amenities: String, amenitiesid: String, name: String, countryname: String, countryid: String, gendertype: String, sharename: String, shareicon: Drawable, shareitem: ResolveInfo,
                wishlistImage: String, wishlistId: String, wishlistName: String, wishlistPrivacy: String, tripstypecount: String, tripstype: String, roomlat: String, roomlong: String, roomtype: String, instantbook: String) {
        this.type = type
        this.explore_room_image = Explore_room_image
        this.roomid = roomid
        this.roomname = roomname
        this.roomprice = roomprice
        this.roomrating = roomrating
        this.roomreview = roomreview
        this.roomiswishlist = roomiswishlist
        this.roomcountryname = roomcountryname
        this.currencycode = currencycode
        this.currencysymbol = currencysymbol

        this.roomtype = roomtype
        this.roomlat = roomlat
        this.roomlong = roomlong

        this.id = id
        this.amenities_image = amenities_image
        this.amenitiesId = amenitiesid
        this.amenities = amenities
        this.amenitiesSelected = amenitiesselected
        this.name = name
        this.countryName = countryname
        this.genderType = gendertype
        this.shareName = sharename
        this.shareIcon = shareicon
        this.shareItem = shareitem
        this.wishlistImage = wishlistImage
        this.wishlistId = wishlistId
        this.wishlistName = wishlistName
        this.wishlistPrivacy = wishlistPrivacy
        this.tripsTypeCount = tripstypecount
        this.tripsType = tripstype
        this.instantBook = instantbook
    }

    constructor(type: String, review_user_name: String, review_user_image: String, review_date: String, review_message: String) {
        this.type = type
        this.reviewUserName = review_user_name
        this.reviewUserImage = review_user_image
        this.reviewDate = review_date
        this.reviewMessage = review_message
    }

    constructor(discount_id: String, discount_type: String, period: String, discount: String) {
        this.discountId = discount_id
        this.discountType = discount_type
        this.period = period
        this.discount = discount
    }

    constructor(days: String, percentage: String) {
        this.days = days
        this.percentage = percentage
    }

    constructor(available_id: String, available_type: String, minimum_stay: String, maximum_stay: String, start_date: String, end_date: String, during: String) {
        this.availableId = available_id
        this.availableType = available_type
        this.minimumStay = minimum_stay
        this.maximumStay = maximum_stay
        this.startDate = start_date
        this.endDate = end_date
        this.during = during
    }

    // Trips Details
    constructor(reservation_id: String, hostid: String, room_id: String, trip_status: String, booking_status: String, trip_date: String,
                room_name: String, room_location: String, host_user_name: String, host_thumb_image: String, specialofferstatus: String, specialofferid: String) {
        this.reservationId = reservation_id
        this.hostId = hostid
        this.roomId = room_id
        this.tripStatus = trip_status
        this.bookingStatus = booking_status
        this.tripDate = trip_date
        this.roomName = room_name
        this.roomLocation = room_location
        this.hostUserName = host_user_name
        this.hostThumbImage = host_thumb_image
        this.specialofferstatus = specialofferstatus
        this.specialofferid = specialofferid
    }

    //InBox Details
    constructor(reservation_id: String, room_id: String, message_status: String,
                room_name: String, room_location: String, host_user_name: String, host_thumb_image: String, message: String, is_message_read: String, total_cost: String, check_in_time: String, check_out_time: String,
                requestuserid: String, host_fee: String, service_fee: String) {
        this.reservationId = reservation_id
        this.roomId = room_id
        this.tripStatus = message_status
        this.roomName = room_name
        this.roomLocation = room_location
        this.hostUserName = host_user_name
        this.hostThumbImage = host_thumb_image
        this.lastMessage = message
        this.isMessageRead = is_message_read
        this.totalCost = total_cost
        this.serviceFee = service_fee
        this.hostFee = host_fee
        this.checkOutTime = check_out_time
        this.checkInTime = check_in_time
        this.requestUserId = requestuserid

    }

    fun getid(): String? {
        return id
    }

    fun setid(id: String) {
        this.id = id
    }

    fun setSharename(sharename: String) {
        this.shareName = sharename
    }
}
