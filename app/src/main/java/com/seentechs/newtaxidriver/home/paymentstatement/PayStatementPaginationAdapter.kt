package com.seentechs.newtaxidriver.home.paymentstatement

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.helper.Constants.VIEW_TYPE_ITEM
import com.seentechs.newtaxidriver.common.helper.Constants.VIEW_TYPE_LOADING
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.home.datamodel.WeeklyTripStatement


import com.seentechs.newtaxidriver.home.interfaces.PaginationAdapterCallback
import javax.inject.Inject

class PayStatementPaginationAdapter(private val context: Context, private val mCallback: PaginationAdapterCallback, private var dailyCallback: DailyCallback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    @Inject
    lateinit var commonMethods: CommonMethods

    private var isLoadingAdded = false
    private var retryPageLoad = false
    private var errorMsg: String? = null

    private var symbol: String? = null

    private var weeklyTripsList: ArrayList<WeeklyTripStatement.Statement>? = ArrayList()
    lateinit var weeklyTripModel: WeeklyTripStatement.Statement

    init {
        AppController.getAppComponent().inject(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var viewHolder: RecyclerView.ViewHolder? = null
        val inflater = LayoutInflater.from(parent.context)
        val viewItem: View
        when (viewType) {
            VIEW_TYPE_ITEM -> {
                viewItem = inflater.inflate(R.layout.pay_statement_itemlayout, parent, false)
                viewHolder = ViewHolder(viewItem)
            }
            VIEW_TYPE_LOADING -> {
                val viewLoading = inflater.inflate(R.layout.item_progress, parent, false)
                viewHolder = LoadingViewHolder(viewLoading)
            }
        }
        return viewHolder!!
    }

    override fun getItemCount(): Int {
        return if (weeklyTripsList == null) 0 else weeklyTripsList!!.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val weeklyTripStatement = weeklyTripsList?.get(position) // Upcoming Detail
        when (getItemViewType(position)) {
            VIEW_TYPE_ITEM -> {
                val weeklyTripsHolder = holder as ViewHolder
                weeklyTripsHolder.tripdatetime.text = weeklyTripStatement?.week
                weeklyTripsHolder.tripamount.text = symbol + " " + weeklyTripStatement?.driverEarnings
                weeklyTripsHolder.parentLay.setOnClickListener { weeklyTripStatement?.date?.let { it1 -> dailyCallback.onItemClick(it1) } }
            }
            VIEW_TYPE_LOADING -> {
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

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tripdatetime: TextView
        val tripamount: TextView
        val trippayback: TextView
        val parentLay: RelativeLayout

        init {
            tripdatetime = view.findViewById(R.id.tripdatetime)
            tripamount = view.findViewById(R.id.tripamount)
            trippayback = view.findViewById(R.id.pay_statement_back)
            parentLay = view.findViewById(R.id.parent_lay)
            //commonMethods.imageChangeforLocality(context, trippayback)
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

    override fun getItemViewType(position: Int): Int {
        weeklyTripModel = weeklyTripsList?.get(position)!!
        return if (position == weeklyTripsList?.size!! - 1 && isLoadingAdded)  VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    fun getSymbol(symbol: String) {
        this.symbol = symbol
    }
    /*
      Helpers - Pagination
    */
    fun add(weeklyTripStatement: WeeklyTripStatement.Statement) {
        weeklyTripsList?.add(weeklyTripStatement)
        notifyItemInserted(weeklyTripsList?.size!! - 1)
    }

    fun addAll(weeklyTriplist: ArrayList<WeeklyTripStatement.Statement>) {
        for (weeklyStatementModel in weeklyTriplist) {
            add(weeklyStatementModel)
        }
    }

    private fun remove(weeklyTripStatementModel: WeeklyTripStatement.Statement) {
        val position = weeklyTripsList!!.indexOf(weeklyTripStatementModel)
        if (position > -1) {
            weeklyTripsList!!.removeAt(position)
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
        weeklyTripsList!!.clear()
        notifyDataSetChanged()
    }


    fun addLoadingFooter() {
        isLoadingAdded = true
        add(WeeklyTripStatement().Statement())
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false

        val position = weeklyTripsList!!.size - 1
        val weeklyTripStatement = getItem(position)

        if (weeklyTripStatement != null) {
            weeklyTripsList!!.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    private fun getItem(position: Int): WeeklyTripStatement.Statement {
        return weeklyTripsList?.get(position)!!
    }

    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param show
     * @param errorMsg to display if page load fails
     */
    fun showRetry(show: Boolean, errorMsg: String?) {
        retryPageLoad = show
        notifyItemChanged(weeklyTripsList!!.size - 1)

        if (errorMsg != null) this.errorMsg = errorMsg
    }


    interface DailyCallback {
        fun onItemClick(date: String)
    }
}