package net.oschina.app.v2.activity.chat.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.MessageBody;
import com.easemob.chat.TextMessageBody;
import com.easemob.exceptions.EaseMobException;
import com.facebook.drawee.view.SimpleDraweeView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.activity.chat.loader.AwareView;
import net.oschina.app.v2.activity.chat.loader.CacheType;
import net.oschina.app.v2.activity.chat.loader.ContactsFetcher;
import net.oschina.app.v2.activity.chat.loader.DisplayListener;
import net.oschina.app.v2.activity.chat.loader.IName;
import net.oschina.app.v2.activity.chat.view.AsynTextView;
import net.oschina.app.v2.base.Constants;
import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.chat.Avatar;
import net.oschina.app.v2.model.chat.IMGroup;
import net.oschina.app.v2.model.chat.IMUser;
import net.oschina.app.v2.ui.AvatarView;
import net.oschina.app.v2.utils.AvatarUtils;
import net.oschina.app.v2.utils.DateUtil;
import net.oschina.app.v2.utils.ImageUtils;

import java.net.URL;

/**
 * Created by Tonlin on 2015/5/27.
 */
public class ConversationAdapter extends RecycleBaseAdapter {

    @Override
    protected View onCreateItemView(ViewGroup parent, int viewType) {
        return getLayoutInflater(parent.getContext()).inflate(R.layout.v2_list_cell_chat_conversation, null);
    }

    @Override
    protected ViewHolder onCreateItemViewHolder(View view, int viewType) {
        return new ViewHolder(viewType, view);
    }

    @Override
    protected void onBindItemViewHolder(RecycleBaseAdapter.ViewHolder holder, int position) {
        super.onBindItemViewHolder(holder, position);
        final EMConversation item = (EMConversation) getItem(position);

        ViewHolder vh = (ViewHolder) holder;

        //vh.name.setText(item.getUserName());

        if (item.getUnreadMsgCount() > 0) {
            // 显示与此用户的消息未读数
            vh.unreadCount.setText(String.valueOf(item.getUnreadMsgCount()));
            vh.unreadCount.setVisibility(View.VISIBLE);
        } else {
            vh.unreadCount.setVisibility(View.INVISIBLE);
        }

        if (item.getMsgCount() != 0) {
            // 把最后一条消息的内容作为item的message内容
            EMMessage lastMessage = item.getLastMessage();
            //vh.message.setText(SmileUtils.getSmiledText(vh.message.getContext(),
            //               getMessageDigest(lastMessage, (vh.message.getContext()))),
            //        TextView.BufferType.SPANNABLE);
            //
            //vh.message.setText("");
            vh.message.setText(getMessageDigest(lastMessage, (vh.message.getContext())));

            vh.time.setText(DateUtil.getFormatTime(lastMessage.getMsgTime()));
            //DateUtils.getTimestampString(new Date(lastMessage.getMsgTime()))
            if (lastMessage.direct == EMMessage.Direct.SEND
                    && lastMessage.status == EMMessage.Status.FAIL) {
                vh.msgState.setVisibility(View.VISIBLE);
            } else {
                vh.msgState.setVisibility(View.GONE);
            }
        }

        final AvatarView avatar = vh.avatar;
        vh.name.setText("");
        vh.avatar.setGroup(item.isGroup());
        if (item.isGroup()) {
            IName name = ContactsFetcher.getInstance().getNameCache().getBitmapFromMemCache(item.getUserName());
            if (name == null) {
                name = ContactsFetcher.getInstance().getNameCache().getBitmapFromDiskCache(item.getUserName(), CacheType.GROUP);
            }
            if (name != null && name instanceof IMGroup) {
                IMGroup group = (IMGroup) name;
                vh.name.setText(group.getName());
                item.setExtField(name.getName());
                //ImageLoader.getInstance().displayImage(group.getPhoto(), avatar);
                avatar.setAvatarUrl(name.getPhoto());
            } else {
                ContactsFetcher.getInstance().loadName(item.getUserName(), CacheType.GROUP, vh.name, new DisplayListener() {
                    @Override
                    public void onLoadSuccess(AwareView awareView, IName name) {
                        item.setExtField(name.getName());
                        //ImageLoader.getInstance().displayImage(name.getPhoto(), avatar);
                        avatar.setAvatarUrl(name.getPhoto());
                    }

                    @Override
                    public void onLoadFailure(AwareView awareView, IName name) {
                    }
                });
            }
        } else {
            IName name = ContactsFetcher.getInstance().getNameCache().getBitmapFromMemCache(item.getUserName());
            if (name == null) {
                name = ContactsFetcher.getInstance().getNameCache().getBitmapFromDiskCache(item.getUserName(), CacheType.USER);
            }
            if (name != null && name instanceof IMUser) {
                IMUser user = (IMUser) name;
                vh.name.setText(user.getName());
                item.setExtField(name.getName());
                //ImageLoader.getInstance().displayImage(user.getPhoto(), avatar);
                avatar.setAvatarUrl(name.getPhoto());
            } else {
                if ("admin".equals(item.getUserName())) {
                    item.setExtField(item.getUserName());
                    vh.name.setText(item.getUserName());
                    vh.avatar.setImageURI(Uri.parse(""));
                } else {
                    ContactsFetcher.getInstance().loadName(item.getUserName(),
                            CacheType.USER, vh.name, new DisplayListener() {
                                @Override
                                public void onLoadSuccess(AwareView awareView, IName name) {
                                    item.setExtField(name.getName());
                                    //ImageLoader.getInstance().displayImage(name.getPhoto(), avatar);
                                    avatar.setAvatarUrl(name.getPhoto());
                                }

                                @Override
                                public void onLoadFailure(AwareView awareView, IName name) {
                                }
                            });
                }
            }
        }
    }

    public static class ViewHolder extends RecycleBaseAdapter.ViewHolder {
        private TextView message, time, unreadCount;
        private AsynTextView name;
        private ImageView msgState;
        private AvatarView avatar;

        public ViewHolder(int viewType, View v) {
            super(viewType, v);
            name = (AsynTextView) v.findViewById(R.id.tv_name);
            message = (TextView) v.findViewById(R.id.tv_message);
            time = (TextView) v.findViewById(R.id.tv_time);
            unreadCount = (TextView) v.findViewById(R.id.tv_unread_msg_number);
            msgState = (ImageView) v.findViewById(R.id.iv_msg_state);
            avatar = (AvatarView) v.findViewById(R.id.iv_avatar);
        }
    }

    /**
     * 根据消息内容和消息类型获取消息内容提示
     *
     * @param message
     * @param context
     * @return
     */
    private String getMessageDigest(EMMessage message, Context context) {
        String digest = "";
        switch (message.getType()) {
            case LOCATION: // 位置消息
                if (message.direct == EMMessage.Direct.RECEIVE) {
                    // 从sdk中提到了ui中，使用更简单不犯错的获取string的方法
                    // digest = EasyUtils.getAppResourceString(context,
                    // "location_recv");
                    digest = getString(context, R.string.location_recv);
                    digest = String.format(digest, message.getFrom());
                    return digest;
                } else {
                    // digest = EasyUtils.getAppResourceString(context,
                    // "location_prefix");
                    digest = getString(context, R.string.location_prefix);
                }
                break;
            case IMAGE: // 图片消息
                ImageMessageBody imageBody = (ImageMessageBody) message.getBody();
//                digest = getString(context, R.string.picture) + imageBody.getFileName();
                digest = getString(context, R.string.picture);
                break;
            case VOICE:// 语音消息
                digest = getString(context, R.string.voice);
                break;
            case VIDEO: // 视频消息
                digest = getString(context, R.string.video);
                break;
            case TXT: // 文本消息
                if (!message.getBooleanAttribute(Constants.MESSAGE_ATTR_IS_VOICE_CALL, false)) {
                    TextMessageBody txtBody = (TextMessageBody) message.getBody();
                    digest = txtBody.getMessage();
                } else {
                    TextMessageBody txtBody = (TextMessageBody) message.getBody();
                    digest = getString(context, R.string.voice_call) + txtBody.getMessage();
                }
                break;
            case FILE: // 普通文件消息
                digest = getString(context, R.string.file);
                break;
            default:
                System.err.println("error, unknow type");
                return "";
        }
        return digest;
    }

    private String getString(Context context, int resid) {
        return context.getResources().getString(resid);
    }
}
