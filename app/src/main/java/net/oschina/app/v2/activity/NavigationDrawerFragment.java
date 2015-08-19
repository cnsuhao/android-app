package net.oschina.app.v2.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.user.view.QrCodeDialog;
import net.oschina.app.v2.base.BaseFragment;
import net.oschina.app.v2.base.Constants;
import net.oschina.app.v2.model.User;
import net.oschina.app.v2.utils.AvatarUtils;
import net.oschina.app.v2.utils.TDevice;
import net.oschina.app.v2.utils.UIHelper;

/**
 * Created by Tonlin on 2015/8/14.
 */
public class NavigationDrawerFragment extends BaseFragment {

    private ImageView mIvAvatar;
    private TextView mTvName;
    private ImageView mIvQrCode;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onResume();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mReceiver);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().registerReceiver(mReceiver,new IntentFilter(Constants.INTENT_ACTION_LOGOUT));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.v2_fragment_main_drawer, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        mIvAvatar = (ImageView) view.findViewById(R.id.iv_avatar);
        mTvName = (TextView) view.findViewById(R.id.tv_name);

        mIvQrCode = (ImageView)view.findViewById(R.id.iv_qr_code);
        mIvQrCode.setOnClickListener(this);

        TextView tvVersionName = (TextView) view
                .findViewById(R.id.tv_version_name);
        tvVersionName.setText(getString(R.string.version_format,TDevice.getVersionName()));

        view.findViewById(R.id.rl_header).setOnClickListener(this);
        view.findViewById(R.id.tv_software).setOnClickListener(this);
        view.findViewById(R.id.tv_event).setOnClickListener(this);
        view.findViewById(R.id.tv_shake).setOnClickListener(this);
        view.findViewById(R.id.tv_scan).setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (AppContext.instance().isLogin()) {
            User user = AppContext.getLoginInfo();

            ImageLoader.getInstance().displayImage(AvatarUtils.getLargeAvatar(user.getFace()), mIvAvatar);
            mTvName.setText(user.getName());
            mIvQrCode.setVisibility(View.VISIBLE);
        } else {
            mIvAvatar.setImageResource(R.drawable.ic_default_avatar);
            mTvName.setText(R.string.unlogin);
            mIvQrCode.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.rl_header:
                if (AppContext.instance().isLogin()) {
                    UIHelper.showUserInfo(getActivity());
                } else {
                    UIHelper.showLogin(getActivity());
                }
                break;
            case R.id.tv_software:
                UIHelper.showSoftware(getActivity());
                break;
            case R.id.tv_event:
                UIHelper.showEvents(getActivity());
                break;
            case R.id.tv_shake:
                UIHelper.showShake(getActivity());
                break;
            case R.id.tv_scan:
                UIHelper.showQRCode(getActivity());
                break;
            case R.id.iv_qr_code:
                QrCodeDialog dialog = new QrCodeDialog(getActivity());
                dialog.setNegativeButton(R.string.cancel,null);
                dialog.show();
                break;
        }
    }
}
