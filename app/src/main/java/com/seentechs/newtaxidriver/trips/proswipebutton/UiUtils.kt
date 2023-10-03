package com.seentechs.newtaxidriver.trips.proswipebutton

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.animation.AnimationUtils

import com.seentechs.newtaxidriver.R

/**
 * Created by shadow-admin on 24/10/17.
 */

object UiUtils {

    fun animateFadeHide(context: Context, view: View?) {
        if (view != null && view.visibility == View.VISIBLE) {
            val animFadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out)

            view.startAnimation(animFadeOut)
            view.visibility = View.GONE
        }
    }

    fun animateFadeShow(context: Context, view: View) {
        if (view.visibility != View.VISIBLE) {
            val animFadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)

            view.startAnimation(animFadeIn)
            view.visibility = View.VISIBLE
        }
    }

    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

}
