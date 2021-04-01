package com.bixin.launcher_t20.activity;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.Settings;
import android.util.Log;

import com.bixin.launcher_t20.model.bean.AppInfo;
import com.bixin.launcher_t20.model.tools.CustomValue;

import java.util.ArrayList;
import java.util.List;

public class LauncherApp extends Application {
    private static LauncherApp myApplication = null;
    public ArrayList<AppInfo> mAppList = new ArrayList<>();
    private boolean isFirstLaunch = true;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        myApplication = this;
//        init();
    }

    public static LauncherApp getInstance() {
        return myApplication;
    }

    public void init() {
        new Thread(() -> {
//            setDefaultIME();
//            initTXZ();
        }).start();
    }

    private void setDefaultIME() {
        ContentResolver resolver = getContentResolver();
        Settings.Secure.putString(resolver, Settings.Secure.DEFAULT_INPUT_METHOD,
                "com.sohu.inputmethod.sogou/.SogouIME");
    }

    /**
     * 获得所有安装的APP
     */
    public ArrayList<AppInfo> initAppList() {
        mAppList.clear();
        PackageManager pm = getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN, null);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> apps = pm.queryIntentActivities(main, 0);
//        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(pm));
        for (ResolveInfo resolveInfo : apps) {
            AppInfo info = new AppInfo();
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (!ignoreApp(activityInfo.packageName)) {
                info.setAppName(resolveInfo.loadLabel(pm).toString());
                info.setPkgName(activityInfo.packageName);
                info.setFlags(activityInfo.flags);
                info.setAppIcon(activityInfo.loadIcon(pm));
                info.setAppIntent(pm.getLaunchIntentForPackage(activityInfo.packageName));
                mAppList.add(info);
            }
            Log.d("MyApplication",
                    "AppList apps info: " + activityInfo.packageName);
        }
        return mAppList;
    }

    public ArrayList<AppInfo> getShowAppList() {
        return initAppList();
    }

    /**
     * 过滤显示
     *
     * @param pkgName 包名
     * @return true or false
     */
/*    private boolean ignoreApp(String pkgName) {
        if (pkgName.equals(CustomValue.PACKAGE_NAME_AUTONAVI)
                || pkgName.equals(CustomValue.PACKAGE_NAME_KWMUSIC)
                || pkgName.equals(CustomValue.PACKAGE_NAME_SETTINGS)
                || pkgName.equals(CustomValue.PACKAGE_NAME_E_DOG)
                || pkgName.equals(CustomValue.PACKAGE_NAME_DSJ)
                || pkgName.equals(CustomValue.PACKAGE_NAME_FILE_MANAGER)
                || pkgName.equals(CustomValue.PACKAGE_NAME_FM)
                || pkgName.equals(CustomValue.PACKAGE_NAME_GPS)
                || pkgName.equals(CustomValue.PACKAGE_NAME_MAP_TOOL)
                || pkgName.equals(CustomValue.PACKAGE_NAME_ViDEO_PLAY_BACK)
                || pkgName.equals(CustomValue.PACKAGE_NAME_RCX)
                || pkgName.equals("com.mapgoo.media.dvrx")) {
            return true;
        }
        return false;
    }*/
    private boolean ignoreApp(String pkgName) {
        if (pkgName.equals("com.bixin.launcher_t20")
                || pkgName.equals("com.bixin.speechrecognitiontool")
                || pkgName.equals("com.txznet.adapter")
                || pkgName.equals(CustomValue.PACKAGE_NAME_SOHU)) {
            return true;
        }
        return false;
    }

    public boolean isFirstLaunch() {
        return this.isFirstLaunch;
    }

    public void setFirstLaunch(boolean isFirst) {
        this.isFirstLaunch = isFirst;
    }

}
