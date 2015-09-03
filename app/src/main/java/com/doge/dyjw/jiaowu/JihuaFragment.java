package com.doge.dyjw.jiaowu;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.doge.dyjw.MainApplication;
import com.doge.dyjw.R;

import java.util.List;
import java.util.Map;

public class JihuaFragment extends Fragment {
    private ListView jihua;
    private List<Map<String, String>> jihuaList;
    JihuaListTask jihuaTask;
    private Jiaowu jw;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_jihua, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findView();
        progressDialog = new ProgressDialog(getActivity(), 3);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnCancelListener(new CancelListener());
        getJihua();
    }

    class CancelListener implements OnCancelListener {

        @Override
        public void onCancel(DialogInterface arg0) {
            if (jihuaTask != null) {
                jihuaTask.cancel(true);
            }
            getActivity().finish();
        }
    }

    private void findView() {
        jw = ((MainApplication)getActivity().getApplicationContext()).getJiaowu();
        jihua = (ListView) getActivity().findViewById(R.id.jihua);
    }

    private void getJihua() {
        jihuaTask = new JihuaListTask();
        jihuaTask.execute();
    }

    class JihuaListTask extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            jihuaList = jw.getJihuaList();
            return jihuaList != null;
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
        jihua.setAdapter(new SimpleAdapter(getActivity(),
                jihuaList, R.layout.list_item_jihua,
                new String[]{"jh_kechengmingcheng", "jh_kaikexueqi", "jh_kechengbianma",
                        "jh_xueshi", "jh_xuefen", "jh_tixi",
                        "jh_shuxing", "jh_kaikedanwei", "jh_kaohefangshi"},
                new int[]{R.id.jh_kechengmingcheng, R.id.jh_kaikexueqi, R.id.jh_kechengbianma
                        , R.id.jh_xueshi, R.id.jh_xuefen, R.id.jh_tixi,
                        R.id.jh_shuxing, R.id.jh_kaikedanwei, R.id.jh_kaohefangshi}));
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
