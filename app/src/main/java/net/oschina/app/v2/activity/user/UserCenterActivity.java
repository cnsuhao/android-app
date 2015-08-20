package net.oschina.app.v2.activity.user;

import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.github.ksoichiro.android.observablescrollview.Scrollable;
import com.github.ksoichiro.android.observablescrollview.TouchInterceptionFrameLayout;
import com.nineoldandroids.view.ViewHelper;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.activity.user.adapter.NavigationAdapter;
import net.oschina.app.v2.activity.user.view.SlidingTabLayout;
import net.oschina.app.v2.base.BaseActivity;

/**
 * Created by Tonlin on 2015/8/20.
 */
public class UserCenterActivity extends BaseActivity implements ObservableScrollViewCallbacks {

    private static final float MAX_TEXT_SCALE_DELTA = 0.3f;
    private static final String TAG = "UCenter";

    private View mRlInfo;
    private View mImageView;
    private View mOverlayView;
    private ImageView mIvAvatar;
    private TextView mTitleView;
    private TouchInterceptionFrameLayout mInterceptionLayout;
    private ViewPager mPager;
    private NavigationAdapter mPagerAdapter;
    private int mSlop;
    private int mFlexibleSpaceHeight;
    private int mTabHeight;
    private boolean mScrolled;

    @Override
    protected int getLayoutId() {
        return R.layout.v2_activity_user_center_3;
    }

    @Override
    protected boolean hasBackButton() {
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_center_menu, menu);
        return true;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        ViewCompat.setElevation(findViewById(R.id.header), getResources().getDimension(R.dimen.toolbar_elevation));
        mPagerAdapter = new NavigationAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.view_pager);
        mPager.setAdapter(mPagerAdapter);
        mImageView = findViewById(R.id.image);
        mOverlayView = findViewById(R.id.overlay);
        // Padding for ViewPager must be set outside the ViewPager itself
        // because with padding, EdgeEffect of ViewPager become strange.
        mFlexibleSpaceHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_image_height)
                - getResources().getDimensionPixelSize(R.dimen.actionbar_size);
        mTabHeight = getResources().getDimensionPixelSize(R.dimen.tab_height);
        findViewById(R.id.pager_wrapper).setPadding(0, mFlexibleSpaceHeight
                + getResources().getDimensionPixelSize(R.dimen.actionbar_size), 0, 0);
        mTitleView = (TextView) findViewById(R.id.title);
        mTitleView.setText(getTitle());

        mRlInfo = findViewById(R.id.rl_info);
        mIvAvatar = (ImageView) findViewById(R.id.iv_avatar);
        setTitle(null);

        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slidingTabLayout.setCustomTabView(R.layout.v2_tab_indicator_uc, android.R.id.text1);
        slidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.white));
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(mPager);
        ((FrameLayout.LayoutParams) slidingTabLayout.getLayoutParams()).topMargin = mFlexibleSpaceHeight - mTabHeight
                + getResources().getDimensionPixelSize(R.dimen.actionbar_size);

        ViewConfiguration vc = ViewConfiguration.get(this);
        mSlop = vc.getScaledTouchSlop() * 2;
        mInterceptionLayout = (TouchInterceptionFrameLayout) findViewById(R.id.container);
        mInterceptionLayout.setScrollInterceptionListener(mInterceptionListener);
        ScrollUtils.addOnGlobalLayoutListener(mInterceptionLayout, new Runnable() {
            @Override
            public void run() {
                updateFlexibleSpace();
            }
        });
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
    }

    private TouchInterceptionFrameLayout.TouchInterceptionListener mInterceptionListener = new TouchInterceptionFrameLayout.TouchInterceptionListener() {
        @Override
        public boolean shouldInterceptTouchEvent(MotionEvent ev, boolean moving, float diffX, float diffY) {
            if (!mScrolled && mSlop < Math.abs(diffX) && Math.abs(diffY) < Math.abs(diffX)) {
                // Horizontal scroll is maybe handled by ViewPager
                return false;
            }

            Scrollable scrollable = getCurrentScrollable();
            if (scrollable == null) {
                mScrolled = false;
                return false;
            }


            // If interceptionLayout can move, it should intercept.
            // And once it begins to move, horizontal scroll shouldn't work any longer.
            int flexibleSpace = mFlexibleSpaceHeight - mTabHeight;
            int translationY = (int) ViewHelper.getTranslationY(mInterceptionLayout);
            boolean scrollingUp = 0 < diffY;
            boolean scrollingDown = diffY < 0;
            if (scrollingUp) {
                // Log.e(TAG,"up y:"+((ListView)scrollable).getScrollY());
                if (translationY < 0 && getScrollY(scrollable) == 0) {
                    Log.e(TAG, "scrollingUp translationY < 0 return true:" + translationY);
                    mScrolled = true;
                    return true;
                }
                Log.e(TAG, "scrollingUp translationY >= 0 return false:" + translationY);
            } else if (scrollingDown) {
                if (-flexibleSpace < translationY) {
                    Log.e(TAG, "scrollingDown -flexibleSpace < translationY return true:" + scrollable.getCurrentScrollY());
                    mScrolled = true;
                    return true;
                }
                Log.e(TAG, "scrollingDown translationY >= 0 return false:" + scrollable.getCurrentScrollY());
            }
            mScrolled = false;
            return false;
        }

        public int getScrollY(Scrollable scrollable) {
            View c = ((ListView) scrollable).getChildAt(0);
            if (c == null) {
                return 0;
            }
            int firstVisiblePosition = ((ListView) scrollable).getFirstVisiblePosition();
            int top = c.getTop();
            return -top + firstVisiblePosition * c.getHeight();
        }

        @Override
        public void onDownMotionEvent(MotionEvent ev) {
        }

        @Override
        public void onMoveMotionEvent(MotionEvent ev, float diffX, float diffY) {
            int flexibleSpace = mFlexibleSpaceHeight - mTabHeight;
            float translationY = ScrollUtils.getFloat(ViewHelper.getTranslationY(mInterceptionLayout) + diffY, -flexibleSpace, 0);
            updateFlexibleSpace(translationY);
            if (translationY < 0) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mInterceptionLayout.getLayoutParams();
                lp.height = (int) (-translationY + getScreenHeight());
                mInterceptionLayout.requestLayout();
            }
        }

        @Override
        public void onUpOrCancelMotionEvent(MotionEvent ev) {
            mScrolled = false;
        }
    };

    private Scrollable getCurrentScrollable() {
        Fragment fragment = getCurrentFragment();
        if (fragment == null) {
            return null;
        }
        View view = fragment.getView();
        if (view == null) {
            return null;
        }
        return (Scrollable) view.findViewById(R.id.scroll);
    }

    private void updateFlexibleSpace() {
        updateFlexibleSpace(ViewHelper.getTranslationY(mInterceptionLayout));
    }

    private void updateFlexibleSpace(float translationY) {
        ViewHelper.setTranslationY(mInterceptionLayout, translationY);
        int minOverlayTransitionY = getActionBarSize() - mOverlayView.getHeight();
        ViewHelper.setTranslationY(mImageView, ScrollUtils.getFloat(-translationY / 2, minOverlayTransitionY, 0));

        // Change alpha of overlay
        float flexibleRange = mFlexibleSpaceHeight - getActionBarSize();
        ViewHelper.setAlpha(mOverlayView, ScrollUtils.getFloat(-translationY / flexibleRange, 0, 1));

        // Scale title text
        float scale = 1 + ScrollUtils.getFloat((flexibleRange + translationY - mTabHeight) / flexibleRange, 0, MAX_TEXT_SCALE_DELTA);
        setPivotXToTitle();
        //ViewHelper.setPivotY(mTitleView, 0);
        //ViewHelper.setScaleX(mTitleView, scale);
        //ViewHelper.setScaleY(mTitleView, scale);
        int totalX = ((findViewById(android.R.id.content).getWidth() / 2) - mTitleView.getWidth() / 2);
        int moveX = (int) (totalX - totalX * ((-translationY) / mFlexibleSpaceHeight));
        int minMoveX = getResources().getDimensionPixelSize(R.dimen.title_margin_left);
        moveX = moveX < minMoveX ? minMoveX : moveX;
        ViewHelper.setTranslationX(mTitleView, moveX);
        //Log.e(TAG, "flexibleRange:" + flexibleRange + " transY:" + translationY + " tabHeight:" + mTabHeight + " scale :" + scale);

        // change avatar
        ViewHelper.setAlpha(mIvAvatar, 1 - ScrollUtils.getFloat(-translationY / flexibleRange, 0, 1));
        ViewHelper.setAlpha(mRlInfo, 1 - ScrollUtils.getFloat(-translationY / flexibleRange, 0, 1));
    }

    private Fragment getCurrentFragment() {
        return mPagerAdapter.getItemAt(mPager.getCurrentItem());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void setPivotXToTitle() {
        Configuration config = getResources().getConfiguration();
        if (Build.VERSION_CODES.JELLY_BEAN_MR1 <= Build.VERSION.SDK_INT
                && config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            ViewHelper.setPivotX(mTitleView, findViewById(android.R.id.content).getWidth());
        } else {
            ViewHelper.setPivotX(mTitleView, 0);
        }
    }
}
