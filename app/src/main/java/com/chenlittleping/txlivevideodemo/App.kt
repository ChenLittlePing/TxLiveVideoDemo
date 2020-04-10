package com.chenlittleping.txlivevideodemo

import android.app.Application
import android.content.Context
import com.tencent.rtmp.TXLiveBase


/**
 *
 * @since TxLiveVideoDemo
 * @version TxLiveVideoDemo
 * @Datetime 2020-04-02 11:53
 *
 */
class App : Application() {
    companion object {
        lateinit var globalContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        globalContext = this
        val licenceURL = "http://license.vod2.myqcloud.com/license/v1/0eec55a1a03c358a38a6b267958a8c1e/TXLiveSDK.licence" // 获取到的 licence url
        val licenceKey = "e90e0872aaeb77972a4831880e70aab5" // 获取到的 licence key
        TXLiveBase.getInstance().setLicence(this, licenceURL, licenceKey)
    }
}