package net.oschina.app.v2.activity.user.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

//import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.drawee.view.SimpleDraweeView;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tonlin.osc.happy.R;
import com.umeng.analytics.MobclickAgent;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.base.BaseFragment;
import net.oschina.app.v2.cache.CacheManager;
import net.oschina.app.v2.model.MyInformation;
import net.oschina.app.v2.model.User;
import net.oschina.app.v2.ui.empty.EmptyLayout;
import net.oschina.app.v2.utils.AvatarUtils;
import net.oschina.app.v2.utils.HTMLSpirit;
import net.oschina.app.v2.utils.TDevice;
import net.oschina.app.v2.utils.UIHelper;
import net.oschina.app.v2.utils.XmlUtils;

import org.apache.http.Header;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;

public class UserProfileFragment extends BaseFragment {
	private static final String USER_PROFILE_SCREEN = "user_profile_screen";
	private ImageView mIvGender;
	private SimpleDraweeView mIvAvatar;
	private TextView mTvName;
	private TextView mTvFavorite,mTvScore, mTvFollowing, mTvFollower;
	private TextView mTvJoinTime, mTvLocation, mTvDevelopmentPlatform,
			mTvAcademicFocus;
	private EmptyLayout mEmptyView;
	private User mUser;
	private AsyncTask<String, Void, MyInformation> mCacheTask;

	private AsyncHttpResponseHandler mHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
			try {
				MyInformation info = XmlUtils.toBean(MyInformation.class,arg2);
				//MyInformation.parse(new ByteArrayInputStream(arg2));
				if (info != null) {
					mUser = info.getUser();
					fillUI();
					mEmptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
					new SaveCacheTask(getActivity(), info, getCacheKey())
							.execute();
				} else {
					onFailure(arg0, arg1, arg2, new Throwable());
				}
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
		requestData(true);
		return view;
	}

	private void initViews(View view) {
		mEmptyView = (EmptyLayout) view.findViewById(R.id.error_layout);
		mEmptyView.setOnLayoutClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				requestData(true);
			}
		});
		mIvAvatar = (SimpleDraweeView) view.findViewById(R.id.iv_avatar);
		mTvName = (TextView) view.findViewById(R.id.tv_name);
		mIvGender = (ImageView) view.findViewById(R.id.iv_gender);

		mTvScore= (TextView) view.findViewById(R.id.tv_score);
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
		//ImageLoader.getInstance().displayImage(
		//		AvatarUtils.getLargeAvatar(mUser.getFace()), mIvAvatar);
		mIvAvatar.setImageURI(Uri.parse(mUser.getFace()));

		mTvName.setText(mUser.getName());
		mIvGender
				.setImageResource(mUser.getGender().equals("1") ? R.drawable.userinfo_icon_male
						: R.drawable.userinfo_icon_female);
		mTvScore.setText(String.valueOf(mUser.getScore()));
		mTvFavorite.setText(String.valueOf(mUser.getFavoriteCount()));
		mTvFollowing.setText(String.valueOf(mUser.getFollowers()));
		mTvFollower.setText(String.valueOf(mUser.getFans()));

		mTvJoinTime.setText(mUser.getJointime());
		mTvLocation.setText(mUser.getFrom());
		mTvDevelopmentPlatform.setText(mUser.getDevplatform());
		mTvAcademicFocus.setText(mUser.getExpertise());
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
        AlertDialog dialog = new AlertDialog.Builder(getActivity(),
                R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setMessage(R.string.message_logout)
                .setCancelable(true)
                .setNegativeButton(R.string.cancel,null)
                .setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AppContext.instance().Logout();
                        AppContext.showToastShort(R.string.tip_logout_success);
                        getActivity().finish();
                    }
                })
                .create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
	}

	private void requestData(boolean refresh) {
		mEmptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
		String key = getCacheKey();
		if (TDevice.hasInternet()
				&& (!CacheManager.isReadDataCache(getActivity(), key) || refresh)) {
			sendRequestData();
		} else {
			readCacheData(key);
		}
	}

	private void readCacheData(String key) {
		cancelReadCacheTask();
		mCacheTask = new CacheTask(getActivity()).execute(key);
	}

	private void cancelReadCacheTask() {
		if (mCacheTask != null) {
			mCacheTask.cancel(true);
			mCacheTask = null;
		}
	}

	private void sendRequestData() {
		int uid = AppContext.instance().getLoginUid();
		NewsApi.getMyInformation(uid, mHandler);
	}

	private String getCacheKey() {
		return "user_profile_" + AppContext.instance().getLoginUid();
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

	private class CacheTask extends AsyncTask<String, Void, MyInformation> {
		private WeakReference<Context> mContext;

		private CacheTask(Context context) {
			mContext = new WeakReference<>(context);
		}

		@Override
		protected MyInformation doInBackground(String... params) {
			Serializable seri = CacheManager.readObject(mContext.get(),
					params[0]);
			if (seri == null) {
				return null;
			} else {
				return (MyInformation) seri;
			}
		}

		@Override
		protected void onPostExecute(MyInformation info) {
			super.onPostExecute(info);
			if (info != null) {
				mUser = info.getUser();
				fillUI();
				mEmptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
			} else {
				mEmptyView.setErrorType(EmptyLayout.NETWORK_ERROR);
			}
		}
	}

	private class SaveCacheTask extends AsyncTask<Void, Void, Void> {
		private WeakReference<Context> mContext;
		private Serializable seri;
		private String key;

		private SaveCacheTask(Context context, Serializable seri, String key) {
			mContext = new WeakReference<>(context);
			this.seri = seri;
			this.key = key;
		}

		@Override
		protected Void doInBackground(Void... params) {
			CacheManager.saveObject(mContext.get(), seri, key);
			return null;
		}
	}
}
