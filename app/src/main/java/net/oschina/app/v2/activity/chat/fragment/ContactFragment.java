package net.oschina.app.v2.activity.chat.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.chat.ChatHelper;
import net.oschina.app.v2.activity.chat.adapter.ContactAdapter;
import net.oschina.app.v2.base.BaseFragment;
import net.oschina.app.v2.base.Constants;
import net.oschina.app.v2.model.chat.IMUser;
import net.oschina.app.v2.model.chat.UserRelation;
import net.oschina.app.v2.ui.empty.EmptyLayout;
import net.oschina.app.v2.ui.pinned.BladeView;
import net.oschina.app.v2.ui.pinned.PinnedHeaderListView;
import net.oschina.app.v2.utils.UIHelper;
import net.oschina.app.v2.utils.WeakAsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by Tonlin on 2015/5/29.
 */
public class ContactFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    private static final String ALL_CHARACTER = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private String[] sections = {"#", "A", "B", "C", "D", "E", "F", "G", "H",
            "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
            "V", "W", "X", "Y", "Z"};

    private MySectionIndexer mIndexer;
    private int[] counts;
    private PinnedHeaderListView mLvContact;
    private EmptyLayout mErrorLayout;
    private List<IMUser> mList = new ArrayList<>();
    private ContactAdapter mAdapter;

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
        View view = inflater.inflate(R.layout.v2_fragment_chat_contact, null);
        initViews(view);
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
        refresh();
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
                mList.clear();
                mList.add(new IMUser());
                mList.add(new IMUser());
            }
        });
    }

    private void startParserUR2User(List<UserRelation> list) {
        IMUser currentUser = IMUser.getCurrentUser(getActivity(), IMUser.class);
        new ParserUserRelationTask(list, currentUser, this).execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            UIHelper.showNewFriend(getActivity());
        } else if (position == 1) {
            UIHelper.showMyGroups(getActivity());
        } else {
            IMUser user = (IMUser) mAdapter.getItem(position);
            if (user != null) {
                UIHelper.showChatMessage(getActivity(), user.getImUserName(),
                        user.getName(), MessageFragment.CHATTYPE_SINGLE);
            }
        }
    }

    private void executeOnFinish() {
        counts = new int[sections.length];
        for (IMUser ur : mList) {
            String firstCharacter = ur.getSortKey();
            int index = ALL_CHARACTER.indexOf(firstCharacter);
            counts[index]++;
        }

        if (mAdapter == null) {
            mIndexer = new MySectionIndexer(sections, counts);
            mAdapter = new ContactAdapter(mList, mIndexer, getActivity());
            mLvContact.setAdapter(mAdapter);
            mLvContact.setOnScrollListener(mAdapter);
            View headerView = LayoutInflater.from(getActivity()).inflate(
                    R.layout.v2_list_cell_chat_contact_letter_header, mLvContact, false);
            mLvContact.setPinnedHeaderView(headerView);
        } else if (mAdapter != null) {
            mIndexer = new MySectionIndexer(sections, counts);
            mAdapter.setIndexer(mIndexer);
            mAdapter.setData(mList);
            mLvContact.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        }

        mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
    }

    static class ParserUserRelationTask extends WeakAsyncTask<Void, Void, List<IMUser>, ContactFragment> {
        private final IMUser mCurrentUser;
        private List<UserRelation> mList;

        public ParserUserRelationTask(List<UserRelation> list, IMUser currentUser, ContactFragment fragment) {
            super(fragment);
            mList = list;
            mCurrentUser = currentUser;
        }

        @Override
        protected List<IMUser> doInBackground(ContactFragment fragment, Void... params) {
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
        protected void onPostExecute(ContactFragment fragment, List<IMUser> list) {
            super.onPostExecute(fragment, list);
            fragment.mList.clear();
            fragment.mList.add(new IMUser());
            fragment.mList.add(new IMUser());
            fragment.mList.addAll(list);
            fragment.executeOnFinish();
        }
    }
}