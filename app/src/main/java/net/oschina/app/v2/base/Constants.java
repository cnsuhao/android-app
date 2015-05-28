package net.oschina.app.v2.base;

import android.os.Environment;

public class Constants {
    public static final String INTENT_ACTION_LOGOUT = "com.tonlin.osc.happy.LOGOUT";
    public static final String INTENT_ACTION_COMMENT_CHANGED = "com.tonlin.osc.happy.COMMENT_CHANGED";
    public static final String INTENT_ACTION_NOTICE = "com.tonlin.osc.happy.action.APPWIDGET_UPDATE";

    public final static String BASE_DIR = Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/HappyOSC/";

    public final static String CACHE_DIR = BASE_DIR + ".cache/";

    public final static String IMAGE_SAVE_PAHT = BASE_DIR + "download_images";

    public static final String WEICHAT_APPID = "wx7aac2075450f71a2";
    public static final String WEICHAT_SECRET = "0101b0595ffe2042c214420fac358abc";

    public static final String QQ_APPID = "100424468";
    public static final String QQ_APPKEY = "c7394704798a158208a74ab60104f0ba";

    public static final long CACHE_EXPIRE_OND_DAY = 3600000;//一天




    public static final String NEW_FRIENDS_USERNAME = "item_new_friends";
    public static final String GROUP_USERNAME = "item_groups";
    public static final String CHAT_ROOM = "item_chatroom";
    public static final String MESSAGE_ATTR_IS_VOICE_CALL = "is_voice_call";
    public static final String MESSAGE_ATTR_IS_VIDEO_CALL = "is_video_call";
    public static final String ACCOUNT_REMOVED = "account_removed";
}
