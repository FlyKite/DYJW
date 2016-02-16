package com.doge.dyjw.jiaowu;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.doge.dyjw.MainApplication;
import com.doge.dyjw.R;
import com.doge.dyjw.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by 政 on 2016/2/15.
 */
public class BukaoFragmeng extends Fragment {
    private ProgressDialog progressDialog;
    private View rootView;
    private Jiaowu jw;
    private TextView kbkc;
    private TextView ybkc;
    private ListView kbkc_list;
    private ListView ybkc_list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_bukao, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findView();
        jw = ((MainApplication)getActivity().getApplicationContext()).getJiaowu();
        progressDialog = new ProgressDialog(getActivity(), AlertDialog.THEME_HOLO_LIGHT);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnCancelListener(new CancelListener());
        getBukao();
    }

    private void findView() {
        kbkc = (TextView) rootView.findViewById(R.id.kbkc);
        ybkc = (TextView) rootView.findViewById(R.id.ybkc);
        kbkc_list = (ListView) rootView.findViewById(R.id.kbkc_list);
        ybkc_list = (ListView) rootView.findViewById(R.id.ybkc_list);
    }

    class CancelListener implements DialogInterface.OnCancelListener {
        @Override
        public void onCancel(DialogInterface arg0) {
            if (bukaoTask != null) {
                bukaoTask.cancel(true);
            }
            getActivity().finish();
        }
    }
    private BukaoTask bukaoTask;
    private void getBukao() {
        bukaoTask = new BukaoTask();
        bukaoTask.execute();
    }

    private List<Map<String, String>> kbkcList = null;
    private List<Map<String, String>> ybkcList = null;
    class BukaoTask extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            kbkcList = jw.getBukaoKebao(true);
            ybkcList = jw.getBukaoKebao(false);
            return kbkcList != null && ybkcList != null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressDialog.hide();
            if (result) {
                setList();
            } else {
                showMessage(getString(R.string.get_failed));
            }
        }
    }

    private void setList() {
        kbkc.setText(kbkcList.get(0).get("kbkc"));
        kbkcList.remove(0);
        kbkc_list.setAdapter(new SimpleAdapter(getActivity(),
                kbkcList, R.layout.list_item_bukao,
                new String[]{"bk_kaikexueqi", "bk_kechengmingcheng", "bk_kechengbianhao",
                        "bk_kaoshixingzhi", "bk_kechengshuxing", "bk_kechengxingzhi",
                        "bk_xueshi", "bk_xuefen", "bk_zongchengji", "bk_shifoujiaofei",
                        "bmid", "status"},
                new int[]{R.id.bk_kaikexueqi, R.id.bk_kechengmingcheng, R.id.bk_kechengbianhao,
                        R.id.bk_kaoshixingzhi, R.id.bk_kechengshuxing, R.id.bk_kechengxingzhi,
                        R.id.bk_xueshi, R.id.bk_xuefen, R.id.bk_zongchengji, R.id.bk_shifoujiaofei,
                        R.id.bmid, R.id.status}));
        kbkc_list.setOnItemClickListener(new InfoListener());

        ybkc.setText(ybkcList.get(0).get("ybkc"));
        ybkcList.remove(0);
        ybkc_list.setAdapter(new SimpleAdapter(getActivity(),
                ybkcList, R.layout.list_item_bukao,
                new String[]{"bk_kaikexueqi", "bk_kechengmingcheng", "bk_kechengbianhao",
                        "bk_kaoshixingzhi", "bk_kechengshuxing", "bk_kechengxingzhi",
                        "bk_xueshi", "bk_xuefen", "bk_zongchengji", "bk_shifoujiaofei",
                        "bmid", "status"},
                new int[]{R.id.bk_kaikexueqi, R.id.bk_kechengmingcheng, R.id.bk_kechengbianhao,
                        R.id.bk_kaoshixingzhi, R.id.bk_kechengshuxing, R.id.bk_kechengxingzhi,
                        R.id.bk_xueshi, R.id.bk_xuefen, R.id.bk_zongchengji, R.id.bk_shifoujiaofei,
                        R.id.bmid, R.id.status}));
        ybkc_list.setOnItemClickListener(new InfoListener());
    }

    class InfoListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
            String msg = ((TextView)view.findViewById(R.id.bk_kaikexueqi)).getText().toString();
            msg += "\n" + ((TextView)view.findViewById(R.id.bk_kechengbianhao)).getText().toString();
            msg += "\n" + ((TextView)view.findViewById(R.id.bk_kaoshixingzhi)).getText().toString();
            msg += "\n" + ((TextView)view.findViewById(R.id.bk_kechengshuxing)).getText().toString();
            msg += "\n" + ((TextView)view.findViewById(R.id.bk_kechengxingzhi)).getText().toString();
            msg += "\n" + ((TextView)view.findViewById(R.id.bk_xueshi)).getText().toString();
            msg += "\n" + ((TextView)view.findViewById(R.id.bk_xuefen)).getText().toString();
            msg += "\n" + ((TextView)view.findViewById(R.id.bk_zongchengji)).getText().toString();
            String shifoujiaofei = ((TextView)view.findViewById(R.id.bk_zongchengji)).getText().toString();
            if (!shifoujiaofei.equals("")) msg += "\n" + shifoujiaofei;
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(((TextView) view.findViewById(R.id.bk_kechengmingcheng)).getText())
                    .setMessage(msg)
                    .setNegativeButton(R.string.back, null);
            final String status = ((TextView)view.findViewById(R.id.status)).getText().toString();
            if(status.equals("报名") || status.equals("取消报名")) {
                builder.setPositiveButton(status, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 报名
                        String bmid = ((TextView)view.findViewById(R.id.bmid)).getText().toString();
                        Connection con = Jsoup.connect("http://jwgl.nepu.edu.cn/bkglAction.do?method=bkbmList&operate="
                                + (status.equals("报名") ? "kbkc" : "ybkc"))
                                .data("cj0716id", bmid)
                                .data("type", status.equals("报名") ? "bm" : "qx")
                                .timeout(6000)
                                .cookie("JSESSIONID", jw.getJsessionId());
                        new SubmitTask().execute(con);
                    }
                });
            } else {
                builder.setPositiveButton(status, null);
            }
            builder.show();
        }
    }

    class SubmitTask extends AsyncTask<Connection, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage(getString(R.string.posting));
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Connection... con) {
            try {
                Document doc = con[0].post();
                Log.d("补考报名", doc.toString());
                String html = doc.toString();
                String result = html.substring(html.indexOf("alert('") + 7, html.indexOf("')"));
                return result;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.hide();
            if (result != null) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(result)
                        .setPositiveButton(R.string.confirm, null)
                        .show();
                getBukao();
            } else {
                showMessage(getString(R.string.post_failed));
            }
        }
    }

    private void showMessage(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        progressDialog.dismiss();
        super.onDestroy();
    }
}
