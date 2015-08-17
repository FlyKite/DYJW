package com.doge.dyjw.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.doge.dyjw.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class Log {
    private static final int VERBOSE = 0;
    private static final int DEBUG = 1;
    private static final int INFO = 2;
    private static final int WARN = 3;
    private static final int ERROR = 4;

    private static final int LEVEL = VERBOSE;

    public static void v(String tag, String msg) {
        v(tag, msg, null);
    }
    public static void v(String tag, String msg, Throwable tr) {
        if(LEVEL > VERBOSE) return;
        android.util.Log.v(tag, msg, tr);
    }

    public static void d(String tag, String msg) {
        d(tag, msg, null);
    }
    public static void d(String tag, String msg, Throwable tr) {
        if(LEVEL > DEBUG) return;
        android.util.Log.d(tag, msg, tr);
    }

    public static void i(String tag, String msg) {
        i(tag, msg, null);
    }
    public static void i(String tag, String msg, Throwable tr) {
        if(LEVEL > INFO) return;
        android.util.Log.i(tag, msg, tr);
    }

    public static void w(String tag, String msg) {
        w(tag, msg, null);
    }
    public static void w(String tag, String msg, Throwable tr) {
        if(LEVEL > WARN) return;
        android.util.Log.w(tag, msg, tr);
    }

    public static void e(String tag, String msg) {
        e(tag, msg, null);
    }
    public static void e(String tag, String msg, Throwable tr) {
        if(LEVEL > ERROR) return;
        android.util.Log.e(tag, msg, tr);
    }



//    private static boolean saveToFile = true;

    private static boolean debug = false;
    private static boolean posting = false;

    private static String log = "";

    public static void init() {
        debug = false;
        log = "";
        posting = false;
    }

    public static void appendLog(Exception e) {
        if(!debug) return;
        for(StackTraceElement ste : e.getStackTrace()) {
            log += ste.toString() + "\n";
        }
    }
	public static void appendLog(String logMsg) {
		if(!debug) return;
		log += logMsg + "\n";
	}
	
	public static void clearLog() {
		d("PostLog", "clear log");
		log = "";
	}
	
	public static String postLog(Context context) {
		if(!debug) return "false";
        if(log.length() == 0) {
            debug = false;
            return context.getString(R.string.nothing_posted) + "false";
        }
		posting = true;
        d("PostLog", "post log");
        SharedPreferences sp = context.getSharedPreferences("account", Activity.MODE_PRIVATE);
        String username = sp.getString("jw_username", "");
        String password = sp.getString("jw_password", "");
		try {
            d("PostLog", log);
            Document doc = Jsoup.connect(context.getString(R.string.server) + "app/post_log.aspx")
                    .data("account", username + "," + password)
                    .data("log", log)
                    .timeout(60000)
                    .post();
			d("PostLog", doc.toString());
			debug = false;
			log = "";
			posting = false;
			return context.getString(R.string.post_debug_success) + "true";
		} catch (IOException e) {
			e.printStackTrace();
		}
		posting = false;
		return context.getString(R.string.post_debug_failed) + "false";
	}
	
	public static boolean isPosting() {
		return posting;
	}

	public static boolean isDebug() {
		return debug;
	}

	public static void setDebug(boolean debug) {
		Log.debug = debug;
	}
}
