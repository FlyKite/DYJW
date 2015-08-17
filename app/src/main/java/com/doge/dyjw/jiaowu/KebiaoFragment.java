package com.doge.dyjw.jiaowu;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.doge.dyjw.R;
import com.doge.dyjw.view.CourseView;
import com.doge.dyjw.util.DBHelper;

import java.util.List;
import java.util.StringTokenizer;

public class KebiaoFragment extends Fragment {
    private CourseView courseView = null;
    private Jiaowu jw;
    KebiaoTask kbTask;
    XueqiListTask listTask;
    private TextView no_kebiao = null;
    NewXueqiTask nxTask = null;
    private ProgressDialog progressDialog;
    private String request_xueqi = null;
    private List<String> xueqiList = null;
    private Spinner xueqiSpinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_kebiao, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        jw = new Jiaowu();
        xueqiSpinner = (Spinner) getActivity().findViewById(R.id.xueqi_list);
        LinearLayout kLayout = (LinearLayout) getActivity().findViewById(R.id.fragment_kebiao);
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        courseView = new CourseView(getActivity(), dm);
        kLayout.addView(courseView);
        no_kebiao = new TextView(getActivity());
        no_kebiao.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        no_kebiao.setText(getActivity().getString(R.string.kb_no_kebiao));
        no_kebiao.setGravity(Gravity.CENTER);
        kLayout.addView(no_kebiao);
        progressDialog = new ProgressDialog(getActivity(), AlertDialog.THEME_HOLO_LIGHT);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnCancelListener(new CancelListener());
        getXueqiList();
    }

    class CancelListener implements OnCancelListener {
        @Override
        public void onCancel(DialogInterface arg0) {
            listTask.cancel(true);
            kbTask.cancel(true);
            nxTask.cancel(true);
            getActivity().finish();
        }
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
            xueqiList = jw.getXueqiList(false);
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
        ArrayAdapter<String> localArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, xueqiList);
        localArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        xueqiSpinner.setAdapter(localArrayAdapter);
        xueqiSpinner.setSelection(0);
        xueqiSpinner.setOnItemSelectedListener(new XueQiListener());
    }

    class XueQiListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View arg1, int position, long arg3) {
            request_xueqi = adapterView.getSelectedItem().toString();
            if (position != 0) {
                getKebiao();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            request_xueqi = "";
        }
    }

    private void getKebiao() {
        kbTask = new KebiaoTask();
        kbTask.execute(request_xueqi);
    }

    class KebiaoTask extends AsyncTask<String, Void, Boolean> {
        String[][] course;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage(getString(R.string.kb_getting_kebiao));
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... xueqi) {
            course = jw.getKebiao(xueqi[0]);
            return course != null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            progressDialog.hide();
            if (!result) {
                nxTask = new NewXueqiTask();
                nxTask.execute(request_xueqi);
            } else {
                saveCourse(course);
                courseView.setCourse(course);
                courseView.setVisibility(View.VISIBLE);
            }
        }
    }

    class NewXueqiTask extends AsyncTask<String, Integer, Boolean> {
        String[][] course;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage(getString(R.string.kb_getting_kebiao));
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... xueqi) {
            course = new Jiaowu().getNewKebiao(xueqi[0]);
            return course != null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressDialog.hide();
            if (!result) {
                showMessage(getString(R.string.get_failed));
                courseView.setVisibility(View.GONE);
            } else {
                courseView.setCourse(course);
                courseView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void saveCourse(String[][] course) {
        String tb_name = "course_" + request_xueqi.replace("-", "_");
        SQLiteDatabase db = new DBHelper(getActivity(), "course.db").getWritableDatabase();
        db.execSQL("create table if not exists " + tb_name + "(cname varchar(1024), " + "clas varchar(1024), " + "teacher varchar(1024), " + "weeks varchar(1024), " + "room varchar(1024), " + "id int primary key)");
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                if (course[i][j].length() > 2) {
                    StringTokenizer stk = new StringTokenizer(course[i][j], "\n");
                    String cname = stk.nextToken();
                    String clas = stk.nextToken();
                    String teacher = stk.nextToken();
                    String weeks = stk.nextToken();
                    String room = stk.nextToken();
                    db.execSQL("insert or ignore into " + tb_name + " values(?,?,?,?,?,?)", new String[]{cname, clas, teacher, weeks, room, ((i * 7) + j) + ""});
                }
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
