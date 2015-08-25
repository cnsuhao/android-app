package net.oschina.app.v2.activity.friend;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.MySwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.friend.adapter.SearchFriendAdapter;
import net.oschina.app.v2.activity.friend.adapter.SelectFriendAdapter;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.base.BaseActivity;
import net.oschina.app.v2.base.Constants;
import net.oschina.app.v2.cache.v2.CacheManager;
import net.oschina.app.v2.model.Friend;
import net.oschina.app.v2.model.FriendList;
import net.oschina.app.v2.ui.AvatarView;
import net.oschina.app.v2.ui.empty.EmptyLayout;
import net.oschina.app.v2.ui.pinned.BladeView;
import net.oschina.app.v2.ui.pinned.PinnedHeaderListView;
import net.oschina.app.v2.utils.GetPinYinUtil;
import net.oschina.app.v2.utils.TDevice;
import net.oschina.app.v2.utils.UIHelper;
import net.oschina.app.v2.utils.WeakAsyncTask;
import net.oschina.app.v2.utils.XmlUtils;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Tonlin on 2015/8/25.
 */
public class SelectFriendsActivity extends BaseActivity implements AdapterView.OnItemClickListener, MySwipeRefreshLayout.OnRefreshListener {

    private static final String ALL_CHARACTER = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final static int MAX_SELECT_SIZE = 10;
    public static final String KEY_SELECTED = "key_selected";
    private static String[] sections = {"#", "A", "B", "C", "D", "E", "F", "G", "H",
            "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
            "V", "W", "X", "Y", "Z"};

    private MySwipeRefreshLayout mRefreshView;
    private PinnedHeaderListView mLvContact;
    private BladeView mLetterListView;
    private EmptyLayout mErrorLayout;
    private LinearLayout mLyAvatarContainer;
    private HorizontalScrollView mHsContainer;
    private EditText mEtSearch;
    private Button mBtnOk;

    private TextView mTvEmptySearch;
    private ListView mLvSearch;
    private View mSearchContainer;
    private SearchFriendAdapter mSearchAdapter;

    private MySectionIndexer mIndexer;
    private int[] counts;
    private List<Friend> mList = new ArrayList<>();
    private ArrayList<Friend> mSelecteds = new ArrayList<>();
    private SelectFriendAdapter mAdapter;
    private boolean mIsWaitingLogin;
    private ParserDataTask mParserTask;
    private Handler mScrollHandler = new Handler();


    private AsyncHttpResponseHandler mHandler = new AsyncHttpResponseHandler() {
        @Override
        public void onSuccess(int i, Header[] headers, byte[] bytes) {
            executeParserTask(bytes, false);
        }

        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
            throwable.printStackTrace();
            executeOnLoadDataError(throwable.getMessage());
            executeOnLoadFinish();
        }
    };

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (TextUtils.isEmpty(s) || mList == null || mList.size() == 0) {
                mSearchContainer.setVisibility(View.GONE);
                mTvEmptySearch.setVisibility(View.GONE);
                mLvSearch.setVisibility(View.GONE);

                mRefreshView.setVisibility(View.VISIBLE);
                mLetterListView.setVisibility(View.VISIBLE);
                return;
            }
            if (mLyAvatarContainer.getChildCount() > 0) {
                View v = mLyAvatarContainer.getChildAt(mLyAvatarContainer.getChildCount() - 1);
                ImageView clover = (ImageView) v.findViewById(R.id.iv_overlay);
                clover.setVisibility(View.GONE);
            }

            mSearchContainer.setVisibility(View.VISIBLE);
            mRefreshView.setVisibility(View.GONE);
            mLetterListView.setVisibility(View.GONE);

            new SearchTask(SelectFriendsActivity.this, s + "", mList).execute();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void executeWithResult(ArrayList<Friend> result) {
        if (result == null || result.size() == 0) {
            mLvSearch.setVisibility(View.GONE);
            mTvEmptySearch.setVisibility(View.VISIBLE);
        } else {
            mTvEmptySearch.setVisibility(View.GONE);
            mLvSearch.setVisibility(View.VISIBLE);
            mSearchAdapter.setData(result);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.v2_activity_select_friends;
    }

    @Override
    protected int getActionBarCustomView() {
        return R.layout.v2_actionbar_select_friends;
    }

    @Override
    protected void initActionBar(Toolbar actionBar) {
        super.initActionBar(actionBar);
        mBtnOk = (Button) actionBar.findViewById(R.id.btn_ok);
        mBtnOk.setOnClickListener(this);
    }

    @Override
    protected boolean hasBackButton() {
        return true;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        setActionBarTitle("选择@好友");

        mHsContainer = (HorizontalScrollView) findViewById(R.id.hs);
        mLyAvatarContainer = (LinearLayout) findViewById(R.id.ly_avatar_container);

        mEtSearch = (EditText) findViewById(R.id.et_search);
        mEtSearch.addTextChangedListener(mTextWatcher);
        mTvEmptySearch = (TextView) findViewById(R.id.tv_empty_search);
        mSearchContainer = findViewById(R.id.rl_search);
        mLvSearch = (ListView) findViewById(R.id.lv_search);
        mLvSearch.setOnItemClickListener(this);

        mSearchAdapter = new SearchFriendAdapter(mSelecteds);
        mLvSearch.setAdapter(mSearchAdapter);

        mErrorLayout = (EmptyLayout) findViewById(R.id.error_layout);
        mErrorLayout.setOnLayoutClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!mIsWaitingLogin) {
                    refresh();
                } else {
                    UIHelper.showLogin(SelectFriendsActivity.this);
                }
            }
        });

        mRefreshView = (MySwipeRefreshLayout) findViewById(R.id.srl_refresh);
        mRefreshView.setOnRefreshListener(this);

        mLvContact = (PinnedHeaderListView) findViewById(R.id.listView);
        mLvContact.setOnItemClickListener(this);

        mLetterListView = (BladeView) findViewById(R.id.bv);
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

        counts = new int[sections.length];
        executeOnLoadDataSuccess(counts, mList);

        refresh();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Friend item = null;
        if (mLvSearch.getVisibility() == View.VISIBLE)
            item = (Friend) mSearchAdapter.getItem(position);
        else
            item = (Friend) mAdapter.getItem(position);

        if (item != null && !item.isSelected() && mSelecteds.size() >= MAX_SELECT_SIZE) {
            AppContext.showToastShort("最多只能选择10个好友哟~");
            return;
        }

        if (item != null) {
            item.setSelected(!item.isSelected());
            if (item.isSelected())
                mSelecteds.add(item);
            else
                mSelecteds.remove(item);

            mAdapter.notifyDataSetChanged();
            mSearchAdapter.notifyDataSetChanged();

            if (item.isSelected()) {
                addSelectedUser(item);
            } else {
                removeSelectedUser(item);
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            if (TextUtils.isEmpty(mEtSearch.getText()) && mLyAvatarContainer.getChildCount() > 0) {
                View v = mLyAvatarContainer.getChildAt(mLyAvatarContainer.getChildCount() - 1);
                Friend item = (Friend) v.getTag();
                ImageView clover = (ImageView) v.findViewById(R.id.iv_overlay);
                if (clover.getVisibility() == View.GONE) {
                    clover.setVisibility(View.VISIBLE);
                } else if (clover.getVisibility() == View.VISIBLE) {
                    mSelecteds.remove(item);
                    mAdapter.notifyDataSetChanged();
                    mSearchAdapter.notifyDataSetChanged();

                    removeSelectedUser(item);
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void removeSelectedUser(Friend item) {
        final int size = mLyAvatarContainer.getChildCount();
        for (int i = 0; i < size; i++) {
            View v = mLyAvatarContainer.getChildAt(i);
            Friend f = (Friend) v.getTag();
            if (f != null && f.getUserid() == item.getUserid()) {
                mLyAvatarContainer.removeView(v);
                fixHSWidth();
                return;
            }
        }
    }

    private void addSelectedUser(final Friend item) {
        if (mLyAvatarContainer.getChildCount() > 0) {
            View v = mLyAvatarContainer.getChildAt(mLyAvatarContainer.getChildCount() - 1);
            ImageView clover = (ImageView) v.findViewById(R.id.iv_overlay);
            clover.setVisibility(View.GONE);
        }

        View v = LayoutInflater.from(this).inflate(R.layout.v2_list_cell_selected_item, null);
        AvatarView avatar = (AvatarView) v.findViewById(R.id.iv_avatar);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        if (mLyAvatarContainer.getChildCount() != 0)
            params.leftMargin = (int) TDevice.dpToPixel(6);
        v.setTag(item);
        mLyAvatarContainer.addView(v, params);
        avatar.setAvatarUrl(item.getPortrait());
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelecteds.remove(item);
                mAdapter.notifyDataSetChanged();
                mSearchAdapter.notifyDataSetChanged();

                removeSelectedUser(item);
            }
        });
        fixHSWidth();
    }

    private void fixHSWidth() {
        float maxWidth = TDevice.getScreenWidth() * 0.8f;
        float width = TDevice.dpToPixel(40) * mLyAvatarContainer.getChildCount()
                + TDevice.dpToPixel(6) * (mLyAvatarContainer.getChildCount() - 1);
        if (width >= maxWidth) {
            mHsContainer.setLayoutParams(new LinearLayout.LayoutParams((int) maxWidth,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
        } else {
            mHsContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
        }
        mScrollHandler.post(new Runnable() {
            @Override
            public void run() {
                mHsContainer.fullScroll(ScrollView.FOCUS_RIGHT);
            }
        });

        updateSelectedUI();
    }

    private void updateSelectedUI() {
        int size = mSelecteds.size();
        if (size > 0) {
            mBtnOk.setText("确定(" + size + "/" + MAX_SELECT_SIZE + ")");
            mBtnOk.setEnabled(true);
        } else {
            mBtnOk.setText("确定");
            mBtnOk.setEnabled(false);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_ok) {
            Intent intent = new Intent();
            intent.putExtra(SelectFriendsActivity.KEY_SELECTED,mSelecteds);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    private void refresh() {
        if (!AppContext.instance().isLogin()) {
            mIsWaitingLogin = true;
            mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
            mErrorLayout.setErrorMessage(getString(R.string.unlogin_tip));
            return;
        } else {
            mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
        }
        if (mList == null || mList.size() == 0) {
            mErrorLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
        }
        requestFromNetwork();
    }

    private void requestFromNetwork() {
        NewsApi.getAllFriends(AppContext.getLoginUid(), mHandler);
    }

    private void executeOnLoadDataSuccess(int[] counts, List<Friend> list) {
        this.counts = counts;
        this.mList = list;

        if (mAdapter == null) {
            mIndexer = new MySectionIndexer(sections, counts);
            mAdapter = new SelectFriendAdapter(mList, mSelecteds, mIndexer, this);
            mLvContact.setAdapter(mAdapter);
            mLvContact.setOnScrollListener(mAdapter);
            View headerView = LayoutInflater.from(this).inflate(
                    R.layout.v2_list_cell_select_friend_letter_header, mLvContact, false);
            mLvContact.setPinnedHeaderView(headerView);

            mAdapter.notifyDataSetChanged();
        } else if (mAdapter != null) {
            mIndexer = new MySectionIndexer(sections, counts);
            mAdapter.setIndexer(mIndexer);
            mAdapter.setData(mList);
            mLvContact.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        }
        mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
    }

    private void executeOnLoadDataError(String errorMsg) {
        mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
    }

    private void executeOnLoadFinish() {
        mRefreshView.setRefreshing(false);
    }

    public String getCacheKey() {
        return "friends_" + AppContext.getLoginUid();
    }

    public long getCacheExpire() {
        return Constants.CACHE_EXPIRE_OND_DAY;
    }

    private void executeParserTask(byte[] data, boolean fromCache) {
        cancelParserTask();
        mParserTask = new ParserDataTask(this, data, fromCache);
        mParserTask.execute();
    }

    private void cancelParserTask() {
        if (mParserTask != null) {
            mParserTask.cancel(true);
            mParserTask = null;
        }
    }

    static class ParserDataTask extends WeakAsyncTask<Void, Void, Void, SelectFriendsActivity> {
        private byte[] responseData;
        private boolean parserError;
        private List<Friend> list;
        private int[] counts;
        private boolean fromCache;

        public ParserDataTask(SelectFriendsActivity act, byte[] responseData, boolean fromCache) {
            super(act);
            this.responseData = responseData;
            this.fromCache = fromCache;
        }

        @Override
        protected Void doInBackground(SelectFriendsActivity act, Void... params) {
            FriendList friends = XmlUtils.toBean(FriendList.class, responseData);
            try {
                if (friends != null) {
                    list = friends.getFriendlist();
                    if (list != null && list.size() > 0) {

                        Collections.sort(list, new Comparator<Friend>() {
                            @Override
                            public int compare(Friend lhs, Friend rhs) {
                                return lhs.getSortKey().compareTo(rhs.getSortKey());
                            }
                        });

                        counts = new int[sections.length];
                        for (Friend ur : list) {
                            String firstCharacter = ur.getSortKey();
                            int index = ALL_CHARACTER.indexOf(firstCharacter);
                            counts[index]++;
                        }

                        if (!fromCache) {
                            CacheManager.setCache(act.getCacheKey(), responseData,
                                    act.getCacheExpire(), CacheManager.TYPE_INTERNAL);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                parserError = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(SelectFriendsActivity act, Void aVoid) {
            super.onPostExecute(act, aVoid);
            if (parserError) {
                if (fromCache) {
                    act.requestFromNetwork();
                } else {
                    act.executeOnLoadDataError(null);
                }
            } else {
                act.executeOnLoadDataSuccess(counts, list);
            }
            act.executeOnLoadFinish();
        }
    }

    static class SearchTask extends WeakAsyncTask<Void, Void, Void, SelectFriendsActivity> {
        private String searchKey;
        private List<Friend> list;
        private ArrayList<Friend> result = new ArrayList<>();

        public SearchTask(SelectFriendsActivity act, String searchKey, List<Friend> list) {
            super(act);
            this.searchKey = searchKey;
            this.list = list;
        }

        @Override
        protected Void doInBackground(SelectFriendsActivity act, Void... params) {
            for (Friend f : list) {
                if (f.getName().toUpperCase().contains(searchKey.toUpperCase())
                        || f.getPinYinHeader().contains(searchKey.toUpperCase())
                        || f.getPinYin().contains(searchKey.toUpperCase())) {
                    result.add(f);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(SelectFriendsActivity act, Void aVoid) {
            super.onPostExecute(act, aVoid);
            act.executeWithResult(result);
        }
    }

}
