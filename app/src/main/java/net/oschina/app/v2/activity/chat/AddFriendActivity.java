package net.oschina.app.v2.activity.chat;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import net.oschina.app.v2.base.BaseActivity;
import net.oschina.app.v2.utils.SimpleTextWatcher;
import net.oschina.app.v2.utils.TDevice;

/**
 * Created by Tonlin on 2015/6/1.
 */
public class AddFriendActivity extends BaseActivity{
    private EditText mEtContent;
    private ImageButton mIbClear;
    private View mIbSearch;
    private AddFriendFragment mFragment;

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

    @Override
    protected void initActionBar(Toolbar actionBar) {
        super.initActionBar(actionBar);
        View view = actionBar;
        mIbSearch = view.findViewById(R.id.ib_search);
        mIbSearch.setOnClickListener(this);
        mIbClear = (ImageButton) view.findViewById(R.id.ib_clear);
        mIbClear.setOnClickListener(this);
        mEtContent = (EditText) view.findViewById(R.id.et_content);
        mEtContent.setOnClickListener(this);
        mEtContent.setHint(R.string.chat_add_friend_hint);
        mEtContent.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mEtContent.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (mEtContent.getText().toString().length() > 0) {
                    mIbClear.setVisibility(View.VISIBLE);
                    mIbSearch.setVisibility(View.GONE);
                } else {
                    mIbClear.setVisibility(View.GONE);
                    mIbSearch.setVisibility(View.GONE);
                }
            }
        });
        mEtContent.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    handleSearch();
                    return true;
                }
                return false;
            }
        });
        mEtContent.requestFocus();
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        FragmentTransaction trans = getSupportFragmentManager()
                .beginTransaction();
        mFragment = new AddFriendFragment();
        trans.replace(R.id.container, mFragment);
        trans.commit();
    }

    private void handleSearch() {
        String text = mEtContent.getText().toString();
        if(TextUtils.isEmpty(text)){
            AppContext.showToastShort(R.string.chat_tip_please_input_nick_name);
            return;
        }
        TDevice.hideSoftKeyboard(mEtContent);
        mEtContent.clearFocus();
        mFragment.search(text);
    }
}
