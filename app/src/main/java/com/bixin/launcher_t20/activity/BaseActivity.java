package com.bixin.launcher_t20.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.trello.rxlifecycle2.components.RxActivity;

import io.reactivex.disposables.CompositeDisposable;

public abstract class BaseActivity extends RxActivity {
    protected CompositeDisposable mDisposable;
    protected Context mContext;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        int option = window.getDecorView().getSystemUiVisibility()
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        window.getDecorView().setSystemUiVisibility(option);
        window.setStatusBarColor(Color.TRANSPARENT);
        super.onCreate(savedInstanceState);
        setContentView(getLayout());
        this.mContext = this;
        this.mDisposable = new CompositeDisposable();
        init();
        initView();
    }

    public abstract int getLayout();

    public abstract void init();

    public abstract void initView();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mContext = null;
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
            mDisposable.clear();
            mDisposable = null;
        }
    }
}
