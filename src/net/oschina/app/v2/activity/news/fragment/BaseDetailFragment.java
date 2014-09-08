package net.oschina.app.v2.activity.news.fragment;

import java.io.ByteArrayInputStream;

import org.apache.http.Header;

import net.oschina.app.AppContext;
import net.oschina.app.bean.Result;
import net.oschina.app.common.UIHelper;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.base.BaseFragment;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.internal.widget.ListPopupWindow;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
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

	private AsyncHttpResponseHandler mAddFavoriteHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
			try {
				Result res = Result.parse(new ByteArrayInputStream(arg2));
				if (res.OK()) {
					AppContext.showToastShort(R.string.add_favorite_success);
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
}
