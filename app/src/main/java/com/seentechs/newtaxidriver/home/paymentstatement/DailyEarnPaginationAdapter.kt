package com.seentechs.newtaxidriver.home.paymentstatement

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.helper.Constants
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.home.datamodel.WeeklyStatementModel
import com.seentechs.newtaxidriver.home.interfaces.PaginationAdapterCallback
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class DailyEarnPaginationAdapter(private val context: Context, private val mCallback: PaginationAdapterCallback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    @Inject
    lateinit var commonMethods: CommonMethods

    private var isLoadingAdded = false
    private var retryPageLoad = false
    private var errorMsg: String? = null

    private var dailyTripsList: ArrayList<WeeklyStatementModel.Statement>? = ArrayList()
    lateinit var dailyTripModel: WeeklyStatementModel.Statement

    init {
        AppController.getAppComponent().inject(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var viewHolder: RecyclerView.ViewHolder? = null
        val inflater = LayoutInflater.from(parent.context)
        val viewItem: View
        when (viewType) {
            Constants.VIEW_TYPE_ITEM -> {
                viewItem = inflater.inflate(R.layout.daily_earning_layout, parent, false)
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
        return if (dailyTripsList == null) 0 else dailyTripsList!!.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val dailyTripStatement = dailyTripsList?.get(position) // Upcoming Detail
        when (getItemViewType(position)) {
            Constants.VIEW_TYPE_ITEM -> {
                val dailyTripsHolder = holder as ViewHolder
                dailyTripsHolder.dailytrip.text = dailyTripStatement?.date?.let { DateFirstUserFormat(it) }
                dailyTripsHolder.dailyamount.text = dailyTripStatement?.driverEarnings
                dailyTripsHolder.rlParent.setOnClickListener {
                    val intent = Intent(context, DailyEarningDetails::class.java)
                    intent.putExtra("daily_date", dailyTripStatement?.date)
                    context.startActivity(intent)
                }
            }
            Constants.VIEW_TYPE_LOADING -> {
                val loadingVH = holder as PayStatementPaginationAdapter.LoadingViewHolder

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
        dailyTripModel = dailyTripsList?.get(position)!!
        return if (position == dailyTripsList?.size!! - 1 && isLoadingAdded) Constants.VIEW_TYPE_LOADING else Constants.VIEW_TYPE_ITEM
    }


    /*
      Helpers - Pagination
    */
    fun add(dailyTripStatement: WeeklyStatementModel.Statement) {
        dailyTripsList?.add(dailyTripStatement)
        notifyItemInserted(dailyTripsList?.size!! - 1)
    }

    fun addAll(dailyTriplist: ArrayList<WeeklyStatementModel.Statement>) {
        for (dailyStatementModel in dailyTriplist) {
            add(dailyStatementModel)
        }
    }

    private fun remove(dailyTripStatementModel: WeeklyStatementModel.Statement) {
        val position = dailyTripsList!!.indexOf(dailyTripStatementModel)
        if (position > -1) {
            dailyTripsList!!.removeAt(position)
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
        dailyTripsList!!.clear()
        notifyDataSetChanged()
    }


    fun addLoadingFooter() {
        isLoadingAdded = true
        add(WeeklyStatementModel.Statement())
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false

        val position = dailyTripsList!!.size - 1
        val dailyTripStatement = getItem(position)

        if (dailyTripStatement != null) {
            dailyTripsList!!.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    private fun getItem(position: Int): WeeklyStatementModel.Statement {
        return dailyTripsList?.get(position)!!
    }

    fun DateFirstUserFormat(date: String): String {
        val input = SimpleDateFormat("yyyy-MM-dd")
        val output = SimpleDateFormat("dd-MM-yyyy")
        var d1: Date? = null
        try {
            d1 = input.parse(date)
            println(output.format(d1))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return output.format(d1)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dailytrip: TextView
        val dailyamount: TextView
        val dailyArrow: TextView
        internal var rlParent: RelativeLayout

        init {
            dailytrip = view.findViewById<View>(R.id.dailytrip) as TextView
            dailyamount = view.findViewById<View>(R.id.dailyamount) as TextView
            dailyArrow = view.findViewById<View>(R.id.daily_earning_arrow) as TextView
            rlParent = view.findViewById<View>(R.id.rl_parent) as RelativeLayout
            commonMethods.imageChangeforLocality(context, dailyArrow)
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
        notifyItemChanged(dailyTripsList!!.size - 1)

        if (errorMsg != null) this.errorMsg = errorMsg
    }
}