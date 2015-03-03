package net.oschina.app.v2.activity.search;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.base.BaseActivity;

/**
 * Created by Sim on 2015/3/3.
 */
public class SearchActivity extends BaseActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.v2_activity_simple_fragment;
    }

    @Override
    protected int getActionBarCustomView() {
        return R.layout.v2_actionbar_search;
    }

    @Override
    protected boolean hasBackButton() {
        return true;
    }
}
