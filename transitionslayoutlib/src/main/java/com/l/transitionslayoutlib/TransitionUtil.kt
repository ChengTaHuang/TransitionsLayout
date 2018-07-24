package com.l.transitionslayoutlib

import android.content.Context

/**
 * Created by zeno on 2018/7/17.
 */
object TransitionUtil {

    fun convertDpToPixel(dp: Float, context: Context): Float {
        return dp * getDensity(context)
    }

    fun convertPixelToDp(px: Float, context: Context): Float {
        return px / getDensity(context)
    }

    private fun getDensity(context: Context): Float {
        val metrics = context.resources.displayMetrics
        return metrics.density
    }
}