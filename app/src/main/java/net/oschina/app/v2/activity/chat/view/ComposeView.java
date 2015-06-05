package net.oschina.app.v2.activity.chat.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tonlin.osc.happy.R;

/**
 * Created by Tonlin on 2015/5/29.
 */
public class ComposeView extends RelativeLayout implements View.OnClickListener, RecordButton.OnFinishedRecordListener {

    private ImageView mIvEmoji;
    private ImageView mIvVoiceText;
    private Animation mShowAnim, mDismissAnim, mShowMoreAnim, mDismissMoreAnim;
    private ImageView mIvMore;
    private Button mBtnSend;
    private RecordButton mBtnVoice;
    private EditText mEtText;

    private OnComposeOperationDelegate mDelegate;
    private View mRlBottom;

    public interface OnComposeOperationDelegate {
        void onSendText(String text);

        void onSendVoice(String file,int length);
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (TextUtils.isEmpty(s) || s.toString().trim().equals("")) {//
                if (mBtnSend.getVisibility() == View.VISIBLE) {
                    dismissSendButton();
                }
            } else {
                if (mBtnSend.getVisibility() != View.VISIBLE) {
                    showSendButton();
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };


    private void showSendButton() {
        mIvMore.clearAnimation();
        mIvMore.startAnimation(mDismissMoreAnim);
        mShowAnim.reset();
        mBtnSend.clearAnimation();
        mBtnSend.startAnimation(mShowAnim);
    }

    private void dismissSendButton() {
        mIvMore.clearAnimation();
        mIvMore.startAnimation(mShowMoreAnim);
        mBtnSend.clearAnimation();
        mBtnSend.startAnimation(mDismissAnim);
    }

    public ComposeView(Context context) {
        this(context, null);
    }

    public ComposeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private ComposeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private ComposeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    private void init(Context context) {
        inflate(context, R.layout.v2_view_compose, this);

        mShowMoreAnim = AnimationUtils.loadAnimation(context, R.anim.chat_show_more_button);
        mDismissMoreAnim = AnimationUtils.loadAnimation(context, R.anim.chat_show_more_button);

        mShowAnim = AnimationUtils.loadAnimation(context, R.anim.chat_show_send_button);
        mShowAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mBtnSend.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mDismissAnim = AnimationUtils.loadAnimation(context, R.anim.chat_dismiss_send_button);
        mDismissAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mBtnSend.setVisibility(View.GONE);
                mIvMore.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mIvEmoji = (ImageView) findViewById(R.id.iv_emoji);
        mEtText = (EditText) findViewById(R.id.et_text);
        mEtText.addTextChangedListener(mTextWatcher);
        mIvMore = (ImageView) findViewById(R.id.iv_more);
        mIvMore.setOnClickListener(this);
        mBtnSend = (Button) findViewById(R.id.btn_send);
        mBtnSend.setOnClickListener(this);
        mBtnVoice = (RecordButton) findViewById(R.id.btn_voice);
        mBtnVoice.setRecorderCallback(this);
        mIvVoiceText = (ImageView) findViewById(R.id.iv_voice_text);
        mIvVoiceText.setOnClickListener(this);

        mRlBottom = findViewById(R.id.rl_bottom);
    }

    @Override
    public void onFinishedRecord(String audioPath,int length) {
        if(mDelegate != null){
            mDelegate.onSendVoice(audioPath,length);
        }
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.btn_send) {
            if (mDelegate != null) {
                mDelegate.onSendText(mEtText.getText().toString());
            }
        } else if (id == R.id.iv_voice_text) {
            if (mBtnVoice.getVisibility() == View.VISIBLE) {
                mBtnVoice.setVisibility(View.GONE);
                mEtText.setVisibility(View.VISIBLE);
                mIvEmoji.setVisibility(View.VISIBLE);
                mIvVoiceText.setImageResource(R.drawable.btn_to_voice_selector);
            } else {
                mBtnVoice.setVisibility(View.VISIBLE);
                mEtText.setVisibility(View.GONE);
                mIvEmoji.setVisibility(View.GONE);
                mIvVoiceText.setImageResource(R.drawable.btn_to_text_selector);
            }
        } else if (id == R.id.iv_more) {
            if (mRlBottom.getVisibility() == View.VISIBLE) {
                mRlBottom.setVisibility(View.GONE);
            } else {
                mRlBottom.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setOperationDelegate(OnComposeOperationDelegate delegate) {
        mDelegate = delegate;
    }

    public void clearText() {
        if (mEtText != null) {
            mEtText.getText().clear();
        }
    }
}
