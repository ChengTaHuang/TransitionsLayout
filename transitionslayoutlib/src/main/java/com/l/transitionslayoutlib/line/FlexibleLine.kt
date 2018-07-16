package com.l.transitionslayoutlib.line

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator

/**
 * Created by zeno on 2018/6/4.
 */
class FlexibleLine : View , FlexibleLineInterface{

    var listener : StatusListener? = null
    var duration: Long = 500L

    private val expandObjectAnimator = ObjectAnimator.ofFloat(this, "scaleX",0f,1f)
    private val collapseObjectAnimator = ObjectAnimator.ofFloat(this, "scaleX",1f,0f)

    init {
        expandObjectAnimator.interpolator = AccelerateInterpolator()
        expandObjectAnimator.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {
                listener?.onExpandFinish()
            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationStart(p0: Animator?) {

            }

        })
        expandObjectAnimator.addUpdateListener {
            var value = it.animatedValue as Float
            value *= 100
            listener?.onExpand(value.toInt())
        }

        collapseObjectAnimator.interpolator = AccelerateInterpolator()
        collapseObjectAnimator.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {
                listener?.onCollapseFinish()
            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationStart(p0: Animator?) {

            }

        })

        collapseObjectAnimator.addUpdateListener {
            var value = it.animatedValue as Float
            value *= 100
            listener?.onCollapse(value.toInt())
        }

    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun expand() {
        this.scaleX = 0f
        expandObjectAnimator.duration = duration
        collapseObjectAnimator.cancel()
        expandObjectAnimator.start()
    }

    override fun collapse() {
        this.scaleX = 1f
        collapseObjectAnimator.duration = duration
        expandObjectAnimator.cancel()
        collapseObjectAnimator.start()
    }

    interface StatusListener{
        fun onExpandFinish()
        fun onExpand(process: Int)
        fun onCollapseFinish()
        fun onCollapse(process: Int)
    }
}