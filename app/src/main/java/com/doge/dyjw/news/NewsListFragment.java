package com.doge.dyjw.news;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.doge.dyjw.ContainerActivity;
import com.doge.dyjw.R;
import com.doge.dyjw.util.DBHelper;
import com.doge.dyjw.util.Log;
import com.doge.dyjw.view.RefreshLayout;
import com.doge.dyjw.view.RefreshLayout.OnLoadListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewsListFragment extends Fragment {
    private static final String PATH = "path";
    private static NewsListFragment[] fragment = new NewsListFragment[13];
    SimpleAdapter adapter;
    LayoutInflater inflater;
    ListView list;
    List<Map<String, Object>> mapList = null;
    int page = 1;
    private String path;
    View rootView;
    RefreshLayout swipeRefreshLayout;

    public static NewsListFragment newInstance(int position, String path) {
        if (fragment[position] != null) {
            return fragment[position];
        }
        fragment[position] = new NewsListFragment();
        Bundle args = new Bundle();
        args.putString(PATH, path);
        fragment[position].setArguments(args);
        return fragment[position];
    }

    public static void releaseFragments() {
        fragment = new NewsListFragment[13];
        for (int i = 0; i < 13; i++) {
            fragment[i] = null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) return;
        if (getArguments() != null) {
            path = getArguments().getString(PATH);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        rootView = inflater.inflate(R.layout.fragment_swipe_to_load, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        swipeRefreshLayout = (RefreshLayout) rootView.findViewById(R.id.swipe_container);
        list = (ListView) rootView.findViewById(R.id.list);
        initRefreshLayout();
        initSQLite();
    }

    private void initRefreshLayout() {
        swipeRefreshLayout.setColorSchemeResources(R.color.blue_main);
        swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            public void onRefresh() {
                mapList = null;
                page = 1;
                new NewsTask().execute();
            }
        });
        swipeRefreshLayout.setOnLoadListener(new OnLoadListener() {
            public void onLoad() {
                page++;
                new NewsTask().execute();
            }
        });
    }

    private void initSQLite() {
        DBHelper dbh = new DBHelper(getActivity(), "news_list.db");
        SQLiteDatabase db = dbh.getWritableDatabase();
        db.execSQL("create table if not exists news_" +
                path.substring(0, path.indexOf(".")) +
                "(top varchar(16)," +
                " title varchar(256)," +
                " url varchar(256) primary key," +
                " pubDate varchar(64))");
        db.close();
        dbh.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            list.setAdapter(adapter);
            Log.d("NewsListFragment", "onResume-setAdapter" + "---" + path);
        } else {
            loadData();
            Log.d("NewsListFragment", "onResume-loadData" + "---" + path);
        }
    }

    public void loadData() {
        if (mapList == null) {
            Log.d("NewsListFragment", "loadData:path=" + path + "&page=" + page);
            setFromDatabase();
            new NewsTask().execute();
            swipeRefreshLayout.setRefreshing(true);
        }
    }

    private class NewsListener implements OnItemClickListener {

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Intent intent = new Intent();
            intent.setClass(getActivity(), ContainerActivity.class);
            if(path.charAt(0) == '5') {
                intent.putExtra("titleId", R.string.news);
            } else {
                intent.putExtra("titleId", R.string.notice);
            }
            intent.putExtra("url", ((TextView) view.findViewById(R.id.url)).getText().toString());
            startActivity(intent);
        }
    }

    class NewsTask extends AsyncTask<Void, Integer, List<Map<String, Object>>> {

        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("GetNewsList", "path" + path + ",page=" + page);
            swipeRefreshLayout.setRefreshing(true);
        }

        protected List<Map<String, Object>> doInBackground(Void... arg0) {
            try {
                return new News(path).getNewsList(page);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(List<Map<String, Object>> mList) {
            super.onPostExecute(mList);
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setLoading(false);
            if (mList == null) {
                Log.d("GetNewsList", "网络出错" + "---" + path);
                Toast.makeText(getActivity(), R.string.load_failed, Toast.LENGTH_SHORT).show();
            } else if (getActivity() != null) {
                if (mList.size() == 0) {
                    Log.d("GetNewsList", "已到达最后一页，没有更多了" + "---" + path);
                    Toast.makeText(getActivity(), R.string.last_page, Toast.LENGTH_SHORT).show();
                } else if (mapList == null) {
                    mapList = mList;
                    saveToDatabase(mList);
                    Log.d("GetNewsList", "第一次加载到数据setList" + "---" + path);
                    setList(mapList);
                } else {
                    mapList.addAll(mList);
                    adapter.notifyDataSetChanged();
                    Log.d("GetNewsList", "数据追加到当前list中" + "---" + path);
                }
            }
        }
    }

    private void setFromDatabase() {
        Log.d("NewsListFragment", "setFromDatabase" + "---" + path);
        DBHelper dbh = new DBHelper(getActivity(), "news_list.db");
        SQLiteDatabase db = dbh.getWritableDatabase();
        Cursor c = db.rawQuery("select * from news_" +
                path.substring(0, path.indexOf(46)) +
                " order by pubDate desc limit 0,15", new String[]{});
        if (c.getCount() == 15 && c.moveToFirst()) {
            List<Map<String, Object>> mapList = new ArrayList<>();
            do {
                Map<String, Object> map = new HashMap<>();
                map.put("top", c.getString(0));
                map.put("title", c.getString(1));
                map.put("url", c.getString(2));
                map.put("bottom", c.getString(3));
                mapList.add(map);
            } while (c.moveToNext());
            setList(mapList);
        }
        c.close();
        db.close();
        dbh.close();
    }

    private void saveToDatabase(List<Map<String, Object>> mapList) {
        DBHelper dbh = new DBHelper(getActivity(), "news_list.db");
        SQLiteDatabase db = dbh.getWritableDatabase();
        String insert = "insert or ignore into news_" +
                path.substring(0, path.indexOf(46)) +
                " values(?,?,?,?)";
        for (Map<String, Object> map : mapList) {
            db.execSQL(insert, new String[]{ map.get("top").toString(),
                    map.get("title").toString(),
                    map.get("url").toString(),
                    map.get("bottom").toString()});
        }
        db.close();
        dbh.close();
    }

    private void setList(List<Map<String, Object>> mapList) {
        adapter = new SimpleAdapter(getActivity(), mapList, R.layout.list_item_news,
                new String[]{"top", "title", "url", "bottom"},
                new int[]{R.id.top, R.id.title, R.id.url, R.id.bottom});
        list.setAdapter(adapter);
        list.setOnItemClickListener(new NewsListener());
    }
}
