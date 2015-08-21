package net.oschina.app.v2.activity;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.model.DailyEnglish;
import net.oschina.app.v2.model.Version;
import net.oschina.app.v2.utils.StringUtils;
import net.oschina.app.v2.utils.SystemBarTintManager;
import net.oschina.app.v2.utils.TDevice;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.TextView;

import com.tonlin.osc.happy.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

/**
 * 应用程序启动类：显示欢迎界面并跳转到主界面
 * 
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class SplashActivity extends Activity {

	private static final String SPLASH_SCREEN = "SplashScreen";
	public static final int MAX_WATTING_TIME = 3000;// 停留时间3秒
	protected boolean mShouldGoTo = true;

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(SPLASH_SCREEN); // 统计页面
		MobclickAgent.onResume(this); // 统计时长
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(SPLASH_SCREEN); // 保证 onPageEnd 在onPause
		MobclickAgent.onPause(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(TDevice.isServiceRunning(this, "net.oschina.app.v2.service.NoticeService")){
			redirectTo();
			return;
		}

		//checkUpdate();
		final View view = View.inflate(this, R.layout.v2_activity_splash, null);
		setContentView(view);

		// 渐变展示启动屏
//		AlphaAnimation aa = new AlphaAnimation(1f, 1.0f);
//		aa.setDuration(MAX_WATTING_TIME);
//		view.startAnimation(aa);
//		aa.setAnimationListener(new AnimationListener() {
//
//			@Override
//			public void onAnimationEnd(Animation arg0) {
//				if (mShouldGoTo) {
//					redirectTo();
//				}
//			}
//
//			@Override
//			public void onAnimationRepeat(Animation animation) {
//			}
//
//			@Override
//			public void onAnimationStart(Animation animation) {
//			}
//		});

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				redirectTo();
			}
		},MAX_WATTING_TIME);

		AppContext.requestDailyEnglish();
		DailyEnglish de = AppContext.getDailyEnglish();
		if (de != null) {
			TextView tvContent = (TextView) findViewById(R.id.tv_eng);
			tvContent.setText(de.getContent());
			TextView tvNote = (TextView) findViewById(R.id.tv_note);
			tvNote.setText(de.getNote());
		}

		View root = findViewById(R.id.app_start_view);
		if (root != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
				&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			setTranslucentStatus(true);
			SystemBarTintManager tintManager = new SystemBarTintManager(this);
			tintManager.setStatusBarTintEnabled(true);
			tintManager.setStatusBarTintResource(R.color.transparent);
		}
	}

	@TargetApi(19)
	protected void setTranslucentStatus(boolean on) {
		Window win = getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		if (on) {
			winParams.flags |= bits;
		} else {
			winParams.flags &= ~bits;
		}
		win.setAttributes(winParams);
	}

	private void redirectTo() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}
}