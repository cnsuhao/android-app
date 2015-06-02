package net.oschina.app.v2.activity.chat.fragment;

import android.view.View;

import com.easemob.chat.EMChat;
import com.easemob.chat.EMMessage;

import net.oschina.app.v2.activity.chat.MessageActivity;
import net.oschina.app.v2.activity.chat.adapter.GroupAdapter;
import net.oschina.app.v2.base.BaseRecycleViewFragment;
import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.chat.IMGroup;
import net.oschina.app.v2.model.chat.IMUser;
import net.oschina.app.v2.ui.empty.EmptyLayout;
import net.oschina.app.v2.utils.UIHelper;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by Tonlin on 2015/6/2.
 */
public class GroupFragment extends BaseRecycleViewFragment {

    @Override
    protected RecycleBaseAdapter getListAdapter() {
        return new GroupAdapter();
    }

    @Override
    protected String getCacheKey() {
        return null;
    }

    @Override
    protected void sendRequestData() {
        IMUser current = IMUser.getCurrentUser(getActivity(), IMUser.class);
        if (current == null)
            return;
        //super.sendRequestData();
        mErrorLayout.setErrorType(EmptyLayout.NETWORK_LOADING);

        BmobQuery<IMGroup> query = new BmobQuery<>();
        query.addWhereEqualTo("members",current);
        //query.addWhereRelatedTo("members",new BmobPointer(user));
        query.findObjects(getActivity(), new FindListener<IMGroup>() {
            @Override
            public void onSuccess(List<IMGroup> list) {
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

    @Override
    protected void onItemClick(View view, int position) {
        super.onItemClick(view, position);
        IMGroup group = (IMGroup) mAdapter.getItem(position);
        if(group != null) {
            UIHelper.showChatMessage(getActivity(), group.getImId(),group.getName(),
                    MessageFragment.CHATTYPE_GROUP);
        }
    }
}
