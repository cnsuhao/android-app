package net.oschina.app.v2.activity.common;

import net.oschina.app.R;
import net.oschina.app.bean.SimpleBackPage;
import net.oschina.app.v2.base.BaseActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

public class SimpleBackActivity extends BaseActivity {

	public final static String BUNDLE_KEY_PAGE = "BUNDLE_KEY_PAGE";

	@Override
	protected int getLayoutId() {
		return R.layout.v2_activity_simple_fragment;
	}

	@Override
	protected boolean hasBackButton() {
		return true;
	}

	@Override
	protected void init(Bundle savedInstanceState) {
		super.init(savedInstanceState);
		Intent data = getIntent();
		if (data == null) {
			throw new RuntimeException(
					"you must provide a page info to display");
		}
		int pageValue = data.getIntExtra(BUNDLE_KEY_PAGE, 0);
		SimpleBackPage page = SimpleBackPage.getPageByValue(pageValue);
		if (page == null) {
			throw new IllegalArgumentException("can not find page by value:"
					+ pageValue);
		}

		setActionBarTitle(page.getTitle());
		
		try {
			Fragment fragment = (Fragment) page.getClz().newInstance();
			FragmentTransaction trans = getSupportFragmentManager()
					.beginTransaction();
			trans.replace(R.id.container, fragment);
			trans.commit();
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException(
					"generate fragment error. by value:" + pageValue);
		}
	}
}
