package com.seentechs.newtaxidriver.trips.tripsdetails

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonKeys
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.PaginationAdapterCallback
import java.util.*
import javax.inject.Inject

class PendingTripsPaginationAdapter(private val context: Context, private val mCallback: PaginationAdapterCallback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var dialog: AlertDialog

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var customDialog: CustomDialog

    @Inject
    lateinit var gson: Gson
    var tripDetailsModel: ArrayList<TripListModelArrayList>? = null
    private var isLoadingAdded = false
    private var retryPageLoad = false
    private var errorMsg: String? = null

    private var tripStatusModel: TripListModelArrayList? = null

    private var onItemRatingClickListener: onItemRatingClickListner? = null

    val isEmpty: Boolean
        get() = itemCount == 0

    fun setOnItemRatingClickListner(onItemRatingClickListner: onItemRatingClickListner) {
        this.onItemRatingClickListener = onItemRatingClickListner
    }

    interface onItemRatingClickListner {
        fun setRatingClick(tripStatusModel: TripListModelArrayList, position: Int)
    }

    init {
        tripDetailsModel = ArrayList()
        AppController.getAppComponent().inject(this)
        dialog = commonMethods.getAlertDialog(this.context)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var viewHolder: RecyclerView.ViewHolder? = null
        val inflater = LayoutInflater.from(parent.context)

        when (viewType) {
            ITEM -> {
                val viewItem: View
                if (tripStatusModel?.bookingType.equals("Manual Booking", ignoreCase = true) && tripStatusModel?.status.equals("pending", ignoreCase = true)) {
                    viewItem = inflater.inflate(R.layout.app_trip_manual_booking_lay, parent, false)
                } else {
                    viewItem = inflater.inflate(R.layout.app_trips_item_layout, parent, false)
                }
                viewHolder = UpcomingViewHolder(viewItem)
            }
            LOADING -> {
                val viewLoading = inflater.inflate(R.layout.item_progress, parent, false)
                viewHolder = LoadingViewHolder(viewLoading)
            }
        }
        return viewHolder!!
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val tripStatusModel = tripDetailsModel!![position] // Upcoming Detail
        when (getItemViewType(position)) {
            ITEM -> {

                val upcomingViewHolder = holder as UpcomingViewHolder

                if (tripStatusModel.bookingType.equals("Manual Booking", ignoreCase = true) && tripStatusModel.status.equals("pending", ignoreCase = true)) {
                    upcomingViewHolder.carType?.text = tripStatusModel.carType +" "+ tripStatusModel.driverEarnings
                    upcomingViewHolder.trip_status?.text = tripStatusModel.status
                    upcomingViewHolder.pickupaddress?.text = tripStatusModel.pickup
                    upcomingViewHolder.destadddress?.text = tripStatusModel.drop
                    upcomingViewHolder.trip_id_button?.text = tripStatusModel.scheduleDisplayDate
                    //upcomingViewHolder.trip_id_button?.text = context.getString(R.string.trip_id) + tripStatusModel?.tripId!!
                } else {
                    val currencysymbol = sessionManager.currencySymbol
                    upcomingViewHolder.tv_country?.text = context.resources.getString(R.string.trip_id) + "" + tripStatusModel?.tripId.toString()
                    upcomingViewHolder.carname?.text = tripStatusModel?.carType
                    if (tripStatusModel.isPool != null && tripStatusModel.isPool!! && tripStatusModel.seats != 0) {
                        upcomingViewHolder.seatcount?.visibility = View.VISIBLE
                        upcomingViewHolder.seatcount?.setText(context.getString(R.string.seat_count) + " " + tripStatusModel.seats.toString())
                    } else {
                        upcomingViewHolder.seatcount?.visibility = View.GONE
                    }
                    //upcomingViewHolder.btnrate.setVisibility(View.GONE);
                    upcomingViewHolder.status?.visibility = View.VISIBLE
                    if (tripStatusModel.status == CommonKeys.TripStatus.Rating) {
                        upcomingViewHolder.status?.text = context.getString(R.string.Rating)
                        /*upcomingViewHolder.status.setVisibility(View.GONE);
                        upcomingViewHolder.btnrate.setVisibility(View.VISIBLE);*/
                    } else if (tripStatusModel.status == CommonKeys.TripStatus.Cancelled) {
                        upcomingViewHolder.status?.text = context.getString(R.string.Cancelled)
                    } else if (tripStatusModel.status == CommonKeys.TripStatus.Completed) {
                        upcomingViewHolder.status?.text = context.getString(R.string.completed)
                    } else if (tripStatusModel.status == CommonKeys.TripStatus.Payment) {
                        upcomingViewHolder.status?.text = context.getString(R.string.payment)
                    } else if (tripStatusModel.status == CommonKeys.TripStatus.Begin_Trip) {
                        upcomingViewHolder.status?.text = context.getString(R.string.begin_trip_text)
                    } else if (tripStatusModel.status == CommonKeys.TripStatus.End_Trip) {
                        upcomingViewHolder.status?.text = context.getString(R.string.end_trip_text)
                    } else if (tripStatusModel.status == CommonKeys.TripStatus.Scheduled) {
                        upcomingViewHolder.status?.text = context.getString(R.string.scheduled)
                    } else {
                        upcomingViewHolder.status?.text = tripStatusModel.carType
                    }

                    upcomingViewHolder.amountcard?.text =/* currencysymbol +*/ tripStatusModel?.driverEarnings

                    if (!TextUtils.isEmpty(tripStatusModel?.mapImage)) {
                        upcomingViewHolder.imageLayout?.visibility = View.VISIBLE
                        upcomingViewHolder.view_line?.visibility = View.GONE
                        upcomingViewHolder.mapView?.visibility = View.INVISIBLE
                        Picasso.get().load(tripStatusModel?.mapImage)
                                .into(upcomingViewHolder.imageView)
                    } else {
                        upcomingViewHolder.tv_pickLocation?.text = tripStatusModel?.pickup
                        upcomingViewHolder.tv_dropLocation?.text = tripStatusModel?.drop
                        upcomingViewHolder.mapView?.visibility = View.VISIBLE
                        upcomingViewHolder.imageLayout?.visibility = View.GONE
                        if (context.resources.getString(R.string.layout_direction).equals("1")) {
                            upcomingViewHolder.mapView?.rotationY = 180f
                        }
                    }

                    upcomingViewHolder.card_view?.setOnClickListener { onItemRatingClickListener!!.setRatingClick(tripStatusModel, position) }
                }
            }
            LOADING -> {
                val loadingVH = holder as LoadingViewHolder

                if (retryPageLoad) {
                    loadingVH.mErrorLayout.visibility = View.VISIBLE
                    loadingVH.mProgressBar.visibility = View.GONE

                    loadingVH.mErrorTxt.text = if (errorMsg != null)
                        errorMsg
                    else
                        context.getString(R.string.error_msg_unknown)

                } else {
                    loadingVH.mErrorLayout.visibility = View.GONE
                    loadingVH.mProgressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return if (tripDetailsModel == null) 0 else tripDetailsModel!!.size
    }

    override fun getItemViewType(position: Int): Int {
        tripStatusModel = tripDetailsModel!![position]

        return if (position == tripDetailsModel!!.size - 1 && isLoadingAdded) LOADING else ITEM
    }

    /*
      Helpers - Pagination
    */

    fun add(tripDetailsModel: TripListModelArrayList) {
        this.tripDetailsModel!!.add(tripDetailsModel)
        notifyItemInserted(this.tripDetailsModel!!.size - 1)
    }

    fun addAll(tripStatusModels: ArrayList<TripListModelArrayList>) {
        for (tripStatusModel in tripStatusModels) {
            add(tripStatusModel)
        }
    }

    fun remove(tripStatusModel: TripListModelArrayList?) {
        val position = tripDetailsModel!!.indexOf(tripStatusModel)
        if (position > -1) {
            tripDetailsModel!!.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun clear() {
        isLoadingAdded = false
        while (itemCount > 0) {
            remove(getItem(0))
        }
    }

    fun clearAll() {
        isLoadingAdded = false
        tripDetailsModel!!.clear()
        notifyDataSetChanged()
    }


    fun addLoadingFooter() {
        isLoadingAdded = true
        add(TripListModelArrayList())
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false

        val position = tripDetailsModel!!.size - 1
        val tripStatusModel = getItem(position)

        if (tripStatusModel != null) {
            tripDetailsModel!!.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    private fun getItem(position: Int): TripListModelArrayList? {
        return tripDetailsModel!![position]
    }

    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param show
     * @param errorMsg to display if page load fails
     */
    fun showRetry(show: Boolean, errorMsg: String?) {
        retryPageLoad = show
        notifyItemChanged(tripDetailsModel!!.size - 1)

        if (errorMsg != null) this.errorMsg = errorMsg
    }


    protected inner class UpcomingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal var tv_country: TextView? = null
        internal var carname: TextView? = null
        internal var status: TextView? = null
        internal var amountcard: TextView? = null
        internal var imageLayout: RelativeLayout? = null
        internal var imageView: ImageView? = null
        internal var manual_booking: TextView? = null
        internal var trip_amount: TextView? = null
        internal var carType: TextView? = null
        internal var trip_status: TextView? = null
        internal var pickupaddress: TextView? = null
        internal var destadddress: TextView? = null
        internal var trip_id_button: TextView? = null
        internal var tv_dropLocation: TextView? = null
        internal var tv_pickLocation: TextView? = null
        internal var seatcount: TextView? = null
        internal var view_line: View? = null

        //private Button btnrate;
        internal var mapView: RelativeLayout? = null
        internal var card_view: CardView? = null

        init {

            tv_country = view.findViewById<View>(R.id.datetime) as? TextView
            carname = view.findViewById<View>(R.id.carname) as? TextView
            status = view.findViewById<View>(R.id.status) as? TextView
            amountcard = view.findViewById<View>(R.id.amountcard) as? TextView
            imageLayout = view.findViewById<View>(R.id.image_layout) as? RelativeLayout
            imageView = view.findViewById<View>(R.id.imageView) as? ImageView
            carType = view.findViewById<View>(R.id.car_type) as? TextView
            trip_status = view.findViewById<View>(R.id.trip_status) as? TextView
            pickupaddress = view.findViewById<View>(R.id.pickupaddress) as? TextView
            destadddress = view.findViewById<View>(R.id.destadddress) as? TextView
            trip_id_button = view.findViewById<View>(R.id.trip_id_button) as? TextView
            trip_amount = view.findViewById<View>(R.id.trip_amount) as? TextView
            view_line = view.findViewById<View>(R.id.line3) as? View
            //btnrate = (Button) view.findViewById(R.id.btnrate);
            mapView = view.findViewById<View>(R.id.static_map) as? RelativeLayout
            card_view = view.findViewById<View>(R.id.card_view) as? CardView
            tv_pickLocation = view.findViewById(R.id.tv_pick_location) as? TextView
            tv_dropLocation = view.findViewById(R.id.tv_drop_location) as? TextView
            seatcount = view.findViewById<View>(R.id.seatcount) as? TextView
        }
    }

    protected inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        internal val mProgressBar: ProgressBar
        private val mRetryBtn: ImageButton
        internal val mErrorTxt: TextView
        internal val mErrorLayout: LinearLayout


        init {

            mProgressBar = itemView.findViewById(R.id.loadmore_progress)
            mRetryBtn = itemView.findViewById(R.id.loadmore_retry)
            mErrorTxt = itemView.findViewById(R.id.loadmore_errortxt)
            mErrorLayout = itemView.findViewById(R.id.loadmore_errorlayout)

            mRetryBtn.setOnClickListener(this)
            mErrorLayout.setOnClickListener(this)
        }


        override fun onClick(view: View) {
            when (view.id) {
                R.id.loadmore_retry, R.id.loadmore_errorlayout -> {
                    showRetry(false, null)
                    mCallback.retryPageLoad()
                }
            }
        }
    }

    companion object {

        // View Types and Pagination variables
        private val ITEM = 0
        private val LOADING = 1
    }

}
