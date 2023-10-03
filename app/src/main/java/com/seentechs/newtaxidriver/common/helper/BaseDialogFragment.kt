package com.seentechs.newtaxidriver.common.helper

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.seentechs.newtaxidriver.R

/**
 * Created by Seen Technologies on 9/7/18.
 */

open class BaseDialogFragment : DialogFragment() {
    protected var mActivity: Activity? = null
    private var layoutId: Int = 0
    private var isNeedAnimation = true


    fun setLayoutId(layoutId: Int) {
        this.layoutId = layoutId
    }

    fun setAnimation(isNeedAnimation: Boolean) {
        this.isNeedAnimation = isNeedAnimation
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isNeedAnimation) {
            setStyle(DialogFragment.STYLE_NO_TITLE, R.style.share_dialog)
        } else {
            setStyle(DialogFragment.STYLE_NO_TITLE, R.style.progress_dialog)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(layoutId, container, false)
        initViews(v)
        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = if (context is Activity) context else null
    }

    open fun initViews(v: View) {
        dialog!!.setCanceledOnTouchOutside(false)
    }
}

