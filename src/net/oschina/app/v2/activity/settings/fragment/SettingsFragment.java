package net.oschina.app.v2.activity.settings.fragment;

import java.io.File;

import net.oschina.app.AppContext;
import net.oschina.app.common.FileUtils;
import net.oschina.app.common.MethodsCompat;
import net.oschina.app.common.UIHelper;
import net.oschina.app.v2.base.BaseFragment;
import net.oschina.app.v2.ui.tooglebutton.ToggleButton;
import net.oschina.app.v2.ui.tooglebutton.ToggleButton.OnToggleChangedListener;
import net.oschina.app.v2.utils.TDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tonlin.osc.happy.R;

public class SettingsFragment extends BaseFragment {

	private TextView mTvPicPath;
	private String mCachePicPath;
	private ToggleButton mTbLoadImage;
	private TextView mTvCacheSize;
	private TextView mTvVersionName;

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.v2_fragment_settings, container,
				false);
		initViews(view);
		initData();
		return view;
	}

	@Override
	public void onResume() {
		mTbLoadImage.setToggle(AppContext.shouldLoadImage());
		super.onResume();
	}

	@SuppressWarnings("deprecation")
	private void initData() {
		mCachePicPath = ImageLoader.getInstance().getDiskCache().getDirectory()
				.getAbsolutePath();

		mTvPicPath.setText(getString(R.string.current_picture_save_path,
				mCachePicPath));
		caculateCacheSize();

		mTvVersionName.setText(TDevice.getVersionName());
	}

	private void initViews(View view) {
		view.findViewById(R.id.ly_about).setOnClickListener(this);
		view.findViewById(R.id.ly_pic_path).setOnClickListener(this);
		mTvPicPath = (TextView) view
				.findViewById(R.id.tv_current_picture_save_path);
		mTbLoadImage = (ToggleButton) view.findViewById(R.id.tb_load_picture);
		mTbLoadImage.setListener(new OnToggleChangedListener() {

			@Override
			public void onToggle(boolean flag) {
				AppContext.setLoadImage(flag);
			}
		});
		view.findViewById(R.id.ly_cache_size).setOnClickListener(this);
		mTvCacheSize = (TextView) view.findViewById(R.id.tv_cache_size);
		mTvVersionName = (TextView) view.findViewById(R.id.tv_version_name);

		view.findViewById(R.id.ly_version_name).setOnClickListener(this);
		view.findViewById(R.id.ly_lisence).setOnClickListener(this);
	}

	private void caculateCacheSize() {
		long fileSize = 0;
		String cacheSize = "0KB";
		File filesDir = getActivity().getFilesDir();
		File cacheDir = getActivity().getCacheDir();

		fileSize += FileUtils.getDirSize(filesDir);
		fileSize += FileUtils.getDirSize(cacheDir);
		// 2.2版本才有将应用缓存转移到sd卡的功能
		if (AppContext.isMethodsCompat(android.os.Build.VERSION_CODES.FROYO)) {
			File externalCacheDir = MethodsCompat
					.getExternalCacheDir(getActivity());
			fileSize += FileUtils.getDirSize(externalCacheDir);
		}
		if (fileSize > 0)
			cacheSize = FileUtils.formatFileSize(fileSize);
		mTvCacheSize.setText(cacheSize);
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		if (id == R.id.ly_pic_path) {
			AppContext.showToastShort(mCachePicPath);
		} else if (id == R.id.ly_about) {
			UIHelper.showAbout(getActivity());
		} else if (id == R.id.ly_cache_size) {
			UIHelper.clearAppCache(getActivity());
			mTvCacheSize.setText("0KB");
		} else if (id == R.id.ly_version_name) {

		} else if (id == R.id.ly_lisence) {
			UIHelper.showLisence(getActivity());
		}
	}
}
