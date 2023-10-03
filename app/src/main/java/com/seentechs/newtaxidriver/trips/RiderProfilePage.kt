package com.seentechs.newtaxidriver.trips

/**
 * @package com.seentechs.newtaxidriver.home
 * @subpackage home
 * @category RiderProfilePage
 * @author Seen Technologies
 *
 */

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.squareup.picasso.Picasso
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.home.datamodel.TripDetailsModel
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonKeys
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.views.CommonActivity
import kotlinx.android.synthetic.main.app_activity_rider_profile.*
import javax.inject.Inject

/* ************************************************************
                      RiderProfilePage
Its used to get RiderProfilePage details
*************************************************************** */
class RiderProfilePage : CommonActivity() {

    lateinit @Inject
    var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    lateinit @BindView(R.id.profile_image1)
    var profileimage: ImageView
    lateinit @BindView(R.id.imgv_rider_accepted_cartypeImage)
    var riderAcceptedCartypeImage: ImageView
    lateinit @BindView(R.id.cancel_lay)
    var cancel_lay: RelativeLayout
    lateinit @BindView(R.id.rating_layout)
    var rating_layout: RelativeLayout
    lateinit @BindView(R.id.nametext)
    var ridername: TextView
    lateinit @BindView(R.id.ratingtext)
    var ratingtext: TextView
    lateinit @BindView(R.id.adresstext)
    var adresstext: TextView
    lateinit @BindView(R.id.droplocation)
    var droplocation: TextView
    lateinit @BindView(R.id.cartype)
    var cartype: TextView
   /* lateinit @BindView(R.id.cancelicon)
    var cancelicon: TextView*/
    lateinit @BindView(R.id.cancel_txt)
    var cancel_txt: TextView
    //AcceptedDriverDetails tripDetailsModel;
    lateinit var tripDetailsModel: TripDetailsModel
    private var currentRiderPosition: Int = 0

    @OnClick(R.id.back)
    fun onBack() {
        onBackPressed()
    }

    @OnClick(R.id.contact_lay)
    fun contact() {
        val requstreceivepage = Intent(applicationContext, RiderContactActivity::class.java)
        requstreceivepage.putExtra("ridername", tripDetailsModel.riderDetails.get(currentRiderPosition).name)
        requstreceivepage.putExtra("mobile_number", tripDetailsModel.riderDetails.get(currentRiderPosition).mobile_number)
        requstreceivepage.putExtra(CommonKeys.KEY_CALLER_ID, tripDetailsModel.riderDetails.get(currentRiderPosition).riderId)
        startActivity(requstreceivepage)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_activity_rider_profile)
        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
       /* common header tittle */
        commonMethods.setheaderText(resources.getString(R.string.enroute),common_header)
        val extras = intent.extras
        if (extras != null) {
            tripDetailsModel = intent.getSerializableExtra("riderDetails") as TripDetailsModel //Obtaining data
            currentRiderPosition = intent.getIntExtra("currentRiderPosition",0)
        }

        /*
                    *  Request rider details
                    */
        ridername.text = tripDetailsModel.riderDetails.get(currentRiderPosition).name
        insertRiderInfoToSession()
        if (tripDetailsModel.riderDetails.get(currentRiderPosition).rating == "0.0" || tripDetailsModel.riderDetails.get(currentRiderPosition).rating  == "") {
            rating_layout.visibility = View.GONE
        } else {
            ratingtext.text = tripDetailsModel.riderDetails.get(currentRiderPosition).rating
        }
        adresstext.text = tripDetailsModel.riderDetails.get(currentRiderPosition).pickupAddress
        val imageUr = tripDetailsModel.riderDetails.get(currentRiderPosition).profileImage
        droplocation.text = tripDetailsModel.riderDetails.get(currentRiderPosition).destAddress
        cartype.text = tripDetailsModel.riderDetails.get(currentRiderPosition).carType

        Picasso.get().load(imageUr)
                .into(profileimage)

        Picasso.get().load(tripDetailsModel.riderDetails.get(currentRiderPosition).carActiveImage).error(R.drawable.car)
                .into(riderAcceptedCartypeImage)

        if (sessionManager.tripStatus != null) {

            if (sessionManager.tripStatus == CommonKeys.TripDriverStatus.BeginTrip || sessionManager.tripStatus == CommonKeys.TripDriverStatus.EndTrip) {
                cancel_lay.isEnabled = false
                cancel_lay.isClickable = false
               // cancelicon.setTextColor(ContextCompat.getColor(applicationContext,R.color.cancel_disable_grey))
                cancel_txt.setTextColor(ContextCompat.getColor(applicationContext,R.color.cancel_disable_grey))
            } else {
                cancel_lay.isEnabled = true
                cancel_lay.isClickable = true
               // cancelicon.setTextColor(ContextCompat.getColor(applicationContext,R.color.app_continue))
                cancel_txt.setTextColor(ContextCompat.getColor(applicationContext,R.color.newtaxi_app_black))
            }
        } else {
            cancel_lay.isEnabled = true
            cancel_lay.isClickable = true
          //  cancelicon.setTextColor(ContextCompat.getColor(applicationContext,R.color.app_continue))
            cancel_txt.setTextColor(ContextCompat.getColor(applicationContext,R.color.newtaxi_app_black))
        }
                   /*
                    *  Redirect to trip cancel
                    */
        cancel_lay.setOnClickListener {
            val requstreceivepage = Intent(applicationContext, CancelYourTripActivity::class.java)
            startActivity(requstreceivepage)
        }

    }

    private fun insertRiderInfoToSession() {
        sessionManager.riderProfilePic = tripDetailsModel.riderDetails.get(currentRiderPosition).profileImage
        sessionManager.riderRating = tripDetailsModel.riderDetails.get(currentRiderPosition).rating
        sessionManager.riderName = tripDetailsModel.riderDetails.get(currentRiderPosition).name
        sessionManager.riderId = tripDetailsModel.riderDetails.get(currentRiderPosition).riderId!!
    }
}
