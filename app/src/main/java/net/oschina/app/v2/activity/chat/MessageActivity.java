package net.oschina.app.v2.activity.chat;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.activity.chat.fragment.MessageFragment;
import net.oschina.app.v2.base.BaseActivity;

/**
 * Created by Tonlin on 2015/5/28.
 */
public class MessageActivity extends BaseActivity {
    public static final String KEY_TO_CHAT_NAME = "key_to_chat_user_name";
    public static final String KEY_CHAT_NICK = "key_chat_nick";
    public static final String KEY_CHAT_TYPE = "key_chat_type";

    @Override
    protected boolean hasBackButton() {
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.v2_activity_simple_fragment;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        FragmentTransaction trans = getSupportFragmentManager()
                .beginTransaction();
        trans.replace(R.id.container, new MessageFragment());
        trans.commit();
    }
}
