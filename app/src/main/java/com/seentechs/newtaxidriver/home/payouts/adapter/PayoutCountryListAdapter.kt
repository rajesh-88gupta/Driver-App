package com.seentechs.newtaxidriver.home.payouts.adapter


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat

import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.home.payouts.PayoutAddressDetailsActivity.Companion.alertDialogStores
import com.seentechs.newtaxidriver.home.payouts.payout_model_classed.CountryModel
import com.seentechs.newtaxidriver.home.payouts.payout_model_classed.Header

import java.util.ArrayList

import javax.inject.Inject



@SuppressLint("ViewHolder")
class PayoutCountryListAdapter(context: Context, private val modelItems: ArrayList<CountryModel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val TYPE_Explore = 1
    val TYPE_LOAD = 2
    internal lateinit var header: Header
    private val activity: Activity? = null
    private val inflater: LayoutInflater? = null
    var alertDialog: android.app.AlertDialog? = null
    internal var oldposition = 1
    var context: Context
    private val mIsItemClicked: BooleanArray

    @Inject
    lateinit var sessionManager: SessionManager


    init {
        this.context = context
        mIsItemClicked = BooleanArray(240)
        println("modelItems" + mIsItemClicked.size)
        AppController.getAppComponent().inject(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        println("View Type$viewType")
        //mIsItemClicked = new boolean[modelItems.size()];
        //System.out.println("modelItems1"+mIsItemClicked.length);
        return if (viewType == TYPE_Explore) {
            MovieHolder(inflater.inflate(R.layout.payout_country_list, parent, false))
        } else {
            LoadHolder(inflater.inflate(R.layout.row_load, parent, false))
        }
        //throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is MovieHolder) {
            println("MovieHolder position$position")
            val currentItem = getItem(position)

            println("MovieHolder getCountryName" + currentItem.countryName)
            holder.countryname.text = currentItem.countryName


            holder.countryname.setOnClickListener {
                for (i in mIsItemClicked.indices) {

                    if (i != position) {
                        mIsItemClicked[i] = false
                    } else {
                        mIsItemClicked[position] = true
                    }

                }
                holder.countryname.setTextColor(ContextCompat.getColor(context,R.color.white))
                sessionManager.countryName = currentItem.countryName.toString()
                sessionManager.payPalCountryCode = currentItem.countryName.toString()
                //					localSharedPreferences.saveSharedPreferences(Constants.CountryName,currentItem.getCountryName().toString());
                //					localSharedPreferences.saveSharedPreferences(Constants.PayPalCountryCode,currentItem.getCountryCode().toString());

                alertDialogStores.cancel()

                // this below line copied from macent and for the file LYS_Step4_AddressDetails.java,
                // hence we not imported this LYS_Step4_AddressDetails file here, so commented
                /*if(alertDialogStorestwo!=null) {
						alertDialogStorestwo.cancel();
					}*/


                /*System.out.println("Position "+position+" Current Value"+currentItem.getCountryName().toString());
					System.out.println("Position "+position+" Current Position"+currentItem.getCountryId());
					notifyItemChanged(position);
					notifyItemChanged(oldposition);
					oldposition=position;*/
            }

        }

    }


    private fun getItem(position: Int): CountryModel {
        return modelItems[position]
    }

    override fun getItemViewType(position: Int): Int {

        return TYPE_Explore
    }


    override fun getItemCount(): Int {
        return modelItems.size
    }


    /* VIEW HOLDERS */

    internal inner class HeaderHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtTitle: TextView

        init {
            this.txtTitle = itemView.findViewById<View>(R.id.header) as TextView
            this.txtTitle.text = context.resources.getString(R.string.selectcountry)
            this.txtTitle.textSize = context.resources.getDimension(R.dimen.midb)
            this.txtTitle.setTextColor(ContextCompat.getColor(context,R.color.white))
        }
    }

    internal class MovieHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

    internal class LoadHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun notifyDataChanged() {
        notifyDataSetChanged()
    }

    companion object {
        private val TYPE_HEADER = 0
        private val TYPE_ITEM = 1

        internal lateinit var context: Context

        protected val TAG: String? = null
    }


}