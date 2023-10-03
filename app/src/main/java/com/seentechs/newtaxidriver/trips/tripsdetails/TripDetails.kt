package com.seentechs.newtaxidriver.trips.tripsdetails

/**
 * @package com.seentechs.newtaxidriver.trips.tripsdetails
 * @subpackage tripsdetails model
 * @category TripsDetails
 * @author Seen Technologies
 *
 */

import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.database.Sqlite
import com.seentechs.newtaxidriver.common.helper.Constants
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonKeys
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.CommonMethods.Companion.showInternetNotAvailableForStoredDataViewer
import com.seentechs.newtaxidriver.common.util.CommonMethods.Companion.showNoInternetAlert
import com.seentechs.newtaxidriver.common.util.Enums.REQ_TRIP_DETAILS
import com.seentechs.newtaxidriver.common.util.RequestCallback
import com.seentechs.newtaxidriver.common.views.CommonActivity
import com.seentechs.newtaxidriver.home.datamodel.InvoiceModel
import com.seentechs.newtaxidriver.home.datamodel.TripDetailsModel
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import com.seentechs.newtaxidriver.trips.rating.PriceRecycleAdapter
import com.seentechs.newtaxidriver.trips.rating.Riderrating
import kotlinx.android.synthetic.main.app_activity_trip_details.*
import org.json.JSONException
import java.lang.Double
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/* ************************************************************
                TripsDetails
Its used to show all the trips details information to view the page
*************************************************************** */
class TripDetails : CommonActivity(), ServiceListener {

    private var isViewUpdatedWithLocalDB: Boolean = false
    lateinit var tripId: String

    @Inject
    lateinit var sessionManager: SessionManager
    var dialog: AlertDialog? = null

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var customDialog: CustomDialog

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var dbHelper: Sqlite

   /* @BindView(R.id.adminamountlayout)
    lateinit var adminamountlayout: RelativeLayout
*/
  /*  @BindView(R.id.oweamountlayout)
    lateinit var oweamountlayout: RelativeLayout*/

 /*   @BindView(R.id.driverpayoutlayout)
    lateinit var driverpayoutlayout: RelativeLayout

    @BindView(R.id.cashcollectamountlayout)
    lateinit var cashcollectamountlayout: RelativeLayout*/
/*
    @BindView(R.id.basefare_amount)
    lateinit var basefare_amount: TextView

    @BindView(R.id.distance_fare)
    lateinit var distance_fare: TextView

    @BindView(R.id.time_fare)
    lateinit var time_fare: TextView

    @BindView(R.id.fee)
    lateinit var fee: TextView

    @BindView(R.id.totalamount)
    lateinit var totalamount: TextView

    @BindView(R.id.total_payouts)
    lateinit var total_payouts: TextView

    @BindView(R.id.cashcollectamount)
    lateinit var cashcollectamount: TextView

    @BindView(R.id.cashcollectamount_txt)
    lateinit var cashcollectamount_txt: TextView

    @BindView(R.id.oweamount)
    lateinit var oweamount: TextView

    @BindView(R.id.driverpayout)
    lateinit var driverpayout: TextView

    @BindView(R.id.adminamount)
    lateinit var adminamount: TextView*/

    @BindView(R.id.trip_amount)
    lateinit var trip_amount: TextView

    @BindView(R.id.trip_km)
    lateinit var trip_km: TextView

    @BindView(R.id.trip_duration)
    lateinit var trip_duration: TextView

    @BindView(R.id.tv_drop_address)
    lateinit var drop_address: TextView

    @BindView(R.id.tv_pick_Address)
    lateinit var pickup_address: TextView

    @BindView(R.id.seatcount)
    lateinit var seatcount: TextView

    @BindView(R.id.trip_date)
    lateinit var trip_date: TextView

    @BindView(R.id.route_image)
    lateinit var route_image: ImageView
    var payment_method: String? = null
    var currencysymbol: String? = null

    @BindView(R.id.rvPrice)
    lateinit var recyclerView: RecyclerView
    internal var invoiceModels = ArrayList<InvoiceModel>()

    @BindView(R.id.rlt_mapview)
    lateinit var staticmapview: RelativeLayout

    @BindView(R.id.btnrate)
    lateinit var btnrate: Button

    @BindView(R.id.carname)
    lateinit var carname : TextView

    @BindView(R.id.tv_tripstatus)
    lateinit var  tripstatus :TextView
    @BindView(R.id.tv_tripid)
    lateinit var tvTripid : TextView

    /*@BindView(R.id.rlt_mapview)
    lateinit var rltImageView: RelativeLayout*/

   /* @BindView(R.id.basrfarelayout)
    lateinit var farelayout: RelativeLayout*/
    var tripDetailsModels: TripDetailsModel? = null

    @OnClick(R.id.back)
    fun backPressed() {
        onBackPressed()
    }

    @OnClick(R.id.btnrate)
    fun rate() {
        sessionManager.tripId = tripDetailsModels?.riderDetails?.get(0)?.tripId!!.toString()
        val rating = Intent(this, Riderrating::class.java)
        rating.putExtra("imgprofile", tripDetailsModels?.riderDetails?.get(0)?.profileImage)
        rating.putExtra("back", 1)
        startActivity(rating)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_activity_trip_details)
        AppController.getAppComponent().inject(this)
        ButterKnife.bind(this)

        /**Commmon Header Text View */
        commonMethods.setheaderText(resources.getString(R.string.tripsdetails),common_header)
      /*  drop_address.visibility = View.GONE
        pickup_address.visibility = View.GONE*/
        currencysymbol = sessionManager.currencySymbol
        val intent = intent
        tripId = intent.getStringExtra("tripId").toString()
      //  farelayout.visibility = View.GONE

        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.setLayoutManager(layoutManager)

        loadTripDetails()
    }

    private fun loadTripDetails() {

        val allHomeDataCursor: Cursor = dbHelper.getDocument(Constants.DB_KEY_TRIP_DETAILS + tripId)
        if (allHomeDataCursor.moveToFirst()) {
            isViewUpdatedWithLocalDB = true
            //tvOfflineAnnouncement.setVisibility(View.VISIBLE)
            try {
                onSuccessTripDetail(allHomeDataCursor.getString(0))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            followProcedureForNoDataPresentInDB()
        }
    }

    fun followProcedureForNoDataPresentInDB() {
        if (commonMethods.isOnline(this)) {
            commonMethods.showProgressDialog(this)
            getTripDetails()
        } else {
            showNoInternetAlert(this, object : CommonMethods.INoInternetCustomAlertCallback {
                override fun onOkayClicked() {
                    finish()
                }

                override fun onRetryClicked() {
                    followProcedureForNoDataPresentInDB()
                }

            })
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
    }

    private fun getTripDetails() {
        if (commonMethods.isOnline(this)) {
            apiService.getTripDetails(sessionManager.accessToken!!, tripId).enqueue(RequestCallback(REQ_TRIP_DETAILS, this))
        } else {
            showInternetNotAvailableForStoredDataViewer(this)
        }
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()

        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog, data)
            return
        }
        when (jsonResp.requestCode) {

            REQ_TRIP_DETAILS -> if (jsonResp.isSuccess) {
                commonMethods.hideProgressDialog()
                dbHelper.insertWithUpdate(Constants.DB_KEY_TRIP_DETAILS.toString() + tripId, jsonResp.strResponse)
                onSuccessTripDetail(jsonResp.strResponse)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.hideProgressDialog()
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        commonMethods.showMessage(this, dialog, data)
    }

    private fun onSuccessTripDetail(jsonResponse: String) {

        tripDetailsModels = gson.fromJson(jsonResponse, TripDetailsModel::class.java)
        invoiceModels.clear()
        with(tripDetailsModels) { this?.riderDetails?.get(0)?.invoice }?.let { invoiceModels.addAll(it) }
        recyclerView.removeAllViewsInLayout()
        val adapter = PriceRecycleAdapter(this, invoiceModels)
        recyclerView.setAdapter(adapter)

        if (tripDetailsModels?.isPool!! && tripDetailsModels?.seats != 0) {
            seatcount.visibility = View.VISIBLE
            seatcount.setText(resources.getString(R.string.seat_count) + " " + tripDetailsModels?.seats)
        } else {
            seatcount.visibility = View.GONE
        }

        carname.setText(tripDetailsModels?.vehicleName)
        tripstatus.text =  with(tripDetailsModels) { this?.riderDetails?.get(0)?.status }



        if (tripDetailsModels?.riderDetails?.size == 0)
            return


        trip_km.text = with(tripDetailsModels) { this?.riderDetails?.get(0)?.totalKm } + " " + resources.getString(R.string.km_value)
        trip_duration.text = with(tripDetailsModels) { this?.riderDetails?.get(0)?.totalTime } + " " + resources.getString(R.string.mins_value)
        pickup_address.text = with(tripDetailsModels) { this?.riderDetails?.get(0)?.pickupAddress }
        drop_address.text = with(tripDetailsModels) { this?.riderDetails?.get(0)?.destAddress }
        tvTripid.text = resources.getString(R.string.trip_id) + with(tripDetailsModels){ this?.riderDetails?.get(0)?.tripId }

        if (sessionManager.userType != null && !TextUtils.isEmpty(sessionManager.userType) && !sessionManager.userType.equals("0", ignoreCase = true) && !sessionManager.userType.equals("1", ignoreCase = true)) {
            // Company
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (java.lang.Float.valueOf(with(tripDetailsModels) { this?.riderDetails?.get(0)?.driverPayout }!!) > 0) {
                    trip_amount.text = Html.fromHtml(with(tripDetailsModels) { this?.riderDetails?.get(0)?.driverEarnings }.toString(), Html.FROM_HTML_MODE_LEGACY)
                } else {
                    trip_amount.text = Html.fromHtml(sessionManager.currencySymbol!! + with(tripDetailsModels) { this?.riderDetails?.get(0)?.totalFare }.toString(), Html.FROM_HTML_MODE_LEGACY)
                }

            } else {
                if (java.lang.Float.valueOf(with(tripDetailsModels) { this?.riderDetails?.get(0)?.driverPayout }!!) > 0) {
                    trip_amount.text = Html.fromHtml(with(tripDetailsModels) { this?.riderDetails?.get(0)?.driverPayout }.toString())
                } else {
                    trip_amount.text = Html.fromHtml(sessionManager.currencySymbol!! + with(tripDetailsModels) { this?.riderDetails?.get(0)?.totalFare }.toString())
                }
            }
        } else {

            // Normal Driver
            if (java.lang.Float.valueOf(with(tripDetailsModels) { this?.riderDetails?.get(0)?.driverPayout }!!) > 0) {
                trip_amount.text = with(tripDetailsModels) { this?.riderDetails?.get(0)?.driverEarnings }
            } else {
                trip_amount.text = sessionManager.currencySymbol!! + with(tripDetailsModels) { this?.riderDetails?.get(0)?.totalFare }
            }
        }

        if (with(tripDetailsModels) { this?.riderDetails?.get(0)?.status }.equals(CommonKeys.TripStatus.Rating, ignoreCase = true)) {
            btnrate.setVisibility(View.VISIBLE)
        } else {
            btnrate.setVisibility(View.GONE)
        }


        var startdate = ""
        val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val targetFormat = SimpleDateFormat("EEEE, dd-MM-yyyy")
        try {
            val date = originalFormat.parse(with(tripDetailsModels) { this?.riderDetails?.get(0)?.createdAt })
            startdate = targetFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }


        trip_date.text = startdate

        if (tripDetailsModels?.riderDetails?.get(0)?.mapImage != null && !tripDetailsModels?.riderDetails?.get(0)?.mapImage.equals("")) {
            Picasso.get().load(tripDetailsModels?.riderDetails?.get(0)?.mapImage)
                    .into(route_image)
        }


        if (TextUtils.isEmpty(with(tripDetailsModels) { this?.riderDetails?.get(0)?.mapImage })) {
            val pikcuplatlng = LatLng(java.lang.Double.valueOf(with(tripDetailsModels) { this?.riderDetails?.get(0)?.pickup_lat }!!), java.lang.Double.valueOf(with(tripDetailsModels) { this?.riderDetails?.get(0)?.pickup_lng }!!))
            val droplatlng = LatLng(Double.valueOf(with(tripDetailsModels) { this?.riderDetails?.get(0)?.drop_lat }!!), Double.valueOf(with(tripDetailsModels) { this?.riderDetails?.get(0)?.drop_lng }!!))

            val pathString = "&path=color:0x000000ff%7Cweight:4%7Cenc:" + with(tripDetailsModels) { this?.riderDetails?.get(0)?.tripPath }
            val pickupstr = pikcuplatlng.latitude.toString() + "," + pikcuplatlng.longitude
            val dropstr = droplatlng.latitude.toString() + "," + droplatlng.longitude
            val positionOnMap = "&markers=size:mid|icon:" + getString(R.string.imageUrl) + "pickup.png|" + pickupstr
            val positionOnMap1 = "&markers=size:mid|icon:" + getString(R.string.imageUrl) + "drop.png|" + dropstr

           /* pickup_address.visibility = View.GONE
            drop_address.visibility = View.GONE*/
            staticmapview.visibility = View.GONE
            if (resources.getString(R.string.layout_direction).equals("1")) {
                staticmapview.rotationY = 180f
            }
            tv_pick_Address.text = tripDetailsModels?.riderDetails?.get(0)?.pickupAddress//pickupLocation
            tv_drop_address.text = tripDetailsModels?.riderDetails?.get(0)?.destAddress


        } else {
          /*  pickup_address.visibility = View.VISIBLE
            drop_address.visibility = View.VISIBLE*/
            staticmapview.visibility = View.VISIBLE
            Picasso.get().load(with(tripDetailsModels) { this?.riderDetails?.get(0)?.mapImage })
                    .into(route_image)
        }

        if (isViewUpdatedWithLocalDB) {
            isViewUpdatedWithLocalDB = false
            getTripDetails()
        }
    }
}
