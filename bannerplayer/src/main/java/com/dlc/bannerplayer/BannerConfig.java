package com.dlc.bannerplayer;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface BannerConfig {
    /**
     * indicator style
     */
    int NOT_INDICATOR = 0;
    int CIRCLE_INDICATOR = 1;
    int NUM_INDICATOR = 2;
    int NUM_INDICATOR_TITLE = 3;
    int CIRCLE_INDICATOR_TITLE = 4;
    int CIRCLE_INDICATOR_TITLE_INSIDE = 5;

    @IntDef({
        NOT_INDICATOR, CIRCLE_INDICATOR, NUM_INDICATOR, NUM_INDICATOR_TITLE, CIRCLE_INDICATOR_TITLE,
        CIRCLE_INDICATOR_TITLE_INSIDE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Indicator {
    }

    /**
     * indicator gravity
     */
    int LEFT = 5;
    int CENTER = 6;
    int RIGHT = 7;

    @IntDef({
        LEFT, CENTER, RIGHT
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Gravity {
    }

    /**
     * banner
     */
    int PADDING_SIZE = 5;
    int TIME = 2000;
    int DURATION = 800;
    boolean IS_AUTO_PLAY = true;
    boolean IS_SCROLL = true;

    /**
     * title style
     */
    int TITLE_BACKGROUND = -1;
    int TITLE_HEIGHT = -1;
    int TITLE_TEXT_COLOR = -1;
    int TITLE_TEXT_SIZE = -1;
}
