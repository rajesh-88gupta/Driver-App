package com.seentechs.newtaxidriver.home.paymentstatement

/**
 * @package com.seentechs.newtaxidriver.home.paymentstatement
 * @subpackage paymentstatement model
 * @category DailyEarnListAdapter
 * @author Seen Technologies
 *
 */

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.home.datamodel.WeeklyStatementModel
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/* ************************************************************
                DailyEarnListAdapter
Its used to view the list dailyearnlistadapter details
*************************************************************** */
class DailyEarnListAdapter(internal var context: Context, private val modelItems: List<WeeklyStatementModel.Statement>) : RecyclerView.Adapter<DailyEarnListAdapter.ViewHolder>() {
    @Inject
    lateinit var commonMethods: CommonMethods
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): DailyEarnListAdapter.ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.daily_earning_layout, viewGroup, false)
        return ViewHolder(view)
    }

    /*
    * Driver earning bind data
    */

    init {
        AppController.getAppComponent().inject(this)
    }

    override fun onBindViewHolder(viewHolder: DailyEarnListAdapter.ViewHolder, i: Int) {
        val currentItem = getItem(i)
        viewHolder.dailytrip.text = currentItem.date?.let { DateFirstUserFormat(it) }
        viewHolder.dailyamount.text = currentItem.driverEarnings
        viewHolder.rlParent.setOnClickListener {
            val intent = Intent(context, DailyEarningDetails::class.java)
            intent.putExtra("daily_date", currentItem.date)
            context.startActivity(intent)
        }
    }

    fun DateFirstUserFormat(date: String): String {

        val input = SimpleDateFormat("yyyy-MM-dd")
        val output = SimpleDateFormat("dd-MM-yyyy")
        var d1: Date? = null
        try {
            d1 = input.parse(date)
            println(output.format(d1))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return output.format(d1)
    }


    override fun getItemCount(): Int {
        return modelItems.size
    }

    private fun getItem(position: Int): WeeklyStatementModel.Statement {
        return modelItems[position]
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dailytrip: TextView
        val dailyamount: TextView
        val dailyArrow: TextView
        internal var rlParent: RelativeLayout

        init {
            dailytrip = view.findViewById<View>(R.id.dailytrip) as TextView
            dailyamount = view.findViewById<View>(R.id.dailyamount) as TextView
            dailyArrow = view.findViewById<View>(R.id.daily_earning_arrow) as TextView
            rlParent = view.findViewById<View>(R.id.rl_parent) as RelativeLayout
            //commonMethods.imageChangeforLocality(context, dailyArrow)
        }
    }

}
