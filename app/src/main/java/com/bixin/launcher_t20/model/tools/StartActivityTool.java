package com.bixin.launcher_t20.model.tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.bixin.launcher_t20.R;
import com.bixin.launcher_t20.activity.LauncherApp;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.List;

import static android.content.ContentValues.TAG;


/**
 * @author Altair
 * @date :2019.10.21 下午 02:18
 * @description:
 */
public class StartActivityTool {
    private Context mContext;

    public StartActivityTool() {
        this.mContext = LauncherApp.getInstance();
    }

    /**
     * 根据action跳转
     *
     * @param action action
     */
    public void jumpByAction(String action) {
        Intent intent = new Intent(action);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    public void jumpToActivity(Activity activity, Class aClass) {
        Intent intent = new Intent(activity, aClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    /**
     * 根据包名启动应用
     *
     * @param packageName clicked app
     */
    public void launchAppByPackageName(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            Log.i(TAG, "package name is null!");
            return;
        }

        Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent == null) {
            ToastTool.showToast(R.string.app_not_install);
        } else {
            mContext.startActivity(launchIntent);
        }
    }

    /**
     * 根据包名启动应用
     *
     * @param packageName clicked app
     */
    public void launchAppByPackageName(String packageName, String name) {
        if (TextUtils.isEmpty(packageName)) {
            Log.i(TAG, "package name is null!");
            return;
        }

        Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent == null) {
            ToastTool.showToast(R.string.app_not_install);
        } else {
            mContext.startActivity(launchIntent);
            sendBToRCX(packageName, name);
        }
    }

    /**
     * 根据包名启动应用
     *
     * @param packageName clicked app
     */
    public void launchAppByPackageName(String packageName, boolean isTrue) {
        if (TextUtils.isEmpty(packageName)) {
            Log.i(TAG, "package name is null!");
            return;
        }

        Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent == null) {
            ToastTool.showToast(R.string.app_not_install);
        } else {
            launchIntent.putExtra("front", isTrue);
            mContext.startActivity(launchIntent);
        }
    }

    /**
     * 返回桌面
     */
    public void goHome() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);// "android.intent.action.MAIN"
        intent.addCategory(Intent.CATEGORY_HOME); //"android.intent.category.HOME"
        mContext.startActivity(intent);
    }

    public void openTXZView() {
        Intent it = new Intent("com.txznet.txz.config.change");
        it.putExtra("type", "screen");
        it.putExtra("x", 0);
        it.putExtra("y", 0);
//        it.putExtra("width", 1024);
//        it.putExtra("height", 517);
        mContext.sendBroadcast(it);
//        TXZAsrManager.getInstance().triggerRecordButton();
    }

    public boolean killPackageName(String packageName) {
        OutputStream out = null;
        try {
            ActivityManager am =
                    (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            am.killBackgroundProcesses(packageName);

            Process process = Runtime.getRuntime().exec("su");
            String cmd = "am force-stop " + packageName + " \n";
            out = process.getOutputStream();
            out.write(cmd.getBytes());
            out.flush();
        } catch (IOException e) {
            Log.e(TAG, " e: " + e.getMessage());
            return false;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    /**
     * 根据包名强制关闭一个应用，不管前台应用还是后台进程，需要share systemuid
     * 需要权限 FORCE_STOP_PACKAGES
     *
     * @param packageName app package name
     */
    public void stopApps(String packageName) {
        try {
            ActivityManager am =
                    (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            Method forceStopPackage = am.getClass().getDeclaredMethod("forceStopPackage",
                    String.class);
            forceStopPackage.setAccessible(true);
            forceStopPackage.invoke(am, packageName);
            System.out.println("TimerV force stop package " + packageName + " successful");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("TimerV force stop package " + packageName + " error!");
        }
    }

    public void killApp(String packageName) {
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        assert am != null;
        List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : processes) {
            String processName = info.processName;
            Log.d(TAG, "killApp: " + processName + " size: " + processes.size());
            if (processName.equals(packageName)) {
                am.killBackgroundProcesses(processName);
                android.os.Process.killProcess(info.pid);
            }
        }
    }

    @SuppressLint("NewApi")
    public void startVoiceRecognitionService() {
        Log.d(TAG, "startVoiceRecognitionService: ");
//        Intent intent = new Intent();
//        String packageName = "com.bixin.speechrecognitiontool";
//        String className = "com.bixin.speechrecognitiontool.SpeechRecognitionService";
//        intent.setClassName(packageName, className);
//        mContext.startForegroundService(intent);
        launchAppByPackageName("com.bixin.speechrecognitiontool");
    }

    public int getWeatherIcon(String weather) {
        Log.d(TAG, "getWeatherIcon: weather " + weather);
        int iconId = 0;
        switch (weather) {
            case "多云":
                iconId = R.drawable.ic_weather_cloudy;
                break;
            case "阴":
            case "阴天":
                iconId = R.drawable.ic_weather_cloudy_day;
                break;
            case "阵雨":
                iconId = R.drawable.ic_weather_shower;
                break;
            case "雷阵雨伴有冰雹":
                iconId = R.drawable.ic_weather_thunder_shower;
                break;
            case "雷阵雨":
                iconId = R.drawable.ic_weather_thunder_storm_and_hail;
                break;
            case "小雨":
            case "雨":
                iconId = R.drawable.ic_weather_light_rain;
                break;
            case "中雨":
                iconId = R.drawable.ic_weather_moderate_rain;
                break;
            case "大雨":
                iconId = R.drawable.ic_weather_heavy_rain;
                break;
            case "暴雨":
                iconId = R.drawable.ic_weather_baoyu;
                break;
            case "大暴雨":
                iconId = R.drawable.ic_weather_dabaoyu;
                break;
            case "特暴大雨":
                iconId = R.drawable.ic_weather_torrential_rain;
                break;
            case "冰雨":
                iconId = R.drawable.ic_weather_ice_rain;
                break;
            case "小雪":
                iconId = R.drawable.ic_weather_light_snow;
                break;
            case "中雪":
                iconId = R.drawable.ic_weather_medium_snow;
                break;
            case "大雪":
                iconId = R.drawable.ic_weather_heavy_snow;
                break;
            case "暴雪":
                iconId = R.drawable.ic_weather_greate_heavy_snow;
                break;
            case "雨夹雪":
                iconId = R.drawable.ic_weather_sleet;
                break;
            case "阵雪":
                iconId = R.drawable.ic_weather_snow_shower;
                break;
            case "雾":
                iconId = R.drawable.ic_weather_fog;
                break;
            case "霾":
                iconId = R.drawable.ic_weather_haze;
                break;
            case "浮尘":
                iconId = R.drawable.ic_weather_float_dust;
                break;
            case "扬沙":
                iconId = R.drawable.ic_weather_raise_sand;
                break;
            case "沙尘暴":
                iconId = R.drawable.ic_weather_dust_storm;
                break;
            default:    // 晴天
                iconId = R.drawable.ic_weather_sunny_day;
                break;

        }
        return iconId;
    }

    public void startRCX() {
        LauncherApp.getInstance().startService(new Intent()
                .setClassName("com.mapgoo.diruite",
                        "com.mapgoo.smart.service.DataSyncServiceSDK"));
    }

    public void sendBToRCX(String packageName, String name) {
        Intent intent = new Intent();
        intent.setAction("com.mapgoo.broadcast.recv.renchexing");
        intent.putExtra("KEY_TYPE", 10017);
        intent.putExtra("EXTRA_PKG", packageName);
        intent.putExtra("EXTRA_NAME", name);
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        mContext.sendBroadcast(intent);
    }

    public void startECarService() {
        ComponentName cn = new ComponentName("com.ecar.assistantnew",
                "com.ecar.assistantnew.service.BootService");
        Intent intent = new Intent();
        intent.setComponent(cn);
        mContext.stopService(intent);
        mContext.startService(intent);
    }

    public void startValidationTools() {
        String path = StoragePaTool.getStoragePath(true);
        Log.d(TAG, "startValidationTools: " + path);
        if (path != null) {
            path = path + "/BixinTest";
            File file = new File(path);
            if (file.exists()) {
                Intent intent = new Intent();
                ComponentName cn = new ComponentName("com.sprd.validationtools",
                        "com.sprd.validationtools.ValidationToolsMainActivity");
                intent.setComponent(cn);
                mContext.startActivity(intent);
                Log.d(TAG, "startValidationTools: OK");
            } else {
                Log.d(TAG, "startValidationTools: !exists");
            }
        }
    }

}
