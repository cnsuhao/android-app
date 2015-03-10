package net.oschina.app.v2.activity.active.adapter;

import android.graphics.Bitmap;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.Active;
import net.oschina.app.v2.model.Active.ObjectReply;
import net.oschina.app.v2.model.Tweet;
import net.oschina.app.v2.ui.AvatarView;
import net.oschina.app.v2.ui.text.MyLinkMovementMethod;
import net.oschina.app.v2.ui.text.MyURLSpan;
import net.oschina.app.v2.ui.text.TweetTextView;
import net.oschina.app.v2.utils.DateUtil;
import net.oschina.app.v2.utils.UIHelper;

public class ActiveAdapter extends RecycleBaseAdapter {
	private final static String AT_HOST_PRE = "http://my.oschina.net";
	private final static String MAIN_HOST = "http://www.oschina.net";
	private DisplayImageOptions options;

	public ActiveAdapter(){
		options = new DisplayImageOptions.Builder().cacheInMemory(true)
				.cacheOnDisk(true).bitmapConfig(Bitmap.Config.RGB_565).build();
	}

    @Override
    protected View onCreateItemView(ViewGroup parent, int viewType) {
        return getLayoutInflater(parent.getContext()).inflate(
                R.layout.v2_list_cell_active, null);
    }

    @Override
    protected RecycleBaseAdapter.ViewHolder onCreateItemViewHolder(View view, int viewType) {
        return new ViewHolder(viewType,view);
    }

    @Override
    protected void onBindItemViewHolder(RecycleBaseAdapter.ViewHolder holder, int position) {
        super.onBindItemViewHolder(holder, position);
        ViewHolder vh = (ViewHolder)holder;
        final Active item = (Active) _data.get(position);

        vh.name.setText(item.getAuthor());

        vh.action.setText(UIHelper.parseActiveAction2(item.getObjectType(),
                item.getObjectCatalog(), item.getObjectTitle()));

        if (TextUtils.isEmpty(item.getMessage())) {
            vh.body.setVisibility(View.GONE);
        } else {
            vh.body.setMovementMethod(MyLinkMovementMethod.a());
            vh.body.setFocusable(false);
            vh.body.setDispatchToParent(true);
            vh.body.setLongClickable(false);
            Spanned span = Html.fromHtml(modifyPath(item.getMessage()));
            vh.body.setText(span);
            MyURLSpan.parseLinkText(vh.body, span);
        }

        ObjectReply reply = item.getObjectReply();
        if (reply != null) {
            vh.reply.setMovementMethod(MyLinkMovementMethod.a());
            vh.reply.setFocusable(false);
            vh.reply.setDispatchToParent(true);
            vh.reply.setLongClickable(false);
            Spanned span = UIHelper.parseActiveReply(reply.objectName, reply.objectBody);
            vh.reply.setText(span);//
            MyURLSpan.parseLinkText(vh.reply, span);
            vh.lyReply.setVisibility(TextView.VISIBLE);
        } else {
            vh.reply.setText("");
            vh.lyReply.setVisibility(TextView.GONE);
        }

        vh.time.setText(DateUtil.getFormatTime(item.getPubDate()));

        vh.from.setVisibility(View.VISIBLE);
        switch (item.getAppClient()) {
            default:
                vh.from.setText("");
                break;
            case Active.CLIENT_MOBILE:
                vh.from.setText(R.string.from_mobile);
                break;
            case Active.CLIENT_ANDROID:
                vh.from.setText(R.string.from_android);
                break;
            case Active.CLIENT_IPHONE:
                vh.from.setText(R.string.from_iphone);
                break;
            case Active.CLIENT_WINDOWS_PHONE:
                vh.from.setText(R.string.from_windows_phone);
                break;
            //case Active.CLIENT_WECHAT:
            //    vh.from.setText(R.string.from_wechat);
            //    break;
        }

        if (item.getCommentCount() > 0) {
            vh.commentCount.setText(String.valueOf(item.getCommentCount()));
            vh.commentCount.setVisibility(View.VISIBLE);
        } else {
            vh.commentCount.setVisibility(View.GONE);
        }
        if (item.getActiveType() == Active.CATALOG_OTHER) {
            vh.retweetCount.setVisibility(View.VISIBLE);
        } else {
            vh.retweetCount.setVisibility(View.GONE);
        }

        vh.avatar.setUserInfo(item.getAuthorId(), item.getAuthor());
        vh.avatar.setAvatarUrl(item.getFace());

        if (!TextUtils.isEmpty(item.getTweetimage())) {
            vh.pic.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().displayImage(item.getTweetimage(), vh.pic,
                    options);
        } else {
            vh.pic.setVisibility(View.GONE);
            vh.pic.setImageBitmap(null);
        }
    }

	private String modifyPath(String message) {
		message = message.replaceAll("(<a[^>]+href=\")/([\\S]+)\"", "$1"
				+ AT_HOST_PRE + "/$2\"");
		message = message.replaceAll(
				"(<a[^>]+href=\")http://m.oschina.net([\\S]+)\"", "$1"
						+ MAIN_HOST + "$2\"");
		return message;
	}

	static class ViewHolder extends RecycleBaseAdapter.ViewHolder{
		public TextView name, from, time, action, actionName, commentCount,
				retweetCount;
		public TweetTextView body,reply;
		public ImageView pic;
		public View lyReply;
		public AvatarView avatar;

		public ViewHolder(int viewType,View view) {
            super(viewType,view);
			name = (TextView) view.findViewById(R.id.tv_name);
			from = (TextView) view.findViewById(R.id.tv_from);
			body = (TweetTextView) view.findViewById(R.id.tv_body);
			lyReply= view.findViewById(R.id.ly_reply);
			reply = (TweetTextView) view.findViewById(R.id.tv_reply);
			time = (TextView) view.findViewById(R.id.tv_time);
			action = (TextView) view.findViewById(R.id.tv_action);
			actionName = (TextView) view.findViewById(R.id.tv_action_name);
			commentCount = (TextView) view.findViewById(R.id.tv_comment_count);
			retweetCount = (TextView) view.findViewById(R.id.tv_retweet_count);
			avatar = (AvatarView) view.findViewById(R.id.iv_avatar);
			pic = (ImageView) view.findViewById(R.id.iv_pic);
		}
	}
}
