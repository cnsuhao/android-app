package net.oschina.app.v2.activity.chat.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;

import com.bmob.BmobProFile;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.chat.ChatHelper;
import net.oschina.app.v2.activity.chat.adapter.AddGroupAdapter;
import net.oschina.app.v2.base.BaseFragment;
import net.oschina.app.v2.base.Config;
import net.oschina.app.v2.base.Constants;
import net.oschina.app.v2.model.chat.Avatar;
import net.oschina.app.v2.model.chat.IMGroup;
import net.oschina.app.v2.model.chat.IMUser;
import net.oschina.app.v2.model.chat.UserRelation;
import net.oschina.app.v2.ui.empty.EmptyLayout;
import net.oschina.app.v2.ui.pinned.BladeView;
import net.oschina.app.v2.ui.pinned.PinnedHeaderListView;
import net.oschina.app.v2.utils.TLog;
import net.oschina.app.v2.utils.UIHelper;
import net.oschina.app.v2.utils.WeakAsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by Tonlin on 2015/5/29.
 */
public class AddGroupFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    private static final String ALL_CHARACTER = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final java.lang.String TAG = "AddGroupFragment";

    private String[] sections = {"#", "A", "B", "C", "D", "E", "F", "G", "H",
            "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
            "V", "W", "X", "Y", "Z"};

    private MySectionIndexer mIndexer;
    private int[] counts;
    private PinnedHeaderListView mLvContact;
    private List<IMUser> mList = new ArrayList<>();
    private AddGroupAdapter mAdapter;
    private Button mBtnOk;
    private EmptyLayout mErrorLayout;
    private EditText mEtSearch;
    private String mDefaultAvatar;

    private boolean mIsWatingLogin;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.INTENT_ACTION_LOGIN)) {
                refresh();
            } else {
                if (mErrorLayout != null) {
                    mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
                    mErrorLayout.setErrorMessage(getString(R.string.unlogin_tip));
                }
                mIsWatingLogin = true;
            }
        }
    };
    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            TLog.log(TAG, "search change:" + charSequence);
            //mLvContact.setFilterText(charSequence+"");
            if (mAdapter != null) {
                new StartFilterAdapterTask(charSequence + "", mAdapter.getSourcList(),
                        AddGroupFragment.this).execute();
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter(Constants.INTENT_ACTION_LOGOUT);
        filter.addAction(Constants.INTENT_ACTION_LOGIN);
        getActivity().registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.v2_fragment_chat_add_group, null);

        initViews(view);

        // load default avatar
        BmobQuery<Avatar> query = new BmobQuery<>();
        query.addWhereEqualTo("sourceName", new Random().nextInt(Config.DEFAULT_AVATAR_SIZE) + ".png");
        query.findObjects(getActivity(), new FindListener<Avatar>() {
            @Override
            public void onSuccess(List<Avatar> list) {
                if (list != null && list.size() > 0) {
                    Avatar a = list.get(0);
                    mDefaultAvatar = BmobProFile.getInstance(getActivity())
                            .signURL(a.getFileName(), a.getUrl(), Config.BMOB_ACCESS_KEY, 0, null);
                }
            }

            @Override
            public void onError(int i, String s) {
            }
        });
        return view;
    }

    private void initViews(View view) {
        mErrorLayout = (EmptyLayout) view.findViewById(R.id.error_layout);
        mErrorLayout.setOnLayoutClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!mIsWatingLogin) {
                    refresh();
                } else {
                    UIHelper.showLogin(getActivity());
                }
            }
        });

        mEtSearch = (EditText) view.findViewById(R.id.et_search);
        mEtSearch.addTextChangedListener(mTextWatcher);

        mLvContact = (PinnedHeaderListView) view.findViewById(R.id.listView);
        //mLvContact.setTextFilterEnabled(true);
        mLvContact.setOnItemClickListener(this);

        BladeView mLetterListView = (BladeView) view
                .findViewById(R.id.bv);
        mLetterListView.setOnItemClickListener(new BladeView.OnItemClickListener() {

            @Override
            public void onItemClick(String s) {
                if (s != null) {
                    if (mIndexer == null) return;
                    int section = ALL_CHARACTER.indexOf(s);
                    int position = mIndexer.getPositionForSection(section);
                    if (position != -1) {
                        mLvContact.setSelection(position);
                    }
                }
            }
        });

        executeOnFinish();
        refresh();
    }

    private void updateOkButton() {
        if (mBtnOk == null || mAdapter == null) return;
        if (mAdapter.getSelecteds().size() == 0 || ChatHelper.needLogin()) {
            //mBtnOk.setEnabled(false);
        } else {
            //mBtnOk.setEnabled(true);
        }
        int size = mAdapter.getSelecteds().size();
        if (size == 0) {
            mBtnOk.setText(R.string.chat_ok_zero);
        } else {
            mBtnOk.setText(getResources().getQuantityString(R.plurals.chat_format_ok,
                    size, size));
        }
    }

    private void refresh() {
        if (ChatHelper.needLogin()) {
            mIsWatingLogin = true;
            mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
            mErrorLayout.setErrorMessage(getString(R.string.unlogin_tip));
            return;
        } else {
            mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
        }

        IMUser currentUser = IMUser.getCurrentUser(getActivity(), IMUser.class);
        if (currentUser == null)
            return;
        BmobQuery<UserRelation> query = new BmobQuery<>();
        query.addWhereEqualTo("owner", currentUser.getObjectId());

        BmobQuery<UserRelation> query2 = new BmobQuery<>();
        query2.addWhereEqualTo("friend", currentUser.getObjectId());

        List<BmobQuery<UserRelation>> queries = new ArrayList<>();
        queries.add(query);
        queries.add(query2);

        BmobQuery<UserRelation> mainQuery = new BmobQuery<>();
        mainQuery.or(queries);
        mainQuery.include("owner,friend");
        mainQuery.findObjects(getActivity(), new FindListener<UserRelation>() {
            @Override
            public void onSuccess(List<UserRelation> list) {
                startParserUR2User(list);
                executeOnFinish();
            }

            @Override
            public void onError(int i, String s) {
                AppContext.showToastShort(s);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            UIHelper.showMyGroups(getActivity());
        } else {
            mAdapter.toggle(position);
            mAdapter.notifyDataSetChanged();
            updateOkButton();
        }
    }

    private void executeOnFinish() {
        counts = new int[sections.length];
        for (IMUser user : mList) {
            String firstCharacter = user.getSortKey();
            int index = ALL_CHARACTER.indexOf(firstCharacter);
            counts[index]++;
        }

        if (mAdapter == null) {
            mIndexer = new MySectionIndexer(sections, counts);
            mAdapter = new AddGroupAdapter(mList, mIndexer, getActivity());
            mLvContact.setAdapter(mAdapter);
            mLvContact.setOnScrollListener(mAdapter);
            View headerView = LayoutInflater.from(getActivity()).inflate(
                    R.layout.v2_list_cell_chat_contact_letter_header, mLvContact, false);
            View v = headerView.findViewById(R.id.group_title);
            mLvContact.setPinnedHeaderView(headerView);
        } else if (mAdapter != null) {
            mIndexer = new MySectionIndexer(sections, counts);
            mAdapter.setIndexer(mIndexer);
            mAdapter.setData(mList);
            mLvContact.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        }
        // mErrorLayout.setErrorType(AHErrorLayout.HIDE_LAYOUT);

        updateOkButton();
    }

    public void setOKButton(Button btnOk) {
        mBtnOk = btnOk;
        mBtnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                handleCreateGroup();
            }
        });
    }

    private void handleCreateGroup() {
        List<IMUser> list = mAdapter.getSelecteds();
        if (list.size() == 0) {
            AppContext.showToastShort(R.string.chat_tip_selecte_users);
            return;
        }
        IMUser currentUser = IMUser.getCurrentUser(getActivity(), IMUser.class);
        if (currentUser == null) {
            return;
        }
        if (list.size() == 1) {
            // 单聊
            IMUser user = list.get(0);
            UIHelper.showChatMessage(getActivity(), user.getImUserName(),
                    user.getName(), MessageFragment.CHATTYPE_SINGLE);
        } else {//如果多人
            createDBGroup(currentUser, list);
        }
    }

    private void createDBGroup(final IMUser currentUser, final List<IMUser> list) {
        showWaitDialog(R.string.progress_submit);
        final IMGroup group = new IMGroup();
        group.setName(generateDefaultGroupName(list));
        group.setOwner(currentUser);
        group.setIsPrivate(false);
        group.setDesc("");
        group.setPhoto(mDefaultAvatar);
        group.setImId("");
        group.save(getActivity(), new SaveListener() {
            @Override
            public void onSuccess() {
                TLog.log(TAG, "创建DB群组成功,准备创建群成员关系");
                createDBGroupUsers(group, currentUser, list);
            }

            @Override
            public void onFailure(int i, String s) {
                AppContext.showToastShort(s);
                hideWaitDialog();
            }
        });
    }

    private String generateDefaultGroupName(List<IMUser> list) {
        return list.get(0).getName() + "、" + list.get(1).getName() + "...";
    }

    private void createDBGroupUsers(final IMGroup group, final IMUser currentUser,
                                    final List<IMUser> users) {
        final String[] members = new String[users.size()];
        int i = 0;
        BmobRelation relation = new BmobRelation();
        for (IMUser user : users) {
            members[i++] = user.getImUserName();
            relation.add(user);
        }
        relation.add(currentUser);
        group.setMembers(relation);
        group.update(getActivity(), new UpdateListener() {
            @Override
            public void onSuccess() {
                createIMGroup(group, members);
            }

            @Override
            public void onFailure(int i, String s) {
                AppContext.showToastShort(s);
                deleteDBGroup(group);
                hideWaitDialog();
            }
        });
    }

    private void deleteDBGroup(IMGroup group) {
        group.delete(getActivity(), new DeleteListener() {
            @Override
            public void onSuccess() {
                TLog.log(TAG, "DB群组信息已删除");
            }

            @Override
            public void onFailure(int i, String s) {
                TLog.log(TAG, "DB群组信息删除失败," + s);
            }
        });
    }

    private void showGroupChat(IMGroup g) {
        UIHelper.showChatMessage(getActivity(), g.getImId(), g.getName(), MessageFragment.CHATTYPE_GROUP);
    }

    private void createIMGroup(final IMGroup group, final String[] members) {
        new CreateIMGroupTask(group, members, this).execute();
    }

    private void startParserUR2User(List<UserRelation> list) {
        IMUser currentUser = IMUser.getCurrentUser(getActivity(), IMUser.class);
        new ParserUserRelationTask(list, currentUser, this).execute();
    }

    static class ParserUserRelationTask extends WeakAsyncTask<Void, Void, List<IMUser>, AddGroupFragment> {
        private final IMUser mCurrentUser;
        private List<UserRelation> mList;

        public ParserUserRelationTask(List<UserRelation> list, IMUser currentUser, AddGroupFragment fragment) {
            super(fragment);
            mList = list;
            mCurrentUser = currentUser;
        }

        @Override
        protected List<IMUser> doInBackground(AddGroupFragment fragment, Void... params) {
            List<IMUser> list = new ArrayList<>();
            if (mList == null || mList.size() == 0 || mCurrentUser == null) return list;
            for (UserRelation item : mList) {
                IMUser user = null;
                if (item.getFriend() != null && !item.getFriend()
                        .getObjectId().equals(mCurrentUser.getObjectId())) {
                    user = item.getFriend();
                }
                if (item.getOwner() != null && !item.getOwner()
                        .getObjectId().equals(mCurrentUser.getObjectId())) {
                    user = item.getOwner();
                }
                if (user != null) {
                    list.add(user);
                }
            }

            // sort list
            Collections.sort(list, new Comparator<IMUser>() {
                @Override
                public int compare(IMUser lhs, IMUser rhs) {
                    return lhs.getSortKey().compareTo(rhs.getSortKey());
                }
            });

            return list;
        }

        @Override
        protected void onPostExecute(AddGroupFragment fragment, List<IMUser> list) {
            super.onPostExecute(fragment, list);
            fragment.mList.clear();
            fragment.mList.add(new IMUser());
            fragment.mList.addAll(list);
            fragment.executeOnFinish();
        }
    }

    static class CreateIMGroupTask extends WeakAsyncTask<Void, Void, EMGroup, AddGroupFragment> {
        private String[] members;
        private IMGroup mGroup;

        public CreateIMGroupTask(IMGroup group, String[] members, AddGroupFragment fragment) {
            super(fragment);
            this.members = members;
            this.mGroup = group;
        }

        @Override
        protected EMGroup doInBackground(AddGroupFragment fragment, Void... params) {
            EMGroup eg = null;
            try {
                eg = EMGroupManager.getInstance().createPublicGroup(
                        mGroup.getName(),
                        mGroup.getDesc(), members, true);
            } catch (EaseMobException e) {
                e.printStackTrace();
            }
            return eg;
        }

        @Override
        protected void onPostExecute(AddGroupFragment fragment, EMGroup group) {
            super.onPostExecute(fragment, group);
            if (group != null) {
                mGroup.setImId(group.getGroupId());
                fragment.updateDBGroup(mGroup);
            } else {
                fragment.hideWaitDialog();
                AppContext.showToastShort(R.string.chat_tip_create_group_failed);
                fragment.deleteDBGroup(this.mGroup);
            }
        }
    }

    private void updateDBGroup(final IMGroup group) {
        group.update(getActivity(), new UpdateListener() {
            @Override
            public void onSuccess() {
                hideWaitDialog();
                getActivity().finish();
                showGroupChat(group);
            }

            @Override
            public void onFailure(int i, String s) {
                AppContext.showToastShort(s);
                hideWaitDialog();
            }
        });
    }

    static class StartFilterAdapterTask extends WeakAsyncTask<Void, Void, List<IMUser>, AddGroupFragment> {
        private final List<IMUser> sourceList;
        private String filter;

        public StartFilterAdapterTask(String filter, List<IMUser> sourceList, AddGroupFragment fragment) {
            super(fragment);
            this.filter = filter;
            this.sourceList = sourceList;
        }

        @Override
        protected List<IMUser> doInBackground(AddGroupFragment fragment, Void... params) {
            List<IMUser> newValues = new ArrayList<>();
            // 如果搜索框内容为空，就恢复原始数据
            if (TextUtils.isEmpty(filter)) {
                newValues = sourceList;
            } else {
                // 过滤出新数据
                for (IMUser user : sourceList) {
                    if(TextUtils.isEmpty(user.getName()))
                        continue;
                    if (-1 != user.getName().toLowerCase().indexOf(filter)) {
                        newValues.add(user);
                    }
                }
            }
            return newValues;
        }

        @Override
        protected void onPostExecute(AddGroupFragment fragment, List<IMUser> imUsers) {
            super.onPostExecute(fragment, imUsers);
            fragment.mAdapter.setFilter(imUsers);
        }
    }
}