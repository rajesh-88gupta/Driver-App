package com.seentechs.newtaxidriver.home.paymentstatement

/**
 * @package com.seentechs.newtaxidriver.trips.rating
 * @subpackage rating
 * @category CommentsRecycleAdapter
 * @author Seen Technologies
 *
 */

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.custompalette.FontCache
import com.seentechs.newtaxidriver.home.datamodel.TripStatement
import com.seentechs.newtaxidriver.common.util.CommonMethods

import java.util.ArrayList

/* ************************************************************
                CommentsRecycleAdapter
Its used to view the feedback comments with rider screen page function
*************************************************************** */
class TripPriceAdapter(private val context: Context, private val feedbackarraylist: ArrayList<TripStatement.Invoice>) : RecyclerView.Adapter<TripPriceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): TripPriceAdapter.ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.price_layout, viewGroup, false)
        return ViewHolder(view)
    }

    /*
     *  Get rider feedback list bind
     */
    override fun onBindViewHolder(viewHolder: TripPriceAdapter.ViewHolder, i: Int) {
        CommonMethods.DebuggableLogI("key", feedbackarraylist[i].key)
        CommonMethods.DebuggableLogI("value", feedbackarraylist[i].value)

        viewHolder.faretxt.text = feedbackarraylist[i].key
        viewHolder.fareAmt.text = feedbackarraylist[i].value?.replace("\"", "")
        if (feedbackarraylist[i].key == "Base fare") {
            viewHolder.isbase.visibility = View.GONE
        }
        if (feedbackarraylist[i].bar == "1") {

            viewHolder.rltprice.background = context.resources.getDrawable(R.drawable.d_topboarder)

            println("Key check feedback : " + feedbackarraylist[i].key)

        } else {
            viewHolder.rltprice.setBackgroundColor(context.resources.getColor(R.color.white))
        }


        if (feedbackarraylist[i].colour == "black") {
            viewHolder.fareAmt.typeface = FontCache.getTypeface(context.resources.getString(R.string.fonts_UBERMedium), context)
            viewHolder.faretxt.typeface = FontCache.getTypeface(context.resources.getString(R.string.fonts_UBERMedium), context)
            viewHolder.fareAmt.setTextColor(context.resources.getColor(R.color.ub__black))
            viewHolder.faretxt.setTextColor(context.resources.getColor(R.color.ub__black))

        }

        if (feedbackarraylist[i].colour == "green") {
            viewHolder.fareAmt.typeface = FontCache.getTypeface(context.resources.getString(R.string.fonts_UBERMedium), context)
            viewHolder.faretxt.typeface = FontCache.getTypeface(context.resources.getString(R.string.fonts_UBERMedium), context)
            viewHolder.fareAmt.setTextColor(context.resources.getColor(R.color.ub__green))
            viewHolder.faretxt.setTextColor(context.resources.getColor(R.color.ub__green))

        }

    }

    override fun getItemCount(): Int {
        return feedbackarraylist.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val faretxt: TextView
        val fareAmt: TextView
        val isbase: TextView
        val rltprice: RelativeLayout

        init {

            faretxt = view.findViewById(R.id.faretxt)
            fareAmt = view.findViewById(R.id.fareAmt)
            isbase = view.findViewById(R.id.baseview)
            rltprice = view.findViewById(R.id.rltprice)

        }
    }


}
