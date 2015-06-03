package net.oschina.app.v2.activity.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.easemob.EMCallBack;
import com.easemob.EMError;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.base.Constants;
import net.oschina.app.v2.model.User;
import net.oschina.app.v2.model.chat.IMUser;
import net.oschina.app.v2.utils.MD5Utils;
import net.oschina.app.v2.utils.TLog;
import net.oschina.app.v2.utils.WeakAsyncTask;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

/**
 * Created by Tonlin on 2015/6/3.
 */
public class ChatHelper {

    private static final java.lang.String TAG = "ChatHelper";
    private static final int MSG_LOGIN_IM_SUC = 1;
    private static final int MSG_LOGIN_IM_FAILED = 2;
    private static ChatHelper mInstance;
    private Context mContext;
    private Handler mHandler;

    private List<AsyncAccountCallback> mCallbacks = new ArrayList<>();

    public interface AsyncAccountCallback {
        void onLoginSuccess();

        void onNeedLogin();

        void onLoginFailed(String msg);
    }

    public static synchronized ChatHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ChatHelper(context);
        }
        return mInstance;
    }

    public void registerCallback(AsyncAccountCallback callback) {
        mCallbacks.add(callback);
    }

    public void unregisterCallback(AsyncAccountCallback callback) {
        mCallbacks.remove(callback);
    }

    private ChatHelper(final Context context) {
        mContext = context;
        mHandler = new Handler(mContext.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_LOGIN_IM_SUC:
                        IMUser u = (IMUser) msg.obj;
                        EMGroupManager.getInstance().loadAllGroups();
                        EMChatManager.getInstance().loadAllConversations();

                        // 登陆成功，保存用户名密码
                        AppContext.instance().setHXUserName(u.getImUserName());
                        AppContext.instance().setHXPassword(u.getImPassword());

                        notifyLoginSuccess();
                        break;
                    case MSG_LOGIN_IM_FAILED:
                        notifyLoginFailed((String) msg.obj);
                        break;
                }
            }
        };
    }


    public static boolean needLogin() {
        User user = AppContext.getLoginInfo();
        return needLogin(user);
    }

    public static boolean needLogin(User user) {
        return !AppContext.instance().isLogin()
                || user == null || TextUtils.isEmpty(user.getAccount())
                || TextUtils.isEmpty(user.getPwd())
                || IMUser.getCurrentUser(AppContext.context()) == null
                || !AppContext.hasHXLogin();
    }

    public void asyncAccount() {
        final User user = AppContext.getLoginInfo();
        if (!AppContext.instance().isLogin()
                || user == null || TextUtils.isEmpty(user.getAccount())
                || TextUtils.isEmpty(user.getPwd())) {
            notifyNeedLogin();
        } else {
            // 检查IM和DB是否都登陆了
            IMUser iu = IMUser.getCurrentUser(mContext, IMUser.class);
            if (iu != null && AppContext.hasHXLogin()) {
                TLog.log(TAG, "DB和IM都登陆了.可以正常获取数据了");
                notifyLoginSuccess();
                return;
            }
            // 检查是否注册过
            BmobQuery<IMUser> u = new BmobQuery<>();
            u.addWhereEqualTo("username", user.getAccount());
            u.findObjects(mContext, new FindListener<IMUser>() {
                @Override
                public void onSuccess(List<IMUser> list) {
                    if (list != null && list.size() > 0) {
                        IMUser u = list.get(0);
                        u.setUsername(user.getAccount());
                        u.setPassword(user.getPwd());
                        handlerUserRealExist(u);
                    } else {
                        TLog.log(TAG, "没有找到用户准备注册IM用户。");
                        new RegisterIMTask(ChatHelper.this, user).execute();
                    }
                }

                @Override
                public void onError(int i, String s) {
                    AppContext.showToastShort("没有找到用户,查询失败");
                }
            });
        }
    }

    private void handlerUserRealExist(final IMUser u) {
        IMUser uu = IMUser.getCurrentUser(mContext, IMUser.class);
        if (uu != null) {
            TLog.log(TAG, "DB 用户已登录.准备登陆IM");
            loginIMServer(u);
        } else {
            TLog.log("DB 用户未登录准备登陆DB");
            u.login(mContext, new SaveListener() {
                @Override
                public void onSuccess() {
                    TLog.log(TAG, "登陆成功 DB,准备登陆IM");
                    loginIMServer(u);
                }

                @Override
                public void onFailure(int i, String s) {
                    TLog.log(TAG, "登陆DB失败." + s);
                }
            });
        }
    }

    private void loginIMServer(final IMUser u) {
        EMChatManager.getInstance().login(u.getImUserName(),
                u.getImPassword(), new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        TLog.log(TAG, "登陆IM成功:" + u.getUsername());
                        mHandler.obtainMessage(MSG_LOGIN_IM_SUC, u).sendToTarget();
                    }

                    @Override
                    public void onError(int i, String s) {
                        TLog.log(TAG, "登陆IM失败:" + s);
                        mHandler.obtainMessage(MSG_LOGIN_IM_FAILED, s).sendToTarget();
                    }

                    @Override
                    public void onProgress(int i, String s) {
                    }
                });
    }

    private void registerDB(final IMUser user) {
        user.signUp(mContext, new SaveListener() {
            @Override
            public void onSuccess() {
                TLog.log(TAG, "注册DB成功:" + user.getName());
                handlerUserRealExist(user);
            }

            @Override
            public void onFailure(int i, String s) {
                Log.d(TAG, "注册失败 DB");
            }
        });
    }

    public static class RegisterIMTask extends WeakAsyncTask<Void,
            Void, Integer, ChatHelper> {

        private String imUserName, imPassword;
        private User mUser;

        public RegisterIMTask(ChatHelper fragment,
                              User user) {
            super(fragment);
            mUser = user;
            imUserName = MD5Utils.getMD5Code(mUser.getAccount().toLowerCase());
            imPassword = MD5Utils.getMD5Code(mUser.getPwd().toLowerCase());
        }

        @Override
        protected Integer doInBackground(ChatHelper fragment, Void... params) {
            try {
                // 调用sdk注册方法
                EMChatManager.getInstance().createAccountOnServer(imUserName, imPassword);
                return 0;
            } catch (final EaseMobException e) {
                //注册失败
                return e.getErrorCode();
            }
        }

        @Override
        protected void onPostExecute(ChatHelper fragment, Integer errorCode) {
            super.onPostExecute(fragment, errorCode);
            if (errorCode == 0) {
                IMUser user = new IMUser();
                user.setUsername(mUser.getAccount());
                user.setPassword(mUser.getPwd());
                user.setImUserName(imUserName);
                user.setImPassword(imPassword);
                user.setName(mUser.getName());
                user.setPhoto(mUser.getFace());
                user.setUid(mUser.getUid());
                fragment.registerDB(user);
            } else {
                String msg;
                if (errorCode == EMError.NONETWORK_ERROR) {
                    msg = "网络异常，请检查网络！";
                } else if (errorCode == EMError.USER_ALREADY_EXISTS) {
                    msg = "用户已存在！";
                } else if (errorCode == EMError.UNAUTHORIZED) {
                    msg = "注册失败，无权限！";
                } else {
                    msg = "注册失败: 其他原因";
                }
                fragment.notifyLoginFailed(msg);
            }
        }
    }

    private void notifyLoginSuccess() {
        for (AsyncAccountCallback callback : mCallbacks) {
            callback.onLoginSuccess();
        }
        mContext.sendBroadcast(new Intent(Constants.INTENT_ACTION_LOGIN));
    }

    private void notifyLoginFailed(String msg) {
        for (AsyncAccountCallback callback : mCallbacks) {
            callback.onLoginFailed(msg);
        }
    }

    private void notifyNeedLogin() {
        for (AsyncAccountCallback callback : mCallbacks) {
            callback.onNeedLogin();
        }
    }
}
