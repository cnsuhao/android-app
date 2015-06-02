package net.oschina.app.v2.activity.chat;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.chat.fragment.AddFriendFragment;
import net.oschina.app.v2.activity.chat.fragment.GroupFragment;
import net.oschina.app.v2.base.BaseActivity;
import net.oschina.app.v2.utils.SimpleTextWatcher;
import net.oschina.app.v2.utils.TDevice;

/**
 * Created by Tonlin on 2015/6/1.
 */
public class GroupActivity extends BaseActivity {
    @Override
    protected int getLayoutId() {
        return R.layout.v2_activity_simple_fragment;
    }

    @Override
    protected boolean hasBackButton() {
        return true;
    }

    @Override
    protected int getActionBarTitle() {
        return R.string.group_chat;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        FragmentTransaction trans = getSupportFragmentManager()
                .beginTransaction();
        trans.replace(R.id.container, new GroupFragment());
        trans.commit();
    }
}
