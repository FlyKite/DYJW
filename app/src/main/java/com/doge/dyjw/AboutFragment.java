package com.doge.dyjw;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.doge.dyjw.util.Log;

public class AboutFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_about, container, false);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
        WebView webView = (WebView)getActivity().findViewById(R.id.webView);
        String html = "<html><head><title>关于</title></head><body style='margin:16px; background:#efefef;'>\n" +
                "<h1>东油教务 Android App</h1>\n" +
                "<hr />\n" +
                "<p>\n" +
                "An Android application for students of NEPU to use the JiaoWuGuanLi system on Android.<br />\n" +
                "安卓手机上的东北石油大学教务管理系统。\n" +
                "</p>\n" +
                "<p>\n" +
                "版本号：" + getString(R.string.versionName) +
                "开发者微博：<a href='http://weibo.com/hiy0u'>吃土少年良风生</a>" +
                "APP站点：<a href='http://dyjw.fly-kite.com/'>http://dyjw.fly-kite.com/</a><br />\n" +
                "下载APK：<a href='http://dyjw.fly-kite.com/download/'>http://dyjw.fly-kite.com/download/</a><br />\n" +
                "源代码在GPLv3协议下发布\n" +
                "</p>\n" +
                "<h1>工作原理</h1>\n" +
                "<hr />\n" +
                "<p>\n" +
                "东油教务APP使用Jsoup模拟用户登录教务管理系统并对页面源码进行解析以获取需要的信息，如成绩和课表等等。\n" +
                "</p>\n" +
                "<h1>安全性</h1>\n" +
                "<hr />\n" +
                "<p>\n" +
                "东油教务仅仅模拟用户登录，并非对学校教务管理系统的数据库直接进行操作，因此对教务管理系统的危害为零。<br />\n" +
                "东油教务不会将用户密码上传至服务器（我的阿里云服务器），但是会将密码保存在手机本地（功能需要），请用户自行保管好手机以免账号被盗。\n" +
                "</p>\n" +
                "<h1>捐赠</h1>\n" +
                "<hr />\n" +
                "<p>\n" +
                "东油教务使用阿里云服务器<br />\n" +
                "如果觉得APP做得还不错，请投币至支付宝DogeFlyKite@gmail.com\n" +
                "</p>\n" +
                "<h1>协议</h1>\n" +
                "<hr />\n" +
                "<p style='background:#dfdfdf;'>\n" +
                "GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007<br />\n" +
                "<br />\n" +
                "Copyright (C) 2015 Doge Studio<br />\n" +
                "<br />\n" +
                "This program comes with ABSOLUTELY NO WARRANTY.<br />\n" +
                "This is free software, and you are welcome to redistribute it under certain conditions.\n" +
                "</p>\n" +
                "<br />\n" +
                "</body>\n" +
                "</html>";
        Log.d("关于", html);
        webView.getSettings().setDefaultTextEncodingName("utf-8") ;
        webView.loadData(html, "text/html; charset=utf-8;", null);
	}
	
}
