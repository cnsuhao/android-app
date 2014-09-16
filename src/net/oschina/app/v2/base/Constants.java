package net.oschina.app.v2.base;

import android.os.Environment;

public class Constants {
	public static final String INTENT_ACTION_LOGOUT = "com.tonlin.osc.happy.LOGOUT";
	public static final String INTENT_ACTION_COMMENT_CHANGED = "com.tonlin.osc.happy.COMMENT_CHANGED";

	public final static String BASE_DIR = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/HappyOSC/";
	
	public final static String IMAGE_SAVE_PAHT = BASE_DIR +"download_images";
	
	public static final String WEICHAT_APPID = "wx967daebe835fbeac";
	public static final String WEICHAT_SECRET = "5fa9e68ca3970e87a1f83e563c8dcbce";
	
	public static final String QQ_APPID = "100424468";
	public static final String QQ_APPKEY = "c7394704798a158208a74ab60104f0ba";
	
}
