package net.oschina.app.v2.activity.chat.fragment;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactManager;
import com.easemob.exceptions.EaseMobException;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.chat.ChatHelper;
import net.oschina.app.v2.activity.chat.adapter.AddFriendAdapter;
import net.oschina.app.v2.base.BaseRecycleViewFragment;
import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.chat.IMUser;
import net.oschina.app.v2.model.chat.Invite;
import net.oschina.app.v2.model.chat.UserRelation;
import net.oschina.app.v2.ui.empty.EmptyLayout;
import net.oschina.app.v2.utils.UIHelper;
import net.oschina.app.v2.utils.WeakAsyncTask;

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
    protected void onItemClick(View view, int position) {
        super.onItemClick(view, position);
        IMUser user = (IMUser) mAdapter.getItem(position);
        if (user != null) {
            UIHelper.showUserCenter(getActivity(), user.getUid(), user.getName());
        }
    }

    @Override
    protected void requestData(boolean refresh) {
        mErrorLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
        BmobQuery<IMUser> query = new BmobQuery<>();
        query.addWhereContains("name", mName);
        query.findObjects(getActivity(), new FindListener<IMUser>() {
            @Override
            public void onSuccess(List<IMUser> list) {
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
    public void onAddUser(final IMUser user) {
        final IMUser currentUser = IMUser.getCurrentUser(getActivity(), IMUser.class);
        if (ChatHelper.needLogin()) {
            UIHelper.showLogin(getActivity());
            return;
        }
        if (user.getObjectId().equals(currentUser.getObjectId())) {
            AppContext.showToastShort(R.string.chat_tip_cant_add_yourself);
            return;
        }
        showWaitDialog();
        // 检查是否已经是好友
        BmobQuery<UserRelation> friendQuery = new BmobQuery<>();
        friendQuery.addWhereEqualTo("friend", currentUser.getObjectId());
        friendQuery.addWhereEqualTo("owner", user.getObjectId());

        BmobQuery<UserRelation> ownerQuery = new BmobQuery<>();
        ownerQuery.addWhereEqualTo("owner", currentUser.getObjectId());
        ownerQuery.addWhereEqualTo("friend", user.getObjectId());

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
                    hideWaitDialog();
                } else {
                    // 还不是好友则检查是否已经邀请
                    checkInvite(currentUser,user);
                }
            }

            @Override
            public void onError(int i, String s) {
                AppContext.showToastShort(s);
                hideWaitDialog();
            }
        });
    }

    private void checkInvite(final IMUser currentUser, final IMUser user) {
        // 检查是否已经发送邀请
        BmobQuery<Invite> invite = new BmobQuery<>();
        invite.addWhereEqualTo("to", user.getObjectId());
        invite.addWhereEqualTo("from", currentUser.getObjectId());
        invite.findObjects(getActivity(), new FindListener<Invite>() {
            @Override
            public void onSuccess(List<Invite> list) {
                hideWaitDialog();
                if (list != null && list.size() > 0) {
                    AppContext.showToastShort(R.string.chat_tip_invite_sended);
                } else {
                    sendInvite(currentUser, user);
                }
            }

            @Override
            public void onError(int i, String s) {
                AppContext.showToastShort(s);
                hideWaitDialog();
            }
        });
    }

    private void sendInvite(final IMUser from, final IMUser to) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.view_dialog_chat_invite, null);
        final AppCompatEditText text = (AppCompatEditText) view.findViewById(R.id.et_message);
        text.setSelection(text.getText().toString().length());
        new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle(R.string.chat_add_friend)
                .setView(view)
                .setPositiveButton(R.string.add_friend, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        String message = text.getText().toString();
                        addInviteToDB(from, to, message);
                    }
                })
                .setNegativeButton(R.string.cancel, null).show();
    }

    private void addInviteToDB(final IMUser from, final IMUser to, final String message) {
        showWaitDialog(R.string.progress_submit);
        Invite invite = new Invite();
        invite.setTo(to);
        invite.setFrom(from);
        invite.setMessage(message);
        showWaitDialog();
        invite.save(getActivity(), new SaveListener() {
            @Override
            public void onSuccess() {
                sendInviteNotification(from, to, message);
            }

            @Override
            public void onFailure(int i, String s) {
                AppContext.showToastShort(s);
                hideWaitDialog();
            }
        });
    }

    private void sendInviteNotification(IMUser from, IMUser to, String message) {
        new InviteIMContactTask(from, to, message, this).execute();
    }

    static class InviteIMContactTask extends WeakAsyncTask<Void, Void, Integer, AddFriendFragment> {
        private IMUser from, to;
        private String message;

        public InviteIMContactTask(IMUser from, IMUser to, String message, AddFriendFragment fragment) {
            super(fragment);
            this.from = from;
            this.to = to;
            this.message = message;
        }

        @Override
        protected Integer doInBackground(AddFriendFragment fragment, Void... params) {
            try {
                EMContactManager.getInstance().addContact(to.getImUserName(), message);
            } catch (EaseMobException e) {
                e.printStackTrace();
                return e.getErrorCode();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(AddFriendFragment fragment, Integer code) {
            super.onPostExecute(fragment, code);
            fragment.hideWaitDialog();
            if (code == 0) {
                AppContext.showToastShort(R.string.chat_tip_invite_sended);
            } else {
                AppContext.showToastShort(R.string.chat_tip_invite_send_failed);
            }
        }
    }
}
