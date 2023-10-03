package com.seentechs.newtaxidriver.home.paymentstatement

/**
 * @package com.seentechs.newtaxidriver.trips.rating
 * @subpackage rating
 * @category CommentsRecycleAdapter
 * @author Seen Technologies
 *
 */

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.custompalette.FontCache
import com.seentechs.newtaxidriver.home.datamodel.InvoiceContent
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods

import java.util.ArrayList

import javax.inject.Inject

/* ************************************************************
                CommentsRecycleAdapter
Its used to view the feedback comments with rider screen page function
*************************************************************** */
class PriceStatementAdapter(private val context: Context, private val invoiceContents: ArrayList<InvoiceContent>) : RecyclerView.Adapter<PriceStatementAdapter.ViewHolder>() {
    @Inject
    lateinit var commonMethods: CommonMethods
    lateinit var dialog: AlertDialog
    private lateinit var  Other_reason: String

    init {

        AppController.getAppComponent().inject(this)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): PriceStatementAdapter.ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.price_layout, viewGroup, false)


        return ViewHolder(view)
    }

    /*
     *  Get rider feedback list bind
     */
    override fun onBindViewHolder(viewHolder: PriceStatementAdapter.ViewHolder, i: Int) {
        CommonMethods.DebuggableLogI("key", invoiceContents[i].key)
        CommonMethods.DebuggableLogI("value", invoiceContents[i].value)

        viewHolder.faretxt.text = invoiceContents[i].key
        viewHolder.fareAmt.text = invoiceContents[i].value!!.replace("\"", "")
        viewHolder.fareinfo.visibility = View.GONE
        viewHolder.fareAmt.visibility = View.VISIBLE
        if (invoiceContents[i].key == "Base fare") {
            viewHolder.isbase.visibility = View.GONE
        }


        if (!TextUtils.isEmpty(invoiceContents[i].tooltip)) {
            viewHolder.fareinfo.visibility = View.VISIBLE
        } else {
            viewHolder.fareinfo.visibility = View.GONE
        }

        if (invoiceContents[i].bar) {

            viewHolder.parentlay.background = context.resources.getDrawable(R.drawable.d_topboarder)

            println("Key check feedback : " + invoiceContents[i].key!!)

        } else {
            viewHolder.rltprice.setBackgroundColor(context.resources.getColor(R.color.white))
        }

        if (invoiceContents[i].colour != null && invoiceContents[i].colour == "black") {
            viewHolder.fareAmt.typeface = FontCache.getTypeface(context.resources.getString(R.string.fonts_UBERMedium), context)
            viewHolder.faretxt.typeface = FontCache.getTypeface(context.resources.getString(R.string.fonts_UBERMedium), context)
            viewHolder.fareAmt.setTextColor(context.resources.getColor(R.color.newtaxi_app_black))
            viewHolder.faretxt.setTextColor(context.resources.getColor(R.color.newtaxi_app_black))

        }
        if (invoiceContents[i].colour != null && invoiceContents[i].colour == "green") {
            viewHolder.fareAmt.typeface = FontCache.getTypeface(context.resources.getString(R.string.fonts_UBERMedium), context)
            viewHolder.faretxt.typeface = FontCache.getTypeface(context.resources.getString(R.string.fonts_UBERMedium), context)
            viewHolder.fareAmt.setTextColor(context.resources.getColor(R.color.newtaxi_app_navy))
            viewHolder.faretxt.setTextColor(context.resources.getColor(R.color.newtaxi_app_navy))
        }

        if(invoiceContents[i].key == "Sub Total"){
            viewHolder.fareAmt.typeface = FontCache.getTypeface(context.resources.getString(R.string.fonts_UBERMedium), context)
            viewHolder.faretxt.typeface = FontCache.getTypeface(context.resources.getString(R.string.fonts_UBERMedium), context)
            viewHolder.fareAmt.setTextColor(context.resources.getColor(R.color.newtaxi_app_black))
            viewHolder.faretxt.setTextColor(context.resources.getColor(R.color.newtaxi_app_black))
            viewHolder.faretxt.setTextSize(20.0F)
            viewHolder.fareAmt.setTextSize(20.0F)
        }
        if(invoiceContents[i].key == "Collectable cash"){
            viewHolder.fareAmt.typeface = FontCache.getTypeface(context.resources.getString(R.string.fonts_UBERMedium), context)
            viewHolder.faretxt.typeface = FontCache.getTypeface(context.resources.getString(R.string.fonts_UBERMedium), context)
            viewHolder.fareAmt.setTextColor(context.resources.getColor(R.color.newtaxi_app_navy))
            viewHolder.faretxt.setTextColor(context.resources.getColor(R.color.newtaxi_app_navy))
            viewHolder.faretxt.setTextSize(20.0F)
            viewHolder.fareAmt.setTextSize(20.0F)
        }
        viewHolder.fareinfo.setOnClickListener {
            dialog = commonMethods.getAlertDialog(context)
            commonMethods.showMessage(context, dialog, invoiceContents[i].tooltip)
        }

    }

    override fun getItemCount(): Int {
        return invoiceContents.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val faretxt: TextView
        val fareAmt: TextView
        val isbase: TextView
        val rltprice: LinearLayout
        val parentlay: LinearLayout
        val fareinfo: ImageView

        init {

            faretxt = view.findViewById(R.id.faretxt)
            fareinfo = view.findViewById(R.id.fareinfo)

            fareAmt = view.findViewById(R.id.fareAmt)
            isbase = view.findViewById(R.id.baseview)
            rltprice = view.findViewById(R.id.rltprice)
            parentlay = view.findViewById(R.id.basrfarelayout)

        }
    }


}
