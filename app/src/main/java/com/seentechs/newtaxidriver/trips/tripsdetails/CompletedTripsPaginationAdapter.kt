package com.seentechs.newtaxidriver.trips.tripsdetails


import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.home.interfaces.PaginationAdapterCallback
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonKeys
import java.util.*
import javax.inject.Inject

class CompletedTripsPaginationAdapter internal constructor(private val context: Context, private val mCallback: PaginationAdapterCallback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    @Inject
    lateinit var sessionManager: SessionManager

    private val tripStatusModels: ArrayList<TripListModelArrayList>?
    private var isLoadingAdded = false
    private var retryPageLoad = false
    private var onItemRatingClickListener: onItemRatingClickListner? = null

    private var errorMsg: String? = null

    val isEmpty: Boolean
        get() = itemCount == 0


    internal fun setOnItemRatingClickListner(onItemRatingClickListner: onItemRatingClickListner) {
        this.onItemRatingClickListener = onItemRatingClickListner
    }

    interface onItemRatingClickListner {
        fun setRatingClick(position: Int, tripStatusModel: TripListModelArrayList)
    }

    init {
        tripStatusModels = ArrayList()
        AppController.getAppComponent().inject(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var viewHolder: RecyclerView.ViewHolder? = null
        val inflater = LayoutInflater.from(parent.context)

        when (viewType) {
            ITEM -> {
                val viewItem = inflater.inflate(R.layout.app_trips_item_layout, parent, false)
                viewHolder = PastTripsViewHolder(viewItem)
            }
            LOADING -> {
                val viewLoading = inflater.inflate(R.layout.item_progress, parent, false)
                viewHolder = LoadingViewHolder(viewLoading)
            }
        }
        return viewHolder!!
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val tripStatusModel = tripStatusModels!![position] // Past Detail

        when (getItemViewType(position)) {
            ITEM -> {
                val pastTripsViewHolder = holder as PastTripsViewHolder
                val currencysymbol = sessionManager.currencySymbol

                pastTripsViewHolder.tv_country.text = context.resources.getString(R.string.trip_id) + tripStatusModel?.tripId!!
                pastTripsViewHolder.carname.text = tripStatusModel?.carType

                //pastTripsViewHolder.btnrate.setVisibility(View.GONE);

                if (tripStatusModel?.status == CommonKeys.TripStatus.Rating) {
                    //pastTripsViewHolder.btnrate.setVisibility(View.VISIBLE);
                    pastTripsViewHolder.status.text = context.getString(R.string.Rating)
                } else if (tripStatusModel?.status == CommonKeys.TripStatus.Cancelled) {
                    pastTripsViewHolder.status.text = context.getString(R.string.Cancelled)
                } else if (tripStatusModel?.status == CommonKeys.TripStatus.Completed) {
                    pastTripsViewHolder.status.text = context.getString(R.string.completed)
                } else if (tripStatusModel?.status == CommonKeys.TripStatus.Payment) {
                    pastTripsViewHolder.status.text = context.getString(R.string.payment)
                } else if (tripStatusModel?.status == CommonKeys.TripStatus.Begin_Trip) {
                    pastTripsViewHolder.status.text = context.getString(R.string.begin_trip)
                } else if (tripStatusModel?.status == CommonKeys.TripStatus.End_Trip) {
                    pastTripsViewHolder.status.text = context.getString(R.string.end_trip)
                } else if (tripStatusModel?.status == CommonKeys.TripStatus.Scheduled) {
                    pastTripsViewHolder.status.text = context.getString(R.string.scheduled)
                } else {
                    pastTripsViewHolder.status.text = tripStatusModel?.carType
                }
                pastTripsViewHolder.amountcard.text = /*sessionManager.currencySymbol+*/tripStatusModel?.driverEarnings



                if (TextUtils.isEmpty(tripStatusModel?.mapImage)) {
                    pastTripsViewHolder.mapView?.visibility =View.VISIBLE
                    if (context.resources.getString(R.string.layout_direction).equals("1")) {
                        pastTripsViewHolder.mapView?.rotationY = 180f
                    }
                    pastTripsViewHolder.tv_pickLocation.text=tripStatusModel?.pickup
                    pastTripsViewHolder.tv_dropLocation.text=tripStatusModel?.drop
                    pastTripsViewHolder.imageLayout?.visibility =View.GONE
                   /* val pikcuplatlng = LatLng(java.lang.Double.valueOf(tripStatusModel?.pickupLatitude!!), java.lang.Double.valueOf(tripStatusModel?.pickupLongitude!!))
                    val droplatlng = LatLng(java.lang.Double.valueOf(tripStatusModel?.dropLatitude!!), java.lang.Double.valueOf(tripStatusModel?.dropLongitude!!))

                    val pathString = "&path=color:0x000000ff%7Cweight:4%7Cenc:" + tripStatusModel?.tripPath
                    val pickupstr = pikcuplatlng.latitude.toString() + "," + pikcuplatlng.longitude
                    val dropstr = droplatlng.latitude.toString() + "," + droplatlng.longitude
                    val positionOnMap = "&markers=size:mid|icon:" + getString(R.string.imageUrl) + "pickup.png|" + pickupstr
                    val positionOnMap1 = "&markers=size:mid|icon:" + getString(R.string.imageUrl) + "drop.png|" + dropstr

                    var staticMapURL: String
                    if (tripStatusModel?.tripPath == "") {
                        staticMapURL = "https://maps.googleapis.com/maps/api/staticmap?size=640x250&" +
                                pikcuplatlng.latitude + "," + pikcuplatlng.longitude +
                                "" + positionOnMap + "" + positionOnMap1 + //"&zoom=14" +

                                "&key=" + sessionManager.googleMapKey + "&language=" + Locale.getDefault()
                    } else {
                        staticMapURL = "https://maps.googleapis.com/maps/api/staticmap?size=640x250&" +
                                pikcuplatlng.latitude + "," + pikcuplatlng.longitude +
                                pathString + "" + positionOnMap + "" + positionOnMap1 + //"&zoom=14" +

                                "&key=" + sessionManager.googleMapKey + "&language=" + Locale.getDefault()
                    }
                    println("Static Map Url : "+staticMapURL)
                    Picasso.with(context).load(staticMapURL)
                            .into(pastTripsViewHolder.imageView)*/
                } else {
                    pastTripsViewHolder.imageLayout?.visibility=View.VISIBLE
                    pastTripsViewHolder.view_line?.visibility = View.GONE
                    pastTripsViewHolder.mapView?.visibility =View.INVISIBLE
                    Picasso.get().load(tripStatusModel?.mapImage)
                            .into(pastTripsViewHolder.imageView)
                }
                if(tripStatusModel.isPool != null && tripStatusModel.isPool!! && tripStatusModel.seats!=0){
                    pastTripsViewHolder.seatcount.visibility = View.VISIBLE
                    pastTripsViewHolder.seatcount.setText(context.getString(R.string.seat_count)+ " " + tripStatusModel.seats.toString())
                }else{
                    pastTripsViewHolder.seatcount.visibility = View.GONE
                }
                pastTripsViewHolder.card_view?.setOnClickListener { onItemRatingClickListener!!.setRatingClick(position, tripStatusModel) }
            }

            LOADING -> {
                val loadingVH = holder as LoadingViewHolder
                if (retryPageLoad) {
                    loadingVH.mErrorLayout.visibility = View.VISIBLE
                    loadingVH.mProgressBar.visibility = View.GONE
                    loadingVH.mErrorTxt.text = if (errorMsg != null) errorMsg else context.getString(R.string.error_msg_unknown)
                } else {
                    loadingVH.mErrorLayout.visibility = View.GONE
                    loadingVH.mProgressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return tripStatusModels?.size ?: 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == tripStatusModels!!.size - 1 && isLoadingAdded) LOADING else ITEM
    }

    /**
     * Helpers - Pagination
     */
    fun add(tripStatusModel: TripListModelArrayList) {
        tripStatusModels!!.add(tripStatusModel)
        notifyItemInserted(tripStatusModels.size - 1)
    }

    fun addAll(tripDetailsModels: ArrayList<TripListModelArrayList>?) {
        for (tripDetailsModel in tripDetailsModels!!) {
            add(tripDetailsModel)
        }
    }

    private fun remove(tripStatusModel: TripListModelArrayList?) {
        val position = tripStatusModels!!.indexOf(tripStatusModel)
        if (position > -1) {
            tripStatusModels.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun clear() {
        isLoadingAdded = false
        while (itemCount > 0) {
            remove(getItem(0))
        }
    }

    internal fun clearAll() {
        isLoadingAdded = false
        tripStatusModels!!.clear()
        notifyDataSetChanged()
    }

    internal fun addLoadingFooter() {
        isLoadingAdded = true
        add(TripListModelArrayList())
    }

    internal fun removeLoadingFooter() {
        isLoadingAdded = false

        val position = tripStatusModels!!.size - 1
        val tripStatusModel = getItem(position)

        if (tripStatusModel != null) {
            tripStatusModels.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    private fun getItem(position: Int): TripListModelArrayList? {
        return tripStatusModels!![position]
    }

    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     */
    private fun showRetry(show: Boolean, errorMsg: String?) {
        retryPageLoad = show
        notifyItemChanged(tripStatusModels!!.size - 1)

        if (errorMsg != null) this.errorMsg = errorMsg
    }

    protected inner class PastTripsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        internal val tv_country: TextView
        internal val carname: TextView
        internal val seatcount: TextView
        internal val status: TextView
        internal val amountcard: TextView
        internal val imageView: ImageView
        internal var imageLayout: RelativeLayout?=null
        internal val tv_dropLocation:TextView
        internal val tv_pickLocation:TextView
        internal var view_line:View ?=null
        internal var manual_booking: TextView?=null

        internal var date_and_time: TextView?=null
        internal var trip_status: TextView?=null
        internal var pickupaddress: TextView?=null
        internal var destadddress: TextView?=null
        internal var trip_id_button: Button?=null
        internal val btnrate: Button? = null
        internal var card_view: CardView?=null
        internal var mapView: RelativeLayout?=null
         init {
             tv_country = view.findViewById<View>(R.id.datetime) as TextView
             carname = view.findViewById<View>(R.id.carname) as TextView
             status = view.findViewById<View>(R.id.status) as TextView
             tv_pickLocation=view.findViewById(R.id.tv_pick_location) as TextView
             tv_dropLocation=view.findViewById(R.id.tv_drop_location)as TextView
             amountcard = view.findViewById<View>(R.id.amountcard) as TextView
             imageLayout = view.findViewById<View>(R.id.image_layout) as? RelativeLayout
             imageView = view.findViewById<View>(R.id.imageView) as ImageView
             trip_status = view.findViewById<View>(R.id.trip_status) as? TextView
             pickupaddress = view.findViewById<View>(R.id.pickupaddress) as? TextView
             destadddress = view.findViewById<View>(R.id.destadddress) as? TextView
             trip_id_button = view.findViewById<View>(R.id.trip_id_button) as? Button
             mapView = view.findViewById<View>(R.id.static_map) as? RelativeLayout
             view_line = view.findViewById<View>(R.id.line3) as? View
             //btnrate = (Button) view.findViewById(R.id.btnrate);
             card_view = view.findViewById<View>(R.id.card_view) as? CardView
             seatcount = view.findViewById<View>(R.id.seatcount) as TextView
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
        // View Types
        private val ITEM = 0
        private val LOADING = 1
    }
}