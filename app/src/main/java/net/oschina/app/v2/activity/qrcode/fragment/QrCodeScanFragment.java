package net.oschina.app.v2.activity.qrcode.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.base.BaseFragment;

/**
 * Created by Tonlin on 2015/8/15.
 */
public class QrCodeScanFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.v2_fragment_qrcode_scan,container,false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {

    }
}
