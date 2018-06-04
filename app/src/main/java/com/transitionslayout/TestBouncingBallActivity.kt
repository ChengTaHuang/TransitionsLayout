package com.transitionslayout

import android.app.Activity
import android.content.Intent
import android.graphics.PointF
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.l.transitionslayoutlib.ball.BouncingBall
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
    }
}
