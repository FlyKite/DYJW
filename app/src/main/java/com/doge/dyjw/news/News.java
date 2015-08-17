package com.doge.dyjw.news;

import android.text.Html;

import com.doge.dyjw.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class News {
	
	public static String JWC = "350312.html";
	public static String DY_YAOWEN = "520202.html";
	
	private String path;
	private String url;
	
	public News(String listPath) {
		this.path = listPath;
		if(path.charAt(0) == '3') {
			url = "http://glbm1.nepu.edu.cn/jwc/dwr/call/plaincall/portalAjax.getNewsXml.dwr";
		} else if(path.charAt(0) == '5') {
			url = "http://news.nepu.edu.cn/dwr/call/plaincall/portalAjax.getNewsXml.dwr";
		}
	}
	public News() {
		
	}

	public List<Map<String, Object>> getNewsList(int pageNum) throws ClientProtocolException, IOException {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		List<Map<String, Object>> list = new ArrayList<>();
		HttpPost post = new HttpPost(url);
		String s = "callCount=1"
				+ "\npage=/jwc/type/" + path
				+ "\nhttpSessionId=0"
				+ "\nscriptSessionId=0"
				+ "\nc0-scriptName=portalAjax"
				+ "\nc0-methodName=getNewsXml"
				+ "\nc0-id=0"
				+ "\nc0-param0=string:" + path.substring(0, 4)
				+ "\nc0-param1=string:" + path.subSequence(0, 6)
				+ "\nc0-param2=string:news_"
				+ "\nc0-param3=number:15"
				+ "\nc0-param4=number:" + pageNum
				+ "\nc0-param5=null:null"
				+ "\nc0-param6=null:null"
				+ "\nbatchId=0";
		HttpEntity entity = new StringEntity(s, "UTF-8");
		post.setEntity(entity);
		HttpResponse response = httpClient.execute(post);
		String html = EntityUtils.toString(response.getEntity());
		html = html.substring(html.indexOf("<?xml version"), html.lastIndexOf("\");"));
		Document doc = Jsoup.parse(unicodeToString(html));
		Elements items = doc.select("item");
		for(Element item : items) {
			Map<String, Object> map = new HashMap<>();
			String title = Html.fromHtml(item.select("title").html())
					.toString()
					.replace("<![CDATA[","")
					.replace("]]>", "");
			map.put("title", title);
			map.put("url", item.select("guid").html());
			map.put("top", item.select("lmmc").html());
			String d = item.select("pubDate").html();
			Date date = new Date(d);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			map.put("bottom", sdf.format(date));
			list.add(map);
            Log.v("News", title);
            Log.v("News", item.select("guid").html());
            Log.v("News", item.select("lmmc").html());
            Log.v("News", sdf.format(date));
            Log.v("News", "-------------------------------------");
		}
		return list;
	}
	
	private String title;
	private String content;
	private String info = "";
	public void getNews(String url) throws IOException {
		Connection con = Jsoup.connect(url).timeout(6000);
		Document doc = con.get();
		title = doc.select("div.title h3").get(0).html();
		content = doc.select("div.xwcon").get(0).html();
		info += doc.select("div.title h4 span.puber").get(0).html() + "\t\t";
//		info += doc.select("div.title h4 span.bm").get(0).html() + "\n";
		info += doc.select("div.title h4 span.pubtime").get(0).html();
//		info += doc.select("div.title h4 span.fwl").get(0).html();
//		info += doc.select("div.title h4 span.num").get(0).html();
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getContent() {
		return content;
	}
	
	public String getInfo() {
		return info;
	}

	private String unicodeToString(String str) {
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(str);
        char ch;
        while (matcher.find()) {
            ch = (char) Integer.parseInt(matcher.group(2), 16);
            str = str.replace(matcher.group(1), ch + "");
        }
        return str;
    }
	
}
