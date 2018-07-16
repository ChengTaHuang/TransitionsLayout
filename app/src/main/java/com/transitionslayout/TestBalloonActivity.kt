package com.transitionslayout

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.l.transitionslayoutlib.balloon.BalloonView
import kotlinx.android.synthetic.main.activity_test_balloon.*

class TestBalloonActivity : AppCompatActivity() {
    var clickTimes = 0

    companion object {
        fun launch(activity: Activity){
            val intent = Intent(activity , TestBalloonActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_balloon)

        balloonView.setOnClickListener {
            if(clickTimes % 2 == 0){
                balloonView.start()
            }else{
                balloonView.stop()
            }
            clickTimes ++
        }

        balloonView.listener = object : BalloonView.Listener{
            override fun playStart() {

            }

            override fun playing(process: Long) {

            }

            override fun playEnd() {
                clickTimes = 0
            }
        }
    }
}
