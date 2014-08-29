package net.oschina.app.v2.ui.pagertab;

import net.oschina.app.v2.base.BaseTabFragment;
import android.content.Context;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;

public abstract class SlidingTabPagerAdapter extends FragmentPagerAdapter
		implements BaseTabFragment.a,
		PagerSlidingTabStrip.OnTabClickListener {

	protected final Context context;
	protected final BaseTabFragment fragments[];
	private int lastPostion;
	private final ViewPager pager;

	public SlidingTabPagerAdapter(FragmentManager fragmentmanager, int i,
			Context context1, ViewPager viewpager) {
		super(fragmentmanager);
		lastPostion = 0;
		fragments = new BaseTabFragment[i];
		context = context1;
		pager = viewpager;
		lastPostion = 0;
	}

	private BaseTabFragment getFragmentByPosition(int i) {
		BaseTabFragment yixintabfragment;
		if (i < 0 || i >= fragments.length)
			yixintabfragment = null;
		else
			yixintabfragment = fragments[i];
		return yixintabfragment;
	}

	private void onLeave(int i) {
		BaseTabFragment yixintabfragment = getFragmentByPosition(lastPostion);
		lastPostion = i;
		if (yixintabfragment != null)
			yixintabfragment.g();
	}

	public abstract int getCacheCount();

	@Override
	public abstract int getCount();

	@Override
	public BaseTabFragment getItem(int i) {
		return fragments[i];
	}

	@Override
	public abstract CharSequence getPageTitle(int i);

	public boolean isCurrent(BaseTabFragment yixintabfragment) {
		boolean flag = false;
		int i = pager.getCurrentItem();
		int j = 0;
		do {
			label0: {
				if (j < fragments.length) {
					if (yixintabfragment != fragments[j] || j != i)
						break label0;
					flag = true;
				}
				return flag;
			}
			j++;
		} while (true);
	}

	public void onPageScrolled(int i) {
		BaseTabFragment yixintabfragment = getFragmentByPosition(i);
		if (yixintabfragment != null) {
			yixintabfragment.h();
			onLeave(i);
		}
	}

	public void onPageSelected(int i) {
		BaseTabFragment yixintabfragment = getFragmentByPosition(i);
		if (yixintabfragment != null) {
			yixintabfragment.f();
			onLeave(i);
		}
	}

	@Override
	public void onTabClicked(int i) {
		BaseTabFragment yixintabfragment = getFragmentByPosition(i);
		if (yixintabfragment != null)
			yixintabfragment.i();
	}
}
