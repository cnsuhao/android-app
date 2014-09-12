package net.oschina.app.v2.activity.tweet.fragment;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;

import net.oschina.app.AppContext;
import net.oschina.app.v2.activity.tweet.adapter.TweetAdapter;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.base.BaseListFragment;
import net.oschina.app.v2.base.ListBaseAdapter;
import net.oschina.app.v2.model.ListEntity;
import net.oschina.app.v2.model.Result;
import net.oschina.app.v2.model.Tweet;
import net.oschina.app.v2.model.TweetList;
import net.oschina.app.v2.ui.dialog.CommonDialog;
import net.oschina.app.v2.ui.dialog.DialogHelper;
import net.oschina.app.v2.utils.UIHelper;

import org.apache.http.Header;

import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.tonlin.osc.happy.R;

/**
 * 动弹
 * @author william_sim
 */
public class TweetFragment extends BaseListFragment implements
		OnItemLongClickListener {
	protected static final String TAG = TweetFragment.class.getSimpleName();
	private static final String CACHE_KEY_PREFIX = "tweet_list";

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
	protected String getCacheKeyPrefix() {
		return CACHE_KEY_PREFIX;
	}

	@Override
	protected ListEntity parseList(InputStream is) throws Exception {
		TweetList list = TweetList.parse(is);
		return list;
	}

	@Override
	protected ListEntity readList(Serializable seri) {
		return ((TweetList) seri);
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
