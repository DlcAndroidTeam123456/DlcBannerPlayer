package com.dlc.bannerplayer.data;

import org.apache.commons.lang3.StringUtils;

public class UrlBannerData implements BannerData {

    private final String mUrl;
    private final int mType;

    public UrlBannerData(String url) {
        if (url == null) {
            mUrl = "";
        } else {
            mUrl = url;
        }

        if (isImage(mUrl)) {
            mType = TYPE_IMAGE;
        } else if (isVideo(mUrl)) {
            mType = TYPE_VIDEO;
        } else {
            mType = TYPE_UNKNOWN;
        }
    }

    @Override
    public String getUrl() {
        return mUrl;
    }

    @Override
    public int getType() {
        return mType;
    }

    private boolean isImage(String url) {
        return StringUtils.containsIgnoreCase(url, ".jpg") || StringUtils.containsIgnoreCase(url,
            ".png") || StringUtils.containsIgnoreCase(url, ".bmp");
    }

    private boolean isVideo(String url) {
        return StringUtils.containsIgnoreCase(url, ".mp4") || StringUtils.containsIgnoreCase(url,
            ".avi") || StringUtils.containsIgnoreCase(url, ".wmv");
    }
}
