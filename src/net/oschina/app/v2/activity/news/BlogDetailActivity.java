package net.oschina.app.v2.activity.news;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import net.oschina.app.R;
import net.oschina.app.v2.activity.news.fragment.NewsDetailFragment;
import net.oschina.app.v2.base.BaseActivity;

/**
 * 新闻资讯详情
 * @author william_sim
 *
 */
public class BlogDetailActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.v2_activity_simple_fragment);
		
		//int newsId = getIntent().getIntExtra("news_id", 0);
		//Bundle args = new Bundle();
		//args.putInt(key, value);
		
		FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
		trans.replace(R.id.container, new NewsDetailFragment());
		trans.commit();
	}
	
	
}
