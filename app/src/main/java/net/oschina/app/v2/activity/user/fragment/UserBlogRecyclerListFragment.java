package net.oschina.app.v2.activity.user.fragment;

import android.os.Bundle;
import android.support.v4.widget.MySwipeRefreshLayout;
import android.view.View;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.user.SwipeRefreshViewControl;
import net.oschina.app.v2.activity.user.adapter.UserBlogAdapter;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.base.BaseRecycleViewFragment;
import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.Blog;
import net.oschina.app.v2.model.BlogList;
import net.oschina.app.v2.model.ListEntity;
import net.oschina.app.v2.utils.UIHelper;

import java.io.InputStream;
import java.io.Serializable;

/**
 * Created by Tonlin on 2015/8/21.
 */
public class UserBlogRecyclerListFragment extends BaseRecycleViewFragment implements SwipeRefreshViewControl {

    private int mUid, mHisUid;
    private String mHisName;

    @Override
    protected boolean useSingleState() {
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUid = AppContext.instance().getLoginUid();
        mHisUid = getActivity().getIntent().getIntExtra("his_id", 0);
        mHisName = getActivity().getIntent().getStringExtra("his_name");
    }

    @Override
    protected String getCacheKeyPrefix() {
        return "user_" + mHisUid + "_blog";
    }

    @Override
    protected ListEntity parseList(InputStream is) throws Exception {
        return BlogList.parse(is);
    }

    @Override
    protected ListEntity readList(Serializable seri) {
        return (BlogList) seri;
    }

    @Override
    protected void sendRequestData() {
        NewsApi.getUserBlogList(mHisUid, mHisName, mUid, mCurrentPage,
                getResponseHandler());
    }

    @Override
    protected RecycleBaseAdapter getListAdapter() {
        return new UserBlogAdapter();
    }

    @Override
    public MySwipeRefreshLayout getSwipeRefreshView() {
        return mSwipeRefresh;
    }

    @Override
    protected void onItemClick(View view, int position) {
        Blog blog = (Blog) mAdapter.getItem(position);
        if (blog != null) {
            UIHelper.showUrlRedirect(view.getContext(), blog.getUrl());
        }
    }
}
