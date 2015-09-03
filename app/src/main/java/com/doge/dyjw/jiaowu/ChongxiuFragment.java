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

import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ChongxiuFragment extends Fragment {
    private ProgressDialog progressDialog;
    private TextView cx_time;
    private ListView result;
    private View rootView;
    private Jiaowu jw;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_chongxiu, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findView();
        jw = ((MainApplication)getActivity().getApplicationContext()).getJiaowu();
        progressDialog = new ProgressDialog(getActivity(), 3);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnCancelListener(new CancelListener());
        getChongxiu();
    }

    private void findView() {
        cx_time = (TextView) rootView.findViewById(R.id.cx_time);
        result = (ListView) rootView.findViewById(R.id.result);
    }

    class CancelListener implements DialogInterface.OnCancelListener {
        @Override
        public void onCancel(DialogInterface arg0) {
            if (chongxiuTask != null) {
                chongxiuTask.cancel(true);
            }
            getActivity().finish();
        }
    }

    private ChongxiuTask chongxiuTask;
    private void getChongxiu() {
        chongxiuTask = new ChongxiuTask();
        chongxiuTask.execute();
    }

    private List<Map<String, String>> chongxiuList = null;
    class ChongxiuTask extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            chongxiuList = jw.getChongxiuList();
            return chongxiuList != null;
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
        cx_time.setText(chongxiuList.get(0).get("cx_time"));
        chongxiuList.remove(0);
        result.setAdapter(new SimpleAdapter(getActivity(),
                chongxiuList, R.layout.list_item_chongxiu,
                new String[]{"cx_shifoubaoming", "cx_qudezige", "cx_xuenianxueqi",
                        "cx_kechengmingcheng", "cx_kechengbianhao", "cx_kaoshixingzhi",
                        "cx_kechengshuxing", "cx_kechengxingzhi", "cx_xueshi",
                        "cx_xuefen", "cx_shifouxuanke", "cx_shifoujiaofei",
                        "cx_xingzhi", "cx_baoming", "cx_quxiao", "status"},
                new int[]{R.id.cx_shifoubaoming, R.id.cx_qudezige, R.id.cx_xuenianxueqi
                        , R.id.cx_kechengmingcheng, R.id.cx_kechengbianhao, R.id.cx_kaoshixingzhi,
                        R.id.cx_kechengshuxing, R.id.cx_kechengxingzhi, R.id.cx_xueshi,
                        R.id.cx_xuefen, R.id.cx_shifouxuanke, R.id.cx_shifoujiaofei,
                        R.id.cx_xingzhi, R.id.cx_baoming, R.id.cx_quxiao, R.id.status}));
        result.setOnItemClickListener(new InfoListener());
    }

    class InfoListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
            String msg = ((TextView)view.findViewById(R.id.cx_shifoubaoming)).getText().toString();
            msg += "\n" + ((TextView)view.findViewById(R.id.cx_qudezige)).getText().toString();
            msg += "\n" + ((TextView)view.findViewById(R.id.cx_xuenianxueqi)).getText().toString();
            msg += "\n" + ((TextView)view.findViewById(R.id.cx_kechengbianhao)).getText().toString();
            msg += "\n" + ((TextView)view.findViewById(R.id.cx_kaoshixingzhi)).getText().toString();
            msg += "\n" + ((TextView)view.findViewById(R.id.cx_kechengshuxing)).getText().toString();
            msg += "\n" + ((TextView)view.findViewById(R.id.cx_kechengxingzhi)).getText().toString();
            msg += "\n" + ((TextView)view.findViewById(R.id.cx_xueshi)).getText().toString();
            msg += "\n" + ((TextView)view.findViewById(R.id.cx_xuefen)).getText().toString();
            msg += "\n" + ((TextView)view.findViewById(R.id.cx_shifouxuanke)).getText().toString();
            msg += "\n" + ((TextView)view.findViewById(R.id.cx_shifoujiaofei)).getText().toString();
            msg += "\n" + ((TextView)view.findViewById(R.id.cx_xingzhi)).getText().toString();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(((TextView) view.findViewById(R.id.cx_kechengmingcheng)).getText())
                    .setMessage(msg)
                    .setNegativeButton(R.string.back, null);
            final String baoming = ((TextView)view.findViewById(R.id.cx_baoming)).getText().toString();
            final String quxiao = ((TextView)view.findViewById(R.id.cx_quxiao)).getText().toString();
            final String status = ((TextView)view.findViewById(R.id.status)).getText().toString();
            if(status.equals("报名") || status.equals("取消报名")) {
                builder.setPositiveButton(status, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 报名
                        String url = status.equals("报名") ? baoming : quxiao;
                        new SubmitTask().execute(url);
                    }
                });
            } else {
                builder.setPositiveButton(status, null);
            }
            builder.show();
        }
    }
    class SubmitTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage(getString(R.string.posting));
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... url) {
            try {
                Jsoup.connect(url[0]).cookie("JSESSIONID", jw.getJsessionId()).post();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressDialog.hide();
            if (result) {
                getChongxiu();
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
