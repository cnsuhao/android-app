package net.oschina.app.v2.activity.user.adapter;

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
import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.Active;
import net.oschina.app.v2.model.Tweet;
import net.oschina.app.v2.ui.text.MyLinkMovementMethod;
import net.oschina.app.v2.ui.text.MyURLSpan;
import net.oschina.app.v2.ui.text.TweetTextView;
import net.oschina.app.v2.utils.StringUtils;
import net.oschina.app.v2.utils.UIHelper;

/**
 * Created by Tonlin on 2015/8/21.
 */
public class UserActiveAdapter extends RecycleBaseAdapter {
    private final static String AT_HOST_PRE = "http://my.oschina.net";
    private final static String MAIN_HOST = "http://www.oschina.net";

    private final DisplayImageOptions options;

    public UserActiveAdapter(){
        options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).postProcessor(new BitmapProcessor() {

                    @Override
                    public Bitmap process(Bitmap arg0) {
                        return arg0;
                    }
                }).build();
    }

    @Override
    protected View onCreateItemView(ViewGroup parent, int viewType) {
        return getLayoutInflater(parent.getContext()).inflate(R.layout.v2_list_cell_active, null);
    }

    @Override
    protected ViewHolder onCreateItemViewHolder(View view, int viewType) {
        return new ViewHolder(viewType, view);
    }

    @Override
    protected void onBindItemViewHolder(RecycleBaseAdapter.ViewHolder holder, int position) {
        super.onBindItemViewHolder(holder, position);
        UserActiveAdapter.ViewHolder vh = (ViewHolder) holder;
        Active item = (Active) _data.get(position);

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

        vh.time.setText(StringUtils.friendly_time(item.getPubDate()));

        vh.from.setVisibility(View.VISIBLE);
        switch (item.getAppClient()) {
            default:
                vh.from.setText("");
                break;
            case Tweet.CLIENT_MOBILE:
                vh.from.setText(R.string.from_mobile);
                break;
            case Tweet.CLIENT_ANDROID:
                vh.from.setText(R.string.from_android);
                break;
            case Tweet.CLIENT_IPHONE:
                vh.from.setText(R.string.from_iphone);
                break;
            case Tweet.CLIENT_WINDOWS_PHONE:
                vh.from.setText(R.string.from_windows_phone);
                break;
            case Tweet.CLIENT_WECHAT:
                vh.from.setText(R.string.from_wechat);
                break;
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

        String faceURL = item.getFace();
        if (faceURL.endsWith("portrait.gif") || StringUtils.isEmpty(faceURL)) {
            vh.avatar.setImageBitmap(null);
        } else {
            ImageLoader.getInstance().displayImage(item.getFace(), vh.avatar);
        }

        if (!TextUtils.isEmpty(item.getTweetimage())) {
            vh.pic.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().displayImage(item.getTweetimage(),
                    vh.pic, options);
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

    public static class ViewHolder extends RecycleBaseAdapter.ViewHolder {
        public TextView name, from, time, action, actionName, commentCount,
                retweetCount;
        public TweetTextView body;
        public ImageView avatar, pic;

        public ViewHolder(int viewType,View view) {
            super(viewType,view);
            name = (TextView) view.findViewById(R.id.tv_name);
            from = (TextView) view.findViewById(R.id.tv_from);
            body = (TweetTextView) view.findViewById(R.id.tv_body);
            time = (TextView) view.findViewById(R.id.tv_time);
            action = (TextView) view.findViewById(R.id.tv_action);
            actionName = (TextView) view.findViewById(R.id.tv_action_name);
            commentCount = (TextView) view.findViewById(R.id.tv_comment_count);
            retweetCount = (TextView) view.findViewById(R.id.tv_retweet_count);
            avatar = (ImageView) view.findViewById(R.id.iv_avatar);
            pic = (ImageView) view.findViewById(R.id.iv_pic);
        }
    }
}
