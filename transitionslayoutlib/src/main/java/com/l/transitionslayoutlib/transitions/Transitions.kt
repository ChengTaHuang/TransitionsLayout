package com.l.transitionslayoutlib.transitions

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import com.l.transitionslayoutlib.TransitionUtil.convertDpToPixel
import com.l.transitionslayoutlib.ball.BouncingBall
import com.l.transitionslayoutlib.ball.BouncingBallEventListener
import com.l.transitionslayoutlib.balloon.BalloonView
import com.l.transitionslayoutlib.burstlayout.BurstLayout

/**
 * Created by zeno on 2018/7/17.
 */
class Transitions  : RelativeLayout, TransitionsInterface {

    private var ball: BouncingBall? = null
    private var burstLayout: BurstLayout? = null
    var burstHeight: Int = convertDpToPixel(100f, context).toInt()
    private var listener: TransitionsEventListener? = null
    private var showBalloonTimes: IntArray? = null
    private var balloonCount = 0
    private var ballY = 0f

    var lineDuration: Long = 500L
        set(value) {
            burstLayout?.lineDuration = value
        }
    var burstDuration: Long = 1_000L
        set(value) {
            burstLayout?.burstDuration = value
        }

    var burstAlphaDuration: Long = 1_500L
        set(value) {
            burstLayout?.burstAlphaDuration = value
        }

    var ballEventListener: BouncingBallEventListener? = null
        set(value) {
            ball?.listener = value
        }

    private var balloonView: BalloonView? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun createBall(width: Float, height: Float, location: PointF): Transitions {
        ball = BouncingBall(context)
        ball!!.visibility = View.GONE
        val newParams = RelativeLayout.LayoutParams(
                width.toInt(),
                height.toInt())
        addView(ball, newParams)
        ball?.setBall(PointF(x, y))

        ball?.listener = object : BouncingBallEventListener {
            override fun start() {
                listener?.startPlayBall()
            }

            override fun top(point: PointF, times: Int, bounce: BouncingBall.Bounce) {
                val halfLineWidth = burstLayout?.line!!.width / 2
                //Log.i("tag","top $times")
                showBalloonTimes?.run {
                    if (showBalloonTimes!!.indexOf(times) > -1) {
                        //Log.i("tag","top ${times}")
                    } else {
                        burstLayout?.expand(bounce.end.x - halfLineWidth, bounce.end.y + ball!!.height / 2)
                    }
                }
            }

            override fun collision(point: PointF, times: Int, bounce: BouncingBall.Bounce) {
                //Log.i("tag","top $times")
                val halfLineWidth = burstLayout?.line!!.width / 2
                showBalloonTimes?.run {
                    if (showBalloonTimes!!.indexOf(times) > -1) {
                        //Log.i("tag","collision ${times}")
                    } else {
                        burstLayout?.burst(
                                bounce.end.x.toInt() - ball!!.width / 2,
                                (bounce.end.y + burstLayout!!.line!!.height + ball!!.height / 2).toInt(),
                                burstLayout!!.line!!.width,
                                burstHeight)
                        burstLayout?.collapse(bounce.end.x - halfLineWidth, bounce.end.y + ball!!.height / 2)
                    }
                }

                showBalloonTimes?.filter {
                    times == it
                }?.forEach {
                    balloonView?.y = Math.abs(balloonView?.height!! / 2 - ball?.y!!)
                    balloonView?.start()
                }
            }

            override fun finish() {
                listener?.finishPlayBall()
            }

        }
        return this
    }

    override fun playBall() {
        stop()
        burstLayout?.clear()

        ball?.visibility = View.VISIBLE
        ball?.playBounce()
    }

    override fun setBallPaths(bounce: List<BouncingBall.Bounce>): Transitions {
        ball?.setPaths(bounce)
        return this
    }

    fun setTransitionsEventListener(listener: TransitionsEventListener): Transitions {
        this.listener = listener
        return this
    }

    fun createBurstLayout(width: Int, height: Int): Transitions {
        burstLayout = BurstLayout(context)
        val newParams = RelativeLayout.LayoutParams(
                width,
                height)
        addView(burstLayout, newParams)
        burstLayout!!.createLine(convertDpToPixel(30f, context), convertDpToPixel(2f, context))
        return this
    }

    fun setLine(width: Float, height: Float, color: Int) {
        burstLayout?.resizeLine(width, height, color)
    }

    fun createBalloon(): Transitions {
        balloonView = BalloonView(context)
        balloonView!!.listener = object : BalloonView.Listener {
            override fun playStart() {
            }

            override fun playing(process: Long) {
            }

            override fun playEnd() {
                balloonCount++
                if (balloonCount == showBalloonTimes?.size) {
                    balloonView?.stop()
                }
            }
        }
        val newBalloonViewParams = RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT)
        addView(balloonView, newBalloonViewParams)
        return this
    }

    fun showBalloon(vararg times: Int): Transitions {
        showBalloonTimes = times
        return this
    }

    private fun stop(){
        balloonView?.stop()
    }
}