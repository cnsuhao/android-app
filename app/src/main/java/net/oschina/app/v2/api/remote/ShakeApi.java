package net.oschina.app.v2.api.remote;

import com.loopj.android.http.AsyncHttpResponseHandler;

import net.oschina.app.v2.api.ApiHttpClient;

/**
 * Created by Tonlin on 2015/8/15.
 */
public class ShakeApi extends BaseApi {

    /**
     * 摇一摇，随机数据
     *
     * @param handler
     */
    public static void shake(AsyncHttpResponseHandler handler) {
        shake(-1, handler);
    }

    /**
     * 摇一摇指定请求类型
     */
    public static void shake(int type, AsyncHttpResponseHandler handler) {
        String inter = "action/api/rock_rock";
        if (type > 0) {
            inter = (inter + "/?type=" + type);
        }
        ApiHttpClient.get(inter, handler);
    }
}
