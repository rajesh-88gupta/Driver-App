package com.seentechs.newtaxidriver.trips.rating

/**
 * @package com.seentechs.newtaxidriver.trips.rating
 * @subpackage rating
 * @category RiderFeedBackModel
 * @author Seen Technologies
 *
 */

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.GestureDetector
import android.view.MotionEvent

import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.util.CommonMethods

import butterknife.ButterKnife
import butterknife.BindView
import butterknife.OnClick
import com.google.gson.Gson
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.RequestCallback
import com.seentechs.newtaxidriver.common.views.CommonActivity
import com.seentechs.newtaxidriver.home.datamodel.RiderFeedBackModel
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import kotlinx.android.synthetic.main.activity_feed_back.*
import java.util.ArrayList
import java.util.HashMap
import javax.inject.Inject

/* ************************************************************
                RiderFeedBackModel
Its used to get the rider feedback details
*************************************************************** */
class RiderFeedBack : CommonActivity(), ServiceListener {

    @BindView(R.id.starcomment)

    lateinit var starcomment: RelativeLayout
    //This is our tablayout
    /*@BindView(R.id.tabLayout)
    lateinit var tabLayout: TabLayout
    //This is our viewPager
    @BindView(R.id.pager)
    lateinit var viewPager: ViewPager*/
    lateinit @Inject
    var commonMethods: CommonMethods

    lateinit var dialog: AlertDialog

    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var sessionManager: SessionManager
    @Inject
    lateinit var gson: Gson
    @Inject
    lateinit var customDialog: CustomDialog

    @BindView(R.id.my_recycler_view2)
    lateinit var recyclerView: RecyclerView
    @BindView(R.id.norating)
    lateinit var Def_rating_text: TextView


    protected var isInternetAvailable: Boolean = false
    private val feedbackarraylist = ArrayList<HashMap<String, String>>()

    @OnClick(R.id.back)
    fun onpBack() {
        onBackPressed()
    }

    val userComments: HashMap<String, String>
        get() {
            val userRatingHashMap = HashMap<String, String>()
            userRatingHashMap["user_type"] = sessionManager.type!!
            userRatingHashMap["token"] = sessionManager.accessToken!!

            return userRatingHashMap
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed_back)
        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)

        /*common header textview*/
        commonMethods.setheaderText(resources.getString(R.string.riderfeedback),common_header)
        starcomment.visibility = View.GONE
        //commonMethods.imageChangeforLocality(this,dochome_back)
        //setupViewPager(viewPager)
        //tabLayout.setupWithViewPager(viewPager)

    commonMethods.setheaderText(resources.getString(R.string.riderfeedback),common_header)
    dialog = commonMethods.getAlertDialog(this)

    isInternetAvailable = commonMethods.isOnline(this)

    recyclerView.setHasFixedSize(true)
    val layoutManager = LinearLayoutManager(this)
    recyclerView.layoutManager = layoutManager

    recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
        var gestureDetector = GestureDetector(applicationContext, object : GestureDetector.SimpleOnGestureListener() {

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

        })

        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {

            CommonMethods.DebuggableLogI("Character sequence ", " Checkins")

            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
            CommonMethods.DebuggableLogI("Character sequence ", " Checkins")
        }

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
            CommonMethods.DebuggableLogI("Character sequence ", " Checkins")
        }
    })

    if (isInternetAvailable) {
        updateUserComments()
    } else {
        dialogfunction()
    }

}



override fun onSuccess(jsonResp: JsonResponse, data: String) {
    commonMethods.hideProgressDialog()
    if (!jsonResp.isOnline) {
        if (!TextUtils.isEmpty(data))
            commonMethods.showMessage(this, dialog, data)
        return
    }

    if (jsonResp.isSuccess) {

        onSuccessComments(jsonResp)

    } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {

        commonMethods.showMessage(this, dialog, jsonResp.statusMsg)


    }
}

/**
 * success response for comments
 *
 * @param jsonResp
 */

private fun onSuccessComments(jsonResp: JsonResponse) {
    val riderFeedBackModel = gson.fromJson(jsonResp.strResponse, RiderFeedBackModel::class.java)
    if (riderFeedBackModel != null) {

        if (riderFeedBackModel.riderFeedBack!!.size >= 1) {

            Def_rating_text.text = getString(R.string.ratingsncomment)
            Def_rating_text.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        } else {

            Def_rating_text.text = getString(R.string.noratings)
            Def_rating_text.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }
        for (i in 0 until riderFeedBackModel.riderFeedBack!!.size) {
            val map = HashMap<String, String>()

            val date = riderFeedBackModel.riderFeedBack!![i].date
            val rating = riderFeedBackModel.riderFeedBack!![i].riderRating
            val rating_comments = riderFeedBackModel.riderFeedBack!![i].riderComments
            val user_id = riderFeedBackModel.riderFeedBack!![i].tripId

            map["date"] = date!!
            map["rating"] = rating!!
            map["rating_comments"] = rating_comments!!
            map["user_id"] = user_id!!

            feedbackarraylist.add(map)

        }


        val adapter = CommentsRecycleAdapter(feedbackarraylist)
        recyclerView.adapter = adapter

    }

}

override fun onFailure(jsonResp: JsonResponse, data: String) {
    commonMethods.hideProgressDialog()

}

/**
 * User Comments Api Call
 */

fun updateUserComments() {

    commonMethods.showProgressDialog( this)
    apiService.updateRiderFeedBack(userComments).enqueue(RequestCallback(this))

}


/*
* Show internet not available
*/
@SuppressLint("UseRequireInsteadOfGet")
fun dialogfunction() {
    val builder = AlertDialog.Builder(this)
    builder.setMessage(getString(R.string.turnoninternet))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.ok)) { _, _ -> builder.setCancelable(true) }

    val alert = builder.create()
    alert.show()
}


    /*private fun setupViewPager(viewPager: ViewPager) {
        val adapter = FeedbackViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(Comments(), "COMMENTS")
        viewPager.adapter = adapter
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        viewPager.currentItem = tab.position

    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
        CommonMethods.DebuggableLogI("onTabUnselected", "onTabUnselected")
    }

    override fun onTabReselected(tab: TabLayout.Tab) {
        CommonMethods.DebuggableLogI("onTabReselected", "onTabReselected")
    }*/


}
