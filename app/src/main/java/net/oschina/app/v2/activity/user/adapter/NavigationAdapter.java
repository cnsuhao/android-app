package net.oschina.app.v2.activity.user.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.github.ksoichiro.android.observablescrollview.CacheFragmentStatePagerAdapter;

import net.oschina.app.v2.activity.user.fragment.MyListFragment;

/**
 * Created by Tonlin on 2015/8/20.
 */
public class NavigationAdapter extends CacheFragmentStatePagerAdapter {
    public NavigationAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    protected Fragment createItem(int i) {
        return new MyListFragment();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "分类";
            case 1:
                return "博客";
            case 2:
                return "资料";
        }
        return "";
    }

    @Override
    public int getCount() {
        return 3;
    }
}
