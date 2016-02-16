package com.doge.dyjw.jiaowu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.doge.dyjw.R;
import com.doge.dyjw.util.Log;
import com.doge.dyjw.jiaowu.PingjiaoFragment.Option;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Connection;
import org.jsoup.Connection.KeyVal;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Jiaowu {
	private Connection con;
	private String pwd = null;
	private String xh = null;
	private String name = null;
	private String JSESSIONID;
	private String xuefen;
	private String jidian;
	private String bjbh;

	public void setAccount(String username, String password) {
		xh = username;
		pwd = password;
	}

    private Log log;
    public Jiaowu(Log log) {
        this.log = log;
    }

	public ArrayList<String> getXueqiList(boolean allXueqi) {
        log.appendLog("&#tstart;--------------获取学期列表及bjbh--------------&#tend;");
		try {
			con = Jsoup.connect("http://jwgl.nepu.edu.cn/tkglAction.do?method=kbxxXs")
					.timeout(6000).cookie("JSESSIONID", JSESSIONID);
			Document doc = con.get();
			log.appendLog(doc.select("body").toString());
			bjbh = doc.getElementsByAttributeValue("name", "bjbh").val();
			Element elm = doc.getElementById("xnxqh");
			ArrayList<String> ar = new ArrayList<>();
			for(Element el: elm.select("option")) {
				ar.add(el.html());
				if(el.html().equals("---请选择---") && allXueqi) {
					ar.add("全部学期");
				}
			}
			return ar;
		} catch (Exception e) {
            e.printStackTrace();
			log.appendLog("&#estart;===========================================");
            log.appendLog(e);
			return null;
		} finally {
			log.appendLog("&#cend;");
		}
	}

	public List<Map<String, String>> getChengJi(String kkxq) {
        log.appendLog("&#tstart;--------------获取成绩列表--------------&#tend;");
		try {
			con = Jsoup.connect("http://jwgl.nepu.edu.cn/xszqcjglAction.do?method=queryxscj")
					.data("kksj", kkxq)
					.data("kcxz", "")
					.data("kcmc", "")
					.data("xsfs", "")
					.timeout(6000)
					.cookie("JSESSIONID", JSESSIONID);
			Document doc = con.post();
			log.appendLog(doc.select("body").toString());//学分和绩点
			Element jd = doc.getElementById("tblBm").select("td").get(0);
			xuefen = jd.select("span").get(1).html();
			jidian = jd.select("span").get(3).html();
			//成绩表格,每行为一条记录
			Element elm = doc.getElementById("mxh");
			List<Map<String, String>> al = new ArrayList<>();
			Elements els = elm.getAllElements().select("tr");
			for(Element e : els) {
				HashMap<String, String> hashMap = new HashMap<>();
				Elements infos = e.select("td");
				Element cheng = infos.get(5).select("a").get(0);
				try {
					int chengji = Integer.parseInt(cheng.html());
					if (chengji < 60) {
						hashMap.put("cj_pass", "×");
					} else {
						hashMap.put("cj_pass", "√");
					}
				} catch(Exception ex) {
					hashMap.put("cj_pass", cheng.html().charAt(0) + "");
				}
				String xiangqing = cheng.attr("onclick");
				hashMap.put("cj_kechengmingcheng", infos.get(4).html());
				hashMap.put("cj_zongchengji", "总成绩：" + cheng.html());
				hashMap.put("cj_chengjixiangqing", xiangqing.substring(xiangqing.indexOf("/"), xiangqing.lastIndexOf("'")));
				hashMap.put("cj_chengjibiaozhi", "成绩标志：" + infos.get(6).html().replace("&nbsp;", " "));
				hashMap.put("cj_kechengxingzhi", "课程性质：" + infos.get(7).html());
				hashMap.put("cj_kechengleibie", "课程类别：" + infos.get(8).html());
				hashMap.put("cj_xueshi", "学时：" + infos.get(9).html());
				hashMap.put("cj_xuefen", "学分：" + infos.get(10).html());
				hashMap.put("cj_kaoshixingzhi", "考试性质：" + infos.get(11).html());
				hashMap.put("cj_buchongxueqi", "补重学期：" + infos.get(12).html().replace("&nbsp;", " "));
				al.add(hashMap);
			}
			return al;
		} catch (Exception e) {
            log.appendLog(e);
			log.appendLog("&#estart;===========================================");
			e.printStackTrace();
			return null;
		} finally {
			log.appendLog("&#cend;");
		}
	}

	public String getChengJiXiangqing(String url) {
        log.appendLog("&#tstart;--------------获取成绩详情--------------&#tend;");
		try {
			con = Jsoup.connect("http://jwgl.nepu.edu.cn" + url)
					.timeout(6000)
					.cookie("JSESSIONID", JSESSIONID);
			Document doc = con.post();
			log.appendLog(doc.select("body").toString());
            Elements tabs = doc.getElementById("tblHead").select("th");
            Elements items = doc.getElementById("mxh").select("td");
            String result = "";
            for(int i = 0; i < tabs.size(); i++) {
                result += tabs.get(i).text() + "：" + items.get(i).text() + "\n";
            }
            result = result.substring(0, result.length() - 1);
			return result;
		} catch (Exception e) {
			log.appendLog("&#estart;===========================================");
			e.printStackTrace();
			log.appendLog(e);
			return null;
		} finally {
			log.appendLog("&#cend;");
		}
	}

	public Bitmap getVerifyCode() {
		Bitmap verifyCode = null;
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setIntParameter("http.socket.timeout",5000);
			HttpResponse response = httpClient
                    .execute(new HttpGet("http://jwgl.nepu.edu.cn/verifycode.servlet"));
			int i = response.getStatusLine().getStatusCode();
			if (i == 200) {
				byte[] bytes = EntityUtils.toByteArray(response.getEntity());
				verifyCode = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
			}
			List<Cookie> cookies = httpClient.getCookieStore().getCookies();
			JSESSIONID = cookies.get(0).getValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return verifyCode;
	}
	
	public String[][] getKebiao(String xueqi) {
        log.appendLog("&#tstart;--------------获取课表详情--------------&#tend;");
		try {
			con = Jsoup.connect("http://jwgl.nepu.edu.cn/tkglAction.do")
				.data("method", "goListKbByXs")
				.data("sql", "")
				.data("xnxqh", xueqi)
				.data("zc", "")
				.data("xs0101id", xh)
				.timeout(6000)
				.cookie("JSESSIONID", JSESSIONID);
			Document doc = con.post();
			log.appendLog(doc.select("body").toString());
			Element tb = doc.getElementById("kbtable");
			if(tb == null) {
				return null;
			}
			Elements tr = tb.select("tr");
			String[][] course = new String[6][7];
			for(int i = 1; i < 7; i++) {
				Elements td = tr.get(i).select("td");
				for(int j = 1; j < 8; j++) {
					course[i-1][j-1] = td.get(j).select("div").get(1).html();
					course[i-1][j-1] = course[i-1][j-1].replace("\n", "")
							.replace(" ", "")
							.replace("&nbsp;", "")
							.replace("<nobr>", "")
							.replace("</nobr>", "");
					if(course[i-1][j-1].length() > 5) {
                        Log.d("course", course[i-1][j-1]);
						course[i-1][j-1] = course[i-1][j-1].substring(0, course[i-1][j-1].length() - 4)
                                .replace("<br>", "\n")
                                .replace("<br/>", "\n");
					}
				}
			}
			return course;
		} catch(Exception e) {
            e.printStackTrace();
			log.appendLog("&#estart;===========================================");
            log.appendLog(e);
			return null;
		} finally {
			log.appendLog("&#cend;");
		}
	}
	
	public String[][] getNewKebiao(String xueqi) {
        log.appendLog("&#tstart;--------------获取新学期课表--------------&#tend;");
		try {
			String url = "http://jwgl.nepu.edu.cn/tkglAction.do?method=printExcel&sql=&type=xsdy&bjbh="
					+ bjbh + "&xnxqh=" + xueqi + "&xsid=" + xh + "&excelFs=server";
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);
			String cookie = "JSESSIONID=" + JSESSIONID + ";";
	        request.addHeader("cookie", cookie);
			HttpResponse response = client.execute(request);
			String html = EntityUtils.toString(response.getEntity());
			log.appendLog(html);
			String body = html.substring(html.indexOf("<body"), html.indexOf("body>") + 5);
			Document doc = Jsoup.parse(body);
			Elements tr = doc.select("table tr");
			String[][] course = new String[6][7];
			for(int i = 3; i < 9; i++) {
				Elements td = tr.get(i).select("td");
				Log.v("NewCourse", tr.get(i).toString());
				for(int j = 1; j < 8; j++) {
					course[i-3][j-1] = td.get(j).html().replace(" ", "")
                            .replace("<br>", "\n")
                            .replace("<br/>", "\n");
				}
			}
			return course;
		} catch (Exception e) {
            e.printStackTrace();
			log.appendLog("&#estart;===========================================");
            log.appendLog(e);
			return null;
		} finally {
			log.appendLog("&#cend;");
		}
	}

    //教学评价暂时有问题----------
	public ArrayList<ArrayList<Option>> getPingjiao() {
		log.appendLog("&#tstart;--------------获取评教信息--------------&#tend;");
		try {
			con = Jsoup.connect("http://jwgl.nepu.edu.cn/jiaowu/jxpj/jxpjgl_queryxs.jsp")
					.timeout(15000)
					.cookie("JSESSIONID", JSESSIONID);
			Elements els = con.get().select("form select");
			log.appendLog(els.toString());
			ArrayList<ArrayList<Option>> als = new ArrayList<ArrayList<Option>>();
			for(Element e : els) {
				ArrayList<Option> al = new ArrayList<Option>();
				for(Element o : e.select("option")) {
					boolean selected = false;
					if(o.toString().contains("selected")) {
						selected = true;
					}
					Option option = new Option(o.html(), o.attr("value"), selected);
					al.add(option);
				}
				als.add(al);
			}
			return als;
		} catch (Exception e) {
			e.printStackTrace();
			log.appendLog("&#estart;===========================================");
			log.appendLog(e);
		} finally {
			log.appendLog("&#cend;");
		}
		return null;
	}
	
	public List<Map<String, Object>> getPingjiaoList(String xnxq, String pjpc, String pjfl, String pjkc) {
		log.appendLog("&#tstart;--------------获取评教列表--------------&#tend;");
		try {
			con = Jsoup.connect("http://jwgl.nepu.edu.cn/jxpjgl.do?method=queryJxpj&type=xs")
					.data("ok", "")
					.data("xnxq", xnxq)
					.data("pjpc", pjpc)
					.data("pjfl", pjfl)
					.data("pjkc", pjkc)
					.data("sfxsyjzb", "")
					.data("cmdok", "查 询")
					.data("zbnrstring", "")
					.timeout(6000)
					.cookie("JSESSIONID", JSESSIONID);
			Document doc = con.post();
			log.appendLog(doc.select("body").toString());
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			if(doc.getElementById("mxh") == null) {
				return null;
			}
			for(Element item : doc.getElementById("mxh").select("tr")) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("title", item.select("td").get(4).html());
				map.put("top", item.select("td").get(5).html());
				map.put("bottom", "已评：" + item.select("td").get(7).html());
				String onclick = item.select("a").get(0).attr("onclick");
				String url = onclick.substring(onclick.indexOf("/"), onclick.lastIndexOf("',"));
				url = "http://jwgl.nepu.edu.cn" + url;
				map.put("url", url);
				list.add(map);
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			log.appendLog("&#estart;===========================================");
			log.appendLog(e);
		} finally {
			log.appendLog("&#cend;");
		}
		return null;
	}
	
	public Document getPingjiaoInfo(String url) {
		log.appendLog("&#tstart;--------------获取评教信息--------------&#tend;");
		try {
			con = Jsoup.connect(url)
					.timeout(6000)
					.cookie("JSESSIONID", JSESSIONID);
			Document doc = con.get();
			log.appendLog(doc.select("body").toString());
			Elements els = doc.select("input");
			System.out.println(doc.getElementsByAttributeValue("name", "zbmc"));
			System.out.println(doc.getElementsByAttributeValue("name", "isCheck"));
			for(Element e : els) {
				Log.v("PingjiaoInput", e.attr("name") + "====" + e.attr("value"));
			}
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
			log.appendLog("&#estart;===========================================");
			log.appendLog(e);
		} finally {
			log.appendLog("&#cend;");
		}
		return null;
	}
	
	public void postPingjiaoInfo(Connection conn) throws IOException {
		try {
			Collection<KeyVal> c = conn.request().data();
			Log.v("PostPingjiaoInfo", "url=" + conn.request().url().toString());
			for(Object obj : c.toArray()) {
				KeyVal k = (KeyVal) obj;
				Log.v("PostPingjiaoInfo", k.key() + "----" + k.value());
			}
			conn = conn.timeout(6000)
                .header("JSESSIONID", "JSESSIONID=" + JSESSIONID)
                .cookie("JSSESSIONID", JSESSIONID);
			Document doc = conn.post();
			System.out.println(doc.toString());
		} catch (Exception e) {
			e.printStackTrace();
			log.appendLog("&#estart;===========================================");
			log.appendLog(e);
		} finally {
			log.appendLog("&#cend;");
		}
	}

	public List<Map<String, String>> getJihuaList() {
        log.appendLog("&#tstart;--------------获取教学计划--------------&#tend;");
		try {
			con = Jsoup.connect("http://jwgl.nepu.edu.cn/pyfajhgl.do?method=toViewJxjhXs")
					.timeout(6000)
					.cookie("JSESSIONID", JSESSIONID);
			Document doc = con.post();
			log.appendLog(doc.select("body").toString());
			List<Map<String, String>> al = new ArrayList<>();
			Elements els = doc.select("#mxh tr");
			int duoyu = doc.select("#mxh tr").get(0).select("td").size() - 11;
			for(Element e : els) {
				HashMap<String, String> hashMap = new HashMap<>();
				Elements infos = e.select("td");
				hashMap.put("jh_kechengmingcheng", infos.get(3).text());
				hashMap.put("jh_kaikexueqi", "开课学期：" + infos.get(1).text());
				hashMap.put("jh_kechengbianma", "课程编码：" + infos.get(2).text());
				hashMap.put("jh_xueshi", "总学时：" + infos.get(4).text());
				hashMap.put("jh_xuefen", "学分：" + infos.get(5).text());
				hashMap.put("jh_tixi", "课程体系：" + infos.get(6).text());
				hashMap.put("jh_shuxing", "课程属性：" + infos.get(7).text());
				hashMap.put("jh_kaikedanwei", "开课单位：" + infos.get(10 + duoyu).text());
				hashMap.put("jh_kaohefangshi", "考核方式：" + infos.get(9 + duoyu).text());
				al.add(hashMap);
			}
            Collections.sort(al, new Comparator<Map<String, String>>() {
                @Override
                public int compare(Map<String, String> lhs, Map<String, String> rhs) {
                    int xueqi = lhs.get("jh_kaikexueqi").compareTo(rhs.get("jh_kaikexueqi"));
                    int kaohefangshi = xueqi == 0 ? lhs.get("jh_kaohefangshi").compareTo(rhs.get("jh_kaohefangshi"))*-1 : xueqi;
                    int kechengmingcheng = kaohefangshi == 0 ? lhs.get("jh_kechengmingcheng").compareTo(rhs.get("jh_kechengmingcheng")) : kaohefangshi;
                    return kechengmingcheng;
                }
            });
			return al;
		} catch (Exception e) {
            e.printStackTrace();
			log.appendLog("&#estart;===========================================");
            log.appendLog(e);
			return null;
		} finally {
			log.appendLog("&#cend;");
		}
	}

    public List<Map<String, String>> getChongxiuList() {
        log.appendLog("&#tstart;--------------获取重修列表--------------&#tend;");
        try {
            con = Jsoup.connect("http://jwgl.nepu.edu.cn/zxglAction.do?method=xszxbmList")
                    .timeout(6000)
                    .cookie("JSESSIONID", JSESSIONID);
            Document doc = con.post();
            log.appendLog(doc.select("body").toString());
            List<Map<String, String>> al = new ArrayList<>();
            // 报名时间
            String cx_time = doc.select("#tbTable td").get(0).text();
            HashMap<String, String> time = new HashMap<>();
            time.put("cx_time", cx_time);
            al.add(time);
            //是否可报名
             boolean sfkbm = doc.toString().contains("var sfkbm = \"true\"");
            // 重修课程
            Elements els = doc.select("#mxh tr");
            for(Element e : els) {
                HashMap<String, String> hashMap = new HashMap<>();
                Elements infos = e.select("td");
                hashMap.put("cx_shifoubaoming", "是否报名：" + infos.get(0).text());
                hashMap.put("cx_shangkeyuansheng", "上课院审：" + infos.get(1).text());
                hashMap.put("cx_kaikeyuansheng", "开课院审：" + infos.get(2).text());
                hashMap.put("cx_qudezige", "取得资格：" + infos.get(3).text());
                hashMap.put("cx_xuenianxueqi", "学年学期：" + infos.get(4).text());
                hashMap.put("cx_kechengmingcheng", infos.get(5).text());
                hashMap.put("cx_kechengbianhao", "课程编号：" + infos.get(6).text());
                hashMap.put("cx_kaoshixingzhi", "考试性质：" + infos.get(7).text());
                hashMap.put("cx_kechengshuxing", "课程属性：" + infos.get(8).text());
                hashMap.put("cx_kechengxingzhi", "课程性质：" + infos.get(9).text());
                hashMap.put("cx_xueshi", "学时：" + infos.get(10).text());
                hashMap.put("cx_xuefen", "学分：" + infos.get(11).text());
                hashMap.put("cx_shifouxuanke", "是否选课：" + infos.get(17).text());
                hashMap.put("cx_shifoujiaofei", "是否缴费：" + infos.get(18).text());
                hashMap.put("cx_xingzhi", "性质：" + infos.get(19).text());
                String server = "http://jwgl.nepu.edu.cn";
                String baoming = infos.get(20).select("a").get(0).attr("onclick");
                baoming = server + baoming.substring(baoming.indexOf("('") + 2, baoming.indexOf("')"));
                hashMap.put("cx_baoming", baoming);
                String quxiao = infos.get(21).select("a").get(0).attr("onclick");
                quxiao = server + quxiao.substring(quxiao.indexOf("('") + 2, quxiao.indexOf("')"));
                hashMap.put("cx_quxiao", quxiao);
                if(sfkbm) {
                    if(infos.get(0).text().equals("√")) {
                        if(infos.get(2).text().equals("－")) {
                            //已审核时不可取消
                            hashMap.put("status", "审核时不可取消");
                        } else {
                            hashMap.put("status", "取消报名");
                        }
                    } else {
                        hashMap.put("status", "报名");
                    }
                } else {
                    hashMap.put("status", "不可操作");
                }
                if(infos.get(18).text().equals("√")) {
                    hashMap.put("status", "已缴费不可取消");
                }
                al.add(hashMap);
            }
            return al;
        } catch (Exception e) {
            e.printStackTrace();
            log.appendLog("&#estart;===========================================");
            log.appendLog(e);
            return null;
        } finally {
            log.appendLog("&#cend;");
        }
    }

    public List<Map<String, String>> getBukaoKebao(boolean kebaoOrYibao) {
        log.appendLog("&#tstart;--------------获取补考可报列表--------------&#tend;");
        try {
            con = Jsoup.connect("http://jwgl.nepu.edu.cn/bkglAction.do?method=bkbmList&operate="
                    + (kebaoOrYibao ? "kbkc" : "ybkc"))
                    .timeout(6000)
                    .cookie("JSESSIONID", JSESSIONID);
            Document doc = con.post();
            log.appendLog(doc.select("body").toString());
            List<Map<String, String>> al = new ArrayList<>();
            // 标题及报名时间
            String title = doc.select("#tbTable td").get(0).text();
            HashMap<String, String> time = new HashMap<>();
            time.put(kebaoOrYibao ? "kbkc" : "ybkc", title);
            al.add(time);
            // 是否可报名
            boolean sfkbm = doc.toString().contains("var sfkbm = \"1\"");
            // 补考课程
            Elements els = doc.select("#mxh tr");
            for(Element e : els) {
                HashMap<String, String> hashMap = new HashMap<>();
                Elements infos = e.select("td");
                hashMap.put("bk_kaikexueqi", "开课学期：" + infos.get(0).text());
                hashMap.put("bk_kechengmingcheng", infos.get(1).text());
                hashMap.put("bk_kechengbianhao", "课程编号：" + infos.get(2).text());
                hashMap.put("bk_kaoshixingzhi", "考试性质：" + infos.get(3).text());
                hashMap.put("bk_kechengshuxing", "课程属性：" + infos.get(4).text());
                hashMap.put("bk_kechengxingzhi", "课程性质：" + infos.get(5).text());
                hashMap.put("bk_xueshi", "学时：" + infos.get(6).text());
                hashMap.put("bk_xuefen", "学分：" + infos.get(7).text());
                hashMap.put("bk_zongchengji", "总成绩：" + infos.get(8).text());
                if (!kebaoOrYibao)  hashMap.put("bk_shifoujiaofei", "是否缴费：" + infos.get(9).text());
                else  hashMap.put("bk_shifoujiaofei", "");
                String bmid = infos.get(9 + (kebaoOrYibao ? 0 : 1)).select("a").get(0).attr("onclick");
                bmid = bmid.substring(bmid.indexOf("('") + 2, bmid.indexOf("')"));
                hashMap.put("bmid", bmid);
                if(sfkbm) {
                    if(kebaoOrYibao) {
                        hashMap.put("status", "报名");
                    } else {
                        hashMap.put("status", "取消报名");
                    }
                } else {
                    hashMap.put("status", "不可操作");
                }
                if(!kebaoOrYibao &&  infos.get(9).text().equals("是")) {
                    hashMap.put("status", "已缴费不可取消");
                }
                al.add(hashMap);
            }
            return al;
        } catch (Exception e) {
            e.printStackTrace();
            log.appendLog("&#estart;===========================================");
            log.appendLog(e);
            return null;
        } finally {
            log.appendLog("&#cend;");
        }
    }
	
	public boolean loginJW(String verifyCode, Context context) throws IOException {
        SharedPreferences sp = context.getSharedPreferences("account", Activity.MODE_PRIVATE);
		con = Jsoup.connect("http://jwgl.nepu.edu.cn/Logon.do?method=logon")
				.data("USERNAME", xh)
				.data("PASSWORD", pwd)
				.data("RANDOMCODE", verifyCode)
				.timeout(6000)
				.cookie("JSESSIONID", JSESSIONID);
		if (con.post().toString().contains("window.location.href='http://jwgl.nepu.edu.cn/framework/main.jsp'")) {
			con = Jsoup.connect("http://jwgl.nepu.edu.cn/framework/main.jsp")
					.timeout(6000).cookie("JSESSIONID", JSESSIONID);
			Document doc = con.post();
			String title = doc.select("title").get(0).html();
			name = title.substring(0, title.indexOf("["));
			System.out.println(name);
			con = Jsoup.connect("http://jwgl.nepu.edu.cn/Logon.do?method=logonBySSO")
					.timeout(6000).cookie("JSESSIONID", JSESSIONID);
			con.post();
			con = Jsoup.connect("http://jwgl.nepu.edu.cn/yhxigl.do?method=editUserInfo")
					.data("account", xh)
					.data("realName", name)
					.data("pwdQuestion1", "")
					.data("pwdAnswer1", "")
					.data("pwdQuestion2", "")
					.data("pwdAnswer2", "")
					.data("pageSize", "200")
					.data("zjftxt", "")
					.data("kyjftxt", "")
					.timeout(6000).cookie("JSESSIONID", JSESSIONID);
			con.post();
            sp.edit().putString("jw_username", xh)
                    .putString("jw_password", pwd)
                    .putString("jw_name", name)
                    .putString("jw_session_id", JSESSIONID)
                    .putLong("jw_lasttime", System.currentTimeMillis())
                    .commit();
            Intent intent = new Intent(context.getString(R.string.broadcast_login));
            intent.putExtra("msg", "login");
            context.sendBroadcast(intent);
            loginToServer(context);
            return true;
		}
        sp.edit().clear().commit();
		return false;
	}

    public void loginToServer(Context context) throws IOException {
        String server = context.getString(R.string.server);
        PackageInfo pi = null;
        try {
            pi = context.getPackageManager().getPackageInfo("com.doge.dyjw", 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int versionCode = pi.versionCode;
        con = Jsoup.connect(server + "app/login.aspx")
                .data("username", xh)
                .data("last_jid", JSESSIONID)
                .data("versioncode", versionCode + "")
                .timeout(10000);
        con.post();
    }

    public void setName(String name) {
        this.name = name;
    }

	public String getName() {
		return name;
	}
	
	public void clearName() {
		name = null;
	}

    public void setJsessionId(String JsessionID) {
        JSESSIONID = JsessionID;
    }
    public String getJsessionId() {
        return JSESSIONID;
    }
	
	public String getXuefen() {
		return "已修学分：" + xuefen;
	}
	
	public String getJidian() {
		return "绩点：" + jidian;
	}
	
	public String getXuehao() {
		return xh;
	}
	
	public String getPwd() {
		return pwd;
	}
	
	public String getBjbh() {
		return bjbh;
	}
}
