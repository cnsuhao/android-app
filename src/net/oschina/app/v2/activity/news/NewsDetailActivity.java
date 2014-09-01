package net.oschina.app.v2.activity.news;

import net.oschina.app.R;
import net.oschina.app.v2.activity.news.fragment.BlogDetailFragment;
import net.oschina.app.v2.activity.news.fragment.NewsDetailFragment;
import net.oschina.app.v2.activity.news.fragment.QuestionDetailFragment;
import net.oschina.app.v2.activity.news.fragment.SoftwareDetailFragment;
import net.oschina.app.v2.activity.tweet.fragment.TweetDetailFragment;
import net.oschina.app.v2.base.BaseActivity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

/**
 * 新闻资讯详情
 * 
 * @author william_sim
 * 
 */
public class NewsDetailActivity extends BaseActivity {

	public static final int DISPLAY_NEWS = 0;
	public static final int DISPLAY_BLOG = 1;
	public static final int DISPLAY_SOFTWARE = 2;
	public static final int DISPLAY_QUESTION = 3;
	public static final int DISPLAY_TWEET = 4;
	public static final String BUNDLE_KEY_DISPLAY_TYPE = "BUNDLE_KEY_DISPLAY_TYPE";

	@Override
	protected int getLayoutId() {
		return R.layout.v2_activity_simple_fragment;
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
	protected void init(Bundle savedInstanceState) {
		super.init(savedInstanceState);
		int displayType = getIntent().getIntExtra(BUNDLE_KEY_DISPLAY_TYPE,
				DISPLAY_NEWS);
		Fragment fragment = null;
		int actionBarTitle = 0;
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
		//setActionBarTitle(actionBarTitle);
		
		FragmentTransaction trans = getSupportFragmentManager()
				.beginTransaction();
		trans.replace(R.id.container, fragment);
		trans.commit();
	}
}
