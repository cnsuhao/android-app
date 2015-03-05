package net.oschina.app.v2.activity;

import android.support.v4.view.ViewPager;

import net.oschina.app.v2.ui.tab.SlidingTabPagerAdapter;

/**
 * Created by Sim on 2015/3/5.
 */
public interface IPagerFragment {

    public SlidingTabPagerAdapter getPagerAdapter();

    public ViewPager getViewPager();
}
