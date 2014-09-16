package net.oschina.app.v2.activity.settings.fragment;

import net.oschina.app.v2.base.BaseFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tonlin.osc.happy.R;
import com.umeng.analytics.MobclickAgent;

public class AboutFragment extends BaseFragment {

	private static final String ABOUT_SCREEN = "about_screen";

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.v2_fragment_about, container,
				false);
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(ABOUT_SCREEN);
		MobclickAgent.onResume(getActivity());
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(ABOUT_SCREEN);
		MobclickAgent.onPause(getActivity());
	}
}
