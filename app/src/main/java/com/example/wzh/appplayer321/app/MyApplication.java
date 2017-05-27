package com.example.wzh.appplayer321.app;

import android.app.Application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import org.xutils.x;


/**
 * Created by WZH on 2017/5/23.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        SpeechUtility.createUtility(this, SpeechConstant.APPID +"=5928e7f4");
    }
}
