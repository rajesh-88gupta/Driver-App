package com.seentechs.newtaxidriver.home.payouts.payout_model_classed

/**
 *
 * @package     com.seentechs.newtaxidriver
 * @subpackage  adapter/host
 * @category    PayPalEmailAdapter
 * @author      Seen Technologies
 *
 */

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast

import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.home.payouts.PayoutEmailListActivity
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.ConnectionDetector
import com.seentechs.newtaxidriver.common.util.RequestCallback

import java.util.ArrayList

import javax.inject.Inject

import butterknife.ButterKnife
import com.seentechs.newtaxidriver.common.network.AppController

@SuppressLint("ViewHolder")
class PayPalEmailAdapter(activity: Activity, context: Context, private val modelItems: ArrayList<PayoutDetail>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ServiceListener {

    val TYPE_Explore = 0
    val TYPE_LOAD = 1
    internal var loadMoreListener: OnLoadMoreListener? = null
    internal var isLoading = false
    internal var isMoreDataAvailable = true
    protected var isInternetAvailable: Boolean = false
    internal var selected = -1
    var context: Context
    internal lateinit var payoutid: String
    internal lateinit var type: String
    internal lateinit var userid: String
    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var sessionManager: SessionManager


    val networkState: ConnectionDetector
        get() = ConnectionDetector(context)

    init {
        this.context = context

        ButterKnife.bind(context as Activity)
        AppController.getAppComponent().inject(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        return if (viewType == TYPE_Explore) {
            MovieHolder(inflater.inflate(R.layout.payout_paypal_list, parent, false))
        } else {
            LoadHolder(inflater.inflate(R.layout.row_load, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (position >= itemCount - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {
            isLoading = true
            loadMoreListener!!.onLoadMore()
        }




        if (getItemViewType(position) == TYPE_Explore) {

            (holder as MovieHolder).bindData(modelItems[position], position)
        }
        //No else part needed as load holder doesn't bind any data
    }

    override fun getItemViewType(position: Int): Int {
        return if (modelItems[position].paypalEmail == "load") {
            TYPE_LOAD
        } else {
            TYPE_Explore
        }
    }

    override fun getItemCount(): Int {
        return modelItems.size
    }


    /* VIEW HOLDERS */

    internal inner class MovieHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var paypal_email: TextView
        var payout_default: TextView
        var paypalemailmore: RelativeLayout
        var paypalemailid: RelativeLayout
        var makedefault: Button
        var delete: Button


        init {
            paypal_email = itemView.findViewById<View>(R.id.paypal_email) as TextView
            payout_default = itemView.findViewById<View>(R.id.paypal_ready) as TextView
            paypalemailid = itemView.findViewById<View>(R.id.paypalemailid) as RelativeLayout
            paypalemailmore = itemView.findViewById<View>(R.id.paypalemailmore) as RelativeLayout
            delete = itemView.findViewById<View>(R.id.paypal_delete) as Button
            makedefault = itemView.findViewById<View>(R.id.paypal_default) as Button

        }

        fun bindData(movieModel: PayoutDetail, position: Int) {

            paypal_email.text = movieModel.paypalEmail
            val screenWidth = context.resources.displayMetrics.widthPixels

            if (movieModel.isDefault.equals("No")) {
                payout_default.visibility = View.GONE
            } else {
                payout_default.visibility = View.VISIBLE
            }

            if (selected == position) {
                paypalemailmore.isClickable = true
                if (context.resources.getString(R.string.layout_direction) == "0") {
                    paypalemailid.animate().translationX((-(screenWidth / 2 + 220)).toFloat()).duration = 200
                } else {
                    paypalemailid.animate().translationX((screenWidth / 2 + 100).toFloat()).duration = 200
                }
            } else {
                paypalemailmore.isClickable = false
                paypalemailid.animate().translationX(0f).duration = 200
            }
            paypalemailmore.isClickable = false

            paypalemailid.setOnClickListener {
                if (movieModel.isDefault.equals("No")) {
                    if (!paypalemailmore.isClickable) {
                        paypalemailmore.isClickable = true
                        //paypalemailid.animate().translationX(-(screenWidth / 2 + 100)).setDuration(200);
                        if (selected == position) {
                            selected = -1
                        } else {

                            selected = position
                        }
                        notifyDataSetChanged()
                    } else {
                        paypalemailmore.isClickable = false
                        //paypalemailid.animate().translationX(0).setDuration(200);
                        selected = -1
                    }
                } else {

                }
            }
            delete.setOnClickListener {
                isInternetAvailable = networkState.isConnectingToInternet
                if (isInternetAvailable) {
                    userid = sessionManager.accessToken!!
                    payoutid = movieModel.payoutId
                    type = "delete"
                    UpdatePayoutDetails()
                    //	payoutemail.payoutOption(view,position,0); // // 0  delete and 1 to set default
                    paypalemailmore.isClickable = false
                    paypalemailid.animate().translationX(0f).duration = 200
                    //notifyItemRemoved(getAdapterPosition());
                } else {
                    Toast.makeText(context, "No Internet Connection", Toast.LENGTH_LONG).show()
                }
            }
            makedefault.setOnClickListener {
                isInternetAvailable = networkState.isConnectingToInternet
                if (isInternetAvailable) {
                    userid = sessionManager.accessToken!!
                    payoutid = movieModel.payoutId
                    type = "default"
                    UpdatePayoutDetails()
                    paypalemailmore.isClickable = false
                    paypalemailid.animate().translationX(0f).duration = 200
                } else {
                    Toast.makeText(context, "No Internet Connection", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    internal class LoadHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun setMoreDataAvailable(moreDataAvailable: Boolean) {
        isMoreDataAvailable = moreDataAvailable
    }

    /* notifyDataSetChanged is final method so we can't override it
         call adapter.notifyDataChanged(); after update the list
         */
    fun notifyDataChanged() {
        notifyDataSetChanged()
        isLoading = false
    }


    interface OnLoadMoreListener {
        fun onLoadMore()
    }

    fun setLoadMoreListener(loadMoreListener: OnLoadMoreListener) {
        this.loadMoreListener = loadMoreListener
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (jsonResp.isSuccess) {

            val x = Intent(context, PayoutEmailListActivity::class.java)
            //activity.setResult(Activity.RESULT_OK,x);
            (context as Activity).finish()
            context.startActivity(x)
        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
    }

    fun UpdatePayoutDetails() {
        apiService.payoutChanges(userid, payoutid, type).enqueue(RequestCallback(this))
    }

    companion object {

        internal lateinit var context: Context

        protected val TAG: String? = null
        internal var check = false
    }

}