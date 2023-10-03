package com.seentechs.newtaxidriver.common.util.userchoice

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.Enums
import com.seentechs.newtaxidriver.home.datamodel.CurreneyListModel
import com.seentechs.newtaxidriver.home.fragments.currency.CurrencyModel
import com.seentechs.newtaxidriver.home.payouts.isClicked
import com.seentechs.newtaxidriver.home.payouts.payout_model_classed.CountryModel
import com.seentechs.newtaxidriver.home.payouts.payout_model_classed.Makent_model
import java.util.*
import javax.inject.Inject

class UserChoice {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    private var context: Context? = null
    private lateinit var languageList: MutableList<CurrencyModel>
    private lateinit var currencyList: ArrayList<CurreneyListModel>
    private lateinit var countryModelList: ArrayList<CountryModel>
    private lateinit var stripeCountryModel: ArrayList<Makent_model>
    private var type: String? = null
    private var userChoiceSuccessResponse: UserChoiceSuccessResponse? = null
    private var bottomSheetDialog: BottomSheetDialog? = null


    fun getUsersLanguages(context: Context?, languageList: MutableList<CurrencyModel>, type: String?, userChoiceSuccessResponse: UserChoiceSuccessResponse?) {
        this.context = context
        this.languageList = languageList
        this.type = type
        this.userChoiceSuccessResponse = userChoiceSuccessResponse
        showBottomSheet()
    }

    fun getUserCurrency(context: Context?, currencyList: ArrayList<CurreneyListModel>, type: String?, userChoiceSuccessResponse: UserChoiceSuccessResponse?) {
        this.context = context
        this.currencyList = currencyList
        this.type = type
        this.userChoiceSuccessResponse = userChoiceSuccessResponse
        showBottomSheet()
    }

    fun getCountryListForPayouts(context: Context?, countryModel: ArrayList<CountryModel>, type: String?, userChoiceSuccessResponse: UserChoiceSuccessResponse?) {
        this.context = context
        this.countryModelList = countryModel
        this.type = type
        this.userChoiceSuccessResponse = userChoiceSuccessResponse
        showBottomSheet()
    }

    fun getStripeCountryCurrency(context: Context?, stripeCountryModel: ArrayList<Makent_model>, type: String?, userChoiceSuccessResponse: UserChoiceSuccessResponse?) {
        this.context = context
        this.stripeCountryModel = stripeCountryModel
        this.type = type
        this.userChoiceSuccessResponse = userChoiceSuccessResponse
        showBottomSheet()
    }

    init {
        AppController.getAppComponent().inject(this)
    }

    /**
     * init BottomSheet
     */
    private fun showBottomSheet() {
        bottomSheetDialog = BottomSheetDialog(context!!, R.style.BottomSheetDialogTheme)
        bottomSheetDialog!!.setContentView(R.layout.app_bottom_sheet_for_language_currency)
        val lltUserChoice = bottomSheetDialog!!.findViewById<LinearLayout>(R.id.llt_user_choice)
        val tvTitle = bottomSheetDialog!!.findViewById<TextView>(R.id.tv_title)
        val ivClose = bottomSheetDialog!!.findViewById<ImageView>(R.id.iv_close)
        when {
            type.equals(Enums.USER_CHOICE_LANGUAGE, ignoreCase = true) -> {
                tvTitle!!.text = context!!.resources.getString(R.string.select_view, context!!.resources.getString(R.string.language))
            }
            type.equals(Enums.USER_CHOICE_CURRENCY, ignoreCase = true) || type.equals(Enums.USER_CHOICE_STRIPE_CURRENCY, ignoreCase = true) -> {
                tvTitle!!.text = context!!.resources.getString(R.string.select_view, context!!.resources.getString(R.string.currencies))
            }
            type.equals(Enums.USER_CHOICE_COUNTRY, ignoreCase = true) || type.equals(Enums.USER_CHOICE_STRIPE_COUNTRY, ignoreCase = true) -> {
                tvTitle!!.text = context!!.resources.getString(R.string.select_view, context!!.resources.getString(R.string.country_hint))
            }

            /**
             * User's Choice
             */
        }
        ivClose!!.setOnClickListener { bottomSheetDialog!!.dismiss() }
        if (!bottomSheetDialog!!.isShowing) {
            bottomSheetDialog!!.show()
            lltUserChoice!!.removeAllViews()
        }

        bottomSheetDialog!!.setOnDismissListener {
            if (type?.equals(Enums.USER_CHOICE_COUNTRY, ignoreCase = true)!!) {
                isClicked = true
            }
        }

        /**
         * User's Choice
         */
        when {
            type.equals(Enums.USER_CHOICE_LANGUAGE, ignoreCase = true) -> {
                for (i in languageList.indices) {
                    val view = LayoutInflater.from(context).inflate(R.layout.app_user_choice_items, null)
                    val rltUserChoice = view.findViewById<RelativeLayout>(R.id.rltUserChoice)
                    val tvName = view.findViewById<TextView>(R.id.tvname)
                    val tvCode = view.findViewById<TextView>(R.id.tv_code)
                    val ivTick = view.findViewById<RadioButton>(R.id.ivTick)
                    tvCode.visibility = View.GONE
                    tvName.text = languageList[i].currencyName
                    if (sessionManager.language.equals(languageList[i].currencyName, ignoreCase = true)) {
                        ivTick.isChecked = true
                    }
                    rltUserChoice.tag = i
                    rltUserChoice.setOnClickListener {
                        sessionManager.language = languageList[rltUserChoice.tag as Int].currencyName
                        sessionManager.languageCode = languageList[rltUserChoice.tag as Int].currencySymbol
                        ivTick.isChecked = true
                        userChoiceSuccessResponse!!.onSuccessUserSelected(type, languageList[rltUserChoice.tag as Int].currencyName, languageList[rltUserChoice.tag as Int].currencySymbol)
                        bottomSheetDialog!!.dismiss()
                    }
                    lltUserChoice?.addView(view)
                }
            }
            type.equals(Enums.USER_CHOICE_CURRENCY, ignoreCase = true) -> {
                for (i in currencyList.indices) {
                    val view = LayoutInflater.from(context).inflate(R.layout.app_user_choice_items, null)
                    val rltUserChoice = view.findViewById<RelativeLayout>(R.id.rltUserChoice)
                    val tvName = view.findViewById<TextView>(R.id.tvname)
                    val tvCode = view.findViewById<TextView>(R.id.tv_code)
                    val ivTick = view.findViewById<RadioButton>(R.id.ivTick)
                    tvCode.visibility = View.VISIBLE
                    tvName.text = currencyList[i].code
                    tvCode.text = currencyList[i].symbol
                    if (sessionManager.currencyCode.equals(currencyList[i].code, ignoreCase = true)) {
                        ivTick.isChecked = true
                    }
                    rltUserChoice.tag = i
                    rltUserChoice.setOnClickListener {
                        sessionManager.currencyCode = currencyList[rltUserChoice.tag as Int].code
                        sessionManager.currencySymbol = currencyList[rltUserChoice.tag as Int].symbol
                        ivTick.isChecked = true
                        userChoiceSuccessResponse!!.onSuccessUserSelected(type, currencyList[rltUserChoice.tag as Int].code, currencyList[rltUserChoice.tag as Int].symbol)
                        bottomSheetDialog!!.dismiss()
                    }
                    lltUserChoice?.addView(view)
                }
            }
            type.equals(Enums.USER_CHOICE_COUNTRY, ignoreCase = true) -> {
                for (i in countryModelList.indices) {
                    val view = LayoutInflater.from(context).inflate(R.layout.app_user_choice_items, null)
                    val rltUserChoice = view.findViewById<RelativeLayout>(R.id.rltUserChoice)
                    val tvName = view.findViewById<TextView>(R.id.tvname)
                    val tvCode = view.findViewById<TextView>(R.id.tv_code)
                    val ivTick = view.findViewById<RadioButton>(R.id.ivTick)
                    tvCode.visibility = View.GONE
                    tvName.text = countryModelList[i].countryName

                    if (sessionManager.payPalCountryCode.equals(countryModelList[i].countryCode, ignoreCase = true)) {
                        ivTick.isChecked = true
                    }
                    rltUserChoice.tag = i
                    rltUserChoice.setOnClickListener {
                        sessionManager.payPalCountryCode = countryModelList[rltUserChoice.tag as Int].countryCode
                        ivTick.isChecked = true
                        userChoiceSuccessResponse!!.onSuccessUserSelected(type, countryModelList[rltUserChoice.tag as Int].countryName, countryModelList[rltUserChoice.tag as Int].countryCode)
                        bottomSheetDialog!!.dismiss()
                    }
                    lltUserChoice?.addView(view)
                }
            }

            type.equals(Enums.USER_CHOICE_STRIPE_COUNTRY, ignoreCase = true) || type.equals(Enums.USER_CHOICE_STRIPE_CURRENCY, ignoreCase = true) || type.equals(Enums.USER_CHOICE_STRIPE_GENDER, ignoreCase = true) -> {
                for (i in stripeCountryModel.indices) {
                    val view = LayoutInflater.from(context).inflate(R.layout.app_user_choice_items, null)
                    val rltUserChoice = view.findViewById<RelativeLayout>(R.id.rltUserChoice)
                    val tvName = view.findViewById<TextView>(R.id.tvname)
                    val tvCode = view.findViewById<TextView>(R.id.tv_code)
                    val ivTick = view.findViewById<RadioButton>(R.id.ivTick)
                    tvCode.visibility = View.GONE
                    tvName.text = stripeCountryModel[i].countryName

                    if (type.equals(Enums.USER_CHOICE_STRIPE_COUNTRY, ignoreCase = true)) {
                        if (sessionManager.countryName2.equals(stripeCountryModel[i].countryName, ignoreCase = true)) {
                            ivTick.isChecked = true
                        }
                    } else if (type.equals(Enums.USER_CHOICE_STRIPE_CURRENCY, ignoreCase = true)) {
                        if (sessionManager.currencyName2.equals(stripeCountryModel[i].countryName, ignoreCase = true)) {
                            ivTick.isChecked = true
                        }
                    } else if (type.equals(Enums.USER_CHOICE_STRIPE_GENDER, ignoreCase = true)) {
                        if (sessionManager.gender.equals(stripeCountryModel[i].countryName, ignoreCase = true)) {
                            ivTick.isChecked = true
                        }
                    }
                    rltUserChoice.tag = i
                    rltUserChoice.setOnClickListener {
                        if (type.equals(Enums.USER_CHOICE_STRIPE_COUNTRY, ignoreCase = true)) {
                            sessionManager.countryName2 = stripeCountryModel[rltUserChoice.tag as Int].countryName
                        } else if (type.equals(Enums.USER_CHOICE_STRIPE_CURRENCY, ignoreCase = true)) {
                            sessionManager.currencyName2 = stripeCountryModel[rltUserChoice.tag as Int].countryName
                        } else if (type.equals(Enums.USER_CHOICE_STRIPE_GENDER, ignoreCase = true)) {
                            sessionManager.gender = stripeCountryModel[rltUserChoice.tag as Int].countryName
                        }
                        ivTick.isChecked = true
                        userChoiceSuccessResponse!!.onSuccessUserSelected(type, stripeCountryModel[rltUserChoice.tag as Int].countryName, stripeCountryModel[rltUserChoice.tag as Int].countryCode)
                        bottomSheetDialog!!.dismiss()
                    }
                    lltUserChoice?.addView(view)
                }
            }
        }
    }

    fun getBottomSheetState():BottomSheetDialog?{
        return bottomSheetDialog
    }
}
