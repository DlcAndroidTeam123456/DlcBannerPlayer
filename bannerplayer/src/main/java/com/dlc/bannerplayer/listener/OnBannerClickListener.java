package com.dlc.bannerplayer.listener;

import com.dlc.bannerplayer.PlayerBanner;

public interface OnBannerClickListener {

    /**
     * 下标从0开始
     *
     * @param banner
     * @param position
     */
    void OnBannerClick(PlayerBanner banner, int position);
}
