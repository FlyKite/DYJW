package com.doge.dyjw.view;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.doge.dyjw.MainApplication;
import com.doge.dyjw.R;
import com.doge.dyjw.jiaowu.Jiaowu;

import java.io.IOException;

public class VerifyDialog extends Builder {
    private LoginCallback callback;
    private Context context;
    private Jiaowu jw;
    private LinearLayout loadingImage;
    private EditText verifyCode;
    private ImageView verifyCodeImage;
    VerifyCodeTask verifyCodeTask;

    public VerifyDialog(Context context, LayoutInflater inflater, LoginCallback callback) {
        super(context);
        this.context = context;
        this.callback = callback;
        View view = inflater.inflate(R.layout.alert_verifycode, null);
        setView(view);
        setTitle(R.string.need_verify_code);
        verifyCode = (EditText) view.findViewById(R.id.login_verifycode);
        verifyCodeImage = (ImageView) view.findViewById(R.id.login_verifyimage);
        loadingImage = (LinearLayout) view.findViewById(R.id.login_verifyimage_loading);
        verifyCodeImage.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                getVerifyImage();
            }
        });
        setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                new LoginTask().execute();
            }
        });
        setNegativeButton(R.string.cancel, null);
        getVerifyImage();
    }

    private void getVerifyImage() {
        verifyCodeTask = new VerifyCodeTask();
        verifyCodeTask.execute();
    }

    public class LoginTask extends AsyncTask<Void, Integer, Boolean> {
        ProgressDialog dialog;
        private String verify;

        protected void onPreExecute() {
            super.onPreExecute();
            verify = verifyCode.getText().toString();
            dialog = new ProgressDialog(getContext());
            dialog.setMessage(getContext().getString(R.string.loging));
            dialog.show();
        }

        protected Boolean doInBackground(Void... param) {
            System.out.println("LoginTask-----------------------------------");
            try {
                if (jw.loginJW(verify, context)) {
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {
            dialog.hide();
            dialog.dismiss();
            if (result) {
                callback.success();
            } else {
                callback.fail();
            }
        }
    }

    private class VerifyCodeTask extends AsyncTask<Void, Integer, Bitmap> {

        protected void onPreExecute() {
            super.onPreExecute();
            verifyCodeImage.setVisibility(View.GONE);
            loadingImage.setVisibility(View.VISIBLE);
        }

        protected Bitmap doInBackground(Void... params) {
            jw = ((MainApplication)context.getApplicationContext()).getJiaowu();
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

    public interface LoginCallback {
        public void fail();
        public void success();
    }
}
