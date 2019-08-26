package com.licheedev.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.dlc.bannerplayer.BannerConfig;
import com.dlc.bannerplayer.PlayerBanner;
import com.dlc.bannerplayer.WeakHandler;
import com.dlc.bannerplayer.data.BannerData;
import com.dlc.bannerplayer.data.UrlBannerData;
import com.dlc.bannerplayer.view.IjkPlayerBannerView;
import com.dlc.bannerplayer.view.PlayAttrs;
import com.dlc.bannerplayer.view.base.BannerPlayerView;
import com.yy.mobile.widget.SlideDirection;
import com.yy.mobile.widget.SlideViewAdapter;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.banner)
    PlayerBanner mBanner;
    @BindView(R.id.btn_reset)
    Button mBtnReset;
    private PlayAttrs mPlayAttrs = new PlayAttrs();
    private WeakHandler mHandler = new WeakHandler();
    private ArrayList<BannerData> mBannerData;
    private String[] mStrings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //mStrings = new String[] {
        //    "http://szykkfj.app.xiaozhuschool.com/public/uploads/imgs/20190525/d9ec36d48962cef4f07363c5de838920.jpg",
        //    "http://szykkfj.app.xiaozhuschool.com/public/uploads/imgs/20190525/b615b496832755301fb82d0042cb6fff.jpg",
        //    "http://szykkfj.app.xiaozhuschool.com/public/uploads/imgs/20190328/ea7a173059bf15698d18ad2000ea646e.jpg",
        //    "http://szykkfj.app.xiaozhuschool.com/public/uploads/imgs/20190525/9b74e55ac9e2f7a18d77bb4b92f1ab4a.jpg",
        //    "http://szykkfj.app.xiaozhuschool.com/public/uploads/imgs/20190529/6a55dc85757ec2c0da23b98d006e0af2.jpg",
        //    "http://szykkfj.app.xiaozhuschool.com/public/uploads/imgs/20190529/48d703da5aef894206ba9790522c3453.jpg",
        //    "http://szykkfj.app.xiaozhuschool.com/public/uploads/imgs/20190529/c6a1488274266e7c74b444f158b99239.jpg"
        //};

        mStrings = new String[] {
            "http://hnshg.a.xiaozhuschool.com/statics/images/2019-06-17/15607536991960.mp4",
            "http://szykkfj.app.xiaozhuschool.com/public/uploads/imgs/20190328/ea7a173059bf15698d18ad2000ea646e.jpg",
            "http://mengyi.sxitdlc.com/public/uploads/imgs/20190404/a1b3590fc03a5a745494df1958a65925.mp4",
            "http://szykkfj.app.xiaozhuschool.com/public/uploads/imgs/20190525/9b74e55ac9e2f7a18d77bb4b92f1ab4a.jpg",
            "http://szykkfj.app.xiaozhuschool.com/public/uploads/imgs/20190529/6a55dc85757ec2c0da23b98d006e0af2.jpg",
            "http://mengyi.sxitdlc.com/public/uploads/video/20190417/402dd1e51d384aed8bf647e0c5f8262e.mp4",
            "http://mengyi.sxitdlc.com/public/uploads/video/20190417/c473f9fcef16561bd1011fb1a2921210.mp4",
            "http://szykkfj.app.xiaozhuschool.com/public/uploads/imgs/20190529/48d703da5aef894206ba9790522c3453.jpg",
            "http://mengyi.sxitdlc.com/public/uploads/video/20190429/47259e7c21e16a5ff3f48d9a22f48b77.mp4"
        };

        mBannerData = new ArrayList<>();
        for (String url : mStrings) {
            mBannerData.add(new UrlBannerData(url));
        }

        mBanner.setBannerIndicatorStyle(BannerConfig.NOT_INDICATOR);
        mBanner.setBannerData(mBannerData);
        mBanner.prepare();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBanner.stopPlay();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBanner.resumePlay();
    }

    @OnClick(R.id.btn_reset)
    public void onViewClicked() {

        int count = (int) (Math.random() * (mStrings.length + 1));

        mBanner.update(mBannerData.subList(0, count));
    }

    private class LoopAdapter extends SlideViewAdapter {

        private final ArrayList<BannerData> mBannerData;

        private int curIdx = 0;

        public LoopAdapter(String[] urls) {
            mBannerData = new ArrayList<>();
            for (String url : urls) {
                mBannerData.add(new UrlBannerData(url));
            }
        }

        @Override
        protected void onBindView(View view, SlideDirection slideDirection) {
            BannerPlayerView bannerPlayerView = (BannerPlayerView) view;
            int index = normalize(slideDirection.moveTo(curIdx));
            BannerData bannerData = mBannerData.get(index);
            bannerPlayerView.setIndex(index);
            bannerPlayerView.setData(bannerData);
            bannerPlayerView.prepare();
        }

        @NotNull
        @Override
        protected View onCreateView(Context context, ViewGroup viewGroup,
            LayoutInflater layoutInflater) {

            ViewGroup.LayoutParams layoutParams =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            IjkPlayerBannerView exoBannerPlayerView = new IjkPlayerBannerView(context);
            exoBannerPlayerView.setLayoutParams(layoutParams);
            exoBannerPlayerView.setConfig(mPlayAttrs, mHandler); // 配置
            return exoBannerPlayerView;
        }

        @Override
        protected void onViewComplete(View view, SlideDirection direction) {
            BannerPlayerView bannerPlayerView = (BannerPlayerView) view;
            bannerPlayerView.play();
        }

        @Override
        protected void onViewDismiss(View view, ViewGroup parent, SlideDirection direction) {
            BannerPlayerView bannerPlayerView = (BannerPlayerView) view;
            bannerPlayerView.stop();
        }

        @Override
        public boolean canSlideTo(SlideDirection slideDirection) {
            return true;
        }

        @Override
        protected void finishSlide(SlideDirection direction) {
            curIdx = normalize(direction.moveTo(curIdx));
        }

        private int normalize(int index) {
            return (index + mBannerData.size()) % mBannerData.size();
        }
    }
}
