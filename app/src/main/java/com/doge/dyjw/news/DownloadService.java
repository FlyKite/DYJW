package com.doge.dyjw.news;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.doge.dyjw.MainActivity;
import com.doge.dyjw.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class DownloadService extends Service {
    private Binder binder;

    public DownloadService() {
        binder = new DownloadBinder();
    }

    ArrayList<DownloadThread> downloadThreads = new ArrayList<>();
    public boolean download(String url, String filename) {
        File file = new File(MainActivity.DOWNLOAD_DIR + "/" + filename);
        File fileTmp = new File(MainActivity.DOWNLOAD_DIR + "/" + filename + ".tmp");
        if(file.exists() || fileTmp.exists()) {
            Toast.makeText(this, R.string.file_exists, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            DownloadThread thread = new DownloadThread(url, filename,progressListener);
            thread.start();
            downloadThreads.add(thread);
            Toast.makeText(this, R.string.download_started, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void continueDownload(String filename) {
        File fileTmp = new File(MainActivity.DOWNLOAD_DIR + "/" + filename + ".tmp");
        File fileProperty = new File(MainActivity.DOWNLOAD_DIR + "/" + filename + ".tfp");
        if(fileTmp.exists() && fileProperty.exists()) {
            String url = null;
            try {
                Scanner sc = new Scanner(fileProperty);
                String json = sc.nextLine();
                JSONObject jo = new JSONObject(json);
                url = jo.getString("url");
                int filesize = jo.getInt("filesize");
                DownloadThread thread = new DownloadThread(url, filename, progressListener, filesize);
                thread.start();
                downloadThreads.add(thread);
            } catch (FileNotFoundException | JSONException e) {
                fileTmp.delete();
                fileProperty.delete();
                if(url != null) {
                    Toast.makeText(this, R.string.file_destroyed_redownload, Toast.LENGTH_LONG).show();
                    download(url, filename);
                } else {
                    Toast.makeText(this, R.string.file_destroyed_delete, Toast.LENGTH_LONG).show();
                }
                e.printStackTrace();
            }
        } else {
            fileTmp.delete();
            fileProperty.delete();
            Toast.makeText(this, R.string.file_destroyed_delete, Toast.LENGTH_LONG).show();
        }
    }

    public void stopDownload(String filename) {
        for(DownloadThread thread : downloadThreads) {
            if(thread.filename.equals(filename)) {
                thread.stopDownload();
                downloadThreads.remove(thread);
                break;
            }
        }
    }

    public boolean isDownloading(String filename) {
        for(DownloadThread thread : downloadThreads) {
            if(thread.filename.equals(filename)) {
                return thread.downloading;
            }
        }
        return false;
    }

    DownloadThread.DownloadProgressListener progressListener = new DownloadThread.DownloadProgressListener() {
        @Override
        public void finish(String filename) {
            for(DownloadThread thread : downloadThreads) {
                if(thread.filename.equals(filename)) {
                    showMsg(filename + getString(R.string.download_finish));
                    downloadThreads.remove(thread);
                    File fileProperty = new File(MainActivity.DOWNLOAD_DIR + "/" + filename + ".tfp");
                    if(fileProperty.exists()) fileProperty.delete();
                    Intent intent = new Intent(getString(R.string.broadcast_download));
                    intent.putExtra("msg", "finish");
                    intent.putExtra("filename", filename);
                    sendBroadcast(intent);
                    break;
                }
            }
        }

        @Override
        public void saveFilesize(String filename, String url, int filesize) {
            File fileProperty = new File(MainActivity.DOWNLOAD_DIR + "/" + filename + ".tfp");
            if(fileProperty.exists()) {
                fileProperty.delete();
            }
            try {
                fileProperty.createNewFile();
                PrintWriter writer = new PrintWriter(fileProperty);
                writer.println("{\"url\" : \"" + url + "\", \"filesize\" : " + filesize + "}");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void stop(String filename) {
            Intent intent = new Intent(getString(R.string.broadcast_download));
            intent.putExtra("msg", "stop");
            intent.putExtra("filename", filename);
            sendBroadcast(intent);
        }

        @Override
        public void updateProgress(String filename, double progress, String speed) {
            String speedText = ((int)(progress * 100)) + "%, " + speed;
            Intent intent = new Intent(getString(R.string.broadcast_download));
            intent.putExtra("msg", "updateProgress");
            intent.putExtra("filename", filename);
            intent.putExtra("speed", speedText);
            sendBroadcast(intent);
        }

        @Override
        public void error(String filename, String msg) {
            Intent intent = new Intent(getString(R.string.broadcast_download));
            intent.putExtra("msg", "error");
            intent.putExtra("filename", filename);
            intent.putExtra("speed", msg);
            sendBroadcast(intent);
        }
    };

    private void showMsg(String msg) {
        Message message = new Message();
        message.obj = msg;
        msgHandler.sendMessage(message);
    }
    Handler msgHandler = new Handler(new Handler.Callback(){
        @Override
        public boolean handleMessage(Message msg) {
            Toast.makeText(DownloadService.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
            return false;
        }
    });

    public class DownloadBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("start service");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("stop service");
    }
}
