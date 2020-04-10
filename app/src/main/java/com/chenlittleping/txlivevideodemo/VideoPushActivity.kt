package com.chenlittleping.txlivevideodemo

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.chenlittleping.txlivevideodemo.filter.GreyFilter
import com.tencent.rtmp.TXLivePushConfig
import com.tencent.rtmp.TXLivePusher
import kotlinx.android.synthetic.main.activity_push.*


/**
 * 推流
 *
 * @since TxLiveVideoDemo
 * @version TxLiveVideoDemo
 * @Datetime 2020-04-02 14:57
 *
 */
class VideoPushActivity : AppCompatActivity(), TXLivePusher.VideoCustomProcessListener {
    private lateinit var mLivePusher: TXLivePusher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_push)
        initPusher()
    }

    private fun initPusher() {
        val mLivePushConfig = TXLivePushConfig()
        mLivePusher = TXLivePusher(this)

        // 一般情况下不需要修改 config 的默认配置
        mLivePusher.config = mLivePushConfig

        mLivePusher.startCameraPreview(tx_cloud_video_view)

        // 开启自定义渲染
        mLivePusher.setVideoProcessListener(this)
    }

    fun startPushing(v: View) {
        val rtmpURL = "xxxxxxxxxx" //替换为自己的推流地址
        val ret = mLivePusher.startPusher(rtmpURL.trim())
        if (ret == -5) {
            Log.i("VideoPushActivity", "startRTMPPush: license 校验失败")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mLivePusher.stopPusher()
        mLivePusher.stopCameraPreview(true)
        greyFilter?.release()
    }

    private var greyFilter: GreyFilter? = null

    override fun onTextureCustomProcess(textureId: Int, width: Int, height: Int): Int {
        if (greyFilter == null) {
            greyFilter = GreyFilter()
            greyFilter?.setTextureID(textureId)
            greyFilter?.setVideoSize(width, height)
        }
        greyFilter?.draw()
        return if(greyFilter?.getFboTextureID() == -1) textureId else greyFilter?.getFboTextureID()!!
    }

    override fun onTextureDestoryed() {
    }

    override fun onDetectFacePoints(p0: FloatArray?) {
    }
}