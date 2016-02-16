package com.doge.dyjw.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.doge.dyjw.R;
import com.doge.dyjw.util.ImageGetter;

/**
 * Created by 政 on 2015/9/15.
 */
public class TradeImageView extends ImageView {

    private String imageName;
    public TradeImageView(Context context) {
        super(context);
    }

    public TradeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TradeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TradeImageView(Context context, String imageName) {
        super(context);
        setImageName(imageName);
    }

    public void setImageName(String imageName) {
        if (imageName == null) {
            return;
        }
        this.imageName = imageName;
        new ImageTask().execute();
    }

    class ImageTask extends AsyncTask<Void, Integer, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Void... arg0) {
            return ImageGetter.get(getContext().getString(R.string.server), "/lmhp/thumbs/" + imageName);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if(result != null) {
                setImageBitmap(result);
            } else {
                //t.setText(context.getString(R.string.load_failed));
                setImageResource(R.drawable.image);
            }
        }
    }

}
