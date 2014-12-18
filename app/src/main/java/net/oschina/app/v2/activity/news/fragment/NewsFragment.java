package net.oschina.app.v2.activity.news.fragment;

import java.io.InputStream;
import java.io.Serializable;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.news.adapter.NewsRecycleAdapter;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.base.BaseRecycleViewFragment;
import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.ListEntity;
import net.oschina.app.v2.model.News;
import net.oschina.app.v2.model.NewsList;
import net.oschina.app.v2.utils.UIHelper;
import android.view.View;
import android.widget.AdapterView;

/**
 * 新闻资讯
 * 
 * @author william_sim
 */
public class NewsFragment extends BaseRecycleViewFragment {

	protected static final String TAG = NewsFragment.class.getSimpleName();
	private static final String CACHE_KEY_PREFIX = "newslist_";
	private static final long MAX_CACAHE_TIME = 12 * 3600 * 1000;// 资讯的缓存最长时间为12小时

	@Override
	protected boolean requestDataFromNetWork() {
		return System.currentTimeMillis()
				- AppContext.getRefreshTime(getCacheKey()) > MAX_CACAHE_TIME;
	}

	@Override
	protected RecycleBaseAdapter getListAdapter() {
		return new NewsRecycleAdapter();
	}

	@Override
	protected String getCacheKeyPrefix() {
		return CACHE_KEY_PREFIX;
	}

	@Override
	protected ListEntity parseList(InputStream is) throws Exception {
		NewsList list = NewsList.parse(is);
		return list;
	}

	@Override
	protected ListEntity readList(Serializable seri) {
		return ((NewsList) seri);
	}

	@Override
	protected void sendRequestData() {
		NewsApi.getNewsList(mCatalog, mCurrentPage, getResponseHandler());
	}

    @Override
    public void onItemClick(View v,int position) {
        News news = (News) mAdapter.getItem(position);
        if (news != null)
            UIHelper.showNewsRedirect(v.getContext(), news);
    }
}
