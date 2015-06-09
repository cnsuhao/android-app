/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.oschina.app.v2.activity.chat.adapter;

import java.io.File;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Handler;
import android.text.Spannable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.EMMessage.Direct;
import com.easemob.chat.EMMessage.Type;
import com.easemob.chat.FileMessageBody;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.LocationMessageBody;
import com.easemob.chat.NormalFileMessageBody;
import com.easemob.chat.TextMessageBody;
import com.easemob.chat.VideoMessageBody;
//import com.easemob.chatuidemo.Constant;
//import com.easemob.chatuidemo.R;
//import com.easemob.chatuidemo.activity.AlertDialog;
//import com.easemob.chatuidemo.activity.BaiduMapActivity;
//import com.easemob.chatuidemo.activity.ChatActivity;
//import com.easemob.chatuidemo.activity.ContextMenu;
//import com.easemob.chatuidemo.activity.ShowBigImage;
//import com.easemob.chatuidemo.activity.ShowNormalFileActivity;
//import com.easemob.chatuidemo.activity.ShowVideoActivity;
//import com.easemob.chatuidemo.task.LoadImageTask;
//import com.easemob.chatuidemo.task.LoadVideoImageTask;
//import com.easemob.chatuidemo.utils.ImageCache;
//import com.easemob.chatuidemo.utils.ImageUtils;
//import com.easemob.chatuidemo.utils.SmileUtils;
//import com.easemob.chatuidemo.utils.UserUtils;
import com.easemob.chat.VoiceMessageBody;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.DateUtils;
import com.easemob.util.EMLog;
import com.easemob.util.LatLng;
import com.easemob.util.TextFormater;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.chat.MessageActivity;
import net.oschina.app.v2.activity.chat.loader.AwareView;
import net.oschina.app.v2.activity.chat.loader.CacheType;
import net.oschina.app.v2.activity.chat.loader.ContactsFetcher;
import net.oschina.app.v2.activity.chat.loader.DisplayListener;
import net.oschina.app.v2.activity.chat.loader.IName;
import net.oschina.app.v2.activity.chat.text.AdvanceTextHelper;
import net.oschina.app.v2.activity.chat.view.AsyncTextView;
import net.oschina.app.v2.base.Constants;
import net.oschina.app.v2.model.User;
import net.oschina.app.v2.model.chat.IMUser;
import net.oschina.app.v2.ui.AvatarView;
import net.oschina.app.v2.utils.ImageUtils;
import net.oschina.app.v2.utils.SmileUtils;
import net.oschina.app.v2.utils.TDevice;
import net.oschina.app.v2.utils.TLog;
import net.oschina.app.v2.utils.UIHelper;

public class MessageAdapter extends BaseAdapter {

    private final static String TAG = "msg";

    private static final int MESSAGE_TYPE_RECV_TXT = 0;
    private static final int MESSAGE_TYPE_SENT_TXT = 1;
    private static final int MESSAGE_TYPE_SENT_IMAGE = 2;
    private static final int MESSAGE_TYPE_SENT_LOCATION = 3;
    private static final int MESSAGE_TYPE_RECV_LOCATION = 4;
    private static final int MESSAGE_TYPE_RECV_IMAGE = 5;
    private static final int MESSAGE_TYPE_SENT_VOICE = 6;
    private static final int MESSAGE_TYPE_RECV_VOICE = 7;
    private static final int MESSAGE_TYPE_SENT_VIDEO = 8;
    private static final int MESSAGE_TYPE_RECV_VIDEO = 9;
    private static final int MESSAGE_TYPE_SENT_FILE = 10;
    private static final int MESSAGE_TYPE_RECV_FILE = 11;
    private static final int MESSAGE_TYPE_SENT_VOICE_CALL = 12;
    private static final int MESSAGE_TYPE_RECV_VOICE_CALL = 13;
    private static final int MESSAGE_TYPE_SENT_VIDEO_CALL = 14;
    private static final int MESSAGE_TYPE_RECV_VIDEO_CALL = 15;

    public static final String IMAGE_DIR = "chat/image/";
    public static final String VOICE_DIR = "chat/audio/";
    public static final String VIDEO_DIR = "chat/video";

    private String username;
    private LayoutInflater inflater;
    private Activity activity;

    private static final int HANDLER_MESSAGE_REFRESH_LIST = 0;
    private static final int HANDLER_MESSAGE_SELECT_LAST = 1;
    private static final int HANDLER_MESSAGE_SEEK_TO = 2;

    // reference to conversation object in chatsdk
    private EMConversation conversation;
    EMMessage[] messages = null;

    private Context context;

    private Map<String, Timer> timers = new Hashtable<String, Timer>();

    private ListView mLvMessage;

    public MessageAdapter(Context context, ListView lv, String username, int chatType) {
        this.username = username;
        this.context = context;
        inflater = LayoutInflater.from(context);
        activity = (Activity) context;
        mLvMessage = lv;
        this.conversation = EMChatManager.getInstance().getConversation(username);
    }

    Handler handler = new Handler() {

        private void refreshList() {
            // UI线程不能直接使用conversation.getAllMessages()
            // 否则在UI刷新过程中，如果收到新的消息，会导致并发问题
            messages = (EMMessage[]) conversation.getAllMessages().toArray(
                    new EMMessage[conversation.getAllMessages().size()]);
            Log.d("message", "获取消息列表:" + messages.length);
            for (int i = 0; i < messages.length; i++) {
                // getMessage will set message as read status
                conversation.getMessage(i);
            }
            notifyDataSetChanged();
        }

        @Override
        public void handleMessage(android.os.Message message) {
            switch (message.what) {
                case HANDLER_MESSAGE_REFRESH_LIST:
                    refreshList();
                    break;
                case HANDLER_MESSAGE_SELECT_LAST:
                    if (messages.length > 0) {
                        mLvMessage.setSelection(messages.length - 1);
                    }
                    break;
                case HANDLER_MESSAGE_SEEK_TO:
                    int position = message.arg1;
                    mLvMessage.setSelection(position);
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 获取item数
     */
    public int getCount() {
        return messages == null ? 0 : messages.length;
    }

    /**
     * 刷新页面
     */
    public void refresh() {
        if (handler.hasMessages(HANDLER_MESSAGE_REFRESH_LIST)) {
            return;
        }
        android.os.Message msg = handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST);
        handler.sendMessage(msg);
    }

    /**
     * 刷新页面, 选择最后一个
     */
    public void refreshSelectLast() {
        handler.sendMessage(handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST));
        handler.sendMessage(handler.obtainMessage(HANDLER_MESSAGE_SELECT_LAST));
    }

    /**
     * 刷新页面, 选择Position
     */
    public void refreshSeekTo(int position) {
        handler.sendMessage(handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST));
        android.os.Message msg = handler.obtainMessage(HANDLER_MESSAGE_SEEK_TO);
        msg.arg1 = position;
        handler.sendMessage(msg);
    }

    public EMMessage getItem(int position) {
        if (messages != null && position < messages.length) {
            return messages[position];
        }
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    /**
     * 获取item类型数
     */
    public int getViewTypeCount() {
        return 16;
    }

    /**
     * 获取item类型
     */
    public int getItemViewType(int position) {
        EMMessage message = getItem(position);
        if (message == null) {
            return -1;
        }
        if (message.getType() == Type.TXT) {
            if (message.getBooleanAttribute(Constants.MESSAGE_ATTR_IS_VOICE_CALL, false))
                return message.direct == Direct.RECEIVE ? MESSAGE_TYPE_RECV_VOICE_CALL : MESSAGE_TYPE_SENT_VOICE_CALL;
            else if (message.getBooleanAttribute(Constants.MESSAGE_ATTR_IS_VIDEO_CALL, false))
                return message.direct == Direct.RECEIVE ? MESSAGE_TYPE_RECV_VIDEO_CALL : MESSAGE_TYPE_SENT_VIDEO_CALL;
            return message.direct == Direct.RECEIVE ? MESSAGE_TYPE_RECV_TXT : MESSAGE_TYPE_SENT_TXT;
        }
        if (message.getType() == Type.IMAGE) {
            return message.direct == Direct.RECEIVE ? MESSAGE_TYPE_RECV_IMAGE : MESSAGE_TYPE_SENT_IMAGE;

        }
        if (message.getType() == Type.LOCATION) {
            return message.direct == Direct.RECEIVE ? MESSAGE_TYPE_RECV_LOCATION : MESSAGE_TYPE_SENT_LOCATION;
        }
        if (message.getType() == Type.VOICE) {
            return message.direct == Direct.RECEIVE ? MESSAGE_TYPE_RECV_VOICE : MESSAGE_TYPE_SENT_VOICE;
        }
        if (message.getType() == Type.VIDEO) {
            return message.direct == Direct.RECEIVE ? MESSAGE_TYPE_RECV_VIDEO : MESSAGE_TYPE_SENT_VIDEO;
        }
        if (message.getType() == Type.FILE) {
            return message.direct == Direct.RECEIVE ? MESSAGE_TYPE_RECV_FILE : MESSAGE_TYPE_SENT_FILE;
        }

        return -1;// invalid
    }


    private View createViewByMessage(EMMessage message, int position) {
        switch (message.getType()) {
            case LOCATION:
                return message.direct == Direct.RECEIVE ? inflater.inflate(R.layout.row_received_location, null) : inflater.inflate(
                        R.layout.row_sent_location, null);
            case IMAGE:
                return message.direct == Direct.RECEIVE ? inflater.inflate(R.layout.row_received_picture, null) : inflater.inflate(
                        R.layout.row_sent_picture, null);

            case VOICE:
                return message.direct == Direct.RECEIVE ? inflater.inflate(R.layout.row_received_voice, null) : inflater.inflate(
                        R.layout.row_sent_voice, null);
            case VIDEO:
                return message.direct == Direct.RECEIVE ? inflater.inflate(R.layout.row_received_video, null) : inflater.inflate(
                        R.layout.row_sent_video, null);
            case FILE:
                return message.direct == Direct.RECEIVE ? inflater.inflate(R.layout.row_received_file, null) : inflater.inflate(
                        R.layout.row_sent_file, null);
            default:
                // 语音通话
                if (message.getBooleanAttribute(Constants.MESSAGE_ATTR_IS_VOICE_CALL, false))
                    return message.direct == Direct.RECEIVE ? inflater.inflate(R.layout.row_received_voice_call, null) : inflater
                            .inflate(R.layout.row_sent_voice_call, null);
                    // 视频通话
                else if (message.getBooleanAttribute(Constants.MESSAGE_ATTR_IS_VIDEO_CALL, false))
                    return message.direct == Direct.RECEIVE ? inflater.inflate(R.layout.row_received_video_call, null) : inflater
                            .inflate(R.layout.row_sent_video_call, null);
                return message.direct == Direct.RECEIVE ? inflater.inflate(R.layout.row_received_message, null) : inflater.inflate(
                        R.layout.row_sent_message, null);
        }
    }

    @SuppressLint("NewApi")
    public View getView(final int position, View convertView, ViewGroup parent) {
        final EMMessage message = getItem(position);
        ChatType chatType = message.getChatType();
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = createViewByMessage(message, position);
            if (message.getType() == Type.IMAGE) {
                try {
                    holder.iv = ((ImageView) convertView.findViewById(R.id.iv_sendPicture));
                    holder.iv_avatar = (AvatarView) convertView.findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView.findViewById(R.id.percentage);
                    holder.pb = (ProgressBar) convertView.findViewById(R.id.progressBar);
                    holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                    holder.tv_usernick = (AsyncTextView) convertView.findViewById(R.id.tv_userid);
                } catch (Exception e) {
                }

            } else if (message.getType() == Type.TXT) {
                try {
                    holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
                    holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                    holder.iv_avatar = (AvatarView) convertView.findViewById(R.id.iv_userhead);
                    // 这里是文字内容
                    holder.tv = (TextView) convertView.findViewById(R.id.tv_chatcontent);
                    holder.tv_usernick = (AsyncTextView) convertView.findViewById(R.id.tv_userid);
                } catch (Exception e) {
                }

                // 语音通话及视频通话
                if (message.getBooleanAttribute(Constants.MESSAGE_ATTR_IS_VOICE_CALL, false)
                        || message.getBooleanAttribute(Constants.MESSAGE_ATTR_IS_VIDEO_CALL, false)) {
                    holder.iv = (ImageView) convertView.findViewById(R.id.iv_call_icon);
                    holder.tv = (TextView) convertView.findViewById(R.id.tv_chatcontent);
                }

            } else if (message.getType() == Type.VOICE) {
                try {
                    holder.iv = ((ImageView) convertView.findViewById(R.id.iv_voice));
                    holder.iv_avatar = (AvatarView) convertView.findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView.findViewById(R.id.tv_length);
                    holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
                    holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                    holder.tv_usernick = (AsyncTextView) convertView.findViewById(R.id.tv_userid);
                    holder.iv_read_status = (ImageView) convertView.findViewById(R.id.iv_unread_voice);
                    holder.voice_content = convertView.findViewById(R.id.ly_voice_content);
                } catch (Exception e) {
                }
            } else if (message.getType() == Type.LOCATION) {
                try {
                    holder.iv_avatar = (AvatarView) convertView.findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView.findViewById(R.id.tv_location);
                    holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
                    holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                    holder.tv_usernick = (AsyncTextView) convertView.findViewById(R.id.tv_userid);
                } catch (Exception e) {
                }
            } else if (message.getType() == Type.VIDEO) {
                try {
                    holder.iv = ((ImageView) convertView.findViewById(R.id.chatting_content_iv));
                    holder.iv_avatar = (AvatarView) convertView.findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView.findViewById(R.id.percentage);
                    holder.pb = (ProgressBar) convertView.findViewById(R.id.progressBar);
                    holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                    holder.size = (TextView) convertView.findViewById(R.id.chatting_size_iv);
                    holder.timeLength = (TextView) convertView.findViewById(R.id.chatting_length_iv);
                    holder.playBtn = (ImageView) convertView.findViewById(R.id.chatting_status_btn);
                    holder.container_status_btn = (LinearLayout) convertView.findViewById(R.id.container_status_btn);
                    holder.tv_usernick = (AsyncTextView) convertView.findViewById(R.id.tv_userid);

                } catch (Exception e) {
                }
            } else if (message.getType() == Type.FILE) {
                try {
                    holder.iv_avatar = (AvatarView) convertView.findViewById(R.id.iv_userhead);
                    holder.tv_file_name = (TextView) convertView.findViewById(R.id.tv_file_name);
                    holder.tv_file_size = (TextView) convertView.findViewById(R.id.tv_file_size);
                    holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
                    holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                    holder.tv_file_download_state = (TextView) convertView.findViewById(R.id.tv_file_state);
                    holder.ll_container = (LinearLayout) convertView.findViewById(R.id.ll_file_container);
                    // 这里是进度值
                    holder.tv = (TextView) convertView.findViewById(R.id.percentage);
                } catch (Exception e) {
                }
                try {
                    holder.tv_usernick = (AsyncTextView) convertView.findViewById(R.id.tv_userid);
                } catch (Exception e) {
                }
            }

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if(holder.tv_usernick != null) {
            // 群聊时，显示接收的消息的发送人的名称
            if ((chatType == ChatType.GroupChat || chatType == ChatType.ChatRoom) && message.direct == Direct.RECEIVE) {
                //demo里使用username代码nick
                holder.tv_usernick.setText(message.getFrom());
                holder.tv_usernick.setVisibility(View.VISIBLE);
            } else {
                holder.tv_usernick.setVisibility(View.GONE);
            }
        }

        // 如果是发送的消息并且不是群聊消息，显示已读textview
        if (!(chatType == ChatType.GroupChat || chatType == chatType.ChatRoom) && message.direct == Direct.SEND) {
            holder.tv_ack = (TextView) convertView.findViewById(R.id.tv_ack);
            holder.tv_delivered = (TextView) convertView.findViewById(R.id.tv_delivered);
            if (holder.tv_ack != null) {
                if (message.isAcked) {
                    if (holder.tv_delivered != null) {
                        holder.tv_delivered.setVisibility(View.INVISIBLE);
                    }
                    holder.tv_ack.setVisibility(View.VISIBLE);
                } else {
                    holder.tv_ack.setVisibility(View.INVISIBLE);

                    // check and display msg delivered ack status
                    if (holder.tv_delivered != null) {
                        if (message.isDelivered) {
                            holder.tv_delivered.setVisibility(View.VISIBLE);
                        } else {
                            holder.tv_delivered.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }
        } else {
            // 如果是文本或者地图消息并且不是group messgae,chatroom message，显示的时候给对方发送已读回执
            if ((message.getType() == Type.TXT || message.getType() == Type.LOCATION)
                    && !message.isAcked && chatType != ChatType.GroupChat && chatType != ChatType.ChatRoom) {
                // 不是语音通话记录
                if (!message.getBooleanAttribute(Constants.MESSAGE_ATTR_IS_VOICE_CALL, false)) {
                    try {
                        EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
                        // 发送已读回执
                        message.isAcked = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        //设置用户头像
        setUserAvatar(message, holder.tv_usernick, holder.iv_avatar);
        boolean bg_normal = false;

        switch (message.getType()) {
            // 根据消息type显示item
            case IMAGE: // 图片
                handleImageMessage(message, holder, position, convertView, bg_normal);
                break;
            case TXT: // 文本
                if (message.getBooleanAttribute(Constants.MESSAGE_ATTR_IS_VOICE_CALL, false)
                        || message.getBooleanAttribute(Constants.MESSAGE_ATTR_IS_VIDEO_CALL, false))
                    // 音视频通话
                    handleCallMessage(message, holder, position);
                else
                    handleTextMessage(message, holder, position);
                break;
            case LOCATION: // 位置
                handleLocationMessage(message, holder, position, convertView);
                break;
            case VOICE: // 语音
                handleVoiceMessage(message, holder, position, convertView);
                break;
            case VIDEO: // 视频
                handleVideoMessage(message, holder, position, convertView);
                break;
            case FILE: // 一般文件
                handleFileMessage(message, holder, position, convertView);
                break;
            default:
                // not supported
        }

        if (message.direct == Direct.SEND) {
            View statusView = convertView.findViewById(R.id.msg_status);
            // 重发按钮点击事件
            statusView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

        } else {
            //final String st = context.getResources().getString(R.string.Into_the_blacklist);
            if (chatType != ChatType.ChatRoom) {
                // 长按头像，移入黑名单
                holder.iv_avatar.setOnLongClickListener(new OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {

                        return true;
                    }
                });
            }
        }

        TextView timestamp = (TextView) convertView.findViewById(R.id.timestamp);

        if (position == 0) {
            timestamp.setText(DateUtils.getTimestampString(new Date(message.getMsgTime())));
            timestamp.setVisibility(View.VISIBLE);
        } else {
            // 两条消息时间离得如果稍长，显示时间
            EMMessage prevMessage = getItem(position - 1);
            if (prevMessage != null && DateUtils.isCloseEnough(message.getMsgTime(), prevMessage.getMsgTime())) {
                timestamp.setVisibility(View.GONE);
            } else {
                timestamp.setText(DateUtils.getTimestampString(new Date(message.getMsgTime())));
                timestamp.setVisibility(View.VISIBLE);
            }
        }
        return convertView;
    }


    /**
     * 显示用户头像
     *
     * @param message
     * @param imageView
     */
    private void setUserAvatar(EMMessage message, final AsyncTextView displayName,
                               final AvatarView imageView) {
        if (message.direct == Direct.SEND) {
            User user = AppContext.getLoginInfo();
            imageView.setAvatarUrl(user.getFace());
            if (displayName != null) {
                displayName.setText(user.getName());
            }
        } else {
            IName name = ContactsFetcher.getInstance().getNameCache().getBitmapFromMemCache(message.getUserName());
            if (name == null) {
                name = ContactsFetcher.getInstance().getNameCache().getBitmapFromDiskCache(message.getUserName(), CacheType.USER);
            }
            if (name != null && name instanceof IMUser) {
                IMUser user = (IMUser) name;
                displayName.setText(user.getName());
                imageView.setAvatarUrl(user.getPhoto());
            } else {
                if ("admin".equals(message.getUserName())) {
                    displayName.setText(message.getUserName());
                    imageView.setImageURI(Uri.parse(""));
                } else {
                    ContactsFetcher.getInstance().loadName(message.getUserName(),
                            CacheType.USER, displayName, new DisplayListener() {
                                @Override
                                public void onLoadSuccess(AwareView awareView, IName name) {
                                    imageView.setAvatarUrl(name.getPhoto());
                                }

                                @Override
                                public void onLoadFailure(AwareView awareView, IName name) {
                                }
                            });
                }
            }
        }
    }

    /**
     * 文本消息
     *
     * @param message
     * @param holder
     * @param position
     */
    private void handleTextMessage(EMMessage message, ViewHolder holder, final int position) {
        TextMessageBody txtBody = (TextMessageBody) message.getBody();
       // Spannable span = SmileUtils.getSmiledText(context, txtBody.getMessage());
        // 设置内容
        //holder.tv.setText(span, BufferType.SPANNABLE);
        AdvanceTextHelper.parseLinkText(holder.tv,txtBody.getMessage());

        // 设置长按事件监听
        holder.tv.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                return true;
            }
        });

        if (message.direct == Direct.SEND) {
            switch (message.status) {
                case SUCCESS: // 发送成功
                    holder.pb.setVisibility(View.GONE);
                    holder.staus_iv.setVisibility(View.GONE);
                    break;
                case FAIL: // 发送失败
                    holder.pb.setVisibility(View.GONE);
                    holder.staus_iv.setVisibility(View.VISIBLE);
                    break;
                case INPROGRESS: // 发送中
                    holder.pb.setVisibility(View.VISIBLE);
                    holder.staus_iv.setVisibility(View.GONE);
                    break;
                default:
                    // 发送消息
                    sendMsgInBackground(message, holder);
            }
        }
    }

    /**
     * 音视频通话记录
     *
     * @param message
     * @param holder
     * @param position
     */
    private void handleCallMessage(EMMessage message, ViewHolder holder, final int position) {
        TextMessageBody txtBody = (TextMessageBody) message.getBody();
        holder.tv.setText(txtBody.getMessage());

    }

    /**
     * 图片消息
     *
     * @param message
     * @param holder
     * @param position
     * @param convertView
     */
    private void handleImageMessage(final EMMessage message, final ViewHolder holder, final int position, View convertView
                                    , boolean bg_normal) {
        holder.pb.setTag(position);
        holder.iv.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        setImageSize(holder.iv, message);

        // 接收方向的消息
        if (message.direct == Direct.RECEIVE) {
            // "it is receive msg";
            if (message.status == EMMessage.Status.INPROGRESS) {
                // "!!!! back receive";
                holder.iv.setImageResource(R.drawable.ic_default_pic);
                showDownloadImageProgress(message, holder);
                // downloadImage(message, holder);
            } else {
                // "!!!! not back receive, show image directly");
                holder.pb.setVisibility(View.GONE);
                holder.tv.setVisibility(View.GONE);
                //holder.iv.setImageResource(R.drawable.ic_default_pic);
                ImageMessageBody imgBody = (ImageMessageBody) message.getBody();
                if (imgBody.getLocalUrl() != null) {
                    // String filePath = imgBody.getLocalUrl();
                    String remotePath = imgBody.getRemoteUrl();
                    String filePath = ImageUtils.getImagePath(remotePath);
                    String thumbRemoteUrl = imgBody.getThumbnailUrl();
                    String thumbnailPath = ImageUtils.getThumbnailImagePath(thumbRemoteUrl);
                    Log.d(TAG, "接收方向的图片消息:" + thumbRemoteUrl);
                    showImageView(thumbnailPath, holder.iv, filePath, imgBody.getRemoteUrl(), message, bg_normal);
                }
            }
            return;
        }

        // 发送的消息
        // process send message
        // send pic, show the pic directly
        ImageMessageBody imgBody = (ImageMessageBody) message.getBody();
        String filePath = imgBody.getLocalUrl();
        if (filePath != null && new File(filePath).exists()) {
            showImageView(ImageUtils.getThumbnailImagePath(filePath), holder.iv, filePath, null, message,bg_normal);
        } else {
            showImageView(ImageUtils.getThumbnailImagePath(filePath), holder.iv, filePath, IMAGE_DIR, message,bg_normal);
        }

        switch (message.status) {
            case SUCCESS:
                holder.pb.setVisibility(View.GONE);
                holder.tv.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.GONE);
                break;
            case FAIL:
                holder.pb.setVisibility(View.GONE);
                holder.tv.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.VISIBLE);
                break;
            case INPROGRESS:
                holder.staus_iv.setVisibility(View.GONE);
                holder.pb.setVisibility(View.VISIBLE);
                holder.tv.setVisibility(View.VISIBLE);
                if (timers.containsKey(message.getMsgId()))
                    return;
                // set a timer
                final Timer timer = new Timer();
                timers.put(message.getMsgId(), timer);
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                holder.pb.setVisibility(View.VISIBLE);
                                holder.tv.setVisibility(View.VISIBLE);
                                holder.tv.setText(message.progress + "%");
                                if (message.status == EMMessage.Status.SUCCESS) {
                                    holder.pb.setVisibility(View.GONE);
                                    holder.tv.setVisibility(View.GONE);
                                    // message.setSendingStatus(Message.SENDING_STATUS_SUCCESS);
                                    timer.cancel();
                                } else if (message.status == EMMessage.Status.FAIL) {
                                    holder.pb.setVisibility(View.GONE);
                                    holder.tv.setVisibility(View.GONE);
                                    // message.setSendingStatus(Message.SENDING_STATUS_FAIL);
                                    // message.setProgress(0);
                                    holder.staus_iv.setVisibility(View.VISIBLE);
                                    //Toast.makeText(activity,
                                    //		activity.getString(R.string.send_fail) + activity.getString(R.string.connect_failuer_toast), 0)
                                    //		.show();
                                    timer.cancel();
                                }

                            }
                        });

                    }
                }, 0, 500);
                break;
            default:
                sendPictureMessage(message, holder);
        }
    }

    private void setImageSize(ImageView iv, EMMessage message) {
        try {
            int width = message.getIntAttribute("pic_width");
            int height = message.getIntAttribute("pic_height");
            if (width != 0 && height != 0) {
                TLog.log(TAG, "图片存在宽高:" + width + "/" + height);

                //ImageSize targetSize = decodingInfo.getTargetSize();
                //boolean powerOf2 = scaleType == ImageScaleType.IN_SAMPLE_POWER_OF_2;
                //scale = ImageSizeUtils.computeImageSampleSize(imageSize, targetSize, decodingInfo.getViewScaleType(), powerOf2);

                if (width > 160 && height > 160) {
                    float ws = width / 160f;
                    float hs = height / 160f;
                    float s = ws > hs ? ws : hs;
                    width = (int) (width / s * 2);
                    height = (int) (height / s * 2);
                } else if (width > 160) {
                    float ws = width / 160f;
                    width = (int) (width / ws * 2);
                    height = (int) (height / ws * 2);
                } else if (height > 160) {
                    float hs = height / 160f;
                    width = (int) (width / hs * 2);
                    height = (int) (height / hs * 2);
                }
                TLog.log(TAG, "图片存在宽高缩放结果:" + width + "/" + height);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);
                iv.setLayoutParams(lp);
            } else {
                TLog.log(TAG, "图片不存在宽高");
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                iv.setLayoutParams(lp);
            }
        } catch (EaseMobException e) {
            TLog.log(TAG, "找不到图片宽高属性");
            //e.printStackTrace();
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            iv.setLayoutParams(lp);
        }
    }

    /**
     * 视频消息
     *
     * @param message
     * @param holder
     * @param position
     * @param convertView
     */
    private void handleVideoMessage(final EMMessage message, final ViewHolder holder, final int position, View convertView) {

        VideoMessageBody videoBody = (VideoMessageBody) message.getBody();
        // final File image=new File(PathUtil.getInstance().getVideoPath(),
        // videoBody.getFileName());
        String localThumb = videoBody.getLocalThumb();

        holder.iv.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

                return true;
            }
        });

        if (localThumb != null) {

            showVideoThumbView(localThumb, holder.iv, videoBody.getThumbnailUrl(), message);
        }
        if (videoBody.getLength() > 0) {
            String time = DateUtils.toTimeBySecond(videoBody.getLength());
            holder.timeLength.setText(time);
        }
        //holder.playBtn.setImageResource(R.drawable.video_download_btn_nor);

        if (message.direct == Direct.RECEIVE) {
            if (videoBody.getVideoFileLength() > 0) {
                String size = TextFormater.getDataSize(videoBody.getVideoFileLength());
                holder.size.setText(size);
            }
        } else {
            if (videoBody.getLocalUrl() != null && new File(videoBody.getLocalUrl()).exists()) {
                String size = TextFormater.getDataSize(new File(videoBody.getLocalUrl()).length());
                holder.size.setText(size);
            }
        }

        if (message.direct == Direct.RECEIVE) {

            // System.err.println("it is receive msg");
            if (message.status == EMMessage.Status.INPROGRESS) {
                // System.err.println("!!!! back receive");
                holder.iv.setImageResource(R.drawable.ic_default_pic);
                showDownloadImageProgress(message, holder);

            } else {
                // System.err.println("!!!! not back receive, show image directly");
                holder.iv.setImageResource(R.drawable.ic_default_pic);
                if (localThumb != null) {
                    showVideoThumbView(localThumb, holder.iv, videoBody.getThumbnailUrl(), message);
                }

            }

            return;
        }
        holder.pb.setTag(position);

        // until here ,deal with send video msg
        switch (message.status) {
            case SUCCESS:
                holder.pb.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.GONE);
                holder.tv.setVisibility(View.GONE);
                break;
            case FAIL:
                holder.pb.setVisibility(View.GONE);
                holder.tv.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.VISIBLE);
                break;
            case INPROGRESS:
                if (timers.containsKey(message.getMsgId()))
                    return;
                // set a timer
                final Timer timer = new Timer();
                timers.put(message.getMsgId(), timer);
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                holder.pb.setVisibility(View.VISIBLE);
                                holder.tv.setVisibility(View.VISIBLE);
                                holder.tv.setText(message.progress + "%");
                                if (message.status == EMMessage.Status.SUCCESS) {
                                    holder.pb.setVisibility(View.GONE);
                                    holder.tv.setVisibility(View.GONE);
                                    // message.setSendingStatus(Message.SENDING_STATUS_SUCCESS);
                                    timer.cancel();
                                } else if (message.status == EMMessage.Status.FAIL) {
                                    holder.pb.setVisibility(View.GONE);
                                    holder.tv.setVisibility(View.GONE);
                                    // message.setSendingStatus(Message.SENDING_STATUS_FAIL);
                                    // message.setProgress(0);
                                    holder.staus_iv.setVisibility(View.VISIBLE);
                                    //Toast.makeText(activity,
                                    //		activity.getString(R.string.send_fail) + activity.getString(R.string.connect_failuer_toast), 0)
                                    //		.show();
                                    timer.cancel();
                                }

                            }
                        });

                    }
                }, 0, 500);
                break;
            default:
                // sendMsgInBackground(message, holder);
                sendPictureMessage(message, holder);

        }

    }

    /**
     * 语音消息
     *
     * @param message
     * @param holder
     * @param position
     * @param convertView
     */
    private void handleVoiceMessage(final EMMessage message, final ViewHolder holder, final int position, View convertView) {
		VoiceMessageBody voiceBody = (VoiceMessageBody) message.getBody();
		holder.tv.setText(voiceBody.getLength() + "\"");
		holder.voice_content.setOnClickListener(new VoicePlayClickListener(message,
                holder.iv, holder.iv_read_status, this, activity, username));
		holder.voice_content.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
//				activity.startActivityForResult(
//						(new Intent(activity, ContextMenu.class)).putExtra("position", position).putExtra("type",
//								Type.VOICE.ordinal()), ChatActivity.REQUEST_CODE_CONTEXT_MENU);
				return true;
			}
		});

        int total = (int) (TDevice.getScreenWidth()-TDevice.dpToPixel(60));
        int min = (int) TDevice.dpToPixel(60);
        int width = (total-min)/128 * voiceBody.getLength() + min;
        if (message.direct == Direct.RECEIVE) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) holder.voice_content.getLayoutParams();
            lp.width = width;
            holder.voice_content.setLayoutParams(lp);
        } else {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.voice_content.getLayoutParams();
            lp.width = width;
            holder.voice_content.setLayoutParams(lp);
        }

		if (((MessageActivity)activity).playMsgId != null
				&& ((MessageActivity)activity).playMsgId.equals(message
						.getMsgId())&&VoicePlayClickListener.isPlaying) {
			AnimationDrawable voiceAnimation;
			if (message.direct == Direct.RECEIVE) {
				holder.iv.setImageResource(R.anim.voice_from_icon);
			} else {
				holder.iv.setImageResource(R.anim.voice_to_icon);
			}
			voiceAnimation = (AnimationDrawable) holder.iv.getDrawable();
			voiceAnimation.start();
		} else {
			if (message.direct == Direct.RECEIVE) {
				holder.iv.setImageResource(R.drawable.chatfrom_voice_playing);
			} else {
				holder.iv.setImageResource(R.drawable.chatto_voice_playing);
			}
		}

		if (message.direct == Direct.RECEIVE) {
			if (message.isListened()) {
				// 隐藏语音未听标志
				holder.iv_read_status.setVisibility(View.INVISIBLE);
			} else {
				holder.iv_read_status.setVisibility(View.VISIBLE);
			}
			EMLog.d(TAG, "it is receive msg");
			if (message.status == EMMessage.Status.INPROGRESS) {
				holder.pb.setVisibility(View.VISIBLE);
				EMLog.d(TAG, "!!!! back receive");
				((FileMessageBody) message.getBody()).setDownloadCallback(new EMCallBack() {

					@Override
					public void onSuccess() {
						activity.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								holder.pb.setVisibility(View.INVISIBLE);
								notifyDataSetChanged();
							}
						});

					}

					@Override
					public void onProgress(int progress, String status) {
					}

					@Override
					public void onError(int code, String message) {
						activity.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								holder.pb.setVisibility(View.INVISIBLE);
							}
						});
					}
				});

			} else {
				holder.pb.setVisibility(View.INVISIBLE);
			}
			return;
		}

		// until here, deal with send voice msg
		switch (message.status) {
		case SUCCESS:
			holder.pb.setVisibility(View.GONE);
			holder.staus_iv.setVisibility(View.GONE);
			break;
		case FAIL:
			holder.pb.setVisibility(View.GONE);
			holder.staus_iv.setVisibility(View.VISIBLE);
			break;
		case INPROGRESS:
			holder.pb.setVisibility(View.VISIBLE);
			holder.staus_iv.setVisibility(View.GONE);
			break;
		default:
			sendMsgInBackground(message, holder);
		}
    }

    /**
     * 文件消息
     *
     * @param message
     * @param holder
     * @param position
     * @param convertView
     */
    private void handleFileMessage(final EMMessage message, final ViewHolder holder, int position, View convertView) {
        final NormalFileMessageBody fileMessageBody = (NormalFileMessageBody) message.getBody();
        final String filePath = fileMessageBody.getLocalUrl();
        holder.tv_file_name.setText(fileMessageBody.getFileName());
        holder.tv_file_size.setText(TextFormater.getDataSize(fileMessageBody.getFileSize()));
        holder.ll_container.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {

            }
        });
        String st1 = "已下载";//context.getResources().getString(R.string.Have_downloaded);
        String st2 = "未下载";//context.getResources().getString(R.string.Did_not_download);
        if (message.direct == Direct.RECEIVE) { // 接收的消息
            EMLog.d(TAG, "it is receive msg");
            File file = new File(filePath);
            if (file != null && file.exists()) {
                holder.tv_file_download_state.setText(st1);
            } else {
                holder.tv_file_download_state.setText(st2);
            }
            return;
        }

        // until here, deal with send voice msg
        switch (message.status) {
            case SUCCESS:
                holder.pb.setVisibility(View.INVISIBLE);
                holder.tv.setVisibility(View.INVISIBLE);
                holder.staus_iv.setVisibility(View.INVISIBLE);
                break;
            case FAIL:
                holder.pb.setVisibility(View.INVISIBLE);
                holder.tv.setVisibility(View.INVISIBLE);
                holder.staus_iv.setVisibility(View.VISIBLE);
                break;
            case INPROGRESS:
                if (timers.containsKey(message.getMsgId()))
                    return;
                // set a timer
                final Timer timer = new Timer();
                timers.put(message.getMsgId(), timer);
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                holder.pb.setVisibility(View.VISIBLE);
                                holder.tv.setVisibility(View.VISIBLE);
                                holder.tv.setText(message.progress + "%");
                                if (message.status == EMMessage.Status.SUCCESS) {
                                    holder.pb.setVisibility(View.INVISIBLE);
                                    holder.tv.setVisibility(View.INVISIBLE);
                                    timer.cancel();
                                } else if (message.status == EMMessage.Status.FAIL) {
                                    holder.pb.setVisibility(View.INVISIBLE);
                                    holder.tv.setVisibility(View.INVISIBLE);
                                    holder.staus_iv.setVisibility(View.VISIBLE);
                                    //Toast.makeText(activity,
                                    //		activity.getString(R.string.send_fail) + activity.getString(R.string.connect_failuer_toast), 0)
                                    //		.show();
                                    timer.cancel();
                                }

                            }
                        });

                    }
                }, 0, 500);
                break;
            default:
                // 发送消息
                sendMsgInBackground(message, holder);
        }

    }

    /**
     * 处理位置消息
     *
     * @param message
     * @param holder
     * @param position
     * @param convertView
     */
    private void handleLocationMessage(final EMMessage message, final ViewHolder holder, final int position, View convertView) {
        TextView locationView = ((TextView) convertView.findViewById(R.id.tv_location));
        LocationMessageBody locBody = (LocationMessageBody) message.getBody();
        locationView.setText(locBody.getAddress());
        LatLng loc = new LatLng(locBody.getLatitude(), locBody.getLongitude());
        locationView.setOnClickListener(new MapClickListener(loc, locBody.getAddress()));
        locationView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });

        if (message.direct == Direct.RECEIVE) {
            return;
        }
        // deal with send message
        switch (message.status) {
            case SUCCESS:
                holder.pb.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.GONE);
                break;
            case FAIL:
                holder.pb.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.VISIBLE);
                break;
            case INPROGRESS:
                holder.pb.setVisibility(View.VISIBLE);
                break;
            default:
                sendMsgInBackground(message, holder);
        }
    }

    /**
     * 发送消息
     *
     * @param message
     * @param holder
     */
    public void sendMsgInBackground(final EMMessage message, final ViewHolder holder) {
        holder.staus_iv.setVisibility(View.GONE);
        holder.pb.setVisibility(View.VISIBLE);

        final long start = System.currentTimeMillis();
        // 调用sdk发送异步发送方法
        EMChatManager.getInstance().sendMessage(message, new EMCallBack() {

            @Override
            public void onSuccess() {

                updateSendedView(message, holder);
            }

            @Override
            public void onError(int code, String error) {

                updateSendedView(message, holder);
            }

            @Override
            public void onProgress(int progress, String status) {
            }

        });

    }

    /*
     * chat sdk will automatic download thumbnail image for the image message we
     * need to register callback show the download progress
     */
    private void showDownloadImageProgress(final EMMessage message, final ViewHolder holder) {
        EMLog.d(TAG, "!!! show download image progress");
        // final ImageMessageBody msgbody = (ImageMessageBody)
        // message.getBody();
        final FileMessageBody msgbody = (FileMessageBody) message.getBody();
        if (holder.pb != null)
            holder.pb.setVisibility(View.VISIBLE);
        if (holder.tv != null)
            holder.tv.setVisibility(View.VISIBLE);

        msgbody.setDownloadCallback(new EMCallBack() {

            @Override
            public void onSuccess() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // message.setBackReceive(false);
                        if (message.getType() == Type.IMAGE) {
                            holder.pb.setVisibility(View.GONE);
                            holder.tv.setVisibility(View.GONE);
                        }
                        notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onError(int code, String message) {

            }

            @Override
            public void onProgress(final int progress, String status) {
                if (message.getType() == Type.IMAGE) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            holder.tv.setText(progress + "%");

                        }
                    });
                }

            }

        });
    }

    /*
     * send message with new sdk
     */
    private void sendPictureMessage(final EMMessage message, final ViewHolder holder) {
        try {
            String to = message.getTo();

            // before send, update ui
            holder.staus_iv.setVisibility(View.GONE);
            holder.pb.setVisibility(View.VISIBLE);
            holder.tv.setVisibility(View.VISIBLE);
            holder.tv.setText("0%");

            final long start = System.currentTimeMillis();
            // if (chatType == ChatActivity.CHATTYPE_SINGLE) {
            EMChatManager.getInstance().sendMessage(message, new EMCallBack() {

                @Override
                public void onSuccess() {
                    Log.d(TAG, "send image message successfully");
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            // send success
                            holder.pb.setVisibility(View.GONE);
                            holder.tv.setVisibility(View.GONE);
                        }
                    });
                }

                @Override
                public void onError(int code, String error) {

                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            holder.pb.setVisibility(View.GONE);
                            holder.tv.setVisibility(View.GONE);
                            // message.setSendingStatus(Message.SENDING_STATUS_FAIL);
                            holder.staus_iv.setVisibility(View.VISIBLE);
                            //Toast.makeText(activity,
                            //		activity.getString(R.string.send_fail) + activity.getString(R.string.connect_failuer_toast), 0).show();
                        }
                    });
                }

                @Override
                public void onProgress(final int progress, String status) {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            holder.tv.setText(progress + "%");
                        }
                    });
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新ui上消息发送状态
     *
     * @param message
     * @param holder
     */
    private void updateSendedView(final EMMessage message, final ViewHolder holder) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // send success
                if (message.getType() == Type.VIDEO) {
                    holder.tv.setVisibility(View.GONE);
                }
                EMLog.d(TAG, "message status : " + message.status);
                if (message.status == EMMessage.Status.SUCCESS) {
                    // if (message.getType() == EMMessage.Type.FILE) {
                    // holder.pb.setVisibility(View.INVISIBLE);
                    // holder.staus_iv.setVisibility(View.INVISIBLE);
                    // } else {
                    // holder.pb.setVisibility(View.GONE);
                    // holder.staus_iv.setVisibility(View.GONE);
                    // }

                } else if (message.status == EMMessage.Status.FAIL) {
                    // if (message.getType() == EMMessage.Type.FILE) {
                    // holder.pb.setVisibility(View.INVISIBLE);
                    // } else {
                    // holder.pb.setVisibility(View.GONE);
                    // }
                    // holder.staus_iv.setVisibility(View.VISIBLE);
                    //Toast.makeText(activity, activity.getString(R.string.send_fail) + activity.getString(R.string.connect_failuer_toast), 0)
                    //		.show();
                }

                notifyDataSetChanged();
            }
        });
    }

    /**
     * load image into image view
     *
     * @param thumbernailPath
     * @param iv
     * @return the image exists or not
     */
    private boolean showImageView(final String thumbernailPath, final ImageView iv, final String localFullSizePath, String remoteDir,
                                  final EMMessage message, final boolean bg_normal) {
//		 // String imagename =
        // localFullSizePath.substring(localFullSizePath.lastIndexOf("/") + 1,
        // localFullSizePath.length());
        // final String remote = remoteDir != null ? remoteDir+imagename :
        // imagename;
        final String remote = remoteDir;
        EMLog.d("###", "local = " + localFullSizePath + " remote: " + remote);
        // first check if the thumbnail image already loaded into cache
        //Bitmap bitmap = null;ImageCache.getInstance().get(thumbernailPath);
        //if (bitmap != null) {
        // thumbnail image is already loaded, reuse the drawable
        //iv.setImageBitmap(bitmap);
        Log.d(TAG, "图片:" + thumbernailPath + " 远程:" + remoteDir);
        File file = new File(thumbernailPath);
        ImageSize size = new ImageSize(160, 160);
        final boolean fromPic = message.direct == EMMessage.Direct.RECEIVE;
        if (fromPic) {
            Log.d(TAG, "接收到的图片：" + remote);
            ImageLoader.getInstance().loadImage(remote, size,
                    ImageUtils.getPictureDisplayOptions(), new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            super.onLoadingComplete(imageUri, view, loadedImage);
                            TLog.log(TAG, "图片载完 :" + loadedImage.getWidth() + "/" + loadedImage.getHeight());
                            iv.setImageBitmap(ImageUtils.getMessageBitmap(loadedImage, fromPic, bg_normal));
                        }
                    });
        } else if (file.exists()) {
            ImageLoader.getInstance().loadImage("file://" + file.getAbsolutePath(), size,
                    ImageUtils.getPictureDisplayOptions(), new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            super.onLoadingComplete(imageUri, view, loadedImage);
                            TLog.log(TAG, "图片载完 :" + loadedImage.getWidth() + "/" + loadedImage.getHeight());
                            iv.setImageBitmap(ImageUtils.getMessageBitmap(loadedImage, fromPic, bg_normal));
                        }
                    });
        } else if (message.direct == EMMessage.Direct.SEND) {
            ImageLoader.getInstance().loadImage("file://" + localFullSizePath, size,
                    ImageUtils.getPictureDisplayOptions(), new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            super.onLoadingComplete(imageUri, view, loadedImage);
                            TLog.log(TAG, "图片载完 :" + loadedImage.getWidth() + "/" + loadedImage.getHeight());
                            iv.setImageBitmap(ImageUtils.getMessageBitmap(loadedImage, fromPic, bg_normal));
                        }
                    });
        } else {
            Log.d(TAG, "接收到的图片：" + remote);
            ImageLoader.getInstance().loadImage(remote, size,
                    ImageUtils.getPictureDisplayOptions(), new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            super.onLoadingComplete(imageUri, view, loadedImage);
                            TLog.log(TAG, "图片载完 :" + loadedImage.getWidth() + "/" + loadedImage.getHeight());
                            iv.setImageBitmap(ImageUtils.getMessageBitmap(loadedImage, fromPic, bg_normal));
                        }
                    });
        }
        iv.setClickable(true);
        iv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String url;
                File file = new File(localFullSizePath);
                if (file.exists()) {
                    url = "file://" + file.getAbsolutePath();
                } else {
                    url = remote;
                }
                if (message != null && message.direct == EMMessage.Direct.RECEIVE && !message.isAcked
                        && message.getChatType() != ChatType.GroupChat) {
                    try {
                        EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
                        message.isAcked = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                UIHelper.showImagePreview(v.getContext(),new String[]{url});
            }
        });
        return true;
    }

    /**
     * 展示视频缩略图
     *
     * @param localThumb   本地缩略图路径
     * @param iv
     * @param thumbnailUrl 远程缩略图路径
     * @param message
     */
    private void showVideoThumbView(String localThumb, ImageView iv, String thumbnailUrl, final EMMessage message) {

    }

    public static class ViewHolder {
        ImageView iv;
        TextView tv;
        ProgressBar pb;
        ImageView staus_iv;
        AvatarView iv_avatar;
        AsyncTextView tv_usernick;
        ImageView playBtn;
        TextView timeLength;
        TextView size;
        LinearLayout container_status_btn;
        LinearLayout ll_container;
        ImageView iv_read_status;
        // 显示已读回执状态
        TextView tv_ack;
        // 显示送达回执状态
        TextView tv_delivered;

        TextView tv_file_name;
        TextView tv_file_size;
        TextView tv_file_download_state;
        View voice_content;
    }

    /*
     * 点击地图消息listener
     */
    class MapClickListener implements OnClickListener {

        LatLng location;
        String address;

        public MapClickListener(LatLng loc, String address) {
            location = loc;
            this.address = address;

        }

        @Override
        public void onClick(View v) {

        }
    }
}