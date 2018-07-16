package com.l.transitionslayoutlib.balloon

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import com.l.transitionslayoutlib.R
import com.l.transitionslayoutlib.burstlayout.BurstEvaluator
import java.io.InputStream
import java.util.*

/**
 * Created by zeno on 2018/7/16.
 */
class BalloonView  : View {
    private var mGifInputStream: InputStream? = null
    private lateinit var mGifMovie: Movie
    private lateinit var mKeepGifMovie: Movie
    private var mMeasuredMovieWidth = 0
    private var mMeasuredMovieHeight = 0
    private var movieDuration: Int = 0
    private var mMovieStart: Long = 0
    private var mScale: Float = 0f
    private val mPaint = Paint()
    private var drawPoint = PointF(0f, 0f)

    private var drawing = false
    private var keep = false
    var isShowKeep = false

    var listener: Listener? = null
    var color: Int = Color.RED
        set(value) {
            mPaint.colorFilter = PorterDuffColorFilter(value, PorterDuff.Mode.SRC_IN)
        }

    private val ringPaint = createPaint(Color.RED, 10f, Paint.Style.STROKE)
    private val rings = mutableListOf<Ring>()
    private var animateSet = AnimatorSet()
    private var drawables = mutableListOf<Drawable>()
    var bitmaps = mutableListOf<CustomBitmap>()

    private val linePaint = createPaint(Color.BLACK, 2f, Paint.Style.STROKE)
    private val lines = mutableListOf<Line>()

    private var originalWidth: Int = 0
    private var originalHeight: Int = 0

    constructor(context: Context) : super(context) {
        init(context)
        color = Color.RED
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.BalloonView,
                0, 0)

        try {
            color = a.getInt(R.styleable.BalloonView_balloonColor, Color.RED)

        } finally {
            a.recycle()
        }

        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.BalloonView,
                0, 0)

        try {
            color = a.getInt(R.styleable.BalloonView_balloonColor, Color.RED)

        } finally {
            a.recycle()
        }

        init(context)
    }

    private fun init(context: Context) {
        isFocusable = true
        mGifInputStream = context.resources.openRawResource(R.raw.balloon)
        val mGifInputStream2 = context.resources.openRawResource(R.raw.balloon)
        mGifMovie = Movie.decodeStream(mGifInputStream)
        mKeepGifMovie = Movie.decodeStream(mGifInputStream2)
        movieDuration = mGifMovie.duration()
        drawables = createDrawables()
        bitmaps = createBitmaps(drawables)
    }

    private fun addBitmapAnimate() {
        for ((index, customBitmap) in bitmaps.withIndex()) {
            val anim = createAnimator(customBitmap, customBitmap.defaultAngle.toInt())

            val animator: Animator? =
                    when (customBitmap.type) {
                        CustomBitmap.Type.STRETCH -> createStretch(customBitmap)
                        CustomBitmap.Type.ROTATE -> createRotate(customBitmap)
                        CustomBitmap.Type.SHRINK -> createShrink(customBitmap)
                        CustomBitmap.Type.NONE -> null
                    }

            animateSet.playTogether(anim , animator)
        }
    }

    private fun createPaint(color: Int, stroke: Float, style: Paint.Style = Paint.Style.STROKE): Paint {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.style = style
        paint.color = color
        paint.strokeWidth = stroke
        return paint
    }

    private fun createDrawables(): MutableList<Drawable> {
        val datas = mutableListOf<Drawable>()
        datas.add(ContextCompat.getDrawable(context, R.drawable.solid_circle))
        datas.add(ContextCompat.getDrawable(context, R.drawable.hollow_circle))
        datas.add(ContextCompat.getDrawable(context, R.drawable.ic_cross_b))

        return datas
    }

    private fun createBitmaps(drawables: MutableList<Drawable>): MutableList<CustomBitmap> {
        val datas = mutableListOf<CustomBitmap>()
        for ((index, drawable) in drawables.withIndex()) {
            val count = Random().nextInt(5) + 5
            val angles = getRandomAngle(count)
            for (i in 0 until count) {
                val size = Random().nextInt(7) + 8
                val bitmap = drawableToBitmap(drawable, size, size)
                val type: CustomBitmap.Type =
                        when (index) {
                            0 -> CustomBitmap.Type.STRETCH
                            1 -> CustomBitmap.Type.SHRINK
                            2 -> CustomBitmap.Type.ROTATE
                            else -> CustomBitmap.Type.NONE
                        }

                datas.add(CustomBitmap(bitmap, size, type, Point(0, 0), angles[i].toFloat()))
            }
        }
        return datas
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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val movieWidth = mGifMovie.width()
        val movieHeight = mGifMovie.height()

        var scaleH = 1f
        val measureModeWidth = View.MeasureSpec.getMode(widthMeasureSpec)

        if (measureModeWidth != View.MeasureSpec.UNSPECIFIED) {
            val maximumWidth = View.MeasureSpec.getSize(widthMeasureSpec)
            originalWidth = maximumWidth
            if (movieWidth > maximumWidth) {
                scaleH = movieWidth.toFloat() / maximumWidth.toFloat()
            }
        }

        var scaleW = 1f
        val measureModeHeight = View.MeasureSpec.getMode(heightMeasureSpec)

        if (measureModeHeight != View.MeasureSpec.UNSPECIFIED) {
            val maximumHeight = View.MeasureSpec.getSize(heightMeasureSpec)
            originalHeight = maximumHeight
            if (movieHeight > maximumHeight) {
                scaleW = movieHeight.toFloat() / maximumHeight.toFloat()
            }
        }

        mScale = 1f / Math.max(scaleH, scaleW)
        mMeasuredMovieWidth = (movieWidth * mScale).toInt()
        mMeasuredMovieHeight = (movieHeight * mScale).toInt()
        //setMeasuredDimension(mMeasuredMovieWidth, mMeasuredMovieHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        drawPoint.x = ((width - mMeasuredMovieWidth) / 2).toFloat()
        drawPoint.y = ((height - mMeasuredMovieHeight) / 2).toFloat()
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (hasWindowFocus)
            initAnimator()
    }

    private fun initAnimator() {
        animateSet = AnimatorSet()
        animateSet.startDelay = 300L
        animateSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                for (obj in bitmaps) {
                    obj.point = Point(0, 0)
                }
                //Log.i("onAnimationStart", "onAnimationEnd")
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationStart(p0: Animator?) {
                //Log.i("onAnimationStart", "onAnimationStart")
            }

        })
        initRings()
        addRingsAnimate()

        addBitmapAnimate()

        initLines()
        addLinesAnimate()
    }

    fun start() {
        drawing = true
        val now = android.os.SystemClock.uptimeMillis()
        mMovieStart = now

        if (!animateSet.isStarted) {
            animateSet.start()
        }

        listener?.playStart()

        invalidate()
    }

    private fun initRings() {
        rings.clear()
        val center = PointF((width / 2).toFloat(), (height / 2).toFloat())

        rings.add(Ring(center,
                50f, 120f, 0f,
                15f, 0f, 15f,
                600L))

        rings.add(Ring(center,
                80f, 200f, 10f,
                10f, 0f, 10f,
                800L))
    }

    private fun addRingsAnimate() {
        animateSet.playTogether(createRingAnim(rings[0]))
        animateSet.playTogether(createRingAnim(rings[1]))
    }

    private fun initLines() {
        lines.clear()
        val angles = getRandomAngle(5)
        for (angle in angles) {
            val radius = Math.min(width, height) / 8
            lines.add(Line(
                    getCirclePoint(PointF(width / 2f, height / 2f), radius.toFloat(), angle),
                    getCirclePoint(PointF(width / 2f, height / 2f), (radius * 3).toFloat(), angle)
            ))
        }
    }

    private fun addLinesAnimate() {
        for (line in lines) {
            animateSet.playTogether(createLineAnim(line))
        }
    }

    fun stop() {
        drawing = false
        keep = false
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {

        if (drawing) {
            val currentTime = getCurrentFrameTime()

            mGifMovie.setTime(currentTime.toInt())
            canvas.save()
            canvas.scale(mScale, mScale)
            mGifMovie.draw(canvas, drawPoint.x / mScale, drawPoint.y / mScale, mPaint)
            canvas.restore()

            if (rings.size > 0) {
                rings.filter { it.startRadius < it.curRadius }.map {
                    ringPaint.alpha = it.alpha
                    ringPaint.strokeWidth = it.curStoke
                    canvas.drawCircle(it.center.x, it.center.y, it.curRadius, ringPaint)
                }
            }

            for (obj in bitmaps) {
                //Log.i("points", "${obj.point.x} , ${obj.point.y}")
                if (obj.point.x > 0 && obj.point.y > 0) {
                    val matrix = Matrix()
                    matrix.setTranslate(-obj.size / 2f, -obj.size / 2f)
                    matrix.postRotate(obj.angle)
                    matrix.postScale(obj.scaleX, obj.scaleY)
                    matrix.postTranslate(obj.point.x.toFloat() + obj.size / 2f, obj.point.y.toFloat() + obj.size / 2f)
                    val p = Paint()
                    p.alpha = obj.alpha
                    canvas.drawBitmap(obj.bitmap, matrix, p)
                }
            }

            for (line in lines) {
                canvas.drawLine(line.headPoint.x, line.headPoint.y, line.tailPoint.x, line.tailPoint.y, linePaint)
            }

            invalidate()
        }

        if (keep && isShowKeep) {
            mKeepGifMovie.setTime(mKeepGifMovie.duration())
            canvas.save()
            canvas.scale(mScale, mScale)
            mKeepGifMovie.draw(canvas, drawPoint.x / mScale, drawPoint.y / mScale, mPaint)
            canvas.restore()

            invalidate()
        }

        //if (drawing || isShowKeep)
        //    invalidate()
    }

    private fun getCurrentFrameTime(): Long {
        val now = SystemClock.uptimeMillis()
        val nowCount = ((now - mMovieStart) / movieDuration)
        if (nowCount >= 1) {
            keep = true
            drawing = false
            listener?.playEnd()
        }
        val currentTime = ((now - mMovieStart) % movieDuration)
        val percent = currentTime * 100 / movieDuration
        if (drawing) listener?.playing(percent)
        return ((now - mMovieStart) % movieDuration)
    }

    interface Listener {
        fun playStart()
        fun playing(process: Long)
        fun playEnd()
    }

    private fun createRingAnim(ring: Ring): ObjectAnimator {
        val objectAnimator = ObjectAnimator.ofFloat(ring, "scaleX", ring.startRadius, ring.targetRadius)

        objectAnimator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(p0: ValueAnimator) {
                ring.curRadius = p0.animatedValue as Float
                ring.alpha = (255 * (1 - p0.animatedFraction)).toInt()
                ring.curStoke = (ring.startStoke - ring.targetStroke) * (1 - p0.animatedFraction)
            }
        })
        objectAnimator.duration = ring.duration
        objectAnimator.interpolator = DecelerateInterpolator()
        return objectAnimator
    }

    private fun createLineAnim(line: Line): AnimatorSet {
        val animators = AnimatorSet()
        val evaluator = BurstEvaluator()
        val animator = ValueAnimator.ofObject(evaluator, line.startPoint, line.endPoint)
        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as PointF
            line.headPoint = animatedValue
        }
        animator.interpolator = AccelerateInterpolator()
        animator.duration = 600L
        val animator2 = ValueAnimator.ofObject(evaluator, line.startPoint, line.endPoint)
        animator2.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as PointF
            line.tailPoint = animatedValue
        }
        animator2.interpolator = DecelerateInterpolator()
        animator2.duration = 600L
        animators.playTogether(animator, animator2)
        return animators
    }

    data class Ring(var center: PointF,
                    val startRadius: Float, val targetRadius: Float, var curRadius: Float,
                    val startStoke: Float, val targetStroke: Float, var curStoke: Float,
                    val duration: Long, var alpha: Int = 255)

    private fun createShrink(target: CustomBitmap): AnimatorSet {
        val animators = AnimatorSet()
        val objectAnimator = ObjectAnimator.ofFloat(target, "scaleX", 1f, 0f)
        val objectAnimator2 = ObjectAnimator.ofFloat(target, "scaleY", 1f, 0f)
        objectAnimator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(p0: ValueAnimator) {
                target.scaleX = p0.animatedFraction
            }

        })
        objectAnimator2.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(p0: ValueAnimator) {
                target.scaleY = p0.animatedFraction
            }

        })
        animators.playTogether(objectAnimator, objectAnimator2)
        animators.duration = movieDuration.toLong()

        return animators
    }

    private fun createStretch(target: CustomBitmap): AnimatorSet {
        val animators = AnimatorSet()
        val objectAnimator = ObjectAnimator.ofFloat(target, "scaleX", 1f, 0.5f)
        val objectAnimator2 = ObjectAnimator.ofFloat(target, "scaleY", 1f, 1.5f)
        objectAnimator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(p0: ValueAnimator) {
                target.scaleX = p0.animatedFraction
            }

        })
        objectAnimator2.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(p0: ValueAnimator) {
                target.scaleY = p0.animatedFraction
            }

        })
        animators.playTogether(objectAnimator, objectAnimator2)
        animators.duration = movieDuration.toLong()
        return animators
    }

    private fun createRotate(target: CustomBitmap): ObjectAnimator {
        val objectAnimator = ObjectAnimator.ofFloat(target, "rotation", 0f, Random().nextFloat() * (30f - 0f))
        objectAnimator.duration = movieDuration.toLong()
        objectAnimator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(p0: ValueAnimator) {
                target.angle = p0.animatedFraction * 100
            }

        })
        objectAnimator.interpolator = DecelerateInterpolator()
        return objectAnimator
    }

    private fun createSpring(target: CustomBitmap): ObjectAnimator {
        val objectAnimator = ObjectAnimator.ofFloat(target, "scaleX", 1f, 10f, 0f)
        objectAnimator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(p0: ValueAnimator) {
                //target.scaleX = p0.animatedFraction
            }

        })
        objectAnimator.duration = movieDuration.toLong()
        return objectAnimator
    }

    private fun createAnimator(target: CustomBitmap, degree: Int): ValueAnimator {
        val evaluator = BurstEvaluator()

        val radius = Random().nextInt(Math.min(width, height) / 3) + 1
        val animator = ValueAnimator.ofObject(evaluator,
                getCirclePoint(
                        PointF((width / 2).toFloat(), (height / 2).toFloat()),
                        radius.toFloat(),
                        degree),

                getCirclePoint(
                        PointF((width / 2).toFloat(), (height / 2).toFloat()),
                        radius.toFloat() * 1.5f,
                        degree))

        animator.duration = movieDuration.toLong()

        when (Random().nextInt(3)) {
            0 -> animator.interpolator = LinearInterpolator()
            1 -> animator.interpolator = AccelerateInterpolator()
            2 -> animator.interpolator = AccelerateDecelerateInterpolator()
            3 -> animator.interpolator = DecelerateInterpolator()
        }

        animator.addUpdateListener { valueAnimator ->
            if (valueAnimator.animatedFraction > 0) {
                val animatedValue = valueAnimator.animatedValue as PointF
                target.point.x = animatedValue.x.toInt()
                target.point.y = animatedValue.y.toInt()
                target.alpha = (255 * (1 - valueAnimator.animatedFraction)).toInt()
            }
        }

        return animator
    }

    private fun getRandomAngle(size : Int) : MutableList<Int>{
        val angles = mutableListOf<Int>()
        val limit = 360 / size
        var newAngle : Int
        for(i in 0 until size){
            newAngle = limit * i
            newAngle += Random().nextInt(limit)
            angles.add(newAngle)
        }
        return angles
    }

    private fun getCirclePoint(center: PointF, radius: Float, angle : Int) : PointF {
        val x = center.x + radius * Math.cos( angle * 3.14 / 180)
        val y = center.y + radius * Math.sin( angle * 3.14 / 180)
        return PointF(x.toFloat(), y.toFloat())
    }

    class CustomBitmap(val bitmap: Bitmap, val size: Int, val type: Type,
                       var point: Point, var angle: Float,
                       var scaleX: Float = 1f, var scaleY: Float = 1f, var alpha: Int = 255) {
        enum class Type {
            STRETCH, ROTATE, SHRINK, NONE
        }

        val defaultAngle = angle
    }

    class Line(val startPoint: PointF, val endPoint: PointF,
               var headPoint: PointF = startPoint, var tailPoint: PointF = startPoint)
}