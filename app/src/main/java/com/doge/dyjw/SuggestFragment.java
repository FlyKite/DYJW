package com.doge.dyjw;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.doge.dyjw.util.Log;

/**
 * Created by 政 on 2015/8/30.
 */
public class SuggestFragment extends HolderFragment implements View.OnClickListener {

    private EditText suggestion;
    private EditText contact;
    private CheckBox submitPassword;
    private Button submit;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_suggest, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findView();
        progressDialog = new ProgressDialog(getActivity(), AlertDialog.THEME_HOLO_LIGHT);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnCancelListener(new CancelListener());
        progressDialog.setMessage(getString(R.string.posting));
        submit.setOnClickListener(this);
    }

    private void findView() {
        suggestion = (EditText) rootView.findViewById(R.id.suggestion);
        contact = (EditText) rootView.findViewById(R.id.contact);
        submitPassword = (CheckBox) rootView.findViewById(R.id.submit_password);
        submit = (Button) rootView.findViewById(R.id.submit);
    }

    Log log;
    @Override
    public void onClick(View v) {
        if (suggestion.getText().length() < 10) {
            Toast.makeText(getActivity(), R.string.length_too_short, Toast.LENGTH_SHORT).show();
            return;
        }
        log = new Log(getActivity());
        log.setDebug(true);
        log.appendLog(suggestion.getText().toString() + "\nContact:" + contact.getText().toString());
        task = new PostLogTask();
        task.execute(submitPassword.isChecked());
    }

    PostLogTask task;
    ProgressDialog progressDialog;
    class PostLogTask extends AsyncTask<Boolean, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Boolean... arg0) {
            return log.postLog(arg0[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.hide();
            if (result.contains("true")) {
                Toast.makeText(getActivity(), result.replace("true", ""), Toast.LENGTH_LONG).show();
                getActivity().finish();
            } else {
                Toast.makeText(getActivity(), result.replace("false", ""), Toast.LENGTH_LONG).show();
            }
        }
    }

    class CancelListener implements DialogInterface.OnCancelListener {

        @Override
        public void onCancel(DialogInterface dialog) {
            if (task != null) {
                task.cancel(true);
            }
        }
    }

    @Override
    public void onDestroy() {
        progressDialog.dismiss();
        super.onDestroy();
    }
}
