package net.oschina.app.v2.activity.user.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import net.oschina.app.R;
import net.oschina.app.v2.base.BaseFragment;

public class ProfileFragment extends BaseFragment {
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.v2_fragment_profile, container,
				false);
		return view;
	}
}
