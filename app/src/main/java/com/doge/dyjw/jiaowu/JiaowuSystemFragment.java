package com.doge.dyjw.jiaowu;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.doge.dyjw.BuildConfig;
import com.doge.dyjw.HolderFragment;
import com.doge.dyjw.R;

import java.io.IOException;

public class JiaowuSystemFragment extends HolderFragment {
    private Jiaowu jw;
    private LinearLayout loadingImage;
    private Button loginButton;
    private LoginTask loginTask;
    private EditText password;
    private View rootView;
    private EditText username;
    private EditText verifyCode;
    private ImageView verifyCodeImage;
    private VerifyCodeTask verifyCodeTask;

    class LoginTask extends AsyncTask<Void, Integer, Boolean> {
        private String pwd;
        SharedPreferences sp;
        private String verify;
        private String xh;

        LoginTask() {
        }

        protected void onPreExecute() {
            super.onPreExecute();
            xh = username.getText().toString();
            pwd = password.getText().toString();
            verify = verifyCode.getText().toString();
            loginButton.setText(R.string.loging);
            sp = getActivity().getSharedPreferences("account", 0);
        }

        protected Boolean doInBackground(Void... param) {
            System.out.println("LoginTask-----------------------------------");
            try {
                Jiaowu.setAccount(xh, pwd);
                if (jw.loginJW(verify, getActivity())) {
                    return Boolean.valueOf(true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Boolean.valueOf(false);
        }

        protected void onPostExecute(Boolean result) {
            if (result.booleanValue()) {
                saveAccount();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.toggleSoftInput(1, 2);
                }
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                Fragment mainPanelFrag = new JiaowuPanelFragment();
                transaction.remove(JiaowuSystemFragment.this);
                transaction.add(R.id.container, mainPanelFrag).commit();
            } else {
                getVerifyImage();
            }
            loginButton.setText(R.string.login);
        }
    }

    private class VerifyCodeTask extends AsyncTask<Void, Integer, Bitmap> {
        private VerifyCodeTask() {
        }

        protected void onPreExecute() {
            super.onPreExecute();
            verifyCodeImage.setVisibility(View.GONE);
            loadingImage.setVisibility(View.VISIBLE);
        }

        protected Bitmap doInBackground(Void... params) {
            jw = new Jiaowu();
            return jw.getVerifyCode();
        }

        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                verifyCodeImage.setImageBitmap(bitmap);
            } else {
                verifyCodeImage.setImageResource(R.drawable.click_to_refresh);
            }
            loadingImage.setVisibility(View.GONE);
            verifyCodeImage.setVisibility(View.VISIBLE);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_jiaowu_login, container, false);
        return rootView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findView();
        if (getSavedAccount()) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            Fragment mainPanelFrag = new JiaowuPanelFragment();
            transaction.remove(this);
            transaction.add(R.id.container, mainPanelFrag).commit();
            return;
        }
        getVerifyImage();
    }

    private void findView() {
        username = (EditText) rootView.findViewById(R.id.login_username);
        password = (EditText) rootView.findViewById(R.id.login_password);
        verifyCode = (EditText) rootView.findViewById(R.id.login_verifycode);
        verifyCodeImage = (ImageView) rootView.findViewById(R.id.login_verifyimage);
        loadingImage = (LinearLayout) rootView.findViewById(R.id.login_verifyimage_loading);
        loginButton = (Button) rootView.findViewById(R.id.login);
        verifyCodeImage.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                getVerifyImage();
            }
        });
        loginButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (loginButton.getText().toString().equals(getString(R.string.login))) {
                    loginTask = new LoginTask();
                    loginTask.execute(new Void[0]);
                }
            }
        });
    }

    private void getVerifyImage() {
        verifyCodeTask = new VerifyCodeTask();
        verifyCodeTask.execute(new Void[0]);
    }

    private boolean getSavedAccount() {
        SharedPreferences sp = getActivity().getSharedPreferences("account", 0);
        username.setText(sp.getString("jw_username", BuildConfig.FLAVOR));
        password.setText(sp.getString("jw_password", BuildConfig.FLAVOR));
        if (username.length() == 0 || password.length() == 0 || sp.getString("jw_name", BuildConfig.FLAVOR).length() == 0) {
            return false;
        }
        return true;
    }

    private void saveAccount() {
        Editor editor = getActivity().getSharedPreferences("account", 0).edit();
        editor.putString("jw_username", username.getText().toString());
        editor.putString("jw_password", password.getText().toString());
        editor.putString("jw_session_id", Jiaowu.getJsessionId());
        editor.putLong("jw_lasttime", System.currentTimeMillis());
        editor.putString("jw_name", Jiaowu.getName());
        editor.commit();
    }
}
