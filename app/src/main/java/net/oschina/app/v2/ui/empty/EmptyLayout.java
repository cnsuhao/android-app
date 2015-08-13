package net.oschina.app.v2.ui.empty;

import net.oschina.app.v2.utils.SimpleAnminationListener;
import net.oschina.app.v2.utils.TDevice;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tonlin.osc.happy.R;

public class EmptyLayout extends LinearLayout implements
        View.OnClickListener {// , ISkinUIObserver {

    public static final int HIDE_LAYOUT = 4;
    public static final int NETWORK_ERROR = 1;
    public static final int NETWORK_LOADING = 2;
    public static final int NODATA = 3;
    public static final int NODATA_ENABLE_CLICK = 5;
    private boolean clickEnable = true;
    private Context context;
    private OnClickListener listener;
    private int mErrorState;
    private String strNoDataContent = "";

    private View mRlLoadingContainer;
    private ImageView mIvLoadingOuter, mIvLoadingInner,mIvIcon;
    private Animation mAnimLoadingOuter, mAnimLoadingInner, mAnimHideOuter, mAnimHideInner;
    private Animation mAnimShowIcon, mAnimShowTitle, mAnimShowMessage;
    private View mRlStateContainer, mRlIcon;
    private TextView mTvEmptyTitle, mTvEmptyMessage;

    public EmptyLayout(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public EmptyLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        View.inflate(context, R.layout.v2_view_error_layout, this);

        setBackgroundColor(-1);
        setOnClickListener(this);

        mRlLoadingContainer = findViewById(R.id.rl_loading_container);
        mIvLoadingOuter = (ImageView) findViewById(R.id.iv_loading_outer);
        mIvLoadingInner = (ImageView) findViewById(R.id.iv_loading_inner);

        mAnimLoadingOuter = AnimationUtils.loadAnimation(context, R.anim.anim_loading_outer);

        mAnimLoadingInner = AnimationUtils.loadAnimation(context, R.anim.anim_loading_inner);

        mAnimHideInner = AnimationUtils.loadAnimation(context, R.anim.anim_loading_hide_inner);
        mAnimHideOuter = AnimationUtils.loadAnimation(context, R.anim.anim_loading_hide_outer);
        mAnimHideOuter.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mIvIcon = (ImageView)findViewById(R.id.iv_icon);

        mRlStateContainer = findViewById(R.id.rl_state_container);
        mRlIcon = findViewById(R.id.rl_icon);
        mTvEmptyTitle = (TextView) findViewById(R.id.tv_empty_title);
        mTvEmptyMessage = (TextView) findViewById(R.id.tv_empty_message);

        mAnimShowIcon = AnimationUtils.loadAnimation(context, R.anim.anim_empty_icon_show);
        mAnimShowIcon.setAnimationListener(new SimpleAnminationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {

            }
        });
        mAnimShowTitle = AnimationUtils.loadAnimation(context, R.anim.anim_empty_title_show);
        mAnimShowMessage = AnimationUtils.loadAnimation(context, R.anim.anim_empty_message_show);

        setVisibility(View.GONE);
    }

    public void dismiss() {
        mErrorState = HIDE_LAYOUT;
        setVisibility(View.GONE);
    }

    public int getErrorState() {
        return mErrorState;
    }

    @Override
    public void onClick(View v) {
        if (clickEnable) {
            if (listener != null)
                listener.onClick(v);
        }
    }

    public void setErrorType(int i) {
        setVisibility(View.VISIBLE);
        switch (i) {
            case NETWORK_ERROR:
                mErrorState = NETWORK_ERROR;
                clickEnable = true;

                if (TDevice.hasInternet()) {
                    //mTvEmptyTitle.setText();
                    mIvIcon.setImageResource(R.drawable.ic_empty_view_error);
                    mTvEmptyMessage.setText(R.string.error_view_load_error_click_to_refresh);
                    showState();
                } else {
                    //mTvEmptyTitle.setText();
                    mIvIcon.setImageResource(R.drawable.ic_empty_view_network);
                    mTvEmptyMessage.setText(R.string.error_view_network_error_click_to_refresh);
                    showState();
                }
                break;
            case NETWORK_LOADING:
                mErrorState = NETWORK_LOADING;
                clickEnable = false;

                setVisibility(View.VISIBLE);
                hideState();

                mRlLoadingContainer.setVisibility(View.VISIBLE);

                mIvLoadingOuter.clearAnimation();
                mIvLoadingOuter.startAnimation(mAnimLoadingOuter);

                mIvLoadingInner.clearAnimation();
                mIvLoadingInner.startAnimation(mAnimLoadingInner);
                break;
            case NODATA:
                mErrorState = NODATA;
                clickEnable = false;

                mIvIcon.setImageResource(R.drawable.ic_empty_view_nodata);
                setTvNoDataContent();
                showState();
                break;
            case HIDE_LAYOUT:
                //hide();
                setVisibility(View.GONE);
                break;
            case NODATA_ENABLE_CLICK:
                mErrorState = NODATA_ENABLE_CLICK;

                mIvIcon.setImageResource(R.drawable.ic_empty_view_nodata);
                setTvNoDataContent();
                showState();
                clickEnable = true;
                break;
            default:
                break;
        }
    }

    private void hideLoading() {
        if (mRlLoadingContainer.getVisibility() == View.VISIBLE) {
            mIvLoadingInner.clearAnimation();
            mIvLoadingOuter.clearAnimation();
            mRlLoadingContainer.setVisibility(View.GONE);
        }
    }

    private void hideState() {
        // Òþ²ØEmptyState
        if (mRlStateContainer.getVisibility() == View.VISIBLE) {
            mRlStateContainer.setVisibility(View.GONE);
        }
    }

    public void hide() {
        if(getVisibility()==View.VISIBLE) {
            hideState();

            mIvLoadingOuter.clearAnimation();
            mIvLoadingInner.clearAnimation();

            mIvLoadingOuter.startAnimation(mAnimHideOuter);
            mIvLoadingInner.startAnimation(mAnimHideInner);
        }
    }

    private void showState() {
        setVisibility(View.VISIBLE);
        // Òþ²ØLoading
        hideLoading();

        mRlStateContainer.setVisibility(View.VISIBLE);
        mRlIcon.clearAnimation();
        mRlIcon.startAnimation(mAnimShowIcon);

        //mTvEmptyTitle.setVisibility(View.GONE);
        //mTvEmptyTitle.clearAnimation();
        //mTvEmptyTitle.startAnimation(mAnimShowTitle);

        mTvEmptyMessage.setVisibility(View.VISIBLE);
        mTvEmptyMessage.clearAnimation();
        mTvEmptyMessage.startAnimation(mAnimShowMessage);
    }


    public void setOnLayoutClickListener(OnClickListener listener) {
        this.listener = listener;
    }

    public void setTvNoDataContent() {
        if (!strNoDataContent.equals(""))
            mTvEmptyMessage.setText(strNoDataContent);
        else
            mTvEmptyMessage.setText(R.string.error_view_no_data);
    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility == View.GONE)
            mErrorState = HIDE_LAYOUT;
        super.setVisibility(visibility);
    }

    public String getMessage() {
        if (mTvEmptyMessage != null) {
            return mTvEmptyMessage.getText().toString();
        }
        return "";
    }

    public void setErrorMessage(String msg) {
        mTvEmptyMessage.setText(msg);
    }
}
