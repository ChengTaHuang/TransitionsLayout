package com.l.transitionslayoutlib.ball

import android.graphics.PointF

/**
 * Created by zeno on 2018/6/4.
 */
interface BouncingBallEventListener {

    fun start()

    fun top(point : PointF, times : Int, bounce : BouncingBall.Bounce)

    fun collision(point : PointF, times : Int, bounce : BouncingBall.Bounce)

    fun finish()
}