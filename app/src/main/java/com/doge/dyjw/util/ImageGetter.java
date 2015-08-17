package com.doge.dyjw.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.doge.dyjw.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by 政 on 2015/5/10.
 */
public class ImageGetter {

    public static Bitmap get(String imageUrl) {
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/"));
        File image = new File(MainActivity.IMAGE_DIR + fileName);
        if (image.exists()) {
            Log.d("GetImage", "从本地获取图片" + imageUrl);
            return fromFile(image);
        } else {
            Log.d("GetImage", "从服务器获取图片" + imageUrl);
            return fromUrl(imageUrl);
        }
    }

    public static boolean exists(String imageUrl) {
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/"));
        File image = new File(MainActivity.IMAGE_DIR + fileName);
        if (image.exists()) {
            return true;
        } else {
            return false;
        }
    }

    private static Bitmap fromUrl(String imageUrl) {
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/"));
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(imageUrl).openConnection();
            conn.setConnectTimeout(6000);//设置超时
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();//获得图片的数据流
            File image = new File(MainActivity.IMAGE_DIR + fileName);
            Log.d("GetImage", "保存图片到" + image.getAbsolutePath());
            if (!image.exists()) {
                image.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(image);
            byte buf[] = new byte[4 * 1024];
            int numRead = is.read(buf);
            while (numRead > 0) {
                fos.write(buf, 0, numRead);
                numRead = is.read(buf);
            }
            fos.close();
            is.close();
            return fromFile(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Bitmap fromFile(File image) {
        Bitmap bitmap = null;
        try {
            FileInputStream fis = new FileInputStream(image);
            bitmap = BitmapFactory.decodeStream(fis);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
