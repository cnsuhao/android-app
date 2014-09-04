package net.oschina.app.v2.emoji;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.oschina.app.AppContext;
import net.oschina.app.R;
import net.oschina.app.v2.base.BaseFragment;
import net.oschina.app.v2.emoji.SoftKeyboardStateHelper.SoftKeyboardStateListener;
import net.oschina.app.v2.utils.TDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.viewpagerindicator.CirclePageIndicator;

public class EmojiFragment extends BaseFragment implements
		SoftKeyboardStateListener {

	private ViewPager mViewPager;
	private ImageButton mBtnEmoji, mBtnSend;
	private EditText mEtInput;
	private EmojiViewPagerAdapter mPagerAdapter;
	private SoftKeyboardStateHelper mKeyboardHelper;
	private boolean mIsKeyboardVisible;
	private boolean mNeedHideEmoji;
	private CirclePageIndicator mIndicator;
	private View mLyEmoji;

	private int mCurrentKeyboardHeigh;

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.v2_fragment_emoji, container,
				false);

		initViews(view);
		mKeyboardHelper = new SoftKeyboardStateHelper(getActivity()
				.findViewById(R.id.activity_root));
		mKeyboardHelper.addSoftKeyboardStateListener(this);
		return view;
	}

	@Override
	public void onDestroyView() {
		mKeyboardHelper.removeSoftKeyboardStateListener(this);
		super.onDestroyView();
	}

	private void initViews(View view) {
		mLyEmoji = view.findViewById(R.id.ly_emoji);
		mViewPager = (ViewPager) view.findViewById(R.id.view_pager);
		mIndicator = (CirclePageIndicator) view.findViewById(R.id.indicator);

		mBtnEmoji = (ImageButton) view.findViewById(R.id.btn_emoji);
		mBtnSend = (ImageButton) view.findViewById(R.id.btn_send);
		mEtInput = (EditText) view.findViewById(R.id.et_input);

		mBtnEmoji.setOnClickListener(this);

		Map<String, Emoji> emojis = EmojiHelper.qq_emojis;
		// int pagerSize = emojis.size() / 20;
		Iterator<Entry<String, Emoji>> itr = emojis.entrySet().iterator();
		List<List<Emoji>> pagers = new ArrayList<List<Emoji>>();
		List<Emoji> es = null;
		int size = 0;
		boolean justAdd = false;
		while (itr.hasNext()) {
			if (size == 0) {
				es = new ArrayList<Emoji>();
			}
			Emoji ej = itr.next().getValue();
			es.add(new Emoji(ej.getResId(), ej.getValue()));
			size++;
			if (size == 20) {
				pagers.add(es);
				size = 0;
				justAdd = true;
			} else {
				justAdd = false;
			}
		}
		if (!justAdd && es != null) {
			pagers.add(es);
		}

		int emojiHeight = caculateEmojiPanelHeight();

		mPagerAdapter = new EmojiViewPagerAdapter(getActivity(), pagers,
				emojiHeight);
		mViewPager.setAdapter(mPagerAdapter);
		mIndicator.setViewPager(mViewPager);
	}

	private int caculateEmojiPanelHeight() {
		mCurrentKeyboardHeigh = AppContext.getSoftKeyboardHeight();
		if (mCurrentKeyboardHeigh == 0) {
			mCurrentKeyboardHeigh = (int) TDevice.dpToPixel(180);
		}
		int emojiPanelHeight = (int) (mCurrentKeyboardHeigh - TDevice
				.dpToPixel(20));
		int emojiHeight = (int) (emojiPanelHeight / 3);

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, emojiPanelHeight);
		mViewPager.setLayoutParams(lp);
		if (mPagerAdapter != null) {
			mPagerAdapter.setEmojiHeight(emojiHeight);
		}
		return emojiHeight;
	}

	@Override
	public boolean onBackPressed() {
		if (mLyEmoji.getVisibility() == View.VISIBLE) {
			hideEmojiPanel();
			return true;
		}
		return super.onBackPressed();
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		if (id == R.id.btn_emoji) {
			if (mLyEmoji.getVisibility() == View.GONE) {
				mNeedHideEmoji = true;
				if (mIsKeyboardVisible) {
					TDevice.hideSoftKeyboard(getActivity().getCurrentFocus());
				} else {
					showEmojiPanel();
				}
			} else {
				hideEmojiPanel();
			}
		}
	}

	private void showEmojiPanel() {
		mNeedHideEmoji = false;
		mLyEmoji.setVisibility(View.VISIBLE);
		mBtnEmoji.setBackgroundResource(R.drawable.btn_emoji_pressed);
	}

	private void hideEmojiPanel() {
		mLyEmoji.setVisibility(View.GONE);
		mBtnEmoji.setBackgroundResource(R.drawable.btn_emoji_selector);
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
	}

	@Override
	public void onSoftKeyboardClosed() {
		mIsKeyboardVisible = false;
		if (mNeedHideEmoji) {
			showEmojiPanel();
		}
	}
}
