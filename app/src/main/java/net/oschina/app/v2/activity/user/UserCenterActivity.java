package net.oschina.app.v2.activity.user;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.MySwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.github.ksoichiro.android.observablescrollview.CacheFragmentStatePagerAdapter;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nineoldandroids.view.ViewHelper;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.user.fragment.UserActiveRecyclerListFragment;
import net.oschina.app.v2.activity.user.fragment.UserBaseInformationRecyclerFragment;
import net.oschina.app.v2.activity.user.fragment.UserBlogRecyclerListFragment;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.base.BaseActivity;
import net.oschina.app.v2.model.Result;
import net.oschina.app.v2.model.User;
import net.oschina.app.v2.utils.AvatarUtils;
import net.oschina.app.v2.utils.TDevice;
import net.oschina.app.v2.utils.UIHelper;

import org.apache.http.Header;

import java.io.ByteArrayInputStream;

/**
 * Created by Tonlin on 2015/8/20.
 */
public class UserCenterActivity extends BaseActivity implements AppBarLayout.OnOffsetChangedListener, UserDataControl {
    private static final String TAG = "UserCenter";
    private static final Object FEMALE = "女";
    private AppBarLayout mAppBar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private MyPagerAdapter mPagerAdapter;
    private TextView mTvActionBarTitle, mTvName;
    private SimpleDraweeView mIvAvatar;
    private User mUser;
    private ImageView mIvGender;

    private int mUid, mHisUid;
    private String mHisName;
    private String mHisAvatarUrl;

    @Override
    protected int getLayoutId() {
        return R.layout.v2_activity_user_center;
    }

    @Override
    protected boolean hasBackButton() {
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAppBar.removeOnOffsetChangedListener(this);
    }

    @Override
    protected int getActionBarCustomView() {
        return R.layout.v2_actionbar_custom_user_center;
    }

    @Override
    protected void initActionBar(Toolbar actionBar) {
        super.initActionBar(actionBar);
        mTvActionBarTitle = (TextView) actionBar.findViewById(R.id.tv_actionbar_title);
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        mUid = AppContext.instance().getLoginUid();
        mHisUid = getIntent().getIntExtra("his_id", 0);
        mHisName = getIntent().getStringExtra("his_name");
        mHisAvatarUrl = getIntent().getStringExtra("his_avatar");

        mAppBar = (AppBarLayout) findViewById(R.id.appbar);
        mAppBar.addOnOffsetChangedListener(this);

        mIvAvatar = (SimpleDraweeView) findViewById(R.id.iv_avatar);
        mTvName = (TextView) findViewById(R.id.tv_name);
        mIvGender = (ImageView) findViewById(R.id.iv_gender);

        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.view_pager_margin));
        mViewPager.setOffscreenPageLimit(3);
        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        mTvActionBarTitle.setText(mHisName);
        mTvName.setText(mHisName);
        mIvAvatar.setImageURI(Uri.parse(AvatarUtils.getLargeAvatar(mHisAvatarUrl)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_center_menu, menu);
        if (mUser != null) {
            if (mUser.getRelation() == User.RELATION_TYPE_FANS_ME || mUser.getRelation() == User.RELATION_TYPE_NULL) {
                menu.findItem(R.id.menu_follow).setVisible(true);
                menu.findItem(R.id.menu_unfollow).setVisible(false);
            } else {
                menu.findItem(R.id.menu_follow).setVisible(false);
                menu.findItem(R.id.menu_unfollow).setVisible(true);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_his_follow:
                UIHelper.showFriends(this, mHisUid, 0);
                break;
            case R.id.menu_his_fans:
                UIHelper.showFriends(this, mHisUid, 1);
                break;
            case R.id.menu_message:
                UIHelper.showMessagePub(this, mHisUid, mHisName);
                break;
            case R.id.menu_follow:
                handleUserRelation();
                break;
            case R.id.menu_unfollow:
                handleUserRelation();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        ViewHelper.setAlpha(mTvActionBarTitle, (-i * 1f) / appBarLayout.getTotalScrollRange());
        for (int k = 0; k < mPagerAdapter.getCount(); k++) {
            Fragment fragment = mPagerAdapter.getItemAt(k);
            if (fragment instanceof SwipeRefreshViewControl) {
                MySwipeRefreshLayout view = ((SwipeRefreshViewControl) fragment).getSwipeRefreshView();
                if (view != null) {
                    view.setEnabled(i == 0);
                }
            }
        }
    }

    @Override
    public void setUserInfo(User user) {
        if (user == null) return;
        mUser = user;
        mTvActionBarTitle.setText(user.getName());
        mTvName.setText(user.getName());
        mIvAvatar.setImageURI(Uri.parse(AvatarUtils.getLargeAvatar(user.getFace())));
        mIvGender.setImageResource(FEMALE.equals(user.getGender()) ? R.drawable.userinfo_icon_female :
                R.drawable.userinfo_icon_male);
        mIvGender.setVisibility(View.VISIBLE);

        supportInvalidateOptionsMenu();
    }

    private void handleUserRelation() {
        if (mUser == null)
            return;
        // 判断登录
        final AppContext ac = AppContext.instance();
        if (!ac.isLogin()) {
            UIHelper.showLogin(this);
            return;
        }
        String dialogTitle = "";
        int relationAction = 0;
        switch (mUser.getRelation()) {
            case User.RELATION_TYPE_BOTH:
                dialogTitle = "确定取消互粉吗？";
                relationAction = User.RELATION_ACTION_DELETE;
                break;
            case User.RELATION_TYPE_FANS_HIM:
                dialogTitle = "确定取消关注吗？";
                relationAction = User.RELATION_ACTION_DELETE;
                break;
            case User.RELATION_TYPE_FANS_ME:
                dialogTitle = "确定关注他吗？";
                relationAction = User.RELATION_ACTION_ADD;
                break;
            case User.RELATION_TYPE_NULL:
                dialogTitle = "确定关注他吗？";
                relationAction = User.RELATION_ACTION_ADD;
                break;
        }
        final int ra = relationAction;
        AlertDialog dialog = new AlertDialog.Builder(this,
                R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle(dialogTitle)
                .setCancelable(true)
                .setNegativeButton(R.string.cancel,null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        sendUpdateRelcationRequest(ra);
                    }
                }).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void sendUpdateRelcationRequest(int ra) {
        NewsApi.updateRelation(mUid, mHisUid, ra,
                new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
                        try {
                            Result result = Result.parse(new ByteArrayInputStream(arg2));
                            if (result.OK()) {
                                UIHelper.sendNoticeBroadcast(UserCenterActivity.this, result);
                                switch (mUser.getRelation()) {
                                    case User.RELATION_TYPE_BOTH:
                                        mUser.setRelation(User.RELATION_TYPE_FANS_ME);
                                        break;
                                    case User.RELATION_TYPE_FANS_HIM:
                                        mUser.setRelation(User.RELATION_TYPE_NULL);
                                        break;
                                    case User.RELATION_TYPE_FANS_ME:
                                        mUser.setRelation(User.RELATION_TYPE_BOTH);
                                        break;
                                    case User.RELATION_TYPE_NULL:
                                        mUser.setRelation(User.RELATION_TYPE_FANS_HIM);
                                        break;
                                }
                                supportInvalidateOptionsMenu();
                            }
                            AppContext.showToastShort(result.getErrorMessage());
                        } catch (Exception e) {
                            e.printStackTrace();
                            onFailure(arg0, arg1, arg2, e);
                        }
                    }

                    @Override
                    public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                                          Throwable arg3) {
                    }
                });
    }

    static class MyPagerAdapter extends CacheFragmentStatePagerAdapter {

        @Override
        protected Fragment createItem(int i) {
            switch (i) {
                case 0:
                    return new UserActiveRecyclerListFragment();
                case 1:
                    return new UserBlogRecyclerListFragment();
                case 2:
                    return new UserBaseInformationRecyclerFragment();
            }
            return new Fragment();
        }

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "分类";
                case 1:
                    return "博客";
                case 2:
                    return "资料";
            }
            return "";
        }
    }
}
