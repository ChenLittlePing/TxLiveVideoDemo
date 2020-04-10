package com.chenlittleping.txlivevideodemo

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun clickPlayer(view: View) {
       startActivity(Intent(this, VideoPlayerActivity::class.java))
    }

    fun clickPusher(view: View) {
        startActivity(Intent(this, VideoPushActivity::class.java))
    }
}
