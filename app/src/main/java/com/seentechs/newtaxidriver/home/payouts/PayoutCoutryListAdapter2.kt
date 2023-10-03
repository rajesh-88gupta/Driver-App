package com.seentechs.newtaxidriver.home.payouts

/**
 *
 * @package     com.seentechs.newtaxidriver
 * @subpackage  adapter
 * @category    PayoutCountryListAdapter
 * @author      Seen Technologies
 *
 */


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
import com.seentechs.newtaxidriver.home.payouts.payout_model_classed.Header
import com.seentechs.newtaxidriver.home.payouts.payout_model_classed.Makent_model

import java.util.ArrayList

import javax.inject.Inject


@SuppressLint("ViewHolder")
class PayoutCoutryListAdapter2(context: Context, private val modelItems: ArrayList<Makent_model>, var type: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    @Inject
    lateinit var sessionManager: SessionManager
    val TYPE_Explore = 1
    val TYPE_LOAD = 2
    lateinit var header: Header
    private val activity: Activity? = null
    private val inflater: LayoutInflater? = null
    internal var oldposition = 1
    var context: Context

    private val mIsItemClicked: BooleanArray

    private var mItemClickListener: onItemClickListener? = null

    fun setOnItemClickListener(mItemClickListener: onItemClickListener) {
        this.mItemClickListener = mItemClickListener
    }

    interface onItemClickListener {
        fun onItemClickListener(position: Int)
    }


    init {
        this.context = context
        mIsItemClicked = BooleanArray(240)
        AppController.getAppComponent().inject(this)
        println("modelItems" + mIsItemClicked.size)
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

            println("MovieHolder position 2 " + currentItem.countryName)
            holder.countryname.text = currentItem.countryName


            holder.countryname.setOnClickListener {
                /*  for (int i = 0; i < mIsItemClicked.length; i++) {

                        if(i!=position) {
                            mIsItemClicked[i] = false;
                        }else
                        {
                            mIsItemClicked[position]=true;
                        }

                    }*/

                //mainholder.countryname.setTextColor(context.getResources().getColor(R.color.white));

                if (type == "country") {
                    sessionManager.countryName2 = getItem(position).countryName.toString()
                    println("get Country name " + getItem(position).countryName)
                    sessionManager.stripeCountryCode = getItem(position).countryCode.toString()
                } else if (type == "currency") {
                    sessionManager.currencyName2 = getItem(position).countryName.toString()
                } else {
                    sessionManager.gender = getItem(position).countryName.toString()
                }


                sessionManager.countryCurrencyType = type

                if (mItemClickListener != null) {
                    println("IS NOT NULL")
                    mItemClickListener!!.onItemClickListener(position)
                }


                /*System.out.println("Position "+position+" Current Value"+currentItem.getCountryName().toString());
					System.out.println("Position "+position+" Current Position"+currentItem.getCountryId());
					notifyItemChanged(position);
					notifyItemChanged(oldposition);
					oldposition=position;*/
            }

        }

    }


    private fun getItem(position: Int): Makent_model {
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
            //this.txtTitle.setText(context.getResources().getString(R.string.selectcountry));
            this.txtTitle.text = context.resources.getString(R.string.select) + " " + type
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

        fun bindData(movieModel: Makent_model, position: Int) {

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