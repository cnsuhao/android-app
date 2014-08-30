package net.oschina.app.v2.activity.news;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import net.oschina.app.R;
import net.oschina.app.v2.activity.news.fragment.BlogDetailFragment;
import net.oschina.app.v2.activity.news.fragment.NewsDetailFragment;
import net.oschina.app.v2.activity.news.fragment.QuestionDetailFragment;
import net.oschina.app.v2.activity.news.fragment.SoftwareDetailFragment;
import net.oschina.app.v2.base.BaseActivity;

/**
 * 新闻资讯详情
 * @author william_sim
 *
 */
public class NewsDetailActivity extends BaseActivity {

	public static final int DISPLAY_NEWS = 0;
	public static final int DISPLAY_BLOG = 1;
	public static final int DISPLAY_SOFTWARE = 2;
	public static final int DISPLAY_QUESTION =3;
	public static final String BUNDLE_KEY_DISPLAY_TYPE = "BUNDLE_KEY_DISPLAY_TYPE";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.v2_activity_simple_fragment);
		
		//int newsId = getIntent().getIntExtra("news_id", 0);
		//Bundle args = new Bundle();
		//args.putInt(key, value);
		
		int displayType = getIntent().getIntExtra(BUNDLE_KEY_DISPLAY_TYPE, DISPLAY_NEWS);
		Fragment fragment = null;
		switch (displayType) {
		case DISPLAY_NEWS:
			fragment = new NewsDetailFragment();
			break;
		case DISPLAY_BLOG:
			fragment = new BlogDetailFragment();
			break;
		case DISPLAY_SOFTWARE:
			fragment = new SoftwareDetailFragment();
			break;
		case DISPLAY_QUESTION:
			fragment = new QuestionDetailFragment();
			break;
		default:
			break;
		}
		
		FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
		trans.replace(R.id.container, fragment);
		trans.commit();
	}
	
	
}
