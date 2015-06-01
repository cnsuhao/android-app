package net.oschina.app.v2.activity.chat;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.activity.chat.fragment.NewFriendFragment;
import net.oschina.app.v2.base.BaseActivity;

/**
 * Created by Tonlin on 2015/6/1.
 */
public class NewFriendActivity extends BaseActivity {

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
        return R.string.new_friend;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        FragmentTransaction trans = getSupportFragmentManager()
                .beginTransaction();
        trans.replace(R.id.container, new NewFriendFragment());
        trans.commit();
    }
}
