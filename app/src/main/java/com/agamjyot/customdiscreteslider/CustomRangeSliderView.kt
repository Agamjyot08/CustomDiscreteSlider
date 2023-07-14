package com.agamjyot.customdiscreteslider

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.max

class CustomRangeSliderView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val barHeight = 150f
    private var currentSlidingBaselineCode: Int = -1

    private val sliderOptions: MutableList<SliderOption> = mutableListOf()
    private val baseLineCodeList = mutableListOf<BaselineCode>()

    private val sliderPaint = Paint()
    private val mainSliderPaint by lazy {
        Paint().apply {
            color = Color.WHITE
        }
    }

    private val sliderWidth = 20f
    private val totalWidthWithoutSliders: Float
        get() = (width - (sliderWidth * (sliderOptions.size - 1)))

    private var minSliderValue = 0f
    private var maxSliderValue = totalWidthWithoutSliders

    private var rangeStart: Float = 0f
    private var rangeEnd: Float = 100f

    private var onSliderChangeListener: OnSliderChangeListener? = null
    private val rect = RectF()
    private val sliderRect = RectF()
    private var stepSize = 5

    interface OnSliderChangeListener {
        fun onSliderValueChanged(sliderOptions: List<SliderOption>)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                baseLineCodeList.firstOrNull {
                    x in it.start..it.end
                }?.let {
                    currentSlidingBaselineCode = baseLineCodeList.indexOf(it)
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                minSliderValue = if (currentSlidingBaselineCode > 0) {
                    baseLineCodeList[currentSlidingBaselineCode - 1].end
                } else {
                    sliderWidth
                }
                maxSliderValue = if (currentSlidingBaselineCode < baseLineCodeList.size - 1) {
                    baseLineCodeList[currentSlidingBaselineCode + 1].start
                } else {
                    totalWidthWithoutSliders
                }

                moveBaselineCode(x.coerceIn(minSliderValue, maxSliderValue), false)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                moveBaselineCode(x.coerceIn(minSliderValue, maxSliderValue), true)
                currentSlidingBaselineCode = -1
                return true
            }
        }

        return super.onTouchEvent(event)
    }

    private fun moveBaselineCode(x: Float, nearest: Boolean) {
        if (currentSlidingBaselineCode >= 0) {
            val sliderOption = sliderOptions[currentSlidingBaselineCode]
            val currPercentage =
                if (nearest) findNearest((x / totalWidthWithoutSliders) * 100) else
                    (x / totalWidthWithoutSliders) * 100

            var sumTillIndex = 0f
            for (i in 0..currentSlidingBaselineCode) {
                sumTillIndex += sliderOptions[i].value
            }
            val diff = (sumTillIndex - currPercentage)

            var effectiveDiff = 0f
            sliderOption.value =
                if (nearest) findNearest(sliderOption.value + (-1 * diff)) else sliderOption.value + (-1 * diff)
            Log.e("TAG", "and curr is now : ${sliderOption.value}")
            if (sliderOption.value < sliderOption.min) {
                Log.e("TAG", "LESS THAN MIN, min is : ${sliderOption.min}")
                Log.e("TAG", "effective diff is : ${sliderOption.min - sliderOption.value}")
                effectiveDiff = sliderOption.min - sliderOption.value
                sliderOption.value = sliderOption.min
            }
            sliderOptions.removeAt(currentSlidingBaselineCode)
            sliderOptions.add(currentSlidingBaselineCode, sliderOption)

            val nextOption = sliderOptions[currentSlidingBaselineCode + 1]
            nextOption.value =
                if (nearest) findNearest(nextOption.value + diff - effectiveDiff) else nextOption.value + diff - effectiveDiff
            Log.e("TAG", "and next value: ${nextOption.value}")
            sliderOptions.removeAt(currentSlidingBaselineCode + 1)
            sliderOptions.add(currentSlidingBaselineCode + 1, nextOption)

            invalidate()
            onSliderChangeListener?.onSliderValueChanged(sliderOptions)
        }
    }

    private fun findNearest(number: Float): Float {
        val remainder = number % stepSize
        val nearestMultiple = if (remainder < 3) {
            number - remainder
        } else {
            number + (stepSize - remainder)
        }
        return nearestMultiple
    }

    fun setSliderOptions(options: List<SliderOption>) {
        sliderOptions.clear()
        sliderOptions.addAll(
            options.map {
                it.copy(value = (it.value / rangeEnd) * 100)
            }
        )
        invalidate()
    }

    fun setStepSize(size: Int) {
        stepSize = size
        invalidate()
    }

    fun setRange(min: Float, max: Float) {
        rangeStart = min
        rangeEnd = max
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (sliderOptions.isEmpty())
            return

        var sliderLastPosition = 0F

        val barTop = height / 2 - barHeight / 2
        val barBottom = height / 2 + barHeight / 2
        baseLineCodeList.clear()
        sliderOptions.forEachIndexed { index, sliderOption ->
            val currentWidth =
                sliderLastPosition + ((sliderOption.value * totalWidthWithoutSliders) / 100)

            if (index == 0) {
                rect.set(sliderLastPosition, barTop, currentWidth, barBottom)
                sliderPaint.color = Color.parseColor(sliderOption.color)
                canvas.drawRoundRect(rect, 20f, 20f, sliderPaint)
            } else if (index == sliderOptions.size - 1) {
                sliderRect.set(sliderLastPosition, barTop, currentWidth, barBottom)
                sliderPaint.color = Color.parseColor(sliderOption.color)
                canvas.drawRoundRect(sliderRect, 20f, 20f, sliderPaint)
            } else {
                rect.set(sliderLastPosition, barTop, currentWidth, barBottom)
                sliderPaint.color = Color.parseColor(sliderOption.color)
                canvas.drawRect(rect, sliderPaint)
            }

            sliderLastPosition = currentWidth + sliderWidth

            if (index != sliderOptions.lastIndex) {
                val baseLineCode = ContextCompat.getDrawable(context, R.drawable.rounded_code)
                val baseLingWidth = 84
                val roundedCodeLeft = (currentWidth - (baseLingWidth / 2) + (sliderWidth / 2))
                val roundedCodeRight = (roundedCodeLeft + baseLingWidth)
                baseLineCode?.setBounds(
                    roundedCodeLeft.toInt(),
                    barBottom.toInt() - 10,
                    roundedCodeRight.toInt(),
                    (barBottom + baseLingWidth).toInt()
                )
                baseLineCodeList.add(BaselineCode(roundedCodeLeft, roundedCodeRight, index))
                baseLineCode?.draw(canvas)
            }
        }
    }

    fun setOnSliderChangeListener(listener: OnSliderChangeListener) {
        onSliderChangeListener = listener
    }
}

data class SliderOption(
    val color: String,
    var value: Float,
    val min: Float
)

data class BaselineCode(
    var start: Float,
    var end: Float,
    val index: Int
)