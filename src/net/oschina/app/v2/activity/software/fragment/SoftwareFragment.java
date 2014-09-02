package net.oschina.app.v2.activity.software.fragment;

import java.io.ByteArrayInputStream;
import java.util.List;

import net.oschina.app.bean.SoftwareList;
import net.oschina.app.bean.SoftwareList.Software;
import net.oschina.app.common.UIHelper;
import net.oschina.app.v2.activity.software.adapter.SoftwareAdapter;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.base.BaseListFragment;
import net.oschina.app.v2.base.ListBaseAdapter;
import net.oschina.app.v2.ui.empty.EmptyLayout;
import net.oschina.app.v2.utils.TDevice;

import org.apache.http.Header;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.loopj.android.http.AsyncHttpResponseHandler;

/**
 * @author william_sim
 */
public class SoftwareFragment extends BaseListFragment {

	public static final String BUNDLE_KEY_CATALOG = "BUNDLE_KEY_CATALOG";
	protected static final String TAG = SoftwareFragment.class.getSimpleName();
	protected static final int STATE_NONE = 0;
	protected static final int STATE_REFRESH = 1;
	protected static final int STATE_LOADMORE = 2;

	private int mCurrentPage = 0;
	private String mCatalog = SoftwareList.TAG_LASTEST;
	private int mState = STATE_NONE;

	private AsyncHttpResponseHandler mHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onSuccess(int statusCode, Header[] headers,
				byte[] responseBytes) {
			try {
				SoftwareList list = SoftwareList.parse(new ByteArrayInputStream(
						responseBytes));
				if (mState == STATE_REFRESH)
					mAdapter.clear();
				List<Software> data = list.getSoftwarelist();
				mAdapter.addData(data);
				mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
				if (data.size() == 0 && mState == STATE_REFRESH) {
					mErrorLayout.setErrorType(EmptyLayout.NODATA);
				} else if (data.size() < TDevice.getPageSize()) {
					if (mState == STATE_REFRESH)
						mAdapter.setState(ListBaseAdapter.STATE_NO_MORE);
					else
						mAdapter.setState(ListBaseAdapter.STATE_NO_MORE);
				} else {
					mAdapter.setState(ListBaseAdapter.STATE_LOAD_MORE);
				}
				// else {
				// mAdapter.setState(ListBaseAdapter.STATE_LESS_ONE_PAGE);
				// }
			} catch (Exception e) {
				e.printStackTrace();
				onFailure(statusCode, headers, responseBytes, null);
			}
		}

		@Override
		public void onFailure(int arg0, Header[] arg1, byte[] arg2,
				Throwable arg3) {
			mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
		}

		@Override
		public void onFinish() {
			executeOnLoadFinish();
			mState = STATE_NONE;
		}
	};

	public void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if (args != null) {
			mCatalog = args.getString(BUNDLE_KEY_CATALOG);
		}
	}

	@Override
	protected ListBaseAdapter getListAdapter() {
		return new SoftwareAdapter();
	}

	@Override
	public void onRefresh(PullToRefreshBase<ListView> refreshView) {
		mCurrentPage = 0;
		mState = STATE_REFRESH;
		sendRequestData();
	}

	@Override
	public void onLastItemVisible() {
		if (mState == STATE_NONE) {
			if (mAdapter.getState() == ListBaseAdapter.STATE_LOAD_MORE) {
				mCurrentPage++;
				mState = STATE_LOADMORE;
				sendRequestData();
			}
		}
	}

	@Override
	protected void sendRequestData() {
		NewsApi.getSoftwareList(mCatalog, mCurrentPage, mHandler);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Software software = (Software) mAdapter.getItem(position - 1);
		UIHelper.showUrlRedirect(view.getContext(), software.url);
	}
}