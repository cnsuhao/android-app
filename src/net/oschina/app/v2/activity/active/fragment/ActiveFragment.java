package net.oschina.app.v2.activity.active.fragment;

import java.io.InputStream;
import java.io.Serializable;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.active.adapter.ActiveAdapter;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.base.BaseListFragment;
import net.oschina.app.v2.base.Constants;
import net.oschina.app.v2.base.ListBaseAdapter;
import net.oschina.app.v2.model.Active;
import net.oschina.app.v2.model.ActiveList;
import net.oschina.app.v2.model.ListEntity;
import net.oschina.app.v2.ui.empty.EmptyLayout;
import net.oschina.app.v2.utils.UIHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.tonlin.osc.happy.R;

/**
 * 动态
 * 
 * @author william_sim
 */
public class ActiveFragment extends BaseListFragment {

	protected static final String TAG = ActiveFragment.class.getSimpleName();
	private static final String CACHE_KEY_PREFIX = "active_list";
	private boolean mIsWatingLogin;

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (mErrorLayout != null) {
				mIsWatingLogin = true;
				mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
				mErrorLayout.setErrorMessage(getString(R.string.unlogin_tip));
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		IntentFilter filter = new IntentFilter(Constants.INTENT_ACTION_LOGOUT);
		getActivity().registerReceiver(mReceiver, filter);
	}

	@Override
	public void onDestroy() {
		getActivity().unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	@Override
	public void onResume() {
		if (mIsWatingLogin) {
			mCurrentPage = 0;
			mState = STATE_REFRESH;
			requestData(false);
		}
		super.onResume();
	}

	@Override
	protected ListBaseAdapter getListAdapter() {
		return new ActiveAdapter();
	}

	@Override
	protected String getCacheKeyPrefix() {
		return CACHE_KEY_PREFIX;
	}

	@Override
	protected ListEntity parseList(InputStream is) throws Exception {
		ActiveList list = ActiveList.parse(is);
		return list;
	}

	@Override
	protected ListEntity readList(Serializable seri) {
		return ((ActiveList) seri);
	}

	@Override
	protected void initViews(View view) {
		super.initViews(view);
		mErrorLayout.setOnLayoutClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (AppContext.instance().isLogin()) {
					requestData(false);
				} else {
					UIHelper.showLogin(getActivity());
				}
			}
		});
	}

	@Override
	protected void requestData(boolean refresh) {
		mErrorLayout.setErrorMessage("");
		if (AppContext.instance().isLogin()) {
			mIsWatingLogin = false;
			super.requestData(refresh);
		} else {
			mIsWatingLogin = true;
			mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
			mErrorLayout.setErrorMessage(getString(R.string.unlogin_tip));
		}
	}

	@Override
	protected void sendRequestData() {
		NewsApi.getActiveList(AppContext.instance().getLoginUid(), mCatalog,
				mCurrentPage, mHandler);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Active active = (Active) mAdapter.getItem(position - 1);
		UIHelper.showActiveRedirect(view.getContext(), active);
	}
}
