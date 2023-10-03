package com.seentechs.newtaxidriver.home.fragments.payment

/**
 * @package com.newtaxi
 * @subpackage views.main.paytoadmin
 * @category Payment Activity
 * @author Seen Technologies
 *
 */

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

import com.google.gson.Gson

import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.Enums
import com.seentechs.newtaxidriver.common.util.RequestCallback



import javax.inject.Inject

import butterknife.ButterKnife
import butterknife.BindView
import butterknife.OnClick
import com.seentechs.newtaxidriver.home.datamodel.PaymentMethodsModel
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonKeys
import com.seentechs.newtaxidriver.common.views.CommonActivity
import kotlinx.android.synthetic.main.activity_payment.*


/*************************************************************
 * PaymentActivity
 * Its used to show the Payment details for the Driver
 */

class PaymentActivity : CommonActivity(), View.OnClickListener, ServiceListener,PaymentMethodAdapter.ItemOnClickListener {


    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var commonMethods: CommonMethods
    @Inject
    lateinit var customDialog: CustomDialog
    @Inject
    lateinit var customDialog1: CustomDialog
    @Inject
    lateinit var sessionManager: SessionManager
    @Inject
    lateinit var gson: Gson

    @BindView(R.id.ivCardTick)
    lateinit var ivCardTick: ImageView
    @BindView(R.id.ivCard)
    lateinit var ivCard: ImageView
    @BindView(R.id.tvCard)
    lateinit var tvCard: TextView
    @BindView(R.id.rltCard)
    lateinit var rltCard: RelativeLayout
    @BindView(R.id.rltAddCard)
    lateinit var rltAddCard: RelativeLayout
    lateinit private var stripePublishKey: String
    lateinit private var dialog: AlertDialog
    private var clientSecretKey = ""

    private lateinit var paymentmethodadapter:PaymentMethodAdapter
    private var paymentArryalist=ArrayList<PaymentMethodsModel.PaymentMethods>()
    @OnClick(R.id.back)
    fun onBack() {
        onBackPressed()
    }
    /*
  *set payment mode as BrainTree
  */
    @OnClick(R.id.rltbraintree)
    fun brainTreeClick()
    {

        sessionManager.paymentMethodkey = CommonKeys.PAYMENT_BRAINTREE
        ivbraintreetick.visibility=View.VISIBLE
        ivCardTick.visibility = View.GONE
        paypal_tickimg.visibility=View.GONE
        onBackPressed()
    }

    /*
*set payment mode as Paypal
*/
    @OnClick(R.id.paypal)
    fun paypalClick()
    {
        sessionManager.paymentMethodkey = CommonKeys.PAYMENT_PAYPAL
        ivbraintreetick.visibility=View.GONE
        ivCardTick.visibility = View.GONE
        paypal_tickimg.visibility=View.VISIBLE
        onBackPressed()
    }
    /**
     * Set payment as Card
     */
    @OnClick(R.id.rltCard)
    fun cardClick() {
        sessionManager.paymentMethodkey = CommonKeys.PAYMENT_CARD
        ivbraintreetick.visibility=View.GONE
        ivCardTick.visibility = View.VISIBLE
        paypal_tickimg.visibility=View.GONE
        onBackPressed()
    }


    override fun onItemClick() {
        val stripe = Intent(applicationContext, AddCardActivity::class.java)
        startActivityForResult(stripe, REQUEST_CODE_PAYMENT)
    }
    /**
     * goes to Add Card Page
     */
    @OnClick(R.id.rltAddCard)
    fun addCardClick() {
        val stripe = Intent(applicationContext, AddCardActivity::class.java)
        startActivityForResult(stripe, REQUEST_CODE_PAYMENT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        AppController.getAppComponent().inject(this)
        ButterKnife.bind(this)
        commonMethods.setheaderText(resources.getString(R.string.payment),common_header)
        dialog = commonMethods.getAlertDialog(this)
        //showPaymentTickAccordingToTheSelection()
        paymentmethodadapter=PaymentMethodAdapter(this@PaymentActivity,paymentArryalist,this)
        rv_payment_list.adapter=paymentmethodadapter
      /*  if (!TextUtils.isEmpty(sessionManager.cardValue))
        {
            rltCard.visibility = View.VISIBLE
            setCardImage(sessionManager.cardBrand)
            tvCard.text = "•••• ${sessionManager.cardValue}"

        }*/

        getPaymentMethodList()


        /**
         * View Card Details
         */
      //  commonMethods.showProgressDialog(this)
      //  commonMethods.rotateArrow(ivBack, this)
        println("Token " + sessionManager.accessToken)
     //   apiService.viewCard(sessionManager.accessToken!!).enqueue(RequestCallback(Enums.REQ_VIEW_PAYMENT, this))

    }

    fun  getPaymentMethodList()
    {
        commonMethods.showProgressDialog(this);
        apiService.getPaymentMethodlist(sessionManager.accessToken!!,CommonKeys.isWallet).enqueue(RequestCallback(Enums.REG_GET_PAYMENTMETHOD, this))
       /* paymentArryalist.clear()
        for(i in 0..10)
        {
            if(i==3) paymentArryalist.add(PaymentMethodsModel(i.toString(),"Cash",true))
            paymentArryalist.add(PaymentMethodsModel(i.toString(),"Paypal",false))
        }*/
    }



    /**
     * Result form Add card
     */

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if(data !=null)
            {
                val setUp_Id = data.getStringExtra("S_intentId")
                if (setUp_Id != null && !setUp_Id.isEmpty()) {
                    commonMethods.showProgressDialog(this@PaymentActivity)
                    addcard(setUp_Id)
                }
            }
        }
    }

    /**
     * After Stripe payment
     */
    fun addcard(payKey: String) {
        if (!TextUtils.isEmpty(payKey)) {
            //            commonMethods.showProgressDialog(this);
            apiService.addCard(payKey, sessionManager.accessToken!!).enqueue(RequestCallback(Enums.REQ_ADD_CARD, this))
        }
    }
    /**
     * On Success From API
     */
    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data)) commonMethods.showMessage(this, dialog, data)
            return
        }
        when (jsonResp.requestCode) {

            Enums.REG_GET_PAYMENTMETHOD-> if(jsonResp.isSuccess)
            {
                val paymentmodel = gson.fromJson(jsonResp.strResponse, PaymentMethodsModel::class.java)
                var isDefaultpaymentmethod=""
                paymentArryalist.addAll(paymentmodel.paymentlist)

                if (sessionManager.paymentMethodkey.isNotEmpty()) {
                    for (i in 0 until paymentmodel.paymentlist.size) {

                        CommonKeys.isSetPaymentMethod=true
                        if (sessionManager.paymentMethodkey.equals(paymentmodel.paymentlist.get(i).paymenMethodKey)) {
                            paymentmethodadapter.notifyDataSetChanged()
                            return
                        } else {
                            if (paymentmodel.paymentlist[i].isDefaultPaymentMethod) {
                                CommonKeys.isSetPaymentMethod=false
                            }
                        }
                    }
                    sessionManager.paymentMethodkey=""
                } else {
                    for (i in 0 until paymentmodel.paymentlist.size) {
                        if (paymentmodel.paymentlist[i].isDefaultPaymentMethod) {
                            CommonKeys.isSetPaymentMethod=false
                           /* sessionManager.paymentMethodkey=paymentmodel.paymentlist[i].paymenMethodKey
                            sessionManager.paymentMethod=paymentmodel.paymentlist[i].paymenMethodvalue
                            sessionManager.paymentMethodImage=paymentmodel.paymentlist[i].paymenMethodIcon*/
                        }
                    }
                }
                paymentmethodadapter.notifyDataSetChanged()

            }else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }
            Enums.REQ_ADD_CARD -> if (jsonResp.isSuccess) {

                val brand = commonMethods.getJsonValue(jsonResp.strResponse, "brand", String::class.java) as String
                val last4 = commonMethods.getJsonValue(jsonResp.strResponse, "last4", String::class.java) as String

                if (!TextUtils.isEmpty(last4)) {
                    rltCard.visibility = View.VISIBLE
                    setCardImage(brand)
                    tvCard.text = "•••• $last4"
                    ivCardTick.visibility = View.VISIBLE
                    ivbraintreetick.visibility=View.GONE
                    paypal_tickimg.visibility=View.GONE
                    sessionManager.paymentMethod = CommonKeys.PAYMENT_CARD
                    sessionManager.paymentMethodkey = CommonKeys.PAYMENT_CARD
                    sessionManager.paymentMethodImage=""
                    sessionManager.walletCard = 1
                    sessionManager.cardValue = last4
                    sessionManager.cardBrand = brand
                } else {
                    rltCard.visibility = View.GONE
                }
                 onBackPressed();
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }
            Enums.REQ_VIEW_PAYMENT -> if (jsonResp.isSuccess) {
                clientSecretKey = commonMethods.getJsonValue(jsonResp.strResponse, "intent_client_secret", String::class.java) as String
                val brand = commonMethods.getJsonValue(jsonResp.strResponse, "brand", String::class.java) as String
                val last4 = commonMethods.getJsonValue(jsonResp.strResponse, "last4", String::class.java) as String

                if (!TextUtils.isEmpty(last4)) {
                    rltCard.visibility = View.VISIBLE
                    setCardImage(brand)
                    tvCard.text = "•••• $last4"
                    sessionManager.cardValue = last4
                    sessionManager.cardBrand = brand
                    sessionManager.walletCard = 1
                    ivCardTick.visibility = View.GONE
                } else {
                    rltCard.visibility = View.GONE
                }
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg) && jsonResp.statusMsg.equals("No record found", ignoreCase = true)) {
                clientSecretKey = commonMethods.getJsonValue(jsonResp.strResponse, "intent_client_secret", String::class.java) as String
            } else {
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }
            else -> {
            }
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }

    /**
     * Set Card Images
     */
    fun setCardImage(brand: String?) {
        if ("Visa".contains(brand!!)) {
            ivCard.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.card_visa))
        } else if ("MasterCard".contains(brand)) {
            ivCard.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.card_master))
        } else if ("Discover".contains(brand)) {
            ivCard.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.card_discover))
        } else if (brand.contains("Amex") || brand.contains("American Express")) {
            ivCard.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.card_amex))
        } else if (brand.contains("JCB") || brand.contains("JCP")) {
            ivCard.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.card_jcp))
        } else if (brand.contains("Diner") || brand.contains("Diners")) {
            ivCard.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.card_diner))
        } else if ("Union".contains(brand) || "UnionPay".contains(brand)) {
            ivCard.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.card_unionpay))
        } else {
            ivCard.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.ic_card))
        }
    }

    override fun onClick(v: View) {

    }

    companion object {


        private val REQUEST_CODE_PAYMENT = 1
    }


}




