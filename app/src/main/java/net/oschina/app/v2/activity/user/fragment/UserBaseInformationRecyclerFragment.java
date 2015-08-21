package net.oschina.app.v2.activity.user.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.user.UserDataControl;
import net.oschina.app.v2.activity.user.adapter.UserBaseInfoAdapter;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.base.BaseFragment;
import net.oschina.app.v2.cache.CacheManager;
import net.oschina.app.v2.model.User;
import net.oschina.app.v2.model.UserInformation;
import net.oschina.app.v2.utils.TDevice;
import net.oschina.app.v2.utils.TLog;
import net.oschina.app.v2.utils.UIHelper;

import org.apache.http.Header;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;

/**
 * Created by Tonlin on 2015/8/21.
 */
public class UserBaseInformationRecyclerFragment extends BaseFragment implements UserBaseInfoAdapter.OnSingleViewClickListener {

    private static final String TAG = "User";
    private RecyclerView mRecycleView;
    private UserBaseInfoAdapter mAdapter;

    private int mUid, mHisUid;
    private String mHisName;
    private User mUser;
    private AsyncTask<String, Void, UserInformation> mCacheTask;

    private AsyncHttpResponseHandler mInfoHandler = new AsyncHttpResponseHandler() {

        @Override
        public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
            try {
                UserInformation information = UserInformation
                        .parse(new ByteArrayInputStream(arg2));
                UIHelper.sendNoticeBroadcast(getActivity(), information);
                mUser = information.getUser();
                fillUI();
                new SaveCacheTask(getActivity(), information, getCacheKey()).execute();
            } catch (Exception e) {
                e.printStackTrace();
                onFailure(arg0, arg1, arg2, e);
            }
        }

        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
            throwable.printStackTrace();
            mAdapter.setState(UserBaseInfoAdapter.STATE_ERROR);
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUid = AppContext.instance().getLoginUid();
        mHisUid = getActivity().getIntent().getIntExtra("his_id", 0);
        mHisName = getActivity().getIntent().getStringExtra("his_name");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.v2_fragment_simple_recycler_view, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        mRecycleView = (RecyclerView) view.findViewById(R.id.recycleView);
        mRecycleView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleView.setLayoutManager(mLayoutManager);
        mAdapter = new UserBaseInfoAdapter(this);
        mRecycleView.setAdapter(mAdapter);

        requestData(true);
    }

    private void requestData(boolean refresh) {
        mAdapter.setState(UserBaseInfoAdapter.STATE_LOADING);
        String key = getCacheKey();
        if (TDevice.hasInternet()
                && (!CacheManager.isReadDataCache(getActivity(), key) || refresh)) {
            Log.e(TAG, "request network" + getCacheKey());
            sendRequestData();
        } else {
            Log.e(TAG, "request cache" + getCacheKey());
            readCacheData(key);
        }
    }

    private void readCacheData(String key) {
        cancelReadCacheTask();
        mCacheTask = new CacheTask(getActivity()).execute(key);
    }

    private void cancelReadCacheTask() {
        if (mCacheTask != null) {
            mCacheTask.cancel(true);
            mCacheTask = null;
        }
    }

    private void sendRequestData() {
        NewsApi.getUserInformation(mUid, mHisUid, mHisName, 0, mInfoHandler);
    }

    private String getCacheKey() {
        return "user_center_profile" + mHisUid;
    }

    @Override
    public void onSingleViewClick(View v) {
        requestData(true);
    }

    private class CacheTask extends AsyncTask<String, Void, UserInformation> {
        private WeakReference<Context> mContext;

        private CacheTask(Context context) {
            mContext = new WeakReference<>(context);
        }

        @Override
        protected UserInformation doInBackground(String... params) {
            Serializable seri = CacheManager.readObject(mContext.get(),
                    params[0]);
            if (seri == null) {
                return null;
            } else {
                return (UserInformation) seri;
            }
        }

        @Override
        protected void onPostExecute(UserInformation info) {
            super.onPostExecute(info);
            if (info != null) {
                mUser = info.getUser();
                fillUI();
            } else {
                //mEmptyView.setErrorType(EmptyLayout.NETWORK_ERROR);
                mAdapter.setState(UserBaseInfoAdapter.STATE_ERROR);
            }
        }
    }

    private void fillUI() {
        mAdapter.setUser(mUser);
        mAdapter.setState(UserBaseInfoAdapter.STATE_INFO);
        try {
            FragmentActivity act = getActivity();
            if (act != null && act instanceof UserDataControl) {
                ((UserDataControl) act).setUserInfo(mUser);
            }
        } catch (Exception e) {
            TLog.error(e.getMessage());
        }
    }

    private class SaveCacheTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<Context> mContext;
        private Serializable seri;
        private String key;

        private SaveCacheTask(Context context, Serializable seri, String key) {
            mContext = new WeakReference<>(context);
            this.seri = seri;
            this.key = key;
        }

        @Override
        protected Void doInBackground(Void... params) {
            CacheManager.saveObject(mContext.get(), seri, key);
            return null;
        }
    }
}
