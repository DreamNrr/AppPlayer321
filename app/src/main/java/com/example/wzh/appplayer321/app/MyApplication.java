package com.example.wzh.appplayer321.app;

import android.app.Application;

import org.xutils.x;

/**
 * Created by WZH on 2017/5/23.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
    }
}
