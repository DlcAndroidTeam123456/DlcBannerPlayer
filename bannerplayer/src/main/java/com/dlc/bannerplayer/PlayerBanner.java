package com.dlc.bannerplayer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.dlc.bannerplayer.data.BannerData;
import com.dlc.bannerplayer.data.UrlBannerData;
import com.dlc.bannerplayer.listener.OnBannerChangedListener;
import com.dlc.bannerplayer.listener.OnBannerClickListener;
import com.dlc.bannerplayer.view.IjkPlayerBannerView;
import com.dlc.bannerplayer.view.PlayAttrs;
import com.dlc.bannerplayer.view.base.BannerPlayerView;
import com.yy.mobile.widget.SlidableLayout;
import com.yy.mobile.widget.SlideDirection;
import com.yy.mobile.widget.SlideViewAdapter;
import java.util.ArrayList;
import java.util.List;

public class PlayerBanner extends FrameLayout {
    public String tag = "BannerPlayer";

    private int mIndicatorMargin = BannerConfig.PADDING_SIZE;
    private int mIndicatorWidth;
    private int mIndicatorHeight;
    private int mIndicatorSize;
    private int mBannerBackgroundImage;
    private int mBannerIndicatorStyle = BannerConfig.CIRCLE_INDICATOR;
    private int mIndicatorSelectedResId = R.drawable.gray_radius;
    private int mIndicatorUnselectedResId = R.drawable.white_radius;
    private int mLayoutResId = R.layout.default_banner_layout;
    private int mTitleHeight;
    private int mTitleBackground;
    private int mTitleTextColor;
    private int mTitleTextSize;
    private int mGravity = -1;
    private int mScaleType = 1;

    private Context mContext;

    private SlidableLayout mViewPager;
    private TextView mBannerTitle, mNumIndicatorInside, mNumIndicator;
    private LinearLayout mIndicator, mIndicatorInside, mTitleView;
    private ImageView mBannerDefaultImage;

    private OnBannerChangedListener mOnBannerChangedListener;
    private OnBannerClickListener mOnBannerClickListener;

    private WeakHandler mHandler = new WeakHandler();

    private PlayAttrs mPlayAttrs; // 播放属性

    private boolean mAllowScroll = BannerConfig.IS_SCROLL;
    private int mDelayTime = BannerConfig.TIME;

    private List<String> mTitles; // 标题数据
    private List<ImageView> mIndicatorImages; // 指示器图
    private ArrayList<BannerData> mBannerData; // 轮播图数据

    private int mCount = 0;
    private int mErrorDelayTime;
    private LoopAdapter mAdapter;

    public PlayerBanner(Context context) {
        this(context, null);
    }

    public PlayerBanner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerBanner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;

        mPlayAttrs = new PlayAttrs();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        mIndicatorSize = dm.widthPixels / 80;

        mTitles = new ArrayList<>();
        mIndicatorImages = new ArrayList<>();
        mBannerData = new ArrayList<>();

        handleTypedArray(context, attrs);
        View view = LayoutInflater.from(context).inflate(mLayoutResId, this, true);
        mBannerDefaultImage = view.findViewById(R.id.bannerDefaultImage);

        mViewPager = view.findViewById(R.id.bannerViewPager);
        mViewPager.setSlideDuration(1000);
        mViewPager.setFlingVelocity(100);
        mViewPager.setScrollable(mAllowScroll);

        mTitleView = view.findViewById(R.id.titleView);
        mIndicator = view.findViewById(R.id.circleIndicator);
        mIndicatorInside = view.findViewById(R.id.indicatorInside);
        mBannerTitle = view.findViewById(R.id.bannerTitle);
        mNumIndicator = view.findViewById(R.id.numIndicator);
        mNumIndicatorInside = view.findViewById(R.id.numIndicatorInside);
        mBannerDefaultImage.setImageResource(mBannerBackgroundImage);

        setBackgroundColor(Color.BLACK);
    }

    /**
     * 设置轮播数据
     *
     * @param data
     * @return
     */
    public PlayerBanner setBannerData(List<? extends BannerData> data) {

        this.mBannerData.clear();
        if (data != null) {
            this.mBannerData.addAll(data);
        }
        this.mCount = mBannerData.size();

        return this;
    }

    /**
     * 设置url形式的轮播数据
     *
     * @param data
     * @return
     */
    public PlayerBanner setBannerDataWithUrls(List<String> data) {
        ArrayList<UrlBannerData> urlBannerData = null;
        if (data != null) {
            urlBannerData = new ArrayList<>();
            for (String url : data) {
                urlBannerData.add(new UrlBannerData(url));
            }
        }
        setBannerData(urlBannerData);
        return this;
    }

    /**
     * 更新数据
     *
     * @param bannerData
     */
    public void update(List<? extends BannerData> bannerData) {
        // 先停止当前播放的
        releaseAllBannerPlayerView();
        setBannerData(bannerData);
        prepare();
        //resumePlay();
    }

    /**
     * 更新数据
     *
     * @param bannerData
     */
    public void updateWithUrls(List<String> bannerData) {
        // 先停止当前播放的
        releaseAllBannerPlayerView();
        setBannerDataWithUrls(bannerData);
        prepare();
        //resumePlay();
    }

    //<editor-fold desc="更新带标题的数据">

    /**
     * 设置标题数据
     *
     * @param titles
     * @return
     */
    public PlayerBanner setBannerTitles(List<String> titles) {
        this.mTitles.clear();
        if (titles != null) {
            this.mTitles.addAll(titles);
        }
        return this;
    }

    /**
     * 更新数据
     *
     * @param bannerData
     * @param titles
     */
    public void update(List<? extends BannerData> bannerData, List<String> titles) {
        // 设置标题数据
        setBannerTitles(titles);
        // 是指轮播数据
        update(bannerData);
    }

    /**
     * 更新数据
     *
     * @param bannerData
     * @param titles
     */
    public void updateWithUrls(List<String> bannerData, List<String> titles) {
        // 设置标题数据
        setBannerTitles(titles);
        // 是指轮播数据
        updateWithUrls(bannerData);
    }
    //</editor-fold>

    /**
     * 准备
     *
     * @return
     */
    public PlayerBanner prepare() {

        applyBannerStyleUI();
        initIndicators();

        if (mGravity != -1) {
            mIndicator.setGravity(mGravity);
        }

        if (mCount <= 0) {
            mBannerDefaultImage.setVisibility(VISIBLE);
            Log.e(tag, "The image data set is empty.");
        } else {
            mBannerDefaultImage.setVisibility(GONE);
        }

        mPlayAttrs.count = mCount;

        mAdapter = new LoopAdapter();
        mViewPager.setScrollable(mAllowScroll && mCount > 1);
        mPlayAttrs.playing = true;
        mViewPager.setAdapter(mAdapter);

        return this;
    }

    /**
     * 数据适配器
     */
    private class LoopAdapter extends SlideViewAdapter {

        private int currentIndex = 0;
        private final ArrayList<BannerPlayerView> mViewCache;
        private BannerPlayerView mCurrentPlayerView;

        public LoopAdapter() {

            mViewCache = new ArrayList<>();
        }

        public void stop() {
            for (BannerPlayerView bannerPlayerView : mViewCache) {
                bannerPlayerView.stop();
            }
        }

        public void resume() {
            if (mCurrentPlayerView != null) {
                if (!mCurrentPlayerView.isPrepared()) {
                    mCurrentPlayerView.prepare();
                }
                mCurrentPlayerView.play();
            }
        }

        public void release() {
            for (BannerPlayerView bannerPlayerView : mViewCache) {
                bannerPlayerView.release();
            }
        }

        @Override
        protected View onCreateView(Context context, ViewGroup viewGroup,
            LayoutInflater layoutInflater) {

            Log.e("fafadfa", "创建");

            if (mCount > 0) {
                ViewGroup.LayoutParams layoutParams =
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);

                final BannerPlayerView bannerPlayerView = new IjkPlayerBannerView(context);
                bannerPlayerView.getSelf().setLayoutParams(layoutParams);
                setImageViewScaleType(bannerPlayerView.getImageView());

                bannerPlayerView.setConfig(mPlayAttrs, mHandler); // 配置

                bannerPlayerView.setListener(new BannerPlayerView.PlayListener() {
                    @Override
                    public void onPlayComplete(BannerPlayerView view) {

                        removeCallbacks();

                        if (mPlayAttrs.playing) {
                            mHandler.postDelayed(mPlayNextTask, 50);
                        }
                    }

                    @Override
                    public void onPlayError(BannerPlayerView view) {
                        removeCallbacks();

                        if (mPlayAttrs.playing) {
                            mHandler.postDelayed(mPlayNextTask, mErrorDelayTime);
                        }
                    }
                });

                if (mOnBannerClickListener != null) {
                    bannerPlayerView.getSelf().setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mOnBannerClickListener.OnBannerClick(PlayerBanner.this,
                                bannerPlayerView.getIndex());
                        }
                    });
                }

                // 加到缓存那里
                mViewCache.add(bannerPlayerView);
                return bannerPlayerView.getSelf();
            } else {
                return new View(context); // 没数据就弄个空白的View
            }
        }

        @Override
        protected void onBindView(View view, SlideDirection slideDirection) {

            int index = normalize(slideDirection.moveTo(currentIndex));
            if (view instanceof BannerPlayerView) {
                BannerPlayerView bannerPlayerView = (BannerPlayerView) view;
                BannerData bannerData = mBannerData.get(index);
                bannerPlayerView.setIndex(index);
                bannerPlayerView.setData(bannerData);
                bannerPlayerView.stop();
                bannerPlayerView.prepare();
                bannerPlayerView.play();
            }

            view.setTag(R.id.bannerIndex, index);
        }

        @Override
        protected void onViewComplete(View view, SlideDirection direction) {
            if (view instanceof BannerPlayerView) {

                mCurrentPlayerView = (BannerPlayerView) view;
                mPlayAttrs.currentIndex = mCurrentPlayerView.getIndex();
                // 更新指示器
                updateIndicators(mCurrentPlayerView.getIndex());
            }

            int index = (int) view.getTag(R.id.bannerIndex);
            if (mOnBannerChangedListener != null && index > -1) {
                mOnBannerChangedListener.onPageSelected(PlayerBanner.this, index);
            }
        }

        @Override
        protected void onViewDismiss(View view, ViewGroup parent, SlideDirection direction) {
            if (view instanceof BannerPlayerView) {
                BannerPlayerView bannerPlayerView = (BannerPlayerView) view;
                bannerPlayerView.stop();
            }
        }

        @Override
        public boolean canSlideTo(SlideDirection slideDirection) {
            return mBannerData.size() > 0;
        }

        @Override
        protected void finishSlide(SlideDirection direction) {
            currentIndex = normalize(direction.moveTo(currentIndex));
        }
    }

    /**
     * 当前下标
     *
     * @param index
     * @return
     */
    private int normalize(int index) {

        if (mBannerData.size() > 0) {
            return (index + mBannerData.size()) % mBannerData.size();
        }
        return -1;
    }

    /**
     * 恢复播放
     */
    public void resumePlay() {

        if (mPlayAttrs.playing) {
            return;
        }

        mPlayAttrs.playing = true;
        removeCallbacks();
        mHandler.postDelayed(mPlayCurrentTask, 50);
    }

    /**
     * 停止播放
     */
    public void stopPlay() {
        mPlayAttrs.playing = false;
        removeCallbacks();
        stopAllBannerPlayerView();
    }

    /**
     * 释放
     */
    public void releaseBanner() {
        mPlayAttrs.playing = false;
        mHandler.removeCallbacksAndMessages(null);
        // 释放控件
        releaseAllBannerPlayerView();
    }

    /**
     * 停止轮播控件
     */
    private void stopAllBannerPlayerView() {
        if (mAdapter != null) {
            mAdapter.stop();
        }
    }

    /**
     * 释放所有的播放控件
     */
    private void releaseAllBannerPlayerView() {
        if (mAdapter != null) {
            mAdapter.release();
        }
    }

    /**
     * 执行播放下一个
     */
    private final Runnable mPlayNextTask = new Runnable() {
        @Override
        public void run() {
            mViewPager.slideTo(SlideDirection.Next);
        }
    };

    private Runnable mPlayCurrentTask = new Runnable() {
        @Override
        public void run() {
            if (mAdapter != null) {
                mAdapter.resume();
            }
        }
    };

    private void removeCallbacks() {
        mHandler.removeCallbacks(mPlayNextTask);
        mHandler.removeCallbacks(mPlayCurrentTask);
    }

    ///////
    //下面代码是用来设置控件参数，或者指示器相关代码，放下面免得看得心烦
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //

    private void handleTypedArray(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PlayerBanner);
        mIndicatorWidth =
            typedArray.getDimensionPixelSize(R.styleable.PlayerBanner_banner_indicator_width,
                mIndicatorSize);
        mIndicatorHeight =
            typedArray.getDimensionPixelSize(R.styleable.PlayerBanner_banner_indicator_height,
                mIndicatorSize);
        mIndicatorMargin =
            typedArray.getDimensionPixelSize(R.styleable.PlayerBanner_banner_indicator_margin,
                BannerConfig.PADDING_SIZE);
        mIndicatorSelectedResId =
            typedArray.getResourceId(R.styleable.PlayerBanner_banner_indicator_drawable_selected,
                R.drawable.gray_radius);
        mIndicatorUnselectedResId =
            typedArray.getResourceId(R.styleable.PlayerBanner_banner_indicator_drawable_unselected,
                R.drawable.white_radius);
        mScaleType =
            typedArray.getInt(R.styleable.PlayerBanner_banner_image_scale_type, mScaleType);
        mDelayTime =
            typedArray.getInt(R.styleable.PlayerBanner_banner_delay_time, BannerConfig.TIME);
        mErrorDelayTime =
            typedArray.getInt(R.styleable.PlayerBanner_banner_error_delay_time, BannerConfig.TIME);
        mPlayAttrs.playDuration = mDelayTime;

        mTitleBackground = typedArray.getColor(R.styleable.PlayerBanner_banner_title_background,
            BannerConfig.TITLE_BACKGROUND);
        mTitleHeight =
            typedArray.getDimensionPixelSize(R.styleable.PlayerBanner_banner_title_height,
                BannerConfig.TITLE_HEIGHT);
        mTitleTextColor = typedArray.getColor(R.styleable.PlayerBanner_banner_title_text_color,
            BannerConfig.TITLE_TEXT_COLOR);
        mTitleTextSize =
            typedArray.getDimensionPixelSize(R.styleable.PlayerBanner_banner_title_text_size,
                BannerConfig.TITLE_TEXT_SIZE);
        mLayoutResId =
            typedArray.getResourceId(R.styleable.PlayerBanner_banner_override_layout, mLayoutResId);
        mBannerBackgroundImage =
            typedArray.getResourceId(R.styleable.PlayerBanner_banner_empty_image,
                R.drawable.no_banner);
        mAllowScroll = typedArray.getBoolean(R.styleable.PlayerBanner_banner_allow_scroll, false);

        // 强制播放时长
        mPlayAttrs.forcePlayDuration =
            typedArray.getBoolean(R.styleable.PlayerBanner_banner_force_show_duration, false);

        mPlayAttrs.overrideImageScale =
            typedArray.getFloat(R.styleable.PlayerBanner_banner_override_image_scale, 1.0f);

        typedArray.recycle();
    }

    /**
     * 设置播放时间（默认为图片的时间）
     *
     * @param delayTime
     * @return
     */
    public PlayerBanner setDelayTime(int delayTime) {
        this.mDelayTime = delayTime;
        mPlayAttrs.playDuration = this.mDelayTime;
        return this;
    }

    /**
     * 强制播放时间（视频无论多长，都跟图片一样显示相同的时间）
     *
     * @param forcePlayDuration
     * @return
     */
    public PlayerBanner setForceShowDuration(boolean forcePlayDuration) {
        mPlayAttrs.forcePlayDuration = forcePlayDuration;
        return this;
    }

    /**
     * 设置发生错误时，等待的时间
     *
     * @param errorDelayTime
     */
    public void setErrorDelayTime(int errorDelayTime) {
        mErrorDelayTime = errorDelayTime;
    }

    /**
     * 设置指示器位置
     *
     * @param type
     * @return
     */
    public PlayerBanner setIndicatorGravity(@BannerConfig.Gravity int type) {
        switch (type) {
            case BannerConfig.LEFT:
                this.mGravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
                break;
            case BannerConfig.CENTER:
                this.mGravity = Gravity.CENTER;
                break;
            case BannerConfig.RIGHT:
                this.mGravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
                break;
        }
        return this;
    }

    /**
     * 设置指示器样式
     *
     * @param bannerIndicatorStyle
     * @return
     */
    public PlayerBanner setBannerIndicatorStyle(@BannerConfig.Indicator int bannerIndicatorStyle) {
        this.mBannerIndicatorStyle = bannerIndicatorStyle;
        return this;
    }

    public PlayerBanner setViewPagerScrollable(boolean allowScroll) {
        this.mAllowScroll = allowScroll;
        mViewPager.setScrollable(allowScroll);
        return this;
    }

    private void applyBannerStyleUI() {
        int visibility = mCount > 1 ? View.VISIBLE : View.GONE;
        switch (mBannerIndicatorStyle) {
            case BannerConfig.CIRCLE_INDICATOR:
                mIndicator.setVisibility(visibility);
                break;
            case BannerConfig.NUM_INDICATOR:
                mNumIndicator.setVisibility(visibility);
                break;
            case BannerConfig.NUM_INDICATOR_TITLE:
                mNumIndicatorInside.setVisibility(visibility);
                applyTitleStyleUI();
                break;
            case BannerConfig.CIRCLE_INDICATOR_TITLE:
                mIndicator.setVisibility(visibility);
                applyTitleStyleUI();
                break;
            case BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE:
                mIndicatorInside.setVisibility(visibility);
                applyTitleStyleUI();
                break;
        }
    }

    private void applyTitleStyleUI() {
        if (mTitles.size() != mBannerData.size()) {
            throw new RuntimeException(
                "[Banner] --> The number of mTitles and images is different");
        }
        if (mTitleBackground != -1) {
            mTitleView.setBackgroundColor(mTitleBackground);
        }
        if (mTitleHeight != -1) {
            mTitleView.setLayoutParams(
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mTitleHeight));
        }
        if (mTitleTextColor != -1) {
            mBannerTitle.setTextColor(mTitleTextColor);
        }
        if (mTitleTextSize != -1) {
            mBannerTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTitleTextSize);
        }
        if (mTitles != null && mTitles.size() > 0) {
            mBannerTitle.setText(mTitles.get(0));
            mBannerTitle.setVisibility(View.VISIBLE);
            mTitleView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 初始化指示器
     */
    private void initIndicators() {
        if (mBannerIndicatorStyle == BannerConfig.CIRCLE_INDICATOR
            || mBannerIndicatorStyle == BannerConfig.CIRCLE_INDICATOR_TITLE
            || mBannerIndicatorStyle == BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE) {
            createIndicator();
        } else if (mBannerIndicatorStyle == BannerConfig.NUM_INDICATOR_TITLE) {
            mNumIndicatorInside.setText("1/" + mCount);
        } else if (mBannerIndicatorStyle == BannerConfig.NUM_INDICATOR) {
            mNumIndicator.setText("1/" + mCount);
        }
    }

    /**
     * 设置ImageView缩放方式
     *
     * @param imageView
     */
    private void setImageViewScaleType(View imageView) {
        if (imageView instanceof ImageView) {
            ImageView view = ((ImageView) imageView);
            switch (mScaleType) {
                case 0:
                    view.setScaleType(ScaleType.CENTER);
                    break;
                case 1:
                    view.setScaleType(ScaleType.CENTER_CROP);
                    break;
                case 2:
                    view.setScaleType(ScaleType.CENTER_INSIDE);
                    break;
                case 3:
                    view.setScaleType(ScaleType.FIT_CENTER);
                    break;
                case 4:
                    view.setScaleType(ScaleType.FIT_END);
                    break;
                case 5:
                    view.setScaleType(ScaleType.FIT_START);
                    break;
                case 6:
                    view.setScaleType(ScaleType.FIT_XY);
                    break;
                case 7:
                    view.setScaleType(ScaleType.MATRIX);
                    break;
            }
        }
    }

    /**
     * 创建指示器
     */
    private void createIndicator() {
        mIndicatorImages.clear();
        mIndicator.removeAllViews();
        mIndicatorInside.removeAllViews();
        for (int i = 0; i < mCount; i++) {
            ImageView imageView = new ImageView(mContext);
            imageView.setScaleType(ScaleType.CENTER_CROP);
            LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(mIndicatorWidth, mIndicatorHeight);
            params.leftMargin = mIndicatorMargin;
            params.rightMargin = mIndicatorMargin;
            if (i == 0) {
                imageView.setImageResource(mIndicatorSelectedResId);
            } else {
                imageView.setImageResource(mIndicatorUnselectedResId);
            }
            mIndicatorImages.add(imageView);
            if (mBannerIndicatorStyle == BannerConfig.CIRCLE_INDICATOR
                || mBannerIndicatorStyle == BannerConfig.CIRCLE_INDICATOR_TITLE) {
                mIndicator.addView(imageView, params);
            } else if (mBannerIndicatorStyle == BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE) {
                mIndicatorInside.addView(imageView, params);
            }
        }
    }

    /**
     * 更新指示器
     *
     * @param position
     */
    private void updateIndicators(int position) {
        if (mCount > 0) {

            int realDataPos = position;

            if (mBannerIndicatorStyle == BannerConfig.CIRCLE_INDICATOR
                || mBannerIndicatorStyle == BannerConfig.CIRCLE_INDICATOR_TITLE
                || mBannerIndicatorStyle == BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE) {

                for (int i = 0; i < mIndicatorImages.size(); i++) {
                    if (i == realDataPos) {
                        mIndicatorImages.get(i).setImageResource(mIndicatorSelectedResId);
                    } else {
                        mIndicatorImages.get(i).setImageResource(mIndicatorUnselectedResId);
                    }
                }
            }

            switch (mBannerIndicatorStyle) {
                case BannerConfig.CIRCLE_INDICATOR:
                    break;
                case BannerConfig.NUM_INDICATOR:
                    mNumIndicator.setText((realDataPos + 1) + "/" + mCount);
                    break;
                case BannerConfig.NUM_INDICATOR_TITLE:
                    mNumIndicatorInside.setText((realDataPos + 1) + "/" + mCount);
                    mBannerTitle.setText(mTitles.get(realDataPos));
                    break;
                case BannerConfig.CIRCLE_INDICATOR_TITLE:
                    mBannerTitle.setText(mTitles.get(realDataPos));
                    break;
                case BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE:
                    mBannerTitle.setText(mTitles.get(realDataPos));
                    break;
            }
        }
    }

    public PlayerBanner setOnBannerClickListener(OnBannerClickListener listener) {
        this.mOnBannerClickListener = listener;
        return this;
    }

    public PlayerBanner setOnBannerChangedListener(OnBannerChangedListener listener) {
        mOnBannerChangedListener = listener;
        return this;
    }
}
