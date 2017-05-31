package com.example.kirito.wechatrecordbutton;

import android.app.Application;

import com.iflytek.cloud.SpeechUtility;

/**
 * Created by jiawei on 2017/5/26.
 */

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        SpeechUtility.createUtility(BaseApplication.this, "appid=" + getString(R.string.app_id));
        super.onCreate();
    }
}
