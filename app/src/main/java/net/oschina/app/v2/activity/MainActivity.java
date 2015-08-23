package net.oschina.app.v2.activity;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.base.BaseActivity;
import net.oschina.app.v2.base.Constants;
import net.oschina.app.v2.service.NoticeUtils;
import net.oschina.app.v2.ui.BadgeView;
import net.oschina.app.v2.ui.tab.SlidingTabLayout;
import net.oschina.app.v2.utils.TLog;
import net.oschina.app.v2.utils.UIHelper;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.MySwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
//import android.view.ViewPropertyAnimator;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.github.ksoichiro.android.observablescrollview.Scrollable;
import com.github.ksoichiro.android.observablescrollview.TouchInterceptionFrameLayout;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.tonlin.osc.happy.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

/**
 * 应用主界面
 *
 * @author tonlin
 * @update 2015/03/08
 * @since 2014/08
 */
public class MainActivity extends BaseActivity implements OnTabChangeListener, IMainTab,
        ObservableScrollViewCallbacks, ActionBar.TabListener {

    private static final String MAIN_SCREEN = "MainScreen";
    private static final java.lang.String TAG = "MainActivity";
    private FragmentTabHost mTabHost;
    private BadgeView mBvTweet;
    private SlidingTabLayout mSlidingTabLayout;

    private Toolbar mToolbarView;

    private int mSlop;
    private boolean mScrolled;
    private ScrollState mLastScrollState;
    private TouchInterceptionFrameLayout mInterceptionLayout;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

    private BroadcastReceiver mNoticeReceiver = new BroadcastReceiver() {

        private static final String TAG = "NoticeReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            int atmeCount = intent.getIntExtra("atmeCount", 0);// @我
            int msgCount = intent.getIntExtra("msgCount", 0);// 留言
            int reviewCount = intent.getIntExtra("reviewCount", 0);// 评论
            int newFansCount = intent.getIntExtra("newFansCount", 0);// 新粉丝
            int activeCount = atmeCount + reviewCount + msgCount;// +
            // newFansCount;//
            // 信息总数

            TLog.log(TAG, "Main收到广播 @me:" + atmeCount + " msg:" + msgCount + " review:"
                    + reviewCount + " newFans:" + newFansCount + " active:"
                    + activeCount + " from:" + intent.getStringExtra("from"));

            if (activeCount > 0) {
                mBvTweet.setText(activeCount + "");
                mBvTweet.show();
            } else {
                mBvTweet.hide();
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.v2_activity_main;
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(MAIN_SCREEN);
        MobclickAgent.onResume(this);
        //supportInvalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(MAIN_SCREEN);
        MobclickAgent.onPause(this);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        checkUpdate();

        AppContext.instance().initLoginInfo();

        mToolbarView = (Toolbar) findViewById(R.id.action_bar);

        setSupportActionBar(mToolbarView);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.root);
        // Set up the drawer.
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                null, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getActivity().invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getActivity().invalidateOptionsMenu();
            }
        };

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);


        final int tabHeight = getResources().getDimensionPixelSize(R.dimen.tab_height);
        findViewById(R.id.pager_wrapper).setPadding(0, getActionBarSize() + tabHeight, 0, 0);

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
        if (Build.VERSION.SDK_INT > 10) {
            mTabHost.getTabWidget().setShowDividers(0);
        }

        initTabs();

        mTabHost.setCurrentTab(0);
        mTabHost.setOnTabChangedListener(this);

        IntentFilter filter = new IntentFilter(Constants.INTENT_ACTION_NOTICE);
        registerReceiver(mNoticeReceiver, filter);

        NoticeUtils.bindToService(this);
        UIHelper.sendBroadcastForNotice(this);

        ViewConfiguration vc = ViewConfiguration.get(this);
        mSlop = vc.getScaledTouchSlop() / 2;
        mInterceptionLayout = (TouchInterceptionFrameLayout) findViewById(R.id.container);
        mInterceptionLayout.setScrollInterceptionListener(mInterceptionListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected int getActionBarCustomView() {
        return super.getActionBarCustomView();
    }

    @Override
    protected void onDestroy() {
        NoticeUtils.unbindFromService(this);
        unregisterReceiver(mNoticeReceiver);
        NoticeUtils.tryToShutDown(this);
        super.onDestroy();
    }

    private void checkUpdate() {
        UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {

            @Override
            public void onUpdateReturned(int updateStatus,
                                         UpdateResponse updateInfo) {
                switch (updateStatus) {
                    case UpdateStatus.Yes: // has update
                        // mVersion = new Version(updateInfo);
                        UmengUpdateAgent.showUpdateDialog(getApplicationContext(),
                                updateInfo);
                        break;
                    case UpdateStatus.No: // has no update
                        break;
                    case UpdateStatus.NoneWifi: // none wifi
                        break;
                    case UpdateStatus.Timeout: // time out
                        break;
                }
            }
        });
        UmengUpdateAgent.setUpdateAutoPopup(false);
        UmengUpdateAgent.update(getApplicationContext());
    }

    private void initTabs() {
        MainTab[] tabs = MainTab.values();
        final int size = tabs.length;
        for (int i = 0; i < size; i++) {
            MainTab mainTab = tabs[i];
            TabSpec tab = mTabHost.newTabSpec(getString(mainTab.getResName()));

            View indicator = inflateView(R.layout.v2_tab_indicator);
            ImageView icon = (ImageView) indicator.findViewById(R.id.tab_icon);
            icon.setImageResource(mainTab.getResIcon());
            TextView title = (TextView) indicator.findViewById(R.id.tab_titile);
            title.setText(getString(mainTab.getResName()));
            tab.setIndicator(indicator);
            tab.setContent(new TabContentFactory() {

                @Override
                public View createTabContent(String tag) {
                    return new View(MainActivity.this);
                }
            });

            mTabHost.addTab(tab, mainTab.getClz(), null);
            if (mainTab.equals(MainTab.ME)) {
                View con = indicator.findViewById(R.id.container);
                // con.setBackgroundColor(Color.parseColor("#00ff00"));
                mBvTweet = new BadgeView(this, con);
                mBvTweet.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
                mBvTweet.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                mBvTweet.setBackgroundResource(R.drawable.tab_notification_bg);
            }
        }
    }

    @Override
    public void onTabChanged(String tabId) {
        final int size = mTabHost.getTabWidget().getTabCount();
        for (int i = 0; i < size; i++) {
            View v = mTabHost.getTabWidget().getChildAt(i);
            if (i == mTabHost.getCurrentTab()) {
                v.findViewById(R.id.tab_icon).setSelected(true);
                v.findViewById(R.id.tab_titile).setSelected(true);
            } else {
                v.findViewById(R.id.tab_icon).setSelected(false);
                v.findViewById(R.id.tab_titile).setSelected(false);
            }
            if (i == 3) {
                mSlidingTabLayout.setMessageTipVisible(View.VISIBLE);
            } else {
                mSlidingTabLayout.setMessageTipVisible(View.INVISIBLE);
            }
        }
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        boolean visible = false;
        int tab = mTabHost.getCurrentTab();
        if (tab == 1 || tab == 2) {
            visible = true;
        }
        menu.findItem(R.id.main_menu_post).setVisible(visible);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                } else {
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                }
                break;
            case R.id.main_menu_post:
                if (mTabHost.getCurrentTab() == 1) {
                    UIHelper.showQuestionPub(this);
                } else if (mTabHost.getCurrentTab() == 2) {
                    UIHelper.showTweetPub(this);
                }
                break;
            case R.id.main_menu_search:
                UIHelper.showSearch(this);
                break;
            case R.id.main_menu_today:
                UIHelper.showDailyEnglish(this);
                break;
            case R.id.main_menu_profile:
                if (AppContext.instance().isLogin()) {
                    UIHelper.showUserInfo(this);
                } else {
                    UIHelper.showLogin(this);
                }
                break;
            case R.id.main_menu_open_software:
                UIHelper.showSoftware(this);
                break;
            case R.id.main_menu_settings:
                UIHelper.showSetting(this);
                break;
            case R.id.main_menu_quit:
                UIHelper.exitApp(this);
                break;
            case R.id.main_menu_feedback:
                FeedbackAgent agent = new FeedbackAgent(this);
                agent.startFeedbackActivity();
                break;
            case R.id.main_menu_event:
                UIHelper.showEvents(this);
                break;
        }
        return true;
    }

    private long mLastExitTime;

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
            return;
        }
        if (System.currentTimeMillis() - mLastExitTime < 2000) {
            super.onBackPressed();
        } else {
            mLastExitTime = System.currentTimeMillis();
            AppContext.showToastShort(R.string.tip_click_back_again_to_exist);
        }
    }

    @Override
    public SlidingTabLayout getSlidingTabLayout() {
        return mSlidingTabLayout;
    }

    @Override
    public int getCurrentTab() {
        return mTabHost.getCurrentTab();
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        Log.e(TAG,"Main onUpOrCancelMotionEvent");
        if (!mScrolled) {
            // This event can be used only when TouchInterceptionFrameLayout
            // doesn't handle the consecutive events.
            adjustToolbar(scrollState);
        }
    }

    private Scrollable getCurrentScrollable() {
        Fragment fragment = getCurrentFragment();
        if (fragment == null) {
            return null;
        }
        View view = fragment.getView();
        if (view == null) {
            return null;
        }
        return (Scrollable) view.findViewById(R.id.recycleView);
    }

    private MySwipeRefreshLayout getCurrentRefreshLayout() {
        Fragment fragment = getCurrentFragment();
        if (fragment == null) {
            return null;
        }
        View view = fragment.getView();
        if (view == null) {
            return null;
        }
        return (MySwipeRefreshLayout) view.findViewById(R.id.srl_refresh);
    }

    private void adjustToolbar(ScrollState scrollState) {
        int toolbarHeight = mToolbarView.getHeight();
        final Scrollable scrollable = getCurrentScrollable();
        if (scrollable == null) {
            return;
        }
        int scrollY = scrollable.getCurrentScrollY();
        if (scrollState == ScrollState.DOWN) {
            Log.e(TAG,"adjustToolbar scrollState down showToolbar");
            showToolbar();
        } else if (scrollState == ScrollState.UP) {
            if (toolbarHeight <= scrollY) {
                Log.e(TAG,"adjustToolbar scrollState up toolbarHeight <= scrollY hideToolbar");
                hideToolbar();
            } else {
                Log.e(TAG,"adjustToolbar scrollState up toolbarHeight > scrollY showToolbar");
                showToolbar();
            }
        } else if (!toolbarIsShown() && !toolbarIsHidden()) {
            // Toolbar is moving but doesn't know which to move:
            // you can change this to hideToolbar()
            Log.e(TAG,"adjustToolbar !toolbarIsShown() && !toolbarIsHidden() showToolbar");
            showToolbar();
        }
    }

    private Fragment getCurrentFragment() {
        Fragment fragment = getPagerFragment();
        if (fragment != null && fragment instanceof IPagerFragment) {
            IPagerFragment fc = (IPagerFragment) fragment;

            fragment = fc.getCurrentFragment();
            return fragment;
        }
        return null;
    }

    private Fragment getPagerFragment() {
        return getSupportFragmentManager().findFragmentByTag(mTabHost.getCurrentTabTag());
    }

    private boolean toolbarIsShown() {
        return ViewHelper.getTranslationY(mInterceptionLayout) == 0;
    }

    private boolean toolbarIsHidden() {
        return ViewHelper.getTranslationY(mInterceptionLayout) == -mToolbarView.getHeight();
    }

    private boolean  mToolbarShow = true;
    private void showToolbar() {
        mToolbarShow = true;
        animateToolbar(0);
    }

    private void hideToolbar() {
        mToolbarShow = false;
        animateToolbar(-mToolbarView.getHeight());
    }

    private void animateToolbar(final float toY) {
        float layoutTranslationY = ViewHelper.getTranslationY(mInterceptionLayout);
        if (layoutTranslationY != toY) {
            ValueAnimator animator = ValueAnimator.ofFloat(ViewHelper.getTranslationY(mInterceptionLayout), toY).setDuration(200);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float translationY = (float) animation.getAnimatedValue();
                    ViewHelper.setTranslationY(mInterceptionLayout, translationY);
                    if (translationY < 0) {
                        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mInterceptionLayout.getLayoutParams();
                        lp.height = (int) (-translationY + getScreenHeight());
                        mInterceptionLayout.requestLayout();
                    }
                }
            });
            animator.start();
        }
    }


    private TouchInterceptionFrameLayout.TouchInterceptionListener mInterceptionListener = new TouchInterceptionFrameLayout.TouchInterceptionListener() {
        @Override
        public boolean shouldInterceptTouchEvent(MotionEvent ev, boolean moving, float diffX, float diffY) {



            if (!mScrolled && mSlop < Math.abs(diffX) && Math.abs(diffY) < Math.abs(diffX)) {
                // Horizontal scroll is maybe handled by ViewPager
                Log.e(TAG,"Horizontal scroll is maybe handled by ViewPager");
                return false;
            }

            Scrollable scrollable = getCurrentScrollable();
            if (scrollable == null) {
                mScrolled = false;
                return false;
            }

            // If interceptionLayout can move, it should intercept.
            // And once it begins to move, horizontal scroll shouldn't work any longer.
            int toolbarHeight = mToolbarView.getHeight();
            int translationY = (int) ViewHelper.getTranslationY(mInterceptionLayout);
            boolean scrollingUp = 0 < diffY;
            boolean scrollingDown = diffY < 0;
//            if (scrollingUp) {
//                if (translationY < 0) {
//                    mScrolled = true;
//                    mLastScrollState = ScrollState.UP;
//                    return true;
//                }
//            } else if (scrollingDown) {
//                if (-toolbarHeight < translationY) {
//                    mScrolled = true;
//                    mLastScrollState = ScrollState.DOWN;
//                    return true;
//                }
//            }
//            mScrolled = false;
//            return false;

            Log.e(TAG,"shouldInterceptTouchEvent "+translationY + ": scrollY:" + getCurrentScrollable().getCurrentScrollY() + " up:"+scrollingUp+" down:"+scrollingDown);
            if(getCurrentScrollable().getCurrentScrollY()==0 && !mToolbarShow) {
                mScrolled = true;
                mLastScrollState = ScrollState.DOWN;
                Log.e(TAG,"shouldInterceptTouchEvent return true");
                return true;
            } else {
                Log.e(TAG,"shouldInterceptTouchEvent return false");
                mScrolled = false;
                return false;
            }
        }

        @Override
        public void onDownMotionEvent(MotionEvent ev) {
        }

        @Override
        public void onMoveMotionEvent(MotionEvent ev, float diffX, float diffY) {
            float translationY = ScrollUtils.getFloat(ViewHelper.getTranslationY(mInterceptionLayout) + diffY, -mToolbarView.getHeight(), 0);
            ViewHelper.setTranslationY(mInterceptionLayout, translationY);
            if (translationY < 0) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mInterceptionLayout.getLayoutParams();
                lp.height = (int) (-translationY + getScreenHeight());
                mInterceptionLayout.requestLayout();
            }
        }

        @Override
        public void onUpOrCancelMotionEvent(MotionEvent ev) {
            Log.e(TAG,"shouldInterceptTouchEvent onUpOrCancelMotionEvent");
            mScrolled = false;
            adjustToolbar(mLastScrollState);
        }
    };

}
