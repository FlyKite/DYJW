package com.doge.dyjw.jiaowu;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.doge.dyjw.MainApplication;
import com.doge.dyjw.R;
import com.doge.dyjw.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PingjiaoInfoFragment extends Fragment {

	String url;
	public PingjiaoInfoFragment() {
        Bundle bundle = getArguments();
		url = bundle.getString("url");
	}
	
	private ProgressDialog progressDialog;
	private LinearLayout container;

    View rootView;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_blank, container, false);
		return rootView;
	}

    private Jiaowu jw;
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		container = (LinearLayout)rootView.findViewById(R.id.frag_container);
        jw = ((MainApplication)getActivity().getApplicationContext()).getJiaowu();
		progressDialog = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_LIGHT);
		progressDialog.setMessage(getString(R.string.loading));
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setOnCancelListener(new BackListener());
		getPingjiaoInfo();
	}
	
	GetPingjiaoInfoTask task;
	private void getPingjiaoInfo() {
		task = new GetPingjiaoInfoTask();
		task.execute();
	}
	
	class BackListener implements OnCancelListener {
		@Override
		public void onCancel(DialogInterface arg0) {
			task.cancel(true);
			getActivity().finish();
		}
	}

	Connection con;
	Document doc;
	Elements tr;
//	List<Map<String, Object>> list;
	class GetPingjiaoInfoTask extends AsyncTask<Void, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.show();
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			doc = jw.getPingjiaoInfo(url);
			if(doc != null) {
				Element tbl = doc.getElementById("table1");
				tr = tbl.select("tr");
				tr.remove(tr.size() - 1);
				return true;
			} else {
				return false;
			}
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			progressDialog.hide();
			if(result) {
				setInfo();
			} else {
				Toast.makeText(getActivity(), getString(R.string.get_failed), Toast.LENGTH_LONG).show();
			}
		}
		
	}
	
	ArrayList<RadioButton> radios;
//    private void setInfo() {
//
//    }
	private void setInfo() {
		radios = new ArrayList<>();
		Pattern p = Pattern.compile("value=\"\\w{5,}\"");
		Matcher m = p.matcher(doc.select("script").toString());
		ArrayList<String> values = new ArrayList<>();
		while(m.find()) {
            String value = m.group().replace("value=", "").replace("\"", "");
			values.add(value);
            Log.v("PingjiaoInfoValue", "value=" + value);
        }
		con = Jsoup.connect("http://jwgl.nepu.edu.cn/jxpjgl.do?method=savePj&tjfs=1&val=");
		con = con.data("type", "1");
		for(Element input : tr.get(0).select("input")) {
			if(!input.attr("name").equals("type")) {
				con = con.data(input.attr("name"), input.attr("value"));
			}
		}
		ScrollView sc = new ScrollView(getActivity());
		sc.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		LinearLayout main = new LinearLayout(getActivity());
		main.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		main.setOrientation(LinearLayout.VERTICAL);
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16F, dm);
		main.setPadding(padding, padding, padding, padding);
		for(int i = 1; i < tr.size(); i++) {
			Element row = tr.get(i);
			
			LinearLayout layout = new LinearLayout(getActivity());
			layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			layout.setOrientation(LinearLayout.VERTICAL);
			
			TextView title = new TextView(getActivity());
			title.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			title.setText(row.select("td").get(0).html());
			layout.addView(title);
			
			RadioGroup group = new RadioGroup(getActivity());
			group.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			group.setOrientation(RadioGroup.HORIZONTAL);
			group.setWeightSum(5F);
			Elements td = row.select("td");
			String value = "";
			if(values.size() > 0) {
				value = values.get(i - 1);
			}
			for(int j = 1; j < td.size(); j++) {
				Element input = td.get(j).select("input").get(0);
				RadioButton rbtn = new RadioButton(getActivity());
				rbtn.setLayoutParams(new RadioGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1F));
				rbtn.setText(6 - j + "");
				String val = input.attr("value");
				rbtn.setTag(val);
				group.addView(rbtn);
				if(val.substring(val.indexOf(",") + 1).equals(value)) {
					group.check(rbtn.getId());
				}
				radios.add(rbtn);
			}
			layout.addView(group);
			
			View line = new View(getActivity());
			int one_px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1F, dm);
			line.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, one_px));
			line.setBackgroundColor(Color.BLACK);
			layout.addView(line);
			
			main.addView(layout);
		}
		Button submit = new Button(getActivity());
		submit.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		int button_padding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15F, dm);
		submit.setPadding(0, button_padding, 0, button_padding);
		submit.setBackgroundResource(R.drawable.corner_button);
		submit.setTextColor(Color.WHITE);
		submit.setText(R.string.submit);
		submit.setOnClickListener(new SubmitListener());
		main.addView(submit);
		sc.addView(main);
		container.addView(sc);
	}
	
	class SubmitListener implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			String chooseColumn = "";
			String val = "";
			boolean same = true;
			int count = 0;
			for(RadioButton rbtn : radios) {
				if(rbtn.isChecked()) {
					if(!rbtn.getText().equals(chooseColumn)) {
						same = false;
					}
					if(count == 0) {
						con.data("ischeck", "on");
						con.data("zbmc", "教学态度");
					} else if(count == 3){
						con.data("ischeck", "on");
						con.data("zbmc", "教学内容");
					} else if(count == 6){
						con.data("ischeck", "on");
						con.data("zbmc", "教学方法与手段");
					} else if(count == 9){
						con.data("ischeck", "on");
						con.data("zbmc", "教学效果");
					}
					count++;
					chooseColumn = rbtn.getText().toString();
					val += rbtn.getTag() + "*";
					con = con.data("radio" + count, rbtn.getTag().toString());
				}
			}
			if(count != 12) {
				Toast.makeText(getActivity(), getString(R.string.select_all), Toast.LENGTH_LONG).show();
			} else {
				if(same) {
					Toast.makeText(getActivity(), getString(R.string.select_same), Toast.LENGTH_LONG).show();
				} else {
//					con = con.url(url + "&method=savePj&tjfs=1&val="
//					+ val.substring(0, val.length() - 1));
					con = Jsoup.connect("http://jwgl.nepu.edu.cn/jxpjgl.do?method=savePj&tjfs=1&val="
							+ val.substring(0, val.length() - 1))
							.data(con.request().data())
							.timeout(6000)
							.cookie("JSSESSIONID", jw.getJsessionId());
					Log.v("Referer", url);
                    con = con.header("Referer", url)
							.header("Host", "jwgl.nepu.edu.cn")
                            .header("DNT", "1")
							.header("Connection", "Keep-Alive")
							.header("Pragma", "no-cache")
							.header("Cache-Control", "no-cache")
							.header("Accept", "*/*")
							.header("Accept-Language", "zh-cn")
							.header("Content-Type", "application/x-www-form-urlencoded")
                            .header("Accept-Language", "zh-Hans-CN,zh-Hans;q=0.7,ja;q=0.3")
							.header("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.3; WOW64; Trident/7.0; Touch; .NET4.0E; .NET4.0C; Tablet PC 2.0; InfoPath.3; .NET CLR 3.5.30729; .NET CLR 2.0.50727; .NET CLR 3.0.30729)")
							.header("Accept-Encoding", "gzip, deflate")
							.header("JSESSIONID", "JSESSIONID=" + jw.getJsessionId())
							.timeout(6000)
                            .postDataCharset("utf-8")
							.cookie("JSSESSIONID", jw.getJsessionId());
					new AlertDialog.Builder(getActivity())
						.setMessage(R.string.pj_save)
						.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								PostTask task = new PostTask();
								task.execute();
							}
						})
						.setNegativeButton(R.string.no, null)
						.show();
				}
			}
		}
	}
	
	class PostTask extends AsyncTask<Void, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... arg0) {
			try {
				jw.postPingjiaoInfo(con);
//				con.post();
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			progressDialog.hide();
            getActivity().finish();
//			getFragmentManager().beginTransaction().remove(PingjiaoInfoFragment.this).commit();
		}
		
	}
	
	@Override
	public void onDestroy() {
		progressDialog.dismiss();
//		getFragmentManager().beginTransaction().remove(this).commit();
		super.onDestroy();
	}
}
