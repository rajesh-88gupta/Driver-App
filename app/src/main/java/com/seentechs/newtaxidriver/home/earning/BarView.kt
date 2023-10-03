package com.seentechs.newtaxidriver.home.earning

/**
 * @package com.seentechs.newtaxidriver.home.earning
 * @subpackage earning
 * @category BarView
 * @author Seen Technologies
 *
 */

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

import com.seentechs.newtaxidriver.common.helper.MyUtils

import java.util.ArrayList

/* ************************************************************
                      BarView
Its used get Bar chat details
*************************************************************** */
class BarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val MINI_BAR_WIDTH: Int
    private val BAR_SIDE_MARGIN: Int
    private val TEXT_TOP_MARGIN: Int
    private val TEXT_COLOR = Color.parseColor("#303841")
    private val FOREGROUND_COLOR = Color.parseColor("#1FBAD6")
    private val percentList: ArrayList<Float> = ArrayList<Float>()
    private var targetPercentList: ArrayList<Float>? = null
    private val textPaint: Paint
    private val fgPaint: Paint
    private val rect: Rect
    private var barWidth: Int = 0
    //    private boolean showSideMargin = true;
    private var bottomTextDescent: Int = 0
    private val autoSetWidth = true
    private val topMargin: Int
    private var bottomTextHeight: Int = 0
    private var bottomTextList: ArrayList<String>? = ArrayList()
    private val animator = object : Runnable {
        override fun run() {
            var needNewFrame = false
            for (i in targetPercentList!!.indices) {
                if (percentList[i] < targetPercentList!![i]) {
                    percentList[i] = percentList[i] + 0.02f
                    needNewFrame = true
                } else if (percentList[i] > targetPercentList!![i]) {
                    percentList[i] = percentList[i] - 0.02f
                    needNewFrame = true
                }
                if (Math.abs(targetPercentList!![i] - percentList[i]) < 0.02f) {
                    percentList[i] = targetPercentList!![i]
                }
            }
            if (needNewFrame) {
                postDelayed(this, 20)
            }
            invalidate()
        }
    }

    init {
        /* bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setColor(BACKGROUND_COLOR);*/
        fgPaint = Paint()
        fgPaint.color = FOREGROUND_COLOR
        rect = Rect()
        topMargin = MyUtils.dip2px(context, 5f)
        val textSize = MyUtils.sp2px(context, 15f)
        barWidth = MyUtils.dip2px(context, 22f)
        MINI_BAR_WIDTH = MyUtils.dip2px(context, 22f)
        BAR_SIDE_MARGIN = MyUtils.dip2px(context, 20f)
        TEXT_TOP_MARGIN = MyUtils.dip2px(context, 5f)
        textPaint = Paint()
        textPaint.isAntiAlias = true
        textPaint.color = TEXT_COLOR
        textPaint.textSize = textSize.toFloat()
        textPaint.textAlign = Paint.Align.CENTER
    }

    /**
     * dataList will be reset when called is method.
     *
     * @param bottomStringList The String ArrayList in the bottom.
     */
    fun setBottomTextList(bottomStringList: ArrayList<String>) {
        //        this.dataList = null;
        this.bottomTextList = bottomStringList
        val r = Rect()
        bottomTextDescent = 0
        barWidth = MINI_BAR_WIDTH
        for (s in bottomTextList!!) {
            textPaint.getTextBounds(s, 0, s.length, r)
            if (bottomTextHeight < r.height()) {
                bottomTextHeight = r.height()
            }
            if (autoSetWidth && barWidth < r.width()) {
                barWidth = r.width()
            }
            if (bottomTextDescent < Math.abs(r.bottom)) {
                bottomTextDescent = Math.abs(r.bottom)
            }
        }
        minimumWidth = 2
        postInvalidate()
    }

    /**
     * @param list The ArrayList of Integer with the range of [0-max].
     */
    fun setDataList(list: ArrayList<Int>, max: Int) {
        var maxm = max
        targetPercentList = ArrayList()
        if (maxm == 0) maxm = 1

        for (integer in list) {
            targetPercentList!!.add(1 - integer.toFloat() / maxm.toFloat())
        }

        // Make sure percentList.size() == targetPercentList.size()
        if (percentList.isEmpty() || percentList.size < targetPercentList!!.size) {
            val temp = targetPercentList!!.size - percentList.size
            for (i in 0 until temp) {
                percentList.add(1f)
            }
        } else if (percentList.size > targetPercentList!!.size) {
            val temp = percentList.size - targetPercentList!!.size
            for (i in 0 until temp) {
                percentList.removeAt(percentList.size - 1)
            }
        }
        minimumWidth = 2
        removeCallbacks(animator)
        post(animator)
    }

    override fun onDraw(canvas: Canvas) {
        /*float py = this.getHeight()/2.0f;
        float px = this.getWidth()/2.0f;
        canvas.rotate(180, px, py);*/
        var i = 1
        if (!percentList.isEmpty()) {

            for (f in percentList) {

                rect.set(BAR_SIDE_MARGIN * i + barWidth * (i - 1),
                        topMargin + ((height - topMargin - bottomTextHeight - TEXT_TOP_MARGIN) * percentList[i - 1]).toInt(),
                        (BAR_SIDE_MARGIN + barWidth) * i,
                        height - bottomTextHeight - TEXT_TOP_MARGIN)
                canvas.drawRect(rect, fgPaint)
                i++
            }

        }

        if (bottomTextList != null && !bottomTextList!!.isEmpty()) {
            i = 1
            for (s in bottomTextList!!) {
                canvas.drawText(s, (BAR_SIDE_MARGIN * i + barWidth * (i - 1) + barWidth / 2).toFloat(),
                        (height - bottomTextDescent).toFloat(), textPaint)
                i++
            }
        }
        canvas.save()
        canvas.restore()

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val mViewWidth = measureWidth(widthMeasureSpec)
        val mViewHeight = measureHeight(heightMeasureSpec)
        setMeasuredDimension(mViewWidth, mViewHeight)
    }

    private fun measureWidth(measureSpec: Int): Int {
        var preferred = 0
        if (bottomTextList != null) {
            preferred = bottomTextList!!.size * (barWidth + BAR_SIDE_MARGIN)
        }
        return getMeasurement(measureSpec, preferred)
    }

    private fun measureHeight(measureSpec: Int): Int {
        val preferred = 222
        return getMeasurement(measureSpec, preferred)
    }

    private fun getMeasurement(measureSpec: Int, preferred: Int): Int {
        val specSize = View.MeasureSpec.getSize(measureSpec)
        val measurement: Int
        when (View.MeasureSpec.getMode(measureSpec)) {
            View.MeasureSpec.EXACTLY -> measurement = specSize
            View.MeasureSpec.AT_MOST -> measurement = Math.min(preferred, specSize)
            else -> measurement = preferred
        }
        return measurement
    }

}
