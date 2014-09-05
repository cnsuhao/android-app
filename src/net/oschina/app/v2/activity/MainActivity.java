package net.oschina.app.v2.activity;

import net.oschina.app.AppContext;
import net.oschina.app.bean.User;
import net.oschina.app.common.UIHelper;
import net.oschina.app.v2.base.BaseActivity;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.internal.widget.ListPopupWindow;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.tonlin.osc.happy.R;

/**
 * 应用主界面
 * 
 * @author tonlin
 * @since 2014/08
 */
public class MainActivity extends BaseActivity implements OnTabChangeListener,
		OnItemClickListener {

	private FragmentTabHost mTabHost;
	private MenuAdapter mMenuAdapter;
	private ListPopupWindow mMenuWindow;

	@Override
	protected int getLayoutId() {
		return R.layout.v2_activity_main;
	}

	@Override
	protected void onResume() {
		if (mMenuAdapter != null) {
			mMenuAdapter.notifyDataSetChanged();
		}
		super.onResume();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void init(Bundle savedInstanceState) {
		super.init(savedInstanceState);
		AppContext.instance().initLoginInfo();

		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
		if (android.os.Build.VERSION.SDK_INT > 10) {
			mTabHost.getTabWidget().setShowDividers(0);
		}

		initTabs();

		mTabHost.setCurrentTab(0);
		mTabHost.setOnTabChangedListener(this);
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.main_menu_post:
			break;
		case R.id.main_menu_search:
			break;
		case R.id.main_menu_more:
			showMoreOptionMenu(findViewById(R.id.main_menu_more));
			break;
		}
		return true;
	}

	private void showMoreOptionMenu(View view) {
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

	private static class MenuAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return 4;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (position == 0) {
				convertView = LayoutInflater.from(parent.getContext()).inflate(
						R.layout.v2_list_cell_popup_menu_userinfo, null);
				TextView name = (TextView) convertView
						.findViewById(R.id.tv_name);
				AppContext.instance().initLoginInfo();
				if (AppContext.instance().isLogin()) {
					User user = AppContext.instance().getLoginInfo();
					name.setText(user.getName());
				} else {
					name.setText(R.string.unlogin);
				}
			} else {
				convertView = LayoutInflater.from(parent.getContext()).inflate(
						R.layout.v2_list_cell_popup_menu, null);
				TextView name = (TextView) convertView
						.findViewById(R.id.tv_name);
				int iconResId = 0;

				if (position == 1) {
					name.setText(R.string.main_menu_software);
					iconResId = R.drawable.actionbar_menu_icn_software;
				} else if (position == 2) {
					name.setText(R.string.main_menu_setting);
					iconResId = R.drawable.actionbar_menu_icn_set;
				} else if (position == 3) {
					name.setText(R.string.main_menu_exit);
					iconResId = R.drawable.actionbar_menu_icn_exit;
				}
				Drawable drawable = AppContext.resources().getDrawable(
						iconResId);
				drawable.setBounds(0, 0, drawable.getMinimumWidth(),
						drawable.getMinimumHeight());
				name.setCompoundDrawables(drawable, null, null, null);
			}
			return convertView;
		}
	}
}
