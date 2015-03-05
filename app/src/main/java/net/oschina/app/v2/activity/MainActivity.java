package net.oschina.app.v2.activity;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.base.BaseActivity;
import net.oschina.app.v2.base.Constants;
import net.oschina.app.v2.model.User;
import net.oschina.app.v2.service.NoticeUtils;
import net.oschina.app.v2.ui.BadgeView;
import net.oschina.app.v2.ui.tab.SlidingTabLayout;
import net.oschina.app.v2.ui.tab.SlidingTabPagerAdapter;
import net.oschina.app.v2.utils.TLog;
import net.oschina.app.v2.utils.UIHelper;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ListPopupWindow;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
//import android.view.ViewPropertyAnimator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.github.ksoichiro.android.observablescrollview.Scrollable;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tonlin.osc.happy.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

/**
 * 应用主界面
 * 
 * @author tonlin
 * @since 2014/08
 */
public class MainActivity extends BaseActivity implements OnTabChangeListener,
		OnItemClickListener , ObservableScrollViewCallbacks {

	private static final String MAIN_SCREEN = "MainScreen";
	private FragmentTabHost mTabHost;
	private MenuAdapter mMenuAdapter;
	private ListPopupWindow mMenuWindow;

	// private Version mVersion;
	private BadgeView mBvTweet;

    private View mHeaderView;
    private View mToolbarView;
    private int mBaseTranslationY;

	private BroadcastReceiver mNoticeReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			int atmeCount = intent.getIntExtra("atmeCount", 0);// @我
			int msgCount = intent.getIntExtra("msgCount", 0);// 留言
			int reviewCount = intent.getIntExtra("reviewCount", 0);// 评论
			int newFansCount = intent.getIntExtra("newFansCount", 0);// 新粉丝
			int activeCount = atmeCount + reviewCount + msgCount;// +
																	// newFansCount;//
																	// 信息总数

			TLog.log("@me:" + atmeCount + " msg:" + msgCount + " review:"
					+ reviewCount + " newFans:" + newFansCount + " active:"
					+ activeCount);

			if (activeCount > 0) {
				mBvTweet.setText(activeCount + "");
				mBvTweet.show();
			} else {
				mBvTweet.hide();
			}
		}
	};
    private SlidingTabLayout mSlidingTabLayout;

    @Override
	protected int getLayoutId() {
		return R.layout.v2_activity_main;
	}

	@Override
	protected void onResume() {
		super.onResume();
		// if (mVersion != null) {
		// UmengUpdateAgent.showUpdateDialog(getApplicationContext(),
		// mVersion.toVersion());
		// mVersion = null;
		// }

		if (mMenuAdapter != null) {
			mMenuAdapter.notifyDataSetChanged();
		}
		MobclickAgent.onPageStart(MAIN_SCREEN);
		MobclickAgent.onResume(this);
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
	
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(MAIN_SCREEN);
		MobclickAgent.onPause(this);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void init(Bundle savedInstanceState) {
		super.init(savedInstanceState);
		checkUpdate();
		// Intent intent = getIntent();
		// if (intent != null) {
		// mVersion = intent.getParcelableExtra(Version.BUNDLE_KEY_VERSION);
		// }

		AppContext.instance().initLoginInfo();

        mHeaderView = findViewById(R.id.header);
        //ViewCompat.setElevation(mHeaderView, getResources().getDimension(R.dimen.toolbar_elevation));
        mToolbarView = findViewById(R.id.actionBar);

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
	}

	@Override
	protected void onDestroy() {
		NoticeUtils.unbindFromService(this);
		unregisterReceiver(mNoticeReceiver);
		NoticeUtils.tryToShutDown(this);
		super.onDestroy();
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
		}
		supportInvalidateOptionsMenu();
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
		case R.id.main_menu_more:
			showMoreOptionMenu(findViewById(R.id.main_menu_more));
			break;
		}
		return true;
	}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_MENU) {
            if(mMenuWindow != null && mMenuWindow.isShowing()){
                mMenuWindow.dismiss();
            } else {
                showMoreOptionMenu(findViewById(R.id.main_menu_more));
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private long mLastExitTime;

	@Override
	public void onBackPressed() {
		if (System.currentTimeMillis() - mLastExitTime < 2000) {
			super.onBackPressed();
		} else {
			mLastExitTime = System.currentTimeMillis();
			AppContext.showToastShort(R.string.tip_click_back_again_to_exist);
		}
	}

	private void showMoreOptionMenu(View view) {
		if (mMenuWindow != null) {
			mMenuWindow.dismiss();
			mMenuWindow = null;
		}
		mMenuWindow = new ListPopupWindow(this);
		if (mMenuAdapter == null) {
			mMenuAdapter = new MenuAdapter();
		}
		mMenuWindow.setModal(true);
		mMenuWindow.setContentWidth(getResources().getDimensionPixelSize(
				R.dimen.popo_menu_dialog_width));
		mMenuWindow.setAdapter(mMenuAdapter);
		mMenuWindow.setOnItemClickListener(this);
		mMenuWindow.setAnchorView(view);
		mMenuWindow.show();
		mMenuWindow.getListView().setDividerHeight(1);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		switch (position) {
		case 0:
			if (AppContext.instance().isLogin()) {
				UIHelper.showUserInfo(this);
			} else {
				UIHelper.showLogin(this);
			}
			break;
		case 1:
			UIHelper.showSoftware(this);
			break;
		case 2:
			UIHelper.showSetting(this);
			break;
		case 3:
			UIHelper.exitApp(this);
			break;
		default:
			break;
		}
		if (mMenuWindow != null) {
			mMenuWindow.dismiss();
			mMenuWindow = null;
		}
	}

    public SlidingTabLayout getSlidingTabLayout() {
        return mSlidingTabLayout;
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        if (dragging) {
            int toolbarHeight = mToolbarView.getHeight();
            float currentHeaderTranslationY = ViewHelper.getTranslationY(mHeaderView);
            if (firstScroll) {
                if (-toolbarHeight < currentHeaderTranslationY) {
                    mBaseTranslationY = scrollY;
                }
            }
            float headerTranslationY = ScrollUtils.getFloat(-(scrollY - mBaseTranslationY), -toolbarHeight, 0);
            ViewPropertyAnimator.animate(mHeaderView).cancel();
            ViewHelper.setTranslationY(mHeaderView, headerTranslationY);
        }
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        mBaseTranslationY = 0;

        Fragment fragment = getCurrentFragment();
        if (fragment == null) {
            return;
        }
        View view = fragment.getView();
        if (view == null) {
            return;
        }

        // ObservableXxxViews have same API
        // but currently they don't have any common interfaces.
        adjustToolbar(scrollState, view);
    }

    private void adjustToolbar(ScrollState scrollState, View view) {
        int toolbarHeight = mToolbarView.getHeight();
        final Scrollable scrollView = (Scrollable) view.findViewById(R.id.recycleView);
        if (scrollView == null) {
            return;
        }
        int scrollY = scrollView.getCurrentScrollY();
        if (scrollState == ScrollState.DOWN) {
            showToolbar();
        } else if (scrollState == ScrollState.UP) {
            if (toolbarHeight <= scrollY) {
                hideToolbar();
            } else {
                showToolbar();
            }
        } else {
            // Even if onScrollChanged occurs without scrollY changing, toolbar should be adjusted
            if (toolbarIsShown() || toolbarIsHidden()) {
                // Toolbar is completely moved, so just keep its state
                // and propagate it to other pages
                propagateToolbarState(toolbarIsShown());
            } else {
                // Toolbar is moving but doesn't know which to move:
                // you can change this to hideToolbar()
                showToolbar();
            }
        }
    }

    private Fragment getCurrentFragment() {
        MainTab[] tabs = MainTab.values();

        MainTab tab = tabs[mTabHost.getCurrentTab()];

        return getSupportFragmentManager().findFragmentByTag(getString(tab.getResName()));
    }

    private void propagateToolbarState(boolean isShown) {
        int toolbarHeight = mToolbarView.getHeight();

        Fragment fragment = getCurrentFragment();
        if(fragment != null && fragment instanceof IPagerFragment) {
            IPagerFragment fc = (IPagerFragment)fragment;
            SlidingTabPagerAdapter mPagerAdapter = fc.getPagerAdapter();
            ViewPager mPager = fc.getViewPager();

            // Set scrollY for the fragments that are not created yet
            mPagerAdapter.setScrollY(isShown ? 0 : toolbarHeight);

            // Set scrollY for the active fragments
            for (int i = 0; i < mPagerAdapter.getCount(); i++) {
                // Skip current item
                if (i == mPager.getCurrentItem()) {
                    continue;
                }

                // Skip destroyed or not created item
                Fragment f = mPagerAdapter.getItemAt(i);
                if (f == null) {
                    continue;
                }

                View view = f.getView();
                if (view == null) {
                    continue;
                }
                propagateToolbarState(isShown, view, toolbarHeight);
            }
        }
    }

    private void propagateToolbarState(boolean isShown, View view, int toolbarHeight) {
        Scrollable scrollView = (Scrollable) view.findViewById(R.id.recycleView);
        if (scrollView == null) {
            return;
        }
        if (isShown) {
            // Scroll up
            if (0 < scrollView.getCurrentScrollY()) {
                scrollView.scrollVerticallyTo(0);
            }
        } else {
            // Scroll down (to hide padding)
            if (scrollView.getCurrentScrollY() < toolbarHeight) {
                scrollView.scrollVerticallyTo(toolbarHeight);
            }
        }
    }

    private boolean toolbarIsShown() {
        return ViewHelper.getTranslationY(mHeaderView) == 0;
    }

    private boolean toolbarIsHidden() {
        return ViewHelper.getTranslationY(mHeaderView) == -mToolbarView.getHeight();
    }

    private void showToolbar() {
        float headerTranslationY = ViewHelper.getTranslationY(mHeaderView);
        if (headerTranslationY != 0) {
            ViewPropertyAnimator.animate(mHeaderView).cancel();
            ViewPropertyAnimator.animate(mHeaderView).translationY(0).setDuration(200).start();
        }
        propagateToolbarState(true);
    }

    private void hideToolbar() {
        float headerTranslationY = ViewHelper.getTranslationY(mHeaderView);
        int toolbarHeight = mToolbarView.getHeight();
        if (headerTranslationY != -toolbarHeight) {
            ViewPropertyAnimator.animate(mHeaderView).cancel();
            ViewPropertyAnimator.animate(mHeaderView).translationY(-toolbarHeight).setDuration(200).start();
        }
        propagateToolbarState(false);
    }



}
