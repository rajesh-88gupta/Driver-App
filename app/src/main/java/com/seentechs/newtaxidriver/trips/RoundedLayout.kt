package com.seentechs.newtaxidriver.trips

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.RelativeLayout

class RoundedLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : RelativeLayout(context, attrs, defStyle) {

    private val mPathCorners = Path()
    private val mPathCircle = Path()
    private var mCornerRadius: Float = 0.toFloat()

    /**
     * border path
     */
    private val mPathCornersBorder = Path()
    private val mPathCircleBorder = Path()
    private var mBorderWidth = 0
    private var mBorderHalf: Int = 0
    private var mShowBorder = false
    private var mBorderColor = -0x8900

    private var mDensity = 1.0f

    /**
     * Rounded corners or circle shape
     */
    private var mIsCircleShape = false

    private val mPaint = Paint()

    // helper reusable vars, just IGNORE
    private var halfWidth: Float = 0.toFloat()
    private var halfHeight: Float = 0.toFloat()
    private var centerX: Float = 0.toFloat()
    private var centerY: Float = 0.toFloat()
    private val rect = RectF(0f, 0f, 0f, 0f)
    private val rectBorder = RectF(0f, 0f, 0f, 0f)

    // helper reusable var, just IGNORE
    private var save: Int = 0

    private fun dpFromPx(px: Float): Float {
        return px / mDensity
    }

    private fun pxFromDp(dp: Float): Float {
        return dp * mDensity
    }

    init {
        mDensity = resources.displayMetrics.density
        // just a default for corner radius
        mCornerRadius = pxFromDp(25f)

        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.STROKE
        mPaint.color = mBorderColor
        setBorderWidth(Math.round(pxFromDp(2f)))
    }

    /**
     * Switch to circle or rectangle shape
     *
     * @param useCircle
     */
    fun setShapeCircle(useCircle: Boolean) {
        mIsCircleShape = useCircle
        invalidate()
    }

    /**
     * change corner radius
     *
     * @param radius
     */
    fun setCornerRadius(radius: Int) {
        mCornerRadius = radius.toFloat()
        invalidate()
    }

    fun showBorder(show: Boolean) {
        mShowBorder = show
        invalidate()
    }

    fun setBorderWidth(width: Int) {
        mBorderWidth = width
        mBorderHalf = Math.round((mBorderWidth / 2).toFloat())
        if (mBorderHalf == 0) {
            mBorderHalf = 1
        }

        mPaint.strokeWidth = mBorderWidth.toFloat()
        updateCircleBorder()
        updateRectangleBorder()
        invalidate()
    }

    fun setBorderColor(color: Int) {
        mBorderColor = color
        mPaint.color = color
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // just calculate both shapes, is not heavy

        // rounded corners path
        rect.left = 0f
        rect.top = 0f
        rect.right = w.toFloat()
        rect.bottom = h.toFloat()
        mPathCorners.reset()
        mPathCorners.addRoundRect(rect, mCornerRadius, mCornerRadius, Path.Direction.CW)
        mPathCorners.close()

        // circle path
        halfWidth = w / 2f
        halfHeight = h / 2f
        centerX = halfWidth
        centerY = halfHeight
        mPathCircle.reset()
        mPathCircle.addCircle(centerX, centerY, Math.min(halfWidth, halfHeight), Path.Direction.CW)
        mPathCircle.close()

        updateRectangleBorder()
        updateCircleBorder()
    }

    override fun dispatchDraw(canvas: Canvas) {
        save = canvas.save()
        canvas.clipPath(if (mIsCircleShape) mPathCircle else mPathCorners)
        super.dispatchDraw(canvas)
        canvas.restoreToCount(save)

        if (mShowBorder) {
            canvas.drawPath(if (mIsCircleShape) mPathCircleBorder else mPathCornersBorder, mPaint)
        }
    }

    private fun updateCircleBorder() {
        // border path for circle
        mPathCircleBorder.reset()
        mPathCircleBorder.addCircle(centerX, centerY, Math.min(halfWidth - mBorderHalf,
                halfHeight - mBorderHalf), Path.Direction.CW)
        mPathCircleBorder.close()
    }

    private fun updateRectangleBorder() {
        // border path for rectangle
        rectBorder.left = rect.left + mBorderHalf
        rectBorder.top = rect.top + mBorderHalf
        rectBorder.right = rect.right - mBorderHalf
        rectBorder.bottom = rect.bottom - mBorderHalf
        mPathCornersBorder.reset()
        mPathCornersBorder.addRoundRect(rectBorder, mCornerRadius - mBorderHalf, mCornerRadius - mBorderHalf, Path.Direction.CW)
        mPathCornersBorder.close()
    }
}