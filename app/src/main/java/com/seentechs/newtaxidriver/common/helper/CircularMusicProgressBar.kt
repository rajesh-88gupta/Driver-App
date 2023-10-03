package com.seentechs.newtaxidriver.common.helper

/**
 * @package com.seentechs.newtaxidriver.common.helper
 * @subpackage helper
 * @category CircularMusicProgressBar
 * @author Seen Technologies
 *
 */

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import android.util.AttributeSet
import android.widget.ImageView
import androidx.core.content.ContextCompat

import com.seentechs.newtaxidriver.R


/* ************************************************************
                CircularMusicProgressBar
Used for circular progress bar in request page
*************************************************************** */
class CircularMusicProgressBar : androidx.appcompat.widget.AppCompatImageView {
    private val mDrawableRect = RectF()
    private val mBorderRect = RectF()

    private val mShaderMatrix = Matrix()
    private val mBitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mFillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mBaseStartAngle = 0f
    private var mBorderColor = DEFAULT_BORDER_COLOR
    private var mBorderWidth = DEFAULT_BORDER_WIDTH
    private var mFillColor = DEFAULT_FILL_COLOR
    private var mProgressColor = DEFAULT_PROGRESS_COLOR
    private var mBitmap: Bitmap? = null
    private var mBitmapShader: BitmapShader? = null
    private var mBitmapWidth: Int = 0
    private var mBitmapHeight: Int = 0
    private var mInnrCircleDiammeter: Float = 0.toFloat()
    private var mDrawableRadius: Float = 0.toFloat()
    private var mProgressValue = 0f
    private var mValueAnimator: ValueAnimator? = null
    private var mColorFilter: ColorFilter? = null
    private var mReady: Boolean = false
    private var mSetupPending: Boolean = false
    private var mBorderOverlay: Boolean = false
    private var mDrawAntiClockwise: Boolean = false
    /**
     * Check transformation is disable or not
     */
    /**
     * Set transformation is disable
     */
    var isDisableCircularTransformation: Boolean = false
        set(disableCircularTransformation) {
            if (isDisableCircularTransformation == disableCircularTransformation) {
                return
            }

            field = disableCircularTransformation
            initializeBitmap()
        }
    private var animationState = true

    /**
     * Get circular view border color
     */
    /**
     * Set border color for circular view
     */
    var borderColor: Int
        get() = mBorderColor
        set(@ColorInt borderColor) {
            if (borderColor == mBorderColor) {
                return
            }

            mBorderColor = borderColor
            mBorderPaint.color = mBorderColor
            invalidate()
        }

    /**
     * Return the color drawn behind the circle-shaped drawable.
     *
     * @return The color drawn behind the drawable
     */
    /**
     * Set a color to be drawn behind the circle-shaped drawable. Note that
     * this has no effect if the drawable is opaque or no drawable is set.
     *
     * @param fillColor The color to be drawn behind the drawable
     */
    var fillColor: Int
        @Deprecated("Fill color support is going to be removed in the future")
        get() = mFillColor
        @Deprecated("Fill color support is going to be removed in the future")
        set(@ColorInt fillColor) {
            if (fillColor == mFillColor) {
                return
            }

            mFillColor = fillColor
            mFillPaint.color = fillColor
            invalidate()
        }

    /**
     * Get border width of circular view
     */
    /**
     * Set border width of circular view
     */
    var borderWidth: Int
        get() = mBorderWidth
        set(borderWidth) {
            if (borderWidth == mBorderWidth) {
                return
            }

            mBorderWidth = borderWidth
            setup()
        }

    /**
     * Check is border overlay or not
     */
    /**
     * Set border overlay
     */
    var isBorderOverlay: Boolean
        get() = mBorderOverlay
        set(borderOverlay) {
            if (borderOverlay == mBorderOverlay) {
                return
            }

            mBorderOverlay = borderOverlay
            setup()
        }

    constructor(context: Context) : super(context) {
        init()
    }

    /**
     * Circular progress bar constructor
     */
    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet, defStyle: Int = 0) : super(context, attrs, defStyle) {

        val a = context.obtainStyledAttributes(attrs, R.styleable.CircularMusicProgressBar, defStyle, 0)

        mBorderWidth = a.getDimensionPixelSize(R.styleable.CircularMusicProgressBar_border_width, DEFAULT_BORDER_WIDTH)
        mBorderColor = a.getColor(R.styleable.CircularMusicProgressBar_border_color, DEFAULT_BORDER_COLOR)
        mBorderOverlay = a.getBoolean(R.styleable.CircularMusicProgressBar_border_overlay, DEFAULT_BORDER_OVERLAY)
        mDrawAntiClockwise = a.getBoolean(R.styleable.CircularMusicProgressBar_draw_anticlockwise, DEFAULT_DRAW_ANTI_CLOCKWISE)
        mFillColor = a.getColor(R.styleable.CircularMusicProgressBar_fill_color, DEFAULT_FILL_COLOR)
        mInnrCircleDiammeter = a.getFloat(R.styleable.CircularMusicProgressBar_centercircle_diammterer, DEFAULT_INNTER_DAIMMETER_FRACTION)
        mProgressColor = a.getColor(R.styleable.CircularMusicProgressBar_progress_color, DEFAULT_PROGRESS_COLOR)
        mBaseStartAngle = a.getFloat(R.styleable.CircularMusicProgressBar_progress_startAngle, 0f)

        a.recycle()
        init()
    }

    /**
     * initialize the animation
     */
    private fun init() {

        // init animator
        mValueAnimator = ValueAnimator.ofFloat(0F, mProgressValue)
        mValueAnimator!!.duration = DEFAULT_ANIMATION_TIME.toLong()
        mValueAnimator!!.addUpdateListener { valueAnimator ->
            mProgressValue = valueAnimator.animatedValue as Float
            invalidate()
        }

        super.setScaleType(SCALE_TYPE)
        mReady = true

        if (mSetupPending) {
            setup()
            mSetupPending = false
        }
    }

    /**
     * Get size of circular view
     */
    override fun getScaleType(): ImageView.ScaleType {
        return SCALE_TYPE
    }

    override fun setScaleType(scaleType: ImageView.ScaleType) {
        if (scaleType == SCALE_TYPE) {
            throw IllegalArgumentException(String.format("ScaleType %s not supported.", scaleType))
        }
    }

    /**
     * Adjust circular view
     */
    override fun setAdjustViewBounds(adjustViewBounds: Boolean) {
        if (adjustViewBounds) {
            throw IllegalArgumentException("adjustViewBounds not supported.")
        }
    }

    /**
     * Draw circular view
     */
    override fun onDraw(canvas: Canvas) {
        if (isDisableCircularTransformation) {
            super.onDraw(canvas)
            return
        }

        if (mBitmap == null) {
            return
        }


        canvas.save()

        canvas.rotate(mBaseStartAngle, mDrawableRect.centerX(), mDrawableRect.centerY())

        if (mBorderWidth > 0) {
            mBorderPaint.color = mBorderColor
            canvas.drawArc(mBorderRect, 0f, 360f, false, mBorderPaint)
        }
        mBorderPaint.color = mProgressColor

        val sweetAngle = mProgressValue / 100 * 360
        canvas.drawArc(mBorderRect, 0f, if (mDrawAntiClockwise) -sweetAngle else sweetAngle, false, mBorderPaint)

        canvas.restore()

        canvas.drawCircle(mDrawableRect.centerX(), mDrawableRect.centerY(), mDrawableRadius, mBitmapPaint)
        if (mFillColor != Color.TRANSPARENT) {
            canvas.drawCircle(mDrawableRect.centerX(), mDrawableRect.centerY(), mDrawableRadius, mFillPaint)
        }

    }

    /**
     * Set value animation
     */
    fun setValue(newValue: Float) {
        if (animationState) {

            if (mValueAnimator!!.isRunning) {
                mValueAnimator!!.cancel()
            }

            mValueAnimator!!.setFloatValues(mProgressValue, newValue)
            mValueAnimator!!.start()
        } else {
            mProgressValue = newValue
            invalidate()
        }

    }

    /**
     * Listener for circular size change
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setup()
    }

    /**
     * Set draw circular view
     */
    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        setup()
    }

    /**
     * Set padding(Space) for circular view
     */
    override fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
        super.setPaddingRelative(start, top, end, bottom)
        setup()
    }

    /**
     * Change state of progress value animation. set it to 'false' if you don't want any animation
     *
     * @param state boolean state of progress animation. if set to false, no animation happen whenever value is changed
     */
    fun setProgressAnimationState(state: Boolean) {
        animationState = state
    }

    /**
     * change interpolator of animation to get more effect on animation
     *
     * @param interpolator animation interpolator
     */
    fun setProgressAnimatorInterpolator(interpolator: TimeInterpolator) {
        mValueAnimator!!.interpolator = interpolator
    }

    /**
     * Set border color for progress
     */
    fun setBorderProgressColor(@ColorInt borderColor: Int) {
        if (borderColor == mProgressColor) {
            return
        }

        mProgressColor = borderColor
        invalidate()
    }


    @Deprecated("Use {@link #setBorderColor(int)} instead")
    fun setBorderColorResource(@ColorRes borderColorRes: Int) {
        borderColor = ContextCompat.getColor(context,borderColorRes)
    }

    /**
     * Set a color to be drawn behind the circle-shaped drawable. Note that
     * this has no effect if the drawable is opaque or no drawable is set.
     *
     * @param fillColorRes The color resource to be resolved to a color and
     * drawn behind the drawable
     */
    @Deprecated("Fill color support is going to be removed in the future")
    fun setFillColorResource(@ColorRes fillColorRes: Int) {
        fillColor =ContextCompat.getColor(context,fillColorRes)
    }

    /**
     * Set bitmap image
     */
    override fun setImageBitmap(bm: Bitmap) {
        super.setImageBitmap(bm)
        initializeBitmap()
    }

    /**
     * Set image as drawable
     */
    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        initializeBitmap()
    }

    /**
     * Set image as resource
     */
    override fun setImageResource(@DrawableRes resId: Int) {
        super.setImageResource(resId)
        initializeBitmap()
    }

    /**
     * Set image as URI
     */
    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        initializeBitmap()
    }

    /**
     * Get color filter
     */
    override fun getColorFilter(): ColorFilter? {
        return mColorFilter
    }

    /**
     * Set color filter
     */
    override fun setColorFilter(cf: ColorFilter) {
        if (cf === mColorFilter) {
            return
        }

        mColorFilter = cf
        applyColorFilter()
        invalidate()
    }

    /**
     * Apply color filter
     */
    private fun applyColorFilter() {
        mBitmapPaint.colorFilter = mColorFilter
    }

    /**
     * Get bitmap from given drawable
     */
    private fun getBitmapFromDrawable(drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            return null
        }

        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        try {
            val bitmap: Bitmap

            if (drawable is ColorDrawable) {
                bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG)
            } else {
                bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, BITMAP_CONFIG)
            }

            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }

    /**
     * Initialize bitmap
     */
    private fun initializeBitmap() {
        if (isDisableCircularTransformation) {
            mBitmap = null
        } else {
            mBitmap = getBitmapFromDrawable(drawable)
        }
        setup()
    }

    /**
     * Setup the circular progress bar
     */
    private fun setup() {
        if (!mReady) {
            mSetupPending = true
            return
        }

        if (width == 0 && height == 0) {
            return
        }

        if (mBitmap == null) {
            invalidate()
            return
        }

        mBitmapShader = BitmapShader(mBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

        mBitmapPaint.isAntiAlias = true
        mBitmapPaint.shader = mBitmapShader

        mBorderPaint.style = Paint.Style.STROKE
        mBorderPaint.isAntiAlias = true
        mBorderPaint.color = mBorderColor
        mBorderPaint.strokeWidth = mBorderWidth.toFloat()
        mBorderPaint.strokeCap = Paint.Cap.ROUND

        mFillPaint.style = Paint.Style.FILL
        mFillPaint.isAntiAlias = true
        mFillPaint.color = mFillColor

        mBitmapHeight = mBitmap!!.height
        mBitmapWidth = mBitmap!!.width

        mBorderRect.set(calculateBounds())

        mDrawableRect.set(mBorderRect)
        if (!mBorderOverlay && mBorderWidth > 0) {
            mDrawableRect.inset(mBorderWidth.toFloat(), mBorderWidth.toFloat())
        }
        mDrawableRadius = Math.min(mDrawableRect.height() / 2, mDrawableRect.width() / 2)

        if (mInnrCircleDiammeter > 1) mInnrCircleDiammeter = 1f

        mDrawableRadius = mDrawableRadius * mInnrCircleDiammeter

        applyColorFilter()
        updateShaderMatrix()
        invalidate()
    }

    /**
     * Calculate bounds(size)
     */
    private fun calculateBounds(): RectF {
        val availableWidth = width - paddingStart - paddingEnd
        val availableHeight = height - paddingTop - paddingBottom

        val sideLength = Math.min(availableWidth, availableHeight)

        val left = paddingStart + (availableWidth - sideLength) / 2f
        val top = paddingTop + (availableHeight - sideLength) / 2f

        return RectF(left + borderWidth, top + borderWidth, left + sideLength - borderWidth, top + sideLength - borderWidth)
    }

    /**
     * Update scale values
     */
    private fun updateShaderMatrix() {
        val scale: Float
        var dx = 0f
        var dy = 0f

        mShaderMatrix.set(null)

        if (mBitmapWidth * mDrawableRect.height() > mDrawableRect.width() * mBitmapHeight) {
            scale = mDrawableRect.height() / mBitmapHeight.toFloat()
            dx = (mDrawableRect.width() - mBitmapWidth * scale) * 0.5f
        } else {
            scale = mDrawableRect.width() / mBitmapWidth.toFloat()
            dy = (mDrawableRect.height() - mBitmapHeight * scale) * 0.5f
        }

        mShaderMatrix.setScale(scale, scale)
        mShaderMatrix.postTranslate((dx + 0.5f).toInt() + mDrawableRect.left, (dy + 0.5f).toInt() + mDrawableRect.top)

        mBitmapShader!!.setLocalMatrix(mShaderMatrix)
    }

    companion object {

        private val SCALE_TYPE = ImageView.ScaleType.CENTER_CROP

        private val BITMAP_CONFIG = Bitmap.Config.ARGB_8888
        private val COLORDRAWABLE_DIMENSION = 2
        private val DEFAULT_ANIMATION_TIME = 10 * 1000
        private val DEFAULT_BORDER_WIDTH = 0
        private val DEFAULT_BORDER_COLOR = Color.BLACK
        private val DEFAULT_FILL_COLOR = Color.TRANSPARENT
        private val DEFAULT_PROGRESS_COLOR = Color.BLUE
        private val DEFAULT_BORDER_OVERLAY = false
        private val DEFAULT_DRAW_ANTI_CLOCKWISE = false
        private val DEFAULT_INNTER_DAIMMETER_FRACTION = 0.805f
    }


}