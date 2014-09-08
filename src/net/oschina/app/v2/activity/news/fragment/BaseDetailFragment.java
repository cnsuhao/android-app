package net.oschina.app.v2.activity.news.fragment;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;

import net.oschina.app.AppContext;
import net.oschina.app.bean.Entity;
import net.oschina.app.bean.Result;
import net.oschina.app.common.UIHelper;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.base.BaseFragment;
import net.oschina.app.v2.cache.CacheManager;
import net.oschina.app.v2.ui.empty.EmptyLayout;
import net.oschina.app.v2.utils.TDevice;

import org.apache.http.Header;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.internal.widget.ListPopupWindow;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ZoomButtonsController;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.tonlin.osc.happy.R;

public class BaseDetailFragment extends BaseFragment implements
		OnItemClickListener {
	private ListPopupWindow mMenuWindow;
	private MenuAdapter mMenuAdapter;
	protected EmptyLayout mEmptyLayout;
	protected WebView mWebView;

	protected WebViewClient mWebClient = new WebViewClient() {

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
			if (mEmptyLayout != null) {
				if (receivedError) {
					mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
				} else {
					mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
				}
			}
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			receivedError = true;
		}
	};

	@SuppressLint("SetJavaScriptEnabled")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	protected void initWebView(WebView webView) {
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

	protected void recycleWebView(WebView webView) {
		if (webView != null) {
			webView.loadUrl("about:blank");
			webView.destroy();
			webView = null;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMenuAdapter = new MenuAdapter();
		setHasOptionsMenu(true);
	}

	@Override
	public void onDestroyView() {
		recycleWebView(mWebView);
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		recycleWebView(mWebView);
		super.onDestroy();
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		requestData(false);
	}

	protected String getCacheKey() {
		return null;
	}

	protected Entity parseData(InputStream is) throws Exception {
		return null;
	}

	protected Entity readData(Serializable seri) {
		return null;
	}

	protected void sendRequestData() {
	}

	protected void requestData(boolean refresh) {
		String key = getCacheKey();
		if (TDevice.hasInternet()
				&& (!CacheManager.isReadDataCache(getActivity(), key) || refresh)) {
			sendRequestData();
		} else {
			readCacheData(key);
		}
	}

	private void readCacheData(String cacheKey) {
		new CacheTask(getActivity()).execute(cacheKey);
	}

	private class CacheTask extends AsyncTask<String, Void, Entity> {
		private WeakReference<Context> mContext;

		private CacheTask(Context context) {
			mContext = new WeakReference<Context>(context);
		}

		@Override
		protected Entity doInBackground(String... params) {
			Serializable seri = CacheManager.readObject(mContext.get(),
					params[0]);
			if (seri == null) {
				return null;
			} else {
				return readData(seri);
			}
		}

		@Override
		protected void onPostExecute(Entity entity) {
			super.onPostExecute(entity);
			if (entity != null) {
				executeOnLoadDataSuccess(entity);
			} else {
				executeOnLoadDataError(null);
			}
			executeOnLoadFinish();
		}
	}

	private class SaveCacheTask extends AsyncTask<Void, Void, Void> {
		private WeakReference<Context> mContext;
		private Serializable seri;
		private String key;

		private SaveCacheTask(Context context, Serializable seri, String key) {
			mContext = new WeakReference<Context>(context);
			this.seri = seri;
			this.key = key;
		}

		@Override
		protected Void doInBackground(Void... params) {
			CacheManager.saveObject(mContext.get(), seri, key);
			return null;
		}
	}

	protected AsyncHttpResponseHandler mHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
			try {
				Entity entity = parseData(new ByteArrayInputStream(arg2));
				if (entity != null && entity.getId() > 0) {
					executeOnLoadDataSuccess(entity);
					saveCache(entity);
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
			executeOnLoadDataError(arg3.getMessage());
		}
	};

	protected void saveCache(Entity entity) {
		new SaveCacheTask(getActivity(), entity, getCacheKey()).execute();
	}

	protected void executeOnLoadDataSuccess(Entity entity) {

	}

	protected void executeOnLoadDataError(String object) {
		mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
	}

	protected void executeOnLoadFinish() {
	}

	protected void onFavoriteChanged(boolean flag) {

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.detail_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.detail_menu_more:
			showMoreOptionMenu(getActivity()
					.findViewById(R.id.detail_menu_more));
			break;
		}
		return true;
	}

	protected int getFavoriteTargetId() {
		return -1;
	}

	protected int getFavoriteTargetType() {
		return -1;
	}

	private void showMoreOptionMenu(View view) {
		mMenuWindow = new ListPopupWindow(getActivity());
		mMenuWindow.setModal(true);
		mMenuWindow.setContentWidth(getResources().getDimensionPixelSize(
				R.dimen.popo_menu_dialog_width));
		mMenuWindow.setAdapter(mMenuAdapter);
		mMenuWindow.setOnItemClickListener(this);
		mMenuWindow.setAnchorView(view);
		mMenuWindow.show();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (position == 0) {
			if (!AppContext.instance().isLogin()) {
				UIHelper.showLogin(getActivity());
				return;
			}
			int uid = AppContext.instance().getLoginUid();
			if (mMenuAdapter.isFavorite()) {
				NewsApi.delFavorite(uid, getFavoriteTargetId(),
						getFavoriteTargetType(), mDelFavoriteHandler);
			} else {
				NewsApi.addFavorite(uid, getFavoriteTargetId(),
						getFavoriteTargetType(), mAddFavoriteHandler);
			}
		} else {
			AppContext.showToastShort("分享");
		}
		if (mMenuWindow != null) {
			mMenuWindow.dismiss();
			mMenuWindow = null;
		}
	}

	protected void notifyFavorite(boolean favorite) {
		if (mMenuAdapter != null) {
			mMenuAdapter.setFavorite(favorite);
		}
	}

	@SuppressLint("ViewHolder")
	private static class MenuAdapter extends BaseAdapter {

		private boolean isFavorite;

		public boolean isFavorite() {
			return isFavorite;
		}

		public void setFavorite(boolean favorite) {
			isFavorite = favorite;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(
					R.layout.v2_list_cell_popup_menu, null);
			TextView name = (TextView) convertView.findViewById(R.id.tv_name);

			int iconResId = 0;
			if (position == 0) {
				name.setText(isFavorite ? R.string.detail_menu_unfavorite
						: R.string.detail_menu_favorite);
				iconResId = isFavorite ? R.drawable.actionbar_menu_icn_unfavoirite
						: R.drawable.actionbar_menu_icn_favoirite;
			} else if (position == 1) {
				name.setText(parent.getResources().getString(
						R.string.detail_menu_for_share));
				iconResId = R.drawable.actionbar_menu_icn_share;
			}
			Drawable drawable = AppContext.resources().getDrawable(iconResId);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			name.setCompoundDrawables(drawable, null, null, null);
			return convertView;
		}
	}

	private AsyncHttpResponseHandler mAddFavoriteHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
			try {
				Result res = Result.parse(new ByteArrayInputStream(arg2));
				if (res.OK()) {
					AppContext.showToastShort(R.string.add_favorite_success);
					mMenuAdapter.setFavorite(true);
					mMenuAdapter.notifyDataSetChanged();
					onFavoriteChanged(true);
				} else {
					AppContext.showToastShort(res.getErrorMessage());
				}

			} catch (Exception e) {
				e.printStackTrace();
				onFailure(arg0, arg1, arg2, e);
			}
		}

		@Override
		public void onFailure(int arg0, Header[] arg1, byte[] arg2,
				Throwable arg3) {
			AppContext.showToastShort(R.string.add_favorite_faile);
		}
	};

	private AsyncHttpResponseHandler mDelFavoriteHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
			try {
				Result res = Result.parse(new ByteArrayInputStream(arg2));
				if (res.OK()) {
					AppContext.showToastShort(R.string.del_favorite_success);
					mMenuAdapter.setFavorite(false);
					mMenuAdapter.notifyDataSetChanged();
					onFavoriteChanged(false);
				} else {
					AppContext.showToastShort(res.getErrorMessage());
				}
			} catch (Exception e) {
				e.printStackTrace();
				onFailure(arg0, arg1, arg2, e);
			}
		}

		@Override
		public void onFailure(int arg0, Header[] arg1, byte[] arg2,
				Throwable arg3) {
			AppContext.showToastShort(R.string.del_favorite_faile);
		}
	};
}
