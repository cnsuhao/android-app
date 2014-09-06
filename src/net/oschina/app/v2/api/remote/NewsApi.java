package net.oschina.app.v2.api.remote;

import java.net.URLEncoder;

import net.oschina.app.bean.Post;
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

	public static void getPostListByTag(String tag, int page,
			AsyncHttpResponseHandler handler) {
		RequestParams params = new RequestParams();
		params.put("tag", tag);
		params.put("pageIndex", page);
		params.put("pageSize", DEF_PAGE_SIZE);
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

	public static void getFriendList(int uid, int relation, int page,
			AsyncHttpResponseHandler handler) {
		RequestParams params = new RequestParams();
		params.put("uid", uid);
		params.put("relation", relation);
		params.put("pageIndex", page);
		params.put("pageSize", DEF_PAGE_SIZE);
		ApiHttpClient.get("action/api/friends_list", params, handler);
	}

	public static void getFavoriteList(int uid, int type, int page,
			AsyncHttpResponseHandler handler) {
		RequestParams params = new RequestParams();
		params.put("uid", uid);
		params.put("type", type);
		params.put("pageIndex", page);
		params.put("pageSize", DEF_PAGE_SIZE);
		ApiHttpClient.get("action/api/favorite_list", params, handler);
	}

	public static void getSoftwareCatalogList(int tag,
			AsyncHttpResponseHandler handler) {
		RequestParams params = new RequestParams("tag", tag);
		ApiHttpClient.get("action/api/softwarecatalog_list", params, handler);
	}

	public static void getSoftwareTagList(int searchTag, int page,
			AsyncHttpResponseHandler handler) {
		RequestParams params = new RequestParams();
		params.put("searchTag", searchTag);
		params.put("pageIndex", page);
		params.put("pageSize", DEF_PAGE_SIZE);
		ApiHttpClient.get("action/api/softwaretag_list", params, handler);
	}

	public static void getSoftwareList(String searchTag, int page,
			AsyncHttpResponseHandler handler) {
		RequestParams params = new RequestParams();
		params.put("searchTag", searchTag);
		params.put("pageIndex", page);
		params.put("pageSize", DEF_PAGE_SIZE);
		ApiHttpClient.get("action/api/software_list", params, handler);
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

	public static void getBlogCommentList(int id, int page,
			AsyncHttpResponseHandler handler) {
		RequestParams params = new RequestParams();
		params.put("id", id);
		params.put("pageIndex", page);
		params.put("pageSize", DEF_PAGE_SIZE);
		ApiHttpClient.get("action/api/blogcomment_list", params, handler);
	}

	public static void getUserInformation(int uid, int hisuid, String hisname,
			int pageIndex, AsyncHttpResponseHandler handler) {
		RequestParams params = new RequestParams();
		params.put("uid", uid);
		params.put("hisuid", hisuid);
		params.put("hisname", hisname);
		params.put("pageIndex", pageIndex);
		params.put("pageSize", DEF_PAGE_SIZE);
		ApiHttpClient.get("action/api/user_information", params, handler);
	}

	@SuppressWarnings("deprecation")
	public static void getUserBlogList(int authoruid, final String authorname,
			final int uid, final int pageIndex, AsyncHttpResponseHandler handler) {
		RequestParams params = new RequestParams();
		params.put("authoruid", authoruid);
		params.put("authorname", URLEncoder.encode(authorname));
		params.put("uid", uid);
		params.put("pageIndex", pageIndex);
		params.put("pageSize", DEF_PAGE_SIZE);
		ApiHttpClient.get("action/api/userblog_list", params, handler);
	}

	public static void updateRelation(int uid, int hisuid, int newrelation,
			AsyncHttpResponseHandler handler) {
		RequestParams params = new RequestParams();
		params.put("uid", uid);
		params.put("hisuid", hisuid);
		params.put("newrelation", newrelation);
		ApiHttpClient.post("action/api/user_updaterelation", params, handler);
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

	public static void publicComment(int catalog, int id, int uid,
			String content, int isPostToMyZone, AsyncHttpResponseHandler handler) {
		RequestParams params = new RequestParams();
		params.put("catalog", catalog);
		params.put("id", id);
		params.put("uid", uid);
		params.put("content", content);
		params.put("isPostToMyZone", isPostToMyZone);
		ApiHttpClient.post("action/api/comment_pub", params, handler);
	}
	
	public static void publicPost(Post post,AsyncHttpResponseHandler handler){
		RequestParams params = new RequestParams();
		params.put("uid", post.getAuthorId());
		params.put("title", post.getTitle());
		params.put("catalog", post.getCatalog());
		params.put("content", post.getBody());
		params.put("isNoticeMe", post.getIsNoticeMe());		
		ApiHttpClient.post("action/api/post_pub", params, handler);
	}
}
