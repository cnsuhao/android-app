package net.oschina.app.v2.activity.question.fragment;

import java.io.ByteArrayInputStream;
import java.util.List;

import net.oschina.app.bean.NewsList;
import net.oschina.app.bean.Post;
import net.oschina.app.bean.PostList;
import net.oschina.app.common.UIHelper;
import net.oschina.app.v2.activity.question.adapter.QuestionAdapter;
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
 * 新闻资讯
 * 
 * @author william_sim
 */
public class QuestionFragment extends BaseListFragment {

	public static final String BUNDLE_KEY_CATALOG = "BUNDLE_KEY_CATALOG";
	protected static final String TAG = QuestionFragment.class.getSimpleName();
	protected static final int STATE_NONE = 0;
	protected static final int STATE_REFRESH = 1;
	protected static final int STATE_LOADMORE = 2;

	private int mCurrentPage = 0;
	private int mCatalog = NewsList.CATALOG_ALL;
	private int mState = STATE_NONE;

	private AsyncHttpResponseHandler mHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onSuccess(int statusCode, Header[] headers,
				byte[] responseBytes) {
			try {
				PostList list = PostList.parse(new ByteArrayInputStream(
						responseBytes));
				if (mState == STATE_REFRESH)
					mAdapter.clear();
				List<Post> data = list.getPostlist();
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
				}// else {
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
			mCatalog = args.getInt(BUNDLE_KEY_CATALOG);
		}
	}

	@Override
	protected ListBaseAdapter getListAdapter() {
		return new QuestionAdapter();
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
		NewsApi.getPostList(mCatalog, mCurrentPage, mHandler);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Post post = (Post) mAdapter.getItem(position - 1);
		UIHelper.showQuestionDetail(view.getContext(),
				post.getId());
	}
}