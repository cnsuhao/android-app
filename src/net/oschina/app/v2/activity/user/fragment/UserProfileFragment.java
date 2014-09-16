package net.oschina.app.v2.activity.user.fragment;

import java.io.ByteArrayInputStream;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.base.BaseFragment;
import net.oschina.app.v2.model.MyInformation;
import net.oschina.app.v2.ui.dialog.CommonDialog;
import net.oschina.app.v2.ui.dialog.DialogHelper;
import net.oschina.app.v2.ui.empty.EmptyLayout;
import net.oschina.app.v2.utils.UIHelper;

import org.apache.http.Header;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tonlin.osc.happy.R;
import com.umeng.analytics.MobclickAgent;

public class UserProfileFragment extends BaseFragment {
	private static final String USER_PROFILE_SCREEN = "user_profile_screen";
	private ImageView mIvAvatar, mIvGender;
	private TextView mTvName;
	private TextView mTvFavorite, mTvFollowing, mTvFollower;
	private TextView mTvJoinTime, mTvLocation, mTvDevelopmentPlatform,
			mTvAcademicFocus;
	private EmptyLayout mEmptyView;
	private MyInformation mInfo;
	private AsyncHttpResponseHandler mHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
			try {
				mInfo = MyInformation.parse(new ByteArrayInputStream(arg2));
				fillUI();
				mEmptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
			} catch (Exception e) {
				e.printStackTrace();
				onFailure(arg0, arg1, arg2, e);
			}
		}

		@Override
		public void onFailure(int arg0, Header[] arg1, byte[] arg2,
				Throwable arg3) {
			mEmptyView.setErrorType(EmptyLayout.NETWORK_ERROR);
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.v2_fragment_profile, container,
				false);
		initViews(view);
		sendLoadInfoRequest();
		return view;
	}

	private void initViews(View view) {
		mEmptyView = (EmptyLayout) view.findViewById(R.id.error_layout);
		mIvAvatar = (ImageView) view.findViewById(R.id.iv_avatar);
		mTvName = (TextView) view.findViewById(R.id.tv_name);
		mIvGender = (ImageView) view.findViewById(R.id.iv_gender);

		mTvFavorite = (TextView) view.findViewById(R.id.tv_favorite);
		mTvFollowing = (TextView) view.findViewById(R.id.tv_following);
		mTvFollower = (TextView) view.findViewById(R.id.tv_follower);

		mTvJoinTime = (TextView) view.findViewById(R.id.tv_join_time);
		mTvLocation = (TextView) view.findViewById(R.id.tv_location);
		mTvDevelopmentPlatform = (TextView) view
				.findViewById(R.id.tv_development_platform);
		mTvAcademicFocus = (TextView) view.findViewById(R.id.tv_academic_focus);

		view.findViewById(R.id.btn_edit_avatar).setOnClickListener(this);
		view.findViewById(R.id.ly_favorite).setOnClickListener(this);
		view.findViewById(R.id.ly_following).setOnClickListener(this);
		view.findViewById(R.id.ly_follower).setOnClickListener(this);
		view.findViewById(R.id.btn_logout).setOnClickListener(this);
	}

	private void fillUI() {
		ImageLoader.getInstance().displayImage(mInfo.getFace(), mIvAvatar);
		mTvName.setText(mInfo.getName());
		mIvGender
				.setImageResource(mInfo.getGender() == 1 ? R.drawable.userinfo_icon_male
						: R.drawable.userinfo_icon_female);
		mTvFavorite.setText(String.valueOf(mInfo.getFavoritecount()));
		mTvFollowing.setText(String.valueOf(mInfo.getFollowerscount()));
		mTvFollower.setText(String.valueOf(mInfo.getFanscount()));

		mTvJoinTime.setText(mInfo.getJointime());
		mTvLocation.setText(mInfo.getFrom());
		mTvDevelopmentPlatform.setText(mInfo.getDevplatform());
		mTvAcademicFocus.setText(mInfo.getExpertise());
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		if (id == R.id.ly_follower) {
			UIHelper.showFriends(getActivity(), 1);
		} else if (id == R.id.ly_following) {
			UIHelper.showFriends(getActivity(), 0);
		} else if (id == R.id.ly_favorite) {
			UIHelper.showUserFavorite(getActivity());
		} else if (id == R.id.btn_logout) {
			handleLogout();
		}
	}

	private void handleLogout() {
		CommonDialog dialog = DialogHelper
				.getPinterestDialogCancelable(getActivity());
		dialog.setMessage(R.string.message_logout);
		dialog.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						AppContext.instance().Logout();
						AppContext.showToastShort(R.string.tip_logout_success);
						getActivity().finish();
					}
				});
		dialog.setNegativeButton(R.string.cancle, null);
		dialog.show();
	}

	private void sendLoadInfoRequest() {
		mEmptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
		int uid = AppContext.instance().getLoginUid();
		NewsApi.getMyInformation(uid, mHandler);
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(USER_PROFILE_SCREEN);
		MobclickAgent.onResume(getActivity());
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(USER_PROFILE_SCREEN);
		MobclickAgent.onPause(getActivity());
	}
}
