package net.oschina.app.v2.activity.settings.fragment;

import net.oschina.app.common.UIHelper;
import net.oschina.app.v2.base.BaseFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tonlin.osc.happy.R;

public class SettingsFragment extends BaseFragment {

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.v2_fragment_settings, container,
				false);
		initViews(view);
		return view;
	}

	private void initViews(View view) {
		view.findViewById(R.id.ly_about).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		if (id == R.id.ly_about) {
			UIHelper.showAbout(getActivity());
		}
	}
}
