package net.oschina.app.v2.activity.event.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.tonlin.osc.happy.R;
import com.umeng.analytics.MobclickAgent;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.event.view.EventApplyDialog;
import net.oschina.app.v2.activity.news.ToolbarEmojiVisiableControl;
import net.oschina.app.v2.activity.news.fragment.BaseDetailFragment;
import net.oschina.app.v2.activity.news.fragment.EmojiFragmentControl;
import net.oschina.app.v2.activity.news.fragment.ToolbarFragment;
import net.oschina.app.v2.activity.news.fragment.ToolbarFragment.OnActionClickListener;
import net.oschina.app.v2.activity.news.fragment.ToolbarFragment.ToolAction;
import net.oschina.app.v2.activity.news.fragment.ToolbarFragmentControl;
import net.oschina.app.v2.api.remote.EventApi;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.emoji.EmojiFragment;
import net.oschina.app.v2.emoji.EmojiFragment.EmojiTextListener;
import net.oschina.app.v2.model.Comment;
import net.oschina.app.v2.model.CommentList;
import net.oschina.app.v2.model.Entity;
import net.oschina.app.v2.model.Event;
import net.oschina.app.v2.model.EventApplyData;
import net.oschina.app.v2.model.FavoriteList;
import net.oschina.app.v2.model.Post;
import net.oschina.app.v2.model.Result;
import net.oschina.app.v2.model.ResultBean;
import net.oschina.app.v2.service.PublicCommentTask;
import net.oschina.app.v2.service.ServerTaskUtils;
import net.oschina.app.v2.ui.empty.EmptyLayout;
import net.oschina.app.v2.ui.widget.WebView;
import net.oschina.app.v2.utils.TDevice;
import net.oschina.app.v2.utils.UIHelper;
import net.oschina.app.v2.utils.XmlUtils;

import org.apache.http.Header;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


public class EventDetailFragment extends BaseDetailFragment implements
        EmojiTextListener, EmojiFragmentControl, ToolbarFragmentControl {

    protected static final String TAG = EventDetailFragment.class
            .getSimpleName();
    private static final String EVENT_CACHE_KEY = "event_";
    private static final String BLOG_DETAIL_SCREEN = "event_detail_screen";
    private TextView mTvTitle, mTvStartTime, mTvEndTime, mTvSpot, mTvEventTip;
    private View mLocation, mBtAttend;
    private Button mBtEventApply;
    private int mEventId;
    private Post mEvent;
    private EmojiFragment mEmojiFragment;
    private ToolbarFragment mToolBarFragment;
    private EventApplyDialog mEventApplyDialog;

    private OnClickListener mMoreListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Activity act = getActivity();
            if (act != null && act instanceof ToolbarEmojiVisiableControl) {
                ((ToolbarEmojiVisiableControl) act).toggleToolbarEmoji();
            }
        }
    };

    private OnActionClickListener mActionListener = new OnActionClickListener() {

        @Override
        public void onActionClick(ToolAction action) {
            switch (action) {
                case ACTION_CHANGE:
                    Activity act = getActivity();
                    if (act != null && act instanceof ToolbarEmojiVisiableControl) {
                        ((ToolbarEmojiVisiableControl) act).toggleToolbarEmoji();
                    }
                    break;
                case ACTION_WRITE_COMMENT:
                    act = getActivity();
                    if (act != null && act instanceof ToolbarEmojiVisiableControl) {
                        ((ToolbarEmojiVisiableControl) act).toggleToolbarEmoji();
                    }
                    mEmojiFragment.showKeyboardIfNoEmojiGrid();
                    break;
                case ACTION_VIEW_COMMENT:
                    if (mEvent != null)
                        UIHelper.showComment(getActivity(), mEvent.getId(),
                                CommentList.CATALOG_POST);
                    break;
                case ACTION_FAVORITE:
                    handleFavoriteOrNot();
                    break;
                case ACTION_SHARE:
                    handleShare();
                    break;
                case ACTION_REPORT:
                    onReportMenuClick();
                    break;
                default:
                    break;
            }
        }
    };

    private final AsyncHttpResponseHandler mApplyHandler = new AsyncHttpResponseHandler() {

        @Override
        public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
            Result rs = XmlUtils.toBean(ResultBean.class,
                    new ByteArrayInputStream(arg2)).getResult();
            if (rs.OK()) {
                AppContext.showToast("报名成功");
                mEventApplyDialog.dismiss();
                mEvent.getEvent().setApplyStatus(Event.APPLYSTATUS_CHECKING);

                fillUI();
            } else {
                AppContext.showToast(rs.getErrorMessage());
            }
        }

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                              Throwable arg3) {
            AppContext.showToast("报名失败");
        }

        @Override
        public void onFinish() {
            hideWaitDialog();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.v2_fragment_event_detail,
                container, false);

        mEventId = getActivity().getIntent().getIntExtra("id", 0);

        initViews(view);

        return view;
    }

    private void initViews(View view) {
        mEmptyLayout = (EmptyLayout) view.findViewById(R.id.error_layout);

        mTvTitle = (TextView) view.findViewById(R.id.tv_event_title);
        mTvStartTime = (TextView) view.findViewById(R.id.tv_event_start_time);
        mTvEndTime = (TextView) view.findViewById(R.id.tv_event_end_time);
        mTvSpot = (TextView) view.findViewById(R.id.tv_event_spot);
        mTvEventTip = (TextView) view.findViewById(R.id.tv_event_tip);

        mLocation = view.findViewById(R.id.rl_event_location);
        mBtAttend = view.findViewById(R.id.bt_event_attend);
        mBtEventApply = (Button) view.findViewById(R.id.bt_event_apply);

        mLocation.setOnClickListener(this);
        mBtAttend.setOnClickListener(this);
        mBtEventApply.setOnClickListener(this);

        mWebView = (WebView) view.findViewById(R.id.webview);
        initWebView(mWebView);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        final int id = v.getId();
        switch (id) {
            case R.id.rl_event_location:
                //UIHelper.showEventLocation(getActivity(), mDetail.getEvent()
                //        .getCity(), mDetail.getEvent().getSpot());
                break;
            case R.id.bt_event_attend:
                UIHelper.showEventApplies(getActivity(), mEventId);
                break;
            case R.id.bt_event_apply:
                showEventApply();
                break;
            default:
                break;
        }
    }

    private void showEventApply() {
        if (mEvent.getEvent().getCategory() == 4) {
            UIHelper.openSysBrowser(getActivity(), mEvent.getEvent().getUrl());
            return;
        }

        if (!AppContext.instance().isLogin()) {
            UIHelper.showLogin(getActivity());
            return;
        }
        if (mEventApplyDialog == null) {
            mEventApplyDialog = new EventApplyDialog(getActivity());
            mEventApplyDialog.setCanceledOnTouchOutside(true);
            mEventApplyDialog.setCancelable(true);
            mEventApplyDialog.setTitle("活动报名");
            mEventApplyDialog.setCanceledOnTouchOutside(true);
            mEventApplyDialog.setNegativeButton(R.string.cancel, null);
            mEventApplyDialog.setPositiveButton(R.string.ok,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface d, int which) {
                            EventApplyData data = null;
                            if ((data = mEventApplyDialog.getApplyData()) != null) {
                                data.setEvent(mEventId);
                                data.setUser(AppContext.instance()
                                        .getLoginUid());
                                showWaitDialog(R.string.progress_submit);
                                EventApi.eventApply(data, mApplyHandler);
                            }
                        }
                    });
        }

        mEventApplyDialog.show();
    }


    @Override
    protected String getCacheKey() {
        return new StringBuilder(EVENT_CACHE_KEY).append(mEventId).toString();
    }

    @Override
    protected void sendRequestData() {
        NewsApi.getPostDetail(mEventId, mHandler);
    }

    @Override
    protected Entity parseData(InputStream is) throws Exception {
        return Post.parse(is);
    }

    @Override
    protected void onCommentChanged(int opt, int id, int catalog,
                                    boolean isBlog, Comment comment) {
        if (id == mEventId && !isBlog) {
            if (Comment.OPT_ADD == opt && mEvent != null) {
                mEvent.setAnswerCount(mEvent.getAnswerCount() + 1);
                if (mToolBarFragment != null) {
                    mToolBarFragment.setCommentCount(mEvent.getAnswerCount());
                }
            }
        }
    }

    @Override
    protected void executeOnLoadDataSuccess(Entity entity) {
        mEvent = (Post) entity;
        fillUI();
        fillWebViewBody();
    }

    private void fillUI() {
        mTvTitle.setText(mEvent.getTitle());
        mTvStartTime.setText(String.format(
                getString(R.string.event_start_time), mEvent.getEvent()
                        .getStartTime()));
        mTvEndTime.setText(String.format(getString(R.string.event_end_time),
                mEvent.getEvent().getEndTime()));
        mTvSpot.setText(mEvent.getEvent().getCity() + " "
                + mEvent.getEvent().getSpot());

        // 站外活动
        if (mEvent.getEvent().getCategory() == 4) {
            mBtEventApply.setVisibility(View.VISIBLE);
            mBtAttend.setVisibility(View.GONE);
            mBtEventApply.setText("报名链接");
        } else {
            notifyEventStatus();
        }

        if (mToolBarFragment != null) {
            mToolBarFragment.setCommentCount(mEvent.getAnswerCount());
        }
        notifyFavorite(mEvent.getFavorite() == 1);
    }

    private void fillWebViewBody() {
//		StringBuffer body = new StringBuffer();
//		body.append(UIHelper.WEB_STYLE).append(UIHelper.WEB_LOAD_IMAGES);
//		body.append(ThemeSwitchUtils.getWebViewBodyString());
//		// 添加title
//		body.append(String.format("<div class='title'>%s</div>", mDetail.getTitle()));
//		// 添加作者和时间
//		String time = StringUtils.friendly_time(mDetail.getPubDate());
//		String author = String.format("<a class='author' href='http://my.oschina.net/u/%s'>%s</a>", mDetail.getAuthorId(), mDetail.getAuthor());
//		body.append(String.format("<div class='authortime'>%s&nbsp;&nbsp;&nbsp;&nbsp;%s</div>", author, time));
//		// 添加图片点击放大支持
//		body.append(UIHelper.setHtmlCotentSupportImagePreview(mDetail.getBody()));
//		// 封尾
//		body.append("</div></body>");

        String body = mEvent.getBody();
        body = UIHelper.clearFontSize(body);

        body = UIHelper.appendStyle(body);

        body = UIHelper.setHtmlCotentSupportImagePreview(body);
        body += UIHelper.WEB_LOAD_IMAGES;

        //TLog.log(TAG,"HTML:"+body);
        mWebView.setWebViewClient(mWebClient);
        mWebView.loadDataWithBaseURL(null, body, "text/html", "utf-8", null);
    }

    // 显示活动 以及报名的状态
    private void notifyEventStatus() {
        int eventStatus = mEvent.getEvent().getStatus();
        int applyStatus = mEvent.getEvent().getApplyStatus();

        if (applyStatus == Event.APPLYSTATUS_ATTEND) {
            mBtAttend.setVisibility(View.VISIBLE);
        } else {
            mBtAttend.setVisibility(View.GONE);
        }

        if (eventStatus == Event.EVNET_STATUS_APPLYING) {
            mBtEventApply.setVisibility(View.VISIBLE);
            mBtEventApply.setEnabled(false);
            switch (applyStatus) {
                case Event.APPLYSTATUS_CHECKING:
                    mBtEventApply.setText("待确认");
                    break;
                case Event.APPLYSTATUS_CHECKED:
                    mBtEventApply.setText("已确认");
                    mBtEventApply.setVisibility(View.GONE);
                    mTvEventTip.setVisibility(View.VISIBLE);
                    break;
                case Event.APPLYSTATUS_ATTEND:
                    mBtEventApply.setText("已出席");
                    break;
                case Event.APPLYSTATUS_CANCLE:
                    mBtEventApply.setText("已取消");
                    mBtEventApply.setEnabled(true);
                    break;
                case Event.APPLYSTATUS_REJECT:
                    mBtEventApply.setText("已拒绝");
                    break;
                default:
                    mBtEventApply.setText("我要报名");
                    mBtEventApply.setEnabled(true);
                    break;
            }
        } else {
            mBtEventApply.setVisibility(View.GONE);
        }
    }

    @Override
    public void setEmojiFragment(EmojiFragment fragment) {
        mEmojiFragment = fragment;
        mEmojiFragment.setEmojiTextListener(this);
        mEmojiFragment.setButtonMoreVisibility(View.VISIBLE);
        mEmojiFragment.setButtonMoreClickListener(mMoreListener);
    }

    @Override
    public void setToolBarFragment(ToolbarFragment fragment) {
        mToolBarFragment = fragment;
        mToolBarFragment.setOnActionClickListener(mActionListener);
        mToolBarFragment.setActionVisiable(ToolAction.ACTION_CHANGE, true);
        mToolBarFragment.setActionVisiable(ToolAction.ACTION_FAVORITE, true);
        mToolBarFragment.setActionVisiable(ToolAction.ACTION_WRITE_COMMENT,
                true);
        mToolBarFragment
                .setActionVisiable(ToolAction.ACTION_VIEW_COMMENT, true);
        mToolBarFragment.setActionVisiable(ToolAction.ACTION_SHARE, true);
        mToolBarFragment.setActionVisiable(ToolAction.ACTION_REPORT, false);
    }

    @Override
    public void onSendClick(String text) {
        if (!TDevice.hasInternet()) {
            AppContext.showToastShort(R.string.tip_network_error);
            return;
        }
        if (!AppContext.instance().isLogin()) {
            UIHelper.showLogin(getActivity());
            return;
        }
        if (TextUtils.isEmpty(text)) {
            AppContext.showToastShort(R.string.tip_comment_content_empty);
            mEmojiFragment.requestFocusInput();
            return;
        }
        PublicCommentTask task = new PublicCommentTask();
        task.setId(mEventId);
        task.setCatalog(CommentList.CATALOG_POST);
        task.setContent(text);
        task.setUid(AppContext.instance().getLoginUid());
        ServerTaskUtils.publicComment(getActivity(), task);
        mEmojiFragment.reset();
    }

    @Override
    protected void onFavoriteChanged(boolean flag) {
        super.onFavoriteChanged(flag);
        if (mToolBarFragment != null) {
            mToolBarFragment.setFavorite(flag);
        }
    }

    @Override
    protected int getFavoriteTargetId() {
        return mEvent != null ? mEvent.getId() : -1;
    }

    @Override
    protected int getFavoriteTargetType() {
        return mEvent != null ? FavoriteList.TYPE_POST : -1;
    }

    @Override
    protected String getShareContent() {
        return mEvent != null ? mEvent.getTitle() : null;
    }

    @Override
    protected String getShareUrl() {
        return mEvent != null ? mEvent.getUrl() : null;
    }

    @Override
    protected void onReportMenuClick() {
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(BLOG_DETAIL_SCREEN);
        MobclickAgent.onResume(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(BLOG_DETAIL_SCREEN);
        MobclickAgent.onPause(getActivity());
    }
}
