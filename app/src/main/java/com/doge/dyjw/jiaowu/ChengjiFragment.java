package com.doge.dyjw.jiaowu;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.doge.dyjw.MainApplication;
import com.doge.dyjw.R;

import java.util.List;
import java.util.Map;

public class ChengjiFragment extends Fragment {
    ChengjiTask cjTask;
    private Jiaowu jw;
    XueqiListTask listTask;
    private ProgressDialog progressDialog;
    private String request_xueqi = null;
    private ListView result;
    private List<Map<String, String>> scoreList;
    XiangqingTask xqTask;
    private TextView xuefen;
    private List<String> xueqiList = null;
    private Spinner xueqiSpinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chengji, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findView();
        progressDialog = new ProgressDialog(getActivity(), AlertDialog.THEME_HOLO_LIGHT);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnCancelListener(new CancelListener());
        getXueqiList();
    }

    class CancelListener implements OnCancelListener {

        @Override
        public void onCancel(DialogInterface dialog) {
            if (listTask != null) {
                listTask.cancel(true);
            }
            if (cjTask != null) {
                cjTask.cancel(true);
            }
            if (xqTask != null) {
                xqTask.cancel(true);
            }
        }
    }

    private void findView() {
        jw = ((MainApplication)getActivity().getApplicationContext()).getJiaowu();
        result = (ListView) getActivity().findViewById(R.id.result);
        xueqiSpinner = (Spinner) getActivity().findViewById(R.id.xueqi_list);
        xuefen = (TextView) getActivity().findViewById(R.id.xuefen);
    }

    private void getXueqiList() {
        listTask = new XueqiListTask();
        listTask.execute();
    }

    class XueqiListTask extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage(getString(R.string.cj_getting_xueqi));
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            xueqiList = jw.getXueqiList(true);
            return xueqiList != null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressDialog.hide();
            if (result) {
                setSpinner();
            } else {
                showMessage(getString(R.string.get_failed));
            }
        }
    }

    private void setSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, xueqiList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        xueqiSpinner.setAdapter(adapter);
        xueqiSpinner.setSelection(0);
        xueqiSpinner.setOnItemSelectedListener(new XueQiListener());
    }

    class XueQiListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View arg1, int position, long arg3) {
            if (request_xueqi != null) {
                request_xueqi = adapterView.getSelectedItem().toString();
                if (position == 1) {
                    request_xueqi = "";
                }
                getChengJi();
                return;
            }
            request_xueqi = adapterView.getSelectedItem().toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            request_xueqi = "";
        }
    }

    private void getChengJi() {
        cjTask = new ChengjiTask();
        cjTask.execute();
    }

    class ChengjiTask extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage(getString(R.string.cj_getting_chengji));
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            scoreList = jw.getChengJi(request_xueqi);
            return scoreList != null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressDialog.hide();
            if (result) {
                setList();
                xuefen.setText(jw.getXuefen() + "," +jw.getJidian());
                return;
            }
            showMessage(getString(R.string.get_failed));
        }
    }

    private void setList() {
        if (scoreList.size() == 0) {
            result.setAdapter(new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_list_item_1,
                    new String[]{getString(R.string.cj_no_chengji)}));
            return;
        }
        result.setAdapter(new SimpleAdapter(getActivity(), scoreList, R.layout.list_item_chengji,
                new String[]{"cj_pass", "cj_chengjixiangqing", "cj_kechengmingcheng",
                        "cj_zongchengji", "cj_chengjibiaozhi", "cj_kechengxingzhi",
                        "cj_kechengleibie", "cj_xueshi", "cj_xuefen", "cj_kaoshixingzhi",
                        "cj_buchongxueqi"},
                new int[]{R.id.cj_pass, R.id.cj_chengjixiangqing, R.id.cj_kechengmingcheng,
                        R.id.cj_zongchengji, R.id.cj_chengjibiaozhi, R.id.cj_kechengxingzhi,
                        R.id.cj_kechengleibie, R.id.cj_xueshi, R.id.cj_xuefen, R.id.cj_kaoshixingzhi,
                        R.id.cj_buchongxueqi}));
        result.setOnItemClickListener(new XiangqingListener());
    }

    class XiangqingListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View v, int arg2, long arg3) {
            TextView url = (TextView) v.findViewById(R.id.cj_chengjixiangqing);
            TextView name = (TextView) v.findViewById(R.id.cj_kechengmingcheng);
            xqTask = new XiangqingTask();
            xqTask.execute(url.getText().toString(), name.getText().toString());
        }
    }

    class XiangqingTask extends AsyncTask<String, Integer, String> {

        String name = "";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... arg0) {
            name = arg0[1];
            String res = jw.getChengJiXiangqing(arg0[0]);
            return res != null ? res : null;
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.hide();
            if (result != null) {
                new Builder(getActivity())
                        .setTitle(name)
                        .setMessage(result)
                        .setCancelable(false)
                        .setPositiveButton(R.string.confirm, null).show();
            } else {
                showMessage(getString(R.string.get_failed));
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
