package com.seentechs.newtaxidriver.trips.tripsdetails

/**
 * @package com.seentechs.newtaxidriver.trips.tripsdetails
 * @subpackage tripsdetails model
 * @category Upcoming
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
import com.seentechs.newtaxidriver.common.util.Enums.REQ_RIDER_PROFILE
import com.seentechs.newtaxidriver.common.util.Enums.REQ_UPCOMING_TRIPS
import com.seentechs.newtaxidriver.common.util.PaginationScrollListener
import com.seentechs.newtaxidriver.common.util.RequestCallback
import com.seentechs.newtaxidriver.home.datamodel.TripDetailsModel
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.PaginationAdapterCallback
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import com.seentechs.newtaxidriver.home.interfaces.YourTripsListener
import com.seentechs.newtaxidriver.trips.RequestAcceptActivity
import com.seentechs.newtaxidriver.trips.rating.PaymentAmountPage
import com.seentechs.newtaxidriver.trips.rating.Riderrating
import org.json.JSONException
import java.util.*
import javax.inject.Inject

/* ************************************************************
                Upcoming
Its used to show all the current trips details view page
*************************************************************** */
class PendingTripsFragment : Fragment(), ServiceListener, PaginationAdapterCallback {
    @Inject
    lateinit var dbHelper: Sqlite
    private var isViewUpdatedWithLocalDB: Boolean = false

    lateinit var dialog: AlertDialog

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var apiService: ApiService

    lateinit @Inject
    var sessionManager: SessionManager

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var customDialog: CustomDialog

    @BindView(R.id.listempty)
    lateinit var emptyList: TextView

    @BindView(R.id.current_recycler_view)
    lateinit var recyclerView: RecyclerView

    @BindView(R.id.swipeToRefresh)
    lateinit var swipeToRefresh: SwipeRefreshLayout

    protected var isInternetAvailable: Boolean = false
    private var tripStatus: String? = null
    private var adapter: PendingTripsPaginationAdapter? = null
    lateinit private var linearLayoutManager: LinearLayoutManager
    private var isLoading = false
    private var isLastPage = false
    private var TOTAL_PAGES = 0
    private var currentPage = PAGE_START

    private var listener: YourTripsListener? = null
    private var mActivity: YourTrips? = null
    private var pendingTripsListModel: TripListModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            listener = activity as YourTripsListener?
        } catch (e: ClassCastException) {
            throw ClassCastException("Upcoming must implement ActivityListener")
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.app_activity_upcoming, container, false)

        AppController.getAppComponent().inject(this)
        ButterKnife.bind(this, rootView)
        dialog = commonMethods.getAlertDialog(requireContext())
        init()

        adapter = context?.let { PendingTripsPaginationAdapter(it, this) }

        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()

        recyclerView.adapter = adapter
        val resId = R.anim.layout_animation_bottom_up
        val animation = AnimationUtils.loadLayoutAnimation(context, resId)
        recyclerView.layoutAnimation = animation

        val laydir = getString(R.string.layout_direction)
        if ("1" == laydir)
            recyclerView.rotationY = 180f

        swipeToRefresh.setOnRefreshListener {
            currentPage = 1
            getPendingTrips(false)
            swipeToRefresh.isRefreshing = false
        }

        recyclerView.addOnScrollListener(object : PaginationScrollListener(linearLayoutManager) {
            override fun loadMoreItems() {
                if (commonMethods.isOnline(mActivity)) {
                    this@PendingTripsFragment.isLoading = true
                    currentPage += 1
                    getPendingTrips(false)
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

        adapter?.setOnItemRatingClickListner(object : PendingTripsPaginationAdapter.onItemRatingClickListner {
            override fun setRatingClick(tripDetailsModel: TripListModelArrayList, position: Int) {


                isInternetAvailable = commonMethods.isOnline(context)
                if (!isInternetAvailable) {
                    commonMethods.showMessage(context, dialog, resources.getString(R.string.no_connection))
                } else {
                    sessionManager.tripId = tripDetailsModel.tripId.toString()
                    sessionManager.isDriverAndRiderAbleToChat = true
                    CommonMethods.startFirebaseChatListenerService(Objects.requireNonNull(activity!!))
                    val status = tripDetailsModel.status
                    if (status.equals("Rating") || status.equals("Payment")) {
                        getRiderApi(tripDetailsModel.tripId.toString())
                    } else {
                        getRiderApi("")
                    }

                }
            }

        })


        getPendingTripsFlow()
        return rootView
    }

    private fun getPendingTripsFlow() {
        val allHomeDataCursor: Cursor = dbHelper.getDocument(Constants.DB_KEY_PENDING_TRIPS.toString())
        if (allHomeDataCursor.moveToFirst()) {
            isViewUpdatedWithLocalDB = true
            try {
                onSuccessUpcomingTrips(allHomeDataCursor.getString(0), true)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            followProcedureForNoDataPresentInDB()

        }
    }

    fun followProcedureForNoDataPresentInDB() {
        if (commonMethods.isOnline(requireContext())) {
            getPendingTrips(true)
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


    private fun init() {
        if (listener == null) return

        mActivity = listener!!.instance
    }

    /**
     * PendingTrips
     */
    private fun getPendingTrips(showLoader: Boolean) {
        if (commonMethods.isOnline(requireContext())) {
            if (currentPage == 1) {
                if (showLoader) {
                    commonMethods.showProgressDialog(context!!)
                }
            }
            apiService.getPendingTrips(sessionManager.accessToken!!, currentPage.toString()).enqueue(RequestCallback(REQ_UPCOMING_TRIPS, this))
        } else {
            CommonMethods.showInternetNotAvailableForStoredDataViewer(requireContext())
        }
    }

    /**
     * get Rider Details to Start Trip
     */
    private fun getRiderApi(tripId: String) {
        //commonMethods.showProgressDialog((AppCompatActivity) getActivity(), customDialog);
        apiService.getTripDetails(sessionManager.accessToken!!, tripId).enqueue(RequestCallback(REQ_RIDER_PROFILE, this))
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(context, dialog, data)
            return
        }

        when (jsonResp.requestCode) {
            REQ_UPCOMING_TRIPS -> if (jsonResp.isSuccess) {
                commonMethods.hideProgressDialog()
                sessionManager.isTrip = true
                val getCurrentPage = commonMethods.getJsonValue(jsonResp.strResponse, "current_page", Int::class.java) as Int
                currentPage = getCurrentPage
                if (currentPage == 1) {
                    //dbHelper.insertWithUpdate(Constants.DB_KEY_PENDING_TRIPS.toString(), jsonResp.strResponse)
                    onSuccessUpcomingTrips(jsonResp.strResponse, false)
                } else {
                    onLoadMoreUpcomingTrips(jsonResp)
                }
            } else if (currentPage == 1 && !TextUtils.isEmpty(jsonResp.statusMsg)) {
                //dbHelper.insertWithUpdate(Constants.DB_KEY_PENDING_TRIPS.toString(), jsonResp.strResponse)
                recyclerView.visibility = View.GONE
                emptyList.visibility = View.VISIBLE
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.hideProgressDialog()
                commonMethods.showMessage(mActivity, dialog, jsonResp.statusMsg)
            }
            REQ_RIDER_PROFILE -> if (jsonResp.isSuccess) {
                onSuccessRiderProfile(jsonResp)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(context, dialog, jsonResp.statusMsg)
            }
        }
    }

    private fun onSuccessUpcomingTrips(jsonResponse: String, isFromDatabase: Boolean) {
        pendingTripsListModel = gson.fromJson(jsonResponse, TripListModel::class.java)
        if (pendingTripsListModel?.statusCode.equals("1", true)) {
            if (pendingTripsListModel!!.data.size > 0) {
                TOTAL_PAGES = pendingTripsListModel!!.totalPages
                adapter!!.clearAll()
                pendingTripsListModel!!.data?.let { adapter!!.addAll(it) }
                adapter!!.notifyDataSetChanged()
                if (isFromDatabase) {
                    isLastPage = true
                    if (isViewUpdatedWithLocalDB) {
                        isViewUpdatedWithLocalDB = false
                        currentPage = 1
                        getPendingTrips(false)
                    }
                } else {
                    if (currentPage <= TOTAL_PAGES && TOTAL_PAGES > 1) {
                        if (commonMethods.isOnline(mActivity)) {
                            isLastPage = false
                            adapter!!.addLoadingFooter()
                        } else
                            isLastPage = true
                    } else {
                        isLastPage = true
                    }
                }
            }
        } else {
            emptyList.visibility = View.VISIBLE
        }

        if (isViewUpdatedWithLocalDB) {
            isViewUpdatedWithLocalDB = false
            getPendingTrips(true)
        }
    }

    private fun onLoadMoreUpcomingTrips(jsonResponse: JsonResponse) {
        pendingTripsListModel = gson.fromJson(jsonResponse.strResponse, TripListModel::class.java)
        TOTAL_PAGES = pendingTripsListModel!!.totalPages!!
        adapter!!.removeLoadingFooter()
        isLoading = false

        pendingTripsListModel!!.data?.let { adapter!!.addAll(it) }
        adapter!!.notifyDataSetChanged()
        if (currentPage != TOTAL_PAGES)
            adapter!!.addLoadingFooter()
        else
            isLastPage = true
    }


    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()

    }


    private fun onSuccessRiderProfile(jsonResp: JsonResponse) {
        commonMethods.hideProgressDialog()
        val earningModel = gson.fromJson(jsonResp.strResponse, TripDetailsModel::class.java)
        val invoiceModels = earningModel.riderDetails.get(0).invoice

        tripStatus = earningModel.riderDetails.get(0).status
        sessionManager.tripId = earningModel.riderDetails.get(0).tripId

        try {
            // Pass different data based on trip status
            if (CommonKeys.TripStatus.Scheduled == tripStatus || CommonKeys.TripStatus.Begin_Trip == tripStatus || CommonKeys.TripStatus.End_Trip == tripStatus) {
                val requestReceivePage = Intent(activity, RequestAcceptActivity::class.java)
                requestReceivePage.putExtra("riderDetails", earningModel)
                if (CommonKeys.TripStatus.Scheduled == tripStatus) {
                    //sessionManager.setTripStatus("CONFIRM YOU'VE ARRIVED");
                    sessionManager.tripStatus = CommonKeys.TripDriverStatus.ConfirmArrived
                    sessionManager.subTripStatus = resources.getString(R.string.confirm_arrived)
                    requestReceivePage.putExtra("isTripBegin", false)
                    requestReceivePage.putExtra("tripstatus", resources.getString(R.string.confirm_arrived))
                } else if (CommonKeys.TripStatus.Begin_Trip == tripStatus) {
                    //sessionManager.setTripStatus("CONFIRM YOU'VE ARRIVED");
                    sessionManager.tripStatus = CommonKeys.TripDriverStatus.ConfirmArrived
                    sessionManager.subTripStatus = resources.getString(R.string.begin_trip)
                    requestReceivePage.putExtra("isTripBegin", false)
                    requestReceivePage.putExtra("tripstatus", resources.getString(R.string.begin_trip))
                } else if (CommonKeys.TripStatus.End_Trip == tripStatus) {
                    //sessionManager.setTripStatus("Begin Trip");
                    sessionManager.tripStatus = CommonKeys.TripDriverStatus.BeginTrip
                    sessionManager.subTripStatus = resources.getString(R.string.end_trip)
                    requestReceivePage.putExtra("isTripBegin", true)
                    requestReceivePage.putExtra("tripstatus", resources.getString(R.string.end_trip))
                }
                startActivity(requestReceivePage)
            } else if (CommonKeys.TripStatus.Rating == tripStatus) {
                //sessionManager.setTripStatus("End Trip");
                sessionManager.tripStatus = CommonKeys.TripDriverStatus.EndTrip
                val rating = Intent(activity, Riderrating::class.java)
                rating.putExtra("imgprofile", earningModel.riderDetails.get(0).profileImage)
                rating.putExtra("back", 1)
                startActivity(rating)
            } else if (CommonKeys.TripStatus.Payment == tripStatus) {

                val bundle = Bundle()
                bundle.putSerializable("invoiceModels", invoiceModels)
                val main = Intent(activity, PaymentAmountPage::class.java)
                main.putExtra("AmountDetails", jsonResp.strResponse)
                main.putExtras(bundle)
                startActivity(main)

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


        Objects.requireNonNull(activity)?.overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
    }

    override fun retryPageLoad() {
        getPendingTrips(false)
    }

    companion object {
        private val PAGE_START = 1
    }
}