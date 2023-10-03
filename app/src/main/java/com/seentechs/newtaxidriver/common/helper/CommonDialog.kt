package com.seentechs.newtaxidriver.common.helper

/**
 * @package com.seentechs.newtaxidriver.common.helper
 * @subpackage helper
 * @category CommonDialog
 * @author Seen Technologies
 *
 */

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.database.AddFirebaseDatabase
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.home.MainActivity
import java.util.*
import javax.inject.Inject

/* ************************************************************
                      CommonDialog
Its used for commondialog screen    (Like Arrive now, Begin trip, Payment completed)
*************************************************************** */

class CommonDialog : Activity(), View.OnClickListener {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods


    internal var status: Int = 0
    lateinit internal var setMessage: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_common_dialog)

        status = intent.getIntExtra("status", 0)
        AddFirebaseDatabase().removeNodesAfterCompletedTrip(this)
        this.setFinishOnTouchOutside(false)
        if (status == 1) {
            setMessage = resources.getString(R.string.yourtripcanceledrider)
        } else if (status == 2) {

            setMessage = resources.getString(R.string.paymentcompleted)
        }
        val message = findViewById<View>(R.id.message) as TextView
        println("getMessag $setMessage")
        message.text = setMessage
        val ok_btn = findViewById<View>(R.id.ok_btn_id) as Button
        ok_btn.setOnClickListener(this)
    }

    override fun attachBaseContext(newBase: Context?) {
        AppController.getAppComponent().inject(this)
        super.attachBaseContext(updateLocale(newBase))
    }

    fun updateLocale(newBase: Context?): Context? {
        var newBase = newBase
        val lang: String = sessionManager.languageCode!! // your language or load from SharedPref
        val locale = Locale(lang)
        val config = Configuration(newBase?.resources?.configuration)
        Locale.setDefault(locale)
        config.setLocale(locale)
        newBase = newBase?.createConfigurationContext(config)
        newBase?.resources?.updateConfiguration(config, newBase.resources.displayMetrics)
        return newBase
    }

    /*
    *  Get driver rating and feed back details API Called
    */
    override fun onClick(v: View) {

        when (v.id) {
            R.id.ok_btn_id -> {

                /*if (getIntent().getIntExtra("type", 0) == 0) {
                    Intent requestaccept = new Intent(getApplicationContext(), Riderrating.class);
                    requestaccept.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(requestaccept);
                }else{
                    Intent requestaccept = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(requestaccept);
                }*/
                val requestaccept = Intent(applicationContext, MainActivity::class.java)
                startActivity(requestaccept)
                this.finish()
            }
            else -> {
            }
        }
    }

    override fun onBackPressed() {

    }
}
