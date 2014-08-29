package net.oschina.app.v2.activity.news.fragment;

import net.oschina.app.v2.activity.news.adapter.NewsAdapter;
import net.oschina.app.v2.base.BaseListFragment;
import net.oschina.app.v2.base.ListBaseAdapter;

public class NewsFragment extends BaseListFragment {

	@Override
	protected ListBaseAdapter getListAdapter() {
		// TODO Auto-generated method stub
		return new NewsAdapter();
	}

}
