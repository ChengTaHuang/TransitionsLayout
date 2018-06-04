package com.l.transitionslayoutlib.ball

/**
 * Created by zeno on 2018/6/4.
 */
interface BouncingBallInterface {

    fun setPaths(paths : MutableList<BouncingBall.Bounce>)

    fun getPaths() : List<BouncingBall.Bounce>

    fun playBounce()
}