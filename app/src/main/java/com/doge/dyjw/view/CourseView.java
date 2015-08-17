package com.doge.dyjw.view;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class CourseView extends ScrollView {
	
	private TextView[][] gezi = new TextView[6][7];
	private HorizontalScrollView hScroll;
	private TableLayout kebiao;
	private Button refresh;
	private TableRow[] row = new TableRow[6];
	private LinearLayout updateLayout;
	private TextView update_date;
	private LinearLayout vLayout;
	
	public CourseView(Context context, DisplayMetrics paramDisplayMetrics) {
		super(context);
		//垂直滚动的ScrollView
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	    setVerticalScrollBarEnabled(false);
	    
//	    vLayout = new LinearLayout(getContext());
//	    vLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
//	    vLayout.setOrientation(1);
//	    addView(vLayout);
//	    
//	    updateLayout = new LinearLayout(getContext());
//	    updateLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
//	    updateLayout.setOrientation(LinearLayout.HORIZONTAL);
//	    updateLayout.setGravity(Gravity.RIGHT);
//	    
//	    update_date = new TextView(updateLayout.getContext());
//	    updateLayout.addView(update_date);
//	    
//	    refresh = new Button(updateLayout.getContext());
//	    refresh.setTextSize(2, 12.0F);
//	    refresh.setGravity(17);
//	    refresh.setText("点击刷新");
//	    refresh.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30.0F, paramDisplayMetrics)));
//	    refresh.setBackgroundResource(R.drawable.corner_view);
//	    updateLayout.addView(refresh);
//	    vLayout.addView(updateLayout);
	    
		//hScroll为水平滚动的ScrollView
	    hScroll = new HorizontalScrollView(getContext());
	    hScroll.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	    addView(hScroll);
	    
	    //kebiao为放课程信息的TableLayout
	    kebiao = new TableLayout(hScroll.getContext());
	    kebiao.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	    hScroll.addView(kebiao);
	    
	    TableRow r_weekday = new TableRow(kebiao.getContext());
	    r_weekday.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	    for(int i = 0; i < 8; i++) {
	    	TextView t = new TextView(r_weekday.getContext());
	    	t.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	    	if(i > 0) t.setMinWidth((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100.0F, paramDisplayMetrics));
	    	t.setGravity(Gravity.CENTER);
	    	if(i % 2 == 0) {
	    		t.setBackgroundColor(Color.rgb(255, 255, 255));
	    	} else {
	    		t.setBackgroundColor(Color.rgb(224, 224, 224));
	    	}
	    	String text = "";
	    	switch(i) {
	    	case 1: text = "星期一"; break;
	    	case 2: text = "星期二"; break;
	    	case 3: text = "星期三"; break;
	    	case 4: text = "星期四"; break;
	    	case 5: text = "星期五"; break;
	    	case 6: text = "星期六"; break;
	    	case 7: text = "星期日"; break;
	    	}
	    	t.setText(text);
	    	r_weekday.addView(t);
	    }
		kebiao.addView(r_weekday);
	    
	    for(int i = 0;  i < 6; i++) {
	    	row[i] = new TableRow(kebiao.getContext());
	    	row[i].setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	    	TextView t = new TextView(r_weekday.getContext());
	    	t.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	    	t.setMinHeight((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100.0F, paramDisplayMetrics));
	    	t.setGravity(Gravity.CENTER);
	    	if(i % 2 == 1) {
	    		t.setBackgroundColor(Color.rgb(255, 255, 255));
	    	} else {
	    		t.setBackgroundColor(Color.rgb(224, 224, 224));
	    	}
	    	String text = "";
	    	switch(i) {
	    	case 0: text = "01\n02\n节"; break;
	    	case 1: text = "03\n04\n节"; break;
	    	case 2: text = "05\n06\n节"; break;
	    	case 3: text = "07\n08\n节"; break;
	    	case 4: text = "09\n10\n节"; break;
	    	case 5: text = "11\n12\n节"; break;
	    	}
	    	t.setText(text);
	    	row[i].addView(t);
	    	for(int j = 0; j < 7; j++) {
	    		gezi[i][j] = new TextView(row[i].getContext());
	    		gezi[i][j].setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		    	gezi[i][j].setGravity(Gravity.CENTER);
		    	if((i + j) % 2 == 0) {
		    		gezi[i][j].setBackgroundColor(Color.rgb(255, 255, 255));
		    	} else {
		    		gezi[i][j].setBackgroundColor(Color.rgb(224, 224, 224));
		    	}
		    	row[i].addView(gezi[i][j]);
	    	}
	    	kebiao.addView(row[i]);
	    }
	}
	
	public void setCourse(String[][] course) {
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 7; j++) {
				gezi[i][j].setText(course[i][j]);
			}
		}
	}

//	  public TextView[][] getGeZi()
//	  {
//	    return gezi;
//	  }
//
//	  public ScrollView getMainView()
//	  {
//	    return vScroll;
//	  }
//
//	  public Button getRefresh()
//	  {
//	    return refresh;
//	  }
//
//	  public TextView getUpdate_date()
//	  {
//	    return update_date;
//	  }
//
//	  public void setCourse(String[][][] paramArrayOfString1, String[][][] paramArrayOfString2, String[][][] paramArrayOfString3, String[][][] paramArrayOfString4, String paramString)
//	  {
//	    int j;
//	    for (int i = 0; ; i++)
//	    {
//	      if (i >= 7)
//	      {
//	        update_date.setText("该课表更新于" + paramString);
//	        setVisibility(0);
//	        return;
//	      }
//	      j = 0;
//	      if (j < 6)
//	        break;
//	    }
//	    String str1 = "";
//	    int k = 0;
//	    if (k >= 4);
//	    int m;
//	    int n;
//	    label100: 
//	    do
//	    {
//	      gezi[(i + 1)][(j + 1)].setText(str1);
//	      j++;
//	      break;
//	      m = 0;
//	      n = 0;
//	      if (n < 4)
//	        break label315;
//	    }
//	    while (paramArrayOfString1[i][j][k].equals(""));
//	    String str2;
//	    if (k != 0)
//	      str2 = str1 + "\n";
//	    for (int i1 = 0; ; i1++)
//	    {
//	      if (i1 > m * 2)
//	      {
//	        str1 = str2 + "\n";
//	        str1 = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(str1)).append(paramArrayOfString1[i][j][k]).toString())).append("\n").append(paramArrayOfString2[i][j][k]).toString())).append("\n").append(paramArrayOfString3[i][j][k]).toString() + "\n" + paramArrayOfString4[i][j][k];
//	        k++;
//	        break;
//	        label315: if (paramArrayOfString1[i][j][n].length() > m)
//	          m = paramArrayOfString1[i][j][n].length();
//	        n++;
//	        break label100;
//	      }
//	      str2 = str2 + "-";
//	    }
//	  }
//
//	  public void setVisibility(int paramInt)
//	  {
//	    vScroll.setVisibility(paramInt);
//	  }
}
