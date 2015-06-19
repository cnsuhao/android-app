package net.oschina.app.v2.activity.chat;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;

import com.tencent.tencentmap.mapsdk.map.MapView;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.base.BaseActivity;

/**
 * Created by Tonlin on 2015/6/19.
 */
public class SelectLocationActivity2 extends BaseActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "SelectLocationActivity2";
    private MapView mMapView;

    @Override
    protected boolean hasBackButton() {
        return true;
    }

    @Override
    protected int getActionBarCustomView() {
        return R.layout.v2_actionbar_custom_chat_map;
    }

    @Override
    protected int getActionBarTitle() {
        return R.string.actionbar_title_location;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.v2_activity_chat_map2;
    }

    @Override
    protected void initActionBar(Toolbar actionBar) {
        super.initActionBar(actionBar);
        actionBar.findViewById(R.id.btn_send).setOnClickListener(this);
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        findViewById(R.id.btn_locate).setOnClickListener(this);
        mMapView = (MapView)findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        final int id = v.getId();
        if (id == R.id.btn_locate) {

        } else if (id == R.id.btn_send) {

            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void requestLocationNearby(double latitude, double longitude) {

    }


}
