package com.seentechs.newtaxidriver.home.payouts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.home.datamodel.PayoutDetailsListModel
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods
import java.util.*
import javax.inject.Inject


class PayoutDetailsListAdapter(private val onPayoutClicklistner: OnPayoutClick, private val context: Context, private val payoutDetailsListModel: ArrayList<PayoutDetailsListModel>) : RecyclerView.Adapter<PayoutDetailsListAdapter.ViewHolder>() {
    @Inject
    lateinit var commonMethods: CommonMethods
    lateinit var dialog: AlertDialog


    init {

        AppController.getAppComponent().inject(this)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): PayoutDetailsListAdapter.ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.payout_details_list, viewGroup, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(viewHolder: PayoutDetailsListAdapter.ViewHolder, i: Int) {

        viewHolder.tvPaypal.text = payoutDetailsListModel[i].value
        Picasso.get().load(payoutDetailsListModel[i].icon).into(viewHolder.ivPaypal)
        if (Integer.parseInt(payoutDetailsListModel[i].id) == 0) {
            viewHolder.tvDesc.text=context.resources.getString(R.string.add_payout)+" "+payoutDetailsListModel[i].value
        } else {
            if(payoutDetailsListModel[i].key.equals("bank_transfer"))
                viewHolder.tvDesc.text = payoutDetailsListModel[i].payoutData.account_number
            else
                viewHolder.tvDesc.text = payoutDetailsListModel[i].payoutData.paypal_email
        }
        if (payoutDetailsListModel[i].isDefault)
            viewHolder.tvDefault.visibility = View.VISIBLE
        else
            viewHolder.tvDefault.visibility = View.GONE

        viewHolder.rltPayout.setOnClickListener {
            onPayoutClicklistner.onPayoutClicK(payoutDetailsListModel[i].key, payoutDetailsListModel[i].id, i)
        }
    }

    override fun getItemCount(): Int {
        return payoutDetailsListModel.size
    }


    interface OnPayoutClick {
        fun onPayoutClicK(payoutType: String, payoutId: String, pos: Int)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPaypal: TextView
        val tvDefault: TextView
        val tvDesc: TextView
        val rltPayout: RelativeLayout
        val ivPaypal: ImageView

        init {
            tvPaypal = view.findViewById(R.id.tv_paypal)
            tvDefault = view.findViewById(R.id.tv_default)
            tvDesc = view.findViewById(R.id.tv_desc)
            rltPayout = view.findViewById(R.id.rlt_payout)
            ivPaypal = view.findViewById(R.id.iv_paypal)
        }
    }


}
