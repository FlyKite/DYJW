package com.doge.dyjw.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.doge.dyjw.R;
import com.doge.dyjw.R.drawable;
import com.doge.dyjw.R.string;
import com.doge.dyjw.util.ImageGetter;

public class NetImageView extends LinearLayout{

	private String url;
	private ImageView img;
	private TextView t;
	
	private Context context;

    public NetImageView(Context context) {
        this(context, null);
    }
	
	public NetImageView(Context context, String url) {
		super(context);
		this.url = url;
		this.context = context;
		setOrientation(LinearLayout.VERTICAL);
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		setGravity(Gravity.CENTER);
		DisplayMetrics metrics = new DisplayMetrics();
		((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, metrics);
		img = new ImageView(context);
		img.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		ViewGroup.LayoutParams params = img.getLayoutParams();
		params.width = (int)width;
		params.height = (int)width;
		img.setLayoutParams(params);
		img.setImageResource(drawable.image);
		img.setScaleType(ScaleType.FIT_CENTER);
		addView(img);
		t = new TextView(context);
		t.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		t.setText(context.getString(string.click_to_load_image));
		addView(t);
		setBackgroundResource(drawable.corner_view);
		setOnClickListener(new LoadListener());
        if(ImageGetter.exists(url)) {
            bitmap = ImageGetter.get(url);
            setImage();
        }
	}

    public void setImageUrl(String url) {
        this.url = url;
    }
	
	private Bitmap bitmap = null;
	NetTask task = null;
	class LoadListener implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			if(task != null) return;
			task = new NetTask();
			task.execute();
		}
	}
	
	class NetTask extends AsyncTask<Void, Integer, Boolean> {
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			t.setText(context.getString(string.loading));
		}

		@Override
		protected Boolean doInBackground(Void... arg0) {
            bitmap = ImageGetter.get(url);
            return bitmap != null;
        }

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
            if(result) {
                setImage();
            } else {
                t.setText(context.getString(string.load_failed));
            }
			task = null;
		}
	}

    private void setImage() {
        t.setVisibility(View.GONE);
        setBackgroundColor(getResources().getColor(R.color.window_background_light));
        img.setImageBitmap(bitmap);
        img.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        setOnClickListener(null);
    }

}
