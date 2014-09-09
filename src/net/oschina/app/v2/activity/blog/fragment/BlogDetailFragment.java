package net.oschina.app.v2.activity.blog.fragment;

import java.io.InputStream;
import java.io.Serializable;

import net.oschina.app.AppContext;
import net.oschina.app.bean.Blog;
import net.oschina.app.bean.Comment;
import net.oschina.app.bean.Entity;
import net.oschina.app.bean.FavoriteList;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import net.oschina.app.v2.activity.news.fragment.BaseDetailFragment;
import net.oschina.app.v2.activity.news.fragment.EmojiFragmentControl;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.emoji.EmojiFragment;
import net.oschina.app.v2.emoji.EmojiFragment.EmojiTextListener;
import net.oschina.app.v2.service.PublicCommentTask;
import net.oschina.app.v2.service.ServerTaskUtils;
import net.oschina.app.v2.ui.empty.EmptyLayout;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.tonlin.osc.happy.R;

public class BlogDetailFragment extends BaseDetailFragment implements
		EmojiTextListener, EmojiFragmentControl {

	protected static final String TAG = BlogDetailFragment.class
			.getSimpleName();
	private static final String BLOG_CACHE_KEY = "blog_";
	private TextView mTvTitle, mTvSource, mTvTime;
	private TextView mTvCommentCount;
	private WebView mWebView;
	private int mBlogId;
	private Blog mBlog;
	private EmojiFragment mEmojiFragment;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		ActionBarActivity act = (ActionBarActivity) activity;
		mTvCommentCount = (TextView) act.getSupportActionBar().getCustomView()
				.findViewById(R.id.tv_comment_count);
		mTvCommentCount.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				UIHelper.showBlogComment(getActivity(), mBlog.getId(),
						mBlog.getAuthorId());
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.v2_fragment_news_detail,
				container, false);

		mBlogId = getActivity().getIntent().getIntExtra("blog_id", 0);

		initViews(view);

		return view;
	}

	private void initViews(View view) {
		mEmptyLayout = (EmptyLayout) view.findViewById(R.id.error_layout);
		mTvTitle = (TextView) view.findViewById(R.id.tv_title);
		mTvSource = (TextView) view.findViewById(R.id.tv_source);
		mTvTime = (TextView) view.findViewById(R.id.tv_time);

		mWebView = (WebView) view.findViewById(R.id.webview);
		initWebView(mWebView);
	}

	@Override
	protected String getCacheKey() {
		return new StringBuilder(BLOG_CACHE_KEY).append(mBlogId).toString();
	}

	@Override
	protected void sendRequestData() {
		mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
		NewsApi.getBlogDetail(mBlogId, mHandler);
	}

	@Override
	protected Entity parseData(InputStream is) throws Exception {
		return Blog.parse(is);
	}

	@Override
	protected Entity readData(Serializable seri) {
		return (Blog) seri;
	}

	@Override
	protected void onCommentChanged(int opt, int id, int catalog,
			boolean isBlog, Comment comment) {
		if (id == mBlogId && isBlog) {
			if (Comment.OPT_ADD == opt && mBlog != null) {
				mBlog.setCommentCount(mBlog.getCommentCount() + 1);
				if (mTvCommentCount != null) {
					mTvCommentCount.setVisibility(View.VISIBLE);
					mTvCommentCount.setText(getString(R.string.comment_count,
							mBlog.getCommentCount()));
				}
			}
		}
	}
	
	@Override
	protected void executeOnLoadDataSuccess(Entity entity) {
		mBlog = (Blog) entity;
		fillUI();
		fillWebViewBody();
	}

	private void fillUI() {
		mTvTitle.setText(mBlog.getTitle());
		mTvSource.setText(mBlog.getAuthor());
		mTvTime.setText(StringUtils.friendly_time(mBlog.getPubDate()));
		if (mTvCommentCount != null) {
			mTvCommentCount.setVisibility(View.VISIBLE);
			mTvCommentCount.setText(getString(R.string.comment_count,
					mBlog.getCommentCount()));
		}

		notifyFavorite(mBlog.getFavorite() == 1);
	}

	private void fillWebViewBody() {
		String body = UIHelper.WEB_STYLE + mBlog.getBody();

		// 读取用户设置：是否加载文章图片--默认有wifi下始终加载图片
		boolean isLoadImage;
		AppContext ac = (AppContext) getActivity().getApplication();
		if (AppContext.NETTYPE_WIFI == ac.getNetworkType()) {
			isLoadImage = true;
		} else {
			isLoadImage = ac.isLoadImage();
		}
		if (isLoadImage) {
			body = body.replaceAll("(<img[^>]*?)\\s+width\\s*=\\s*\\S+", "$1");
			body = body.replaceAll("(<img[^>]*?)\\s+height\\s*=\\s*\\S+", "$1");

			// 添加点击图片放大支持
			body = body
					.replaceAll("(<img[^>]+src=\")(\\S+)\"",
							"$1$2\" onClick=\"javascript:mWebViewImageListener.onImageClick('$2')\"");
		} else {
			body = body.replaceAll("<\\s*img\\s+([^>]*)\\s*>", "");
		}

		mWebView.setWebViewClient(mWebClient);
		mWebView.loadDataWithBaseURL(null, body, "text/html", "utf-8", null);
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
		task.setId(mBlogId);
		task.setContent(text);
		task.setUid(AppContext.instance().getLoginUid());
		ServerTaskUtils.publicBlogComment(getActivity(), task);
		mEmojiFragment.reset();
	}

	@Override
	protected int getFavoriteTargetId() {
		return mBlog != null ? mBlog.getId() : -1;
	}
	
	@Override
	protected int getFavoriteTargetType() {
		return mBlog != null ? FavoriteList.TYPE_BLOG : -1;
	}
}
