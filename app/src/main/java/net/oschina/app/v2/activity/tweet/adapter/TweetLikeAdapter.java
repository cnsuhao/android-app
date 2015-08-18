package net.oschina.app.v2.activity.tweet.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.activity.tweet.TweetTabClickListener;
import net.oschina.app.v2.base.ListBaseAdapter;
import net.oschina.app.v2.model.User;
import net.oschina.app.v2.utils.TDevice;

/**
 * Created by Tonlin on 2015/8/18.
 */
public class TweetLikeAdapter extends ListBaseAdapter {

    private static final java.lang.String TAG = "TweetLike";
    private final TweetTabClickListener mDelegate;
    private int mEmptyHeight = -1;
    private View mTabBar;
    private int mCommentCount;
    private int mLikeCount;

    public TweetLikeAdapter(TweetTabClickListener delegate) {
        mDelegate = delegate;
    }

    public void setLikeCount(int count) {
        mLikeCount = count;
    }

    public void setCommentCount(int count) {
        mCommentCount = count;
    }

    public View getTabBar() {
        return mTabBar;
    }

    public void setEmptyHeight(int height) {
        mEmptyHeight = height;
    }

    @Override
    protected int getLastPos(int position) {
        if (getState() == STATE_LOAD_MORE || getState() == STATE_NO_MORE
                || getState() == STATE_EMPTY_ITEM
                || getState() == STATE_NETWORK_ERROR)
            return 2;
        return 1;
    }

    @Override
    public int getCount() {
        return super.getCount() + 1;
    }

    @Override
    protected View getRealView(int position, View convertView, ViewGroup parent) {
        if (getCount() - 1 == position) {
            convertView = getLayoutInflater(parent.getContext()).inflate(R.layout.v2_list_cell_tweet_empty, null);
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
                convertView = getLayoutInflater(parent.getContext()).inflate(R.layout.v2_list_cell_tweet_like, null);
                vh = new ViewHolder(convertView);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) obj;
            }

            vh.bar.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
            if (position == 0) {
                mTabBar = vh.bar;
                vh.commentCount.setText("评论 (" + mCommentCount + ")");
                vh.likeCount.setText("点赞 (" + mLikeCount + ")");
                vh.likeCount.setSelected(true);
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

            User item = (User) _data.get(position);

            vh.name.setText(item.getName());
            ImageLoader.getInstance().displayImage(item.getFace(), vh.avatar);
        }
        return convertView;
    }

    static class ViewHolder {
        LinearLayout bar;
        TextView commentCount, likeCount;
        ImageView avatar;
        TextView name;

        ViewHolder(View view) {
            commentCount = (TextView) view.findViewById(R.id.tv_comment_count);
            likeCount = (TextView) view.findViewById(R.id.tv_like_count);
            bar = (LinearLayout) view.findViewById(R.id.tab_bar);
            avatar = (ImageView) view.findViewById(R.id.iv_avatar);
            name = (TextView) view.findViewById(R.id.tv_name);
        }
    }
}
