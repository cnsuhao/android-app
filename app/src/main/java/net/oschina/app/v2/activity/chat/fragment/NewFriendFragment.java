package net.oschina.app.v2.activity.chat.fragment;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.chat.adapter.NewFriendAdapter;
import net.oschina.app.v2.base.BaseRecycleViewFragment;
import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.chat.IMUser;
import net.oschina.app.v2.model.chat.Invite;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by Tonlin on 2015/6/1.
 */
public class NewFriendFragment extends BaseRecycleViewFragment {
    @Override
    protected RecycleBaseAdapter getListAdapter() {
        return new NewFriendAdapter();
    }

    @Override
    protected String getCacheKey() {
        return null;
    }

    @Override
    protected void sendRequestData() {
        super.sendRequestData();
        IMUser currentUser = IMUser.getCurrentUser(getActivity(), IMUser.class);
        if (currentUser == null) return;
        BmobQuery<Invite> query = new BmobQuery<>();
        query.addWhereEqualTo("from", currentUser.getObjectId());
        query.findObjects(getActivity(), new FindListener<Invite>() {
            @Override
            public void onSuccess(List<Invite> list) {
                AppContext.showToastShort("find invite end");
                executeOnLoadDataSuccess(list);
                executeOnLoadFinish();
            }

            @Override
            public void onError(int i, String s) {
                executeOnLoadDataError(s);
                executeOnLoadFinish();
            }
        });
    }
}
