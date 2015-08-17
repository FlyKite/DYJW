package com.doge.dyjw.news;

import com.doge.dyjw.MainActivity;
import com.doge.dyjw.util.Log;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by 政 on 2015/7/24.
 */
public class DownloadThread extends Thread {
    boolean downloading;
    public String filename;
    int filesize;
    DownloadProgressListener listener;
    String url = null;

    public interface DownloadProgressListener {
        public void finish(String filename);
        public void saveFilesize(String filename, String url, int filesize);
        public void stop(String filename);
        public void updateProgress(String filename, double progress, String speed);
        public void error(String filename, String msg);
    }

    public DownloadThread(String url, String filename, DownloadProgressListener listener) {
        downloading = true;
        this.url = url;
        this.filename = filename;
        this.listener = listener;
    }

    public DownloadThread(String url, String filename, DownloadProgressListener listener, int filesize) {
        downloading = true;
        this.url = url;
        this.filename = filename;
        this.listener = listener;
        this.filesize = filesize;
    }

    public void run() {
        try {
            URL downloadURL = new URL(url);
            File file = new File(MainActivity.DOWNLOAD_DIR + "/" + filename + ".tmp");
            if (!file.exists()) {
                file.createNewFile();
                HttpURLConnection sizecon = (HttpURLConnection) downloadURL.openConnection();
                sizecon.connect();
                filesize = sizecon.getContentLength();
                listener.saveFilesize(filename, url, filesize);
            }
            long dLength = file.length();
            HttpURLConnection con = (HttpURLConnection) downloadURL.openConnection();
            if (dLength != 0) {
                con.setRequestProperty("Range", "bytes=" + dLength + "-" + filesize);
            }
            InputStream is = con.getInputStream();
            RandomAccessFile fos = new RandomAccessFile(file, "rw");
            fos.seek(dLength);
            byte[] buf = new byte[1024];
            int numread = is.read(buf);
            long download_per_second = (long) numread;
            long time1 = System.currentTimeMillis();
            long downloaded_size = 0;
            while (downloading && numread > 0) {
                fos.write(buf, 0, numread);
                numread = is.read(buf);
                download_per_second += (long) numread;
                long time2 = System.currentTimeMillis();
                if (time2 - time1 > 1000) {
                    String speed = ((download_per_second / 1024) / ((time2 - time1) / 1000)) + "kb/s";
                    downloaded_size += download_per_second;
                    download_per_second = 0;
                    long progress = dLength + downloaded_size;
                    time1 = time2;
                    listener.updateProgress(filename, progress * 1.0 / filesize, speed);
                }
            }
            fos.close();
            is.close();
            if (downloading) {
                file.renameTo(new File(MainActivity.DOWNLOAD_DIR + "/" + filename));
                listener.finish(filename);
                return;
            }
            Log.v("download", "DownloadThread:stopDownload");
            listener.stop(filename);
        } catch (Exception e) {
            e.printStackTrace();
            listener.error(filename, e.getLocalizedMessage());
        }
    }

    public void stopDownload() {
        downloading = false;
    }
}
