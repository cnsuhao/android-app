package net.oschina.app.v2.activity.chat.fragment;

import android.text.TextUtils;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactManager;
import com.easemob.exceptions.EaseMobException;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.chat.adapter.AddFriendAdapter;
import net.oschina.app.v2.base.BaseRecycleViewFragment;
import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.chat.IMUser;
import net.oschina.app.v2.model.chat.Invite;
import net.oschina.app.v2.model.chat.UserRelation;
import net.oschina.app.v2.ui.empty.EmptyLayout;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

/**
 * Created by Tonlin on 2015/6/1.
 */
public class AddFriendFragment extends BaseRecycleViewFragment implements AddFriendAdapter.OnAddUserCallback {

    private String mName;

    public void search(String value) {
        mName = value;
        refresh();
    }

    @Override
    protected RecycleBaseAdapter getListAdapter() {
        return new AddFriendAdapter(this);
    }

    @Override
    protected boolean requestDataIfViewCreated() {
        return false;
    }

    @Override
    protected String getCacheKey() {
        return null;
    }

    @Override
    protected void requestData(boolean refresh) {
        if (TextUtils.isEmpty(mName) || IMUser.getCurrentUser(getActivity())==null)
            return;
        mErrorLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
        BmobQuery<IMUser> query = new BmobQuery<>();
        query.addWhereContains("name", mName);
        query.findObjects(getActivity(), new FindListener<IMUser>() {
            @Override
            public void onSuccess(List<IMUser> list) {
                AppContext.showToastShort("find user");
                executeOnLoadDataSuccess(list);
                executeOnLoadFinish();
            }

            @Override
            public void onError(int i, String s) {
                AppContext.showToastShort("find error");
                executeOnLoadDataError(s);
                executeOnLoadFinish();
            }
        });
    }

    @Override
    public void onAddUser(final IMUser user) {
        // 添加到DB

        final IMUser currentUser = IMUser.getCurrentUser(getActivity(),IMUser.class);
        if(currentUser != null){
            if(user.getObjectId().equals(currentUser.getObjectId())){
                AppContext.showToastShort("you can't add yourself");
                return;
            }
            // 检查不是好友
            //BmobQuery<UserRelation> ur = new BmobQuery<>();
            //ur.addWhereEqualTo("")

            // 检查还没有邀请
            BmobQuery<Invite> invite = new BmobQuery<>();
            invite.addWhereEqualTo("to", user.getObjectId());
            invite.addWhereEqualTo("from", currentUser.getObjectId());
            invite.findObjects(getActivity(), new FindListener<Invite>() {
                @Override
                public void onSuccess(List<Invite> list) {
                    if(list !=null && list.size()>0){
                        AppContext.showToastShort("请求已发送");
                    } else {
                        sendInvite(currentUser,user);
                    }
                }

                @Override
                public void onError(int i, String s) {
                    AppContext.showToastShort("请求失败");
                }
            });
        }
    }

    private void sendInvite(final IMUser from,final IMUser to){
        // 发送请求
        Invite invite = new Invite();
        invite.setTo(to);
        invite.setFrom(from);
        showWaitDialog();
        invite.save(getActivity(), new SaveListener() {
            @Override
            public void onSuccess() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            EMContactManager.getInstance().addContact(to.getImUserName(), "加个好友吧");
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AppContext.showToastShort("已发送请求");
                                    hideWaitDialog();
                                }
                            });
                        } catch (EaseMobException e) {
                            e.printStackTrace();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hideWaitDialog();
                                }
                            });
                        }
                    }
                }).start();
            }

            @Override
            public void onFailure(int i, String s) {
                AppContext.showToastShort(s);
                hideWaitDialog();
            }
        });
    }
}
