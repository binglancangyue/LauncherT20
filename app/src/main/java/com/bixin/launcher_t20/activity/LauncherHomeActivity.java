package com.bixin.launcher_t20.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.bixin.launcher_t20.R;
import com.bixin.launcher_t20.model.bean.WeatherBean;
import com.bixin.launcher_t20.model.listener.OnLocationListener;
import com.bixin.launcher_t20.model.receiver.WeatherReceiver;
import com.bixin.launcher_t20.model.tools.CallBackManagement;
import com.bixin.launcher_t20.model.tools.CustomValue;
import com.bixin.launcher_t20.model.tools.StartActivityTool;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.lang.ref.WeakReference;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class LauncherHomeActivity extends BaseActivity implements View.OnClickListener, View.OnLongClickListener, OnLocationListener {
    private static final String TAG = "HomeActivity";
    private LauncherApp myApplication;
    public InnerHandler mHandler;
    private StartActivityTool activityTools;
    private WeatherReceiver mWeatherReceiver;
    private ImageView ivWeatherIcon;
    private TextView tvWeather;
    private TextView tvCurrentCity;
    private WeatherBean weatherBean;
    private static final int MIN_CLICK_DELAY_TIME = 5000;
    private static long lastClickTime;
    private static final String CAMERA_RECORD_STATUS = "camera_record_status";
    private ImageView ivRecordState;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getFileData();
        registerDVRContentObserver();
        if (!CustomValue.IS_ENGLISH) {
            mHandler.sendEmptyMessageDelayed(2, 4000);
            mHandler.sendEmptyMessageDelayed(3, 6000);
            mHandler.sendEmptyMessageDelayed(6, 8000);
        }
        if (CustomValue.IS_START_TEST_APP) {
            mHandler.sendEmptyMessageDelayed(7, 12000);
        }
    }

    @Override
    public int getLayout() {
        return R.layout.activity_home_layout_weather;
    }

    @Override
    public void init() {
        myApplication = (LauncherApp) getApplication();
        mHandler = new InnerHandler(this);
        activityTools = new StartActivityTool();
        CallBackManagement.getInstance().setOnLocationListener(this);
    }

    @Override
    public void initView() {
        FrameLayout ivNavigation = findViewById(R.id.iv_navigation);
        FrameLayout ivRecorder = findViewById(R.id.iv_recorder);
        FrameLayout ivCloudService = findViewById(R.id.iv_cloud_service);
        FrameLayout ivFM = findViewById(R.id.iv_fm);
        FrameLayout ivBluetooth = findViewById(R.id.iv_bluetooth);
        FrameLayout ivMusic = findViewById(R.id.iv_music);
        FrameLayout ivAPP = findViewById(R.id.iv_app);
        ivWeatherIcon = findViewById(R.id.iv_weather);
        tvWeather = findViewById(R.id.tv_weather);
        ivRecordState = findViewById(R.id.iv_record_state);
        tvCurrentCity = findViewById(R.id.tv_city);
        if (CustomValue.IS_ENGLISH) {
            ivWeatherIcon.setVisibility(View.GONE);
            tvWeather.setVisibility(View.GONE);
            tvCurrentCity.setVisibility(View.GONE);
            TextView tvService = findViewById(R.id.tv_service);
            ImageView ivService = findViewById(R.id.iv_service);
            ImageView ivGPSIcon = findViewById(R.id.iv_gps_icon);
            ivGPSIcon.setVisibility(View.GONE);
            tvService.setText(R.string.home_title_file);
            ivService.setImageResource(R.drawable.icon_bx_home_file_normal);
        }
        if (CustomValue.NOT_CLOUD_SERVICE) {
            TextView tvService = findViewById(R.id.tv_service);
            ImageView ivService = findViewById(R.id.iv_service);
            tvService.setText(R.string.home_title_file);
            ivService.setImageResource(R.drawable.icon_bx_home_file_normal);
        }
        if (CustomValue.NOT_DVR) {
            TextView tvRecord = findViewById(R.id.tv_record);
            ImageView ivRecord = findViewById(R.id.iv_record);
            tvRecord.setText(R.string.home_title_hotspot);
            ivRecord.setImageResource(R.drawable.icon_bx_home_hotspot_normal);
        }
        ConstraintLayout constraintLayout = findViewById(R.id.cl_weather);
        constraintLayout.setOnClickListener(this);

        ivNavigation.setOnClickListener(this);
        ivRecorder.setOnClickListener(this);
        ivCloudService.setOnClickListener(this);
        ivFM.setOnClickListener(this);
        ivBluetooth.setOnClickListener(this);
        ivMusic.setOnClickListener(this);
        ivAPP.setOnClickListener(this);
        ivNavigation.setOnLongClickListener(this);
    }

    private void registerWeatherReceiver() {
        mWeatherReceiver = new WeatherReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(CustomValue.ACTION_UPDATE_WEATHER);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
//        filter.addAction(ACTION_GAODE_SEND);
        registerReceiver(mWeatherReceiver, filter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_navigation: // 高德导航
                String pkg = Settings.Global.getString(getContentResolver(), CustomValue.DEFAULT_MAP);
                Log.d(TAG, "onClick:pkg:" + pkg + ";");
                if (CustomValue.IS_ENGLISH) {
                    if (pkg == null || pkg.equals("")) {
                        pkg = CustomValue.PACKAGE_NAME_MAPS;
                    }
                    activityTools.launchAppByPackageName(pkg, "google地图");
                } else {
                    if (pkg == null || pkg.equals("")) {
                        pkg = CustomValue.PACKAGE_NAME_AUTONAVI;
                    }
                    activityTools.launchAppByPackageName(pkg, "高德地图");
                }
                break;
            case R.id.iv_recorder: // 记录仪
                if (CustomValue.NOT_DVR) {
                    Intent intent = new Intent();
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setAction("android.intent.action.MAIN");
                    ComponentName cn = new ComponentName("com.android.settings",
                            "com.android.settings.Settings$TetherSettingsActivity");
                    intent.setComponent(cn);
                    startActivity(intent);
                } else {
                    activityTools.launchAppByPackageName(CustomValue.PACKAGE_NAME_DVR, "记录仪");
//                    activityTools.launchAppByPackageName(CustomValue.PACKAGE_NAME_RCX_DVR, "记录仪");
                }
                break;
            case R.id.iv_cloud_service: // 云服务
                if (CustomValue.IS_ENGLISH || CustomValue.NOT_CLOUD_SERVICE) {
                    activityTools.launchAppByPackageName(CustomValue.PACKAGE_NAME_ESTRONGS, "文件管理器");
                } else if (CustomValue.IS_E_CAR) {
                    activityTools.launchAppByPackageName(CustomValue.PACKAGE_NAME_E_CAR);
                } else {
                    activityTools.launchAppByPackageName(CustomValue.PACKAGE_NAME_RCX, "任车性");
                }
                break;
            case R.id.iv_fm: // FM
//                Intent intent=new Intent(this,SettingsWindowActivity.class);
//                startActivity(intent);
                activityTools.launchAppByPackageName(CustomValue.PACKAGE_NAME_FM, "FM");
                break;
            case R.id.iv_bluetooth: // 蓝牙
                if (!CustomValue.IS_ENGLISH) {
                    activityTools.launchAppByPackageName(CustomValue.PACKAGE_NAME_BLUETOOTH);
                } else {
                    activityTools.jumpByAction(Settings.ACTION_BLUETOOTH_SETTINGS);
                }
                break;
            case R.id.iv_music: // 音乐
                if (CustomValue.IS_ENGLISH) {
                    activityTools.launchAppByPackageName(CustomValue.PACKAGE_NAME_SPOTIFY, "google音乐");
                } else {
                    activityTools.launchAppByPackageName(CustomValue.PACKAGE_NAME_KWMUSIC, "酷我");
                }
                break;
            case R.id.iv_playback: // 视频回放
                activityTools.launchAppByPackageName(CustomValue.PACKAGE_NAME_ViDEO_PLAY_BACK, "视频回放");
                break;
            case R.id.cl_weather:
                getWeather();
                break;
            default: // 应用
                activityTools.jumpToActivity(this, AppListActivity.class);
//                Intent intent=new Intent(Settings.ACTION_DREAM_SETTINGS);
//                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        int viewID = v.getId();
        if (viewID == R.id.iv_navigation) {
            setNavAPP();
            return true;
        }
        return false;
    }

    @Override
    public void gpsSpeedChanged() {

    }

    @Override
    public void updateWeather(String weatherInfo) {
        Log.d(TAG, "updateWeather:weatherInfo " + weatherInfo);
        if (weatherInfo.equals("update")) {
            Log.d(TAG, "updateWeather: 40");
            mHandler.sendEmptyMessageDelayed(8, 45000);
        } else {
            weatherBean = JSONObject.parseObject(weatherInfo, WeatherBean.class);
            if (weatherBean == null) {
                return;
            }
            if (mHandler != null) {
                mHandler.sendEmptyMessage(4);
            }
        }
    }

    private void updateWeather() {
        String cityName = weatherBean.getCityName();
        String weather = weatherBean.getWeather();
        tvCurrentCity.setText(cityName);
        String weatherInfo = weather + "  " + weatherBean.getCurrentTemperature() + "°";
        tvWeather.setText(weatherInfo);
        ivWeatherIcon.setImageResource(activityTools.getWeatherIcon(weather));
    }


    public static class InnerHandler extends Handler {
        private LauncherHomeActivity activity;

        private InnerHandler(LauncherHomeActivity activity) {
            WeakReference<LauncherHomeActivity> activityWeakReference = new WeakReference<>(activity);
            this.activity = activityWeakReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 2:
                    activity.startDVR();
                    break;
                case 3:
                    activity.registerWeatherReceiver();
                    activity.activityTools.startVoiceRecognitionService();
                    break;
                case 4:
                    activity.updateWeather();
                    break;
                case 5:
                    activity.startDVR();
                    break;
                case 6:
                    activity.activityTools.startRCX();
//                activity.activityTools.startECarService();
                    break;
                case 7:
                    activity.activityTools.startValidationTools();
                    break;
                case 8:
                    activity.getWeatherByNetwork();
                    break;
                case 9:
                    activity.isShowRecordIcon();
                    break;
            }
        }
    }

    private void getWeather() {
        if (isFastClick()) {
            Intent intent = new Intent(CustomValue.ACTION_GET_WEATHER);
            sendBroadcast(intent);
        }
    }

    private void getWeatherByNetwork() {
        Intent intent = new Intent(CustomValue.ACTION_GET_WEATHER);
        sendBroadcast(intent);
        Log.d(TAG, "getWeatherByNetwork: ");
    }

    public boolean isFastClick() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
            flag = true;
        }
        lastClickTime = curClickTime;
        return flag;
    }

    private void initAppInfo() {
        mDisposable.add(Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                myApplication.initAppList();
//                TXZConfigManager.getInstance().initialize(mContext, HomeActivity.this);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(aBoolean -> {

                }));
    }

    @SuppressLint("NewApi")
    private void getFileData() {
        getWindow().getDecorView().post(() -> mHandler.post(() -> {
//            mScreenControl = new ScreenControl();
//            mScreenControl.init();
//            mScreenControl.checkAndTurnOnDeviceManager(this);

//            RequestPermissionsTool requestPermissionsTools = new RequestPermissionsTool();
//            requestPermissionsTools.requestPermissions(HomeActivity.this,
//                    new String[]{KILL_BACKGROUND_PROCESSES});
        }));
    }

    private void setNavAPP() {
        Settings.Global.putInt(getContentResolver(), CustomValue.OPEN_SET_DEFAULT_MAP, 1);
        activityTools.jumpToActivity(this, AppListActivity.class);
    }

    private void startDVR() {
/*        Intent intent = new Intent();
        String packageName = "com.bx.carDVR";
        String className = "com.bx.carDVR.DVRService";
        intent.setComponent(new ComponentName(packageName, className));
        try {
            mContext.startService(intent);
        } catch (Exception e) {
            Log.e(TAG, "startDVR: " + e.getMessage());
        }*/
        Intent launchIntent = mContext.getPackageManager()
                .getLaunchIntentForPackage(CustomValue.PACKAGE_NAME_DVR);
        if (launchIntent == null) {
            mHandler.sendEmptyMessageDelayed(5, 1000);
        } else {
            mContext.startActivity(launchIntent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void isShowRecordIcon() {
        int state = 1;
        try {
            state = Settings.Global.getInt(getContentResolver(), CAMERA_RECORD_STATUS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "dvr state: " + state);
        if (state == 1) {
            ivRecordState.setVisibility(View.VISIBLE);
        } else {
            ivRecordState.setVisibility(View.GONE);
        }
    }


    private final ContentObserver mDVRContentObserver = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            mHandler.sendEmptyMessage(9);
        }
    };

    private void registerDVRContentObserver() {
        Log.d(TAG, "registerGPSContentObserver: ");
        getContentResolver().registerContentObserver(
                Settings.Global.getUriFor(CAMERA_RECORD_STATUS),
                false, mDVRContentObserver);
    }

    private void unRegisterContentObserver() {
        getContentResolver().unregisterContentObserver(mDVRContentObserver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if (mWeatherReceiver != null) {
            try {
                unregisterReceiver(mWeatherReceiver);
            } catch (Exception e) {
                Log.d(TAG, "onDestroy:e " + e.getMessage());
            }
            mWeatherReceiver = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        unRegisterContentObserver();
        myApplication = null;
        activityTools = null;
    }
}
