package net.oschina.app.v2.api;

import net.oschina.app.AppContext;

public class ApiClientHelper {

	public static String getUserAgent(AppContext appContext) {
		StringBuilder ua = new StringBuilder("OSChina.NET");
		ua.append('/' + appContext.getPackageInfo().versionName + '_'
				+ appContext.getPackageInfo().versionCode);// App版本
		ua.append("/Android(悦览版)");// 手机系统平台
		ua.append("/" + android.os.Build.VERSION.RELEASE);// 手机系统版本
		ua.append("/" + android.os.Build.MODEL); // 手机型号
		ua.append("/" + appContext.getAppId());// 客户端唯一标识
		return ua.toString();
	}
}
