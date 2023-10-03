package com.seentechs.newtaxidriver.home.fragments.Referral

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.StyleSpan
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView

import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.gson.Gson
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.home.datamodel.ReferredFriendsModel
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.RequestCallback

import javax.inject.Inject

import butterknife.ButterKnife
import butterknife.BindView
import butterknife.OnClick
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.network.AppController

import com.seentechs.newtaxidriver.common.util.CommonKeys.CompletedReferralArray
import com.seentechs.newtaxidriver.common.util.CommonKeys.IncompleteReferralArray
import com.seentechs.newtaxidriver.common.views.CommonActivity
import kotlinx.android.synthetic.main.app_activity_show_referral_options.*

class ShowReferralOptionsActivity : CommonActivity(), ServiceListener {

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
    @BindView(R.id.rv_in_completed_referrals)
    lateinit var rvIncompletedReferrals: RecyclerView

    @BindView(R.id.rv_completed_referrals)
    lateinit var rvCompletedReferrals: RecyclerView


    @BindView(R.id.constraintLayout_in_completed_friends)
    lateinit var cvIncompleteFriends: ConstraintLayout

    @BindView(R.id.constraintLayout_completed_friends)
    lateinit var cvCompleteFriends: ConstraintLayout

    @BindView(R.id.constraintLayout_referral_code)
    lateinit var cvReferralHeader: ConstraintLayout

    @BindView(R.id.tv_referral_code)
    lateinit var tvReferralCode: TextView

    @BindView(R.id.imag_share)
    lateinit var tv_share_option: ImageView


    @BindView(R.id.tv_total_earned)
    lateinit var tvTotalEarned: TextView

    @BindView(R.id.tv_amount)
    lateinit var tvEarnedAmount: TextView

    @BindView(R.id.tv_referral_benifit_text)
    lateinit var tvReferralBenifitStatement: TextView

    @BindView(R.id.rlt_share)
    lateinit var rltShare: RelativeLayout

    @BindView(R.id.scv_referal)
    lateinit var scvReferal: ScrollView

    @BindView(R.id.remaing_referral_amount)
    lateinit var remainingReferral: TextView


    @BindView(R.id.tv_no_referrals_yet)
    lateinit var tvNoReferralsYet: TextView

    private var referralCode = ""
    private var referralLink = ""
    private lateinit var referredFriendsModel: ReferredFriendsModel

    @OnClick(R.id.imag_copy)
    fun copy() {
        connect()
    }

    @OnClick(R.id.imag_share)
    fun share() {
        shareMyReferralCode()
    }

    @OnClick(R.id.back)
    fun backPressed() {
        onBackPressed()
    }

    @OnClick(R.id.imag_copy)
    fun connect() {
        CommonMethods.copyContentToClipboard(this, referralCode)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_activity_show_referral_options)
        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        commonMethods.setheaderText(resources.getString(R.string.referral),common_header)
        //commonMethods.imageChangeforLocality(this,arrow)
        dialog = commonMethods.getAlertDialog(this)
        scvReferal.visibility = View.GONE
        initView()
    }

    /**
     * init Views For Referral
     */
    private fun initView() {
        showOrHideReferralAccordingToSessionData()
        getReferralInformationFromAPI()
    }

    /**
     * Hide and Show the Referral  Based on Referral Enable
     */
    private fun showOrHideReferralAccordingToSessionData() {
        if (sessionManager.isReferralOptionEnabled) {
            cvReferralHeader.visibility = View.VISIBLE
        } else {
            cvReferralHeader.visibility = View.GONE
        }
    }

    /**
     * get Referral info for user
     */
    private fun getReferralInformationFromAPI() {
        commonMethods.showProgressDialog(this)
        apiService.getReferralDetails(sessionManager.accessToken!!).enqueue(RequestCallback(this))
    }


    /**
     * Get My Referral Code
     */
    fun shareMyReferralCode() {
        val share = Intent(Intent.ACTION_SEND)
        share.type = "text/plain"
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        // Add data to the intent, the receiving app will decide
        // what to do with it.
        share.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.app_name) + " " + resources.getString(R.string.referral))
        share.putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.invite_msg) + " " + spannableString(referralCode) + " " + referralLink)
        startActivity(Intent.createChooser(share, resources.getString(R.string.share_my_code)))
    }

    /**
     * Combine Your Referral Code
     */
    private fun spannableString(referralCode: String): String {
        val ss = SpannableString(referralCode)
        ss.setSpan(StyleSpan(Typeface.BOLD), 0, referralCode.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return ss.toString()
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (jsonResp.isSuccess) {
            scvReferal.visibility = View.VISIBLE
            onSuccessResult(jsonResp.strResponse)
            //jsonResp.strResponse.let { onSuccessResult(it) }
        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }

    /**
     * onSuccessResponse of the Referral
     */
    private fun onSuccessResult(strResponse: String) {
        referredFriendsModel = gson.fromJson(strResponse, ReferredFriendsModel::class.java)
        if (!TextUtils.isEmpty(referredFriendsModel.remainingReferral)) {
            remainingReferral.text = referredFriendsModel.remainingReferral
        } else {
            remainingReferral.text = sessionManager.currencySymbol + "0"
        }
        updateReferralCodeInUI()
        if (referredFriendsModel.pendingReferrals?.size != 0 || referredFriendsModel.completedReferrals?.size != 0) {
            showReferralsNotAvailable(true)
            proceedCompleteReferralDetails()
            proceedIncompleteReferralDetails()
        } else {
            showReferralsNotAvailable(false)
        }
    }

    /**
     * Update Your Referral UI
     */
    private fun updateReferralCodeInUI() {
        referralCode = referredFriendsModel.referralCode.toString()
        referralLink = referredFriendsModel.referralLink.toString()
        tvReferralCode.text = referralCode
        if ("1".equals(resources.getString(R.string.layout_direction), ignoreCase = true)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tvReferralBenifitStatement.text = resources.getString(R.string.max_referral_earning_statement, setCurrencyFrontForRTL(Html.fromHtml(referredFriendsModel.referralAmount, Html.FROM_HTML_MODE_LEGACY).toString()))
            }else{
                tvReferralBenifitStatement.text = resources.getString(R.string.max_referral_earning_statement, setCurrencyFrontForRTL(Html.fromHtml(referredFriendsModel.referralAmount).toString()))
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tvReferralBenifitStatement.text = resources.getString(R.string.max_referral_earning_statement, Html.fromHtml(referredFriendsModel.referralAmount, Html.FROM_HTML_MODE_LEGACY).toString())
            }else{
                tvReferralBenifitStatement.text = resources.getString(R.string.max_referral_earning_statement, Html.fromHtml(referredFriendsModel.referralAmount).toString())
            }
        }
        //tvTotalEarned.append(referredFriendsModel.getTotalEarning().toString());
        tvEarnedAmount.text = referredFriendsModel.totalEarning
    }

    /**
     * Currency symbol  RTL for Amount
     */
    private fun setCurrencyFrontForRTL(amount: String): String {
        println("amount $amount")
        val currency = amount[0].toString()
        println("currency $currency")
        return amount.replace(currency, " ") + currency
    }

    /**
     * Referral Hide
     */
    private fun showReferralsNotAvailable(show: Boolean) {
        if (show) {
            cvIncompleteFriends.visibility = View.VISIBLE
            cvCompleteFriends.visibility = View.VISIBLE
            tvNoReferralsYet.visibility = View.GONE
        } else {
            cvIncompleteFriends.visibility = View.GONE
            cvCompleteFriends.visibility = View.GONE
            tvNoReferralsYet.visibility = View.VISIBLE
        }
    }

    /**
     * InComplete ReferralDetails
     */
    private fun proceedIncompleteReferralDetails() {
        if (referredFriendsModel.pendingReferrals?.size != 0) {
            rvIncompletedReferrals.setHasFixedSize(true)
            val layoutManager = LinearLayoutManager(this)
            rvIncompletedReferrals.layoutManager = layoutManager
            rvIncompletedReferrals.adapter = ReferralFriendsListRecyclerViewAdapter(this, referredFriendsModel.pendingReferrals, IncompleteReferralArray)
        } else {
            cvIncompleteFriends.visibility = View.GONE
        }
    }

    /**
     * Proceed Completed ReferralDetails
     */
    private fun proceedCompleteReferralDetails() {
        if (referredFriendsModel.completedReferrals?.size != 0) {
            rvCompletedReferrals.setHasFixedSize(true)
            val layoutManager = LinearLayoutManager(this)
            rvCompletedReferrals.layoutManager = layoutManager
            rvCompletedReferrals.adapter = ReferralFriendsListRecyclerViewAdapter(this, referredFriendsModel.completedReferrals, CompletedReferralArray)
        } else {
            cvCompleteFriends.visibility = View.GONE
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {

    }
}