package com.transitionslayout

import android.app.Activity
import android.content.Intent
import android.graphics.PointF
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.l.transitionslayoutlib.TransitionUtil
import com.l.transitionslayoutlib.ball.BouncingBall
import com.l.transitionslayoutlib.transitions.TransitionsEventListener
import kotlinx.android.synthetic.main.activity_test_transitions.*

class TestTransitionsActivity : AppCompatActivity() {

    companion object {
        fun launch(activity: Activity){
            val intent = Intent(activity , TestTransitionsActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_transitions)

        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        var halfWidth = (width) / 2
        halfWidth /= 3

        val path1 = BouncingBall.createBouncePath(
                BouncingBall.Bounce(PointF(0f, height / 2.toFloat()),
                        PointF(halfWidth.toFloat(), height / 2.toFloat()),
                        50f), 3)
        val path2 = BouncingBall.createBouncePath(
                BouncingBall.Bounce(PointF(width.toFloat(), height / 2.toFloat()),
                        PointF(width - halfWidth.toFloat(), height / 2.toFloat()),
                        50f), 3)
        path1.addAll(path2)

        transitions
                .createBall(
                        TransitionUtil.convertDpToPixel(20f, baseContext),
                        TransitionUtil.convertDpToPixel(20f, baseContext),
                        PointF(0f, height / 2.toFloat()))
                .setBallPaths(path1)
                .createBurstLayout(width, height)
                .createBalloon()
                .showBalloon(3,6)
                .setTransitionsEventListener(object : TransitionsEventListener {
                    override fun startPlayBall() {

                    }

                    override fun finishPlayBall() {
                    }
                })

        transitions.setOnClickListener {
            transitions.playBall()
        }
    }
}
