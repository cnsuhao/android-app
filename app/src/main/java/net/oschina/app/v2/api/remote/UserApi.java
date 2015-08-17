package net.oschina.app.v2.api.remote;

import net.oschina.app.v2.api.ApiHttpClient;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class UserApi extends BaseApi {

	public static void login(String username, String password,
			AsyncHttpResponseHandler handler) {
		RequestParams params = new RequestParams();
		params.put("username", username);
		params.put("pwd", password);
		params.put("keep_login", 1);
		
		String loginurl = "action/api/login_validate";
		ApiHttpClient.get(loginurl, params, handler);
	}

	/***
	 * 客户端扫描二维码登陆
	 *
	 * @author 火蚁 2015-3-13 上午11:45:47
	 *
	 * @return void
	 * @param url
	 * @param handler
	 */
	public static void scanQrCodeLogin(String url,
									   AsyncHttpResponseHandler handler) {
		RequestParams params = new RequestParams();
		String uuid = url.substring(url.lastIndexOf("=") + 1);
		params.put("uuid", uuid);
		ApiHttpClient.getDirect(url, handler);
	}

	public static void singnIn(String url, AsyncHttpResponseHandler handler) {
		ApiHttpClient.getDirect(url, handler);
	}
}
