package net.oschina.app.v2.activity.chat.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.MySwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.EMError;
import com.easemob.EMEventListener;
import com.easemob.EMNotifierEvent;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMVideoCallHelper;
import com.easemob.exceptions.EaseMobException;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.chat.adapter.ConversationAdapter;
import net.oschina.app.v2.base.BaseTabFragment;
import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.easemob.controller.HXSDKHelper;
import net.oschina.app.v2.model.User;
import net.oschina.app.v2.model.chat.IMUser;
import net.oschina.app.v2.ui.decorator.DividerItemDecoration;
import net.oschina.app.v2.ui.empty.EmptyLayout;
import net.oschina.app.v2.ui.widget.FixedRecyclerView;
import net.oschina.app.v2.utils.MD5Utils;
import net.oschina.app.v2.utils.UIHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

/**
 * Created by Tonlin on 2015/5/27.
 */
public class ConversationFragment extends BaseTabFragment implements RecycleBaseAdapter.OnItemClickListener, EMEventListener {

    protected MySwipeRefreshLayout mSwipeRefresh;
    protected FixedRecyclerView mRecycleView;
    protected LinearLayoutManager mLayoutManager;
    protected ConversationAdapter mAdapter;
    protected EmptyLayout mErrorLayout;
    protected int mStoreEmptyState = -1;
    protected String mStoreEmptyMessage;

    protected int mCurrentPage = 0;

    public int getLayoutRes() {
        return R.layout.v2_fragment_swipe_refresh_recyclerview;
    }

    @Override
    public void onResume() {
        super.onResume();
        // register the event listener when enter the foreground
        EMChatManager.getInstance().registerEventListener(this,
                new EMNotifierEvent.Event[]{
                        EMNotifierEvent.Event.EventNewMessage,
                        EMNotifierEvent.Event.EventOfflineMessage,
                        EMNotifierEvent.Event.EventConversationListChanged
                });
    }

    @Override
    public void onStop() {
        super.onStop();
        EMChatManager.getInstance().unregisterEventListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutRes(), container, false);
        initViews(view);
        return view;
    }

    protected void initViews(View view) {
        mErrorLayout = (EmptyLayout) view.findViewById(R.id.error_layout);
        mErrorLayout.setOnLayoutClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mCurrentPage = 0;
                mState = STATE_REFRESH;
                mErrorLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
                //requestData(true);
            }
        });

        mSwipeRefresh = (MySwipeRefreshLayout) view.findViewById(R.id.srl_refresh);
        mSwipeRefresh.setColorSchemeResources(R.color.main_green, R.color.main_gray, R.color.main_black, R.color.main_purple);
        mSwipeRefresh.setOnRefreshListener(new MySwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        mRecycleView = (FixedRecyclerView) view.findViewById(R.id.recycleView);
        //mRecycleView.setOnScrollListener(mScrollListener);

        // use a linear layout manager
        mRecycleView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL_LIST));

        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleView.setLayoutManager(mLayoutManager);
        mRecycleView.setHasFixedSize(true);


        mAdapter = new ConversationAdapter();
        mAdapter.setOnItemClickListener(this);
        mRecycleView.setAdapter(mAdapter);

        Activity parentActivity = getActivity();

        mRecycleView.setTouchInterceptionViewGroup((ViewGroup) parentActivity.findViewById(R.id.container));

        if (parentActivity instanceof ObservableScrollViewCallbacks) {
            mRecycleView.setScrollViewCallbacks((ObservableScrollViewCallbacks) parentActivity);
        }

        executeWithAccount();
    }

    private void executeWithAccount() {
        final User user = AppContext.getLoginInfo();
        if (user == null || TextUtils.isEmpty(user.getAccount()) || TextUtils.isEmpty(user.getPwd())) {
            AppContext.showToastShort("需要登录");
        } else {
            IMUser iu = IMUser.getCurrentUser(getActivity(), IMUser.class);
            if (iu != null && AppContext.hasHXLogin()) {
                AppContext.showToastShort("均已登陆");
                refresh();
                return;
            }

            // 检查是否注册过
            BmobQuery<IMUser> u = new BmobQuery<>();
            u.addWhereEqualTo("username", user.getAccount());
            u.findObjects(getActivity(), new FindListener<IMUser>() {
                @Override
                public void onSuccess(List<IMUser> list) {
                    if (list != null && list.size() > 0) {
                        AppContext.showToastShort("找到用户:" + list.get(0).getUsername());
                        handlerUserRealExist(user.getAccount(), user.getPwd());
                    } else {
                        AppContext.showToastShort("没有找到用户");

                        registerAccountIM(user.getAccount(), user.getPwd());
                    }
                }

                @Override
                public void onError(int i, String s) {
                    AppContext.showToastShort("没有找到用户,查询失败");
                }
            });
        }
    }

    private void registerAccountIM(final String username, final String pwd) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Log.d("TAG", "账户:" + username + " 密码：" + pwd);
                    // 调用sdk注册方法
                    EMChatManager.getInstance().createAccountOnServer(MD5Utils.getMD5Code(username), MD5Utils.getMD5Code(pwd));

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            registerDB(username, pwd);
                        }
                    });

                } catch (final EaseMobException e) {
                    //注册失败
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int errorCode = e.getErrorCode();
                            if (errorCode == EMError.NONETWORK_ERROR) {
                                Toast.makeText(getActivity(), "网络异常，请检查网络！", Toast.LENGTH_SHORT).show();
                            } else if (errorCode == EMError.USER_ALREADY_EXISTS) {
                                Toast.makeText(getActivity(), "用户已存在！", Toast.LENGTH_SHORT).show();
                            } else if (errorCode == EMError.UNAUTHORIZED) {
                                Toast.makeText(getActivity(), "注册失败，无权限！", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "注册失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }).start();
    }

    private void registerDB(final String username, final String pwd) {
        final IMUser u = new IMUser();
        u.setUsername(username);
        u.setPassword(pwd);
        u.signUp(getActivity(), new SaveListener() {
            @Override
            public void onSuccess() {
                AppContext.showToastShort("注册成功 DB");

                handlerUserRealExist(username, pwd);
            }

            @Override
            public void onFailure(int i, String s) {
                AppContext.showToastShort("注册失败 DB");
            }
        });
    }

    private void handlerUserRealExist(final String username, final String pwd) {
        IMUser u = new IMUser();
        u.setUsername(username);
        u.setPassword(pwd);
        IMUser uu = IMUser.getCurrentUser(getActivity(), IMUser.class);
        if (uu != null) {
            AppContext.showToastShort("DB用户已登录");
            loginIMServer(username, pwd);
        } else {
            AppContext.showToastShort("DB用户未登录");
            u.login(getActivity(), new SaveListener() {
                @Override
                public void onSuccess() {
                    AppContext.showToastShort("登陆成功 DB");
                    loginIMServer(username, pwd);
                }

                @Override
                public void onFailure(int i, String s) {
                    AppContext.showToastShort("登陆失败 DB");
                }
            });
        }
    }

    private void loginIMServer(final String username, final String pwd) {
        EMChatManager.getInstance().login(MD5Utils.getMD5Code(username),
                MD5Utils.getMD5Code(pwd), new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                EMGroupManager.getInstance().loadAllGroups();
                                EMChatManager.getInstance().loadAllConversations();
                                AppContext.showToastShort("登陆聊天服务器成功！");

                                // 登陆成功，保存用户名密码
                                AppContext.instance().setHXUserName(MD5Utils.getMD5Code(username));
                                AppContext.instance().setHXPassword(MD5Utils.getMD5Code(pwd));

                                refresh();
                            }
                        });
                    }

                    @Override
                    public void onError(int i, String s) {
                        AppContext.showToastShort("登陆聊天服务器失败！");
                    }

                    @Override
                    public void onProgress(int i, String s) {

                    }
                });
    }

    private void refresh() {
        mAdapter.clear();
        mAdapter.addData(loadConversationsWithRecentChat());
    }


    /**
     * 获取所有会话
     *
     * @return +
     */
    private List<EMConversation> loadConversationsWithRecentChat() {
        // 获取所有会话，包括陌生人
        Hashtable<String, EMConversation> conversations = EMChatManager.getInstance().getAllConversations();
        // 过滤掉messages size为0的conversation
        /**
         * 如果在排序过程中有新消息收到，lastMsgTime会发生变化
         * 影响排序过程，Collection.sort会产生异常
         * 保证Conversation在Sort过程中最后一条消息的时间不变
         * 避免并发问题
         */
        List<Pair<Long, EMConversation>> sortList = new ArrayList<Pair<Long, EMConversation>>();
        synchronized (conversations) {
            for (EMConversation conversation : conversations.values()) {
                if (conversation.getAllMessages().size() != 0) {
                    //if(conversation.getType() != EMConversationType.ChatRoom){
                    sortList.add(new Pair<Long, EMConversation>(conversation.getLastMessage().getMsgTime(), conversation));
                    //}
                }
            }
        }
        try {
            // Internal is TimSort algorithm, has bug
            sortConversationByLastChatTime(sortList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<EMConversation> list = new ArrayList<EMConversation>();
        for (Pair<Long, EMConversation> sortItem : sortList) {
            list.add(sortItem.second);
        }
        return list;
    }

    /**
     * 根据最后一条消息的时间排序
     */
    private void sortConversationByLastChatTime(List<Pair<Long, EMConversation>> conversationList) {
        Collections.sort(conversationList, new Comparator<Pair<Long, EMConversation>>() {
            @Override
            public int compare(final Pair<Long, EMConversation> con1, final Pair<Long, EMConversation> con2) {

                if (con1.first == con2.first) {
                    return 0;
                } else if (con2.first > con1.first) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
    }

    @Override
    public void onItemClick(View view) {
        int position = mRecycleView.getChildPosition(view);
        EMConversation conversation = (EMConversation) mAdapter.getItem(position);

        UIHelper.showChatMessage(getActivity(), conversation.getUserName());
    }

    @Override
    public void onEvent(EMNotifierEvent event) {
        switch (event.getEvent()) {
            case EventNewMessage: {// 普通消息
                Log.d("IMA-LOG", "onEvent: new message");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refresh();
                    }
                });
                EMMessage message = (EMMessage) event.getData();
                // 提示新消息
                HXSDKHelper.getInstance().getNotifier().onNewMsg(message);
                break;
            }
            case EventOfflineMessage: {
                refresh();
                break;
            }
            case EventConversationListChanged: {
                refresh();
                break;
            }
            default:
                break;
        }
    }
}
