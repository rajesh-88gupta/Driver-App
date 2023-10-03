package com.seentechs.newtaxidriver.home.payouts

/**
 *
 * @package     com.seentechs.newtaxidriver
 * @subpackage  Profile
 * @category    PayoutMainActivity
 * @author      Seen Technologies
 *
 */

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout

import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.views.CommonActivity

/* ************************************************************
                   Payout Main Page
Show payment message in static page
*************************************************************** */
class PayoutMainActivity : CommonActivity(), View.OnClickListener {

    internal lateinit var payout_title: RelativeLayout
    internal lateinit var payout_start: Button
    internal lateinit var payout_stripe: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payout_main)

        payout_title = findViewById<View>(R.id.payout_title) as RelativeLayout
        payout_start = findViewById<View>(R.id.payout_start) as Button
        payout_stripe = findViewById<View>(R.id.payout_stripe) as Button

        payout_title.setOnClickListener(this)
        payout_start.setOnClickListener(this)
        payout_stripe.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.payout_title -> {
                onBackPressed()
            }

            R.id.payout_start -> {
                // Call payout address page
                val x = Intent(applicationContext, PayoutAddressDetailsActivity::class.java)
                startActivity(x)
                finish()
            }
            R.id.payout_stripe -> {
                // Call payout address page
                val x = Intent(applicationContext, PayoutBankDetailsActivity::class.java)
                startActivity(x)
                finish()
            }
        }
    }
}
