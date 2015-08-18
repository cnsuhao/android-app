package net.oschina.app.v2.activity.tweet.adapter;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.activity.tweet.TweetTabClickListener;
import net.oschina.app.v2.base.ListBaseAdapter;
import net.oschina.app.v2.model.Comment;
import net.oschina.app.v2.model.Tweet;
import net.oschina.app.v2.ui.text.MyLinkMovementMethod;
import net.oschina.app.v2.ui.text.MyURLSpan;
import net.oschina.app.v2.ui.text.TweetTextView;
import net.oschina.app.v2.utils.DateUtil;
import net.oschina.app.v2.utils.TDevice;
import net.oschina.app.v2.utils.UIHelper;

/**
 * Created by Tonlin on 2015/8/18.
 */
public class TweetCommentAdapter extends ListBaseAdapter {

    private final TweetTabClickListener mDelegate;
    private View mTabBar, mEmptyView;
    private int mEmptyHeight = -1;
    private int mLikeCount;
    private int mCommentCount;

    public TweetCommentAdapter(TweetTabClickListener delegate) {
        mDelegate = delegate;
    }

    public View getEmptyView() {
        return mEmptyView;
    }

    public View getTabBar() {
        return mTabBar;
    }

    public void setEmptyHeight(int height) {
        mEmptyHeight = height;
    }

    public void setLikeCount(int count) {
        mLikeCount = count;
    }

    public void setCommentCount(int count) {
        mCommentCount = count;
    }

    @Override
    protected int getLastPos(int position) {
        return 2;
    }

    @Override
    public int getCount() {
        return super.getCount() + 1;
    }

    @Override
    protected View getRealView(int position, View convertView, ViewGroup parent) {
        if (getCount() - 1 == position) {
            if (mEmptyView == null) {
                mEmptyView = getLayoutInflater(parent.getContext()).inflate(R.layout.v2_list_cell_tweet_empty, null);
            }
            convertView = mEmptyView;
            View item = convertView.findViewById(R.id.empty);
            if (mEmptyHeight != -1) {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mEmptyHeight);
                item.setLayoutParams(lp);
            } else {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) TDevice.dpToPixel(800));
                item.setLayoutParams(lp);
            }
        } else {
            ViewHolder vh;
            Object obj;
            if (convertView == null || (!((obj = convertView.getTag()) instanceof ViewHolder))) {
                convertView = getLayoutInflater(parent.getContext()).inflate(R.layout.v2_list_cell_tweet_comment, null);
                vh = new ViewHolder(convertView);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) obj;
            }

            vh.bar.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
            if (position == 0) {
                mTabBar = vh.bar;
                vh.commentCount.setSelected(true);
                vh.commentCount.setText("评论 (" + mCommentCount + ")");
                vh.likeCount.setText("点赞 (" + mLikeCount + ")");
                final TextView cc = vh.commentCount;
                final TextView lc = vh.likeCount;
                vh.commentCount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mDelegate != null) {
                            mDelegate.onTabChanged(0);
                        }
                        cc.setSelected(true);
                        lc.setSelected(false);
                    }
                });
                vh.likeCount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mDelegate != null) {
                            mDelegate.onTabChanged(1);
                        }
                        cc.setSelected(false);
                        lc.setSelected(true);
                    }
                });
            }

            final Comment item = (Comment) _data.get(position);
            vh.name.setText(item.getAuthor());

            vh.content.setMovementMethod(MyLinkMovementMethod.a());
            vh.content.setFocusable(false);
            vh.content.setDispatchToParent(true);
            vh.content.setLongClickable(false);
            Spanned span = Html.fromHtml(item.getContent());
            vh.content.setText(span);
            MyURLSpan.parseLinkText(vh.content, span);

            vh.time.setText(DateUtil.getFormatTime(item.getPubDate()));

            vh.from.setVisibility(View.VISIBLE);
            switch (item.getAppClient()) {
                default:
                    vh.from.setText("");
                    vh.from.setVisibility(View.GONE);
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

            final Context context = vh.avatar.getContext();
            ImageLoader.getInstance().displayImage(item.getFace(), vh.avatar);

            vh.avatar.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    UIHelper.showUserCenter(context,
                            item.getAuthorId(), item.getAuthor());
                }
            });

            vh.split.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    static class ViewHolder {
        TextView name, time, from;
        TweetTextView content;
        View split;
        ImageView avatar;
        LinearLayout bar;
        TextView commentCount, likeCount;

        ViewHolder(View view) {
            commentCount = (TextView) view.findViewById(R.id.tv_comment_count);
            likeCount = (TextView) view.findViewById(R.id.tv_like_count);
            bar = (LinearLayout) view.findViewById(R.id.tab_bar);
            avatar = (ImageView) view.findViewById(R.id.iv_avatar);
            name = (TextView) view.findViewById(R.id.tv_name);
            content = (TweetTextView) view.findViewById(R.id.tv_content);
            time = (TextView) view.findViewById(R.id.tv_time);
            from = (TextView) view.findViewById(R.id.tv_from);
            split = view.findViewById(R.id.split);
        }
    }
}
