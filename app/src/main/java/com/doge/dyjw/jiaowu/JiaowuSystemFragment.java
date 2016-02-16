package com.doge.dyjw.jiaowu;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
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

import com.doge.dyjw.HolderFragment;
import com.doge.dyjw.MainApplication;
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
                jw.setAccount(xh, pwd);
                if (jw.loginJW(verify, getActivity())) {
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
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
        private String verifycodeStr = "";

        protected void onPreExecute() {
            super.onPreExecute();
            verifyCodeImage.setVisibility(View.GONE);
            loadingImage.setVisibility(View.VISIBLE);
        }

        protected Bitmap doInBackground(Void... params) {
            jw = ((MainApplication)getActivity().getApplicationContext()).getJiaowu();
            Bitmap bitmap = jw.getVerifyCode();
            if (bitmap != null) {
                Bitmap vcBitmap = Bitmap.createBitmap(45, 12, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(vcBitmap);
                final Paint paint = new Paint();
                ColorMatrix cm = new ColorMatrix();
                cm.setSaturation(0);
                ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
                paint.setColorFilter(f);
                canvas.drawBitmap(bitmap, -3, -4, paint);

                int[][] colors = new int[12][45];
                for (int y = 0; y < 12; y++) {
                    for (int x = 0; x < 45; x++) {
                        colors[y][x] = vcBitmap.getPixel(x, y) < -8000000 ? 1 : 0;
                    }
                }
                // 已知字符及该字符的二进制表示
                String[][] letters = {
                        {"b", "111111111111111111111111000011000110000110000011000110000011000111000111000011111110000001111100"},
                        {"c", "000001111100000011111110000111000111000110000011000110000011000111000111000011000100"},
                        {"m", "000111111111000111111111000011000000000110000000000110000000000111111111000011111111000011000000000110000000000110000000000111111111000011111111"},
                        {"n", "000111111111000111111111000011000000000110000000000110000000000110000000000111111111000011111111"},
                        {"v", "000111000000000111111000000000111111000000000111000000111111000111111000000111000000"},
                        {"x", "000110000011000111000111000011111110000000111000000011111110000111000111000110000011"},
                        {"z", "000110000011000110001111000110011111000110111011000111110011000111100011000110000011"},
                        {"1", "000110000000001100000000011000000000111111111111111111111111"},
                        {"2", "001100000011011100000111111000001111110000011011110000111011110001110011011111100011001110000011"},
                        {"3", "001000001100011000001110110000000111110011000011110011000011110011100111111111111110011100111100"}
                };
                // 各个字母每一列像素点的比特值
                int[][] nums ={
                        {4095,4095,1584,3096,3096,3640,2032,992},
                        {992,2032,3640,3096,3096,3640,560},
                        {4088,4088,48,24,24,4088,4080,48,24,24,4088,4080},
                        {4088,4088,48,24,24,24,4088,4080},
                        {56,504,4032,3584,4032,504,56},
                        {3096,3640,2032,448,2032,3640,3096},
                        {3096,3864,3992,3544,3320,3192,3096},
                        {24,12,6,4095,4095},
                        {3084,3598,3847,3459,3523,3299,3198,3100},
                        {772,1798,3587,3123,3123,3699,2047,974}
                };
                String vcode = "";
                for (int x = 0; x < 45; x++) {
                    // 从每一列开始往后比较
                    for (int i = 0; i < 10; i++) {
                        // 与已知各个字符进行比较
                        if (compare(colors, x, nums[i])) {
                            vcode += letters[i][0];
                            System.out.print(letters[i][0]);
                            break;
                        }
                    }
                }
                this.verifycodeStr = vcode;
                System.out.println(vcode);
            }
            return bitmap;
        }

        private boolean compare(int[][] source, int position, int[] dest) {
            for (int x = position; x < position + dest.length - 1; x++) {
                // 计算图片当前列的像素点的比特值
                int lineValue = 0;
                int binBit = 1;
                for (int y = 0; y < 12; y++) {
                    lineValue += source[y][x] == 1 ? binBit : 0;
                    binBit *= 2;
                }
                // 进行按位与运算,若结果与当前字母当前列相等则可能为该字母,进行详细比较
                if ((lineValue & dest[x-position]) != dest[x-position]) {
                    // 若某一列不相等则不可能为该字符
                    return false;
                }
            }
            return true;
        }

        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                verifyCodeImage.setImageBitmap(bitmap);
                verifyCode.setText(verifycodeStr);
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
        jw = ((MainApplication)getActivity().getApplicationContext()).getJiaowu();
        if (jw.getXuehao().length() != 0 && jw.getPwd().length() != 0 && jw.getJsessionId().length() != 0) {
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
                    loginTask.execute();
                }
            }
        });
    }

    private void getVerifyImage() {
        VerifyCodeTask verifyCodeTask = new VerifyCodeTask();
        verifyCodeTask.execute();
    }

    private void saveAccount() {
        Editor editor = getActivity().getSharedPreferences("account", 0).edit();
        editor.putString("jw_username", username.getText().toString());
        editor.putString("jw_password", password.getText().toString());
        editor.putString("jw_session_id", jw.getJsessionId());
        editor.putLong("jw_lasttime", System.currentTimeMillis());
        editor.putString("jw_name", jw.getName());
        editor.commit();
    }
}
