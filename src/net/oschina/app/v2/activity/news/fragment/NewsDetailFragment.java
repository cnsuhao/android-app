package net.oschina.app.v2.activity.news.fragment;

import java.io.ByteArrayInputStream;

import net.oschina.app.AppContext;
import net.oschina.app.R;
import net.oschina.app.bean.News;
import net.oschina.app.bean.News.Relative;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.base.BaseFragment;
import net.oschina.app.v2.ui.empty.EmptyLayout;

import org.apache.http.Header;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;

public class NewsDetailFragment extends BaseFragment {

	protected static final String TAG = NewsDetailFragment.class
			.getSimpleName();
	private EmptyLayout mEmptyLayout;
	//private ScrollView mNewsContainer;
	private TextView mTvTitle, mTvSource, mTvTime;
	private WebView mWebView;
	private int mNewsId;
	private News mNews;

	private AsyncHttpResponseHandler mHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
			try {
				mNews = News.parse(new ByteArrayInputStream(arg2));
				if (mNews != null && mNews.getId() > 0) {
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

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.v2_fragment_news_detail,
				container, false);

		mNewsId = getActivity().getIntent().getIntExtra("news_id", 0);

		initViews(view);

		initData();
		return view;
	}

	private void initViews(View view) {
		mEmptyLayout = (EmptyLayout) view.findViewById(R.id.error_layout);
		//mNewsContainer = (ScrollView) view.findViewById(R.id.sv_news_container);
		mTvTitle = (TextView) view.findViewById(R.id.tv_title);
		mTvSource = (TextView) view.findViewById(R.id.tv_source);
		mTvTime = (TextView) view.findViewById(R.id.tv_time);

		mWebView = (WebView) view.findViewById(R.id.webview);
		mWebView.getSettings().setSupportZoom(true);
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.getSettings().setDefaultFontSize(15);
	}

	private void initData() {
		mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
		// start to load news detail
		NewsApi.getNewsDetail(mNewsId, mHandler);
	}

	private void fillUI() {
		mTvTitle.setText(mNews.getTitle());
		mTvSource.setText(mNews.getAuthor());
		mTvTime.setText(StringUtils.friendly_time(mNews.getPubDate()));
	}

	private void fillWebViewBody() {
		String body = UIHelper.WEB_STYLE + mNews.getBody();
		// 读取用户设置：是否加载文章图片--默认有wifi下始终加载图片
		boolean isLoadImage;
		AppContext ac = (AppContext) getActivity().getApplication();
		if (AppContext.NETTYPE_WIFI == ac.getNetworkType()) {
			isLoadImage = true;
		} else {
			isLoadImage = ac.isLoadImage();
		}
		if (isLoadImage) {
			// 过滤掉 img标签的width,height属性
			body = body.replaceAll("(<img[^>]*?)\\s+width\\s*=\\s*\\S+", "$1");
			body = body.replaceAll("(<img[^>]*?)\\s+height\\s*=\\s*\\S+", "$1");

			// 添加点击图片放大支持
			body = body
					.replaceAll("(<img[^>]+src=\")(\\S+)\"",
							"$1$2\" onClick=\"javascript:mWebViewImageListener.onImageClick('$2')\"");

		} else {
			// 过滤掉 img标签
			body = body.replaceAll("<\\s*img\\s+([^>]*)\\s*>", "");
		}

		// 更多关于***软件的信息
		String softwareName = mNews.getSoftwareName();
		String softwareLink = mNews.getSoftwareLink();
		if (!StringUtils.isEmpty(softwareName)
				&& !StringUtils.isEmpty(softwareLink))
			body += String
					.format("<div id='oschina_software' style='margin-top:8px;color:#FF0000;font-weight:bold'>更多关于:&nbsp;<a href='%s'>%s</a>&nbsp;的详细信息</div>",
							softwareLink, softwareName);

		// 相关新闻
		if (mNews.getRelatives().size() > 0) {
			String strRelative = "";
			for (Relative relative : mNews.getRelatives()) {
				strRelative += String.format(
						"<a href='%s' style='text-decoration:none'>%s</a><p/>",
						relative.url, relative.title);
			}
			body += String.format("<p/><hr/><b>相关资讯</b><div><p/>%s</div>",
					strRelative);
		}

		body += "<div style='margin-bottom: 80px'/>";
		//System.out.println(body);
		mWebView.loadDataWithBaseURL(null, body, "text/html", "utf-8", null);
	}
}
