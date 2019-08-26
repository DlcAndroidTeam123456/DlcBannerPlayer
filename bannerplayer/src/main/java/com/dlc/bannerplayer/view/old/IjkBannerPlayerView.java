//package com.dlc.bannerplayer.view;
//
//import android.content.Context;
//import android.graphics.SurfaceTexture;
//import android.media.MediaPlayer;
//import android.net.Uri;
//import android.os.Build;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.view.Surface;
//import android.view.TextureView;
//import android.view.View;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.engine.DiskCacheStrategy;
//import com.bumptech.glide.request.RequestOptions;
//import com.danikula.videocache.HttpProxyCacheServer;
//import com.dlc.bannerplayer.VideoCacheProxy;
//import com.dlc.bannerplayer.WeakHandler;
//import com.dlc.bannerplayer.data.BannerData;
//import com.dlc.bannerplayer.view.base.BannerPlayerView;
//import java.io.IOException;
//import tv.danmaku.ijk.media.player.IMediaPlayer;
//import tv.danmaku.ijk.media.player.IjkMediaPlayer;
//import tv.danmaku.ijk.media.player.IjkTimedText;
//
//public class IjkBannerPlayerView extends FrameLayout implements BannerPlayerView {
//
//    private static final String TAG = "BannerPlayerView";
//    private static final boolean SHOW_LOG = true;
//
//    private ImageView mImageView;
//    private BannerData mData;
//
//    private PlayListener mListener;
//    private int mIndex;
//
//    private WeakHandler mHandler;
//    private PlayAttrs mPlayAttrs;
//
//    private boolean mStarted;
//    private boolean mPlaying;
//    private boolean mPrepared;
//    private boolean mVideoReady;
//
//    private TextureView mPlayerView;
//    private IjkMediaPlayer mIjkMediaPlayer;
//    private Surface mSurface;
//    private Uri mUri;
//    private boolean mSurfaceBound;
//
//    public IjkBannerPlayerView(@NonNull Context context) {
//        this(context, null);
//    }
//
//    private IjkBannerPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
//        this(context, attrs, 0);
//    }
//
//    private IjkBannerPlayerView(@NonNull Context context, @Nullable AttributeSet attrs,
//        int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//
//        // 播放控件
//
//        mIjkMediaPlayer = new IjkMediaPlayer();
//        IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_ERROR);
//        resetIJkPlayer();
//
//        mPlayerView = new TextureView(context);
//        mPlayerView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
//            @Override
//            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//                mSurface = new Surface(surface);
//                if (!mSurfaceBound) {
//                    loadVideo();
//                }
//            }
//
//            @Override
//            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//
//            }
//
//            @Override
//            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//                return false;
//            }
//
//            @Override
//            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//
//            }
//        });
//
//        addView(mPlayerView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//        // 占满全屏
//
//        mImageView = new ImageView(context);
//        addView(mImageView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//
//        VideoCacheProxy.getProxy(context);
//    }
//
//    private void resetIJkPlayer() {
//        mIjkMediaPlayer.reset();
//        setListener(mIjkListener);
//        mIjkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
//    }
//
//    @Override
//    public void setListener(PlayListener listener) {
//        mListener = listener;
//    }
//
//    @Override
//    public ImageView getImageView() {
//        return mImageView;
//    }
//
//    @Override
//    public void setData(BannerData data) {
//        mData = data;
//        updateChildViewVisible(data);
//    }
//
//    private void updateChildViewVisible(BannerData data) {
//        switch (data.getType()) {
//            case BannerData.TYPE_IMAGE:
//                mImageView.setVisibility(VISIBLE);
//                mPlayerView.setVisibility(GONE);
//                break;
//            case BannerData.TYPE_VIDEO:
//                mImageView.setVisibility(GONE);
//                mPlayerView.setVisibility(VISIBLE);
//                break;
//            default:
//                mImageView.setVisibility(GONE);
//                mPlayerView.setVisibility(GONE);
//                break;
//        }
//    }
//
//    @Override
//    public void setIndex(int index) {
//        mIndex = index;
//    }
//
//    @Override
//    public int getIndex() {
//        return mIndex;
//    }
//
//    @Override
//    public BannerData getData() {
//        return mData;
//    }
//
//    @Override
//    public void prepare() {
//
//        log(Log.ERROR, "index=" + getIndex() + ",执行prepare");
//
//        if (mData == null) {
//            return;
//        }
//
//        if (mPrepared) {
//            return;
//        }
//
//        mPrepared = true;
//
//        switch (mData.getType()) {
//            case BannerData.TYPE_IMAGE:
//                // 如果是图片类型，则直接加载图片
//                loadImage();
//                break;
//            case BannerData.TYPE_VIDEO:
//                // 视频的话，就准备载入，当不播放
//                prepareVideo();
//                break;
//            default:
//                // 其他类型就不管了
//                break;
//        }
//    }
//
//    /**
//     * 加载图片
//     */
//    private void loadImage() {
//        Glide.with(this)
//            .asBitmap()
//            .load(mData.getUrl())
//            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL).dontAnimate())
//            .into(mImageView);
//    }
//
//    /**
//     * 准备视频
//     */
//    private void prepareVideo() {
//        // 获取缓存的代理地址
//        String url = getProxyUrl(mData.getUrl());
//        mUri = Uri.parse(url);
//        loadVideo();
//    }
//
//    private IjkPlayerListener mIjkListener = new BaseIjkPlayerListener() {
//
//        @Override
//        public void onPrepared(IMediaPlayer iMediaPlayer) {
//            mVideoReady = true;
//        }
//
//        @Override
//        public void onCompletion(IMediaPlayer iMediaPlayer) {
//            // 如果是强制播放时间，，而且还没结束，就设置重新播放
//            if (mPlayAttrs.forcePlayDuration || mPlayAttrs.count == 1) {
//                if (mStarted) {
//                    mIjkMediaPlayer.seekTo(0);
//                    play();
//                }
//                return;
//            }
//            // 执行停止
//            onStop();
//        }
//
//        @Override
//        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
//
//            resetFlags();
//
//            // 播放异常
//            if (mListener != null && mPlayAttrs.currentIndex == getIndex()) {
//                mListener.onPlayError((BannerPlayerView) getSelf(),
//                    new RuntimeException("IJK播放错误"));
//            }
//
//            return true;
//        }
//    };
//
//    private void resetFlags() {
//        mStarted = false;
//        mPlaying = false;
//        mPrepared = false;
//        mVideoReady = false;
//    }
//
//    private void loadVideo() {
//
//        if (mUri != null) {
//
//            mIjkMediaPlayer.stop();
//            mIjkMediaPlayer.setSurface(null);
//            resetIJkPlayer();
//
//            if (mSurface != null) {
//                mIjkMediaPlayer.setSurface(mSurface);
//                mSurfaceBound = true;
//            }
//
//            try {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//                    mIjkMediaPlayer.setDataSource(getContext().getApplicationContext(), mUri, null);
//                } else {
//                    mIjkMediaPlayer.setDataSource(mUri.toString());
//                }
//
//                mIjkMediaPlayer.prepareAsync();
//            } catch (IOException e) {
//                e.printStackTrace();
//
//                if (mIjkListener != null) {
//                    mIjkListener.onError(mIjkMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
//                }
//            }
//        }
//    }
//
//    @Override
//    public void play() {
//
//        log(Log.INFO, "index=" + getIndex() + ",执行play");
//
//        if (mStarted) {
//            if (mPlaying) {
//                return;
//            }
//        }
//
//        mStarted = true;
//        mPlaying = true;
//
//        removeCallbacks();
//
//        if (mData.getType() == BannerData.TYPE_VIDEO) {
//            mHandler.post(mRunRealPlay);
//        } else if (mData.getType() == BannerData.TYPE_IMAGE) {
//            loadImage();
//        }
//
//        if (mData.getType() == BannerData.TYPE_IMAGE || mPlayAttrs.forcePlayDuration) {
//            // 强制播放时间的话，延时执行停止
//            mHandler.postDelayed(mRunStopPlay, mPlayAttrs.playDuration);
//        }
//    }
//
//    private void removeCallbacks() {
//
//        mHandler.removeCallbacks(mRunRealPlay);
//        mHandler.removeCallbacks(mRunPlay);
//        mHandler.removeCallbacks(mRunStopPlay);
//    }
//
//    private Runnable mRunPlay = new Runnable() {
//        @Override
//        public void run() {
//            play();
//        }
//    };
//
//    private Runnable mRunRealPlay = new Runnable() {
//        @Override
//        public void run() {
//
//            if (mPlaying) {
//                if (mSurfaceBound && mVideoReady) {
//                    mIjkMediaPlayer.start();
//                } else {
//                    mHandler.removeCallbacks(mRunRealPlay);
//                    mHandler.postDelayed(mRunRealPlay, 10);
//                }
//            }
//        }
//    };
//
//    private Runnable mRunStopPlay = new Runnable() {
//        @Override
//        public void run() {
//            if (mStarted) {
//                onStop();
//            }
//        }
//    };
//
//    @Override
//    public void delayPlay(final long delay) {
//        mHandler.postDelayed(mRunPlay, delay);
//    }
//
//    @Override
//    public void pause(boolean seek0) {
//
//        mPlaying = false;
//
//        log(Log.DEBUG, "index=" + getIndex() + ",执行pause");
//
//        removeCallbacks();
//
//        mIjkMediaPlayer.pause();
//        if (seek0) {
//            mIjkMediaPlayer.seekTo(0);
//        }
//    }
//
//    private void onStop() {
//        resetFlags();
//
//        log(Log.WARN, "index=" + getIndex() + ",执行onStop");
//
//        if (mListener != null && mPlayAttrs.currentIndex == getIndex()) {
//
//            stopVideo();
//            mListener.onPlayComplete((BannerPlayerView) getSelf());
//        }
//    }
//
//    /**
//     * 停止播放视频
//     */
//    private void stopVideo() {
//        mIjkMediaPlayer.stop();
//        mIjkMediaPlayer.setSurface(null);
//    }
//
//    @Override
//    public void stop() {
//        mStarted = false;
//        mPlaying = false;
//        mPrepared = false;
//        log(Log.WARN, "index=" + getIndex() + ",执行stop");
//        removeCallbacks();
//        stopVideo();
//    }
//
//    @Override
//    public void release() {
//
//        mStarted = false;
//        mPlaying = false;
//        mPrepared = false;
//
//        try {
//            removeCallbacks();
//            stopVideo();
//            releaseMediaPlayer();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public View getSelf() {
//        return this;
//    }
//
//    @Override
//    public void setConfig(PlayAttrs playAttrs, WeakHandler handler) {
//        mPlayAttrs = playAttrs;
//        mHandler = handler;
//    }
//
//    @Override
//    public boolean isStarted() {
//        return mStarted;
//    }
//
//    @Override
//    public boolean isPlaying() {
//        return mPlaying;
//    }
//
//    public boolean isPrepared() {
//        return mPrepared;
//    }
//
//    /**
//     * 获取带缓存的视频url
//     *
//     * @param originalUrl
//     * @return
//     */
//    private String getProxyUrl(String originalUrl) {
//        HttpProxyCacheServer proxy = VideoCacheProxy.getProxy(getContext());
//        return proxy.getProxyUrl(originalUrl);
//    }
//
//    public void releaseMediaPlayer() {
//
//        if (mIjkMediaPlayer != null) {
//            mIjkMediaPlayer.reset();
//            mIjkMediaPlayer.setDisplay(null);
//            mIjkMediaPlayer.release();
//            mIjkMediaPlayer = null;
//        }
//    }
//
//    public void setListener(IjkPlayerListener listener) {
//        if (mIjkMediaPlayer != null) {
//            mIjkMediaPlayer.setOnPreparedListener(listener);
//            mIjkMediaPlayer.setOnVideoSizeChangedListener(listener);
//            mIjkMediaPlayer.setOnCompletionListener(listener);
//            mIjkMediaPlayer.setOnErrorListener(listener);
//            mIjkMediaPlayer.setOnInfoListener(listener);
//            mIjkMediaPlayer.setOnBufferingUpdateListener(listener);
//            mIjkMediaPlayer.setOnSeekCompleteListener(listener);
//            mIjkMediaPlayer.setOnTimedTextListener(listener);
//        }
//    }
//
//    @Override
//    public int compareTo(@NonNull BannerPlayerView o) {
//        return this.getIndex() - o.getIndex();
//    }
//
//    private void log(int logLevel, String msg) {
//
//        if (SHOW_LOG) {
//
//            switch (logLevel) {
//                case Log.VERBOSE:
//                    Log.v(TAG, msg);
//                    break;
//                case Log.DEBUG:
//                    Log.d(TAG, msg);
//                    break;
//                case Log.INFO:
//                    Log.i(TAG, msg);
//                    break;
//                case Log.WARN:
//                    Log.w(TAG, msg);
//                    break;
//                case Log.ERROR:
//                    Log.e(TAG, msg);
//                    break;
//            }
//        }
//    }
//
//    public interface IjkPlayerListener
//        extends IMediaPlayer.OnPreparedListener, IMediaPlayer.OnVideoSizeChangedListener,
//        IMediaPlayer.OnCompletionListener, IMediaPlayer.OnErrorListener,
//        IMediaPlayer.OnInfoListener, IMediaPlayer.OnBufferingUpdateListener,
//        IMediaPlayer.OnSeekCompleteListener, IMediaPlayer.OnTimedTextListener {
//
//        @Override
//        void onPrepared(IMediaPlayer iMediaPlayer);
//
//        @Override
//        void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3);
//
//        @Override
//        void onCompletion(IMediaPlayer iMediaPlayer);
//
//        @Override
//        boolean onError(IMediaPlayer iMediaPlayer, int i, int i1);
//
//        @Override
//        boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1);
//
//        @Override
//        void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i);
//
//        @Override
//        void onSeekComplete(IMediaPlayer iMediaPlayer);
//
//        @Override
//        void onTimedText(IMediaPlayer iMediaPlayer, IjkTimedText ijkTimedText);
//    }
//
//    public static class BaseIjkPlayerListener implements IjkPlayerListener {
//
//        @Override
//        public void onPrepared(IMediaPlayer iMediaPlayer) {
//
//        }
//
//        @Override
//        public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {
//
//        }
//
//        @Override
//        public void onCompletion(IMediaPlayer iMediaPlayer) {
//
//        }
//
//        @Override
//        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
//            return false;
//        }
//
//        @Override
//        public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
//            return false;
//        }
//
//        @Override
//        public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {
//
//        }
//
//        @Override
//        public void onSeekComplete(IMediaPlayer iMediaPlayer) {
//
//        }
//
//        @Override
//        public void onTimedText(IMediaPlayer iMediaPlayer, IjkTimedText ijkTimedText) {
//
//        }
//    }
//}
