package com.example.sliderbarsample

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.core.content.ContextCompat

class SliderBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val thumbMin: View
    private val thumbMax: View
    private val thumbSize = resources.getDimensionPixelSize(R.dimen.slider_thumb_size)
    private val paint = Paint().also {
        it.isAntiAlias = true
        it.color = ContextCompat.getColor(context, android.R.color.holo_blue_dark)
        it.strokeWidth = resources.getDimension(R.dimen.slider_bar_height)
    }

    private val sliderBarNestedValue: SliderBarNestedValue

    private var dX = 0f
    private var lastX = 0f
    private var lastValue = 0
    private var listener: OnSliderChangedListener? = null

    init {
        inflate(context, R.layout.view_slider_bar, this)
        thumbMax = findViewById(R.id.thumb_max)
        thumbMin = findViewById(R.id.thumb_min)
        val resourceId = extractRangeAttribute(context, attrs)
        val ranges =
            if (resourceId == 0) createDefaultRange()
            else context.resources.getIntArray(resourceId)
        sliderBarNestedValue = SliderBarNestedValue(ranges)
        setupThumb()
    }

    private fun extractRangeAttribute(context: Context, attrs: AttributeSet?): Int {
        val a = context.obtainStyledAttributes(attrs, R.styleable.SliderBarView, 0, 0)
        val resourceId = a.getResourceId(R.styleable.SliderBarView_rangeArray, 0)
        a.recycle()
        return resourceId
    }

    private fun createDefaultRange() = (0..100).step(5).map { it }.toIntArray()

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        val y = height / 2F
        val (startX, endX) = getProgressRange()
        canvas?.drawLine(startX, y, endX, y, paint)
    }

    fun setOnSliderChangedListener(listener: OnSliderChangedListener) {
        this.listener = listener
    }

    private fun setupThumb() {
        thumbMax.setOnTouchListener(this::onTouch)
        thumbMin.setOnTouchListener(this::onTouch)
    }

    private fun onTouch(view: View, event: MotionEvent?): Boolean {
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                dX = view.x - event.rawX
                lastX = view.x
                val (min, max) = getValueRange()
                val (nestedMin, nestedMax) = sliderBarNestedValue.get(min, max)
                lastValue = if (isThumbMin(view)) nestedMin else nestedMax
                when {
                    thumbMin.x != thumbMax.x -> return true
                    dX > 0 -> thumbMax.bringToFront()
                    dX < 0 -> thumbMin.bringToFront()
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                view.x = validateX(view, event.rawX + dX)
                invalidate()
                val (min, max) = getValueRange()
                val (nestedMin, nestedMax) = sliderBarNestedValue.get(min, max)
                listener?.onChanged(nestedMin, nestedMax)
                return true
            }
            MotionEvent.ACTION_UP -> {
                val (min, max) = getValueRange()
                val (nestedMin, nestedMax) = sliderBarNestedValue.get(min, max)
                when {
                    nestedMin == nestedMax -> {
                        createAnimation(view, lastX).start()
                        if (isThumbMax(view)) listener?.onChanged(lastValue, nestedMax)
                        else listener?.onChanged(nestedMin, lastValue)
                        return true
                    }
                    isThumbMin(view) -> {
                        createAnimation(view, nestedMin * getMaxValue() / 100F).start()
                        return true
                    }
                    isThumbMax(view) -> {
                        createAnimation(view, nestedMax * getMaxValue() / 100F).start()
                        return true
                    }
                    else -> return true
                }
            }
            else -> return false
        }
    }

    private fun validateX(view: View, x: Float): Float {
        val maxValue = getMaxValue()
        return when {
            x <= 0 -> 0f
            x >= maxValue -> maxValue
            isThumbMin(view) && x >= thumbMax.x -> thumbMax.x
            isThumbMax(view) && x <= thumbMin.x -> thumbMin.x
            else -> x
        }
    }

    private fun getMaxValue() = (width - thumbSize).toFloat()

    private fun getProgressRange(): Pair<Float, Float> {
        val startX = thumbMin.x + thumbSize / 2F
        val endX = thumbMax.x + thumbSize / 2F
        return Pair(startX, endX)
    }

    private fun getValueRange(): Pair<Float, Float> {
        val maxValue = getMaxValue()
        val min = thumbMin.x * 100F / maxValue
        val max = thumbMax.x * 100F / maxValue
        return Pair(min, max)
    }

    private fun createAnimation(view: View, destinationValue: Float) =
        ValueAnimator.ofFloat(view.x, destinationValue).also { animator ->
            animator.duration = 10L
            animator.interpolator = LinearInterpolator()
            animator.addUpdateListener {
                view.x = it.animatedValue as Float
                invalidate()
            }
        }

    private fun isThumbMin(view: View) = view.id == R.id.thumb_min

    private fun isThumbMax(view: View) = view.id == R.id.thumb_max

    interface OnSliderChangedListener {
        fun onChanged(min: Int, max: Int)
    }
}
