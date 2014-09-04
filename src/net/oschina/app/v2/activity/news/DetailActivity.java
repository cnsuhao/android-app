package net.oschina.app.v2.activity.news;

import java.lang.ref.WeakReference;

import net.oschina.app.AppContext;
import net.oschina.app.R;
import net.oschina.app.v2.activity.news.fragment.BlogDetailFragment;
import net.oschina.app.v2.activity.news.fragment.NewsDetailFragment;
import net.oschina.app.v2.activity.news.fragment.QuestionDetailFragment;
import net.oschina.app.v2.activity.news.fragment.SoftwareDetailFragment;
import net.oschina.app.v2.activity.tweet.fragment.TweetDetailFragment;
import net.oschina.app.v2.base.BaseActivity;
import net.oschina.app.v2.base.BaseFragment;
import net.oschina.app.v2.emoji.EmojiFragment;
import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.internal.widget.ListPopupWindow;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * 新闻资讯详情
 * 
 * @author william_sim
 * 
 */
public class DetailActivity extends BaseActivity implements OnItemClickListener {

	public static final int DISPLAY_NEWS = 0;
	public static final int DISPLAY_BLOG = 1;
	public static final int DISPLAY_SOFTWARE = 2;
	public static final int DISPLAY_QUESTION = 3;
	public static final int DISPLAY_TWEET = 4;
	public static final String BUNDLE_KEY_DISPLAY_TYPE = "BUNDLE_KEY_DISPLAY_TYPE";
	private ListPopupWindow mMenuWindow;
	private MenuAdapter mMenuAdapter;

	private WeakReference<BaseFragment> mFragment, mEmojiFragment;

	@Override
	protected int getLayoutId() {
		return R.layout.v2_activity_detail;
	}

	@Override
	protected boolean hasBackButton() {
		return true;
	}

	@Override
	protected int getActionBarTitle() {
		return R.string.actionbar_title_detail;
	}

	@Override
	protected int getActionBarCustomView() {
		return R.layout.v2_actionbar_custom_detail;
	}

	@Override
	protected void init(Bundle savedInstanceState) {
		super.init(savedInstanceState);
		int displayType = getIntent().getIntExtra(BUNDLE_KEY_DISPLAY_TYPE,
				DISPLAY_NEWS);
		BaseFragment fragment = null;
		int actionBarTitle = 0;
		boolean needEmoji = true;
		switch (displayType) {
		case DISPLAY_NEWS:
			actionBarTitle = R.string.actionbar_title_news;
			fragment = new NewsDetailFragment();
			break;
		case DISPLAY_BLOG:
			actionBarTitle = R.string.actionbar_title_blog;
			fragment = new BlogDetailFragment();
			break;
		case DISPLAY_SOFTWARE:
			actionBarTitle = R.string.actionbar_title_software;
			fragment = new SoftwareDetailFragment();
			needEmoji = false;
			break;
		case DISPLAY_QUESTION:
			actionBarTitle = R.string.actionbar_title_question;
			fragment = new QuestionDetailFragment();
			break;
		case DISPLAY_TWEET:
			actionBarTitle = R.string.actionbar_title_tweet;
			fragment = new TweetDetailFragment();
			break;
		default:
			break;
		}
		// setActionBarTitle(actionBarTitle);
		FragmentTransaction trans = getSupportFragmentManager()
				.beginTransaction();
		mFragment = new WeakReference<BaseFragment>(fragment);
		trans.replace(R.id.container, fragment);
		if (needEmoji) {
			EmojiFragment f = new EmojiFragment();
			mEmojiFragment = new WeakReference<BaseFragment>(f);
			trans.replace(R.id.emoji_container, f);
		}
		trans.commit();
	}

	@Override
	public void onBackPressed() {
		if (mEmojiFragment != null && mEmojiFragment.get() != null) {
			if (mEmojiFragment.get().onBackPressed()) {
				return;
			}
		}
		if (mFragment != null && mFragment.get() != null) {
			if (mFragment.get().onBackPressed()) {
				return;
			}
		}
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.detail_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.detail_menu_more:
			showMoreOptionMenu(findViewById(R.id.detail_menu_more));
			break;
		}
		return true;
	}

	private void showMoreOptionMenu(View view) {
		mMenuWindow = new ListPopupWindow(this);
		if (mMenuAdapter == null) {
			mMenuAdapter = new MenuAdapter();
		}
		mMenuWindow.setModal(true);
		mMenuWindow.setContentWidth(getResources().getDimensionPixelSize(
				R.dimen.popo_menu_dialog_width));
		mMenuWindow.setAdapter(mMenuAdapter);
		mMenuWindow.setOnItemClickListener(this);
		mMenuWindow.setAnchorView(view);
		mMenuWindow.show();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (mMenuWindow != null) {
			mMenuWindow.dismiss();
			mMenuWindow = null;
		}
	}

	@SuppressLint("ViewHolder")
	private static class MenuAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(
					R.layout.v2_list_cell_popup_menu, null);
			TextView name = (TextView) convertView.findViewById(R.id.tv_name);

			int iconResId = 0;
			if (position == 0) {
				name.setText(R.string.detail_menu_favorite);
				iconResId = R.drawable.actionbar_menu_icn_favoirite;
			} else if (position == 1) {
				name.setText(parent.getResources().getString(
						R.string.detail_menu_for_share));
				iconResId = R.drawable.actionbar_menu_icn_share;
			}
			Drawable drawable = AppContext.resources().getDrawable(iconResId);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			name.setCompoundDrawables(drawable, null, null, null);
			return convertView;
		}
	}
}
