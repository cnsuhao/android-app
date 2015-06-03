package net.oschina.app.v2.activity.chat.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;

import com.bmob.BmobProFile;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.MainActivity;
import net.oschina.app.v2.activity.chat.adapter.AddGroupAdapter;
import net.oschina.app.v2.activity.chat.adapter.ContactAdapter;
import net.oschina.app.v2.base.BaseFragment;
import net.oschina.app.v2.base.Config;
import net.oschina.app.v2.model.chat.Avatar;
import net.oschina.app.v2.model.chat.GroupUserRelation;
import net.oschina.app.v2.model.chat.IMGroup;
import net.oschina.app.v2.model.chat.IMUser;
import net.oschina.app.v2.model.chat.UserRelation;
import net.oschina.app.v2.ui.pinned.BladeView;
import net.oschina.app.v2.ui.pinned.PinnedHeaderListView;
import net.oschina.app.v2.utils.UIHelper;
import net.oschina.app.v2.utils.WeakAsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by Tonlin on 2015/5/29.
 */
public class AddGroupFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    private static final String ALL_CHARACTER = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private String[] sections = {"#", "A", "B", "C", "D", "E", "F", "G", "H",
            "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
            "V", "W", "X", "Y", "Z"};

    private MySectionIndexer mIndexer;
    private int[] counts;
    private PinnedHeaderListView mLvContact;
    private List<IMUser> mList = new ArrayList<>();
    private AddGroupAdapter mAdapter;
    private Button mBtnOk;

    private String mDefaultAvatar;

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
        mLvContact = (PinnedHeaderListView) view.findViewById(R.id.listView);
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
        requestData();
    }

    private void requestData() {
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
            UIHelper.showNewFriend(getActivity());
        } else {
            mAdapter.toggle(position);
            mAdapter.notifyDataSetChanged();
            if (mBtnOk != null) {
                int size = mAdapter.getSelecteds().size();
                if (size == 0) {
                    mBtnOk.setText(R.string.chat_ok_zero);
                } else {
                    mBtnOk.setText(getResources().getQuantityString(R.plurals.chat_format_ok,
                            size, size));
                }
            }
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
            return;
        }
        IMUser currentUser = IMUser.getCurrentUser(getActivity(), IMUser.class);
        if (currentUser == null) {
            return;
        }
        if (list.size() == 0) {
            // 单聊
        } else {//如果多人
            // 创建DB群组信息
            createDBGroup(currentUser, list);
            // 添加群组成员

            // 发送群组邀请
        }
    }

    private void createDBGroup(final IMUser currentUser, final List<IMUser> list) {
        final IMGroup group = new IMGroup();
        group.setName("群聊(" + list.size() + ")");
        group.setOwner(currentUser);
        group.setIsPrivate(false);
        group.setDesc("");
        group.setPhoto(mDefaultAvatar);
        group.setImId(UUID.randomUUID().toString());
        group.save(getActivity(), new SaveListener() {
            @Override
            public void onSuccess() {
                AppContext.showToastShort("Save DB group success");
                createDBGroupUsers(group, currentUser, list);
            }

            @Override
            public void onFailure(int i, String s) {
                AppContext.showToastShort(s);
            }
        });
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
                createIMGroup(group, currentUser, members);
            }

            @Override
            public void onFailure(int i, String s) {
                AppContext.showToastShort(s);
            }
        });
    }

    private void createIMGroup(final IMGroup group, final IMUser currentUser, final String[] members) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final EMGroup eg = EMGroupManager.getInstance().createPublicGroup("group name",
                            group.getDesc(), members, true);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            group.setImId(eg.getGroupId());
                            group.update(getActivity(), new UpdateListener() {
                                @Override
                                public void onSuccess() {
                                    getActivity().finish();
                                }

                                @Override
                                public void onFailure(int i, String s) {
                                }
                            });
                        }
                    });
                } catch (final EaseMobException e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AppContext.showToastShort("失败了" + e.getMessage());
                        }
                    });
                }
            }
        }).start();
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
}