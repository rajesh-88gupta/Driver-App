package com.seentechs.newtaxidriver.home.payouts

/**
 *
 * @package     com.seentechs.newtaxidriver
 * @subpackage  Profile
 * @category    PayoutEmailListActivity
 * @author      Seen Technologies
 *
 */

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

import com.google.gson.Gson
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.home.payouts.payout_model_classed.PayPalEmailAdapter


import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.home.payouts.payout_model_classed.PayoutDetail
import com.seentechs.newtaxidriver.home.payouts.payout_model_classed.PayoutDetailResult
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.ConnectionDetector
import com.seentechs.newtaxidriver.common.util.RequestCallback

import java.util.ArrayList

import javax.inject.Inject

import butterknife.ButterKnife
import com.seentechs.newtaxidriver.common.network.AppController

import com.seentechs.newtaxidriver.common.util.Enums.REQ_CURRENCYLIST
import com.seentechs.newtaxidriver.common.views.CommonActivity
import kotlinx.android.synthetic.main.activity_payout_email_list.*

/* ************************************************************
                   Payout Email list Page
Show list of PayPal email address for payout option and to change payout email delete, set default
*************************************************************** */
class PayoutEmailListActivity : CommonActivity(), View.OnClickListener, ServiceListener {

    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var commonMethods: CommonMethods
    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var sessionManager: SessionManager

    internal lateinit var recyclerView: RecyclerView

    internal lateinit var adapter: PayPalEmailAdapter
    internal var paypalemaillist: Array<String>? = null

    internal lateinit var payoutemaillist_title: RelativeLayout
    internal lateinit var payout_addpayout: Button
    internal lateinit var payout_addstripe: Button
    internal var payoutid: String? = null
    internal var payoutoption: Int = 0
    var userid: String? = null

    var context: Context? = null
    lateinit var payoutmain_title: TextView
    protected var isInternetAvailable: Boolean = false
    @Inject
    lateinit var customDialog: CustomDialog

    internal lateinit var payoutDetailResult: PayoutDetailResult
    internal var payoutDetails = ArrayList<PayoutDetail>()

    // Check network available or not
    val networkState: ConnectionDetector
        get() = ConnectionDetector(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payout_email_list)
        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        commonMethods.imageChangeforLocality(this,payoutemaillist_back)
        payoutmain_title = findViewById<View>(R.id.payoutmain_title) as TextView
        payoutemaillist_title = findViewById<View>(R.id.payoutemaillist_title) as RelativeLayout
        payout_addpayout = findViewById<View>(R.id.payout_addpayout) as Button
        payout_addstripe = findViewById<View>(R.id.payout_addstripe) as Button
        payout_addpayout.visibility = View.GONE
        payout_addstripe.visibility = View.GONE
        payout_addpayout.setOnClickListener(this)
        payout_addstripe.setOnClickListener(this)
        payoutemaillist_title.setOnClickListener(this)



        recyclerView = findViewById<View>(R.id.recyclerview) as RecyclerView

        adapter = PayPalEmailAdapter(this, this, payoutDetails)

        // recyclerView.setHasFixedSize(true);
        recyclerView.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        //recyclerView.addItemDecoration(new VerticalLineDecorator(2));
        recyclerView.adapter = adapter
        //load(0);
        isInternetAvailable = networkState.isConnectingToInternet

    }


    override fun onResume() {
        super.onResume()
        isInternetAvailable = networkState.isConnectingToInternet
        if (isInternetAvailable) {
            loadPayout()

        } else {
            snackBar(resources.getString(R.string.Interneterror))
        }
    }

    /* @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            adapter = new PayPalEmailAdapter(this,this, makent_host_modelList);

        }
    }*/

    override fun onBackPressed() {
        super.onBackPressed()

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.payoutemaillist_title -> {
                onBackPressed()
            }

            R.id.payout_addpayout -> {
                // Get address details for add new payout option
                val x = Intent(applicationContext, PayoutAddressDetailsActivity::class.java)
                startActivity(x)
                //                finish();
            }

            R.id.payout_addstripe -> {
                // Get address details for add new payout option

                val x = Intent(applicationContext, PayoutBankDetailsActivity::class.java)
                startActivity(x)

            }
        }
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (jsonResp.isSuccess) {
            onSuccessPayout(jsonResp) // onSuccessResponse call method
        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {

            payoutmain_title.visibility = View.VISIBLE
            payout_addpayout.visibility = View.VISIBLE
            payout_addstripe.visibility = View.VISIBLE
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()

        //snackBar();
    }

    // Get PayPal email address from API
    fun loadPayout() {
        commonMethods.showProgressDialog(this)
        apiService.payoutDetails(sessionManager.accessToken!!).enqueue(RequestCallback(REQ_CURRENCYLIST, this))
    }

    fun onSuccessPayout(jsonResp: JsonResponse) {
        payoutDetailResult = gson.fromJson(jsonResp.strResponse, PayoutDetailResult::class.java)
        payoutDetails.clear()
        val payDetail = payoutDetailResult.payout_details
        payoutDetails.addAll(payDetail)
        adapter.notifyDataChanged()
        if (payoutDetails.size > 0) {

            payoutmain_title.visibility = View.GONE

            payout_addpayout.visibility = View.VISIBLE
            payout_addstripe.visibility = View.VISIBLE
        } else {

            payoutmain_title.visibility = View.VISIBLE
            payout_addpayout.visibility = View.VISIBLE
            payout_addstripe.visibility = View.VISIBLE
        }
    }

    //Show network error and exception
    fun snackBar(statusmessage: String) {
        // Create the Snackbar
        val snackbar = Snackbar.make(recyclerView, "", Snackbar.LENGTH_LONG)
        // Get the Snackbar's layout view
        val layout = snackbar.view as Snackbar.SnackbarLayout
        // Hide the text
        val textView = layout.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
        textView.visibility = View.INVISIBLE

        // Inflate our custom view
        val snackView = layoutInflater.inflate(R.layout.snackbar, null)
        // Configure the view

        val snackbar_background = snackView.findViewById<View>(R.id.snackbar) as RelativeLayout
        snackbar_background.setBackgroundColor(ContextCompat.getColor(this,R.color.app_background))

        val button = snackView.findViewById<View>(R.id.snackbar_action) as Button
        button.visibility = View.GONE
        button.text = resources.getString(R.string.showpassword)
        button.setTextColor(ContextCompat.getColor(this,R.color.app_background))
        button.setOnClickListener { }

        val textViewTop = snackView.findViewById<View>(R.id.snackbar_text) as TextView
        if (isInternetAvailable) {
            textViewTop.text = statusmessage
        } else {
            textViewTop.text = resources.getString(R.string.Interneterror)
        }
        // textViewTop.setTextSize(getResources().getDimension(R.dimen.midb));
        textViewTop.setTextColor(ContextCompat.getColor(this,R.color.white))

        // Add the view to the Snackbar's layout
        layout.addView(snackView, 0)
        // Show the Snackbar
        val snackBarView = snackbar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this,R.color.app_background))
        snackbar.show()

    }
}
