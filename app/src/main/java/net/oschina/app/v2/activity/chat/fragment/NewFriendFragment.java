package net.oschina.app.v2.activity.chat.fragment;

import com.easemob.chat.EMChatManager;
import com.easemob.exceptions.EaseMobException;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.chat.adapter.NewFriendAdapter;
import net.oschina.app.v2.base.BaseRecycleViewFragment;
import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.chat.IMUser;
import net.oschina.app.v2.model.chat.Invite;
import net.oschina.app.v2.model.chat.UserRelation;
import net.oschina.app.v2.utils.WeakAsyncTask;

import java.util.ArrayList;
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
        showWaitDialog(R.string.progress_submit);

        //先检查是否已经是好友关系
        BmobQuery<UserRelation> friendQuery = new BmobQuery<>();
        friendQuery.addWhereEqualTo("friend", currentUser.getObjectId());
        friendQuery.addWhereEqualTo("owner", invite.getFrom().getObjectId());

        BmobQuery<UserRelation> ownerQuery = new BmobQuery<>();
        ownerQuery.addWhereEqualTo("owner", currentUser.getObjectId());
        ownerQuery.addWhereEqualTo("friend", invite.getFrom().getObjectId());

        List<BmobQuery<UserRelation>> queries = new ArrayList<>();
        queries.add(friendQuery);
        queries.add(ownerQuery);

        BmobQuery<UserRelation> mainQuery = new BmobQuery<>();
        mainQuery.or(queries);
        mainQuery.findObjects(getActivity(), new FindListener<UserRelation>() {
            @Override
            public void onSuccess(List<UserRelation> list) {
                if (list != null && list.size() > 0) {
                    AppContext.showToastShort("你们已经是好友了");
                    // 删除可能存在的邀请
                    deleteInvite(invite);

                    hideWaitDialog();
                } else {
                    // 还不是好友则添加好友关系
                    addRelation(currentUser, invite);
                }
            }

            @Override
            public void onError(int i, String s) {
                AppContext.showToastShort(s);
                hideWaitDialog();
            }
        });
    }

    private void deleteInvite(final Invite invite) {
        //删除邀请信息
        invite.delete(getActivity(), new DeleteListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int i, String s) {
                AppContext.showToastShort(s);
                hideWaitDialog();
            }
        });
    }

    /**
     * 添加好友关系到DB
     */
    private void addRelation(final IMUser currentUser, final Invite invite) {
        UserRelation ur = new UserRelation();
        ur.setOwner(currentUser);
        ur.setFriend(invite.getFrom());
        ur.save(getActivity(), new SaveListener() {
            @Override
            public void onSuccess() {
                // 添加好友关系成功后，建立IM好友关系
                addIMRelation(invite);
            }

            @Override
            public void onFailure(int i, String s) {
                AppContext.showToastShort(s);
                hideWaitDialog();
            }
        });
    }

    /**
     * IM建立好友关系
     */
    private void addIMRelation(Invite invite) {
        new AddIMRelationShipTask(invite, invite.getFrom().getImUserName(), this).execute();
    }

    static class AddIMRelationShipTask extends WeakAsyncTask<Void,
            Void, Integer, NewFriendFragment> {
        private String from;
        private Invite invite;

        public AddIMRelationShipTask(Invite invite, String from, NewFriendFragment fragment) {
            super(fragment);
            this.from = from;
            this.invite = invite;
        }

        @Override
        protected Integer doInBackground(NewFriendFragment fragment, Void... params) {
            try {
                EMChatManager.getInstance().acceptInvitation(from);
                return 0;
            } catch (EaseMobException e) {
                return e.getErrorCode();
            }
        }

        @Override
        protected void onPostExecute(NewFriendFragment fragment, Integer code) {
            super.onPostExecute(fragment, code);
            fragment.hideWaitDialog();
            if (code == 0) {
                fragment.deleteInvite(invite);
                fragment.refresh();
            } else {
                AppContext.showToastShort("操作失败");
            }
        }
    }
}
