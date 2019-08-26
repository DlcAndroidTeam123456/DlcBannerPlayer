package com.dlc.bannerplayer.view;

/**
 * 播放属性
 */
public class PlayAttrs {

    /**
     * 播放时长
     */
    public long playDuration;
    /**
     * 是否强制播放时长
     * true，强制无论是图片和视频，都必须播放特定的时间（视频较短的话，会重复播放）；false,图片播放特定时间，视频需要播放完毕
     */
    public boolean forcePlayDuration;
    /**
     * 当前播放条目索引
     */
    public int currentIndex = Integer.MIN_VALUE;

    /**
     * 正在播放
     */
    public boolean playing;

    /**
     * 播放个数
     */
    public int count;

    public float overrideImageScale = 1.0f;
    
}
