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
    private val sliderWidth = 30f
    private val totalWidthWithoutSliders: Float
        get() = (width - (sliderWidth * (sliderOptions.size - 1)))

    private var rangeStart: Float = 0f
    private var rangeEnd: Float = 100f

    private var onSliderChangeListener: OnSliderChangeListener? = null
    private val rect = RectF()
    private val sliderRect = RectF()
    private var stepSize = 10

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
                moveBaselineCode(x, false)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                moveBaselineCode(x, true)
                currentSlidingBaselineCode = -1
                return true
            }
        }

        return super.onTouchEvent(event)
    }

    private fun moveBaselineCode(x: Float, nearest: Boolean) {
        val baselineCodeMutableList = baseLineCodeList
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

            Log.e("TAG", "currentSlidingBaselineCode: $currentSlidingBaselineCode")

            val minSliderValue = if (currentSlidingBaselineCode > 0) {
                baseLineCodeList[currentSlidingBaselineCode - 1].end
            } else {
                0f
            }
            val maxSliderValue = if (currentSlidingBaselineCode < baseLineCodeList.size - 1) {
                baseLineCodeList[currentSlidingBaselineCode + 1].start
            } else {
                width.toFloat()
            }

            Log.e("TAG", "current value: ${baseLineCodeList[currentSlidingBaselineCode]}")
            Log.e("TAG", "minSliderValue: $minSliderValue")
            Log.e("TAG", "maxSliderValue: $maxSliderValue")

            sliderOption.value =
                if (nearest) findNearest(sliderOption.value + (-1 * diff)) else sliderOption.value + (-1 * diff)
            sliderOptions.removeAt(currentSlidingBaselineCode)
            sliderOptions.add(currentSlidingBaselineCode, sliderOption)

            val nextOption = sliderOptions[currentSlidingBaselineCode + 1]
            nextOption.value =
                if (nearest) findNearest(nextOption.value + diff) else nextOption.value + diff
            sliderOptions.removeAt(currentSlidingBaselineCode + 1)
            sliderOptions.add(currentSlidingBaselineCode + 1, nextOption)

            invalidate()
            onSliderChangeListener?.onSliderValueChanged(sliderOptions)
        }
    }

    private fun setInRange(min: Float, max: Float, value: Float): Float {
        var finalValue = value
        if (value > max) {
            finalValue = max
        }
        if (value < min) {
            finalValue = max
        }
        return finalValue
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
            rect.set(sliderLastPosition, barTop, currentWidth, barBottom)
            sliderPaint.color = Color.parseColor(sliderOption.color)
            canvas.drawRect(rect, sliderPaint)

            sliderLastPosition = currentWidth + sliderWidth
            sliderRect.set(currentWidth, barTop, sliderLastPosition, barBottom)
            canvas.drawRect(sliderRect, mainSliderPaint)

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
    var value: Float
)

data class BaselineCode(
    var start: Float,
    var end: Float,
    val index: Int
)