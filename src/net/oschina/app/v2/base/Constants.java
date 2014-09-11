package net.oschina.app.v2.base;

import android.os.Environment;

public class Constants {
	public static final String INTENT_ACTION_LOGOUT = "com.tonlin.osc.happy.LOGOUT";
	public static final String INTENT_ACTION_COMMENT_CHANGED = "com.tonlin.osc.happy.COMMENT_CHANGED";

	public final static String BASE_DIR = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/HappyOSC/";
	
	public final static String IMAGE_SAVE_PAHT = BASE_DIR +"download_images";
}
