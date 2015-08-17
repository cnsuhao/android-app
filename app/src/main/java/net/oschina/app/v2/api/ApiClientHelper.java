package net.oschina.app.v2.api;

import net.oschina.app.v2.AppContext;

public class ApiClientHelper {

	public static String getUserAgent(AppContext appContext) {
		StringBuilder ua = new StringBuilder("OSChina.NET");
		ua.append('/' + appContext.getPackageInfo().versionName + '_'
				+ "47");// App版本 appContext.getPackageInfo().versionCode
		ua.append("/Android");// 手机系统平台
		ua.append("/" + android.os.Build.VERSION.RELEASE);// 手机系统版本
		ua.append("/" + android.os.Build.MODEL); // 手机型号
		ua.append("/" + appContext.getAppId());// 客户端唯一标识
		return "OSChina.NET/2.4_47/Android/5.0.1/M040/d673452c-da9d-4f4a-998c-b2239cd7048b";//ua.toString();
	}
}
