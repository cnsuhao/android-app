package net.oschina.app.v2.ui.pagertab;

import java.util.Locale;

import net.oschina.app.R;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PagerSlidingTabStrip extends HorizontalScrollView implements
		android.support.v4.view.ViewPager.OnPageChangeListener {

	public static interface OnTabClickListener {

		public abstract void onTabClicked(int i);
	}

	static class SavedState extends BaseSavedState {
		int currentPosition;

		private SavedState(Parcel parcel) {
			super(parcel);
			currentPosition = parcel.readInt();
		}

		public SavedState(Parcelable parcelable) {
			super(parcelable);
		}

		@Override
		public void writeToParcel(Parcel parcel, int i) {
			super.writeToParcel(parcel, i);
			parcel.writeInt(currentPosition);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}

			@Override
			public SavedState createFromParcel(Parcel source) {
				return new SavedState(source);
			}
		};
	}

	private static final int ATTRS[];
	private int checkedTextColor;
	private int currentPosition;
	private float currentPositionOffset;
	private int dividerColor;
	private int dividerPadding;
	private Paint dividerPaint;
	private int dividerWidth;
	private int indicatorColor;
	private int indicatorHeight;
	private int lastScrollX;
	private Locale locale;
	private OnTabClickListener onTabClickListener;
	private ViewPager pager;
	private Paint rectPaint;
	private int scrollOffset;
	private int tabCount;
	private int tabPadding;
	private LinearLayout.LayoutParams tabViewLayoutParams;
	private LinearLayout tabsContainer;
	private boolean textAllCaps;
	private int unCheckedTextColor;
	private int underlineColor;
	private int underlineHeight;

	static {
		int ai[] = new int[2];
		ai[0] = 0x1010095;
		ai[1] = 0x1010098;
		ATTRS = ai;
	}

	public PagerSlidingTabStrip(Context context) {
		this(context, null);
	}

	public PagerSlidingTabStrip(Context context, AttributeSet attributeset) {
		this(context, attributeset, 0);
	}

	public PagerSlidingTabStrip(Context context, AttributeSet attributeset,
			int i) {
		super(context, attributeset, i);
		currentPosition = 0;
		currentPositionOffset = 0.0F;
		indicatorColor = Color.parseColor("#40AA53");// 0xff01d9ae;
		underlineColor = Color.parseColor("#DADADA");
		dividerColor = 0;
		checkedTextColor = R.color.tab_strip_text_selected;
		unCheckedTextColor = R.color.tab_strip_text_unselected;
		textAllCaps = true;
		scrollOffset = 52;
		indicatorHeight = 4;
		underlineHeight = 1;
		dividerPadding = 12;
		tabPadding = 0;
		dividerWidth = 1;
		lastScrollX = 0;
		onTabClickListener = null;
		setFillViewport(true);
		setWillNotDraw(false);
		tabsContainer = new LinearLayout(context);
		tabsContainer.setOrientation(0);
		tabsContainer.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
		addView(tabsContainer);
		android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
		scrollOffset = (int) TypedValue.applyDimension(1, scrollOffset, dm);
		indicatorHeight = (int) TypedValue.applyDimension(1, indicatorHeight,
				dm);
		underlineHeight = (int) TypedValue.applyDimension(0, underlineHeight,
				dm);
		dividerPadding = (int) TypedValue.applyDimension(1, dividerPadding, dm);
		tabPadding = (int) TypedValue.applyDimension(1, tabPadding, dm);
		dividerWidth = (int) TypedValue.applyDimension(1, dividerWidth, dm);
		context.obtainStyledAttributes(attributeset, ATTRS).recycle();
		TypedArray td = context.obtainStyledAttributes(attributeset,
				R.styleable.PagerSlidingTabStrip);
		indicatorColor = td.getColor(0, indicatorColor);
		underlineColor = td.getColor(1, underlineColor);
		dividerColor = td.getColor(2, dividerColor);
		checkedTextColor = td.getResourceId(4, R.color.tab_strip_text_selected);
		unCheckedTextColor = td.getResourceId(4,
				R.color.tab_strip_text_unselected);
		indicatorHeight = td.getDimensionPixelSize(5, indicatorHeight);
		underlineHeight = td.getDimensionPixelSize(6, underlineHeight);
		dividerPadding = td.getDimensionPixelSize(7, dividerPadding);
		tabPadding = td.getDimensionPixelSize(8, tabPadding);
		scrollOffset = td.getDimensionPixelSize(9, scrollOffset);
		textAllCaps = td.getBoolean(12, textAllCaps);
		td.recycle();
		rectPaint = new Paint();
		rectPaint.setAntiAlias(true);
		rectPaint.setStyle(Paint.Style.FILL);
		dividerPaint = new Paint();
		dividerPaint.setAntiAlias(true);
		dividerPaint.setStrokeWidth(dividerWidth);
		tabViewLayoutParams = new LinearLayout.LayoutParams(0, -1, 1.0F);
		if (locale == null)
			locale = getResources().getConfiguration().locale;
	}

	private void addTab(final int position, View view) {
		view.setFocusable(true);
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (pager.getCurrentItem() == position
						&& onTabClickListener != null)
					onTabClickListener.onTabClicked(position);
				else
					pager.setCurrentItem(position, true);
			}
		});
		view.setPadding(tabPadding, 0, tabPadding, 0);
		tabsContainer.addView(view, position, tabViewLayoutParams);
	}

	@SuppressLint("InflateParams")
	private void addTabView(int i, String s) {
		LinearLayout linearlayout = (LinearLayout) LayoutInflater.from(
				getContext()).inflate(R.layout.v2_tab_layout_main, null);
		((TextView) linearlayout.findViewById(R.id.tab_title_label)).setText(s);
		addTab(i, linearlayout);
	}

	private void scrollToChild(int i, int j) {
		if (tabCount != 0) {
			int k = j + tabsContainer.getChildAt(i).getLeft();
			if (i > 0 || j > 0)
				k -= scrollOffset;
			if (k != lastScrollX) {
				lastScrollX = k;
				scrollTo(k, 0);
			}
		}// goto _L2; else goto _L1
	}

	private void setChooseTabViewTextColor(int i) {
		int j = tabsContainer.getChildCount();
		int k = 0;
		while (k < j) {
			TextView textview = (TextView) ((LinearLayout) tabsContainer
					.getChildAt(k)).findViewById(R.id.tab_title_label);
			if (k == i)
				textview.setTextColor(getResources().getColor(checkedTextColor));
			else
				textview.setTextColor(getResources().getColor(
						unCheckedTextColor));
			k++;
		}
	}

	public int getCurrentPosition() {
		return currentPosition;
	}

	public int getDividerColor() {
		return dividerColor;
	}

	public int getDividerPadding() {
		return dividerPadding;
	}

	public int getIndicatorColor() {
		return indicatorColor;
	}

	public int getIndicatorHeight() {
		return indicatorHeight;
	}

	public int getScrollOffset() {
		return scrollOffset;
	}

	public int getTabPaddingLeftRight() {
		return tabPadding;
	}

	public int getUnderlineColor() {
		return underlineColor;
	}

	public int getUnderlineHeight() {
		return underlineHeight;
	}

	public boolean isTextAllCaps() {
		return textAllCaps;
	}

	public void notifyDataSetChanged() {
		tabsContainer.removeAllViews();
		tabCount = pager.getAdapter().getCount();
		for (int i = 0; i < tabCount; i++)
			addTabView(i, pager.getAdapter().getPageTitle(i).toString());

		getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {

					@SuppressWarnings("deprecation")
					@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
					@Override
					public void onGlobalLayout() {
						if (android.os.Build.VERSION.SDK_INT < 16)
							getViewTreeObserver().removeGlobalOnLayoutListener(
									this);
						else
							getViewTreeObserver().removeOnGlobalLayoutListener(
									this);
						currentPosition = pager.getCurrentItem();
						setChooseTabViewTextColor(currentPosition);
						scrollToChild(currentPosition, 0);
					}
				});
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (!isInEditMode() && tabCount != 0) {
			int i = getHeight();
			rectPaint.setColor(underlineColor);
			canvas.drawRect(0.0F, i - underlineHeight,
					tabsContainer.getWidth(), i, rectPaint);
			rectPaint.setColor(indicatorColor);
			View view = tabsContainer.getChildAt(currentPosition);
			float f = view.getLeft();
			float f1 = view.getRight();
			float f2;
			if (currentPositionOffset > 0.0F && currentPosition < -1 + tabCount) {
				View view1 = tabsContainer.getChildAt(1 + currentPosition);
				float f3 = view1.getLeft();
				float f4 = view1.getRight();
				f2 = f3 * currentPositionOffset + f
						* (1.0F - currentPositionOffset);
				f1 = f4 * currentPositionOffset + f1
						* (1.0F - currentPositionOffset);
			} else {
				f2 = f;
			}
			canvas.drawRect(f2, i - indicatorHeight, f1, i, rectPaint);
		}
	}

	@Override
	public void onPageScrollStateChanged(int i) {
		if (i == 0)
			scrollToChild(pager.getCurrentItem(), 0);
	}

	@Override
	public void onPageScrolled(int i, float f, int j) {
		currentPosition = i;
		currentPositionOffset = f;
		scrollToChild(i, (int) (f * tabsContainer.getChildAt(i).getWidth()));
		invalidate();
	}

	@Override
	public void onPageSelected(int i) {
		setChooseTabViewTextColor(i);
	}

	@Override
	public void onRestoreInstanceState(Parcelable parcelable) {
		super.onRestoreInstanceState(((SavedState) parcelable).getSuperState());
		currentPosition = 0;
		requestLayout();
	}

	@Override
	public Parcelable onSaveInstanceState() {
		SavedState savedstate = new SavedState(super.onSaveInstanceState());
		savedstate.currentPosition = currentPosition;
		return savedstate;
	}

	public void setAllCaps(boolean flag) {
		textAllCaps = flag;
	}

	public void setCheckedTextColorResource(int i) {
		checkedTextColor = i;
		invalidate();
	}

	public void setDividerColor(int i) {
		dividerColor = i;
		invalidate();
	}

	public void setDividerColorResource(int i) {
		dividerColor = getResources().getColor(i);
		invalidate();
	}

	public void setDividerPadding(int i) {
		dividerPadding = i;
		invalidate();
	}

	public void setIndicatorColor(int i) {
		indicatorColor = i;
		invalidate();
	}

	public void setIndicatorColorResource(int i) {
		indicatorColor = getResources().getColor(i);
		invalidate();
	}

	public void setIndicatorHeight(int i) {
		indicatorHeight = i;
		invalidate();
	}

	public void setOnTabClickListener(OnTabClickListener listener) {
		onTabClickListener = listener;
	}

	public void setScrollOffset(int i) {
		scrollOffset = i;
		invalidate();
	}

	public void setUnderlineColor(int i) {
		underlineColor = i;
		invalidate();
	}

	public void setUnderlineColorResource(int i) {
		underlineColor = getResources().getColor(i);
		invalidate();
	}

	public void setUnderlineHeight(int i) {
		underlineHeight = i;
		invalidate();
	}

	public void setViewPager(ViewPager viewpager) {
		pager = viewpager;
		if (viewpager.getAdapter() == null) {
			throw new IllegalStateException(
					"ViewPager does not have adapter instance.");
		} else {
			notifyDataSetChanged();
			return;
		}
	}

	public void updateTab(int i, q q) {
		LinearLayout linearlayout = (LinearLayout) tabsContainer.getChildAt(i);
		ImageView imageview = (ImageView) linearlayout.findViewById(0x7f070618);
		r.a(q, (TextView) linearlayout.findViewById(0x7f070619), imageview);
	}
}
