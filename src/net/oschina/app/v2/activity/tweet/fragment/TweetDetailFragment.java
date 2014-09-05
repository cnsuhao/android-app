package net.oschina.app.v2.activity.tweet.fragment;

import java.io.ByteArrayInputStream;
import java.util.List;

import net.oschina.app.AppContext;
import net.oschina.app.bean.Comment;
import net.oschina.app.bean.CommentList;
import net.oschina.app.bean.Tweet;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import net.oschina.app.v2.activity.comment.adapter.CommentAdapter;
import net.oschina.app.v2.activity.news.fragment.EmojiFragmentControl;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.base.BaseFragment;
import net.oschina.app.v2.base.ListBaseAdapter;
import net.oschina.app.v2.emoji.EmojiFragment;
import net.oschina.app.v2.emoji.EmojiFragment.EmojiTextListener;
import net.oschina.app.v2.service.PublicCommentTask;
import net.oschina.app.v2.service.ServerTaskUtils;
import net.oschina.app.v2.ui.empty.EmptyLayout;
import net.oschina.app.v2.utils.TDevice;

import org.apache.http.Header;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ZoomButtonsController;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tonlin.osc.happy.R;

public class TweetDetailFragment extends BaseFragment implements
		EmojiTextListener, EmojiFragmentControl {
	protected static final String TAG = TweetDetailFragment.class
			.getSimpleName();
	private ListView mListView;
	private EmptyLayout mEmptyView;
	private TextView mTvName, mTvFrom, mTvTime, mTvCommentCount;
	private WebView mContent;
	private int mTweetId;
	private Tweet mTweet;
	private int mCurrentPage = 0;
	private CommentAdapter mAdapter;
	private EmojiFragment mEmojiFragment;

	private AsyncHttpResponseHandler mDetailHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
			try {
				mTweet = Tweet.parse(new ByteArrayInputStream(arg2));
				if (mTweet != null && mTweet.getId() > 0) {
					fillUI();
					mCurrentPage = 0;

					mState = STATE_REFRESH;
					mEmptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
					sendRequestCommentData();
				} else {
					throw new RuntimeException("load detail error");
				}
			} catch (Exception e) {
				e.printStackTrace();
				onFailure(arg0, arg1, arg2, e);
			}
		}

		@Override
		public void onFailure(int arg0, Header[] arg1, byte[] arg2,
				Throwable arg3) {
			mState = STATE_NONE;
			mEmptyView.setErrorType(EmptyLayout.NETWORK_ERROR);
		}
	};

	private AsyncHttpResponseHandler mCommentHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
			try {
				CommentList list = CommentList.parse(new ByteArrayInputStream(
						arg2));
				if (mState == STATE_REFRESH)
					mAdapter.clear();
				List<Comment> data = list.getCommentlist();
				mAdapter.addData(data);
				mEmptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
				if (data.size() == 0 && mState == STATE_REFRESH) {
					// mEmptyView.setErrorType(EmptyLayout.NODATA);
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
				onFailure(arg0, arg1, arg2, e);
			}
		}

		@Override
		public void onFailure(int arg0, Header[] arg1, byte[] arg2,
				Throwable arg3) {
			mEmptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
		}

		public void onFinish() {
			mState = STATE_NONE;
		}
	};

	private OnScrollListener mScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {

		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			if (mAdapter != null
					&& mAdapter.getDataSize() > 0
					&& mListView.getLastVisiblePosition() == (mListView
							.getCount() - 1)) {
				if (mState == STATE_NONE
						&& mAdapter.getState() == ListBaseAdapter.STATE_LOAD_MORE) {
					mState = STATE_LOADMORE;
					mCurrentPage++;
					sendRequestCommentData();
				}
			}
		}
	};
	private ImageView mIvAvatar;

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.v2_fragment_tweet_detail,
				container, false);
		mTweetId = getActivity().getIntent().getIntExtra("tweet_id", 0);

		initViews(view);

		sendRequestData();
		return view;
	}

	@SuppressLint("InflateParams")
	private void initViews(View view) {
		mEmptyView = (EmptyLayout) view.findViewById(R.id.error_layout);
		mListView = (ListView) view.findViewById(R.id.listview);
		mListView.setOnScrollListener(mScrollListener);
		View header = LayoutInflater.from(getActivity()).inflate(
				R.layout.v2_list_header_tweet_detail, null);
		mIvAvatar = (ImageView) header.findViewById(R.id.iv_avatar);
		mIvAvatar.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				UIHelper.showUserCenter(getActivity(), mTweet.getAuthorId(),
						mTweet.getAuthor());
			}
		});
		mTvName = (TextView) header.findViewById(R.id.tv_name);
		mTvFrom = (TextView) header.findViewById(R.id.tv_from);
		mTvTime = (TextView) header.findViewById(R.id.tv_time);
		mTvCommentCount = (TextView) header.findViewById(R.id.tv_comment_count);
		mContent = (WebView) header.findViewById(R.id.webview);
		initWebView(mContent);

		mListView.addHeaderView(header);
		mAdapter = new CommentAdapter(true);
		mListView.setAdapter(mAdapter);
	}

	@SuppressLint("SetJavaScriptEnabled")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void initWebView(WebView webView) {
		WebSettings settings = webView.getSettings();
		settings.setDefaultFontSize(20);
		settings.setJavaScriptEnabled(true);
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true);
		int sysVersion = Build.VERSION.SDK_INT;
		if (sysVersion >= 11) {
			settings.setDisplayZoomControls(false);
		} else {
			ZoomButtonsController zbc = new ZoomButtonsController(webView);
			zbc.getZoomControls().setVisibility(View.GONE);
		}
		UIHelper.addWebImageShow(getActivity(), webView);
	}

	private void fillUI() {
		ImageLoader.getInstance().displayImage(mTweet.getFace(), mIvAvatar);
		mTvName.setText(mTweet.getAuthor());
		mTvTime.setText(StringUtils.friendly_time(mTweet.getPubDate()));
		switch (mTweet.getAppClient()) {
		default:
			mTvFrom.setText("");
			break;
		case Tweet.CLIENT_MOBILE:
			mTvFrom.setText(R.string.from_mobile);
			break;
		case Tweet.CLIENT_ANDROID:
			mTvFrom.setText(R.string.from_android);
			break;
		case Tweet.CLIENT_IPHONE:
			mTvFrom.setText(R.string.from_iphone);
			break;
		case Tweet.CLIENT_WINDOWS_PHONE:
			mTvFrom.setText(R.string.from_windows_phone);
			break;
		case Tweet.CLIENT_WECHAT:
			mTvFrom.setText(R.string.from_wechat);
			break;
		}

		mTvCommentCount.setText(getString(R.string.comment_count,
				mTweet.getCommentCount()));

		// mTvCommentCount.setText(mTweet.getBody());

		// set content
		String body = UIHelper.WEB_STYLE + mTweet.getBody();
		body = body.replaceAll("(<img[^>]*?)\\s+width\\s*=\\s*\\S+", "$1");
		body = body.replaceAll("(<img[^>]*?)\\s+height\\s*=\\s*\\S+", "$1");

		mContent.loadDataWithBaseURL(null, body, "text/html", "utf-8", null);
		mContent.setWebViewClient(UIHelper.getWebViewClient());
	}

	private void sendRequestData() {
		mState = STATE_REFRESH;
		mEmptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
		NewsApi.getTweetDetail(mTweetId, mDetailHandler);
	}

	private void sendRequestCommentData() {
		NewsApi.getCommentList(mTweetId, CommentList.CATALOG_TWEET,
				mCurrentPage, mCommentHandler);
	}

	@Override
	public void setEmojiFragment(EmojiFragment fragment) {
		mEmojiFragment = fragment;
		mEmojiFragment.setEmojiTextListener(this);
	}

	@Override
	public void onSendClick(String text) {
		if (TextUtils.isEmpty(text)) {
			AppContext.showToastShort(R.string.tip_comment_content_empty);
			mEmojiFragment.requestFocusInput();
			return;
		}
		PublicCommentTask task = new PublicCommentTask();
		task.setId(mTweetId);
		task.setCatalog(CommentList.CATALOG_TWEET);
		task.setIsPostToMyZone(0);
		task.setContent(text);
		task.setUid(AppContext.instance().getLoginUid());
		ServerTaskUtils.publicComment(getActivity(), task);
		mEmojiFragment.reset();
	}
}
