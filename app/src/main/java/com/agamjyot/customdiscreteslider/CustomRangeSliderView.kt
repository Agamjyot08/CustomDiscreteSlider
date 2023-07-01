package com.agamjyot.customdiscreteslider

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.agamjyot.customdiscreteslider.R
import kotlin.math.max
import kotlin.math.min

class CustomRangeSliderView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val barDrawable = ContextCompat.getDrawable(context, R.drawable.rangebar_drawable)

    private val barHeight = 100f
    private val sliderRadius = 20f

    private var sliderCount: Int = 0
    private val sliderPositions: MutableList<Float> = mutableListOf()
    private val sliderValues: MutableList<Float> = mutableListOf()

    private val sliderWidth = 30f
    private val sliderHeight = 150f
    private val sliderPaint = Paint()
    private val sliderFillColor = Color.WHITE

    private var rangeStart: Float = 0f
    private var rangeEnd: Float = 100f

    private val touchSlop = 10
    private var activeSliderIndex = -1
    private var activeSliderOffset = 0f
    private var onSliderChangeListener: OnSliderChangeListener? = null

    interface OnSliderChangeListener {
        fun onSliderValueChanged(sliderIndex: Int, newValue: Float)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                for (i in 0 until sliderCount) {
                    val xPos = getSliderXPosition(i)
                    if (x >= xPos - sliderRadius && x <= xPos + sliderRadius) {
                        activeSliderIndex = i
                        activeSliderOffset = x - xPos
                        break
                    }
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (activeSliderIndex != -1) {
                    val newSliderPosition = min(max(x / width, 0f), 1f)
                    val leftSliderPosition = if (activeSliderIndex > 0) sliderPositions[activeSliderIndex - 1] else 0f
                    val rightSliderPosition =
                        if (activeSliderIndex < sliderCount - 1) sliderPositions[activeSliderIndex + 1] else 1f
                    if (newSliderPosition >= leftSliderPosition && newSliderPosition <= rightSliderPosition) {
                        sliderPositions[activeSliderIndex] = newSliderPosition
                        sliderValues[activeSliderIndex] = mapPositionToValue(newSliderPosition)
                        onSliderChangeListener?.onSliderValueChanged(activeSliderIndex, sliderValues[activeSliderIndex])
                        invalidate()
                    }
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activeSliderIndex = -1
                return true
            }
        }

        return super.onTouchEvent(event)
    }

    fun setSliderCount(count: Int, positions: List<Float>, values: List<Float>) {
        sliderCount = count
        sliderPositions.clear()
        sliderPositions.addAll(positions)
        sliderValues.clear()
        sliderValues.addAll(values)
        invalidate()
    }

    fun setRange(min: Float, max: Float) {
        rangeStart = min
        rangeEnd = max
        invalidate()
    }

    private fun getSliderXPosition(index: Int): Float {
        return sliderPositions[index] * width
    }

    fun mapPositionToValue(position: Float): Float {
        return rangeStart + (position * (rangeEnd - rangeStart))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val barTop = height / 2 - barHeight / 2
        val barBottom = height / 2 + barHeight / 2
        barDrawable?.setBounds(0, barTop.toInt(), width, barBottom.toInt())
        barDrawable?.draw(canvas)

        for (i in 0 until sliderCount) {
            val xPos = getSliderXPosition(i)
            val yPos = height / 2

            val left = (xPos - sliderWidth / 2).toInt()
            val top = (yPos - sliderHeight / 2).toInt()
            val right = (xPos + sliderWidth / 2).toInt()
            val bottom = (yPos + sliderHeight / 2).toInt()
            sliderPaint.color = sliderFillColor
            canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), sliderPaint)
        }
    }
    fun setOnSliderChangeListener(listener: OnSliderChangeListener) {
        onSliderChangeListener = listener
    }
}