package com.transitionslayout

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import kotlinx.android.synthetic.main.activity_test_burst_layout.*

class TestBurstLayoutActivity : AppCompatActivity() {

    companion object {
        fun launch(activity: Activity) {
            val intent = Intent(activity, TestBurstLayoutActivity::class.java)
            activity.startActivity(intent)
        }
    }

    private val handler = Handler()
    private val burstHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_burst_layout)

        burstLayout.createLine(60f, 2f)
        burstLayout.setLineAnimationDuration(500L)

        burstLayout.setOnTouchListener { _, event ->
            val x = event.x
            val y = event.y
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {

                    if(enableLineCheckBox.isChecked){
                        burstLayout.expand(x,y)

                        handler.postDelayed({
                            burstLayout.collapse(x,y)
                        }, 500L)

                    }

                    burstHandler.postDelayed({
                        burstLayout.burst(
                                x.toInt(),
                                (2f + y).toInt(),
                                20,
                                burstLayout.height
                        )
                    }, 200L)

                }
                else -> {

                }
            }
            return@setOnTouchListener false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        burstLayout.clear()
    }
}
