package com.licheedev.myapplication;

import android.app.Application;
import com.danikula.videocache.HttpProxyCacheServer;
import com.dlc.bannerplayer.VideoCacheProxy;

public class App extends Application implements VideoCacheProxy.AppWrapper {

    static App sInstance;
    private HttpProxyCacheServer mProxy;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }
    
    public static App getInstance() {
        return sInstance;
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer.Builder(this).cacheDirectory(
            VideoCacheProxy.getVideoCacheDir(this))

            .build();
    }

    @Override
    public HttpProxyCacheServer getVideoCacheProxy() {
        return mProxy == null ? (mProxy = newProxy()) : mProxy;
    }
}
