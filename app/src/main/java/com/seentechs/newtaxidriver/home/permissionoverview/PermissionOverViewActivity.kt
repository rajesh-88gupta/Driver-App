package com.seentechs.newtaxidriver.home.permissionoverview

import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.data.local.ApplicationPermissionModel
import com.seentechs.newtaxidriver.databinding.ActivityPermissionOverViewBinding
import com.seentechs.newtaxidriver.home.signinsignup.SigninSignupHomeActivity
import com.seentechs.newtaxidriver.taxiapp.views.permissionoverview.PermissionOverViewAdapter

class PermissionOverViewActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityPermissionOverViewBinding

    private lateinit var mAdapter: PermissionOverViewAdapter
    private val appName = "NewTaxi-Driver"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_permission_over_view)

        initView()
    }

    private fun initView() {

        customTextView(mBinding.textViewPrivacyPolicy)

        mBinding.recyclerPermissionList.setHasFixedSize(true)
        mBinding.btnSkip.setOnClickListener {

            Toast.makeText(
                this,
                "Permissions are mandatory",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }

        mBinding.btnGrantPermission.setOnClickListener {
            if (mBinding.checkbox.isChecked) {
                val x = Intent(this, SigninSignupHomeActivity::class.java)
                try {
                    val bndlanimation = ActivityOptions.makeCustomAnimation(
                        applicationContext,
                        R.anim.cb_fade_in,
                        R.anim.cb_face_out
                    ).toBundle()
                    startActivity(x, bndlanimation)
                } catch (e: Exception) {
                    startActivity(x)
                }
                //startActivity(x);
                finish()
            } else {
                Toast.makeText(
                    this,
                    "Please accept our privacy policy and Terms and condition",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        mAdapter = PermissionOverViewAdapter(preparePermissionList(), this)
        mBinding.recyclerPermissionList.adapter = mAdapter
    }

    private fun preparePermissionList(): List<ApplicationPermissionModel> {
        val permissionList = mutableListOf<ApplicationPermissionModel>()


        permissionList.add(
            ApplicationPermissionModel(
                R.drawable.ic_baseline_person_24,
                "Personal Info",
                "We need first name, last name, email address, mobile number and gender to register you in our application and provide you user specific services."
            )
        )

        permissionList.add(
            ApplicationPermissionModel(
                R.drawable.ic_baseline_location_on_24,
                "Location",
                "We need  your device’s location service for more reliable rides and provide you accurate location selection."
            )
        )

        permissionList.add(
            ApplicationPermissionModel(
                R.drawable.ic_baseline_contacts_24,
                "Contact",
                "We need this permission to share ride details with one or more contacts during any ride. Also you can make a trusted contact an emergency contact too. We may need to call them if we can’t reach you in case of any emergency."
            )
        )

        permissionList.add(
            ApplicationPermissionModel(
                R.drawable.ic_baseline_camera_alt_24,
                "Camera",
                "We need this permission to capture the user photo for the profile picture. \n"
            )
        )

        permissionList.add(
            ApplicationPermissionModel(
                R.drawable.ic_baseline_storage_24,
                "Files and Media",
                "We need this permission to access the device files and media to select a photo for the profile picture."
            )
        )

        permissionList.add(
            ApplicationPermissionModel(
                R.drawable.ic_baseline_mic_24,
                "Mic",
                "We need this permission to handle call  with you to provide better service and also to talk on the emergency number in case of any emergency."
            )
        )

        permissionList.add(
            ApplicationPermissionModel(
                R.drawable.ic_baseline_notifications_24,
                "Notification",
                "We need this permission to  send notification to provide regular updates about the trip and other promotional notification."
            )
        )

        return permissionList

    }

    private fun customTextView(view: TextView) {
        val spanTxt = SpannableStringBuilder(
            resources.getString(R.string.sigin_terms1_)
        )
        spanTxt.append(resources.getString(R.string.sigin_terms4))
        spanTxt.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                val url = resources.getString(R.string.privacy_policy)
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(i)
            }
        }, spanTxt.length - resources.getString(R.string.sigin_terms4).length, spanTxt.length, 0)
        spanTxt.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.blue_text_color)),
            spanTxt.length - resources.getString(R.string.sigin_terms4).length,
            spanTxt.length,
            0
        )

        spanTxt.append(resources.getString(R.string.and))

        spanTxt.append(resources.getString(R.string.sigin_terms5))

        spanTxt.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                val url = resources.getString(R.string.terms_condition)
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(i)
            }
        }, spanTxt.length - resources.getString(R.string.sigin_terms5).length, spanTxt.length, 0)
        spanTxt.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.blue_text_color)),
            spanTxt.length - resources.getString(R.string.sigin_terms5).length,
            spanTxt.length,
            0
        )

        spanTxt.append(".")
        view.movementMethod = LinkMovementMethod.getInstance()
        view.setText(spanTxt, TextView.BufferType.SPANNABLE)
    }


}