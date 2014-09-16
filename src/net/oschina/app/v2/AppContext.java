package net.oschina.app.v2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Properties;
import java.util.UUID;

import net.oschina.app.v2.api.ApiHttpClient;
import net.oschina.app.v2.base.BaseApplication;
import net.oschina.app.v2.base.Constants;
import net.oschina.app.v2.emoji.EmojiHelper;
import net.oschina.app.v2.model.User;
import net.oschina.app.v2.utils.CyptoUtils;
import net.oschina.app.v2.utils.FileUtils;
import net.oschina.app.v2.utils.ImageUtils;
import net.oschina.app.v2.utils.MethodsCompat;
import net.oschina.app.v2.utils.StringUtils;
import net.oschina.app.v2.utils.UIHelper;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.CacheManager;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.tonlin.osc.happy.R;
import com.umeng.analytics.MobclickAgent;

/**
 * 全局应用程序类：用于保存和调用全局应用配置及访问网络数据
 * 
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class AppContext extends BaseApplication {

	public static final int NETTYPE_WIFI = 0x01;
	public static final int NETTYPE_CMWAP = 0x02;
	public static final int NETTYPE_CMNET = 0x03;

	public static final int PAGE_SIZE = 20;// 默认分页大小
	private static final int CACHE_TIME = 60 * 60000;// 缓存失效时间
	private static final String KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN";
	private static final String KEY_SOFTKEYBOARD_HEIGHT = "KEY_SOFTKEYBOARD_HEIGHT";
	private static final String KEY_LOAD_IMAGE = "KEY_LOAD_IMAGE";
	private static final String LAST_QUESTION_CATEGORY_IDX = "LAST_QUESTION_CATEGORY_IDX";

	private boolean login = false; // 登录状态
	private int loginUid = 0; // 登录用户的id
	private Hashtable<String, Object> memCacheRegion = new Hashtable<String, Object>();

	private String saveImagePath;// 保存图片路径

	private Handler unLoginHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				UIHelper.ToastMessage(AppContext.this,
						getString(R.string.msg_login_error));
				// UIHelper.showLoginDialog(AppContext.this);
			}
		}
	};
	private static AppContext instance;

	@Override
	public void onCreate() {
		super.onCreate();
		// 注册App异常崩溃处理器
		// Thread.setDefaultUncaughtExceptionHandler(AppException.getAppExceptionHandler());

		instance = this;

		init2();
		AsyncHttpClient client = new AsyncHttpClient();
		PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
		client.setCookieStore(myCookieStore);
		ApiHttpClient.setHttpClient(client);
		ApiHttpClient.setCookie(ApiHttpClient.getCookie(this));

		EmojiHelper.initEmojis();
		initImageLoader(this);

		MobclickAgent.setDebugMode(true);
	}

	public static void initImageLoader(Context context) {
		DisplayImageOptions displayOptions = new DisplayImageOptions.Builder()
				.preProcessor(new BitmapProcessor() {

					@Override
					public Bitmap process(Bitmap source) {
						Bitmap target = getRoundedCornerBitmapBig(source);
						if (source != target) {
							source.recycle();
						}
						return target;
					}
				}).cacheInMemory(true).cacheOnDisk(true)
				.bitmapConfig(Config.ARGB_8888).build();
		// This configuration tuning is custom. You can tune every option, you
		// may tune some of them,
		// or you can create default configuration by
		// ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context).threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.diskCacheSize(50 * 1024 * 1024)
				// 50 Mb
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs() // Remove for release app
				.defaultDisplayImageOptions(displayOptions).build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}

	public static Bitmap getRoundedCornerBitmapBig(Bitmap bitmap) {
		Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(outBitmap);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPX = bitmap.getWidth() / 2;
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPX, roundPX, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return outBitmap;
	}

	public static void setSoftKeyboardHeight(int height) {
		Editor editor = getPreferences().edit();
		editor.putInt(KEY_SOFTKEYBOARD_HEIGHT, height);
		apply(editor);
	}

	public static int getSoftKeyboardHeight() {
		return getPreferences().getInt(KEY_SOFTKEYBOARD_HEIGHT, 0);
	}

	public static void setAccessToken(String token) {
		Editor editor = getPreferences().edit();
		editor.putString(KEY_ACCESS_TOKEN, token);
		apply(editor);
	}

	public static String getAccessToken() {
		return getPreferences().getString(KEY_ACCESS_TOKEN, null);
	}

	public static boolean shouldLoadImage() {
		return getPreferences().getBoolean(KEY_LOAD_IMAGE, true);
	}

	public static void setLoadImage(boolean flag) {
		Editor editor = getPreferences().edit();
		editor.putBoolean(KEY_LOAD_IMAGE, flag);
		apply(editor);
	}

	/**
	 * 初始化
	 */
	private void init2() {
		// 设置保存图片的路径
		saveImagePath = getProperty(AppConfig.SAVE_IMAGE_PATH);
		if (StringUtils.isEmpty(saveImagePath)) {
			setProperty(AppConfig.SAVE_IMAGE_PATH,
					AppConfig.DEFAULT_SAVE_IMAGE_PATH);
			saveImagePath = AppConfig.DEFAULT_SAVE_IMAGE_PATH;
		}
	}

	/**
	 * 检测当前系统声音是否为正常模式
	 * 
	 * @return
	 */
	public boolean isAudioNormal() {
		AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		return mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
	}

	/**
	 * 应用程序是否发出提示音
	 * 
	 * @return
	 */
	public boolean isAppSound() {
		return isAudioNormal() && isVoice();
	}

	/**
	 * 检测网络是否可用
	 * 
	 * @return
	 */
	public boolean isNetworkConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}

	/**
	 * 获取当前网络类型
	 * 
	 * @return 0：没有网络 1：WIFI网络 2：WAP网络 3：NET网络
	 */
	public int getNetworkType() {
		int netType = 0;
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo == null) {
			return netType;
		}
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE) {
			String extraInfo = networkInfo.getExtraInfo();
			if (!StringUtils.isEmpty(extraInfo)) {
				if (extraInfo.toLowerCase().equals("cmnet")) {
					netType = NETTYPE_CMNET;
				} else {
					netType = NETTYPE_CMWAP;
				}
			}
		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			netType = NETTYPE_WIFI;
		}
		return netType;
	}

	/**
	 * 判断当前版本是否兼容目标版本的方法
	 * 
	 * @param VersionCode
	 * @return
	 */
	public static boolean isMethodsCompat(int VersionCode) {
		int currentVersion = android.os.Build.VERSION.SDK_INT;
		return currentVersion >= VersionCode;
	}

	/**
	 * 获取App安装包信息
	 * 
	 * @return
	 */
	public PackageInfo getPackageInfo() {
		PackageInfo info = null;
		try {
			info = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace(System.err);
		}
		if (info == null)
			info = new PackageInfo();
		return info;
	}

	/**
	 * 获取App唯一标识
	 * 
	 * @return
	 */
	public String getAppId() {
		String uniqueID = getProperty(AppConfig.CONF_APP_UNIQUEID);
		if (StringUtils.isEmpty(uniqueID)) {
			uniqueID = UUID.randomUUID().toString();
			setProperty(AppConfig.CONF_APP_UNIQUEID, uniqueID);
		}
		return uniqueID;
	}

	/**
	 * 用户是否登录
	 * 
	 * @return
	 */
	public boolean isLogin() {
		return login;
	}

	/**
	 * 获取登录用户id
	 * 
	 * @return
	 */
	public int getLoginUid() {
		return this.loginUid;
	}

	/**
	 * 用户注销
	 */
	public void Logout() {
		ApiHttpClient.cleanCookie();
		this.cleanCookie();
		this.login = false;
		this.loginUid = 0;

		Intent intent = new Intent(Constants.INTENT_ACTION_LOGOUT);
		sendBroadcast(intent);
	}

	/**
	 * 未登录或修改密码后的处理
	 */
	public Handler getUnLoginHandler() {
		return this.unLoginHandler;
	}

	/**
	 * 初始化用户登录信息
	 */
	public void initLoginInfo() {
		User loginUser = getLoginInfo();
		if (loginUser != null && loginUser.getUid() > 0
				&& !TextUtils.isEmpty(ApiHttpClient.getCookie(this))) {// &&
																		// loginUser.isRememberMe()
			this.loginUid = loginUser.getUid();
			this.login = true;
		} else {
			this.Logout();
		}
	}

	/**
	 * 保存登录信息
	 * 
	 * @param username
	 * @param pwd
	 */
	public void saveLoginInfo(final User user) {
		this.loginUid = user.getUid();
		this.login = true;
		setProperties(new Properties() {
			{
				setProperty("user.uid", String.valueOf(user.getUid()));
				setProperty("user.name", user.getName());
				setProperty("user.face", FileUtils.getFileName(user.getFace()));// 用户头像-文件名
				setProperty("user.account", user.getAccount());
				setProperty("user.pwd",
						CyptoUtils.encode("oschinaApp", user.getPwd()));
				setProperty("user.location", user.getLocation());
				setProperty("user.followers",
						String.valueOf(user.getFollowers()));
				setProperty("user.fans", String.valueOf(user.getFans()));
				setProperty("user.score", String.valueOf(user.getScore()));
				setProperty("user.isRememberMe",
						String.valueOf(user.isRememberMe()));// 是否记住我的信息
			}
		});
	}

	/**
	 * 清除登录信息
	 */
	public void cleanLoginInfo() {
		this.loginUid = 0;
		this.login = false;
		removeProperty("user.uid", "user.name", "user.face", "user.account",
				"user.pwd", "user.location", "user.followers", "user.fans",
				"user.score", "user.isRememberMe");
	}

	/**
	 * 获取登录信息
	 * 
	 * @return
	 */
	public User getLoginInfo() {
		User lu = new User();
		lu.setUid(StringUtils.toInt(getProperty("user.uid"), 0));
		lu.setName(getProperty("user.name"));
		lu.setFace(getProperty("user.face"));
		lu.setAccount(getProperty("user.account"));
		lu.setPwd(CyptoUtils.decode("oschinaApp", getProperty("user.pwd")));
		lu.setLocation(getProperty("user.location"));
		lu.setFollowers(StringUtils.toInt(getProperty("user.followers"), 0));
		lu.setFans(StringUtils.toInt(getProperty("user.fans"), 0));
		lu.setScore(StringUtils.toInt(getProperty("user.score"), 0));
		lu.setRememberMe(StringUtils.toBool(getProperty("user.isRememberMe")));
		return lu;
	}

	/**
	 * 保存用户头像
	 * 
	 * @param fileName
	 * @param bitmap
	 */
	public void saveUserFace(String fileName, Bitmap bitmap) {
		try {
			ImageUtils.saveImage(this, fileName, bitmap);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取用户头像
	 * 
	 * @param key
	 * @return
	 * @throws AppException
	 */
	public Bitmap getUserFace(String key) throws AppException {
		FileInputStream fis = null;
		try {
			fis = openFileInput(key);
			return BitmapFactory.decodeStream(fis);
		} catch (Exception e) {
			throw AppException.run(e);
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 是否加载显示文章图片
	 * 
	 * @return
	 */
	@Deprecated
	public boolean isLoadImage() {
		String perf_loadimage = getProperty(AppConfig.CONF_LOAD_IMAGE);
		// 默认是加载的
		if (StringUtils.isEmpty(perf_loadimage))
			return true;
		else
			return StringUtils.toBool(perf_loadimage);
	}

	/**
	 * 设置是否加载文章图片
	 * 
	 * @param b
	 */
	@Deprecated
	public void setConfigLoadimage(boolean b) {
		setProperty(AppConfig.CONF_LOAD_IMAGE, String.valueOf(b));
	}

	/**
	 * 是否发出提示音
	 * 
	 * @return
	 */
	public boolean isVoice() {
		String perf_voice = getProperty(AppConfig.CONF_VOICE);
		// 默认是开启提示声音
		if (StringUtils.isEmpty(perf_voice))
			return true;
		else
			return StringUtils.toBool(perf_voice);
	}

	/**
	 * 设置是否发出提示音
	 * 
	 * @param b
	 */
	public void setConfigVoice(boolean b) {
		setProperty(AppConfig.CONF_VOICE, String.valueOf(b));
	}

	/**
	 * 是否启动检查更新
	 * 
	 * @return
	 */
	public boolean isCheckUp() {
		String perf_checkup = getProperty(AppConfig.CONF_CHECKUP);
		// 默认是开启
		if (StringUtils.isEmpty(perf_checkup))
			return true;
		else
			return StringUtils.toBool(perf_checkup);
	}

	/**
	 * 设置启动检查更新
	 * 
	 * @param b
	 */
	public void setConfigCheckUp(boolean b) {
		setProperty(AppConfig.CONF_CHECKUP, String.valueOf(b));
	}

	/**
	 * 是否左右滑动
	 * 
	 * @return
	 */
	public boolean isScroll() {
		String perf_scroll = getProperty(AppConfig.CONF_SCROLL);
		// 默认是关闭左右滑动
		if (StringUtils.isEmpty(perf_scroll))
			return false;
		else
			return StringUtils.toBool(perf_scroll);
	}

	/**
	 * 设置是否左右滑动
	 * 
	 * @param b
	 */
	public void setConfigScroll(boolean b) {
		setProperty(AppConfig.CONF_SCROLL, String.valueOf(b));
	}

	/**
	 * 是否Https登录
	 * 
	 * @return
	 */
	public boolean isHttpsLogin() {
		String perf_httpslogin = getProperty(AppConfig.CONF_HTTPS_LOGIN);
		// 默认是http
		if (StringUtils.isEmpty(perf_httpslogin))
			return false;
		else
			return StringUtils.toBool(perf_httpslogin);
	}

	/**
	 * 设置是是否Https登录
	 * 
	 * @param b
	 */
	public void setConfigHttpsLogin(boolean b) {
		setProperty(AppConfig.CONF_HTTPS_LOGIN, String.valueOf(b));
	}

	/**
	 * 清除保存的缓存
	 */
	public void cleanCookie() {
		removeProperty(AppConfig.CONF_COOKIE);
	}

	/**
	 * 判断缓存数据是否可读
	 * 
	 * @param cachefile
	 * @return
	 */
	private boolean isReadDataCache(String cachefile) {
		return readObject(cachefile) != null;
	}

	/**
	 * 判断缓存是否存在
	 * 
	 * @param cachefile
	 * @return
	 */
	private boolean isExistDataCache(String cachefile) {
		boolean exist = false;
		File data = getFileStreamPath(cachefile);
		if (data.exists())
			exist = true;
		return exist;
	}

	/**
	 * 判断缓存是否失效
	 * 
	 * @param cachefile
	 * @return
	 */
	public boolean isCacheDataFailure(String cachefile) {
		boolean failure = false;
		File data = getFileStreamPath(cachefile);
		if (data.exists()
				&& (System.currentTimeMillis() - data.lastModified()) > CACHE_TIME)
			failure = true;
		else if (!data.exists())
			failure = true;
		return failure;
	}

	/**
	 * 清除app缓存
	 */
	public void clearAppCache() {
		// 清除webview缓存
		File file = CacheManager.getCacheFileBaseDir();
		if (file != null && file.exists() && file.isDirectory()) {
			for (File item : file.listFiles()) {
				item.delete();
			}
			file.delete();
		}
		deleteDatabase("webview.db");
		deleteDatabase("webview.db-shm");
		deleteDatabase("webview.db-wal");
		deleteDatabase("webviewCache.db");
		deleteDatabase("webviewCache.db-shm");
		deleteDatabase("webviewCache.db-wal");
		// 清除数据缓存
		clearCacheFolder(getFilesDir(), System.currentTimeMillis());
		clearCacheFolder(getCacheDir(), System.currentTimeMillis());
		// 2.2版本才有将应用缓存转移到sd卡的功能
		if (isMethodsCompat(android.os.Build.VERSION_CODES.FROYO)) {
			clearCacheFolder(MethodsCompat.getExternalCacheDir(this),
					System.currentTimeMillis());
		}
		// 清除编辑器保存的临时内容
		Properties props = getProperties();
		for (Object key : props.keySet()) {
			String _key = key.toString();
			if (_key.startsWith("temp"))
				removeProperty(_key);
		}
	}

	/**
	 * 清除缓存目录
	 * 
	 * @param dir
	 *            目录
	 * @param numDays
	 *            当前系统时间
	 * @return
	 */
	private int clearCacheFolder(File dir, long curTime) {
		int deletedFiles = 0;
		if (dir != null && dir.isDirectory()) {
			try {
				for (File child : dir.listFiles()) {
					if (child.isDirectory()) {
						deletedFiles += clearCacheFolder(child, curTime);
					}
					if (child.lastModified() < curTime) {
						if (child.delete()) {
							deletedFiles++;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return deletedFiles;
	}

	/**
	 * 将对象保存到内存缓存中
	 * 
	 * @param key
	 * @param value
	 */
	public void setMemCache(String key, Object value) {
		memCacheRegion.put(key, value);
	}

	/**
	 * 从内存缓存中获取对象
	 * 
	 * @param key
	 * @return
	 */
	public Object getMemCache(String key) {
		return memCacheRegion.get(key);
	}

	/**
	 * 保存磁盘缓存
	 * 
	 * @param key
	 * @param value
	 * @throws IOException
	 */
	public void setDiskCache(String key, String value) throws IOException {
		FileOutputStream fos = null;
		try {
			fos = openFileOutput("cache_" + key + ".data", Context.MODE_PRIVATE);
			fos.write(value.getBytes());
			fos.flush();
		} finally {
			try {
				fos.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 获取磁盘缓存数据
	 * 
	 * @param key
	 * @return
	 * @throws IOException
	 */
	public String getDiskCache(String key) throws IOException {
		FileInputStream fis = null;
		try {
			fis = openFileInput("cache_" + key + ".data");
			byte[] datas = new byte[fis.available()];
			fis.read(datas);
			return new String(datas);
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 保存对象
	 * 
	 * @param ser
	 * @param file
	 * @throws IOException
	 */
	public boolean saveObject(Serializable ser, String file) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = openFileOutput(file, MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(ser);
			oos.flush();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				oos.close();
			} catch (Exception e) {
			}
			try {
				fos.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 读取对象
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public Serializable readObject(String file) {
		if (!isExistDataCache(file))
			return null;
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = openFileInput(file);
			ois = new ObjectInputStream(fis);
			return (Serializable) ois.readObject();
		} catch (FileNotFoundException e) {
		} catch (Exception e) {
			e.printStackTrace();
			// 反序列化失败 - 删除缓存文件
			if (e instanceof InvalidClassException) {
				File data = getFileStreamPath(file);
				data.delete();
			}
		} finally {
			try {
				ois.close();
			} catch (Exception e) {
			}
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
		return null;
	}

	public boolean containsProperty(String key) {
		Properties props = getProperties();
		return props.containsKey(key);
	}

	public void setProperties(Properties ps) {
		AppConfig.getAppConfig(this).set(ps);
	}

	public Properties getProperties() {
		return AppConfig.getAppConfig(this).get();
	}

	public void setProperty(String key, String value) {
		AppConfig.getAppConfig(this).set(key, value);
	}

	public String getProperty(String key) {
		return AppConfig.getAppConfig(this).get(key);
	}

	public void removeProperty(String... key) {
		AppConfig.getAppConfig(this).remove(key);
	}

	/**
	 * 获取内存中保存图片的路径
	 * 
	 * @return
	 */
	public String getSaveImagePath() {
		return saveImagePath;
	}

	/**
	 * 设置内存中保存图片的路径
	 * 
	 * @return
	 */
	public void setSaveImagePath(String saveImagePath) {
		this.saveImagePath = saveImagePath;
	}

	public static AppContext instance() {
		return instance;
	}

	public static int getLastQuestionCategoryIdx() {
		return getPreferences().getInt(LAST_QUESTION_CATEGORY_IDX, 0);
	}

	public static String getLastQuestionCategory() {
		int idx = getLastQuestionCategoryIdx();
		return resources().getStringArray(R.array.post_pub_options)[idx];
	}
}
