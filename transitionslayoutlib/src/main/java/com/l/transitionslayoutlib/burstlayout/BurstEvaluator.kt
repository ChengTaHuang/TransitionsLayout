package com.l.transitionslayoutlib.burstlayout

import android.animation.TypeEvaluator
import android.graphics.PointF

/**
 * Created by zeno on 2018/6/4.
 */
class BurstEvaluator : TypeEvaluator<PointF> {
    override fun evaluate(v: Float, start: PointF, end: PointF): PointF {
        val x = start.x + v * (end.x - start.x)
        val y = start.y + v * (end.y - start.y)

        return PointF(x, y)
    }
}