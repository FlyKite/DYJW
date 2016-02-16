package com.doge.dyjw;

import android.app.AlertDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.doge.dyjw.util.DBHelper;
import com.doge.dyjw.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link HolderFragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 */
public class MainCourseFragment extends HolderFragment {

    View rootView;
    List<Map<String, String>> mapList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main_course, container, false);
        initWeekdays();
        initGridView(getCourseFromDatabase());
        return rootView;
    }


    private void initWeekdays() {
        LinearLayout weekDays = (LinearLayout) rootView.findViewById(R.id.course_head);
        String[] days = getResources().getStringArray(R.array.weekdays);
        for (int i = 0; i < 7; i++) {
            TextView tv = new TextView(getActivity());
            tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1.0F));
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(getResources().getColor(R.color.text_white));
            tv.setText(days[i]);
            weekDays.addView(tv);
        }
    }

    private void initGridView(boolean isCourseSaved) {
        if (!isCourseSaved) return;
        rootView.findViewById(R.id.no_course).setVisibility(View.GONE);
        SimpleAdapter adapter = new SimpleAdapter(getActivity(), mapList, R.layout.course_grid,
                new String[]{"course_name", "course_room", "course_id"},
                new int[]{R.id.course_name, R.id.course_room, R.id.course_id});
        GridView courseView = (GridView) rootView.findViewById(R.id.course);
        courseView.setAdapter(adapter);
        courseView.setOnItemClickListener(new InfoListener());
    }

    class InfoListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String course_id = ((TextView) view.findViewById(R.id.course_id)).getText().toString();
            SQLiteDatabase db = new DBHelper(getActivity(), "course.db").getReadableDatabase();
            Cursor c = db.rawQuery("select * from " + tb_name + " where id=?", new String[] {course_id});
            if(c.moveToFirst()) {
                String info = "课程：" + c.getString(0) + "\n";
                info += "班级：" + c.getString(1) + "\n";
                info += "教师：" + c.getString(2) + "\n";
                info += "时间：" + c.getString(3) + "\n";
                info += "教室：" + c.getString(4);
                new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.course_info)
                    .setMessage(info)
                    .show();
            }
            c.close();
            db.close();
        }
    }

    private String tb_name = "";
    private boolean getCourseFromDatabase() {
        SQLiteDatabase db = new DBHelper(getActivity(), "course.db").getReadableDatabase();
        Date d = new Date(System.currentTimeMillis());
        int month = d.getMonth() + 1;
        int date = d.getDate();
        int year = d.getYear() + 1900;
        int term;
        Log.v("Course", year +"-"+month+"-"+date);
        if (month > 8 || (month == 8 && date >= 10)) {
            term = 1;
        } else {
            year -= 1;
            if (month == 1 || (month == 2 && date <= 10)) {
                term = 1;
            } else {
                term = 2;
            }
        }
        tb_name = "course_" + year + "_" + (year + 1) + "_" + term;
        try {
            Cursor c = db.rawQuery("select * from " + tb_name + " order by id asc", null);
            c.moveToFirst();
            mapList = new ArrayList<>();
            for (int i = 0; i < 42; i++) {
                HashMap<String, String> map = new HashMap<>();
                if (c.getInt(5) == i) {
                    map.put("course_name", c.getString(0));
                    String room = c.getString(4);
                    if (room == null) {
                        room = c.getString(3);
                        int start = room.indexOf("]") + 1;
                        if (start >= 0 && start < room.length()) room = room.substring(start);
                    }
                    map.put("course_room", room.replace("楼", ""));
                    map.put("course_id", c.getString(5));
                    mapList.add(map);
                    if (!c.moveToNext()) {
                        for (int j = i + 1; j < 42; j++) {
                            HashMap<String, String> map2 = new HashMap<>();
                            mapList.add(map2);
                        }
                        return true;
                    }
                } else {
                    mapList.add(map);
                }
            }
            c.close();
            db.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
