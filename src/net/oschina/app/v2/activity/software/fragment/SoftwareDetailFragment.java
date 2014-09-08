package net.oschina.app.v2.activity.software.fragment;

import java.io.InputStream;
import java.io.Serializable;

import net.oschina.app.AppContext;
import net.oschina.app.bean.Entity;
import net.oschina.app.bean.FavoriteList;
import net.oschina.app.bean.Software;
import net.oschina.app.common.UIHelper;
import net.oschina.app.v2.activity.news.fragment.BaseDetailFragment;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.ui.empty.EmptyLayout;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.tonlin.osc.happy.R;

/**
 * 软件详情
 * 
 * @author william_sim
 * @since 2014/09/02
 */
public class SoftwareDetailFragment extends BaseDetailFragment {

	protected static final String TAG = SoftwareDetailFragment.class
			.getSimpleName();
	private static final String SOFTWARE_CACHE_KEY = "software_";
	private TextView mTvLicense, mTvLanguage, mTvOs, mTvRecordTime;
	private TextView mTvTitle;
	private WebView mWebView;
	private ImageView mIvLogo;
	private String mIdent;
	private Software mSoftware;
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.v2_fragment_software_detail,
				container, false);

		mIdent = getActivity().getIntent().getStringExtra("ident");

		initViews(view);

		return view;
	}

	private void initViews(View view) {
		mEmptyLayout = (EmptyLayout) view.findViewById(R.id.error_layout);
		// mNewsContainer = (ScrollView)
		// view.findViewById(R.id.sv_news_container);
		mTvTitle = (TextView) view.findViewById(R.id.tv_title);
		
		mWebView = (WebView) view.findViewById(R.id.webview);
		initWebView(mWebView);

		mTvLicense = (TextView) view.findViewById(R.id.tv_software_license);
		mTvLanguage = (TextView) view.findViewById(R.id.tv_software_language);
		mTvOs = (TextView) view.findViewById(R.id.tv_software_os);
		mTvRecordTime = (TextView) view
				.findViewById(R.id.tv_software_recordtime);
		mIvLogo = (ImageView)view.findViewById(R.id.iv_logo);
		
		view.findViewById(R.id.btn_software_index).setOnClickListener(this);
		view.findViewById(R.id.btn_software_download).setOnClickListener(this);
		view.findViewById(R.id.btn_software_document).setOnClickListener(this);
	}

	@Override
	protected String getCacheKey() {
		return new StringBuilder(SOFTWARE_CACHE_KEY).append(mIdent).toString();
	}

	@Override
	protected void sendRequestData() {
		mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
		NewsApi.getSoftwareDetail(mIdent, mHandler);
	}

	@Override
	protected Entity parseData(InputStream is) throws Exception {
		return Software.parse(is);
	}

	@Override
	protected Entity readData(Serializable seri) {
		return (Software) seri;
	}

	@Override
	protected void executeOnLoadDataSuccess(Entity entity) {
		mSoftware = (Software) entity;
		fillUI();
		fillWebViewBody();
	}
	
	private void fillUI() {
		mTvTitle.setText(mSoftware.getTitle());
		mTvLicense.setText(mSoftware.getLicense());
		mTvLanguage.setText(mSoftware.getLanguage());
		mTvOs.setText(mSoftware.getOs());
		mTvRecordTime.setText(mSoftware.getRecordtime());
		DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
				.cacheOnDisk(true).postProcessor(new BitmapProcessor() {

					@Override
					public Bitmap process(Bitmap arg0) {
						return arg0;
					}
				}).build();
		ImageLoader.getInstance().displayImage(mSoftware.getLogo(), mIvLogo,options);
		
		notifyFavorite(mSoftware.getFavorite() == 1);
	}

	private void fillWebViewBody() {
		String body = UIHelper.WEB_STYLE + mSoftware.getBody();
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

		mWebView.setWebViewClient(mWebClient);// UIHelper.getWebViewClient()
		mWebView.loadDataWithBaseURL(null, body, "text/html", "utf-8", null);
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		if (id == R.id.btn_software_index) {
			UIHelper.openBrowser(v.getContext(), mSoftware.getHomepage());
		} else if (id == R.id.btn_software_download) {
			UIHelper.openBrowser(v.getContext(), mSoftware.getDownload());
		} else if (id == R.id.btn_software_document) {
			UIHelper.openBrowser(v.getContext(), mSoftware.getDocument());
		}
	}

	@Override
	protected int getFavoriteTargetId() {
		return mSoftware != null ? mSoftware.getId() : -1;
	}
	
	@Override
	protected int getFavoriteTargetType() {
		return mSoftware != null ? FavoriteList.TYPE_SOFTWARE : -1;
	}
}
