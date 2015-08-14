package net.oschina.app.v2.activity.event.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import com.tonlin.osc.happy.R;
import com.umeng.analytics.MobclickAgent;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.event.adapter.EventAdapter;
import net.oschina.app.v2.api.remote.EventApi;
import net.oschina.app.v2.base.BaseRecycleViewFragment;
import net.oschina.app.v2.base.Constants;
import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.Event;
import net.oschina.app.v2.model.EventList;
import net.oschina.app.v2.model.ListEntity;
import net.oschina.app.v2.model.User;
import net.oschina.app.v2.ui.empty.EmptyLayout;
import net.oschina.app.v2.utils.UIHelper;
import net.oschina.app.v2.utils.XmlUtils;

import java.io.InputStream;
import java.io.Serializable;

/**
 * 我的好友
 *
 * @author william_sim
 */
public class EventFragment extends BaseRecycleViewFragment {

    protected static final String TAG = EventFragment.class.getSimpleName();
    private static final String CACHE_KEY_PREFIX = "event_list";
    private static final String FRIEND_SCREEN = "event_screen";
    private boolean mIsWaitingLogin;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mErrorLayout != null && mCatalog == EventList.EVENT_LIST_TYPE_MY_EVENT) {
                mIsWaitingLogin = true;
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
    protected void initViews(View view) {
        super.initViews(view);
        UIHelper.sendBroadcastForNotice(getActivity());
    }

    @Override
    protected RecycleBaseAdapter getListAdapter() {
        return new EventAdapter();
    }

    @Override
    protected String getCacheKeyPrefix() {
        return CACHE_KEY_PREFIX;
    }

    @Override
    protected ListEntity parseList(InputStream is) throws Exception {
        return XmlUtils.toBean(EventList.class, is);
    }

    @Override
    protected ListEntity readList(Serializable seri) {
        return ((EventList) seri);
    }

    @Override
    protected String getEmptyTip() {
        if(mCatalog == EventList.EVENT_LIST_TYPE_MY_EVENT)
            return getString(R.string.empty_tip_my_event_list);
        return super.getEmptyTip();
    }

    @Override
    protected void requestData(boolean refresh) {
        if (mCatalog == EventList.EVENT_LIST_TYPE_NEW_EVENT || AppContext.instance().isLogin()) {
            mIsWaitingLogin = false;
            super.requestData(refresh);
        } else {
            mIsWaitingLogin = true;
            mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
            mErrorLayout.setErrorMessage(getString(R.string.unlogin_tip));
        }
    }

    @Override
    protected void sendRequestData() {
        int uid = 0;
        if (mCatalog == EventList.EVENT_LIST_TYPE_MY_EVENT) {
            if (AppContext.instance().isLogin()) {
                User user = AppContext.instance().getLoginInfo();
                uid = user.getUid();
            }
        }
        EventApi.getEventList(mCurrentPage, uid, getResponseHandler());
    }

    @Override
    public void onItemClick(View view, int position) {
        Event item = (Event) mAdapter.getItem(position);
        if (item != null) {
            UIHelper.showEventDetail(getActivity(),item.getId());
        }
    }

    @Override
    public void onResume() {
        if (mIsWaitingLogin) {
            mCurrentPage = 0;
            mState = STATE_REFRESH;
            requestData(false);
        }
        MobclickAgent.onPageStart(FRIEND_SCREEN);
        MobclickAgent.onResume(getActivity());
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(FRIEND_SCREEN);
        MobclickAgent.onPause(getActivity());
    }
}
