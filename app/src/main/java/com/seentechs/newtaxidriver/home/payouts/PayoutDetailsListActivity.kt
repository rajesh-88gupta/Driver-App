package com.seentechs.newtaxidriver.home.payouts

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.RequestCallback
import com.seentechs.newtaxidriver.common.views.CommonActivity
import com.seentechs.newtaxidriver.home.datamodel.BankDetailsModel
import com.seentechs.newtaxidriver.home.datamodel.PayoutDetailsList
import com.seentechs.newtaxidriver.home.datamodel.PayoutDetailsListModel
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import kotlinx.android.synthetic.main.app_activity_payout_details_list.*
import kotlinx.android.synthetic.main.app_activity_trip_details.common_header
import javax.inject.Inject

class PayoutDetailsListActivity : CommonActivity(), PayoutDetailsListAdapter.OnPayoutClick, ServiceListener {


    override fun onPayoutClicK(payoutType: String, payoutId: String, pos: Int) {

        this.payoutType = payoutType
        this.payoutId = payoutId

        if (Integer.parseInt(this.payoutId) == 0 || payoutDetailsModel.paymentlist[pos].isDefault) {
            addPayoutPageRedirection(pos)
        } else
            showBottomSheet(pos)
    }

    lateinit private var payoutDetailsModel: PayoutDetailsList

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var alertDialog: AlertDialog
    private var payoutFunctionality: String = ""
    private var payoutType: String = ""
    private var payoutId: String = ""


    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var customDialog: CustomDialog


    lateinit var dialog: BottomSheetDialog


    lateinit var tvEdit: TextView
    var payoutDetailsList = ArrayList<PayoutDetailsListModel>()

    @OnClick(R.id.back)
    fun onClickclose() {
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_activity_payout_details_list)

        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        /**Commmon Header Text View */
        commonMethods.setheaderText(resources.getString(R.string.payout_details), common_header)


    }

    /*
    * To update Bank Details
    * */
    private fun updatePayoutDetails() {
        commonMethods.showProgressDialog(this as AppCompatActivity)
        apiService.getPayoutDetails(getPayoutListDetails()).enqueue(RequestCallback(this))
    }


    /**
     * Bank Details params
     *
     * @return hash Map contains Bank Details
     */

    fun getPayoutListDetails(): HashMap<String, String> {

        val hashMap: HashMap<String, String> = HashMap<String, String>()
        hashMap["token"] = sessionManager.accessToken!!
        hashMap["type"] = this.payoutFunctionality
        hashMap["payout_id"] = payoutId
        return hashMap
    }


    private fun showBottomSheet(pos: Int) {


        val tvEdit: TextView
        val tvDelete: TextView
        val tvDefault: TextView
        val tvTitle: TextView

        val view = layoutInflater.inflate(R.layout.payout_edit_dialog, null)

        tvEdit = view.findViewById(R.id.tv_edit)
        tvDelete = view.findViewById(R.id.tv_delete)
        tvDefault = view.findViewById(R.id.tv_default)
        tvTitle = view.findViewById(R.id.tv_payout_title)

        if (payoutDetailsModel.paymentlist.get(pos).key.equals("stripe")) {
            tvEdit.text = resources.getString(R.string.update)
        } else {
            tvEdit.text = resources.getString(R.string.edit)
        }
        tvTitle.text = payoutDetailsModel.paymentlist.get(pos).value + "\t" + resources.getString(R.string.payout)

        tvEdit.setOnClickListener {

            addPayoutPageRedirection(pos)
            dialog.dismiss()

        }

        tvDelete.setOnClickListener {
            this.payoutFunctionality = "delete"
            updatePayoutDetails()
            dialog.dismiss()
        }

        tvDefault.setOnClickListener {
            this.payoutFunctionality = "default"
            updatePayoutDetails()
            dialog.dismiss()
        }

        dialog = BottomSheetDialog(this, R.style.DialogStyle)
        dialog.setContentView(view)
        if (!dialog.isShowing) {
            dialog.show()
        }

    }

    override fun onResume() {
        super.onResume()
        updatePayoutDetails()
    }

    private fun addPayoutPageRedirection(pos: Int) {

        val payoutdata = payoutDetailsModel.paymentlist[pos].payoutData
        val bankdetailsModel = BankDetailsModel()
        bankdetailsModel.account_holder_name = payoutdata.holder_name
        bankdetailsModel.account_number = payoutdata.account_number
        bankdetailsModel.bank_code = payoutdata.branch_code
        bankdetailsModel.bank_location = payoutdata.bank_location
        bankdetailsModel.bank_name = payoutdata.bank_name

        when {
            payoutType.equals("paypal") -> {
                val x = Intent(applicationContext, PayoutAddressDetailsActivity::class.java)
                x.putExtra("payoutData", payoutdata)
                startActivity(x)
            }
            payoutType.equals("stripe") -> {
                val x = Intent(applicationContext, PayoutBankDetailsActivity::class.java)
                startActivity(x)
            }
            payoutType.equals("bank_transfer") -> {
                val x = Intent(applicationContext, BankDetailsActivity::class.java)
                x.putExtra("bankdetailsModel", bankdetailsModel)
                startActivity(x)
            }
        }
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data)) commonMethods.showMessage(this, alertDialog, data)
            return
        }
        if (jsonResp.isSuccess) {
            onSuccessPayoutDetails(jsonResp)
        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, alertDialog, jsonResp.statusMsg)
        }

    }

    private fun onSuccessPayoutDetails(jsonResp: JsonResponse) {

        payoutDetailsModel = gson.fromJson(jsonResp.strResponse, PayoutDetailsList::class.java)

        val myRecyclerViewAdapter = PayoutDetailsListAdapter(this, this, payoutDetailsModel.paymentlist)
        rvPayoutList.setAdapter(myRecyclerViewAdapter)

        this.payoutId = ""
        this.payoutFunctionality = ""
        this.payoutType = ""
    }


    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
    }

}
