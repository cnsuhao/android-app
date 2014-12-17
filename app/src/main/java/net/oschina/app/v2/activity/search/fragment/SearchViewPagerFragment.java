package net.oschina.app.v2.activity.search.fragment;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.search.adapter.SearchTabPagerAdapter;
import net.oschina.app.v2.base.BaseFragment;
import net.oschina.app.v2.ui.pagertab.PagerSlidingTabStrip;
import net.oschina.app.v2.utils.SimpleTextWatcher;
import net.oschina.app.v2.utils.TDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.tonlin.osc.happy.R;

public class SearchViewPagerFragment extends BaseFragment implements
		OnPageChangeListener, OnClickListener {

	private PagerSlidingTabStrip mTabStrip;
	private ViewPager mViewPager;
	private SearchTabPagerAdapter mTabAdapter;

	private EditText mEtSearch;
	private View mRlContent;
	private ImageButton mIbClear;
	private View mIbSearch;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int mode = WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
				| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
		getActivity().getWindow().setSoftInputMode(mode);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.v2_fragment_search, container,
				false);
		mTabStrip = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);
		mViewPager = (ViewPager) view.findViewById(R.id.main_tab_pager);

		if (mTabAdapter == null) {
			mTabAdapter = new SearchTabPagerAdapter(getChildFragmentManager(),
					getActivity(), mViewPager);
		}
		mViewPager.setOffscreenPageLimit(mTabAdapter.getCacheCount());
		mViewPager.setAdapter(mTabAdapter);
		mViewPager.setOnPageChangeListener(this);
		mTabStrip.setViewPager(mViewPager);

		mIbSearch = view.findViewById(R.id.ib_search);
		mIbSearch.setOnClickListener(this);
		mIbClear = (ImageButton) view.findViewById(R.id.ib_clear);
		mIbClear.setOnClickListener(this);
		mEtSearch = (EditText) view.findViewById(R.id.et_content);
		mEtSearch.setOnClickListener(this);
		mEtSearch.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		mEtSearch.addTextChangedListener(new SimpleTextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (mEtSearch.getText().toString().length() > 0) {
					mIbClear.setVisibility(View.VISIBLE);
					mIbSearch.setVisibility(View.VISIBLE);
				} else {
					mIbClear.setVisibility(View.GONE);
					mIbSearch.setVisibility(View.GONE);
				}
			}
		});
		mEtSearch.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH
						|| (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					handleSearch();
					return true;
				}
				return false;
			}
		});
		mEtSearch.requestFocus();
		mRlContent = view.findViewById(R.id.rl_content);
		return view;
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		mTabStrip.onPageScrollStateChanged(arg0);
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		mTabStrip.onPageScrolled(arg0, arg1, arg2);
		mTabAdapter.onPageScrolled(arg0);
	}

	@Override
	public void onPageSelected(int arg0) {
		mTabStrip.onPageSelected(arg0);
		mTabAdapter.onPageSelected(arg0);
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		if (id == R.id.ib_search) {
			handleSearch();
		} else if (id == R.id.ib_clear) {
			mEtSearch.getText().clear();
		}
	}

	private void handleSearch() {
		String content = mEtSearch.getText().toString().trim();
		if (!TextUtils.isEmpty(content)) {
			mTabAdapter.search(content);
			mRlContent.setVisibility(View.VISIBLE);
			TDevice.hideSoftKeyboard(mEtSearch);
		} else {
			AppContext.showToastShort(R.string.tip_search_content_empty);
		}
	}
}