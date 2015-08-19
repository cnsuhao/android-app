package net.oschina.app.v2.activity.tweet.fragment;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ZoomButtonsController;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.tonlin.osc.happy.R;
import com.umeng.analytics.MobclickAgent;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.comment.adapter.CommentAdapter.OnOperationListener;
import net.oschina.app.v2.activity.news.fragment.EmojiFragmentControl;
import net.oschina.app.v2.activity.tweet.TweetTabClickListener;
import net.oschina.app.v2.activity.tweet.adapter.TweetCommentAdapter;
import net.oschina.app.v2.activity.tweet.adapter.TweetLikeAdapter;
import net.oschina.app.v2.api.OperationResponseHandler;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.base.BaseFragment;
import net.oschina.app.v2.base.Constants;
import net.oschina.app.v2.base.ListBaseAdapter;
import net.oschina.app.v2.cache.CacheManager;
import net.oschina.app.v2.emoji.EmojiFragment;
import net.oschina.app.v2.emoji.EmojiFragment.EmojiTextListener;
import net.oschina.app.v2.model.Comment;
import net.oschina.app.v2.model.CommentList;
import net.oschina.app.v2.model.Result;
import net.oschina.app.v2.model.Tweet;
import net.oschina.app.v2.model.TweetDetail;
import net.oschina.app.v2.model.TweetLikeUserList;
import net.oschina.app.v2.model.User;
import net.oschina.app.v2.service.PublicCommentTask;
import net.oschina.app.v2.service.ServerTaskUtils;
import net.oschina.app.v2.ui.empty.EmptyLayout;
import net.oschina.app.v2.ui.text.MyLinkMovementMethod;
import net.oschina.app.v2.ui.text.MyURLSpan;
import net.oschina.app.v2.ui.text.TweetTextView;
import net.oschina.app.v2.utils.HTMLSpirit;
import net.oschina.app.v2.utils.StringUtils;
import net.oschina.app.v2.utils.TDevice;
import net.oschina.app.v2.utils.TLog;
import net.oschina.app.v2.utils.UIHelper;
import net.oschina.app.v2.utils.XmlUtils;

import org.apache.http.Header;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.List;


public class TweetDetailFragmentNew extends BaseFragment implements
        EmojiTextListener, EmojiFragmentControl, OnOperationListener, AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener, TweetTabClickListener {
    protected static final String TAG = TweetDetailFragmentNew.class
            .getSimpleName();
    private static final int REQUEST_CODE = 0x1;
    private static final String CACHE_KEY_PREFIX = "tweet_";
    private static final String CACHE_KEY_TWEET_COMMENT = "tweet_comment_";
    private static final String CACHE_KEY_TWEET_LIKE = "tweet_like_";
    private static final String TWEET_DETAIL_SCREEN = "tweet_detail_screen";
    private ListView mListView;
    private EmptyLayout mEmptyView;
    private ImageView mIvAvatar, mIvPic;
    private TextView mTvName, mTvFrom, mTvTime, mTvCommentCount;
    private WebView mWVContent;
    private int mTweetId;
    private Tweet mTweet;
    private int mCurrentPage = 0, mCurrentLikePage = 0;
    private TweetCommentAdapter mCommentAdapter;
    private TweetLikeAdapter mLikeAdapter;
    private EmojiFragment mEmojiFragment;
    private BroadcastReceiver mCommentReceiver;
    private String mTweetContent;
    private TweetTextView mTvContent;
    private RadioGroup mStickyTabBar;
    private int mCurrentList = 0;
    private RadioButton mRbCommentCount, mRbLikeCount;
    private TextView mTvLikeOpt;
    private int mStateComment = STATE_NONE;
    private int mStateLike = STATE_NONE;


    class CommentChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int opt = intent.getIntExtra(Comment.BUNDLE_KEY_OPERATION, 0);
            int id = intent.getIntExtra(Comment.BUNDLE_KEY_ID, 0);
            int catalog = intent.getIntExtra(Comment.BUNDLE_KEY_CATALOG, 0);
            boolean isBlog = intent.getBooleanExtra(Comment.BUNDLE_KEY_BLOG,
                    false);
            Comment comment = intent
                    .getParcelableExtra(Comment.BUNDLE_KEY_COMMENT);
            onCommentChanged(opt, id, catalog, isBlog, comment);
        }
    }

    private boolean mCalculateEmptyHeight = false, mCalculateLikeEmptyHeight = false;
    private AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem == 1 && !mCalculateEmptyHeight && mCurrentList == 0
                    && mCommentAdapter != null && mCommentAdapter.getTabBar() != null) {
                Log.e(TAG, "需要重新计算Comment Empty 高度");
                if ((firstVisibleItem + visibleItemCount) == totalItemCount) {
                    Rect r = new Rect();
                    View v = mListView.getChildAt(mListView.getLastVisiblePosition() - 1);
                    v.getGlobalVisibleRect(r);
                    int height = r.bottom - r.top;
                    Log.e(TAG, "Comment Empty height:" + height + " top:" + r.top + " bottom:" + r.bottom + "　view:" + v.getTag());
                    mCommentAdapter.setEmptyHeight(height);
                } else {
                    mCommentAdapter.setEmptyHeight(0);
                }
                mCalculateEmptyHeight = true;
                mCommentAdapter.notifyDataSetChanged();
            } else if (firstVisibleItem == 1 && !mCalculateLikeEmptyHeight && mCurrentList == 1 && mLikeAdapter != null && mLikeAdapter.getTabBar() != null) {
                Log.e(TAG, "需要重新计算Like Empty 高度");
                if ((firstVisibleItem + visibleItemCount) == totalItemCount) {
                    Rect r = new Rect();
                    mListView.getChildAt(mListView.getChildCount() - 1).getGlobalVisibleRect(r);
                    int height = r.bottom - r.top;
                    Log.e(TAG, "Like Empty height:" + height);
                    mLikeAdapter.setEmptyHeight(height);
                } else {
                    mLikeAdapter.setEmptyHeight(0);
                }
                mCalculateLikeEmptyHeight = true;
                mLikeAdapter.notifyDataSetChanged();
            }

            if (mListView.getLastVisiblePosition() == (mListView.getCount() - 1)) {
                if (mCurrentList == 0 && mStateComment == STATE_NONE && mCommentAdapter != null
                        && (mCommentAdapter.getState() == ListBaseAdapter.STATE_LOAD_MORE
                        || mCommentAdapter.getState() == ListBaseAdapter.STATE_NETWORK_ERROR)
                        && mCommentAdapter.getDataSize() > 0) {
                    mStateComment = STATE_LOADMORE;
                    mCurrentPage++;
                    requestTweetCommentData(true);
                } else if (mCurrentList == 1 && mStateLike == STATE_NONE && mLikeAdapter != null
                        && (mLikeAdapter.getState() == ListBaseAdapter.STATE_LOAD_MORE
                        || mLikeAdapter.getState() == ListBaseAdapter.STATE_NETWORK_ERROR)
                        && mLikeAdapter.getDataSize() > 0) {
                    mStateLike = STATE_LOADMORE;
                    mCurrentLikePage++;
                    requestTweetLikeData(true);
                }
            }

            if (mStickyTabBar != null) {
                mStickyTabBar.setVisibility(firstVisibleItem >= 1 ? View.VISIBLE : View.GONE);
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Comment comment = data
                    .getParcelableExtra(Comment.BUNDLE_KEY_COMMENT);
            if (comment != null && mTweet != null) {
                // mAdapter.addItem(0, comment);
                // mTweet.setCommentCount(mTweet.getCommentCount() + 1);
                // mTvCommentCount.setText(getString(R.string.comment_count,
                // mTweet.getCommentCount()));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onCommentChanged(int opt, int id, int catalog, boolean isBlog,
                                  Comment comment) {
        if (Comment.OPT_ADD == opt && catalog == CommentList.CATALOG_TWEET
                && id == mTweetId
                && (mCommentAdapter.getState() != TweetCommentAdapter.STATE_TWEET_LOADING &&
                mCommentAdapter.getState() != TweetCommentAdapter.STATE_TWEET_ERROR)) {
            if (mTweet != null && mTvCommentCount != null) {
                mTweet.setCommentCount(mTweet.getCommentCount() + 1);
                fillUI();

                if (mCommentAdapter.getState() == TweetCommentAdapter.STATE_TWEET_EMPTY) {
                    mCommentAdapter.setState(TweetCommentAdapter.STATE_LESS_ONE_PAGE);
                }
                mCommentAdapter.addItem(0, comment);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TWEET_DETAIL_SCREEN);
        MobclickAgent.onResume(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TWEET_DETAIL_SCREEN);
        MobclickAgent.onPause(getActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        IntentFilter filter = new IntentFilter(
                Constants.INTENT_ACTION_COMMENT_CHANGED);
        mCommentReceiver = new CommentChangeReceiver();
        getActivity().registerReceiver(mCommentReceiver, filter);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        if (mCommentReceiver != null) {
            getActivity().unregisterReceiver(mCommentReceiver);
        }
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.v2_fragment_tweet_detail,
                container, false);
        mTweetId = getActivity().getIntent().getIntExtra("tweet_id", 0);
        mTweetContent = getActivity().getIntent().getStringExtra("tweet_content");

        initViews(view);

        // sendRequestData();
        requestTweetData(true);
        return view;
    }

    @SuppressLint("InflateParams")
    private void initViews(View view) {
        mEmptyView = (EmptyLayout) view.findViewById(R.id.error_layout);
        mListView = (ListView) view.findViewById(R.id.list_view);
        mStickyTabBar = (RadioGroup) view.findViewById(R.id.sticky_tab_bar);
        mRbCommentCount = (RadioButton) view.findViewById(R.id.rb_comment_count);
        mRbCommentCount.setOnClickListener(this);
        mRbLikeCount = (RadioButton) view.findViewById(R.id.rb_like_count);
        mRbLikeCount.setOnClickListener(this);
        mTvLikeOpt = (TextView)view.findViewById(R.id.tv_like_opt);
        mTvLikeOpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLikeClick();
            }
        });

        View header = LayoutInflater.from(getActivity()).inflate(
                R.layout.v2_list_header_tweet_detail, null);
        mIvAvatar = (ImageView) header.findViewById(R.id.iv_avatar);
        mIvAvatar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                UIHelper.showUserCenter(getActivity(), mTweet.getAuthorId(),
                        mTweet.getAuthor());
            }
        });

        mTvName = (TextView) header.findViewById(R.id.tv_name);
        mTvFrom = (TextView) header.findViewById(R.id.tv_from);
        mTvTime = (TextView) header.findViewById(R.id.tv_time);
        mTvCommentCount = (TextView) header.findViewById(R.id.tv_comment_count);
        mWVContent = (WebView) header.findViewById(R.id.webview);
        mIvPic = (ImageView) header.findViewById(R.id.iv_pic);
        mIvPic.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mTweet != null && !TextUtils.isEmpty(mTweet.getImgSmall())) {
                    UIHelper.showImagePreview(getActivity(),
                            new String[]{mTweet.getImgBig()});
                }
            }
        });
        mTvContent = (TweetTextView) header.findViewById(R.id.tv_content);

        initWebView(mWVContent);

        mListView.addHeaderView(header);
        mListView.setOnScrollListener(mScrollListener);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);

        mCommentAdapter = new TweetCommentAdapter(this);
        mLikeAdapter = new TweetLikeAdapter(this);

        mListView.setAdapter(mCommentAdapter);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initWebView(WebView webView) {
        WebSettings settings = webView.getSettings();
        settings.setDefaultFontSize(20);
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        int sysVersion = Build.VERSION.SDK_INT;
        if (sysVersion >= 11) {
            settings.setDisplayZoomControls(false);
        } else {
            ZoomButtonsController zbc = new ZoomButtonsController(webView);
            zbc.getZoomControls().setVisibility(View.GONE);
        }
        UIHelper.addWebImageShow(getActivity(), webView);
    }

    private void fillUI() {
        ImageLoader.getInstance().displayImage(mTweet.getFace(), mIvAvatar);
        mTvName.setText(mTweet.getAuthor());
        mTvTime.setText(StringUtils.friendly_time(mTweet.getPubDate()));
        switch (mTweet.getAppClient()) {
            default:
                mTvFrom.setText("");
                break;
            case Tweet.CLIENT_MOBILE:
                mTvFrom.setText(R.string.from_mobile);
                break;
            case Tweet.CLIENT_ANDROID:
                mTvFrom.setText(R.string.from_android);
                break;
            case Tweet.CLIENT_IPHONE:
                mTvFrom.setText(R.string.from_iphone);
                break;
            case Tweet.CLIENT_WINDOWS_PHONE:
                mTvFrom.setText(R.string.from_windows_phone);
                break;
            case Tweet.CLIENT_WECHAT:
                mTvFrom.setText(R.string.from_wechat);
                break;
        }

        mTvCommentCount.setText(getString(R.string.comment_count,
                mTweet.getCommentCount()));

        mRbCommentCount.setText("评论 (" + mTweet.getCommentCount() + ")");
        mRbLikeCount.setText("点赞 (" + mTweet.getLikeCount() + ")");
        mTvLikeOpt.setText(mTweet.getIsLike() == 1 ? "已赞" : "赞一下");
        mTvLikeOpt.setCompoundDrawablesWithIntrinsicBounds(mTweet.getIsLike() == 1 ?
                R.drawable.ic_like_selected : R.drawable.ic_like_normal, 0, 0, 0);

        mCommentAdapter.setTweet(mTweet);
        mCommentAdapter.notifyDataSetChanged();

        mLikeAdapter.setTweet(mTweet);
        mLikeAdapter.notifyDataSetChanged();
        // mTvCommentCount.setText(mTweet.getBody());

        // set content
        String body = UIHelper.WEB_STYLE + mTweet.getBody();
        body = body.replaceAll("(<img[^>]*?)\\s+width\\s*=\\s*\\S+", "$1");
        body = body.replaceAll("(<img[^>]*?)\\s+height\\s*=\\s*\\S+", "$1");


        mWVContent.loadDataWithBaseURL(null, body, "text/html", "utf-8", null);
        mWVContent.setWebViewClient(UIHelper.getWebViewClient());

        mTvContent.setMovementMethod(MyLinkMovementMethod.a());
        mTvContent.setFocusable(false);
        mTvContent.setDispatchToParent(true);
        mTvContent.setLongClickable(false);
        Spanned span = Html.fromHtml(mTweetContent);
        mTvContent.setText(span);
        MyURLSpan.parseLinkText(mTvContent, span);

        if (TextUtils.isEmpty(mTweet.getImgSmall())) {
            return;
        }
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true).cacheOnDisk(true)
                .postProcessor(new BitmapProcessor() {

                    @Override
                    public Bitmap process(Bitmap arg0) {
                        return arg0;
                    }
                }).build();
        mIvPic.setVisibility(View.VISIBLE);
        ImageLoader.getInstance().displayImage(mTweet.getImgSmall(), mIvPic,
                options);
    }

    private void sendRequestData() {
        mState = STATE_REFRESH;
        mEmptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
        NewsApi.getTweetDetail(mTweetId, mDetailHandler);
    }

    private void sendRequestCommentData() {
        NewsApi.getCommentList(mTweetId, CommentList.CATALOG_TWEET,
                mCurrentPage, mCommentHandler);
    }

    private void sendRequestLikeData() {
        TLog.log(TAG, "sendRequestLikeData");
        NewsApi.getTweetLikeList(mTweetId, mCurrentLikePage, mLikeHandler);
    }

    @Override
    public void setEmojiFragment(EmojiFragment fragment) {
        mEmojiFragment = fragment;
        mEmojiFragment.setEmojiTextListener(this);
    }

    @Override
    public void onSendClick(String text) {
        if (!TDevice.hasInternet()) {
            AppContext.showToastShort(R.string.tip_network_error);
            return;
        }
        if (!AppContext.instance().isLogin()) {
            UIHelper.showLogin(getActivity());
            mEmojiFragment.hideKeyboard();
            return;
        }
        if (TextUtils.isEmpty(text)) {
            AppContext.showToastShort(R.string.tip_comment_content_empty);
            mEmojiFragment.requestFocusInput();
            return;
        }
        PublicCommentTask task = new PublicCommentTask();
        task.setId(mTweetId);
        task.setCatalog(CommentList.CATALOG_TWEET);
        task.setIsPostToMyZone(0);
        task.setContent(text);
        task.setUid(AppContext.instance().getLoginUid());
        ServerTaskUtils.publicComment(getActivity(), task);
        mEmojiFragment.reset();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.rb_comment_count:
                onTabChanged(0);
                break;
            case R.id.rb_like_count:
                onTabChanged(1);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mCurrentList == 0) {
            final Comment comment = (Comment) mCommentAdapter.getItem(position - 1);
            if (comment == null)
                return;
            handleReplyComment(comment);
        } else {
            final User user = (User) mLikeAdapter.getItem(position - 1);
            if (user == null)
                return;
            UIHelper.showUserCenter(getActivity(), user.getUid(), user.getName());
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (mCurrentList != 0)
            return false;
        if (position == 0)
            return false;
        final Comment item = (Comment) mCommentAdapter.getItem(position - 1);
        if (item == null)
            return false;

        String[] items;
        if (AppContext.instance().isLogin()
                && AppContext.instance().getLoginUid() == item.getAuthorId()) {
            items = new String[]{getResources().getString(R.string.reply),
                    getResources().getString(R.string.copy),
                    getResources().getString(R.string.delete)};
        } else {
            items = new String[]{getResources().getString(R.string.reply),
                    getResources().getString(R.string.copy)};
        }
        AlertDialog dialog = new AlertDialog.Builder(getActivity(),
                R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle(R.string.operation)
                .setCancelable(true)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (which == 0) {
                            handleReplyComment(item);
                        } else if (which == 1) {
                            TDevice.copyTextToBoard(HTMLSpirit.delHTMLTag(item
                                    .getContent()));
                        } else if (which == 2) {
                            handleDeleteComment(item);
                        }
                    }
                }).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        return false;
    }

    @Override
    public void onMoreClick(final Comment comment) {
    }

    private void handleReplyComment(Comment comment) {
        if (!AppContext.instance().isLogin()) {
            UIHelper.showLogin(getActivity());
            return;
        }
        UIHelper.showReplyCommentForResult(this, REQUEST_CODE, false, mTweetId,
                CommentList.CATALOG_TWEET, comment);
    }

    private void handleDeleteComment(Comment comment) {
        if (!AppContext.instance().isLogin()) {
            UIHelper.showLogin(getActivity());
            return;
        }
        AppContext.showToastShort(R.string.deleting);
        NewsApi.deleteComment(mTweetId, CommentList.CATALOG_TWEET,
                comment.getId(), comment.getAuthorId(),
                new DeleteOperationResponseHandler(comment));
    }

    class DeleteOperationResponseHandler extends OperationResponseHandler {

        DeleteOperationResponseHandler(Object... args) {
            super(args);
        }

        @Override
        public void onSuccess(int code, ByteArrayInputStream is, Object[] args) {
            try {
                Result res = Result.parse(is);
                if (res.OK()) {
                    AppContext.showToastShort(R.string.delete_success);
                    UIHelper.sendNoticeBroadcast(getActivity(), res);

                    mTweet.setCommentCount(mTweet.getCommentCount() - 1);

                    fillUI();

                    if (mCommentAdapter.getDataSize() == 1) {
                        mCommentAdapter.setState(TweetCommentAdapter.STATE_TWEET_EMPTY);
                    }
                    mCommentAdapter.removeItem(args[0]);
                } else {
                    AppContext.showToastShort(res.getErrorMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
                onFailure(code, e.getMessage(), args);
            }
        }

        @Override
        public void onFailure(int code, String errorMessage, Object[] args) {
            AppContext.showToastShort(R.string.delete_faile);
        }
    }

    protected void requestTweetData(boolean refresh) {
        String key = getCacheKey();
        if (TDevice.hasInternet()
                && (!CacheManager.isReadDataCache(getActivity(), key) || refresh)) {
            sendRequestData();
        } else {
            readCacheData(key);
        }
    }

    private String getCacheKey() {
        return CACHE_KEY_PREFIX + mTweetId;
    }

    private void readCacheData(String cacheKey) {
        new CacheTask(getActivity()).execute(cacheKey);
    }

    private class CacheTask extends AsyncTask<String, Void, Tweet> {
        private WeakReference<Context> mContext;

        private CacheTask(Context context) {
            mContext = new WeakReference<Context>(context);
        }

        @Override
        protected Tweet doInBackground(String... params) {
            if (mContext.get() != null) {
                Serializable seri = CacheManager.readObject(mContext.get(),
                        params[0]);
                if (seri == null) {
                    return null;
                } else {
                    return (Tweet) seri;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Tweet tweet) {
            super.onPostExecute(tweet);
            if (tweet != null) {
                executeOnLoadDataSuccess(tweet);
                mState = STATE_NONE;
            } else {
                executeOnLoadDataError(null);
                mState = STATE_NONE;
            }
        }
    }

    private class SaveCacheTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<Context> mContext;
        private Serializable seri;
        private String key;

        private SaveCacheTask(Context context, Serializable seri, String key) {
            mContext = new WeakReference<>(context);
            this.seri = seri;
            this.key = key;
        }

        @Override
        protected Void doInBackground(Void... params) {
            CacheManager.saveObject(mContext.get(), seri, key);
            return null;
        }
    }

    private AsyncHttpResponseHandler mDetailHandler = new AsyncHttpResponseHandler() {

        @Override
        public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
            try {
                mTweet = XmlUtils.toBean(TweetDetail.class, arg2).getTweet();
                //Tweet.parse(new ByteArrayInputStream(arg2));
                if (mTweet != null && mTweet.getId() > 0) {
                    UIHelper.sendNoticeBroadcast(getActivity(), mTweet);
                    executeOnLoadDataSuccess(mTweet);
                    mState = STATE_NONE;
                    mEmptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
                    new SaveCacheTask(getActivity(), mTweet, getCacheKey())
                            .execute();
                } else {
                    throw new RuntimeException("load detail error");
                }
            } catch (Exception e) {
                e.printStackTrace();
                onFailure(arg0, arg1, arg2, e);
            }
        }

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                              Throwable arg3) {
            readCacheData(getCacheKey());
        }
    };

    private void executeOnLoadDataSuccess(Tweet tweet) {
        mTweet = tweet;
        if (mTweet != null && mTweet.getId() > 0) {
            fillUI();
            refreshCommentList();
            refreshLikeList();
        } else {
            throw new RuntimeException("load detail error");
        }
    }

    private void executeOnLoadDataError(Object object) {
        mEmptyView.setErrorType(EmptyLayout.NETWORK_ERROR);
    }

    private void refreshCommentList() {
        if (mStateComment == STATE_NONE) {
            mStateComment = STATE_REFRESH;
            mCurrentPage = 0;

            mCommentAdapter.setState(TweetCommentAdapter.STATE_TWEET_LOADING);
            mCommentAdapter.notifyDataSetChanged();
            requestTweetCommentData(true);
        }
    }

    private void refreshLikeList() {
        if (mStateLike == STATE_NONE) {
            mStateLike = STATE_REFRESH;
            mCurrentLikePage = 0;

            mLikeAdapter.setState(TweetLikeAdapter.STATE_TWEET_LOADING);
            mLikeAdapter.notifyDataSetChanged();
            requestTweetLikeData(true);
        }
    }

    protected void requestTweetCommentData(boolean refresh) {
        String key = getCacheCommentKey();
        if (TDevice.hasInternet()
                && (!CacheManager.isReadDataCache(getActivity(), key) || refresh)) {
            sendRequestCommentData();
        } else {
            readCacheCommentData(key);
        }
    }

    protected void requestTweetLikeData(boolean refresh) {
        String key = getCacheLikeKey();
        if (TDevice.hasInternet()
                && (!CacheManager.isReadDataCache(getActivity(), key) || refresh)) {
            sendRequestLikeData();
        } else {
            readCacheLikeData(key);
        }
    }

    private String getCacheCommentKey() {
        return CACHE_KEY_TWEET_COMMENT + mTweetId + "_" + mCurrentPage;
    }

    private String getCacheLikeKey() {
        return CACHE_KEY_TWEET_LIKE + mTweetId + "_" + mCurrentPage;
    }

    private void readCacheCommentData(String cacheKey) {
        TLog.log(TAG, "readCacheCommentData :" + cacheKey);
        new CacheCommentTask(getActivity()).execute(cacheKey);
    }

    private void readCacheLikeData(String cacheKey) {
        new CacheLikeTask(getActivity()).execute(cacheKey);
    }

    private class CacheCommentTask extends AsyncTask<String, Void, CommentList> {
        private WeakReference<Context> mContext;

        private CacheCommentTask(Context context) {
            mContext = new WeakReference<>(context);
        }

        @Override
        protected CommentList doInBackground(String... params) {
            if (mContext.get() != null) {
                Serializable seri = CacheManager.readObject(mContext.get(),
                        params[0]);
                if (seri == null) {
                    return null;
                } else {
                    return (CommentList) seri;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(CommentList list) {
            super.onPostExecute(list);
            if (list != null) {
                executeOnLoadCommentDataSuccess(list);
            } else {
                executeOnLoadCommentDataError(null);
            }
            executeOnLoadCommentFinish();
        }
    }

    private AsyncHttpResponseHandler mCommentHandler = new AsyncHttpResponseHandler() {

        @Override
        public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
            try {
                CommentList list = CommentList.parse(new ByteArrayInputStream(
                        arg2));
                UIHelper.sendNoticeBroadcast(getActivity(), list);
                executeOnLoadCommentDataSuccess(list);
                executeOnLoadCommentFinish();
                new SaveCacheTask(getActivity(), list, getCacheCommentKey()).execute();
            } catch (Exception e) {
                e.printStackTrace();
                onFailure(arg0, arg1, arg2, e);
            }
        }

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                              Throwable arg3) {
            executeOnLoadCommentDataError(arg3.getMessage());
            executeOnLoadCommentFinish();
        }
    };

    private void executeOnLoadCommentDataSuccess(CommentList list) {
        TLog.log(TAG, "executeOnLoadCommentDataSuccess state :" + mStateComment);
        if (mStateComment == STATE_REFRESH)
            mCommentAdapter.clear();
        List<Comment> data = list.getCommentlist();
        mCommentAdapter.addData(data);
        if (data.size() == 0 && (mStateComment == STATE_REFRESH || mCurrentPage == 0)) {
            TLog.log(TAG, "empty comment");
            mCommentAdapter.setState(TweetCommentAdapter.STATE_TWEET_EMPTY);
        } else if (data.size() < TDevice.getPageSize()) {
            if (mStateComment == STATE_REFRESH || mCurrentPage == 0) {
                TLog.log(TAG, "comment less one page");
                mCommentAdapter.setState(ListBaseAdapter.STATE_LESS_ONE_PAGE);
            } else {
                TLog.log(TAG, "comment no more");
                mCommentAdapter.setState(ListBaseAdapter.STATE_NO_MORE);
            }
        } else {
            mCommentAdapter.setState(ListBaseAdapter.STATE_LOAD_MORE);
        }
    }

    private void executeOnLoadCommentFinish() {
        mStateComment = STATE_NONE;
        mCommentAdapter.notifyDataSetChanged();
    }

    private void executeOnLoadCommentDataError(Object object) {
        if (mCurrentPage == 0) {
            TLog.log(TAG, "comment error page 1");
            mCommentAdapter.setState(TweetCommentAdapter.STATE_TWEET_ERROR);
        } else {
            TLog.log(TAG, "comment error page !=1");
            mCommentAdapter.setState(ListBaseAdapter.STATE_NETWORK_ERROR);
        }
    }

    private class CacheLikeTask extends AsyncTask<String, Void, TweetLikeUserList> {
        private WeakReference<Context> mContext;

        private CacheLikeTask(Context context) {
            mContext = new WeakReference<Context>(context);
        }

        @Override
        protected TweetLikeUserList doInBackground(String... params) {
            if (mContext.get() != null) {
                Serializable seri = CacheManager.readObject(mContext.get(),
                        params[0]);
                if (seri == null) {
                    return null;
                } else {
                    return (TweetLikeUserList) seri;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(TweetLikeUserList list) {
            super.onPostExecute(list);
            if (list != null) {
                executeOnLoadLikeDataSuccess(list);
            } else {
                executeOnLoadLikeDataError(null);
            }
            executeOnLoadLikeFinish();
        }
    }

    private AsyncHttpResponseHandler mLikeHandler = new AsyncHttpResponseHandler() {

        @Override
        public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
            try {
                TweetLikeUserList list = XmlUtils.toBean(TweetLikeUserList.class, arg2);
                UIHelper.sendNoticeBroadcast(getActivity(), list);
                executeOnLoadLikeDataSuccess(list);
                executeOnLoadLikeFinish();
                new SaveCacheTask(getActivity(), list, getCacheLikeKey()).execute();
            } catch (Exception e) {
                e.printStackTrace();
                onFailure(arg0, arg1, arg2, e);
            }
        }

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                              Throwable arg3) {
            executeOnLoadLikeDataError(arg3.getMessage());
            executeOnLoadLikeFinish();
        }
    };

    private void executeOnLoadLikeDataSuccess(TweetLikeUserList list) {
        if (mStateLike == STATE_REFRESH)
            mLikeAdapter.clear();
        List<User> data = list.getList();
        mLikeAdapter.addData(data);
        if (data.size() == 0 && (mStateLike == STATE_REFRESH || mCurrentLikePage == 0)) {
            mLikeAdapter.setState(TweetLikeAdapter.STATE_TWEET_EMPTY);
        } else if (data.size() < TDevice.getPageSize()) {
            if (mStateLike == STATE_REFRESH || mCurrentLikePage == 0)
                mLikeAdapter.setState(ListBaseAdapter.STATE_LESS_ONE_PAGE);
            else
                mLikeAdapter.setState(ListBaseAdapter.STATE_NO_MORE);
        } else {
            mLikeAdapter.setState(ListBaseAdapter.STATE_LOAD_MORE);
        }
    }

    private void executeOnLoadLikeDataError(Object object) {
        if (mCurrentLikePage == 0) {
            mLikeAdapter.setState(TweetLikeAdapter.STATE_TWEET_ERROR);
        } else {
            mLikeAdapter.setState(ListBaseAdapter.STATE_NETWORK_ERROR);
        }
    }

    private void executeOnLoadLikeFinish() {
        mStateLike = STATE_NONE;
        mLikeAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTabChanged(int idx) {
        switch (idx) {
            case 0:
                if (mCurrentList != 0) {
                    mCurrentList = 0;
                    mStickyTabBar.check(R.id.rb_comment_count);
                    boolean needStick = false;
                    if (mStickyTabBar.getVisibility() == View.VISIBLE)
                        needStick = true;
                    mListView.setAdapter(mCommentAdapter);
                    mCommentAdapter.notifyDataSetChanged();
                    if (needStick)
                        mListView.setSelection(1);
                }
                break;
            case 1:
                if (mCurrentList != 1) {
                    mCurrentList = 1;
                    mStickyTabBar.check(R.id.rb_like_count);
                    boolean needStick = false;
                    if (mStickyTabBar.getVisibility() == View.VISIBLE)
                        needStick = true;
                    mListView.setAdapter(mLikeAdapter);
                    mCommentAdapter.notifyDataSetChanged();
                    if (needStick)
                        mListView.setSelection(1);
                }
                break;
        }
    }

    @Override
    public void onCommentClick() {
        mEmojiFragment.requestFocusInput();
    }

    @Override
    public void onLikeClick() {
        if (!AppContext.instance().isLogin()) {
            UIHelper.showLogin(getActivity());
            return;
        }
        if (mTweet != null) {
            int uid = AppContext.getLoginUid();
            if (mTweet.getIsLike() == 1) {
                NewsApi.pubUnLikeTweet(uid, mTweet.getId(), mTweet.getAuthorId(), new UnLikeTweetResponseHandler(mTweet));
            } else {
                NewsApi.pubLikeTweet(uid, mTweet.getId(), mTweet.getAuthorId(), new LikeTweetResponseHandler(mTweet));
            }
        }
    }

    @Override
    public void onRefreshData(int idx) {
        switch (idx) {
            case 0:
                refreshCommentList();
                break;
            case 1:
                refreshLikeList();
                break;
        }
    }

    class UnLikeTweetResponseHandler extends OperationResponseHandler {

        UnLikeTweetResponseHandler(Object... args) {
            super(args);
        }

        @Override
        public void onSuccess(int code, ByteArrayInputStream is, Object[] args)
                throws Exception {
            try {
                Result res = Result.parse(is);
                if (res != null && res.OK()) {
                    AppContext.showToastShort("已取消赞");
                    Tweet tweet = (Tweet) args[0];
                    tweet.setIsLike(0);
                    tweet.setLikeCount(tweet.getLikeCount() - 1);

                    fillUI();

                    if (mLikeAdapter != null
                            && mLikeAdapter.getState() != TweetLikeAdapter.STATE_TWEET_ERROR &&
                            mLikeAdapter.getState() != TweetLikeAdapter.STATE_TWEET_LOADING) {
                        User cu = AppContext.getLoginInfo();
                        if (mLikeAdapter.getDataSize() == 1) {
                            User user = (User) mLikeAdapter.getItem(0);
                            if (user.getUid() == cu.getUid()) {
                                mLikeAdapter.setState(TweetLikeAdapter.STATE_TWEET_EMPTY);
                            }
                        }
                        mLikeAdapter.removeItem(cu);
                    }
                } else {
                    onFailure(code, res.getErrorMessage(), args);
                }
            } catch (Exception e) {
                e.printStackTrace();
                onFailure(code, e.getMessage(), args);
            }
        }

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                              Throwable arg3) {
            AppContext.showToastShort("取消赞失败了");
        }
    }

    class LikeTweetResponseHandler extends OperationResponseHandler {

        LikeTweetResponseHandler(Object... args) {
            super(args);
        }

        @Override
        public void onSuccess(int code, ByteArrayInputStream is, Object[] args)
                throws Exception {
            try {
                Result res = Result.parse(is);
                if (res != null && res.OK()) {
                    AppContext.showToastShort("赞成功了");
                    Tweet tweet = (Tweet) args[0];
                    tweet.setIsLike(1);
                    tweet.setLikeCount(tweet.getLikeCount() + 1);

                    fillUI();

                    if (mLikeAdapter != null
                            && mLikeAdapter.getState() != TweetLikeAdapter.STATE_TWEET_ERROR &&
                            mLikeAdapter.getState() != TweetLikeAdapter.STATE_TWEET_LOADING) {
                        if (mLikeAdapter.getState() == TweetLikeAdapter.STATE_TWEET_EMPTY) {
                            mLikeAdapter.setState(TweetLikeAdapter.STATE_LESS_ONE_PAGE);
                        }
                        User cu = AppContext.getLoginInfo();
                        mLikeAdapter.addItem(0, cu);
                    }
                } else {
                    onFailure(code, res.getErrorMessage(), args);
                }
            } catch (Exception e) {
                e.printStackTrace();
                onFailure(code, e.getMessage(), args);
            }
        }

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                              Throwable arg3) {
            AppContext.showToastShort("赞失败了");
        }
    }
}
