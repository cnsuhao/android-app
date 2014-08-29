package net.oschina.app.v2.activity.news;

import java.util.Iterator;
import java.util.List;

import net.oschina.app.v2.activity.news.fragment.NewsFragment;
import net.oschina.app.v2.base.BaseTabFragment;
import net.oschina.app.v2.ui.pagertab.SlidingTabPagerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

public final class NewsTabPagerAdapter extends SlidingTabPagerAdapter {

	public NewsTabPagerAdapter(FragmentManager mgr, Context context,
			ViewPager viewpager) {
		super(mgr, NewsTab.values().length, context.getApplicationContext(),
				viewpager);
		NewsTab[] values = NewsTab.values();
		for (int i = 0; i < values.length; i++) {
			Fragment fragment = null;
			List<Fragment> list = mgr.getFragments();
			if (list != null) {
				Iterator<Fragment> iterator = list.iterator();
				while (iterator.hasNext()) {
					fragment = iterator.next();
					if (fragment.getClass() == values[i].getClz()) {
						break;
					}
				}
			}
			BaseTabFragment tabFragment = (BaseTabFragment) fragment;
			if (tabFragment == null)
				try {
					tabFragment = (BaseTabFragment) values[i].getClz()
							.newInstance();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			tabFragment.a(this);
			
			Bundle args = new Bundle();
			args.putInt(NewsFragment.BUNDLE_KEY_CATALOG, values[i].getCatalog());
			tabFragment.setArguments(args);
			
			fragments[values[i].getIdx()] = tabFragment;
		}
	}

	public final int getCacheCount() {
		return NewsTab.values().length;
	}

	public final int getCount() {
		return NewsTab.values().length;
	}

	public final CharSequence getPageTitle(int i) {
		NewsTab e1 = NewsTab.getTabByIdx(i);
		int j;
		Object obj;
		if (e1 != null)
			j = e1.getTitle();
		else
			j = 0;
		if (j != 0)
			obj = context.getText(j);
		else
			obj = "";
		return ((CharSequence) (obj));
	}
}