package net.oschina.app.v2.activity.chat.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.tonlin.osc.happy.R;
import com.viewpagerindicator.CirclePageIndicator;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.chat.emoji.EmojiViewPagerAdapter;
import net.oschina.app.v2.activity.chat.emoji.Emojicon;
import net.oschina.app.v2.activity.chat.emoji.EmojiconEditText;
import net.oschina.app.v2.activity.chat.emoji.EmojiconTextView;
import net.oschina.app.v2.activity.chat.emoji.People;
import net.oschina.app.v2.emoji.SoftKeyboardStateHelper;
import net.oschina.app.v2.utils.TDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tonlin on 2015/5/29.
 */
public class ComposeView extends LinearLayout implements View.OnClickListener, RecordButton.OnFinishedRecordListener, SoftKeyboardStateHelper.SoftKeyboardStateListener, EmojiViewPagerAdapter.OnClickEmojiListener {

    private ImageView mIvEmoji;
    private ImageView mIvVoiceText;
    private Animation mShowAnim, mDismissAnim, mShowMoreAnim, mDismissMoreAnim;
    private ImageView mIvMore;
    private Button mBtnSend;
    private RecordButton mBtnVoice;
    private EmojiconEditText mEtText;

    private OnComposeOperationDelegate mDelegate;
    private View mRlBottom;
    private SoftKeyboardStateHelper mKeyboardHelper;
    private ViewPager mViewPager;
    private EmojiViewPagerAdapter mPagerAdapter;
    private int mCurrentKeyboardHeigh;
    private View mLyOpt, mLyEmoji;
    private boolean mIsKeyboardVisible;
    private boolean mNeedShowEmojiOnKeyboardClosed;


    public interface OnComposeOperationDelegate {
        void onSendText(String text);

        void onSendVoice(String file, int length);
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
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
        mIvEmoji.setOnClickListener(this);
        mEtText = (EmojiconEditText) findViewById(R.id.et_text);
        mEtText.addTextChangedListener(mTextWatcher);
        mIvMore = (ImageView) findViewById(R.id.iv_more);
        mIvMore.setOnClickListener(this);
        mBtnSend = (Button) findViewById(R.id.btn_send);
        mBtnSend.setOnClickListener(this);
        mBtnVoice = (RecordButton) findViewById(R.id.btn_voice);
        mBtnVoice.setRecorderCallback(this);
        mIvVoiceText = (ImageView) findViewById(R.id.iv_voice_text);
        mIvVoiceText.setOnClickListener(this);

        mLyEmoji = findViewById(R.id.ly_emoji);
        mLyOpt = findViewById(R.id.ly_opt);
        mRlBottom = findViewById(R.id.rl_bottom);


        mKeyboardHelper = new SoftKeyboardStateHelper(((Activity) getContext()).getWindow()
                .getDecorView());
        mKeyboardHelper.addSoftKeyboardStateListener(this);


        mViewPager = (ViewPager) findViewById(R.id.view_pager);

        int emojiHeight = caculateEmojiPanelHeight();

        Emojicon[] emojis = People.DATA;
        List<List<Emojicon>> pagers = new ArrayList<List<Emojicon>>();
        List<Emojicon> es = null;
        int size = 0;
        boolean justAdd = false;
        for (Emojicon ej : emojis) {
            if (size == 0) {
                es = new ArrayList<>();
            }
            if (size == 27) {
                es.add(new Emojicon(""));
            } else {
                es.add(ej);
            }
            size++;
            if (size == 28) {
                pagers.add(es);
                size = 0;
                justAdd = true;
            } else {
                justAdd = false;
            }
        }
        if (!justAdd && es != null) {
            int exSize = 28 - es.size();
            for (int i = 0; i < exSize; i++) {
                es.add(new Emojicon(""));
            }
            pagers.add(es);
        }

        mPagerAdapter = new EmojiViewPagerAdapter(getContext(), pagers,
                emojiHeight, this);
        mViewPager.setAdapter(mPagerAdapter);

        CirclePageIndicator indicator = (CirclePageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(mViewPager);
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
            if (mLyOpt.getVisibility() == View.VISIBLE) {
                mLyOpt.setVisibility(View.GONE);
            } else {
                mLyOpt.setVisibility(View.VISIBLE);
            }
            hideEmojiAndKeyboard();
        } else if (id == R.id.iv_emoji) {//点击表情
            if (mLyOpt.getVisibility() == View.VISIBLE) {
                mLyOpt.setVisibility(View.GONE);
            }

            if (mLyEmoji.getVisibility() == View.GONE) {
                mNeedShowEmojiOnKeyboardClosed = true;
                tryShowEmojiPanel();
            } else {
                tryHideEmojiPanel();
            }
        }
    }

    private int caculateEmojiPanelHeight() {
        mCurrentKeyboardHeigh = AppContext.getSoftKeyboardHeight();
        if (mCurrentKeyboardHeigh == 0) {
            mCurrentKeyboardHeigh = (int) TDevice.dpToPixel(180);
        }

        mLyOpt.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, mCurrentKeyboardHeigh));

        int emojiPanelHeight = (int) (mCurrentKeyboardHeigh - TDevice
                .dpToPixel(20));
        int emojiHeight = (int) (emojiPanelHeight / 4);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, emojiPanelHeight);
        mViewPager.setLayoutParams(lp);
        if (mPagerAdapter != null) {
            mPagerAdapter.setEmojiHeight(emojiHeight);
        }
        return emojiHeight;
    }

    private void tryShowEmojiPanel() {
        if (mIsKeyboardVisible) {
            TDevice.hideSoftKeyboard(mEtText);
        } else {
            showEmojiPanel();
        }
    }

    private void tryHideEmojiPanel() {
        if (!mIsKeyboardVisible) {
            mEtText.requestFocus();
            TDevice.showSoftKeyboard(mEtText);
        } else {
            hideEmojiPanel();
        }
    }

    private void showEmojiPanel() {
        mNeedShowEmojiOnKeyboardClosed = false;
        mLyEmoji.setVisibility(View.VISIBLE);
        //mDelegate.onEmojiPanelVisiable(true, mLyEmoji.getHeight());
        //mIvEmoji.setBackgroundResource(R.drawable.btn_emoji_pressed);
    }

    private void hideEmojiPanel() {
        if (mLyEmoji.getVisibility() == View.VISIBLE) {
            mLyEmoji.setVisibility(View.GONE);
            //mDelegate.onEmojiPanelVisiable(true,0);
            //mIvEmoji.setBackgroundResource(R.drawable.btn_emoji_selector);
        }
    }

    public void hideEmojiAndKeyboard() {
        hideEmojiPanel();
        TDevice.hideSoftKeyboard(mEtText);
    }

    @Override
    public void onSoftKeyboardOpened(int keyboardHeightInPx) {
        int realKeyboardHeight = keyboardHeightInPx
                - TDevice.getStatusBarHeight();

        AppContext.setSoftKeyboardHeight(realKeyboardHeight);
        if (mCurrentKeyboardHeigh != realKeyboardHeight) {
            caculateEmojiPanelHeight();
        }

        mIsKeyboardVisible = true;
        hideEmojiPanel();

        if (mDelegate != null) {
            //mDelegate.onSoftKeyboardOpened();
        }
    }

    @Override
    public void onSoftKeyboardClosed() {
        mIsKeyboardVisible = false;
        if (mNeedShowEmojiOnKeyboardClosed) {
            showEmojiPanel();
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                }
//            },1000);
        }
    }


    public static void input(EditText editText, Emojicon emojicon) {
        if (editText == null || emojicon == null) {
            return;
        }

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (start < 0) {
            editText.append(emojicon.getEmoji());
        } else {
            editText.getText().replace(Math.min(start, end), Math.max(start, end), emojicon.getEmoji(), 0, emojicon.getEmoji().length());
        }
    }

    public static void backspace(EditText editText) {
        KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
        editText.dispatchKeyEvent(event);
    }

    @Override
    public void onEmojiClick(Emojicon emoji) {
        input(mEtText,emoji);
    }

    @Override
    public void onDelete() {
        backspace(mEtText);
    }

    @Override
    public void onFinishedRecord(String audioPath, int length) {
        if (mDelegate != null) {
            mDelegate.onSendVoice(audioPath, length);
        }
    }

    public void setOperationDelegate(OnComposeOperationDelegate delegate) {
        mDelegate = delegate;
    }

    public void clearText() {
        if (mEtText != null) {
            mEtText.setText("");
        }
    }
}
