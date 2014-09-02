package net.oschina.app.v2.activity.news.fragment;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.util.List;

import net.oschina.app.AppContext;
import net.oschina.app.R;
import net.oschina.app.bean.Post;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.base.BaseFragment;
import net.oschina.app.v2.ui.empty.EmptyLayout;

import org.apache.http.Header;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.ZoomButtonsController;

import com.loopj.android.http.AsyncHttpResponseHandler;

public class QuestionDetailFragment extends BaseFragment {

	protected static final String TAG = QuestionDetailFragment.class
			.getSimpleName();
	private EmptyLayout mEmptyLayout;
	private TextView mTvTitle, mTvSource, mTvTime;
	private WebView mWebView;
	private int mPostId;
	private Post mPost;

	private AsyncHttpResponseHandler mHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
			try {
				mPost = Post.parse(new ByteArrayInputStream(arg2));
				if (mPost != null && mPost.getId() > 0) {
					fillUI();
					fillWebViewBody();
					mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
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
			mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
		}
	};


	private WebViewClient mWebClient = new WebViewClient() {

		private boolean receivedError = false;

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			receivedError = false;
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			UIHelper.showUrlRedirect(view.getContext(), url);
			return true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			if (receivedError) {
				mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
			} else {
				mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
			}
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			receivedError = true;
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.v2_fragment_news_detail,
				container, false);

		mPostId = getActivity().getIntent().getIntExtra("post_id", 0);

		initViews(view);

		initData();
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

	@SuppressLint("SetJavaScriptEnabled")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void initWebView(WebView webView) {
		WebSettings settings = webView.getSettings();
		settings.setDefaultFontSize(15);
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
	
	private void initData() {
		mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
		// start to load news detail
		NewsApi.getPostDetail(mPostId, mHandler);
	}

	private void fillUI() {
		mTvTitle.setText(mPost.getTitle());
		mTvSource.setText(mPost.getAuthor());
		mTvTime.setText(StringUtils.friendly_time(mPost.getPubDate()));
	}

	private void fillWebViewBody() {
		// 显示标签
		String tags = getPostTags(mPost.getTags());

		String body = UIHelper.WEB_STYLE + mPost.getBody() + tags
				+ "<div style=\"margin-bottom: 80px\" />";
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

	@SuppressWarnings("deprecation")
	private String getPostTags(List<String> taglist) {
		if (taglist == null)
			return "";
		String tags = "";
		for (String tag : taglist) {
			tags += String
					.format("<a class='tag' href='http://www.oschina.net/question/tag/%s' >&nbsp;%s&nbsp;</a>&nbsp;&nbsp;",
							URLEncoder.encode(tag), tag);
		}
		return String.format("<div style='margin-top:10px;'>%s</div>", tags);
	}
}
