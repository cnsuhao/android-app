package net.oschina.app.v2.activity;

import net.oschina.app.R;
import net.oschina.app.v2.base.BaseActivity;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

/**
 * 应用主界面
 * @author tonlin
 * @since 2014/08
 */
public class MainActivity extends BaseActivity implements OnTabChangeListener {

	private FragmentTabHost mTabHost;

	@Override
	protected int getLayoutId() {
		return R.layout.v2_activity_main;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void init(Bundle savedInstanceState) {
		super.init(savedInstanceState);
		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
		if (android.os.Build.VERSION.SDK_INT > 10) {
			mTabHost.getTabWidget().setShowDividers(0);
		}

		initTabs();

		mTabHost.setCurrentTab(0);
		mTabHost.setOnTabChangedListener(this);
	}

	// protected void initActionBar(ActionBar actionBar) {
	// //actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
	// //actionBar.setDisplayUseLogoEnabled(false);
	//
	// //actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP|ActionBar.DISPLAY_SHOW_TITLE);
	// actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
	// View view =
	// LayoutInflater.from(this).inflate(R.layout.actionbar_custom_backtitle,
	// null);
	// actionBar.setCustomView(view);
	// }

	@SuppressLint("InflateParams")
	private void initTabs() {
		MainTab[] tabs = MainTab.values();
		final int size = tabs.length;
		for (int i = 0; i < size; i++) {
			MainTab mainTab = tabs[i];
			TabSpec tab = mTabHost.newTabSpec(getString(mainTab.getResName()));

			View indicator = LayoutInflater.from(this).inflate(
					R.layout.v2_tab_indicator, null);
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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}
}
