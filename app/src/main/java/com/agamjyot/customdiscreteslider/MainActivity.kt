package com.agamjyot.customdiscreteslider

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    private lateinit var customRangeSliderView: CustomRangeSliderView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        customRangeSliderView = findViewById(R.id.customRangeSliderView)
        customRangeSliderView.setRange(0f, 100f)

        val sliderPositions = listOf(0.2f, 0.4f, 0.6f)
        val sliderValues = sliderPositions.map { position ->
            customRangeSliderView.mapPositionToValue(position)
        }
        customRangeSliderView.setSliderCount(3, sliderPositions, sliderValues)

        customRangeSliderView.setOnSliderChangeListener(object : CustomRangeSliderView.OnSliderChangeListener {
            override fun onSliderValueChanged(sliderIndex: Int, newValue: Float) {
            }
        })
    }
}