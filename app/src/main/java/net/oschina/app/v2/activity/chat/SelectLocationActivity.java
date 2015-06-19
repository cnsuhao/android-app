//package net.oschina.app.v2.activity.chat;
//
//import android.os.Bundle;
//import android.support.v7.widget.Toolbar;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.ListView;
//
//import com.baidu.location.BDLocation;
//import com.baidu.location.BDLocationListener;
//import com.baidu.location.LocationClient;
//import com.baidu.location.LocationClientOption;
//import com.baidu.mapapi.map.BaiduMap;
//import com.baidu.mapapi.map.BitmapDescriptor;
//import com.baidu.mapapi.map.BitmapDescriptorFactory;
//import com.baidu.mapapi.map.MapStatusUpdate;
//import com.baidu.mapapi.map.MapStatusUpdateFactory;
//import com.baidu.mapapi.map.MapView;
//import com.baidu.mapapi.map.MarkerOptions;
//import com.baidu.mapapi.map.MyLocationConfiguration;
//import com.baidu.mapapi.map.MyLocationData;
//import com.baidu.mapapi.map.OverlayOptions;
//import com.baidu.mapapi.model.LatLng;
//import com.baidu.mapapi.search.core.PoiInfo;
//import com.baidu.mapapi.search.core.SearchResult;
//import com.baidu.mapapi.search.geocode.GeoCodeResult;
//import com.baidu.mapapi.search.geocode.GeoCoder;
//import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
//import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
//import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
//import com.tonlin.osc.happy.R;
//
//import net.oschina.app.v2.activity.chat.adapter.NearbyLocationAdapter;
//import net.oschina.app.v2.base.BaseActivity;
//import net.oschina.app.v2.utils.TLog;
//
//import java.util.ArrayList;
//
///**
// * Created by Tonlin on 2015/6/19.
// */
//public class SelectLocationActivity extends BaseActivity implements AdapterView.OnItemClickListener {
//
//    private static final java.lang.String TAG = "SelectLocationActivity";
//    private MapView mMapView;
//    public BDLocationListener myListener = new MyLocationListener();
//    private LocationClient mLocationClient;
//    private BaiduMap mBaiduMap;
//    private boolean isFirstLoc = true;
//
//    private ListView mLvLocation;
//    private NearbyLocationAdapter mAdapter;
//
//    private BitmapDescriptor mLocIcon = BitmapDescriptorFactory
//            .fromResource(R.drawable.ic_chat_current_location);
//    private BitmapDescriptor mMarker = BitmapDescriptorFactory
//            .fromResource(R.drawable.ic_chat_current_location_marker);
//    private LatLng mCurrentLocation;
//
//    @Override
//    protected boolean hasBackButton() {
//        return true;
//    }
//
//    @Override
//    protected int getActionBarCustomView() {
//        return R.layout.v2_actionbar_custom_chat_map;
//    }
//
//    @Override
//    protected int getActionBarTitle() {
//        return R.string.actionbar_title_location;
//    }
//
//    @Override
//    protected int getLayoutId() {
//        return R.layout.v2_activity_chat_map;
//    }
//
//    @Override
//    protected void initActionBar(Toolbar actionBar) {
//        super.initActionBar(actionBar);
//        actionBar.findViewById(R.id.btn_send).setOnClickListener(this);
//    }
//
//    @Override
//    protected void init(Bundle savedInstanceState) {
//        super.init(savedInstanceState);
//        findViewById(R.id.btn_locate).setOnClickListener(this);
//        //获取地图控件引用
//        mMapView = (MapView) findViewById(R.id.bmapView);
//        mMapView.showScaleControl(false);
//        mMapView.showZoomControls(false);
//
//        mBaiduMap = mMapView.getMap();
//        mBaiduMap.setMyLocationEnabled(false);
//        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(15.0f));
//
//        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
//                MyLocationConfiguration.LocationMode.FOLLOWING, true, mMarker));
//
//        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
//        mLocationClient.registerLocationListener(myListener);    //注册监听函数
//
//        LocationClientOption option = new LocationClientOption();
//        option.setIsNeedAddress(true);
//        mLocationClient.setLocOption(option);
//        mLocationClient.setDebug(true);
//        mLocationClient.start();
//        //mLocationClient.requestLocation();
//
//        mLvLocation = (ListView) findViewById(R.id.lv_location);
//        mLvLocation.setOnItemClickListener(this);
//        mAdapter = new NearbyLocationAdapter();
//        mLvLocation.setAdapter(mAdapter);
//
//    }
//
//    @Override
//    public void onClick(View v) {
//        super.onClick(v);
//        final int id = v.getId();
//        if (id == R.id.btn_locate) {
//            mLocationClient.requestLocation();
//        } else if (id == R.id.btn_send) {
//
//            setResult(RESULT_OK);
//            finish();
//        }
//    }
//
//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        PoiInfo p = (PoiInfo) mAdapter.getItem(position);
//        if (p != null) {
//            mAdapter.setSelected(position);
//            updateLocation(p.location);
//        }
//    }
//
//    private void updateLocation(LatLng location) {
//        mBaiduMap.clear();
//
//        mBaiduMap.addOverlay(new MarkerOptions().position(mCurrentLocation)
//                .icon(mMarker));
//
//        OverlayOptions overlayOption = new MarkerOptions().position(location)
//                .icon(mLocIcon);
//        mBaiduMap.addOverlay(overlayOption);
//
//        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(location);
//        mBaiduMap.animateMapStatus(u);
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        mMapView.onDestroy();
//        mLocationClient.stop();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        mMapView.onResume();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        mMapView.onPause();
//    }
//
//    private void requestLocationNearby(double latitude, double longitude) {
//        TLog.log(TAG, "lat:" + latitude + " lng:" + longitude);
//
//        ReverseGeoCodeOption option = new ReverseGeoCodeOption();
//        option.location(new LatLng(latitude, longitude));
//
//        GeoCoder coder = GeoCoder.newInstance();
//        coder.reverseGeoCode(option);
//        coder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
//
//            @Override
//
//            public void onGetReverseGeoCodeResult(
//                    ReverseGeoCodeResult result) {
//                // 这个result 里面有个附近poi的列表
//                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
//                    TLog.log(TAG, "搜索出错了");
//                    return;
//                }
//
//                mAdapter.addData((ArrayList) result.getPoiList());
//            }
//
//            @Override
//
//            public void onGetGeoCodeResult(GeoCodeResult arg0) {
//
//            }
//        });
//    }
//
//    public class MyLocationListener implements BDLocationListener {
//
//        @Override
//        public void onReceiveLocation(BDLocation location) {
//            if (location == null)
//                return;
//            MyLocationData locData = new MyLocationData.Builder()
//                    .accuracy(location.getRadius())
//                            // 此处设置开发者获取到的方向信息，顺时针0-360
//                    .direction(100).latitude(location.getLatitude())
//                    .longitude(location.getLongitude()).build();
//            mBaiduMap.setMyLocationData(locData);
//
//            LatLng ll = new LatLng(location.getLatitude(),
//                    location.getLongitude());
//
//            mCurrentLocation = ll;
//
//            PoiInfo p = new PoiInfo();
//            p.location = mCurrentLocation;
//            p.name = "[位置]";
//            p.address = location.getAddrStr();
//
//            mAdapter.clear();
//
//            mAdapter.addItem(0, p);
//            mAdapter.setSelected(0);
//
//            updateLocation(ll);
//
//            requestLocationNearby(location.getLatitude(), location.getLongitude());
//        }
//    }
//}
