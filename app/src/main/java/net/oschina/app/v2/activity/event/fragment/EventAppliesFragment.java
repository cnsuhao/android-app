package net.oschina.app.v2.activity.event.fragment;

import android.view.View;

import com.tonlin.osc.happy.R;
import com.umeng.analytics.MobclickAgent;

import net.oschina.app.v2.activity.event.adapter.EventAdapter;
import net.oschina.app.v2.api.remote.EventApi;
import net.oschina.app.v2.base.BaseRecycleViewFragment;
import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.Apply;
import net.oschina.app.v2.model.EventAppliesList;
import net.oschina.app.v2.model.ListEntity;
import net.oschina.app.v2.utils.UIHelper;
import net.oschina.app.v2.utils.XmlUtils;

import java.io.InputStream;
import java.io.Serializable;

/**
 * Created by Tonlin on 2015/8/14.
 */
public class EventAppliesFragment  extends BaseRecycleViewFragment {

    protected static final String TAG = EventFragment.class.getSimpleName();
    private static final String CACHE_KEY_PREFIX = "event_applies_list";
    private static final String FRIEND_SCREEN = "event_applies_screen";

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
        return XmlUtils.toBean(EventAppliesList.class, is);
    }

    @Override
    protected ListEntity readList(Serializable seri) {
        return ((EventAppliesList) seri);
    }

    @Override
    protected String getEmptyTip() {
        return getString(R.string.empty_tip_my_event_apply_list);
    }

    @Override
    protected void sendRequestData() {
        EventApi.getEventApplies(mCatalog,mCurrentPage,getResponseHandler());
    }

    @Override
    public void onItemClick(View view, int position) {
        Apply item = (Apply) mAdapter.getItem(position);
        if (item != null) {

        }
    }

    @Override
    public void onResume() {
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

