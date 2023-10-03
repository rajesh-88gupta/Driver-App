package com.seentechs.newtaxidriver.common.views

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.webkit.URLUtil
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.model.Support
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.home.splash.SplashActivity.Companion.checkVersionModel
import kotlinx.android.synthetic.main.activity_support_common.*
import java.util.*
import javax.inject.Inject


class SupportActivityCommon : CommonActivity(), SupportAdapter.OnClickListener {
    @BindView(R.id.rv_support_list)
    lateinit var rvSupportList: RecyclerView

    var supportList: ArrayList<Support> = ArrayList()

    @OnClick(R.id.arrow)
    fun onBack() {
        onBackPressed()
    }

    @Inject
    lateinit var commonMethods: CommonMethods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support_common)

        AppController.getAppComponent().inject(this)
        ButterKnife.bind(this)
        commonMethods.setheaderText(resources.getString(R.string.support),common_header)
        //commonMethods.imageChangeforLocality(this, arrow)
        initViews()


    }

    private fun initViews() {
        supportList.clear()
        supportList.addAll(checkVersionModel.support)
        rvSupportList.adapter = SupportAdapter(this, supportList, this)
    }

    override fun onClick(pos: Int) {9
        if (checkVersionModel.support[pos].id == 1) {
            onClickWhatsApp(checkVersionModel.support[pos].link)
        } else if (checkVersionModel.support[pos].id == 2) {
            openSkype(this, checkVersionModel.support[pos].link)
        } else {
            redirectWeb(checkVersionModel.support[pos].link)
        }
    }

    private fun redirectWeb(link: String) {
        if (URLUtil.isValidUrl(link) || Patterns.WEB_URL.matcher(link).matches()) {
            val redirectLink: String = if (!(link.contains("https://") || link.contains("http://"))) {
                "http://$link"
            } else {
                link
            }
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(redirectLink)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(i)
        }else{
            Toast.makeText(this,resources.getString(R.string.not_valid_data), Toast.LENGTH_SHORT).show()
        }
    }


    private fun openSkype(context: Context, skypeId: String) {

        // Make sure the Skype for Android client is installed
        if (!isSkypeClientInstalled(context)) {
            goToMarket(context)
            return
        }
        val mySkypeUri = "skype:" + skypeId + "?chat"
        // Create the Intent from our Skype URI.
        val skypeUri = Uri.parse(mySkypeUri)
        val myIntent = Intent(Intent.ACTION_VIEW, skypeUri)

        myIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK



        try {
            context.startActivity(myIntent)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Determine whether the Skype for Android client is installed on this device.
     */
    fun isSkypeClientInstalled(myContext: Context): Boolean {
        val myPackageMgr: PackageManager = myContext.getPackageManager()
        try {
            myPackageMgr.getPackageInfo("com.skype.raider", PackageManager.GET_ACTIVITIES)
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
        return true
    }

    /**
     * Install the Skype client through the market: URI scheme.
     */
    fun goToMarket(myContext: Context) {
        val marketUri = Uri.parse("market://details?id=com.skype.raider")
        val myIntent = Intent(Intent.ACTION_VIEW, marketUri)
        myIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        myContext.startActivity(myIntent)
    }

    fun onClickWhatsApp(phoneNumberWithCountryCode: String) {
        //val phoneNumberWithCountryCode = "+9112345678"
        val message = ""

        startActivity(
                Intent(Intent.ACTION_VIEW,
                        Uri.parse(String.format(getString(R.string.whatsapp_url), phoneNumberWithCountryCode, message))
                )
        )

    }
}