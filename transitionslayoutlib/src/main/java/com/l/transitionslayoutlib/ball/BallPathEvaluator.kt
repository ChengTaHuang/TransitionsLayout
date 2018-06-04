package com.l.transitionslayoutlib.ball

import android.animation.TypeEvaluator
import android.graphics.PointF
import android.support.annotation.NonNull

/**
 * Created by zeno on 2018/6/4.
 */
class BallPathEvaluator(@NonNull private val vertex: PointF) : TypeEvaluator<BallPathPointF> {

    private var parabola : Parabola? = null
    private var avgOffset = 0f
    private var count = 0
    private var currentX = 0f
    private var point : PointF? = null

    override fun evaluate(v: Float, start: BallPathPointF, end: BallPathPointF): BallPathPointF {
        if(parabola == null){
            parabola = Parabola(start.pointF, end.pointF, vertex)
        }
        count++
        avgOffset = (avgOffset + v) / count
        currentX = start.pointF.x + v * (end.pointF.x - start.pointF.x)
        point = parabola!!.calculatePoint(currentX)
        return BallPathPointF(point!! , avgOffset)
    }

    class Parabola(start: PointF , end: PointF , vertex: PointF){
        private val p : Float by lazy {
            start.y/((start.x-vertex.x)*(start.x-end.x))
        }

        private val q : Float by lazy {
            vertex.y/((vertex.x-start.x)*(vertex.x-end.x))
        }

        private val r : Float by lazy {
            end.y/((end.x-start.x)*(end.x-vertex.x))
        }

        private val a : Float by lazy {
            p+q+r
        }

        private val b : Float by lazy {
            -p*(vertex.x+end.x)-q*(start.x+end.x)-r*(start.x+vertex.x)
        }

        private val c : Float by lazy {
            p*vertex.x*end.x+q*start.x*end.x+r*start.x*vertex.x
        }

        fun calculatePoint(curX : Float) : PointF{
            //y = ax2 + bx + c
            return PointF(curX , a*curX*curX + b*curX + c)
        }
    }
}

data class BallPathPointF(val pointF: PointF, val offset: Float = 0f)