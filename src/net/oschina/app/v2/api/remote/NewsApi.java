package net.oschina.app.v2.api.remote;

import net.oschina.app.v2.api.ApiHttpClient;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class NewsApi extends BaseApi {

	/**
	 * 获取新闻列表
	 * 
	 * @param catalog
	 *            类别 （1，2，3）
	 * @param page
	 *            第几页
	 * @param handler
	 */
	public static void getNewsList(int catalog, int page,
			AsyncHttpResponseHandler handler) {
		RequestParams params = new RequestParams();
		// params.put("access_token", "");
		params.put("catalog", catalog);
		params.put("pageIndex", page);
		params.put("pageSize", DEF_PAGE_SIZE);
		params.put("dataType", "json");
		ApiHttpClient.get("action/api/news_list", params, handler);
	}

	public static void getPostList(int catalog, int page,
			AsyncHttpResponseHandler handler) {
		RequestParams params = new RequestParams();
		// params.put("access_token", "");
		params.put("catalog", catalog);
		params.put("pageIndex", page);
		params.put("pageSize", DEF_PAGE_SIZE);
		params.put("dataType", "json");
		ApiHttpClient.get("action/api/post_list", params, handler);
	}

	public static void getTweetList(int uid, int page,
			AsyncHttpResponseHandler handler) {
		RequestParams params = new RequestParams();
		// params.put("access_token", "");
		params.put("uid", uid);
		params.put("pageIndex", page);
		params.put("pageSize", DEF_PAGE_SIZE);
		params.put("dataType", "json");
		ApiHttpClient.get("action/api/tweet_list", params, handler);
	}

	public static void getActiveList(int uid, int catalog, int page,
			AsyncHttpResponseHandler handler) {
		RequestParams params = new RequestParams();
		params.put("uid", uid);
		params.put("catalog", catalog);
		params.put("pageIndex", page);
		params.put("pageSize", DEF_PAGE_SIZE);
		ApiHttpClient.get("action/api/active_list", params, handler);
	}
	
	/**
	 * 获取评论列表
	 * 
	 * @param id
	 * @param catalog
	 *            1新闻 2帖子 3动弹 4动态
	 * @param page
	 * @param handler
	 */
	public static void getCommentList(int id, int catalog, int page,
			AsyncHttpResponseHandler handler) {
		RequestParams params = new RequestParams();
		params.put("catalog", catalog);
		params.put("id", id);
		params.put("pageIndex", page);
		params.put("pageSize", DEF_PAGE_SIZE);
		ApiHttpClient.get("action/api/comment_list", params, handler);
	}

	/**
	 * 获取新闻明细
	 * 
	 * @param newsId
	 * @param handler
	 */
	public static void getNewsDetail(int id, AsyncHttpResponseHandler handler) {
		RequestParams params = new RequestParams("id", id);
		ApiHttpClient.get("action/api/news_detail", params, handler);
	}

	public static void getBlogDetail(int id, AsyncHttpResponseHandler handler) {
		RequestParams params = new RequestParams("id", id);
		ApiHttpClient.get("action/api/blog_detail", params, handler);
	}

	public static void getSoftwareDetail(String ident,
			AsyncHttpResponseHandler handler) {
		RequestParams params = new RequestParams("ident", ident);
		ApiHttpClient.get("action/api/software_detail", params, handler);
	}

	public static void getPostDetail(int id, AsyncHttpResponseHandler handler) {
		RequestParams params = new RequestParams("id", id);
		ApiHttpClient.get("action/api/post_detail", params, handler);
	}

	public static void getTweetDetail(int id, AsyncHttpResponseHandler handler) {
		RequestParams params = new RequestParams("id", id);
		ApiHttpClient.get("action/api/tweet_detail", params, handler);
	}
}
