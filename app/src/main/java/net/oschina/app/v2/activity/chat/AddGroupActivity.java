package net.oschina.app.v2.activity.chat;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.activity.chat.fragment.AddGroupFragment;
import net.oschina.app.v2.base.BaseActivity;

/**
 * Created by Tonlin on 2015/6/2.
 */
public class AddGroupActivity extends BaseActivity {

    private Button mBtnOk;

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
        return R.layout.v2_actionbar_custom_chat_add_group;
    }

    @Override
    protected int getActionBarTitle() {
        return R.string.chat_add_group;
    }

    @Override
    protected void initActionBar(Toolbar actionBar) {
        super.initActionBar(actionBar);
        mBtnOk = (Button) actionBar.findViewById(R.id.btn_ok);
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        FragmentTransaction trans = getSupportFragmentManager()
                .beginTransaction();
        AddGroupFragment fragment =  new AddGroupFragment();
        fragment.setOKButton(mBtnOk);
        trans.replace(R.id.container,fragment);
        trans.commit();
    }
}
