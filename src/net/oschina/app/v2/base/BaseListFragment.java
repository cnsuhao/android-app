package net.oschina.app.v2.base;

import net.oschina.app.R;
import net.oschina.app.v2.ui.empty.EmptyLayout;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public abstract class BaseListFragment extends BaseTabFragment implements
		OnRefreshListener<ListView>, OnLastItemVisibleListener,
		OnItemClickListener {

	protected PullToRefreshListView mListView;
	protected ListBaseAdapter mAdapter;
	protected EmptyLayout mErrorLayout;

	protected int getLayoutRes() {
		return R.layout.v2_fragment_pull_refresh_listview;
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(getLayoutRes(), container, false);
		initViews(view);
		return view;
	}

	protected void initViews(View view) {
		mErrorLayout = (EmptyLayout) view.findViewById(R.id.error_layout);
		mErrorLayout.setOnLayoutClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				onRefresh(null);
			}
		});
		mListView = (PullToRefreshListView) view.findViewById(R.id.listview);

		mListView.setOnItemClickListener(this);
		mListView.setOnRefreshListener(this);
		mListView.setOnLastItemVisibleListener(this);

		if (mAdapter != null) {
			mListView.setAdapter(mAdapter);
			mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
		} else {
			mAdapter = getListAdapter();
			// mListView.setRefreshing();
			mListView.setAdapter(mAdapter);
			mErrorLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
			onRefresh(null);
		}
	}

	protected abstract ListBaseAdapter getListAdapter();

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
	}

	@Override
	public void onRefresh(PullToRefreshBase<ListView> refreshView) {
	}

	@Override
	public void onLastItemVisible() {

	}

	protected void sendRequestData() {

	}

	protected void executeOnLoadFinish() {
		mListView.onRefreshComplete();
	}
}
