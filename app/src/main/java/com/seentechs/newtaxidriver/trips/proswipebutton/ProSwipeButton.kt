package com.seentechs.newtaxidriver.trips.proswipebutton

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.trips.proswipebutton.Constants.BTN_INIT_RADIUS
import com.seentechs.newtaxidriver.trips.proswipebutton.Constants.BTN_MORPHED_RADIUS
import com.seentechs.newtaxidriver.trips.proswipebutton.Constants.DEFAULT_SWIPE_DISTANCE
import com.seentechs.newtaxidriver.trips.proswipebutton.Constants.DEFAULT_TEXT_SIZE
import com.seentechs.newtaxidriver.trips.proswipebutton.Constants.MORPH_ANIM_DURATION
import com.seentechs.newtaxidriver.trips.proswipebutton.UiUtils.animateFadeHide
import com.seentechs.newtaxidriver.trips.proswipebutton.UiUtils.animateFadeShow
import com.seentechs.newtaxidriver.trips.proswipebutton.UiUtils.dpToPx

/**
 * Created by shadow-admin on 24/10/17.
 */

class ProSwipeButton : RelativeLayout {

    var contextl:Context
    private var view: View? = null
    private var gradientDrawable: GradientDrawable? = null
    private var contentContainer: RelativeLayout? = null
    private var contentTv: TextView? = null
    private var arrow1: ImageView? = null
    private var arrow2: ImageView? = null
    private var arrowHintContainer: LinearLayout? = null
    private var progressBar: ProgressBar? = null

    //// TODO: 26/10/17 Add touch blocking

    /*
        User configurable settings
     */
    private var btnText: CharSequence = "BUTTON"
    @ColorInt
    private var textColorInt: Int = 0
    @ColorInt
    private var bgColorInt: Int = 0
    @ColorInt
    var arrowColorRes: Int = 0
        private set
    var cornerRadius = BTN_INIT_RADIUS.toFloat()
    @Dimension
    private var textSize = DEFAULT_TEXT_SIZE
    private var swipeListener: OnSwipeListener? = null
    private var onAutoSwipeListener: OnAutoSwipeListener? = null
    /**
     * How much of the button must the user swipe to trigger the OnSwipeListener successfully
     *
     * @param swipeDistance float from 0.0 to 1.0 where 1.0 means user must swipe the button fully from end to end. Default is 0.85.
     */
    @get:Dimension
    var swipeDistance = DEFAULT_SWIPE_DISTANCE
        set(@Dimension swipeDistance) {
            var swipeDistances = swipeDistance
            if (swipeDistances > 1.0f) {
                swipeDistances = 1.0f
            }
            if (swipeDistances < 0.0f) {
                swipeDistances = 0.0f
            }
            field = swipeDistances
        }

    var text: CharSequence
        get() = this.btnText
        set(text) {
            this.btnText = text
            contentTv!!.text = text
        }

    var textColor: Int
        @ColorInt
        get() = this.textColorInt
        set(@ColorInt textColor) {
            this.textColorInt = textColor
            contentTv!!.setTextColor(textColor)
        }

    constructor(context: Context) : super(context) {
        this.contextl = context
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.contextl = context
        setAttrs(context, attrs)
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.contextl = context
        setAttrs(context, attrs)
        init()
    }

    private fun setAttrs(context: Context, attrs: AttributeSet) {
        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.ProSwipeButton,
                0, 0)
        try {
            val btnString = a.getString(R.styleable.ProSwipeButton_btn_text)
            if (btnString != null)
                btnText = btnString
            textColorInt = a.getColor(R.styleable.ProSwipeButton_text_color, ContextCompat.getColor(context, R.color.newtaxi_app_black))
            bgColorInt = a.getColor(R.styleable.ProSwipeButton_bg_color, ContextCompat.getColor(context, R.color.newtaxi_app_navy))
            arrowColorRes = a.getColor(R.styleable.ProSwipeButton_arrow_color, ContextCompat.getColor(context, R.color.proswipebtn_translucent_white))
            cornerRadius = a.getFloat(R.styleable.ProSwipeButton_btn_radius, BTN_INIT_RADIUS.toFloat())
            textSize = a.getDimensionPixelSize(R.styleable.ProSwipeButton_text_size, DEFAULT_TEXT_SIZE.toInt()).toFloat()
        } finally {
            a.recycle()
        }
    }

    fun init() {
        val inflater = LayoutInflater.from(context)
        view = inflater.inflate(R.layout.view_proswipebtn, this, true)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        contentContainer = view!!.findViewById(R.id.relativeLayout_swipeBtn_contentContainer)
        arrowHintContainer = view!!.findViewById(R.id.linearLayout_swipeBtn_hintContainer)
        contentTv = view!!.findViewById(R.id.tv_btnText)
        arrow1 = view!!.findViewById(R.id.iv_arrow1)
        arrow2 = view!!.findViewById(R.id.iv_arrow2)

        rotateArrow()
        tintArrowHint()
        contentTv!!.text = btnText
        contentTv!!.setTextColor(textColorInt)
        contentTv!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        gradientDrawable = GradientDrawable()
        gradientDrawable!!.shape = GradientDrawable.RECTANGLE
        gradientDrawable!!.cornerRadius = cornerRadius
        setBackgroundColor(bgColorInt)
        updateBackground()
        setupTouchListener()
    }

    private fun rotateArrow() {

        val layoutDirection = resources.getString(R.string.layout_direction)
        if (layoutDirection == "0") {
            arrow1!!.scaleX = 1f
            arrow1!!.scaleY = 1f
            arrow2!!.scaleX = 1f
            arrow2!!.scaleY = 1f
        } else {
            arrow1!!.scaleX = -1f
            arrow1!!.scaleY = -1f
            arrow2!!.scaleX = -1f
            arrow2!!.scaleY = -1f
        }

    }

    private fun setupTouchListener() {
        setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                            swipeListener?.onButtonTouched()

                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        // Movement logic here

                        println("event getx One : " + event.x) // ?
                        println("arrowHintContainer.getWidth() One" + arrowHintContainer!!.width) // width of the arrow
                        println("getWidth() One$width") // total button width
                        println("arrowHintContainer.getX() One" + arrowHintContainer!!.x) // position of the arrow

                        var layoutDirection = resources.getString(R.string.layout_direction)
                        if (layoutDirection == "0") {

                            if (event.x > arrowHintContainer!!.width / 2 &&
                                    event.x + arrowHintContainer!!.width / 2 < width &&
                                    (event.x < arrowHintContainer!!.x + arrowHintContainer!!.width || arrowHintContainer!!.x != 0f)) {
                                // snaps the hint to user touch, only if the touch is within hint width or if it has already been displaced
                                arrowHintContainer!!.x = event.x - arrowHintContainer!!.width / 2
                            }

                            if (arrowHintContainer!!.x + arrowHintContainer!!.width > width && arrowHintContainer!!.x + arrowHintContainer!!.width / 2 < width) {
                                // allows the hint to go up to a max of btn container width
                                arrowHintContainer!!.x = (width - arrowHintContainer!!.width).toFloat()
                            }

                            if (event.x < arrowHintContainer!!.width / 2 && arrowHintContainer!!.x > 0) {
                                // allows the hint to go up to a min of btn container starting
                                arrowHintContainer!!.x = 0f
                            }


                        } else {

                            if (event.x > arrowHintContainer!!.width / 2 &&
                                    event.x + arrowHintContainer!!.width / 2 < width &&
                                    (event.x < arrowHintContainer!!.x + arrowHintContainer!!.width || arrowHintContainer!!.x != 0f)) {
                                // snaps the hint to user touch, only if the touch is within hint width or if it has already been displaced

                                arrowHintContainer!!.x = event.x - arrowHintContainer!!.width / 2
                            }


                        }






                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        println("event getx Two : " + event.x)
                        println("arrowHintContainer.getWidth() Two" + arrowHintContainer!!.width) // width of the arrow
                        println("getWidth() Two$width") // total button width
                        println("arrowHintContainer.getX() Two" + arrowHintContainer!!.x) // position of the arrow

                        swipeListener?.onButtonReleased()

                        var layoutDirection = resources.getString(R.string.layout_direction)
                        if (layoutDirection == "0") {


                            //Release logic here
                            if (arrowHintContainer!!.x + arrowHintContainer!!.width > width * swipeDistance) {
                                // swipe completed, fly the hint away!
                                performSuccessfulSwipe()
                            } else if (arrowHintContainer!!.x <= 0) {
                                // upon click without swipe
                                startFwdAnim()
                            } else {
                                // swipe not completed, pull back the hint
                                animateHintBack()
                            }


                        } else {


                            //Release logic here

                            //if (arrowHintContainer.getX() + arrowHintContainer.getWidth() > getWidth() * swipeDistance) {
                            if (arrowHintContainer!!.x - arrowHintContainer!!.width < 0) {
                                // swipe completed, fly the hint away!
                                performSuccessfulSwipe()
                                //}else if (getWidth()-arrowHintContainer.getWidth() >arrowHintContainer.getX()) {
                            } else if (arrowHintContainer!!.x <= 0) {
                                // upon click without swipe
                                startFwdAnim()
                            } else {
                                // swipe not completed, pull back the hint
                                animateHintBack()
                            }


                        }




                        return true
                    }
                }

                return false
            }
        })
    }

    private fun performSuccessfulSwipe() {
        if (swipeListener != null)
            swipeListener!!.onSwipeConfirm()
        morphToCircle()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        startFwdAnim()
    }

    private fun animateHintBack() {
        val positionAnimator = ValueAnimator.ofFloat(arrowHintContainer!!.x, 0.toFloat())
        positionAnimator.interpolator = AccelerateDecelerateInterpolator()
        positionAnimator.addUpdateListener {
            val layoutDirection = resources.getString(R.string.layout_direction)
            if (layoutDirection == "0") {
                val x = positionAnimator.animatedValue as Float
                arrowHintContainer!!.x = x
            } else {
                arrowHintContainer!!.x = (width - arrowHintContainer!!.width).toFloat()
            }
        }

        positionAnimator.duration = 200
        positionAnimator.start()
    }

    private fun startFwdAnim() {
        if (isEnabled) {
            val animation = TranslateAnimation(0f, measuredWidth.toFloat(), 0f, 0f)
            animation.interpolator = AccelerateDecelerateInterpolator()
            animation.duration = 1000
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    startHintInitAnim()
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
            arrowHintContainer!!.startAnimation(animation)
        }
    }

    /**
     * animate entry of hint from the left-most edge
     */
    private fun startHintInitAnim() {
        val anim = TranslateAnimation((-arrowHintContainer!!.width).toFloat(), 0f, 0f, 0f)
        anim.duration = 500
        arrowHintContainer!!.startAnimation(anim)
    }


    @SuppressLint("ObjectAnimatorBinding")
    fun morphToCircle() {
        animateFadeHide(contextl, arrowHintContainer)
        //contextl?.let { animateFadeHide(it, arrowHintContainer) }
        setOnTouchListener(null)
        val cornerAnimation = ObjectAnimator.ofFloat(gradientDrawable, "cornerRadius", BTN_INIT_RADIUS, BTN_MORPHED_RADIUS)

        animateFadeHide(contextl, contentTv)
        // contextl?.let { animateFadeHide(it, contentTv) }
        val widthAnimation: ValueAnimator
        widthAnimation = ValueAnimator.ofInt(width, dpToPx(50))
        widthAnimation.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams = contentContainer!!.layoutParams
            layoutParams.width = `val`
            contentContainer!!.layoutParams = layoutParams
        }
        val heightAnimation = ValueAnimator.ofInt(height, dpToPx(50))
        heightAnimation.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams = contentContainer!!.layoutParams
            layoutParams.height = `val`
            contentContainer!!.layoutParams = layoutParams
        }

        val animatorSet = AnimatorSet()
        animatorSet.duration = MORPH_ANIM_DURATION.toLong()
        animatorSet.playTogether(cornerAnimation, widthAnimation, heightAnimation)
        animatorSet.start()

        showProgressBar()
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun morphToRect() {
        setupTouchListener()
        val cornerAnimation = ObjectAnimator.ofFloat(gradientDrawable, "cornerRadius", BTN_MORPHED_RADIUS, BTN_INIT_RADIUS)

        progressBar!!.visibility = View.GONE
        val widthAnimation: ValueAnimator
        widthAnimation = ValueAnimator.ofInt(dpToPx(50), width)
        widthAnimation.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams = contentContainer!!.layoutParams
            layoutParams.width = `val`
            contentContainer!!.layoutParams = layoutParams
        }
        val heightAnimation = ValueAnimator.ofInt(dpToPx(50), width)
        heightAnimation.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams = contentContainer!!.layoutParams
            layoutParams.height = `val`
            contentContainer!!.layoutParams = layoutParams
        }

        val animatorSet = AnimatorSet()
        animatorSet.duration = MORPH_ANIM_DURATION.toLong()
        animatorSet.playTogether(cornerAnimation, widthAnimation, heightAnimation)
        animatorSet.start()
    }

    fun updateBackground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            contentContainer!!.background = gradientDrawable
        } else {

            contentContainer!!.background = (gradientDrawable)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (!enabled) {
            gradientDrawable!!.setColor(ContextCompat.getColor(contextl, R.color.proswipebtn_disabled_grey))
            updateBackground()
            this.alpha = 0.5f
        } else {
            setBackgroundColor(getBackgroundColor())
            this.alpha = 1f
        }
    }

    private fun showProgressBar() {
        progressBar = ProgressBar(contextl)
        progressBar!!.indeterminateDrawable.setColorFilter(ContextCompat.getColor(contextl, android.R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
        animateFadeHide(contextl, contentTv)
        contentContainer!!.addView(progressBar)
    }

    fun hideProgressBar() {
        progressBar!!.visibility = View.GONE
    }

    @JvmOverloads
    fun showResultIcon(isSuccess: Boolean, shouldReset: Boolean = !isSuccess) {
        //contextl?.let { animateFadeHide(it, progressBar) }
        animateFadeHide(contextl, progressBar)

        val failureIcon = AppCompatImageView(contextl)
        val icLayoutParams = RelativeLayout.LayoutParams(dpToPx(50), dpToPx(50))
        failureIcon.layoutParams = icLayoutParams
        failureIcon.visibility = View.GONE
        /*int icon;
        if (isSuccess)
            icon = R.drawable.ic_check_circle_36dp;
        else
            icon = R.drawable.ic_cancel_full_24dp;
        failureIcon.setImageResource(icon);
        contentContainer.addView(failureIcon);*/
        animateFadeShow(contextl, failureIcon)

        if (shouldReset) {
            // expand the btn again
            Handler().postDelayed({
                val layoutDirection = resources.getString(R.string.layout_direction)
                if (layoutDirection == "0") {
                    animateFadeHide(contextl, failureIcon)
                    morphToRect()
                    arrowHintContainer!!.x = 0f
                    animateFadeShow(contextl, arrowHintContainer!!)
                    contentTv?.let { animateFadeShow(contextl, it) }
                } else {
                    animateFadeHide(contextl, failureIcon)
                    morphToRect()
                    arrowHintContainer!!.x = (width - arrowHintContainer!!.width).toFloat()
                    animateFadeShow(contextl, arrowHintContainer!!)
                    contentTv?.let { animateFadeShow(contextl, it) }
                }
            }, 1000)
        }
    }

    private fun tintArrowHint() {
        arrow1!!.setColorFilter(arrowColorRes, PorterDuff.Mode.MULTIPLY)
        arrow2!!.setColorFilter(arrowColorRes, PorterDuff.Mode.MULTIPLY)
    }

    interface OnSwipeListener {
        fun onSwipeConfirm()


        fun onButtonTouched()

        fun onButtonReleased()
    }

    interface OnAutoSwipeListener {
        fun onAutoSwipeConfirm(tripStatus: String)
    }

    override fun setBackgroundColor(@ColorInt bgColor: Int) {
        this.bgColorInt = bgColor
        gradientDrawable!!.setColor(bgColor)
        updateBackground()
    }

    @ColorInt
    fun getBackgroundColor(): Int {
        return this.bgColorInt
    }

    /**
     * Include alpha in arrowColor for transparency (ex: #33FFFFFF)
     */
    fun setArrowColor(arrowColor: Int) {
        this.arrowColorRes = arrowColor
        tintArrowHint()
    }

    fun setTextSize(@Dimension textSize: Float) {
        this.textSize = textSize
        contentTv!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
    }

    @Dimension
    fun getTextSize(): Float {
        return this.textSize
    }

    fun setOnSwipeListener(customSwipeListener: OnSwipeListener?) {
        this.swipeListener = customSwipeListener
    }


    fun AutoSwipe(onAutoSwipeListener: OnAutoSwipeListener?, tripStatus: String) {
        this.onAutoSwipeListener = onAutoSwipeListener

        if (onAutoSwipeListener != null) {
            onAutoSwipeListener.onAutoSwipeConfirm(tripStatus)

           /* if (tripStatus.equals(CommonKeys.TripDriverStatus.BeginTrip, ignoreCase = true)) {
                val failureIcon = AppCompatImageView(contextl)
                val icLayoutParams = RelativeLayout.LayoutParams(dpToPx(50), dpToPx(50))
                failureIcon.layoutParams = icLayoutParams
                 animateFadeShow(contextl, failureIcon)
            } else {
                morphToCircle()
            }*/
            morphToCircle()
        }
    }

    companion object {
        var TripStatus = ""
    }

}
