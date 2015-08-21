package net.oschina.app.v2.activity.user.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.MySwipeRefreshLayout;
import android.view.View;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.user.SwipeRefreshViewControl;
import net.oschina.app.v2.activity.user.UserDataControl;
import net.oschina.app.v2.activity.user.adapter.UserActiveAdapter;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.base.BaseRecycleViewFragment;
import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.Active;
import net.oschina.app.v2.model.ListEntity;
import net.oschina.app.v2.model.UserInformation;
import net.oschina.app.v2.utils.TLog;
import net.oschina.app.v2.utils.UIHelper;

import java.io.InputStream;
import java.io.Serializable;

/**
 * Created by Tonlin on 2015/8/21.
 */
public class UserActiveRecyclerListFragment extends BaseRecycleViewFragment implements SwipeRefreshViewControl {

    private int mUid, mHisUid;
    private String mHisName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUid = AppContext.instance().getLoginUid();
        mHisUid = getActivity().getIntent().getIntExtra("his_id", 0);
        mHisName = getActivity().getIntent().getStringExtra("his_name");
    }

    @Override
    protected boolean useSingleState() {
        return true;
    }

    @Override
    protected boolean isNeedListDivider() {
        return false;
    }

    @Override
    protected String getCacheKeyPrefix() {
        return "user_" + mHisUid + "_active";
    }

    @Override
    protected ListEntity parseList(InputStream is) throws Exception {
        UserInformation info = UserInformation.parse(is);
        return info;
    }

    private void fillUser(UserInformation info) {
        if (mCurrentPage == 0 && info != null) {
            try {
                FragmentActivity act = getActivity();
                if (act != null && act instanceof UserDataControl) {
                    ((UserDataControl) act).setUserInfo(info.getUser());
                }
            } catch (Exception e) {
                TLog.error(e.getMessage());
            }
        }
    }

    @Override
    protected ListEntity readList(Serializable seri) {
        UserInformation info = (UserInformation) seri;
        return info;
    }

    @Override
    protected void sendRequestData() {
        NewsApi.getUserInformation(mUid, mHisUid, mHisName, mCurrentPage,
                getResponseHandler());
    }

    @Override
    protected RecycleBaseAdapter getListAdapter() {
        return new UserActiveAdapter();
    }

    @Override
    public MySwipeRefreshLayout getSwipeRefreshView() {
        return mSwipeRefresh;
    }

    @Override
    protected void onItemClick(View view, int position) {
        Active active = (Active) mAdapter.getItem(position);
        if (active != null) {
            UIHelper.showActiveRedirect(view.getContext(), active);
        }
    }
}
