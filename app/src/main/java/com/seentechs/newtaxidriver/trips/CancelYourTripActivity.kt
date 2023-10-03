package com.seentechs.newtaxidriver.trips

/**
 * @package com.seentechs.newtaxidriver.home
 * @subpackage home
 * @category CancelYourTripActivity
 * @author Seen Technologies
 *
 */

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.gson.Gson
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.database.AddFirebaseDatabase
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.Enums.REQ_CANCEL
import com.seentechs.newtaxidriver.common.util.Enums.REQ_CANCEL_TRIP
import com.seentechs.newtaxidriver.common.util.RequestCallback
import com.seentechs.newtaxidriver.common.views.CommonActivity
import com.seentechs.newtaxidriver.home.MainActivity
import com.seentechs.newtaxidriver.home.datamodel.cancel.CancelReasonModel
import com.seentechs.newtaxidriver.home.datamodel.cancel.CancelResultModel
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import kotlinx.android.synthetic.main.app_activity_trip_details.*
import org.json.JSONObject
import java.util.*
import javax.inject.Inject

/* ************************************************************
                      CancelYourTripActivity
Its used to get CancelYourTripActivity for rider
*************************************************************** */
class CancelYourTripActivity : CommonActivity(), ServiceListener {


    var cancelReasonModels = ArrayList<CancelReasonModel>()

    lateinit @Inject
    var apiService: ApiService

    lateinit @Inject
    var sessionManager: SessionManager

    lateinit @Inject
    var commonMethods: CommonMethods

    lateinit @Inject
    var customDialog: CustomDialog
    lateinit var dialog: AlertDialog

    lateinit @Inject
    var gson: Gson

    lateinit @BindView(R.id.spinner)
    var spinner: Spinner

    var cancelreason: String = ""
    var cancelmessage: String = ""

    lateinit @BindView(R.id.cancel_reason)
    var cancel_reason: EditText
    protected var isInternetAvailable: Boolean = false

    @OnClick(R.id.back)
    fun onClickclose() {
        finish()
    }

    @OnClick(R.id.cancelreservation)
    fun onClickReserv() {
        /*
         *  Update cancel reason in server
         */
        isInternetAvailable = commonMethods.isOnline(this)
        /*String spinnerpos = String.valueOf(spinner.getSelectedItemPosition());
        if ("0".equals(spinnerpos)) {
            cancelreason = "";
        } else {
            cancelreason = spinner.getSelectedItem().toString();
        }*/
        cancelmessage = cancel_reason.text.toString()


        if (isInternetAvailable) {
            cancelTrip()
        } else {
            commonMethods.showMessage(this@CancelYourTripActivity, dialog, resources.getString(R.string.no_connection))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_activity_cancel_your_trip)
        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        /**Commmon Header Text View */
        commonMethods.setheaderText(resources.getString(R.string.cancel_your_trip), common_header)
        dialog = commonMethods.getAlertDialog(this)
        isInternetAvailable = commonMethods.isOnline(this)

        getCancelReasons()
        /*

        ArrayAdapter<CharSequence> canceladapter;

        canceladapter = ArrayAdapter.createFromResource(
                this, R.array.cancel_types, R.layout.spinner_layout);
        canceladapter.setDropDownViewResource(R.layout.spinner_layout);


        spinner.setAdapter(canceladapter);

        */
        /*
         *  Cancel trip reasons in spinner
         *//*

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here

            }

        });
*/


    }

    private fun getCancelReasons() {
        commonMethods.showProgressDialog(this)
        apiService.cancelReasons(sessionManager.accessToken!!).enqueue(RequestCallback(REQ_CANCEL, this))
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog, data)
            return
        }
        val statuscode = commonMethods.getJsonValue(jsonResp.strResponse!!, "status_code", String::class.java) as String

        when (jsonResp.requestCode) {

            REQ_CANCEL_TRIP -> if (jsonResp.isSuccess) {
                commonMethods.hideProgressDialog()
                val tripriders = JSONObject(jsonResp.strResponse).getJSONArray("trip_riders")
                if (tripriders.length() > 0) {
                    sessionManager.isTrip = true
                } else {
                    sessionManager.isTrip = false
                }

                onSuccessCancel()
            } else if (statuscode.equals("2")) {
                commonMethods.hideProgressDialog()
                cancelFunction(jsonResp.statusMsg)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.hideProgressDialog()
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }
            REQ_CANCEL -> if (jsonResp.isSuccess) {
                commonMethods.hideProgressDialog()
                onSuccessCancelReasons(jsonResp)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.hideProgressDialog()
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }
        }
    }

    private fun onSuccessCancelReasons(jsonResp: JsonResponse) {
        val cancelResultModel = gson.fromJson(jsonResp.strResponse, CancelResultModel::class.java)
        if (cancelResultModel != null) {
            val cancelReasonModel = CancelReasonModel()
            cancelReasonModel.id = 0
            cancelReasonModel.reason = getString(R.string.select_reason)
            cancelReasonModels.add(cancelReasonModel)
            cancelReasonModels.addAll(cancelResultModel.cancelReasons)

            val cancelReason = arrayOfNulls<String>(cancelReasonModels.size)

            for (i in cancelReasonModels.indices) {
                cancelReason[i] = cancelReasonModels[i].reason
            }

            val adapter = ArrayAdapter(this, R.layout.spinner_layout, cancelReason)

            spinner.adapter = adapter
            adapter.notifyDataSetChanged()


            /**
             * Cancel trip reasons in spinner
             */
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {

                }

                override fun onNothingSelected(parentView: AdapterView<*>) {
                    // your code here
                }

            }
        }
    }

    fun cancelFunction(statusMsg: String) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(statusMsg)
                .setCancelable(false)
                .setPositiveButton(R.string.ok) { dialog, which ->
                    dialog.dismiss()
                    sessionManager.clearTripID()
                    sessionManager.clearTripStatus()
                    sessionManager.isDriverAndRiderAbleToChat = false
                    val requestaccept = Intent(applicationContext, MainActivity::class.java)
                    startActivity(requestaccept)
                    this.finish()

                }
        builder.create().show()
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.hideProgressDialog()
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }

    /*
     *  Cancel reason update API called
     */
    fun cancelTrip() {
        val position: Int
        position = spinner.selectedItemPosition
        /* if (cancelreason.equals("")) {
            commonMethods.showMessage(CancelYourTripActivity.this, dialog, getResources().getString(R.string.cancelreason));
        } else {*/
        if (position > 0) {
            commonMethods.showProgressDialog(this)
            apiService.cancelTrip(sessionManager.type!!, cancelReasonModels[position].id!!.toString(), cancelmessage, sessionManager.tripId!!, sessionManager.accessToken!!).enqueue(RequestCallback(REQ_CANCEL_TRIP, this))
        } else {
            commonMethods.showMessage(this, dialog, resources.getString(R.string.select_reason))
        }
    }

    fun onSuccessCancel() {
        CommonMethods.stopFirebaseChatListenerService(this)
        CommonMethods.stopSinchService(this)
        AddFirebaseDatabase().removeLiveTrackingNodesAfterCompletedTrip(this)
        AddFirebaseDatabase().removeNodesAfterCompletedTrip(this)
        sessionManager.clearTripID()
        sessionManager.clearTripStatus()
        sessionManager.isDriverAndRiderAbleToChat = false

        val requestaccept = Intent(applicationContext, MainActivity::class.java)
        startActivity(requestaccept)
        finish()
    }


}

