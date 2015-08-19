package net.oschina.app.v2.activity.tweet.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.activity.tweet.TweetTabClickListener;
import net.oschina.app.v2.base.ListBaseAdapter;
import net.oschina.app.v2.model.Tweet;
import net.oschina.app.v2.model.User;
import net.oschina.app.v2.ui.AvatarView;
import net.oschina.app.v2.utils.TDevice;

/**
 * Created by Tonlin on 2015/8/18.
 */
public class TweetLikeAdapter extends ListBaseAdapter {

    public static final int STATE_TWEET_LOADING = 6;
    public static final int STATE_TWEET_ERROR = 7;
    public static final int STATE_TWEET_EMPTY = 8;

    private static final java.lang.String TAG = "TweetLike";
    private final TweetTabClickListener mDelegate;
    private int mEmptyHeight = -1;
    private View mTabBar;
    private Tweet mTweet;

    public TweetLikeAdapter(TweetTabClickListener delegate) {
        mDelegate = delegate;
    }

    public void setTweet(Tweet tweet) {
        mTweet = tweet;
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
        switch (getState()) {
            case STATE_TWEET_LOADING:
            case STATE_TWEET_ERROR:
            case STATE_TWEET_EMPTY:
                return getDataSize() + 2;
        }
        return super.getCount() + 1;
    }

    @Override
    protected View getRealView(int position, View convertView, ViewGroup parent) {
        if (getCount() - 2 == position &&
                (getState() == STATE_TWEET_LOADING || getState() == STATE_TWEET_ERROR || getState() == STATE_TWEET_EMPTY)) {
            convertView = getLayoutInflater(parent.getContext()).inflate(R.layout.v2_list_cell_tweet_loading, null);

            View loading = convertView.findViewById(R.id.ly_loading);
            View error = convertView.findViewById(R.id.ly_error);
            Button btn = (Button) convertView.findViewById(R.id.btn_refresh);
            TextView text = (TextView)convertView.findViewById(R.id.tv_text);
            switch (getState()){
                case STATE_TWEET_LOADING:
                    loading.setVisibility(View.VISIBLE);
                    error.setVisibility(View.GONE);
                    break;
                case STATE_TWEET_EMPTY:
                    loading.setVisibility(View.GONE);
                    error.setVisibility(View.VISIBLE);
                    text.setText("还没有小伙伴来点赞\n赶紧来抢个沙发吧");
                    btn.setText("点赞");
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mDelegate != null) {
                                mDelegate.onLikeClick();
                            }
                        }
                    });
                    break;
                case STATE_TWEET_ERROR:
                    loading.setVisibility(View.GONE);
                    error.setVisibility(View.VISIBLE);
                    if(TDevice.hasInternet()){
                        text.setText("Oops加载出错啦\n");
                        btn.setText("刷新");
                    } else {
                        text.setText("什么年代啦,还没有网络哦");
                        btn.setText("刷新");
                    }
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(mDelegate != null){
                                mDelegate.onRefreshData(1);
                            }
                        }
                    });
                    break;
            }
            View bar = convertView.findViewById(R.id.tab_bar);
            final TextView commentCount = (TextView) convertView.findViewById(R.id.tv_comment_count);
            final TextView likeCount = (TextView) convertView.findViewById(R.id.tv_like_count);
            //bar.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
            if (position == 0) {
                TextView like = (TextView) convertView.findViewById(R.id.tv_like_opt);
                like.setText(mTweet.getIsLike()==1?"已赞":"赞一下");
                like.setCompoundDrawablesWithIntrinsicBounds(mTweet.getIsLike() == 1 ?
                        R.drawable.ic_like_selected : R.drawable.ic_like_normal, 0, 0, 0);
                like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mDelegate != null){
                            mDelegate.onLikeClick();
                        }
                    }
                });
                likeCount.setSelected(true);
                commentCount.setText("评论 (" + mTweet.getCommentCount() + ")");
                likeCount.setText("点赞 (" + mTweet.getLikeCount() + ")");
                commentCount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mDelegate != null) {
                            mDelegate.onTabChanged(0);
                        }
                        commentCount.setSelected(true);
                        likeCount.setSelected(false);
                    }
                });
                likeCount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mDelegate != null) {
                            mDelegate.onTabChanged(1);
                        }
                        commentCount.setSelected(false);
                        likeCount.setSelected(true);
                    }
                });
            }
        } else if (getCount() - 1 == position) {
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
                vh.like.setText(mTweet.getIsLike()==1?"已赞":"赞一下");
                vh.like.setCompoundDrawablesWithIntrinsicBounds(mTweet.getIsLike() == 1
                        ? R.drawable.ic_like_selected : R.drawable.ic_like_normal, 0, 0, 0);
                vh.like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mDelegate != null) {
                            mDelegate.onLikeClick();
                        }
                    }
                });
                vh.commentCount.setText("评论 (" + mTweet.getCommentCount() + ")");
                vh.likeCount.setText("点赞 (" + mTweet.getLikeCount() + ")");
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
//            ImageLoader.getInstance().displayImage(item.getFace(), vh.avatar);
            vh.avatar.setUserInfo(item.getUid(),item.getName());
            vh.avatar.setAvatarUrl(item.getFace());
        }
        return convertView;
    }

    static class ViewHolder {
        LinearLayout bar;
        TextView commentCount, likeCount,like;
        AvatarView avatar;
        TextView name;

        ViewHolder(View view) {
            like = (TextView) view.findViewById(R.id.tv_like_opt);
            commentCount = (TextView) view.findViewById(R.id.tv_comment_count);
            likeCount = (TextView) view.findViewById(R.id.tv_like_count);
            bar = (LinearLayout) view.findViewById(R.id.tab_bar);
            avatar = (AvatarView) view.findViewById(R.id.iv_avatar);
            name = (TextView) view.findViewById(R.id.tv_name);
        }
    }
}
