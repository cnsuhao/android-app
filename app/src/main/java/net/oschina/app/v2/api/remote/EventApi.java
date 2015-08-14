package net.oschina.app.v2.api.remote;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.oschina.app.v2.api.ApiHttpClient;
import net.oschina.app.v2.model.EventApplyData;
import net.oschina.app.v2.utils.TDevice;

/**
 * Created by Tonlin on 2015/8/14.
 */
public class EventApi extends BaseApi {

    /**
     * 获取活动列表
     *
     * @param pageIndex
     * @param uid
     *            <= 0 近期活动 实际的用户ID 则获取用户参与的活动列表，需要已登陆的用户
     * @param handler
     */
    public static void getEventList(int pageIndex, int uid,
                                    AsyncHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.put("pageIndex", pageIndex);
        params.put("uid", uid);
        params.put("pageSize", TDevice.getPageSize());
        ApiHttpClient.get("action/api/event_list", params, handler);
    }

    /**
     * 获取某活动已出席的人员列表
     *
     * @param eventId
     * @param pageIndex
     * @param handler
     */
    public static void getEventApplies(int eventId, int pageIndex,
                                       AsyncHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.put("pageIndex", pageIndex);
        params.put("event_id", eventId);
        params.put("pageSize", TDevice.getPageSize());
        ApiHttpClient.get("action/api/event_attend_user", params, handler);
    }

    /**
     * 活动报名
     *
     * @param data
     * @param handler
     */
    public static void eventApply(EventApplyData data,
                                  AsyncHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.put("event", data.getEvent());
        params.put("user", data.getUser());
        params.put("name", data.getName());
        params.put("gender", data.getGender());
        params.put("mobile", data.getPhone());
        params.put("company", data.getCompany());
        params.put("job", data.getJob());
        ApiHttpClient.post("action/api/event_apply", params, handler);
    }
}
