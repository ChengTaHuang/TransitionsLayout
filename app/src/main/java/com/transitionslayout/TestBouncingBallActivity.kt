package com.transitionslayout

import android.app.Activity
import android.content.Intent
import android.graphics.PointF
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.l.transitionslayoutlib.ball.BouncingBall
import com.l.transitionslayoutlib.ball.BouncingBallEventListener
import kotlinx.android.synthetic.main.activity_test_bouncing_ball.*

class TestBouncingBallActivity : AppCompatActivity() {

    companion object {
        fun launch(activity: Activity){
            val intent = Intent(activity , TestBouncingBallActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_bouncing_ball)

        constraintLayoutBackground.setOnClickListener {
            val width = constraintLayoutBackground.width
            val height = constraintLayoutBackground.height

            val data = BouncingBall.createBouncePath(
                    BouncingBall.Bounce(
                            PointF(0f,height/2f) ,
                            PointF(width/6f,height/2f) ,
                            100f),
                    3)
            bouncingBall.setPaths(data)
            bouncingBall.playBounce()
        }

        bouncingBall.listener = object : BouncingBallEventListener{
            override fun start() {
                Log.i("tag","start")
            }

            override fun top(point: PointF, times: Int, bounce: BouncingBall.Bounce) {
                Log.i("tag","top")
            }

            override fun collision(point: PointF, times: Int, bounce: BouncingBall.Bounce) {
                Log.i("tag","collision")
            }

            override fun finish() {
                Log.i("tag","finish")
            }

        }
    }
}
