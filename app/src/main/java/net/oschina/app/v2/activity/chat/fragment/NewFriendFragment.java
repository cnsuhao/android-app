package net.oschina.app.v2.activity.chat.fragment;

import com.easemob.chat.EMChatManager;
import com.easemob.exceptions.EaseMobException;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.chat.adapter.NewFriendAdapter;
import net.oschina.app.v2.base.BaseRecycleViewFragment;
import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.chat.IMUser;
import net.oschina.app.v2.model.chat.Invite;
import net.oschina.app.v2.model.chat.UserRelation;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

/**
 * Created by Tonlin on 2015/6/1.
 */
public class NewFriendFragment extends BaseRecycleViewFragment implements NewFriendAdapter.OnOperationCallback {

    @Override
    protected RecycleBaseAdapter getListAdapter() {
        return new NewFriendAdapter(this);
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
        query.include("to,from");
        query.addWhereEqualTo("to", currentUser.getObjectId());
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

    @Override
    public void onAcceptUser(final Invite invite) {
        final IMUser currentUser = IMUser.getCurrentUser(getActivity(), IMUser.class);
        if (currentUser == null) {
            return;
        }
        showWaitDialog();
        //删除邀请信息
        invite.delete(getActivity(), new DeleteListener() {
            @Override
            public void onSuccess() {
                // addInvite
                addRelation(currentUser, invite.getFrom());
            }

            @Override
            public void onFailure(int i, String s) {
                AppContext.showToastShort(s);
                hideWaitDialog();
            }
        });
    }

    private void addRelation(final IMUser currentUser, final IMUser from) {
        UserRelation ur = new UserRelation();
        ur.setOwner(currentUser);
        ur.setFriend(from);
        ur.setSortKey("A");
        ur.save(getActivity(), new SaveListener() {
            @Override
            public void onSuccess() {
                // IM建立好友关系
                addIMRelation(from.getImUserName());
            }

            @Override
            public void onFailure(int i, String s) {
                AppContext.showToastShort(s);
                hideWaitDialog();
            }
        });
    }

    private void addIMRelation(final String from) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EMChatManager.getInstance().acceptInvitation(from);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refresh();
                            hideWaitDialog();
                        }
                    });
                } catch (final EaseMobException e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AppContext.showToastShort(e.getMessage());
                            hideWaitDialog();
                        }
                    });
                }
            }
        }).start();
    }
}
