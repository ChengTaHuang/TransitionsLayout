package com.l.transitionslayoutlib.ball

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.graphics.drawable.GradientDrawable
import android.support.annotation.ColorRes
import android.support.annotation.NonNull
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import com.l.transitionslayoutlib.R

/**
 * Created by zeno on 2018/6/4.
 */
class BouncingBall : View, BouncingBallInterface {

    var isScale = true
    var isFinishVisible = false
    private var animateStart = false

    @ColorRes var ballBackgroundColor: Int = R.color.default_main_color
        set(value) {
            setBallColor(value)
        }
    var listener: BouncingBallEventListener? = null
    private var paths = listOf<Bounce>()
    private val animatorSet = AnimatorSet()
    private val animatorList = mutableListOf<ValueAnimator>()

    companion object {
        fun createBouncePath(bounce: Bounce, times: Int): MutableList<Bounce> {
            val data = mutableListOf<Bounce>()
            data.add(bounce)
            for(i in 1 until times){
                val currentBounce = data[i - 1]
                val nextPoint = createNextPoint(currentBounce.start , currentBounce.end)
                val nextBounce = Bounce(currentBounce.end , nextPoint , currentBounce.height)
                data.add(nextBounce)
            }
            return data
        }

        private fun createNextPoint(start: PointF, end: PointF): PointF {
            return PointF(end.x + end.x - start.x, end.y)
        }
    }

    constructor(context: Context) : super(context) {
        setBallColor(ballBackgroundColor)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val typedArray = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.BouncingBall,
                0,
                0)

        try {
            ballBackgroundColor = typedArray.getInt(
                    R.styleable.BouncingBall_ballColor,
                    R.color.default_main_color)

            isScale = typedArray.getBoolean(R.styleable.BouncingBall_scale,
                    true)

            isFinishVisible = typedArray.getBoolean(R.styleable.BouncingBall_isFinishVisible ,
                    true)

        } finally {
            typedArray.recycle()
        }
    }

    override fun setPaths(paths: List<Bounce>) {
        this.paths = paths
    }

    override fun getPaths(): List<Bounce> = paths

    override fun playBounce() {
        animateStart = false
        clearAnimatorSet()
        for((index , path) in paths.withIndex()){
            val animate = createAnimate(path, index)
            animate.duration = path.duration
            animate.interpolator = LinearInterpolator()
            animatorList.add(animate)
        }
        animatorSet.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {
                visibility = if(isFinishVisible) VISIBLE else INVISIBLE
                listener?.finish()
                animateStart = false
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationStart(p0: Animator?) {
                visibility = VISIBLE
                listener?.start()
                animateStart = true
            }

        })

        animatorSet.playSequentially(animatorList as List<Animator>?)
        animatorSet.start()
    }

    private fun clearAnimatorSet(){
        for(animator in animatorList){
            animator.repeatCount = 0
            animator.end()
            animator.cancel()
            animator.removeAllListeners()
            animator.removeAllUpdateListeners()
        }
        animatorList.clear()
        animatorSet.removeAllListeners()
        animatorSet.end()
        animatorSet.cancel()
    }

    private var flag = false

    private fun createAnimate(bounce: Bounce, index: Int): ValueAnimator {
        val valueAnimator = ValueAnimator.ofObject(
                BallPathEvaluator(bounce.vertex),
                BallPathPointF(bounce.start),
                BallPathPointF(bounce.end))
        valueAnimator.addUpdateListener {
            val ballPathPoint = it.animatedValue as BallPathPointF
            val point = ballPathPoint.pointF
            val percent = valueAnimator.animatedFraction
            if(percent == 1.0f && flag && animateStart){
                listener?.collision(point, index+1, bounce)
                flag = false
            }else if(percent > 0.49 && !flag && animateStart){
                listener?.top(point, index+1, bounce)
                flag = true
            }

            this@BouncingBall.x = point.x - this@BouncingBall.width / 2
            this@BouncingBall.y = point.y - this@BouncingBall.height / 2

            val value = valueAnimator.animatedFraction
            if (isScale && value > 0.8) {
                this@BouncingBall.scaleY = Math.pow((Math.abs(0.9 - value) + 0.9f), 4.0).toFloat()
            }
        }
        return valueAnimator
    }

    fun setBall(pointF: PointF) {
        this.x = pointF.x
        this.y = pointF.y
    }

    private fun setBallColor(@ColorRes backgroundColor: Int) {
        val icon = ContextCompat.getDrawable(context, R.drawable.circle) as GradientDrawable
        icon.setColor(ContextCompat.getColor(context , backgroundColor))
        background = icon
    }

    data class Bounce(@NonNull val start: PointF,
                      @NonNull val end: PointF,
                      @NonNull val height: Float,
                      var vertex: PointF = PointF((start.x + end.x) / 2, start.y - height)) {
        var duration = 1000L
    }
}