package com.l.transitionslayoutlib.burstlayout

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import com.l.transitionslayoutlib.R
import com.l.transitionslayoutlib.line.FlexibleLine
import java.util.*

/**
 * Created by zeno on 2018/6/4.
 */
class BurstLayout : RelativeLayout, BurstLayoutInterface {

    var drawables: MutableList<Drawable>
        set(value) {
            bitmaps = createBitmaps(value)
        }

    lateinit var bitmaps: MutableList<CustomBitmap>
    var line: FlexibleLine? = null
    private var animatorSet = AnimatorSet()
    private val imageViews = mutableListOf<ImageView>()
    private val animatorSets = mutableListOf<AnimatorSet>()
    var burstDuration = 1_000L
    var burstAlphaDuration = 1_500L

    constructor(context: Context) : super(context) {
        drawables = createDrawables()
        //bitmaps = createBitmaps(drawables)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        drawables = createDrawables()
        //bitmaps = createBitmaps(drawables)
    }

    private fun createDrawables(): MutableList<Drawable> {
        val drawables = mutableListOf<Drawable>()
        drawables.add(ContextCompat.getDrawable(context, R.drawable.solid_circle))
        drawables.add(ContextCompat.getDrawable(context, R.drawable.hollow_circle))
        drawables.add(ContextCompat.getDrawable(context, R.drawable.ic_cross))
        return drawables
    }

    private fun createBitmaps(drawables: MutableList<Drawable>): MutableList<CustomBitmap> {
        val customBitmaps = mutableListOf<CustomBitmap>()
        for (i in 0..11) {
            val index = Random().nextInt(drawables.size)
            val drawable = drawables[index]
            val size = Random().nextInt(7) + 8
            val bitmap = drawableToBitmap(drawable, size, size)
            val type: CustomBitmap.Type =
                    when (index) {
                        0 -> CustomBitmap.Type.STRETCH
                        1 -> CustomBitmap.Type.SHRINK
                        2 -> CustomBitmap.Type.ROTATE
                        else -> CustomBitmap.Type.NONE
                    }

            customBitmaps.add(CustomBitmap(bitmap, type))
        }
        return customBitmaps
    }

    private fun drawableToBitmap(drawable: Drawable, width: Int, height: Int): Bitmap {

        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)

        return bitmap
    }

    fun createLine(width: Float, height: Float) {
        if(line == null) {
            line = FlexibleLine(context)
            line!!.setBackgroundColor(ContextCompat.getColor(context, R.color.burst_color))

            val newParams = RelativeLayout.LayoutParams(
                    width.toInt(),
                    height.toInt())

            addView(line, newParams)
            line!!.scaleX = 0f
        }
    }

    fun setLineAnimationDuration(duration : Long){
        line?.duration = duration
    }

    fun expand(startX: Float, startY: Float) {
        line?.x = startX
        line?.y = startY
        line?.expand()
    }

    fun collapse(startX: Float, startY: Float) {
        line?.x = startX
        line?.y = startY
        line?.collapse()
    }

    override fun burst(startX: Int, startY: Int, width: Int, height: Int) {
        animatorSet = AnimatorSet()
        for ((index, customBitmap) in bitmaps.withIndex()) {
            val imageView = ImageView(context)
            imageView.setImageBitmap(customBitmap.bitmap)
            imageViews.add(imageView)
            addView(imageView)
            val anim = createAnimator(
                    imageView,
                    customBitmap.bitmap.width,
                    index,
                    bitmaps.size,
                    startX,
                    startY,
                    width,
                    height)

            val animator: Animator? =
                    when (customBitmap.type) {
                        CustomBitmap.Type.STRETCH -> createStretch(imageView)
                        CustomBitmap.Type.ROTATE -> createRotate(imageView)
                        CustomBitmap.Type.SHRINK -> createShrink(imageView)
                        CustomBitmap.Type.NONE -> null
                    }

            if (animator == null)
                animatorSet.play(anim)
            else
                animatorSet.playTogether(anim, animator)
        }
        animatorSet.start()
        animatorSets.add(animatorSet)
    }

    private fun createAnimator(target : View, bitmapWidth : Int , index : Int, size : Int,
                               startX: Int, startY: Int, width: Int, height: Int) : ValueAnimator {
        val evaluator = BurstEvaluator()

        val animator = ValueAnimator.ofObject(evaluator,
                PointF((Math.abs(width - bitmapWidth) * index / size).toFloat() + startX ,startY.toFloat()),
                PointF(Random().nextInt(Math.abs(width - bitmapWidth)).toFloat() + startX, Random().nextInt(height).toFloat() + startY))

        animator.duration = burstAlphaDuration
        animator.setTarget(target)

        when(Random().nextInt(3)){
            0 -> animator.interpolator = LinearInterpolator()
            1 -> animator.interpolator = AccelerateInterpolator()
            2 -> animator.interpolator = AccelerateDecelerateInterpolator()
            3 -> animator.interpolator = DecelerateInterpolator()
        }

        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as PointF
            target.x = animatedValue.x
            target.y = animatedValue.y
            target.alpha = 1 - valueAnimator.animatedFraction
        }

        return animator
    }

    private fun createShrink(target: View): AnimatorSet {
        val animators = AnimatorSet()
        val objectAnimator = ObjectAnimator.ofFloat(target, "scaleX", 1f, 0f)
        val objectAnimator2 = ObjectAnimator.ofFloat(target, "scaleY", 1f, 0f)
        animators.playTogether(objectAnimator, objectAnimator2)
        animators.duration = burstDuration

        return animators
    }

    private fun createStretch(target: View): AnimatorSet {
        val animators = AnimatorSet()
        val objectAnimator = ObjectAnimator.ofFloat(target, "scaleX", 1f, 0.5f)
        val objectAnimator2 = ObjectAnimator.ofFloat(target, "scaleY", 1f, 1.5f)
        animators.playTogether(objectAnimator, objectAnimator2)
        animators.duration = burstDuration
        return animators
    }

    private fun createRotate(target: View): ObjectAnimator {
        val objectAnimator = ObjectAnimator.ofFloat(target, "rotation", 0f, Random().nextFloat() * (90f - 0f))
        objectAnimator.duration = burstDuration

        return objectAnimator
    }

    fun clear(){
        for(animatorSet in animatorSets){
            animatorSet.removeAllListeners()
            animatorSet.end()
            animatorSet.cancel()
        }
        imageViews.clear()
    }

    class CustomBitmap(val bitmap: Bitmap, val type: Type) {
        enum class Type {
            STRETCH, ROTATE, SHRINK, NONE
        }
    }

}