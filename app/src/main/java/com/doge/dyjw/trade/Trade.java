package com.doge.dyjw.trade;

import android.content.Context;

import com.doge.dyjw.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 政 on 2015/9/14.
 */
public class Trade {
    private Context context;
    public Trade(Context context) {
        this.context = context;
    }
    public List<Map<String, Object>> getItems(int page) throws IOException {
        Connection con = Jsoup.connect(context.getString(R.string.server)
                + "app/lmhp/item.aspx?page=" + page);
        Document doc = con.get();
        String json = doc.body().text();
        List<Map<String, Object>> list = null;
        try {
            JSONArray ja = new JSONArray(json + "");
            list = new ArrayList<>();
            for (int i = 0; i < ja.length(); i++) {
                //{"id" : 1, "nickname" : "121501140113", "logo" : "", "title" : "黎明湖畔测试1", "price" : 9999, "images" : "", "description" : "测试黎明湖畔功能专用", "type_name" : "", "pubtime" : "2015/7/20 0:40:39", }
                JSONObject jo = (JSONObject) ja.get(i);
                HashMap<String, Object> map = new HashMap<>();
                map.put("id", jo.getString("id"));
                map.put("nickname", jo.getString("nickname"));
//                map.put("logo", ImageGetter.get(context.getString(R.string.server)
//                        , "images/logo/" + jo.getString("logo")));
                map.put("logo", null);
                map.put("title", jo.getString("title"));
                map.put("price", "￥" + jo.getString("price"));
                map.put("images", jo.getString("images"));
                map.put("description", jo.getString("description"));
                map.put("type_name", jo.getString("type_name"));
                map.put("pubtime", jo.getString("pubtime"));
                list.add(map);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}
