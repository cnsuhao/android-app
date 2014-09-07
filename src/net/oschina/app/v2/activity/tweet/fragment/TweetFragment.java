package net.oschina.app.v2.activity.tweet.fragment;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import net.oschina.app.AppContext;
import net.oschina.app.AppException;
import net.oschina.app.bean.NewsList;
import net.oschina.app.bean.Result;
import net.oschina.app.bean.Tweet;
import net.oschina.app.bean.TweetList;
import net.oschina.app.common.UIHelper;
import net.oschina.app.v2.activity.tweet.adapter.TweetAdapter;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.base.BaseListFragment;
import net.oschina.app.v2.base.ListBaseAdapter;
import net.oschina.app.v2.ui.dialog.CommonDialog;
import net.oschina.app.v2.ui.dialog.DialogHelper;
import net.oschina.app.v2.ui.empty.EmptyLayout;
import net.oschina.app.v2.utils.TDevice;
import net.oschina.app.v2.utils.TLog;

import org.apache.http.Header;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.tonlin.osc.happy.R;

/**
 * 新闻资讯
 * 
 * @author william_sim
 */
public class TweetFragment extends BaseListFragment implements
		OnItemLongClickListener {

	public static final String BUNDLE_KEY_CATALOG = "BUNDLE_KEY_CATALOG";
	protected static final String TAG = TweetFragment.class.getSimpleName();
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
				TweetList list = TweetList.parse(new ByteArrayInputStream(
						responseBytes));
				if (mState == STATE_REFRESH)
					mAdapter.clear();
				List<Tweet> data = list.getTweetlist();
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

	private AsyncHttpResponseHandler mDelTweetHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
			try {
				Result res = Result.parse(new ByteArrayInputStream(arg2));
				if (res != null && res.OK()) {
					AppContext.showToastShort(R.string.tip_del_tweet_success);
				} else {
					onFailure(arg0, arg1, arg2,
							new Throwable(res.getErrorMessage()));
				}
			} catch (Exception e) {
				e.printStackTrace();
				onFailure(arg0, arg1, arg2, e);
			}
		}

		@Override
		public void onFailure(int arg0, Header[] arg1, byte[] arg2,
				Throwable arg3) {
			AppContext.showToastShort(R.string.tip_del_tweet_faile);
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
	protected void initViews(View view) {
		super.initViews(view);
		mListView.getRefreshableView().setOnItemLongClickListener(this);
	}

	@Override
	protected ListBaseAdapter getListAdapter() {
		return new TweetAdapter();
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
		NewsApi.getTweetList(mCatalog, mCurrentPage, mHandler);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Tweet tweet = (Tweet) mAdapter.getItem(position - 1);
		UIHelper.showTweetDetail(view.getContext(), tweet.getId());
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		Tweet tweet = (Tweet) mAdapter.getItem(position - 1);
		if (tweet != null
				&& AppContext.instance().getLoginUid() == tweet.getAuthorId()) {
			handleDeleteTweet(tweet);
			return true;
		}
		return false;
	}

	private void handleDeleteTweet(final Tweet tweet) {
		CommonDialog dialog = DialogHelper
				.getPinterestDialogCancelable(getActivity());
		dialog.setMessage(R.string.message_delete_tweet);
		dialog.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						NewsApi.deleteTweet(tweet.getAuthorId(), tweet.getId(),
								mDelTweetHandler);
					}
				});
		dialog.setNegativeButton(R.string.cancle, null);
		dialog.show();
	}
}
