package com.dlc.bannerplayer.data;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface BannerData {

    /**
     * 未知类型
     */
    int TYPE_UNKNOWN = 0;

    /**
     * 图片
     */
    int TYPE_IMAGE = 1;

    /**
     * 视频
     */
    int TYPE_VIDEO = 2;

    @IntDef({ TYPE_UNKNOWN, TYPE_IMAGE, TYPE_VIDEO })
    @Retention(RetentionPolicy.SOURCE)
    @interface DataType {
    }

    String getUrl();

    @DataType
    int getType();
}
