package com.android.aegentcam.helper

import android.view.View
import androidx.viewpager.widget.ViewPager

class DepthPageTransformer : ViewPager.PageTransformer {
    override fun transformPage(view: View, position: Float) {
        when {
            position < -1 -> view.alpha = 0f
            position <= 0 -> {
                view.alpha = 1f
                view.translationX = 0f
                view.scaleX = 1f
                view.scaleY = 1f
            }
            position <= 1 -> {
                view.alpha = 1 - position
                view.scaleX = 0.75f + (0.25f * (1 - Math.abs(position)))
                view.scaleY = 0.75f + (0.25f * (1 - Math.abs(position)))
            }
            else -> view.alpha = 0f
        }
    }
}
