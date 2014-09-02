package net.oschina.app.v2.activity.user.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import net.oschina.app.R;
import net.oschina.app.common.UIHelper;
import net.oschina.app.v2.base.BaseFragment;

public class ProfileFragment extends BaseFragment {
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.v2_fragment_profile, container,
				false);
		initViews(view);
		return view;
	}

	private void initViews(View view) {
		view.findViewById(R.id.ly_favorite).setOnClickListener(this);
		view.findViewById(R.id.ly_following).setOnClickListener(this);
		view.findViewById(R.id.ly_follower).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		if (id == R.id.ly_follower) {
			UIHelper.showFriends(getActivity());
		} else if (id == R.id.ly_following) {
			UIHelper.showFriends(getActivity());
		} else if(id == R.id.ly_favorite){
			UIHelper.showUserFavorite(getActivity());
		}
	}
}
