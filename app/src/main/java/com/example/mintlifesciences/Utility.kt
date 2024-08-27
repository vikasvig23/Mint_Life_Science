package com.example.mintlifesciences

import android.graphics.drawable.GradientDrawable

object Utility {

    fun createGeadientDrawable(
        cornerRadius: Float,
        startColor:Int,
        endColor:Int
    ):GradientDrawable{

        val gd=GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(
                startColor,
                endColor
            )
        )
        gd.gradientType=GradientDrawable.LINEAR_GRADIENT
        gd.cornerRadius= cornerRadius.toFloat()
        return gd
    }
}