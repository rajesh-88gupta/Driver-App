package com.seentechs.newtaxidriver.home.paymentstatement

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.helper.Constants
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.home.datamodel.DailyStatement
import com.seentechs.newtaxidriver.home.interfaces.PaginationAdapterCallback
import java.util.*
import javax.inject.Inject

class DailyHoursPaginationAdapter(private val context: Context, private val mCallback: PaginationAdapterCallback, private val dailyEarngCallback: DailyEarngCallback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    @Inject
    lateinit var commonMethods: CommonMethods

    private var isLoadingAdded = false
    private var retryPageLoad = false
    private var errorMsg: String? = null

    private var dailyTripsHoursList: ArrayList<DailyStatement.Statement>? = ArrayList()
    lateinit var dailyTripsHoursModel: DailyStatement.Statement

    init {
        AppController.getAppComponent().inject(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var viewHolder: RecyclerView.ViewHolder? = null
        val inflater = LayoutInflater.from(parent.context)
        val viewItem: View
        when (viewType) {
            Constants.VIEW_TYPE_ITEM -> {
                viewItem = inflater.inflate(R.layout.trip_earning_layout, parent, false)
                viewHolder = ViewHolder(viewItem)
            }
            Constants.VIEW_TYPE_LOADING -> {
                val viewLoading = inflater.inflate(R.layout.item_progress, parent, false)
                viewHolder = LoadingViewHolder(viewLoading)
            }
        }
        return viewHolder!!
    }

    override fun getItemCount(): Int {
        return if (dailyTripsHoursList == null) 0 else dailyTripsHoursList!!.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val dailyTripsHoursStatement = dailyTripsHoursList?.get(position) // Upcoming Detail
        when (getItemViewType(position)) {
            Constants.VIEW_TYPE_ITEM -> {
                val dailyTripsHoursHolder = holder as ViewHolder
                dailyTripsHoursHolder.dailytrip.text = dailyTripsHoursStatement?.time
                dailyTripsHoursHolder.dailyamount.text = dailyTripsHoursStatement?.driver_earning
                dailyTripsHoursHolder.parentLay.setOnClickListener { dailyEarngCallback.onItemClick(dailyTripsHoursStatement?.id) }
            }
            Constants.VIEW_TYPE_LOADING -> {
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

    override fun getItemViewType(position: Int): Int {
        dailyTripsHoursModel = dailyTripsHoursList?.get(position)!!
        return if (position == dailyTripsHoursList?.size!! - 1 && isLoadingAdded) Constants.VIEW_TYPE_LOADING else Constants.VIEW_TYPE_ITEM
    }


    /*
      Helpers - Pagination
    */
    fun add(dailyTripsHoursStatement: DailyStatement.Statement) {
        dailyTripsHoursList?.add(dailyTripsHoursStatement)
        notifyItemInserted(dailyTripsHoursList?.size!! - 1)
    }

    fun addAll(dailyTripsHourslist: ArrayList<DailyStatement.Statement>) {
        for (dailyStatementModel in dailyTripsHourslist) {
            add(dailyStatementModel)
        }
    }

    private fun remove(dailyTripsHoursStatementModel: DailyStatement.Statement) {
        val position = dailyTripsHoursList!!.indexOf(dailyTripsHoursStatementModel)
        if (position > -1) {
            dailyTripsHoursList!!.removeAt(position)
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
        dailyTripsHoursList!!.clear()
        notifyDataSetChanged()
    }


    fun addLoadingFooter() {
        isLoadingAdded = true
        add(DailyStatement.Statement())
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false

        val position = dailyTripsHoursList!!.size - 1
        val dailyTripsHoursStatement = getItem(position)

        if (dailyTripsHoursStatement != null) {
            dailyTripsHoursList!!.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    private fun getItem(position: Int): DailyStatement.Statement {
        return dailyTripsHoursList?.get(position)!!
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dailytrip: TextView
        val dailyamount: TextView
        val dailyarrow: TextView
        val parentLay: RelativeLayout

        init {
            dailytrip = view.findViewById<View>(R.id.dailytrip) as TextView
            dailyamount = view.findViewById<View>(R.id.dailyamount) as TextView
            dailyarrow = view.findViewById<View>(R.id.trip_earning_arrow) as TextView
            parentLay = view.findViewById(R.id.rl_parent)
            //commonMethods.imageChangeforLocality(context, dailyarrow)
        }
    }

    inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

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

    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param show
     * @param errorMsg to display if page load fails
     */
    fun showRetry(show: Boolean, errorMsg: String?) {
        retryPageLoad = show
        notifyItemChanged(dailyTripsHoursList!!.size - 1)

        if (errorMsg != null) this.errorMsg = errorMsg
    }

    interface DailyEarngCallback {
        fun onItemClick(tripId: String?)
    }
}