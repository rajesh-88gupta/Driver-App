package com.seentechs.newtaxidriver.home.fragments.currency

/**
 * @package com.seentechs.newtaxidriver
 * @subpackage Side_Bar.currency
 * @category CurrencyListAdapter
 * @author Seen Technologies
 *
 */

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.home.managevehicles.SettingActivity.Companion.alertDialogStores1
import com.seentechs.newtaxidriver.home.managevehicles.SettingActivity.Companion.currencyclick
import com.seentechs.newtaxidriver.common.network.AppController
import javax.inject.Inject




@SuppressLint("ViewHolder")
class CurrencyListAdapter(context: Context, Items: List<CurrencyModel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val TYPE_Explore = 0
    val TYPE_LOAD = 1
    lateinit @Inject
    var sessionManager: SessionManager
    var isLoading: Boolean = false



    init {
        CurrencyListAdapter.context = context
        CurrencyListAdapter.modelItems = Items
        isLoading = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        if (viewType == TYPE_Explore) {
            AppController.getAppComponent().inject(this)
            return MovieHolder(inflater.inflate(R.layout.currency_item_view, parent, false))
        } else {
            return LoadHolder(inflater.inflate(R.layout.row_load, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        /*if (position >= itemCount - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {
            isLoading = true
            loadMoreListener!!.onLoadMore()
        }*/

        if (getItemViewType(position) == TYPE_Explore) {
            (holder as MovieHolder).bindData(modelItems[position], position)
        }
        //No else part needed as load holder doesn't bind any data
    }

    override fun getItemViewType(position: Int): Int {
        return if (modelItems[position].currencyName == "load") {
            TYPE_LOAD
        } else {
            TYPE_Explore
        }
    }

    override fun getItemCount(): Int {
        return modelItems.size
    }

   /* *//* VIEW HOLDERS *//*

    fun setMoreDataAvailable(moreDataAvailable: Boolean) {
        isMoreDataAvailable = moreDataAvailable
    }*/

    /* notifyDataSetChanged is final method so we can't override it
         call adapter.notifyDataChanged(); after update the list
         */
    fun notifyDataChanged() {
        notifyDataSetChanged()
        isLoading = false
    }

    /*fun setLoadMoreListener(loadMoreListener: OnLoadMoreListener) {
        this.loadMoreListener = loadMoreListener
    }*/

    interface OnLoadMoreListener {
        fun onLoadMore()
    }

    internal class LoadHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    internal inner class MovieHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        var currencyname: TextView
        var currencysymbol: TextView
        var radiobutton: RadioButton
        var selectcurrency: RelativeLayout

        init {
            currencyname = itemView.findViewById<View>(R.id.currencyname_txt) as TextView
            currencysymbol = itemView.findViewById<View>(R.id.currencysymbol_txt) as TextView
            radiobutton = itemView.findViewById<View>(R.id.radioButton1) as RadioButton
            selectcurrency = itemView.findViewById<View>(R.id.selectcurrency) as RelativeLayout
        }

        fun bindData(movieModel: CurrencyModel, position: Int) {

            val currencycode: String
            currencycode = sessionManager.currencyCode!!


            currencyname.text = movieModel.currencyName
            currencysymbol.text = movieModel.currencySymbol

            //currency=movieModel.getCurrencyName()+""+movieModel.getCurrencySymbol();

            val colorStateList = ColorStateList(
                    arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)),
                    intArrayOf(

                            ContextCompat.getColor(context,R.color.ub__uber_black_60), ContextCompat.getColor(context,R.color.rb_blue_button))
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                radiobutton.buttonTintList = colorStateList
            }


            radiobutton.isChecked = false

            if (movieModel.currencyName == currencycode) {
                radiobutton.isChecked = true
            }
            if (lastCheckedPosition == position) {


            }

            selectcurrency.setOnClickListener {
                currency = currencyname.text.toString() + " (" + currencysymbol.text.toString() + ")"

                sessionManager.currencyCode = currencyname.text.toString()
                sessionManager.currencySymbol = currencysymbol.text.toString()


                lastCheckedPosition = adapterPosition
                radiobutton.isChecked = true

                //new SettingActivity.Updatecurrency().execute();
                /*
                    if(alertDialogStores!=null) {
                        alertDialogStores.cancel();
                    }*/
                currencyclick = true
                alertDialogStores1?.cancel()
                /* if(alertDialogStores2!=null) {
                        alertDialogStores2.cancel();
                    }*/
            }
        }
    }

    companion object {

        protected val TAG: String? = null
        lateinit var context: Context
        var lastCheckedPosition = -1
        lateinit var currency: String
        lateinit var  modelItems: List<CurrencyModel>
    }


}
