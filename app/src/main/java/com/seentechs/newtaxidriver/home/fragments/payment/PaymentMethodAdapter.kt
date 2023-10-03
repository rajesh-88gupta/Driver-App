package com.seentechs.newtaxidriver.home.fragments.payment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.home.datamodel.PaymentMethodsModel
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonKeys
import javax.inject.Inject

class PaymentMethodAdapter(private var context:Context, private var paymentlist: ArrayList<PaymentMethodsModel.PaymentMethods>,private var listener:ItemOnClickListener):RecyclerView.Adapter<PaymentMethodAdapter.PaymentViewHolder>() {
    private var paymentmethodlist=ArrayList<PaymentMethodsModel>()
    @Inject
    lateinit var  sessionManager:SessionManager

    init {
        AppController.getAppComponent().inject(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
       val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_payment_method,parent,false)

        return PaymentViewHolder(view)
    }

    override fun getItemCount(): Int {
            return paymentlist.size
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val paymentmode= paymentlist.get(position)

        holder.tv_paymentname.text=paymentmode.paymenMethodvalue
        Picasso.get().load(paymentmode.paymenMethodIcon).error(R.drawable.app_ic_card).into(holder.iv_paymentIcon)
      /*  if(paymentmode.isDefaultPaymentMethod && !CommonKeys.isSetPaymentMethod)
        {
            sessionManager.paymentMethod=paymentmode.paymenMethodvalue
            sessionManager.paymentMethodkey=paymentmode.paymenMethodKey
            sessionManager.paymentMethodImage=paymentmode.paymenMethodIcon

            holder.iv_default.visibility=View.GONE
        }else*/ if(sessionManager.paymentMethodkey.equals(paymentmode.paymenMethodKey)){
            holder.iv_default.visibility=View.VISIBLE
        }else{
            holder.iv_default.visibility=View.GONE
        }



        holder.rowview.setOnClickListener(View.OnClickListener {

          if(paymentmode.paymenMethodKey.contains(context.resources.getString(R.string.stripe),ignoreCase = true) && !paymentmode.paymenMethodKey.equals(CommonKeys.PAYMENT_CARD))
          {

             listener.onItemClick()
              return@OnClickListener
          }
           // holder.iv_default.visibility=View.VISIBLE
            sessionManager.paymentMethodkey=paymentmode.paymenMethodKey
            sessionManager.paymentMethod=paymentmode.paymenMethodvalue
            sessionManager.paymentMethodImage=paymentmode.paymenMethodIcon
                  CommonKeys.isSetPaymentMethod=true
            notifyDataSetChanged()
        })
    }
    class PaymentViewHolder(val rowview:View):RecyclerView.ViewHolder(rowview)
    {
        internal var tv_paymentname: TextView
        internal var iv_paymentIcon:ImageView
        internal var iv_default:ImageView
        init {
            iv_paymentIcon=rowview.findViewById(R.id.iv_paymentIcon)
            tv_paymentname=rowview.findViewById(R.id.tv_paymentName)
            iv_default=rowview.findViewById(R.id.iv_isSelected)

        }
    }
    interface ItemOnClickListener{
        fun onItemClick()
    }
}