package com.example.sliderbarsample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SliderBarView.OnSliderChangedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        slider_bar.setOnSliderChangedListener(this)
    }

    override fun onChanged(min: Int, max: Int) {
        text_min.text = min.toString()
        text_max.text = max.toString()
    }
}
