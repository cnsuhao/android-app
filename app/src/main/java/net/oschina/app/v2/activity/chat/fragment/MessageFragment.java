package net.oschina.app.v2.activity.chat.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.easemob.EMCallBack;
import com.easemob.EMEventListener;
import com.easemob.EMNotifierEvent;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.DemoHXSDKHelper;
import net.oschina.app.v2.activity.chat.MessageActivity;
import net.oschina.app.v2.activity.chat.adapter.MessageAdapter;
import net.oschina.app.v2.base.BaseFragment;
import net.oschina.app.v2.easemob.controller.HXSDKHelper;
import net.oschina.app.v2.utils.MD5Utils;

import java.util.List;

/**
 * Created by Tonlin on 2015/5/28.
 */
public class MessageFragment extends BaseFragment implements EMEventListener {
    public static final int CHATTYPE_SINGLE = 1;
    public static final int CHATTYPE_GROUP = 2;
    public static final int CHATTYPE_CHATROOM = 3;


    private String mToChatUsername;//MD5Utils.getMD5Code("448133553@qq.com");//;//"cun.only.yun@gmail.com"
    private int mChatType = CHATTYPE_SINGLE;
    private EMConversation mConversation;
    private int mPageSize = 30;
    private ListView mLvMessage;
    private MessageAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToChatUsername = getActivity().getIntent().getStringExtra(MessageActivity.KEY_TO_CHAT_NAME);
        AppContext.showToastShort("to chat:"+mToChatUsername);
    }

    @Override
    public void onStop() {
        // unregister this event listener when this activity enters the
        // background
        EMChatManager.getInstance().unregisterEventListener(this);

        DemoHXSDKHelper sdkHelper = (DemoHXSDKHelper) DemoHXSDKHelper.getInstance();

        // 把此activity 从foreground activity 列表里移除
        sdkHelper.popActivity(getActivity());
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null) {
            mAdapter.refresh();
        }

        DemoHXSDKHelper sdkHelper = (DemoHXSDKHelper) DemoHXSDKHelper.getInstance();
        sdkHelper.pushActivity(getActivity());
        // register the event listener when enter the foreground
        EMChatManager.getInstance().registerEventListener(
                this,
                new EMNotifierEvent.Event[]{
                        EMNotifierEvent.Event.EventNewMessage,
                        EMNotifierEvent.Event.EventOfflineMessage,
                        EMNotifierEvent.Event.EventDeliveryAck,
                        EMNotifierEvent.Event.EventReadAck
                });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.v2_fragment_chat_message, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        view.findViewById(R.id.btn_send).setOnClickListener(this);
        mLvMessage = (ListView) view.findViewById(R.id.lv_message);


        initData();
    }

    private void initData() {
        if (mChatType == CHATTYPE_SINGLE) {
            mConversation = EMChatManager.getInstance().getConversationByType(mToChatUsername, EMConversation.EMConversationType.Chat);
        } else if (mChatType == CHATTYPE_GROUP) {
            mConversation = EMChatManager.getInstance().getConversationByType(mToChatUsername, EMConversation.EMConversationType.GroupChat);
        } else if (mChatType == CHATTYPE_CHATROOM) {
            mConversation = EMChatManager.getInstance().getConversationByType(mToChatUsername, EMConversation.EMConversationType.ChatRoom);
        }

        // 把此会话的未读数置为0
        mConversation.markAllMessagesAsRead();

        // 初始化db时，每个conversation加载数目是getChatOptions().getNumberOfMessagesLoaded
        // 这个数目如果比用户期望进入会话界面时显示的个数不一样，就多加载一些
        final List<EMMessage> msgs = mConversation.getAllMessages();
        int msgCount = msgs != null ? msgs.size() : 0;
        if (msgCount < mConversation.getAllMsgCount() && msgCount < mPageSize) {
            String msgId = null;
            if (msgs != null && msgs.size() > 0) {
                msgId = msgs.get(0).getMsgId();
            }
            if (mChatType == CHATTYPE_SINGLE) {
                mConversation.loadMoreMsgFromDB(msgId, mPageSize);
            } else {
                mConversation.loadMoreGroupMsgFromDB(msgId, mPageSize);
            }
        }

        mAdapter = new MessageAdapter(getActivity(), mToChatUsername, mChatType);
        // 显示消息
        mLvMessage.setAdapter(mAdapter);

        mAdapter.refreshSelectLast();
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.btn_send) {
            EditText text = (EditText) getView().findViewById(R.id.et_content);
            sendTextMessage(text.getText().toString());
        }
    }

    private void sendTextMessage(String text) {
        //获取到与聊天人的会话对象。参数username为聊天人的userid或者groupid，后文中的username皆是如此
        EMConversation conversation = EMChatManager.getInstance().getConversation(mToChatUsername);
        //创建一条文本消息
        EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
        //如果是群聊，设置chattype,默认是单聊
        message.setChatType(EMMessage.ChatType.GroupChat);
        //设置消息body
        TextMessageBody txtBody = new TextMessageBody(text);
        message.addBody(txtBody);
        //设置接收人
        message.setReceipt(mToChatUsername);
        //把消息加入到此会话对象中
        conversation.addMessage(message);
        //发送消息
        EMChatManager.getInstance().sendMessage(message, new EMCallBack() {
            @Override
            public void onSuccess() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AppContext.showToastShort("发送成功");
                    }
                });
            }

            @Override
            public void onError(int i, final String s) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AppContext.showToastShort("发送失败:" + s);
                    }
                });
            }

            @Override
            public void onProgress(int i, String s) {

            }
        });
    }

    @Override
    public void onEvent(EMNotifierEvent event) {
        switch (event.getEvent()) {
            case EventNewMessage: {
                AppContext.showToastShort("收到新消息");
                //获取到message
                EMMessage message = (EMMessage) event.getData();

                String username = null;
                //群组消息
                if (message.getChatType() == EMMessage.ChatType.GroupChat || message.getChatType() == EMMessage.ChatType.ChatRoom) {
                    username = message.getTo();
                } else {
                    //单聊消息
                    username = message.getFrom();
                }

                //如果是当前会话的消息，刷新聊天页面
                if (username.equals(mToChatUsername)) {
                    refreshUIWithNewMessage();
                    //声音和震动提示有新消息
                    HXSDKHelper.getInstance().getNotifier().viberateAndPlayTone(message);
                } else {
                    //如果消息不是和当前聊天ID的消息
                    HXSDKHelper.getInstance().getNotifier().onNewMsg(message);
                }

                break;
            }
            case EventDeliveryAck: {
                //获取到message
                EMMessage message = (EMMessage) event.getData();
                refreshUI();
                break;
            }
            case EventReadAck: {
                //获取到message
                EMMessage message = (EMMessage) event.getData();
                refreshUI();
                break;
            }
            case EventOfflineMessage: {
                //a list of offline messages
                //List<EMMessage> offlineMessages = (List<EMMessage>) event.getData();
                refreshUI();
                break;
            }
            default:
                break;
        }
    }

    private void refreshUI() {
        if (mAdapter == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mAdapter.refresh();
            }
        });
    }


    private void refreshUIWithNewMessage() {
        if (mAdapter == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mAdapter.refreshSelectLast();
            }
        });
    }
}
