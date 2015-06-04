package net.oschina.app.v2.activity.chat;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.activity.chat.fragment.MessageFragment;
import net.oschina.app.v2.activity.chat.view.AsynTextView;
import net.oschina.app.v2.base.BaseActivity;

/**
 * Created by Tonlin on 2015/5/28.
 */
public class MessageActivity extends BaseActivity {
    public static final String KEY_TO_CHAT_NAME = "key_to_chat_user_name";
    public static final String KEY_CHAT_NICK = "key_chat_nick";
    public static final String KEY_CHAT_TYPE = "key_chat_type";
    private AsynTextView mTvTitle;

    @Override
    protected boolean hasBackButton() {
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.v2_activity_simple_fragment;
    }

    @Override
    protected int getActionBarCustomView() {
        return R.layout.v2_actionbar_custom_chat_message;
    }

    @Override
    protected void initActionBar(Toolbar actionBar) {
        super.initActionBar(actionBar);
        mTvTitle = (AsynTextView) actionBar.findViewById(R.id.tv_title);
        String nick = getIntent().getStringExtra(KEY_CHAT_NICK);
        String uid = getIntent().getStringExtra(KEY_TO_CHAT_NAME);
        int type = getIntent().getIntExtra(KEY_CHAT_TYPE, MessageFragment.CHATTYPE_SINGLE);
        if (TextUtils.isEmpty(nick)) {
            loadNickName(nick, uid, type);
        } else {
            setActionBarTitle(nick);
        }
    }

    private void loadNickName(String nick, String uid, int type) {

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
