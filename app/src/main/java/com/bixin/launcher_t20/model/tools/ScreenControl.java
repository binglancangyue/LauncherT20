package com.bixin.launcher_t20.model.tools;

import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.util.Log;

import com.bixin.launcher_t20.activity.LauncherApp;
import com.bixin.launcher_t20.model.receiver.ScreenOffAdminReceiver;

import static android.content.ContentValues.TAG;
import static android.content.Context.DEVICE_POLICY_SERVICE;
import static android.content.Context.POWER_SERVICE;

/**
 * @author Altair
 * @date :2019.11.23 上午 11:28
 * @description:
 */
public class ScreenControl {
    private ComponentName componentName;
    private DevicePolicyManager policyManager;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;

    @SuppressLint("InvalidWakeLockTag")
    public void init() {
        Context context = LauncherApp.getInstance();
        componentName = new ComponentName(context, ScreenOffAdminReceiver.class);
        mPowerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        policyManager =
                (DevicePolicyManager) context.getSystemService(DEVICE_POLICY_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
    }

    /**
     * 检测并去激活设备管理器权限
     */
    public void checkAndTurnOnDeviceManager(Context context) {
        if (!policyManager.isAdminActive(componentName)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "开启后可以使用息屏功能");
            context.startActivity(intent);
        }
    }

    /**
     * 亮屏
     */
    public void checkScreenOn() {
        mWakeLock.acquire();
        mWakeLock.release();
    }

    /**
     * 息屏
     */
    public void checkScreenOff() {
        boolean admin = policyManager.isAdminActive(componentName);
        if (admin) {
            policyManager.lockNow();
        } else {
            Log.d(TAG, "checkScreenOff:没有设备管理权限 ");
        }
       /* handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkScreenOn();
            }
        }, 10 * 1000);*/
    }

    /**
     * 移除设备管理员
     */
    public void removeActiveAdmin() {
        policyManager.removeActiveAdmin(componentName);
        Intent intent = new Intent();
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setAction("android.intent.action.VIEW");
        intent.setData(Uri.parse("package:" + LauncherApp.getInstance().getPackageName()));
        LauncherApp.getInstance().startActivity(intent);
    }
}
