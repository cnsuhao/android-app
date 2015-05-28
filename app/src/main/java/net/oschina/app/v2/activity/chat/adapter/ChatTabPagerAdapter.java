package net.oschina.app.v2.activity.chat.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.chat.ChatTab;
import net.oschina.app.v2.activity.news.NewsTab;
import net.oschina.app.v2.activity.news.fragment.NewsFragment;
import net.oschina.app.v2.base.BaseViewPagerAdapter;

public final class ChatTabPagerAdapter extends BaseViewPagerAdapter {

    public ChatTabPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public final int getCount() {
        return ChatTab.values().length;
    }

    @Override
    public final CharSequence getPageTitle(int i) {
        ChatTab tab = ChatTab.getTabByIdx(i);
        int idx = 0;
        CharSequence title = "";
        if (tab != null)
            idx = tab.getTitle();
        if (idx != 0)
            title = AppContext.string(idx);
        return title;
    }

    @Override
    protected Fragment createItem(int position) {
        final int pattern = position % 2;
        ChatTab[] values = ChatTab.values();
        Fragment f = null;
        try {
            f = (Fragment) values[pattern].getClz().newInstance();

            Bundle args = new Bundle();
            args.putInt(NewsFragment.BUNDLE_KEY_CATALOG, values[pattern].getCatalog());

            f.setArguments(args);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return f;
    }
}