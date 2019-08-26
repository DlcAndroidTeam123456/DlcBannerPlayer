package com.dlc.bannerplayer.listener;

import com.dlc.bannerplayer.PlayerBanner;

public interface OnBannerChangedListener {

    /**
     * 下标从0开始
     *
     * @param banner
     * @param position
     */
    void onPageSelected(PlayerBanner banner, int position);
}
