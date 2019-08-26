package com.dlc.bannerplayer;

import android.content.Context;
import com.danikula.videocache.HttpProxyCacheServer;
import java.io.File;

public class VideoCacheProxy {

    public static HttpProxyCacheServer getProxy(Context context) {
        Context app = context.getApplicationContext();
        AppWrapper wrapper = null;
        try {
            wrapper = (AppWrapper) app;
            return wrapper.getVideoCacheProxy();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClassCastException(
                "Application not implemented com.dlc.bannerplayer.VideoCacheProxy.AppWrapper");
        }
    }

    public static File getVideoCacheDir(Context context) {
        return new File(context.getExternalCacheDir(), "video-cache");
    }
    
    public interface AppWrapper {

        HttpProxyCacheServer getVideoCacheProxy();
    }
}
