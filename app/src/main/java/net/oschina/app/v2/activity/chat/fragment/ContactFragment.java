package net.oschina.app.v2.activity.chat.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.activity.chat.adapter.ContactAdapter;
import net.oschina.app.v2.base.BaseFragment;
import net.oschina.app.v2.model.chat.IMUser;
import net.oschina.app.v2.model.chat.UserRelation;
import net.oschina.app.v2.ui.pinned.BladeView;
import net.oschina.app.v2.ui.pinned.PinnedHeaderListView;
import net.oschina.app.v2.utils.UIHelper;

import java.util.ArrayList;
import java.util.List;

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
    private List<UserRelation> mList = new ArrayList<>();
    private ContactAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.v2_fragment_chat_contact, null);

        initViews(view);

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
                    if(mIndexer == null) return;
                    int section = ALL_CHARACTER.indexOf(s);
                    int position = mIndexer.getPositionForSection(section);
                    if (position != -1) {
                        mLvContact.setSelection(position);
                    }
                }
            }
        });
        mList.add(new UserRelation("#"));
        mList.add(new UserRelation("#"));

        executeOnFinish();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(position ==0) {
            UIHelper.showNewFriend(getActivity());
        }
    }

    private void executeOnFinish() {
        counts = new int[sections.length];
        for (UserRelation city : mList) {
            String firstCharacter = city.getSortKey();
            int index = ALL_CHARACTER.indexOf(firstCharacter);
            counts[index]++;
        }

        if (mAdapter == null) {
            mIndexer = new MySectionIndexer(sections, counts);
            mAdapter = new ContactAdapter(mList, mIndexer, getActivity());
            mLvContact.setAdapter(mAdapter);
            mLvContact.setOnScrollListener(mAdapter);
            View headerView = LayoutInflater.from(getActivity()).inflate(
                    R.layout.v2_list_cell_contact_letter_header, mLvContact, false);
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
}