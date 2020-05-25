package com.example.map1;


import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

public class MainActivity extends AppCompatActivity {

    private MapView mapView;
    private BaiduMap baiduMap;
    private TextView postionText;
    private boolean isFirstLocate = true;
    private LocationClient mLocationClient;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getApplicationContext());//创建Location实例并接受Context参数
        mLocationClient.registerLocationListener(new MyLocationListener());//注册定位监视器，接受位置信息时会回调到该监视器
        SDKInitializer.initialize(this.getApplicationContext());//初始化操作，传入Context
        setContentView(R.layout.activity_main);
        postionText = (TextView) findViewById(R.id.position_text_view);
        mapView = (MapView) findViewById(R.id.mmapview);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);//显示当地位置
        mLocationClient.start();//开始定位，定位成功后会回调到监视器
        initLocation();


    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");//直接返回Bi0911坐标系，避免地图纠偏
        option.setScanSpan(5000);//每隔5000毫秒更新一次定位信息
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);//强制GPS
        option.setIsNeedAddress(true);//需要精确的信息
        mLocationClient.setLocOption(option);
    }

    @Override
    protected void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mLocationClient.stop();
        baiduMap.setMyLocationEnabled(false);
        mapView.onDestroy();
        mapView = null;
        super.onDestroy();
    }
    class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation.getLocType() == bdLocation.TypeGpsLocation || bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                navigateTo(bdLocation);
            }
            StringBuilder currentPostion = new StringBuilder();
            currentPostion.append("纬度：").append(bdLocation.getLatitude()).append("\n");//将经纬度信息转化为国家/省份/城市等信息
            currentPostion.append("经度：").append(bdLocation.getLongitude()).append("\n");
            currentPostion.append("国家：").append(bdLocation.getCountry()).append("\n");
            currentPostion.append("省：").append(bdLocation.getProvince()).append("\n");
            currentPostion.append("市：").append(bdLocation.getCity()).append("\n");
            currentPostion.append("区：").append(bdLocation.getDistrict()).append("\n");
            currentPostion.append("街道：").append(bdLocation.getStreet()).append("\n");
            if (bdLocation.getLocType() == bdLocation.TypeGpsLocation) {//判断定位的类型
                currentPostion.append("GPS");
            } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                currentPostion.append("网络");
            } else {
                currentPostion.append("无");
            }
            postionText.setText(currentPostion);
        }
    }
    private void navigateTo(BDLocation location) {//显示自己在地图上的位置
        if (isFirstLocate) {
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());//存储经纬度到LatLng
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);//显示我所在的区域
            update = MapStatusUpdateFactory.zoomTo(16f);//地图显示的缩放级别
            baiduMap.animateMapStatus(update);//传入到经纬度
            isFirstLocate = false;//防止多次调用
        }
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();//将经纬度信息存储到 Builder中
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        baiduMap.setMyLocationData(locationData);//将我的位置显示出来
    }



}
