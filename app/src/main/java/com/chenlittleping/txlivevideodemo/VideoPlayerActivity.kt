package com.chenlittleping.txlivevideodemo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.tencent.rtmp.ITXLivePlayListener
import com.tencent.rtmp.TXLiveConstants
import com.tencent.rtmp.TXLivePlayer
import kotlinx.android.synthetic.main.activity_player.*


class VideoPlayerActivity : AppCompatActivity(), ITXLivePlayListener {
    private lateinit var mLivePlayer: TXLivePlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        initPlayer()
    }

    private fun initPlayer() {
        mLivePlayer = TXLivePlayer(this)
        mLivePlayer.setPlayerView(video_view)
        mLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION)
//        mLivePlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_LANDSCAPE)
        mLivePlayer.setPlayListener(this)
    }

    fun startPlaying(v: View) {
        // 腾讯云测试地址，需替换为自己的播放地址
        val flvUrl = "http://5815.liveplay.myqcloud.com/live/5815_89aad37e06ff11e892905cb9018cf0d4_900.flv"
        mLivePlayer.startPlay(flvUrl, TXLivePlayer.PLAY_TYPE_LIVE_FLV)
    }

    override fun onPlayEvent(p0: Int, p1: Bundle?) {
    }

    override fun onNetStatus(b: Bundle) {
    }

    override fun onDestroy() {
        super.onDestroy()
        mLivePlayer.stopPlay(true)
    }
}
