package com.l.transitionslayoutlib.transitions

import android.graphics.PointF
import com.l.transitionslayoutlib.ball.BouncingBall

/**
 * Created by zeno on 2018/7/17.
 */
interface TransitionsInterface {
    fun createBall(width : Float , height : Float , location : PointF) : Transitions

    fun setBallPaths(bounce: List<BouncingBall.Bounce>) : Transitions

    fun playBall()

    //fun setBallEventListener(listener : BouncingBallEventListener) : Transitions
}