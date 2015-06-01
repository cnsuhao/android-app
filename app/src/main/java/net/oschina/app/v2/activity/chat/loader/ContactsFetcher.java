package net.oschina.app.v2.activity.chat.loader;

import android.util.Log;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.model.chat.IMUser;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by Sim on 2015/1/26.
 */
public class ContactsFetcher extends NameWorker {

    private static final String TAG = ContactsFetcher.class.getSimpleName();

    private volatile static ContactsFetcher instance;

    /**
     * Returns singleton class instance
     */
    public static ContactsFetcher getInstance() {
        if (instance == null) {
            synchronized (ContactsFetcher.class) {
                if (instance == null) {
                    instance = new ContactsFetcher();
                }
            }
        }
        return instance;
    }

    protected ContactsFetcher() {
        init();
    }

    private void init() {
    }

    private IName processName(String data, CacheType cacheType) {
        return null;
    }

    @Override
    protected IName processName(Object data, CacheType type) {
        return processName((String) data, type);
    }

    @Override
    protected void queryValueFromRemoteDB(final String data, CacheType cacheType,
                                         final INameCache mNameCache, final AwareView awareView) {
        if (CacheType.USER.equals(cacheType)) {
            Log.e(TAG,"queryValueFromRemoteDB:"+data);
            BmobQuery<IMUser> query = new BmobQuery<IMUser>();
            query.addWhereEqualTo("imUserName",data);
            query.findObjects(AppContext.context(), new FindListener<IMUser>() {
                @Override
                public void onSuccess(List<IMUser> list) {
                    Log.e(TAG,"查询用户成功");
                    if (list != null && list.size() > 0) {
                        Log.e(TAG, "存在用户准备赋值:" + list.get(0).getName());
                        executeOnQueryRemoteSuccess(list.get(0), awareView);
                        //save to cache
                        if(mNameCache != null){
                            mNameCache.addBitmapToCache(data,list.get(0));
                        }
                    }
                }

                @Override
                public void onError(int i, String s) {
                    Log.e(TAG,"查询用户失败");
                }
            });
        }
    }
}
