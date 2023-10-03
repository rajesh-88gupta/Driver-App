package com.seentechs.newtaxidriver.trips.tripsdetails

/**
 * @package com.seentechs.newtaxidriver.trips.tripsdetails
 * @subpackage tripsdetails model
 * @category Past
 * @author Seen Technologies
 *
 */

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.google.gson.Gson
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.database.Sqlite
import com.seentechs.newtaxidriver.common.helper.Constants
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonKeys
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.Enums.REQ_PAST_TRIPS
import com.seentechs.newtaxidriver.common.util.Enums.REQ_TRIP_DETAILS
import com.seentechs.newtaxidriver.common.util.PaginationScrollListener
import com.seentechs.newtaxidriver.common.util.RequestCallback
import com.seentechs.newtaxidriver.home.datamodel.TripDetailsModel
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.PaginationAdapterCallback
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import com.seentechs.newtaxidriver.trips.RequestAcceptActivity
import com.seentechs.newtaxidriver.trips.rating.PaymentAmountPage
import com.seentechs.newtaxidriver.trips.rating.Riderrating
import org.json.JSONException
import javax.inject.Inject

/* ************************************************************
                PastDetails
Its used to show all the past trips details information to view the page
*************************************************************** */
class CompletedTripsFragments : Fragment(), ServiceListener, PaginationAdapterCallback {
    @Inject
    lateinit var dbHelper: Sqlite
    private var isViewUpdatedWithLocalDB: Boolean = false

    lateinit var dialog: AlertDialog

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var customDialog: CustomDialog

    @BindView(R.id.listempty)
    lateinit var emptylist: TextView

    @BindView(R.id.past_recycler_view)
    lateinit var recyclerView: RecyclerView

    var tripStatus: String? = null
    protected var isInternetAvailable: Boolean = false

    @BindView(R.id.swipeToRefresh)
    lateinit var swipeToRefresh: SwipeRefreshLayout

    private var completedTripListModel: TripListModel? = null
    private var adapter: CompletedTripsPaginationAdapter? = null
    lateinit private var linearLayoutManager: LinearLayoutManager
    private var isLoading = false
    private var isLastPage = false
    private var TOTAL_PAGES = 0
    private var currentPage = PAGE_START


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.activity_past, container, false)

        AppController.getAppComponent().inject(this)
        ButterKnife.bind(this, rootView)
        dialog = commonMethods.getAlertDialog(requireContext())

        val laydir = getString(R.string.layout_direction)
        if ("1" == laydir) {
            recyclerView.rotationY = 180f
            emptylist.rotationY = 180f
        }


        //  adapter = PastTripsPaginationAdapter(context, this)
        adapter = context?.let { CompletedTripsPaginationAdapter(it, this) }

        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()

        recyclerView.adapter = adapter
        val resId = R.anim.layout_animation_bottom_up
        val animation = AnimationUtils.loadLayoutAnimation(context, resId)
        recyclerView.layoutAnimation = animation

        recyclerView.addOnScrollListener(object : PaginationScrollListener(linearLayoutManager) {
            override fun loadMoreItems() {
                if (commonMethods.isOnline(requireContext())) {
                    this@CompletedTripsFragments.isLoading = true
                    currentPage += 1
                    getPastTrips()
                }
            }

            override fun getTotalPageCount(): Int {
                return TOTAL_PAGES
            }

            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }
        })

        swipeToRefresh.setOnRefreshListener {
            currentPage = 1
            getPastTrips()
            swipeToRefresh.isRefreshing = false
        }


        adapter?.setOnItemRatingClickListner(object : CompletedTripsPaginationAdapter.onItemRatingClickListner {
            override fun setRatingClick(position: Int, tripDetailsModel: TripListModelArrayList) {
                tripStatus = tripDetailsModel.status
                val tripId = tripDetailsModel.tripId!!.toString()
                // Check trip completed or cancelled then open trips details page
                if (CommonKeys.TripStatus.Completed == tripStatus || CommonKeys.TripStatus.Cancelled == tripStatus) {
                    val intent = Intent(activity, TripDetails::class.java)
                    intent.putExtra("tripId", tripId)
                    startActivity(intent)
                    activity!!.overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
                } /*else if (CommonKeys.TripStatus.Rating == tripStatus) {
                    sessionManager.setIsrequest(false)
                    sessionManager.setIsTrip(true)
                    sessionManager.isDriverAndRiderAbleToChat = false
                    //String tripId=tripStatusModel.tripDetailsModel?.riderDetails?.get(0)?.getTripId().toString();
                    sessionManager.tripId = tripId
                    sessionManager.tripStatus = "end_trip"
                    *//*startActivity(new Intent(getApplicationContext(), PaymentAmountPage.class));*//*
                    val rating = Intent(activity, Riderrating::class.java)
                    rating.putExtra("imgprofile", tripDetailsModel.profileImage)
                    rating.putExtra("back", 1)
                    startActivity(rating)

                } */ else {
                    // Get rider profile for open the trips details like (Accepted, Arrive, Begin, End trip)
                    isInternetAvailable = commonMethods.isOnline(context)
                    if (!isInternetAvailable) {
                        commonMethods.showMessage(context, dialog, resources.getString(R.string.no_connection))
                    } else {
                        sessionManager.tripId = tripId
                        sessionManager.isDriverAndRiderAbleToChat = true
                        CommonMethods.startFirebaseChatListenerService(activity!!)
                        getTripDetailApi()
                    }
                }
            }
        })

        adapter?.setOnItemRatingClickListner(object : CompletedTripsPaginationAdapter.onItemRatingClickListner {
            override fun setRatingClick(position: Int, tripDetailsModel: TripListModelArrayList) {

                tripStatus = tripDetailsModel.status
                val tripId = tripDetailsModel.tripId!!.toString()
                // Check trip completed or cancelled then open trips details page
                if (CommonKeys.TripStatus.Completed == tripStatus || CommonKeys.TripStatus.Cancelled == tripStatus) {
                    val intent = Intent(activity, TripDetails::class.java)
                    intent.putExtra("tripId", tripId)
                    startActivity(intent)
                    activity!!.overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
                }
            }
        })

        getCompletedTripsFlow()
        return rootView
    }

    private fun getCompletedTripsFlow() {
        val allHomeDataCursor: Cursor = dbHelper.getDocument(Constants.DB_KEY_COMPLETED_TRIPS.toString())
        if (allHomeDataCursor.moveToFirst()) {
            isViewUpdatedWithLocalDB = true
            try {
                onSuccessPastTrips(allHomeDataCursor.getString(0), true)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            followProcedureForNoDataPresentInDB()

        }
    }

    fun followProcedureForNoDataPresentInDB() {
        if (commonMethods.isOnline(requireContext())) {
            getPastTrips()
        } else {
            CommonMethods.showNoInternetAlert(requireContext(), object : CommonMethods.INoInternetCustomAlertCallback {
                override fun onOkayClicked() {
                    requireActivity().finish()
                }

                override fun onRetryClicked() {
                    followProcedureForNoDataPresentInDB()
                }

            })
        }
    }


    override fun retryPageLoad() {
        getPastTrips()
    }

    private fun getPastTrips() {
        /*if (currentPage==1) {
            commonMethods.showProgressDialog((AppCompatActivity) getActivity(), customDialog);
        }*/
        apiService.getPastTrips(sessionManager.accessToken!!, currentPage.toString()).enqueue(RequestCallback(REQ_PAST_TRIPS, this))
    }

    private fun onSuccessPastTrips(jsonResponse: String, isFromDatabase: Boolean) {
        commonMethods.hideProgressDialog()
        completedTripListModel = gson.fromJson(jsonResponse, TripListModel::class.java)
        if (completedTripListModel?.statusCode.equals("1", true)) {
            if (completedTripListModel!!.data?.size!! > 0) {
                TOTAL_PAGES = completedTripListModel!!.totalPages!!
                adapter!!.clearAll()
                completedTripListModel!!.data?.let { adapter!!.addAll(it) }
                adapter!!.notifyDataSetChanged()
                if (isFromDatabase) {
                    isLastPage = true
                    if (isViewUpdatedWithLocalDB) {
                        isViewUpdatedWithLocalDB = false
                        currentPage = 1
                        getPastTrips()
                    }
                } else {
                    if (currentPage <= TOTAL_PAGES && TOTAL_PAGES > 1) {
                        if (commonMethods.isOnline(context)) {
                            isLastPage = false
                            adapter!!.addLoadingFooter()
                        }
                    } else
                        isLastPage = true
                }
            }
        } else {
            emptylist.visibility = View.VISIBLE
        }
    }

    private fun onLoadMorePastTrips(jsonResponse: JsonResponse) {
        completedTripListModel = gson.fromJson(jsonResponse.strResponse, TripListModel::class.java)
        TOTAL_PAGES = completedTripListModel!!.totalPages!!
        adapter!!.removeLoadingFooter()
        isLoading = false

        adapter!!.addAll(completedTripListModel!!.data)
        adapter!!.notifyDataSetChanged()
        if (currentPage != TOTAL_PAGES)
            adapter!!.addLoadingFooter()
        else
            isLastPage = true
    }


    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(context, dialog, data)
            return
        }
        when (jsonResp.requestCode) {

            REQ_TRIP_DETAILS -> if (jsonResp.isSuccess) {
                commonMethods.hideProgressDialog()
                onSuccessRiderProfile(jsonResp)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.hideProgressDialog()
                commonMethods.showMessage(context, dialog, jsonResp.statusMsg)
            }

            REQ_PAST_TRIPS -> {
                commonMethods.hideProgressDialog()
                if (jsonResp.isSuccess) {
                    val getCurrentPage = commonMethods.getJsonValue(jsonResp.strResponse, "current_page", Int::class.java)
                    currentPage = getCurrentPage as Int
                    if (currentPage == 1) {
                        dbHelper.insertWithUpdate(Constants.DB_KEY_COMPLETED_TRIPS.toString(), jsonResp.strResponse)
                        onSuccessPastTrips(jsonResp.strResponse, false)
                    } else {
                        onLoadMorePastTrips(jsonResp)
                    }
                } else if (currentPage == 1 && !TextUtils.isEmpty(jsonResp.statusMsg)) {
                    recyclerView.visibility = View.GONE
                    emptylist.visibility = View.VISIBLE
                } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                    commonMethods.showMessage(context, dialog, jsonResp.statusMsg)
                }
            }
            else -> if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.hideProgressDialog()
                commonMethods.showMessage(context, dialog, jsonResp.statusMsg)
            }
        }
    }


    private fun onSuccessRiderProfile(jsonResp: JsonResponse) {
        commonMethods.hideProgressDialog()
        val tripDetailsModel = gson.fromJson(jsonResp.strResponse, TripDetailsModel::class.java)
        val invoiceModels = tripDetailsModel.riderDetails.get(0).invoice
        //  sessionManager.paymentMethod = tripDetailsModel.paymentMode
        sessionManager.bookingType = tripDetailsModel.riderDetails.get(0).bookingType

        // Pass different data based on trip status
        if (CommonKeys.TripStatus.Scheduled == tripStatus || CommonKeys.TripStatus.Begin_Trip == tripStatus || CommonKeys.TripStatus.End_Trip == tripStatus) {
            val requstreceivepage = Intent(activity, RequestAcceptActivity::class.java)
            requstreceivepage.putExtra("riderDetails", tripDetailsModel)
            if (CommonKeys.TripStatus.Scheduled == tripStatus) {
                sessionManager.isTrip = true
                sessionManager.tripStatus = CommonKeys.TripDriverStatus.ConfirmArrived
                sessionManager.subTripStatus = resources.getString(R.string.confirm_arrived)
                requstreceivepage.putExtra("isTripBegin", false)
                requstreceivepage.putExtra("tripstatus", resources.getString(R.string.confirm_arrived))
            } else if (CommonKeys.TripStatus.Begin_Trip == tripStatus) {
                sessionManager.isTrip = true
                sessionManager.tripStatus = CommonKeys.TripDriverStatus.ConfirmArrived
                sessionManager.subTripStatus = resources.getString(R.string.begin_trip)
                requstreceivepage.putExtra("isTripBegin", false)
                requstreceivepage.putExtra("tripstatus", resources.getString(R.string.begin_trip))
            } else if (CommonKeys.TripStatus.End_Trip == tripStatus) {
                sessionManager.isTrip = true
                sessionManager.tripStatus = CommonKeys.TripDriverStatus.BeginTrip
                sessionManager.subTripStatus = resources.getString(R.string.end_trip)
                requstreceivepage.putExtra("isTripBegin", true)
                requstreceivepage.putExtra("tripstatus", resources.getString(R.string.end_trip))
            }
            startActivity(requstreceivepage)
            //recyclerView.addOnItemTouchListener(this);
        } else if (CommonKeys.TripStatus.Rating == tripStatus) {
            sessionManager.tripStatus = CommonKeys.TripDriverStatus.EndTrip
            val rating = Intent(activity, Riderrating::class.java)
            rating.putExtra("imgprofile", tripDetailsModel.riderDetails.get(0).profileImage)
            rating.putExtra("back", 1)
            startActivity(rating)
            //recyclerView.addOnItemTouchListener(this);

        } else if (CommonKeys.TripStatus.Payment == tripStatus) {

            val bundle = Bundle()
            bundle.putSerializable("invoiceModels", invoiceModels)
            val main = Intent(activity, PaymentAmountPage::class.java)
            main.putExtra("AmountDetails", jsonResp.strResponse)
            main.putExtras(bundle)
            startActivity(main)
            //recyclerView.addOnItemTouchListener(this);
        }
        requireActivity().overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)


    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        //recyclerView.addOnItemTouchListener(this);
    }

    private fun getTripDetailApi() {
        apiService.getTripDetails(sessionManager.accessToken!!, sessionManager.tripId!!).enqueue(RequestCallback(REQ_TRIP_DETAILS, this))
    }

    companion object {
        private val PAGE_START = 1
    }
}