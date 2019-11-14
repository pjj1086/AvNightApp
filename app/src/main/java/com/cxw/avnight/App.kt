package com.cxw.avnight

import android.app.Application
import android.content.Context
import com.baidu.mobstat.MtjConfig
import com.baidu.mobstat.StatService
import com.cxw.avnight.state.EmptyCallback
import com.cxw.avnight.state.ErrorCallback
import com.cxw.avnight.state.LoadingCallback
import com.kingja.loadsir.core.LoadSir

import kotlin.properties.Delegates
import com.cxw.avnight.weight.MediaLoader
import com.yanzhenjie.album.AlbumConfig
import com.yanzhenjie.album.Album
import java.util.*


class App : Application() {

    companion object {
        var CONTEXT: Context by Delegates.notNull()   //委托属性检查为空
    }

    override fun onCreate() {
        super.onCreate()
        CONTEXT = applicationContext


        Album.initialize(
            AlbumConfig.newBuilder(this)
                .setAlbumLoader(MediaLoader())
                .setLocale(Locale.getDefault())
                .build()
        )


        StatService.setOn(this, StatService.JAVA_EXCEPTION_LOG)  //仅收集java crash，flag = StatService.JAVA_EXCEPTION_LOG
        StatService.setSessionTimeOut(300)  //设置应用进入后台再回到前台为同一次启动的最大间隔时间
        StatService.setDebugOn(true)
     //   StatService.autoTrace(this) // 自动埋点
        StatService.setFeedTrack(MtjConfig.FeedTrackStrategy.TRACK_SINGLE)

        LoadSir.beginBuilder()
            .addCallback(ErrorCallback())
            .addCallback(EmptyCallback())
            .addCallback(LoadingCallback())
            .setDefaultCallback(
                LoadingCallback::class.java
            )
            .commit()
    }
}