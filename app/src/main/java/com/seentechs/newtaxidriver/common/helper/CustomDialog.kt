package com.seentechs.newtaxidriver.common.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import com.seentechs.newtaxidriver.R

/**
 * Created by Seen Technologies on 9/7/18.
 */

@SuppressLint("ValidFragment")
class CustomDialog : BaseDialogFragment {
    private var title = ""
    private var message = ""
    private var positiveBtnTxt = ""
    private var negativeBtnTxt = ""
    private var confirmTxt = ""
    private var index = -1
    private var tvAllow: TextView? = null
    private var tvDeny: TextView? = null
    private var allowClickListener: btnAllowClick? = null
    private var denyClickListener: btnDenyClick? = null
    private var listItemsClickListener: listItemsClick? = null
    private var popUpItemsClickListener: popUpItemsClick? = null
    private var listPopupWindow: ListPopupWindow? = null
    private var isProgressDialog = false
    private var prgresslayout: RelativeLayout? = null
    private var alertDialogLayout: RelativeLayout? = null


    constructor() {}

    constructor(message: String, confirmTxt: String, okClickListener: btnAllowClick) {
        this.message = message
        this.confirmTxt = confirmTxt
        this.allowClickListener = okClickListener
        this.mActivity = null
        this.index = -1
        setLayoutId(R.layout.activity_custom_dialog)
    }

    constructor(isProgressDialog: Boolean) {
        this.isProgressDialog = isProgressDialog
        this.mActivity = null
        this.message = "Loading..."
        this.index = -1
        setAnimation(false)
        setLayoutId(R.layout.activity_custom_dialog)
    }

    constructor(isProgressDialog: Boolean,message: String) {
        this.isProgressDialog = isProgressDialog
        this.mActivity = null
        this.index = -1
        this.message = message
        setAnimation(false)
        setLayoutId(R.layout.activity_custom_dialog)
    }

    constructor(message: String, positiveBtnTxt: String, negativeBtnTxt: String, allowClickListener: btnAllowClick, denyClickListener: btnDenyClick) {
        this.message = message
        this.confirmTxt = ""
        this.positiveBtnTxt = positiveBtnTxt
        this.negativeBtnTxt = negativeBtnTxt
        this.allowClickListener = allowClickListener
        this.denyClickListener = denyClickListener
        this.mActivity = null
        this.index = -1
        setLayoutId(R.layout.activity_custom_dialog)
    }


    constructor(title: String, message: String, confirmTxt: String, okClickListener: btnAllowClick) {
        this.title = title
        this.message = message
        this.confirmTxt = confirmTxt
        this.allowClickListener = okClickListener
        this.mActivity = null
        this.index = -1
        setLayoutId(R.layout.activity_custom_dialog)
    }

    constructor(title: String, message: String, positiveBtnTxt: String, negativeBtnTxt: String, allowClickListener: btnAllowClick, denyClickListener: btnDenyClick) {
        this.title = title
        this.message = message
        this.confirmTxt = ""
        this.positiveBtnTxt = positiveBtnTxt
        this.negativeBtnTxt = negativeBtnTxt
        this.allowClickListener = allowClickListener
        this.denyClickListener = denyClickListener
        this.mActivity = null
        this.index = -1
        setLayoutId(R.layout.activity_custom_dialog)
    }

    constructor(index: Int, title: String, message: String, positiveBtnTxt: String, negativeBtnTxt: String, allowClickListener: btnAllowClick, denyClickListener: btnDenyClick) {
        this.title = title
        this.message = message
        this.confirmTxt = ""
        this.positiveBtnTxt = positiveBtnTxt
        this.negativeBtnTxt = negativeBtnTxt
        this.allowClickListener = allowClickListener
        this.denyClickListener = denyClickListener
        this.mActivity = null
        this.index = index
        setAnimation(false)
        setLayoutId(R.layout.activity_custom_dialog)
    }

    fun showListDialog(context: Context, listItems: List<String>, listener: listItemsClick) {
        this.listItemsClickListener = listener
        val listAlertDialogBuilder = AlertDialog.Builder(context)
        val arrayAdapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1)
        arrayAdapter.addAll(listItems)
        listAlertDialogBuilder.setAdapter(arrayAdapter) { _, which -> listItemsClickListener!!.clicked(which) }
        listAlertDialogBuilder.show()
    }

    fun showListPopup(context: Context, options: List<String>, anchorView: View, listener: popUpItemsClick) {

        this.popUpItemsClickListener = listener
        listPopupWindow = ListPopupWindow(context)
        listPopupWindow!!.anchorView = anchorView
        listPopupWindow!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context, R.color.white)))
        val dataAdapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, options)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        listPopupWindow!!.setAdapter(dataAdapter)
        listPopupWindow!!.setOnItemClickListener { parent, _, position, _ ->
            popUpItemsClickListener!!.clicked(parent.getItemAtPosition(position).toString())
            if (listPopupWindow != null && listPopupWindow!!.isShowing) {
                listPopupWindow!!.dismiss()
            }
        }
        listPopupWindow!!.show()
    }

    override fun initViews(v: View) {
        super.initViews(v)
        val tvTitle = v.findViewById<View>(R.id.tv_dialog_title) as TextView
        val tvMessage = v.findViewById<View>(R.id.tv_loading) as TextView
        this.tvAllow = v.findViewById<View>(R.id.tv_allow) as TextView
        this.tvDeny = v.findViewById<View>(R.id.tv_deny) as TextView
        this.alertDialogLayout = v.findViewById<View>(R.id.rlt_alert_dialog_layout) as RelativeLayout
        this.prgresslayout = v.findViewById<View>(R.id.llt_progress_dialog) as RelativeLayout
        tvMessage.text = message

        if (isProgressDialog) {
            this.prgresslayout!!.visibility = View.VISIBLE
            this.alertDialogLayout!!.visibility = View.GONE
        } else {
            this.prgresslayout!!.visibility = View.GONE
            this.alertDialogLayout!!.visibility = View.VISIBLE
            if (!TextUtils.isEmpty(title)) {
                tvTitle.text = title
                tvTitle.visibility = View.VISIBLE
            }
            if (!TextUtils.isEmpty(confirmTxt)) {
                this.tvDeny!!.visibility = View.GONE
                this.tvAllow!!.visibility = View.VISIBLE
                this.tvAllow!!.text = confirmTxt
            } else {
                this.tvAllow!!.visibility = View.VISIBLE
                this.tvDeny!!.visibility = View.VISIBLE
                this.tvAllow!!.text = positiveBtnTxt
                this.tvDeny!!.text = negativeBtnTxt
            }

            if (index >= 0) {
                this.tvAllow!!.setTextColor(ContextCompat.getColor(mActivity!!, R.color.app_background))
            }
        }

        initEvent()
        isCancelable = false
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mActivity = if (context is Activity) context else null
    }

    private fun initEvent() {
        tvAllow!!.setOnClickListener {
            if (allowClickListener != null) {
                allowClickListener!!.clicked()
            }
            dismiss()
        }

        tvDeny!!.setOnClickListener {
            if (denyClickListener != null) {
                denyClickListener!!.clicked()
            }
            dismiss()
        }
    }

    interface btnAllowClick {
        fun clicked()
    }


    interface btnDenyClick {
        fun clicked()
    }

    interface listItemsClick {
        fun clicked(which: Int): Int
    }

    interface popUpItemsClick {
        fun clicked(selectedItem: String): String
    }
}

