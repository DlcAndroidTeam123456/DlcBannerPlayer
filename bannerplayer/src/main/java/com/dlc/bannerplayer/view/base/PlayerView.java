package com.dlc.bannerplayer.view.base;

public interface PlayerView {

    interface Listener {

        void onPrepared(PlayerView playerView);

        void onCompleted(PlayerView playerView);

        void onError(PlayerView playerView);
    }

    void setListener(Listener listener);

    void setData(String url);

    void seekTo(int msec);

    /**
     * 准备加载图片/视频
     */
    void prepare();

    /**
     * 播放
     */
    void play();

    /**
     * 暂停
     */
    void pause();

    /**
     * 恢复
     */
    void resume();

    /**
     * 停止
     */
    void stop();

    /**
     * 销毁
     */
    void release();

    /**
     * 是否已经开始播放（含播放和暂停）
     *
     * @return
     */
    boolean isStarted();

    /**
     * 是否在正在播放
     *
     * @return
     */
    boolean isPlaying();

    boolean isPrepared();
}
