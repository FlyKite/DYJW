package com.doge.dyjw;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;

import com.doge.dyjw.jiaowu.Jiaowu;
import com.doge.dyjw.util.Log;

/**
 * Created by 政 on 2015/6/9.
 */
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (Log.LEVEL > Log.DEBUG) {
            CrashHandler crashHandler = CrashHandler.getInstance();
            crashHandler.init(getApplicationContext());
        }
    }

    private Log log = null;
    public Log getLog() {
        if (log == null) {
            log = new Log(this);
        }
        return log;
    }

    private Jiaowu jiaowu = null;
    public Jiaowu getJiaowu() {
        if (jiaowu == null) {
            jiaowu = new Jiaowu(getLog());
            SharedPreferences sp = getSharedPreferences("account", Activity.MODE_PRIVATE);
            String xh = sp.getString("jw_username", "");
            String pwd = sp.getString("jw_password", "");
            String jsessionid = sp.getString("jw_session_id", "");
            String name = sp.getString("jw_name", "");
            jiaowu.setAccount(xh, pwd);
            jiaowu.setJsessionId(jsessionid);
            jiaowu.setName(name);
        }
        return jiaowu;
    }
}
