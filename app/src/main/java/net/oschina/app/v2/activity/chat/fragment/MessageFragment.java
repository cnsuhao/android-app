package net.oschina.app.v2.activity.chat.fragment;

import android.app.Activity;
import android.content.Intent;
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
import net.oschina.app.v2.activity.chat.view.ComposeView;
import net.oschina.app.v2.base.BaseFragment;
import net.oschina.app.v2.easemob.controller.HXSDKHelper;
import net.oschina.app.v2.utils.MD5Utils;
import net.oschina.app.v2.utils.TLog;

import java.util.List;

/**
 * Created by Tonlin on 2015/5/28.
 */
public class MessageFragment extends BaseFragment implements EMEventListener, ComposeView.OnComposeOperationDelegate {
    public static final int CHATTYPE_SINGLE = 1;
    public static final int CHATTYPE_GROUP = 2;
    public static final int CHATTYPE_CHATROOM = 3;
    private static final java.lang.String TAG = "IMA-LOG";


    private String mToChatUsername;
    private int mChatType = CHATTYPE_SINGLE;
    private EMConversation mConversation;
    private int mPageSize = 30;
    private ListView mLvMessage;
    private MessageAdapter mAdapter;
    private ComposeView mComposeView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getActivity().getIntent();
        mToChatUsername = intent.getStringExtra(MessageActivity.KEY_TO_CHAT_NAME);
        mChatType = intent.getIntExtra(MessageActivity.KEY_CHAT_TYPE, CHATTYPE_SINGLE);
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
        mLvMessage = (ListView) view.findViewById(R.id.lv_message);
        mComposeView = (ComposeView) view.findViewById(R.id.compose);
        mComposeView.setOperationDelegate(this);
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

        mAdapter = new MessageAdapter(getActivity(), mLvMessage, mToChatUsername, mChatType);
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
        if (text.length() > 0) {
            EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
            // 如果是群聊，设置chattype,默认是单聊
            if (mChatType == CHATTYPE_GROUP) {
                message.setChatType(EMMessage.ChatType.GroupChat);
            } else if (mChatType == CHATTYPE_CHATROOM) {
                message.setChatType(EMMessage.ChatType.ChatRoom);
            }

            TextMessageBody txtBody = new TextMessageBody(text);
            // 设置消息body
            message.addBody(txtBody);

            if (mChatType == CHATTYPE_SINGLE) {
                //message.setAttribute("user_avatar", AppContext.getLoginInfo().getFace());
                //message.setAttribute("user_nick_name", AppContext.getLoginInfo().getName());
            }

            // 设置要发给谁,用户username或者群聊groupid
            message.setReceipt(mToChatUsername);
            // 把messgage加到conversation中
            mConversation.addMessage(message);
            // 通知adapter有消息变动，adapter会根据加入的这条message显示消息和调用sdk的发送方法
            mAdapter.refreshSelectLast();
            mComposeView.clearText();
            getActivity().setResult(Activity.RESULT_OK);
        }
    }

    @Override
    public void onEvent(final EMNotifierEvent event) {
        switch (event.getEvent()) {
            case EventNewMessage: {
                //获取到message
                EMMessage message = (EMMessage) event.getData();

                String username = null;
                //群组消息
                if (message.getChatType() == EMMessage.ChatType.GroupChat
                        || message.getChatType() == EMMessage.ChatType.ChatRoom) {
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
        TLog.log(TAG, "refreshUI");
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
        TLog.log(TAG, "refreshUIWithNewMessage");
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mAdapter.refreshSelectLast();
            }
        });
    }

    @Override
    public void onSendText(String text) {
        sendTextMessage(text);
    }

    @Override
    public void onSendVoice(String file) {

    }
}
