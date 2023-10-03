/*
 * Copyright (c) 2017. Truiton (http://www.truiton.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * Mohit Gupt (https://github.com/mohitgupt)
 *
 */

package com.seentechs.newtaxidriver.home.fragments

/**
 * @package com.seentechs.newtaxidriver.home.fragments
 * @subpackage fragments
 * @category RatingActivity
 * @author Seen Technologies
 *
 */

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.gson.Gson
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.home.datamodel.RatingModel
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.RequestCallback
import com.seentechs.newtaxidriver.common.views.CommonActivity
import com.seentechs.newtaxidriver.trips.rating.Comments
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


/* ************************************************************
                      RatingActivity
Its used get home screen rating fragment details
*************************************************************** */
class RatingActivity : CommonActivity(), ServiceListener {

    lateinit var dialog: AlertDialog
    lateinit @Inject
    var commonMethods: CommonMethods
    lateinit @Inject
    var apiService: ApiService
    lateinit @Inject
    var sessionManager: SessionManager
    lateinit @Inject
    var gson: Gson
    lateinit @Inject
    var customDialog: CustomDialog

    lateinit @BindView(R.id.feedbackhistorylayout)
    var feedbackhistorylayout: RelativeLayout
    lateinit @BindView(R.id.rating_lay)
    var rating_lay: RelativeLayout
    lateinit @BindView(R.id.lifetime)
    var lifetime: TextView
    lateinit @BindView(R.id.ratingtrips)
    var ratingtrips: TextView
    lateinit @BindView(R.id.fivestar)
    var fivestar: TextView
    lateinit @BindView(R.id.textView2)
    var textView2: TextView
    lateinit @BindView(R.id.tv_rating_content)
    var tvRatingContent: TextView
    lateinit @BindView(R.id.arrarowone)
    var arrarowone: TextView
    protected var isInternetAvailable: Boolean = false

    @OnClick(R.id.ivBack)
    fun onBack() {
        onBackPressed()
    }


    @BindView(R.id.tvTitle)
    lateinit var tvTitle: TextView

    val userRating: HashMap<String, String>
        get() {
            val userRatingHashMap = HashMap<String, String>()
            userRatingHashMap["user_type"] = sessionManager.type!!
            userRatingHashMap["token"] = sessionManager.accessToken!!

            return userRatingHashMap
        }

    @Singleton
    @OnClick(R.id.feedbackhistorylayout)
    fun feedbackHistoryLayout() {
    feedbackhistorylayout.isEnabled = false
        val intent = Intent(this, Comments::class.java)
        startActivity(intent)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating)
        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)

        dialog = commonMethods.getAlertDialog(this)
        isInternetAvailable = commonMethods.isOnline(this)
        textView2.visibility = View.GONE
        tvRatingContent.visibility = View.GONE
        tvTitle.text = getString(R.string.rating)

        if (isInternetAvailable) {
            /*
         *  Get driver rating and feed back details API
         **/
            updateEarningChart()

        } else {
            dialogfunction()
        }
        initView()
    }

    private fun initView() {
    }


    fun updateEarningChart() {

        commonMethods.showProgressDialog(this)
        apiService.updateDriverRating(userRating).enqueue(RequestCallback(this))

    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog, data)
            return
        }

        if (jsonResp.isSuccess) {

            onSuccessRating(jsonResp)

        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {

            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)

        }
    }

    override fun onResume() {
        super.onResume()
        feedbackhistorylayout.isEnabled = true
    }

    private fun onSuccessRating(jsonResp: JsonResponse) {

        val ratingModel = gson.fromJson(jsonResp.strResponse, RatingModel::class.java)
        if (ratingModel != null) {
            val total_rating = ratingModel.totalRating
            val total_rating_count = ratingModel.totalRatingCount
            val five_rating_count = ratingModel.fiveRatingCount
            val driver_rating = ratingModel.driverRating

            lifetime.text = total_rating_count
            ratingtrips.text = total_rating
            fivestar.text = five_rating_count

            if (driver_rating!!.equals("0.00", ignoreCase = true) || driver_rating.equals("0", ignoreCase = true)) {
                tvRatingContent.visibility = View.GONE
                textView2.visibility = View.VISIBLE
                textView2.text = resources.getString(R.string.no_ratings_display)
                textView2.textSize = 20f
                textView2.setCompoundDrawablesRelative(null, null, null, null)
            } else {
                textView2.visibility = View.VISIBLE
                textView2.text = driver_rating
                tvRatingContent.visibility = View.VISIBLE
            }


        }


    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()

    }


    /*
     *  show dialog for no internet available
     */
    fun dialogfunction() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(resources.getString(R.string.turnoninternet))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok)) { _, _ -> builder.setCancelable(true) }

        val alert = builder.create()
        alert.show()
    }

    companion object {


    }

}
