package com.seentechs.newtaxidriver.home.placesearch

/**
 * @package com.seentechs.newtaxidriver
 * @subpackage placesearch
 * @category Recycler view Item click listener
 * @author Seen Technologies
 *
 */

import android.content.Context
import androidx.recyclerview.widget.RecyclerView

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

import com.seentechs.newtaxidriver.common.util.CommonMethods

/* ************************************************************
Place search list click listener
*************************************************************** */

class RecyclerItemClickListener(context: Context, private val mListener: OnItemClickListener?) : RecyclerView.OnItemTouchListener {
    private val mGestureDetector: GestureDetector

    init {
        mGestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }
        })
    }

    override fun onInterceptTouchEvent(view: RecyclerView, e: MotionEvent): Boolean {
        val childView = view.findChildViewUnder(e.x, e.y)
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            mListener.onItemClick(childView, view.getChildLayoutPosition(childView))
            return true
        }
        return false
    }

    override fun onTouchEvent(view: RecyclerView, motionEvent: MotionEvent) {
        CommonMethods.DebuggableLogI("MotionEvent", "motionEvent")
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        CommonMethods.DebuggableLogI("disallowIntercept", "disallowIntercept")

    }

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }
}