package net.oschina.app.v2.activity.chat;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.mapsdk.raster.model.BitmapDescriptor;
import com.tencent.mapsdk.raster.model.GeoPoint;
import com.tencent.mapsdk.raster.model.LatLng;
import com.tencent.mapsdk.raster.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.map.MapController;
import com.tencent.tencentmap.mapsdk.map.MapView;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.base.BaseActivity;
import net.oschina.app.v2.utils.TLog;

/**
 * Created by Tonlin on 2015/6/19.
 */
public class SelectLocationActivity2 extends BaseActivity implements AdapterView.OnItemClickListener, TencentLocationListener {

    private static final String TAG = "SelectLocationActivity2";
    private MapView mMapView;
    private MapController mMapController;
    private BitmapDescriptor mCenterMarker;

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
        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapController = mMapView.getController();

        mCenterMarker = new BitmapDescriptor(
                BitmapFactory.decodeResource(getResources(),R.drawable.ic_chat_current_location_marker)
        );
        requestMyLocation();
    }

    private void requestMyLocation() {
        TencentLocationRequest request = TencentLocationRequest.create();
        request.setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_NAME);
        TencentLocationManager locationManager = TencentLocationManager
                .getInstance(getApplicationContext());
        int error = locationManager.requestLocationUpdates(request, this);
        if (error == 0) {
            TLog.log(TAG, "注册监听成功");
        }
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
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onStop() {
        mMapView.onStop();
        super.onStop();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    private void requestLocationNearby(double latitude, double longitude) {

    }

    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int error, String s) {
        if (TencentLocation.ERROR_OK == error) {
            // 定位成功
            mMapController.animateTo(new GeoPoint((int)(tencentLocation.getLatitude()*1E6) , (int)(tencentLocation.getLongitude()*1E6)));
            mMapController.setZoom(16);

            mMapView.addMarker(new MarkerOptions().icon(mCenterMarker).position(new LatLng(tencentLocation.getLatitude(),tencentLocation.getLongitude())));
        } else {
            // 定位失败
        }

        TencentLocationManager locationManager = TencentLocationManager.getInstance(getApplicationContext());
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusUpdate(String s, int i, String s1) {

    }

}
