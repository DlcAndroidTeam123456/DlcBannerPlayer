package com.dlc.bannerplayer.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ViewTarget;
import com.dlc.bannerplayer.R;
import com.dlc.bannerplayer.WeakHandler;
import com.dlc.bannerplayer.data.BannerData;
import com.dlc.bannerplayer.view.base.BannerPlayerView;
import com.dlc.bannerplayer.view.base.PlayerView;

public class IjkPlayerBannerView extends FrameLayout implements BannerPlayerView {

    private static final String TAG = "BannerPlayerView";
    private static final boolean SHOW_LOG = true;

    private IjkPlayerView mPlayerView;
    private ImageView mImageView;
    private BannerData mData;

    private PlayListener mListener;
    private int mIndex;

    private WeakHandler mHandler;
    private PlayAttrs mPlayAttrs;

    private boolean mPlaying;

    private ViewTarget<ImageView, Drawable> mImageTarget;
    private RequestOptions mRequestOptions;
    private int mTargetWidth;
    private int mTargetHeight;

    public IjkPlayerBannerView(@NonNull Context context) {
        this(context, null);
    }

    private IjkPlayerBannerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private IjkPlayerBannerView(@NonNull Context context, @Nullable AttributeSet attrs,
        int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 播放控件
        mPlayerView = new IjkPlayerView(context);
        mPlayerView.setListener(new PlayerView.Listener() {
            @Override
            public void onPrepared(PlayerView playerView) {

            }

            @Override
            public void onCompleted(PlayerView playerView) {
                // 如果是强制播放时间，，而且还没结束，就设置重新播放
                if (mPlayAttrs.forcePlayDuration || mPlayAttrs.count == 1) {
                    if (mPlaying) {
                        mPlayerView.seekTo(0);
                        play();
                    }
                    return;
                }
                // 执行停止
                onStop();
            }

            @Override
            public void onError(PlayerView playerView) {
                log(Log.ERROR, "index=" + getIndex() + ",执行onPlayerError");
                mPlaying = false;
                removeCallbacks();
                // 播放异常
                if (mListener != null) {
                    mListener.onPlayError((BannerPlayerView) getSelf());
                }
            }
        });

        addView(mPlayerView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        mImageView = new ImageView(context);
        addView(mImageView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        mRequestOptions = RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL)
            //.skipMemoryCache(false)
            .placeholder(R.drawable.shape_white).dontAnimate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int width = w;
        int height = h;
        mTargetWidth = (int) (w * mPlayAttrs.overrideImageScale);
        mTargetHeight = (int) (h * mPlayAttrs.overrideImageScale);
    }

    @Override
    public void setListener(PlayListener listener) {
        mListener = listener;
    }

    @Override
    public ImageView getImageView() {
        return mImageView;
    }

    @Override
    public void setData(BannerData data) {
        mData = data;
        updateChildViewVisible(data);
    }

    private void updateChildViewVisible(BannerData data) {
        switch (data.getType()) {
            case BannerData.TYPE_IMAGE:
                mImageView.setVisibility(VISIBLE);
                mPlayerView.setVisibility(GONE);
                break;
            case BannerData.TYPE_VIDEO:
                mImageView.setVisibility(GONE);
                mPlayerView.setVisibility(VISIBLE);
                break;
            default:
                mImageView.setVisibility(GONE);
                mPlayerView.setVisibility(GONE);
                break;
        }
    }

    @Override
    public void setIndex(int index) {
        mIndex = index;
    }

    @Override
    public int getIndex() {
        return mIndex;
    }

    @Override
    public BannerData getData() {
        return mData;
    }

    @Override
    public void prepare() {

        log(Log.ERROR, "index=" + getIndex() + ",执行prepare");

        if (mData == null) {
            return;
        }

        switch (mData.getType()) {
            case BannerData.TYPE_IMAGE:
                // 如果是图片类型，则直接加载图片
                loadImage();
                break;
            case BannerData.TYPE_VIDEO:
                // 视频的话，就准备载入，当不播放
                mPlayerView.setData(mData.getUrl());
                mPlayerView.prepare();
                break;
            default:
                // 其他类型就不管了
                break;
        }
    }

    /**
     * 加载图片
     */
    private void loadImage() {

        try {
            Glide.with(this).clear(mImageView);
        } catch (Exception e) {
            //e.printStackTrace();
        }

        mImageTarget = Glide.with(this)
            .load(mData.getUrl())
            .apply(mRequestOptions.override(mTargetWidth, mTargetHeight))
            .into(mImageView);
    }

    @Override
    public void play() {

        log(Log.INFO, "index=" + getIndex() + ",执行play");

        mPlaying = true;
        removeCallbacks();

        if (mData.getType() == BannerData.TYPE_VIDEO) {
            // 播放
            mPlayerView.play();
        } else if (mData.getType() == BannerData.TYPE_IMAGE) {
            if (mImageTarget != null) {
                Request request = mImageTarget.getRequest();
                if (request != null && request.isFailed()) {
                    loadImage();
                }
            }
        }

        if (mData.getType() == BannerData.TYPE_IMAGE || mPlayAttrs.forcePlayDuration) {
            // 强制播放时间的话，延时执行停止
            mHandler.postDelayed(mRunStopPlay, mPlayAttrs.playDuration);
        }
    }

    private Runnable mRunPlay = new Runnable() {
        @Override
        public void run() {
            play();
        }
    };

    private Runnable mRunStopPlay = new Runnable() {
        @Override
        public void run() {
            if (mPlaying) {
                onStop();
            }
        }
    };

    @Override
    public void delayPlay(final long delay) {
        removeCallbacks();
        mHandler.postDelayed(mRunPlay, delay);
    }

    @Override
    public void pause() {

        //mPlaying = false;

        log(Log.DEBUG, "index=" + getIndex() + ",执行pause");

        removeCallbacks();
        if (mData.getType() == BannerData.TYPE_VIDEO) {
            // 暂停
            mPlayerView.pause();
        }
    }

    private void removeCallbacks() {
        mHandler.removeCallbacks(mRunPlay);
        mHandler.removeCallbacks(mRunStopPlay);
    }

    private void onStop() {

        mPlaying = false;

        log(Log.WARN, "index=" + getIndex() + ",执行onStop");
        stopVideo();
        if (mListener != null && mPlayAttrs.currentIndex == getIndex()) {
            mListener.onPlayComplete((BannerPlayerView) getSelf());
        }
    }

    @Override
    public void stop() {
        mPlaying = false;
        //log(Log.WARN, "index=" + getIndex() + ",执行stop");
        removeCallbacks();
        stopVideo();
    }

    private void stopVideo() {
        if (mData.getType() == BannerData.TYPE_VIDEO) {
            // 播放
            mPlayerView.stop();
        }
    }

    @Override
    public void release() {

        mPlaying = false;

        try {
            removeCallbacks();
            stopVideo();
            mPlayerView.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View getSelf() {
        return this;
    }

    @Override
    public void setConfig(PlayAttrs playAttrs, WeakHandler handler) {
        mPlayAttrs = playAttrs;
        mHandler = handler;
    }

    @Override
    public boolean isPlaying() {
        return mPlaying;
    }

    @Override
    public boolean isPrepared() {

        if (mData != null) {
            if (mData.getType() == BannerData.TYPE_VIDEO) {
                return mPlayerView.isPrepared();
            }
        }
        return true;
    }

    @Override
    public int compareTo(@NonNull BannerPlayerView o) {
        return this.getIndex() - o.getIndex();
    }

    private void log(int logLevel, String msg) {

        if (SHOW_LOG) {

            switch (logLevel) {
                case Log.VERBOSE:
                    Log.v(TAG, msg);
                    break;
                case Log.DEBUG:
                    Log.d(TAG, msg);
                    break;
                case Log.INFO:
                    Log.i(TAG, msg);
                    break;
                case Log.WARN:
                    Log.w(TAG, msg);
                    break;
                case Log.ERROR:
                    Log.e(TAG, msg);
                    break;
            }
        }
    }
}
