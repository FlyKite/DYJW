package com.doge.dyjw;

import android.app.Application;

/**
 * Created by 政 on 2015/6/9.
 */
public class CrashApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
    }
}
