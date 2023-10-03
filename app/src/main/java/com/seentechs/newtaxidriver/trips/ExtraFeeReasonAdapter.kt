package com.seentechs.newtaxidriver.trips


import android.annotation.SuppressLint
import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat

import androidx.recyclerview.widget.RecyclerView

import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.home.datamodel.ExtraFeeReason
import com.seentechs.newtaxidriver.home.payouts.payout_model_classed.CountryModel
import com.seentechs.newtaxidriver.home.payouts.payout_model_classed.Header

import java.util.ArrayList


@SuppressLint("ViewHolder")
class ExtraFeeReasonAdapter(internal var context: Context, private val modelItems: ArrayList<ExtraFeeReason>, internal var iExtraFeeReasonSelectListener: IExtraFeeReasonSelectListener)//AppController.getAppComponent().inject(this);
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    internal var header: Header? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        println("View Type$viewType")
        return ReasonHolder(inflater.inflate(R.layout.payout_country_list, parent, false))


    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {


        println("MovieHolder position$position")
        val currentItem = getItem(position)

        val mainholder = holder as ReasonHolder

        println("MovieHolder getCountryName" + currentItem.name)
        mainholder.countryname.text = currentItem.name


        mainholder.countryname.setOnClickListener { iExtraFeeReasonSelectListener.selectedExtraFeeReason(currentItem) }


    }


    private fun getItem(position: Int): ExtraFeeReason {
        return modelItems[position]
    }


    override fun getItemCount(): Int {
        println("ModelItem" + modelItems.size)
        return modelItems.size
    }


    /* VIEW HOLDERS */


    internal inner class ReasonHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var countryname: TextView
        var country_layout: RelativeLayout

        init {
            countryname = itemView.findViewById<View>(R.id.countryname) as TextView
            country_layout = itemView.findViewById<View>(R.id.country_layout) as RelativeLayout
        }

        fun bindData(movieModel: CountryModel, position: Int) {

            countryname.text = movieModel.countryName


            countryname.setOnClickListener {
                //	this.notifyDataChanged();
                println("Position" + position + "Country name" + countryname.text.toString())

                countryname.setTextColor(ContextCompat.getColor(context,R.color.white))
                Toast.makeText(context, "Position" + position + "Country name" + countryname.text.toString(), Toast.LENGTH_SHORT).show()
            }

        }
    }

    interface IExtraFeeReasonSelectListener {
        fun selectedExtraFeeReason(extraFeeReason: ExtraFeeReason)
    }


}