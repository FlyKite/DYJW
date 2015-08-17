package com.doge.dyjw.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.Html;
import android.text.Spanned;
import android.widget.Toast;

import com.doge.dyjw.MainActivity;
import com.doge.dyjw.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class Update {

    private Context context;
    private ProgressDialog updateDialog;

	private int nowVersionCode = 0;
	
	private int newVersionCode = 0;
	private String newVersionName = "";
	private String url = "";
	private String contents = "";
	private long length = 0;
	
	public Update(Context context) {
        this.context = context;
	}

    private boolean isWorking = false;
    private boolean startByUser = false;
    public void startUpdate() {
        if(!isWorking) {
            Log.d("CheckUpdate", "检测更新");
            new UpdateInfoTask().execute();
            isWorking = true;
        } else {
            Log.d("CheckUpdate", "已经在检测更新");
        }
    }

    public void startUpdateByUser() {
        startByUser = true;
        startUpdate();
    }

    private class UpdateInfoTask extends AsyncTask<Void, Integer, Boolean> {
        private boolean new_user = true;

        @Override
        protected void onPreExecute() {
            Log.d("CheckUpdate", "判断是否需要检查更新（距离上次检测是否超过一小时）");
            SharedPreferences sp = context.getSharedPreferences("settings", Activity.MODE_PRIVATE);
            new_user = sp.getBoolean("new_user", true);
            if (System.currentTimeMillis() - sp.getLong("check_date", 0) < 3600000) {
                isWorking = false;
                cancel(true);
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.d("Update","getUpdateInfo");
            // 获取当前版本信息
            PackageInfo pi = null;
            try {
                pi = context.getPackageManager().getPackageInfo("com.doge.dyjw", 0);
                nowVersionCode = pi.versionCode;
                String param = new_user ? "?new_user=1" : "";
                Connection con = Jsoup.connect(context.getString(R.string.server) +
                        "app/update.aspx" + param).timeout(15000);
                Document doc = con.get();
                Spanned json = Html.fromHtml(doc.toString());
                Log.v("Update", "updateInfo=\n" + json);
                JSONObject jo = new JSONObject(json + "");
                newVersionCode = jo.getInt("versionCode");
                newVersionName = jo.getString("versionName");
                url = jo.getString("url");
                contents = jo.getString("contents");
                length = jo.getLong("length");
                return true;
            } catch (JSONException | NameNotFoundException | IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                final SharedPreferences sp = context.getSharedPreferences("settings", Activity.MODE_PRIVATE);
                if (new_user) {
                    sp.edit().putBoolean("new_user", false).commit();
                }
                if (newVersionCode > nowVersionCode) {
                    Log.d("CheckUpdate", "检测到新版本");
                    sp.edit().putLong("check_date", 0).commit();
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.update)
                            .setMessage(context.getString(R.string.update_new)
                                    + newVersionName
                                    + context.getString(R.string.update_contents)
                                    + contents
                                    + context.getString(R.string.update_or_not))
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new DownloadTask().execute();
                                }
                            })
                            .setNeutralButton(R.string.no_alert, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d("CheckUpdate", "不再提醒（一年后提醒）");
                                    isWorking = false;
                                    sp.edit().putLong("check_date", System.currentTimeMillis() +
                                            31536000000L).commit();
                                }
                            })
                            .setNegativeButton(R.string.no, null)
                            .setCancelable(false)
                            .show();
                } else {
                    isWorking = false;
                    sp.edit().putLong("check_date", System.currentTimeMillis()).commit();
                    if(startByUser) {
                        Toast.makeText(context, context.getString(R.string.is_newest_version), Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                Log.d("CheckUpdate", "获取新版本信息失败");
                isWorking = false;
                Toast.makeText(context, context.getString(R.string.check_update_failed), Toast.LENGTH_LONG).show();
            }
        }
    }

    private class DownloadTask extends AsyncTask<Void, Integer, String> {

        @Override
        protected void onPreExecute() {
            // 初始化进度条
            updateDialog = new ProgressDialog(context, AlertDialog.THEME_HOLO_LIGHT);
            updateDialog.setMessage(context.getString(R.string.downloading));
            updateDialog.setCanceledOnTouchOutside(false);
            updateDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    isWorking = false;
                    cancel(true);
                }
            });
            updateDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            updateDialog.setProgress(0);
            updateDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            Log.d("DownloadUpdate", "开始下载新版APP");
            String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
            File dldir = new File(sdcard + MainActivity.APK_DIR);
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);
            try {
                File myTempFile = new File(dldir + "/dyjw_" + newVersionName + ".apk");
                HttpResponse response = client.execute(request);
                if (myTempFile.exists()) {
                    myTempFile.delete();
                }
                myTempFile.createNewFile();
                InputStream is = response.getEntity().getContent();
                RandomAccessFile fos = new RandomAccessFile(myTempFile, "rw");
                byte[] buf = new byte[1024];
                int numread = is.read(buf);
                long downloaded_size = (long) numread;
                long time1 = System.currentTimeMillis();
                while (numread > 0) {
                    fos.write(buf, 0, numread);
                    numread = is.read(buf);
                    downloaded_size += (long) numread;
                    if (System.currentTimeMillis() - time1 > 50) {
                        int progress = (int) ((100 * downloaded_size) / length);
                        publishProgress(progress);
                    }
                }
                is.close();
                return myTempFile.getAbsolutePath();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            updateDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            updateDialog.dismiss();
            isWorking = false;
            if (result != null) {
                Log.d("DownloadUpdate", "新版下载完毕");
                Intent intent = new Intent("android.intent.action.VIEW");
                intent.setDataAndType(Uri.fromFile(new File(result)), "application/vnd.android.package-archive");
                context.startActivity(intent);
                return;
            }
            Log.d("DownloadUpdate", "下载失败");
            Toast.makeText(context, context.getString(R.string.update_failed), Toast.LENGTH_LONG).show();
        }
    }

}
