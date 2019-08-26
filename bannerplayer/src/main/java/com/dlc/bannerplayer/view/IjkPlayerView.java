package com.dlc.bannerplayer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import android.view.TextureView;
import com.danikula.videocache.HttpProxyCacheServer;
import com.dlc.bannerplayer.VideoCacheProxy;
import com.dlc.bannerplayer.view.base.PlayerView;
import java.io.IOException;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.IjkTimedText;

public class IjkPlayerView extends TextureView implements PlayerView {

    private final IjkMediaPlayer mIjkMediaPlayer;
    private Listener mListener;
    private Surface mSurface;
    private final Handler mHandler;
    private Uri mUri;

    private boolean mPlaywhenready;
    private boolean mReady;
    private final HandlerThread mHandlerThread;

    public IjkPlayerView(Context context) {
        super(context);

        mHandlerThread = new HandlerThread("player-control-thread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");

        mIjkMediaPlayer = new IjkMediaPlayer();
        IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_FATAL);
        mIjkMediaPlayer.setLogEnabled(false);

        //ijk关闭log
    
        resetIJkPlayer();

        mPlaywhenready = false;

        setSurfaceTextureListener(listener);

        VideoCacheProxy.getProxy(context);
    }

    SurfaceTextureListener listener = new SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Surface _surface = new Surface(surface);
            clearFrame(_surface);
            mSurface = _surface;
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    @Override
    public void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public void setData(String url) {
        // 获取缓存的代理地址
        mUri = Uri.parse(getProxyUrl(url));
    }

    @Override
    public void seekTo(int msec) {
        mIjkMediaPlayer.seekTo(msec);
    }

    @Override
    public void prepare() {

        mReady = false;

        if (mSurface == null) {
            mHandler.post(mRunPrepare);
            return;
        }

        mIjkMediaPlayer.stop();
        resetIJkPlayer();
        mIjkMediaPlayer.setSurface(mSurface);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mIjkMediaPlayer.setDataSource(getContext().getApplicationContext(), mUri, null);
            } else {
                mIjkMediaPlayer.setDataSource(mUri.toString());
            }
            mIjkMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();

            if (mIjkListener != null) {
                mIjkListener.onError(mIjkMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            }
        }
    }

    Runnable mRunPrepare = new Runnable() {
        @Override
        public void run() {
            if (mSurface != null) {
                prepare();
            } else {
                mHandler.postDelayed(mRunPrepare, 1);
            }
        }
    };

    Runnable mRunPlay = new Runnable() {
        @Override
        public void run() {
            if (mPlaywhenready) {
                if (mReady) {
                    mIjkMediaPlayer.start();
                } else {
                    mHandler.postDelayed(mRunPlay, 1);
                }
            }
        }
    };

    private void removeCallbacks() {
        mHandler.removeCallbacks(mRunPrepare);
        mHandler.removeCallbacks(mRunPlay);
    }

    private void resetIJkPlayer() {
        mIjkMediaPlayer.setDisplay(null);
        mIjkMediaPlayer.reset();
        clearFrame(mSurface);
        setIjkListener(mIjkListener);
        mIjkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
        
        IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_FATAL);
        mIjkMediaPlayer.setLogEnabled(false);
    }

    @Override
    public void play() {
        mPlaywhenready = true;
        mHandler.removeCallbacks(mRunPlay);
        mRunPlay.run();
    }

    @Override
    public void pause() {
        mPlaywhenready = false;
        mHandler.removeCallbacks(mRunPlay);
        mIjkMediaPlayer.pause();
    }

    @Override
    public void resume() {
        play();
    }

    @Override
    public void stop() {
        mPlaywhenready = false;
        mReady = false;
        removeCallbacks();
        mIjkMediaPlayer.stop();
        mIjkMediaPlayer.setDisplay(null);
        mIjkMediaPlayer.reset();
        clearFrame(mSurface);
    }

    private void clearFrame(Surface surface) {
        try {
            Canvas canvas = surface.lockCanvas(null);
            canvas.drawColor(Color.BLACK);
            surface.unlockCanvasAndPost(canvas);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    @Override
    public void release() {
        stop();
        mIjkMediaPlayer.reset();
        mIjkMediaPlayer.setDisplay(null);
        mIjkMediaPlayer.release();
        mHandlerThread.quitSafely();
    }

    @Override
    public boolean isStarted() {
        return mPlaywhenready;
    }

    @Override
    public boolean isPlaying() {
        return mIjkMediaPlayer.isPlaying();
    }

    @Override
    public boolean isPrepared() {
        return mReady;
    }

    ///////////////////////////////////////////////////////////////

    /**
     * 获取带缓存的视频url
     *
     * @param originalUrl
     * @return
     */
    private String getProxyUrl(String originalUrl) {
        HttpProxyCacheServer proxy = VideoCacheProxy.getProxy(getContext());
        return proxy.getProxyUrl(originalUrl);
    }

    private IjkPlayerListener mIjkListener = new BaseIjkPlayerListener() {

        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {

            mReady = true;

            if (mListener != null) {
                mListener.onPrepared(IjkPlayerView.this);
            }
        }

        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            if (mListener != null) {
                mListener.onCompleted(IjkPlayerView.this);
            }
        }

        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
            if (mListener != null) {
                mListener.onError(IjkPlayerView.this);
            }
            return true;
        }
    };

    public void setIjkListener(IjkPlayerListener listener) {
        if (mIjkMediaPlayer != null) {
            mIjkMediaPlayer.setOnPreparedListener(listener);
            mIjkMediaPlayer.setOnVideoSizeChangedListener(listener);
            mIjkMediaPlayer.setOnCompletionListener(listener);
            mIjkMediaPlayer.setOnErrorListener(listener);
            mIjkMediaPlayer.setOnInfoListener(listener);
            mIjkMediaPlayer.setOnBufferingUpdateListener(listener);
            mIjkMediaPlayer.setOnSeekCompleteListener(listener);
            mIjkMediaPlayer.setOnTimedTextListener(listener);
        }
    }

    public interface IjkPlayerListener
        extends IMediaPlayer.OnPreparedListener, IMediaPlayer.OnVideoSizeChangedListener,
        IMediaPlayer.OnCompletionListener, IMediaPlayer.OnErrorListener,
        IMediaPlayer.OnInfoListener, IMediaPlayer.OnBufferingUpdateListener,
        IMediaPlayer.OnSeekCompleteListener, IMediaPlayer.OnTimedTextListener {

        @Override
        void onPrepared(IMediaPlayer iMediaPlayer);

        @Override
        void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3);

        @Override
        void onCompletion(IMediaPlayer iMediaPlayer);

        @Override
        boolean onError(IMediaPlayer iMediaPlayer, int i, int i1);

        @Override
        boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1);

        @Override
        void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i);

        @Override
        void onSeekComplete(IMediaPlayer iMediaPlayer);

        @Override
        void onTimedText(IMediaPlayer iMediaPlayer, IjkTimedText ijkTimedText);
    }

    public static class BaseIjkPlayerListener implements IjkPlayerListener {

        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {

        }

        @Override
        public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {

        }

        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {

        }

        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
            return false;
        }

        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
            return false;
        }

        @Override
        public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {

        }

        @Override
        public void onSeekComplete(IMediaPlayer iMediaPlayer) {

        }

        @Override
        public void onTimedText(IMediaPlayer iMediaPlayer, IjkTimedText ijkTimedText) {

        }
    }
}
