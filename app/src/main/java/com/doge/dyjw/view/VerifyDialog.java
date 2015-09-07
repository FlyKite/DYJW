package com.doge.dyjw.view;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
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

        private String verifycodeStr = "";
        protected void onPreExecute() {
            super.onPreExecute();
            verifyCodeImage.setVisibility(View.GONE);
            loadingImage.setVisibility(View.VISIBLE);
        }

        protected Bitmap doInBackground(Void... params) {
            jw = ((MainApplication)context.getApplicationContext()).getJiaowu();
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

    public interface LoginCallback {
        public void fail();
        public void success();
    }


}
