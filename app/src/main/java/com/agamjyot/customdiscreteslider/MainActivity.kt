package com.agamjyot.customdiscreteslider

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var customRangeSliderView: CustomRangeSliderView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        customRangeSliderView = findViewById(R.id.customRangeSliderView)
        customRangeSliderView.setRange(0f, 100f)
        customRangeSliderView.setStepSize(5)

        val options = listOf(
            SliderOption("#017AFF", 30f),
            SliderOption("#33A34D", 30f),
            SliderOption("#283964", 20f),
            SliderOption("#ff0000", 20f)
        )

        customRangeSliderView.setSliderOptions(options)

        customRangeSliderView.setOnSliderChangeListener(object : CustomRangeSliderView.OnSliderChangeListener {
            override fun onSliderValueChanged(sliderOptions: List<SliderOption>) {
                Log.e("TAG", "onSliderValueChanged: ${sliderOptions.map { it.value }}")
            }
        })
    }
}