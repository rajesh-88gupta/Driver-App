package com.seentechs.newtaxidriver.home.fragments.language

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
import com.seentechs.newtaxidriver.home.fragments.currency.CurrencyModel
import com.seentechs.newtaxidriver.home.managevehicles.SettingActivity
import com.seentechs.newtaxidriver.home.managevehicles.SettingActivity.Companion.alertDialogStores2
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.home.signinsignup.SigninSignupHomeActivity
import java.util.*
import javax.inject.Inject

/**
 * Created by Seen Technologies on 31/5/18.
 */

class LanguageAdapter(context: Context, Items: MutableList<CurrencyModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val TYPE_Explore = 0
    val TYPE_LOAD = 1
      var  modelItems: MutableList<CurrencyModel>
    val context :Context
   @Inject
   lateinit var sessionManager: SessionManager


    init {
        this.context= context
        this.modelItems = Items
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        AppController.getAppComponent().inject(this)
        return MovieHolder(inflater.inflate(R.layout.currency_item_view, parent, false))

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (getItemViewType(position) == TYPE_Explore) {
                (holder as MovieHolder).bindData(modelItems.get(position))
        }
        //No else part needed as load holder doesn't bind any data
    }

    override fun getItemViewType(position: Int): Int {

        return TYPE_Explore

    }

    override fun getItemCount(): Int {

        return modelItems.size
      //  println("modelItems"+modelItems.size)
       /* if (this::modelItems.isInitialized) {
            println("true")
            return this.modelItems.size
            // file is not null
        }
        println("false")

        return 0*/
    }

    fun setLocale(lang: String) {
        val myLocale = Locale(lang)
        val res = context.resources
        val dm = res.displayMetrics
        val conf = res.configuration
        conf.setLocale(myLocale)
        conf.locale = myLocale
        res.updateConfiguration(conf, dm)
    }

    internal inner class MovieHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val languagen: TextView
        private val languagecode: TextView
        private val radiobutton: RadioButton
        private val selectlanguage: RelativeLayout

        init {
            languagen = itemView.findViewById<View>(R.id.currencyname_txt) as TextView
            languagecode = itemView.findViewById<View>(R.id.currencysymbol_txt) as TextView
            radiobutton = itemView.findViewById<View>(R.id.radioButton1) as RadioButton
            selectlanguage = itemView.findViewById<View>(R.id.selectcurrency) as RelativeLayout
        }

        fun bindData(movieModel: CurrencyModel) {

            val currencycode: String
            currencycode = sessionManager.language!!


            languagen.text = movieModel.currencyName
            languagecode.text = movieModel.currencySymbol
            languagecode.visibility = View.GONE

            val colorStateList = ColorStateList(
                arrayOf(
                    intArrayOf(-android.R.attr.state_checked),
                    intArrayOf(android.R.attr.state_checked)
                ),
                intArrayOf(

                    ContextCompat.getColor(context,R.color.ub__uber_black_60),
                        ContextCompat.getColor(context,R.color.rb_blue_button)
                   // context.resources.getColor(R.color.rb_blue_button)
                )
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                radiobutton.buttonTintList = colorStateList
            }

            radiobutton.isChecked = false

            if (movieModel.currencyName == currencycode) {
                radiobutton.isChecked = true
            }


            selectlanguage.setOnClickListener {
                SettingActivity.langclick = true
                language = languagen.text.toString() + " (" + languagecode.text.toString() + ")"
                sessionManager.language = languagen.text.toString()
                sessionManager.languageCode = languagecode.text.toString()


                lastCheckedPosition = adapterPosition
                radiobutton.isChecked = true
                val myLocale = Locale(movieModel.currencySymbol)
                val res = context.resources
                val dm = res.displayMetrics
                val conf = res.configuration
                conf.setLocale(myLocale)
                conf.locale = myLocale
                res.updateConfiguration(conf, dm)

                alertDialogStores2?.cancel()
              //  if (SigninSignupHomeActivity.Companion.alertDialogStores!= null) {
                    SigninSignupHomeActivity.Companion.alertDialogStores?.cancel()
              //  }
            }
        }
    }

    companion object {

        lateinit var context: Context
        lateinit var language: String
        var lastCheckedPosition = -1

    }
}
