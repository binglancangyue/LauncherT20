package com.bixin.launcher_t20.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.bixin.launcher_t20.R;
import com.bixin.launcher_t20.adapter.RecyclerGridViewAdapter;
import com.bixin.launcher_t20.model.bean.AppInfo;
import com.bixin.launcher_t20.model.listener.OnAppUpdateListener;
import com.bixin.launcher_t20.model.listener.OnRecyclerViewItemListener;
import com.bixin.launcher_t20.model.receiver.APPReceiver;
import com.bixin.launcher_t20.model.tools.CallBackManagement;
import com.bixin.launcher_t20.model.tools.CustomValue;
import com.bixin.launcher_t20.model.tools.StartActivityTool;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Altair
 * @date :2019.10.28 上午 10:03
 * @description: AppList页面
 */
public class AppListActivity extends BaseActivity implements OnRecyclerViewItemListener, OnAppUpdateListener {
    private final static String TAG = "AppListActivity";
    private LauncherApp myApplication;
    private ArrayList<AppInfo> appInfoArrayList = new ArrayList<>();
    private StartActivityTool mActivityTools;
    private RecyclerGridViewAdapter mRecyclerGridViewAdapter;
    private RecyclerView recyclerView;
    private APPReceiver mUpdateAppReceiver;
    private int openDefaultMap = 0;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        initData();
        initAppInfo();
    }

    @Override
    public int getLayout() {
        return R.layout.activity_app_list;
    }

    @Override
    protected void onStart() {
        super.onStart();
        openDefaultMap = Settings.Global.getInt(getContentResolver(), CustomValue.OPEN_SET_DEFAULT_MAP, 0);
        Log.d(TAG, "onStart:openDefaultMap " + openDefaultMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void init() {
        mUpdateAppReceiver = new APPReceiver();
        myApplication = (LauncherApp) getApplication();
//        mActivityTools = new StartActivityTool(mContext);
        mActivityTools = new StartActivityTool();
        CallBackManagement.getInstance().setOnAppUpdateListener(this);
        registerAppReceiver();
    }

    @Override
    public void initView() {
        recyclerView = findViewById(R.id.rcv_app);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        //取消动画,避免更新图片闪烁
//        ((DefaultItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        GridLayoutManager manager = new GridLayoutManager(this, 5);
        recyclerView.setLayoutManager(manager);
    }


//    private void initData() {
//        getAppList();
//    }

    private void initAppInfo() {
        mDisposable.add(Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                getAppList(emitter);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(aBoolean -> {
                    if (mRecyclerGridViewAdapter == null) {
                        mRecyclerGridViewAdapter = new RecyclerGridViewAdapter(this, appInfoArrayList);
                        mRecyclerGridViewAdapter.setOnRecyclerViewItemListener(this);
                        recyclerView.setAdapter(mRecyclerGridViewAdapter);
                    } else {
                        mRecyclerGridViewAdapter.setAppInfoArrayList(appInfoArrayList);
                        mRecyclerGridViewAdapter.notifyDataSetChanged();
                    }
                }));
    }

    public void getAppList(ObservableEmitter<Boolean> emitter) {
        appInfoArrayList.clear();
        appInfoArrayList = myApplication.getShowAppList();
        emitter.onNext(true);
    }

    @Override
    public void onItemClickListener(int position, String packageName) {
        Log.d(TAG, "onItemClickListener packageName: " + packageName + " openDefaultMap " + openDefaultMap);
        if (openDefaultMap == 1) {
            openDefaultMap = 0;
            Settings.Global.putInt(getContentResolver(), CustomValue.OPEN_SET_DEFAULT_MAP, 0);
            Settings.Global.putString(getContentResolver(), CustomValue.DEFAULT_MAP, packageName);
        } else {
            mActivityTools.launchAppByPackageName(packageName, "null");
        }
    }

    @Override
    public void onItemLongClickListener(int position, String packageName) {
//        Log.d(TAG, "onItemLongClickListener packageName: " + packageName);
//        Uri uri = Uri.fromParts("package", packageName, null);
//        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
//        startActivity(intent);
    }

    @Override
    public void updateAppList() {
        initAppInfo();
    }

    private void registerAppReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
//        filter.addAction(Customer.ACTION_TXZ_CUSTOM_COMMAND);
        filter.addDataScheme("package");
        registerReceiver(mUpdateAppReceiver, filter);
    }

    @Override
    protected void onPause() {
        Settings.Global.putInt(getContentResolver(), CustomValue.OPEN_SET_DEFAULT_MAP, 0);
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUpdateAppReceiver != null) {
            unregisterReceiver(mUpdateAppReceiver);
        }
    }

}
